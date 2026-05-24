package com.erp.material.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.material.domain.dto.MaterialRequest;
import com.erp.material.domain.vo.MaterialAlertVO;
import com.erp.material.domain.vo.MaterialReplenishmentVO;
import com.erp.material.domain.vo.MaterialVO;
import com.erp.material.service.MaterialService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/material/materials")
public class MaterialController {
    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).MATERIAL_LIST)")
    public R<PageVO<MaterialVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Integer status
    ) {
        return R.ok(materialService.list(pageNum, pageSize, name, categoryId, status));
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).ALERT_LIST)")
    public R<PageVO<MaterialAlertVO>> listAlerts(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name
    ) {
        return R.ok(materialService.listAlerts(pageNum, pageSize, name));
    }

    @GetMapping("/replenishment")
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).REPLENISH_LIST)")
    public R<PageVO<MaterialReplenishmentVO>> listReplenishmentSuggestions(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name
    ) {
        return R.ok(materialService.listReplenishmentSuggestions(pageNum, pageSize, name));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).MATERIAL_CREATE)")
    public R<MaterialVO> create(@Valid @RequestBody MaterialRequest request) {
        return R.ok(materialService.save(null, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).MATERIAL_UPDATE)")
    public R<MaterialVO> update(@PathVariable UUID id, @Valid @RequestBody MaterialRequest request) {
        return R.ok(materialService.save(id, request));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).MATERIAL_EXPORT)")
    public ResponseEntity<InputStreamResource> exportMaterials() throws IOException {
        InputStreamResource resource = new InputStreamResource(materialService.exportMaterials());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=materials.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).MATERIAL_IMPORT)")
    public R<Void> importMaterials(@RequestPart("file") MultipartFile file) {
        materialService.importMaterials(file);
        return R.ok(null);
    }
}
