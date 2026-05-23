package com.erp.system.logging;

import com.erp.common.core.domain.R;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.domain.LoginUser;
import com.erp.common.security.util.SecurityUtils;
import com.erp.system.domain.entity.SysOperationLog;
import com.erp.system.security.DataScopeContext;
import com.erp.system.security.DataScopeService;
import com.erp.system.security.FieldPermissionService;
import com.erp.system.service.SysOperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OperationLogAspect {
    private final SysOperationLogService sysOperationLogService;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest httpServletRequest;
    private final DataScopeService dataScopeService;
    private final FieldPermissionService fieldPermissionService;

    public OperationLogAspect(SysOperationLogService sysOperationLogService,
                              ObjectMapper objectMapper,
                              HttpServletRequest httpServletRequest,
                              DataScopeService dataScopeService,
                              FieldPermissionService fieldPermissionService) {
        this.sysOperationLogService = sysOperationLogService;
        this.objectMapper = objectMapper;
        this.httpServletRequest = httpServletRequest;
        this.dataScopeService = dataScopeService;
        this.fieldPermissionService = fieldPermissionService;
    }

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        int responseCode = 200;
        boolean success = true;
        String errorMessage = null;
        try {
            Object result = joinPoint.proceed();
            if (result instanceof R<?> response) {
                responseCode = response.getCode();
            }
            return result;
        } catch (BizException ex) {
            responseCode = ex.getCode();
            success = false;
            errorMessage = ex.getMessage();
            throw ex;
        } catch (Throwable ex) {
            responseCode = 10006;
            success = false;
            errorMessage = ex.getMessage();
            throw ex;
        } finally {
            writeLog(joinPoint, operationLog, responseCode, success, errorMessage, (int) (System.currentTimeMillis() - start));
        }
    }

    private void writeLog(ProceedingJoinPoint joinPoint,
                          OperationLog operationLog,
                          int responseCode,
                          boolean success,
                          String errorMessage,
                          int duration) {
        try {
            SysOperationLog log = new SysOperationLog();
            LoginUser loginUser = SecurityUtils.getLoginUser();
            DataScopeContext dataScopeContext = dataScopeService.resolveCurrentScope();
            log.setUserId(loginUser == null ? null : loginUser.getUserId());
            log.setUsername(loginUser == null ? extractUsername(joinPoint.getArgs()) : loginUser.getUsername());
            log.setModule(operationLog.module());
            log.setAction(operationLog.action());
            log.setDescription(operationLog.description());
            log.setMethod(httpServletRequest.getMethod() + " " + ((MethodSignature) joinPoint.getSignature()).toShortString());
            log.setRequestUrl(httpServletRequest.getRequestURI());
            log.setRequestParams(maskSensitive(serializeArgs(joinPoint.getArgs())));
            log.setResponseCode(responseCode);
            log.setIp(resolveIp());
            log.setDuration(duration);
            log.setTraceId(resolveTraceId());
            log.setSuccess(success);
            log.setErrorMessage(limit(errorMessage, 500));
            log.setDataScopeLevel(dataScopeContext.getLevel().name());
            log.setDataScopeSnapshot(buildDataScopeSnapshot(dataScopeContext));
            log.setFieldPermissionSnapshot(fieldPermissionService.currentPermissionSnapshot());
            log.setAuditTags(buildAuditTags(joinPoint.getArgs(), dataScopeContext));
            log.setCreatedAt(OffsetDateTime.now());
            sysOperationLogService.save(log);
        } catch (Exception ignored) {
            // Logging should never block the main business flow.
        }
    }

    private String serializeArgs(Object[] args) throws Exception {
        List<Object> payload = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) {
                payload.add(null);
                continue;
            }
            if (arg instanceof org.springframework.validation.BindingResult) {
                continue;
            }
            payload.add(arg);
        }
        return objectMapper.writeValueAsString(payload);
    }

    private String maskSensitive(String raw) {
        return raw
                .replaceAll("(\"password\"\\s*:\\s*\")[^\"]*(\")", "$1******$2")
                .replaceAll("(\"accessToken\"\\s*:\\s*\")[^\"]*(\")", "$1******$2")
                .replaceAll("(\"refreshToken\"\\s*:\\s*\")[^\"]*(\")", "$1******$2")
                .replaceAll("(\"token\"\\s*:\\s*\")[^\"]*(\")", "$1******$2");
    }

    private String resolveIp() {
        String forwarded = httpServletRequest.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return httpServletRequest.getRemoteAddr();
    }

    private String resolveTraceId() {
        String traceId = httpServletRequest.getHeader("X-Trace-Id");
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        String requestId = httpServletRequest.getHeader("X-Request-Id");
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    private String buildDataScopeSnapshot(DataScopeContext context) throws Exception {
        return objectMapper.writeValueAsString(java.util.Map.of(
                "level", context.getLevel().name(),
                "userId", context.getUserId(),
                "departmentIds", context.getDepartmentIds()
        ));
    }

    private String buildAuditTags(Object[] args, DataScopeContext context) {
        List<String> tags = new ArrayList<>();
        if (containsProperty(args, "phone") || containsProperty(args, "email")) {
            tags.add("sensitive-field-access:user-contact");
        }
        if (context.getLevel() != null) {
            tags.add("data-scope:" + context.getLevel().name().toLowerCase());
        }
        return String.join(",", tags);
    }

    private boolean containsProperty(Object[] args, String propertyName) {
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            try {
                Object value = arg.getClass().getMethod("get" + capitalize(propertyName)).invoke(arg);
                if (value != null) {
                    return true;
                }
            } catch (Exception ignored) {
                // Ignore and continue probing other arguments.
            }
        }
        return false;
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String extractUsername(Object[] args) {
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            try {
                Object value = arg.getClass().getMethod("getUsername").invoke(arg);
                if (value instanceof String username && !username.isBlank()) {
                    return username;
                }
            } catch (Exception ignored) {
                // Ignore and continue probing other arguments.
            }
        }
        return null;
    }
}
