package com.erp.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.product.domain.entity.ProductSku;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {
    @Select("SELECT * FROM product_sku WHERE product_id = CAST(#{productId} AS uuid) ORDER BY created_at ASC")
    List<ProductSku> selectByProductId(@Param("productId") UUID productId);
}
