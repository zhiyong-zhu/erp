package com.erp.admin;

import com.erp.common.core.domain.R;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/ping")
    public R<Map<String, Object>> ping() {
        return R.ok(Map.of("service", "erp-admin", "status", "ok"));
    }
}
