package com.erp.system.service;

import com.erp.system.domain.dto.DictDataCreateRequest;
import com.erp.system.domain.dto.DictDataUpdateRequest;
import com.erp.system.domain.dto.DictTypeCreateRequest;
import com.erp.system.domain.dto.DictTypeUpdateRequest;
import com.erp.system.domain.vo.DictDataVO;
import com.erp.system.domain.vo.DictTypeVO;
import com.erp.system.domain.vo.PageVO;
import java.util.List;
import java.util.UUID;

public interface SysDictService {
    PageVO<DictTypeVO> listDictTypes(long pageNum, long pageSize);
    DictTypeVO createDictType(DictTypeCreateRequest request);
    DictTypeVO updateDictType(UUID id, DictTypeUpdateRequest request);
    List<DictDataVO> listDictData(String code);
    DictDataVO createDictData(String code, DictDataCreateRequest request);
    DictDataVO updateDictData(UUID id, DictDataUpdateRequest request);
}
