package com.erp.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.product.domain.entity.ProductPackage;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductPackageMapper extends BaseMapper<ProductPackage> {
    @Select("SELECT * FROM product_package WHERE product_id = CAST(#{productId} AS uuid) ORDER BY level ASC, created_at ASC")
    List<ProductPackage> selectByProductId(@Param("productId") UUID productId);
}
