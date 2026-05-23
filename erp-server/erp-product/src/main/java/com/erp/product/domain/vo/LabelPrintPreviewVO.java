package com.erp.product.domain.vo;

public class LabelPrintPreviewVO {
    private String pdfUrl;
    private Integer totalCount;
    private String summary;
    private String previewHtml;

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPreviewHtml() {
        return previewHtml;
    }

    public void setPreviewHtml(String previewHtml) {
        this.previewHtml = previewHtml;
    }
}
