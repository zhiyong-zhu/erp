package com.erp.product.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class ProductCreateRequest extends BaseDTO {
    @NotBlank(message = "产品编码不能为空")
    private String code;
    @NotBlank(message = "产品名称不能为空")
    private String name;
    private UUID categoryId;
    private String brand;
    @NotBlank(message = "单位不能为空")
    private String unit;
    private String description;
    private List<String> images;
    private String specifications;
    private Integer status;
    @Valid
    @NotNull(message = "SKU列表不能为空")
    private List<ProductSkuRequest> skus;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<ProductSkuRequest> getSkus() {
        return skus;
    }

    public void setSkus(List<ProductSkuRequest> skus) {
        this.skus = skus;
    }
}
