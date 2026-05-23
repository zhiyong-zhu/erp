package com.erp.system.controller;

import com.erp.common.core.domain.R;
import com.erp.common.core.domain.PageVO;
import com.erp.system.domain.dto.DictDataCreateRequest;
import com.erp.system.domain.dto.DictDataUpdateRequest;
import com.erp.system.domain.dto.DictTypeCreateRequest;
import com.erp.system.domain.dto.DictTypeUpdateRequest;
import com.erp.system.domain.vo.DictDataVO;
import com.erp.system.domain.vo.DictTypeVO;
import com.erp.system.logging.OperationLog;
import com.erp.system.permission.SystemPermissionCodes;
import com.erp.system.service.SysDictService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/dicts")
public class SysDictController {
    private final SysDictService sysDictService;

    public SysDictController(SysDictService sysDictService) {
        this.sysDictService = sysDictService;
    }

    @GetMapping("/types")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).DICT_LIST)")
    public R<PageVO<DictTypeVO>> listTypes(@RequestParam(defaultValue = "1") long pageNum,
                                           @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(sysDictService.listDictTypes(pageNum, pageSize));
    }

    @PostMapping("/types")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).DICT_CREATE)")
    @OperationLog(module = "system:dict", action = "create_type", description = "创建字典类型")
    public R<DictTypeVO> createType(@Valid @RequestBody DictTypeCreateRequest request) {
        return R.ok(sysDictService.createDictType(request));
    }

    @PutMapping("/types/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).DICT_UPDATE)")
    @OperationLog(module = "system:dict", action = "update_type", description = "更新字典类型")
    public R<DictTypeVO> updateType(@PathVariable UUID id, @Valid @RequestBody DictTypeUpdateRequest request) {
        return R.ok(sysDictService.updateDictType(id, request));
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).DICT_LIST)")
    public R<List<DictDataVO>> listData(@PathVariable String code) {
        return R.ok(sysDictService.listDictData(code));
    }

    @PostMapping("/{code}/items")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).DICT_CREATE)")
    @OperationLog(module = "system:dict", action = "create_item", description = "创建字典项")
    public R<DictDataVO> createData(@PathVariable String code, @Valid @RequestBody DictDataCreateRequest request) {
        return R.ok(sysDictService.createDictData(code, request));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).DICT_UPDATE)")
    @OperationLog(module = "system:dict", action = "update_item", description = "更新字典项")
    public R<DictDataVO> updateData(@PathVariable UUID id, @Valid @RequestBody DictDataUpdateRequest request) {
        return R.ok(sysDictService.updateDictData(id, request));
    }
}
