package com.erp.product.service;

import com.erp.product.domain.dto.LabelPrintRequest;
import com.erp.product.domain.vo.LabelPrintPreviewVO;

public interface LabelPrintService {
    LabelPrintPreviewVO preview(LabelPrintRequest request);
}
