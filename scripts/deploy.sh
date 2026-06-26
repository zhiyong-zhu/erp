#!/bin/bash
set -euo pipefail

# ERP 部署脚本 - 在 ECS 上执行
# 由 GitHub Actions 调用，处理：停服 → 备份 → 部署 → 启动 → 健康检查

DEPLOY_ROOT="/opt/erp"
BACKEND_DIR="${DEPLOY_ROOT}/backend"
FRONTEND_DIR="/usr/share/erp/web"
TMP_DIR="${DEPLOY_ROOT}/tmp"
SERVICE_NAME="erp-admin"
JAR_FILE="erp-admin-1.0.0-SNAPSHOT.jar"
BACKUP_DIR="${DEPLOY_ROOT}/backups"
SYSTEMD_UNIT_SRC="${DEPLOY_ROOT}/scripts/erp-admin.service"
NGINX_CONF_SRC="${DEPLOY_ROOT}/scripts/nginx.conf"
ENV_FILE="${DEPLOY_ROOT}/scripts/env.production"
HEALTH_URL="http://localhost:8080/actuator/health"
MAX_RETRIES=30
RETRY_INTERVAL=2
CURRENT_BACKUP_PATH=""

log() { echo "[$(date '+%H:%M:%S')] $1"; }
err() { echo "[$(date '+%H:%M:%S')] ERROR: $1" >&2; exit 1; }

# --- 1. 安装运行时配置 ---
install_runtime_config() {
    mkdir -p "$BACKEND_DIR" "${DEPLOY_ROOT}/logs" "$BACKUP_DIR" "$TMP_DIR" "${DEPLOY_ROOT}/scripts" "$FRONTEND_DIR"

    if [ -f "$ENV_FILE" ] && ! grep -q '^SPRING_PROFILES_ACTIVE=' "$ENV_FILE"; then
        log "补充生产 profile 配置..."
        printf '\nSPRING_PROFILES_ACTIVE=prod\n' >> "$ENV_FILE"
    fi

    if [ -f "$SYSTEMD_UNIT_SRC" ]; then
        log "安装 systemd 服务配置..."
        cp "$SYSTEMD_UNIT_SRC" "/etc/systemd/system/${SERVICE_NAME}.service"
        systemctl daemon-reload
        systemctl enable "${SERVICE_NAME}" >/dev/null 2>&1 || true
    else
        log "未找到 systemd 服务配置，跳过安装"
    fi

    if [ -f "$NGINX_CONF_SRC" ]; then
        log "安装 Nginx 配置..."
        cp "$NGINX_CONF_SRC" /etc/nginx/sites-available/erp
        ln -sf /etc/nginx/sites-available/erp /etc/nginx/sites-enabled/erp
        rm -f /etc/nginx/sites-enabled/default
        nginx -t
        reload_nginx
    else
        log "未找到 Nginx 配置，跳过安装"
    fi
}

# --- 2. 停止当前服务 ---
stop_service() {
    log "停止 ${SERVICE_NAME} 服务..."
    systemctl stop "${SERVICE_NAME}" 2>/dev/null || log "服务未运行，跳过停止"
}

