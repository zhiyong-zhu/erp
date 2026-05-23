package com.erp.common.core.domain;

import java.util.List;

public class PageVO<T> {
    private List<T> records;
    private Long total;
    private Long pageNum;
    private Long pageSize;
    private Long pages;

    public PageVO() {
    }

    public PageVO(List<T> records, Long total, Long pageNum, Long pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = pageSize == null || pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getPageNum() {
        return pageNum;
    }

    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public Long getPages() {
        return pages;
    }

    public void setPages(Long pages) {
        this.pages = pages;
    }
}
