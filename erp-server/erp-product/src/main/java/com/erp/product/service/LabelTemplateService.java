package com.erp.product.service;

import com.erp.product.domain.dto.LabelTemplateRequest;
import com.erp.product.domain.vo.LabelTemplateVO;
import java.util.List;

public interface LabelTemplateService {
    List<LabelTemplateVO> list();
    LabelTemplateVO save(LabelTemplateRequest request);
}