# --- 3. 备份当前版本 ---
backup_current() {
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    local backup_path="${BACKUP_DIR}/${timestamp}"

    if [ -f "${BACKEND_DIR}/${JAR_FILE}" ]; then
        mkdir -p "$backup_path"
        cp "${BACKEND_DIR}/${JAR_FILE}" "$backup_path/"
    fi

    if [ -d "${FRONTEND_DIR}" ] && [ "$(ls -A "${FRONTEND_DIR}" 2>/dev/null)" ]; then
        mkdir -p "$backup_path"
        tar czf "$backup_path/web.tar.gz" -C "${FRONTEND_DIR}" .
    fi

    if [ -d "$backup_path" ]; then
        CURRENT_BACKUP_PATH="$backup_path"
        log "备份当前版本到: $backup_path"
        # 保留最近 5 个备份
        ls -dt "${BACKUP_DIR}"/*/ 2>/dev/null | tail -n +6 | xargs rm -rf 2>/dev/null || true
    else
        log "当前没有可备份的已部署产物"
    fi
}

# --- 4. 部署后端 ---
deploy_backend() {
    local jar_src="${DEPLOY_ROOT}/erp-admin.jar"
    if [ ! -f "$jar_src" ]; then
        log "未找到新 JAR，跳过后端部署"
        return
    fi
    log "部署后端 JAR..."
    mv "$jar_src" "${BACKEND_DIR}/${JAR_FILE}" || return 1
    log "后端 JAR 已就位"
}

# --- 5. 部署前端 ---
deploy_frontend() {
    local pkg="${DEPLOY_ROOT}/web-dist.tar.gz"
    if [ ! -f "$pkg" ]; then
        log "未找到前端包，跳过前端部署"
        return
    fi
    log "部署前端（解压 tar.gz）..."
    rm -rf "${FRONTEND_DIR:?}"/* || return 1
    tar xzf "$pkg" -C "${FRONTEND_DIR}/" || return 1
    rm -f "$pkg" || return 1
    chown -R www-data:www-data "${FRONTEND_DIR}" 2>/dev/null || true
    log "前端部署完成"

    reload_nginx || return 1
}

# --- 6. 重载 Nginx ---
reload_nginx() {
    if systemctl is-active --quiet nginx; then
        nginx -t
        systemctl reload nginx
        log "Nginx 已重载"
    fi
}

# --- 7. 启动服务 ---
start_service() {
    log "启动 ${SERVICE_NAME} 服务..."
    if ! systemctl start "${SERVICE_NAME}"; then
        log "启动 ${SERVICE_NAME} 失败"
        return 1
    fi

    log "等待后端启动..."
    local retries=0
    while [ $retries -lt $MAX_RETRIES ]; do
        if curl -sf "$HEALTH_URL" > /dev/null 2>&1; then
            log "后端启动成功（耗时 $((retries * RETRY_INTERVAL))s）"
            return 0
        fi
        retries=$((retries + 1))
        sleep $RETRY_INTERVAL
    done
    log "后端启动超时！查看日志: journalctl -u ${SERVICE_NAME} -n 50"
    return 1
}

# --- 8. 执行部署 ---
deploy_release() {
    deploy_backend || return 1
    deploy_frontend || return 1
    start_service || return 1
}

# --- 9. 回滚 ---
rollback_current() {
    if [ -z "$CURRENT_BACKUP_PATH" ] || [ ! -d "$CURRENT_BACKUP_PATH" ]; then
        log "没有可用备份，无法自动回滚"
        return 1
    fi

    log "部署失败，开始回滚到: $CURRENT_BACKUP_PATH"
    systemctl stop "${SERVICE_NAME}" 2>/dev/null || true

    if [ -f "${CURRENT_BACKUP_PATH}/${JAR_FILE}" ]; then
        cp "${CURRENT_BACKUP_PATH}/${JAR_FILE}" "${BACKEND_DIR}/${JAR_FILE}"
        log "后端 JAR 已回滚"
    fi

    if [ -f "${CURRENT_BACKUP_PATH}/web.tar.gz" ]; then
        rm -rf "${FRONTEND_DIR:?}"/*
        tar xzf "${CURRENT_BACKUP_PATH}/web.tar.gz" -C "${FRONTEND_DIR}/"
        chown -R www-data:www-data "${FRONTEND_DIR}" 2>/dev/null || true
        reload_nginx || true
        log "前端资源已回滚"
    fi

    if start_service; then
        log "回滚后服务已恢复"
        return 0
    fi

    log "回滚后服务仍无法启动，请手动检查: journalctl -u ${SERVICE_NAME} -n 100"
    return 1
}

# --- 10. 健康检查 ---
health_check() {
    log "健康检查..."
    curl -sf "$HEALTH_URL" > /dev/null 2>&1 && log "✅ 后端正常" || log "⚠️ 后端异常"
    curl -sf -o /dev/null http://localhost/ 2>&1 && log "✅ Nginx 正常" || log "⚠️ Nginx 异常"
    systemctl is-active --quiet postgresql && log "✅ PostgreSQL 正常" || log "⚠️ PostgreSQL 异常"
    systemctl is-active --quiet redis-server 2>/dev/null && log "✅ Redis 正常" || log "⚠️ Redis 异常"
    systemctl is-active --quiet rustfs 2>/dev/null && log "✅ RustFS 正常" || log "⚠️ RustFS 异常"
}

# --- 11. 清理日志 ---
cleanup_logs() {
    find "${DEPLOY_ROOT}/logs" -name "*.log.*" -mtime +7 -delete 2>/dev/null || true
    journalctl --vacuum-time=7d 2>/dev/null || true
}

# --- 主流程 ---
main() {
    log "=========================================="
    log "ERP 部署开始"
    log "=========================================="
    install_runtime_config
    stop_service
    backup_current
    if ! deploy_release; then
        rollback_current || true
        err "部署失败，已尝试回滚"
    fi
    health_check
    cleanup_logs
    log "=========================================="
    log "ERP 部署完成"
    log "=========================================="
}

main "$@"
