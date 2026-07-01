package com.erp.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.production.domain.entity.ProductionProductStock;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductionProductStockMapper extends BaseMapper<ProductionProductStock> {
    @Update("""
            UPDATE production_product_stock
            SET current_stock = current_stock - #{quantity},
                reserved_stock = reserved_stock - #{quantity},
                updated_by = #{updatedBy},
                updated_at = #{updatedAt}
            WHERE id = #{id}
              AND current_stock >= #{quantity}
              AND reserved_stock >= #{quantity}
            """)
    int decreaseReservedIfEnough(
            @Param("id") UUID id,
            @Param("quantity") BigDecimal quantity,
            @Param("updatedBy") UUID updatedBy,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Update("""
            UPDATE production_product_stock
            SET current_stock = current_stock - #{quantity},
                updated_by = #{updatedBy},
                updated_at = #{updatedAt}
            WHERE id = #{id}
              AND current_stock >= #{quantity}
            """)
    int decreaseCurrentIfEnough(
            @Param("id") UUID id,
            @Param("quantity") BigDecimal quantity,
            @Param("updatedBy") UUID updatedBy,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Update("""
            UPDATE production_product_stock
            SET current_stock = current_stock - #{quantity},
                updated_by = #{updatedBy},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int decreaseCurrentForce(
            @Param("id") UUID id,
            @Param("quantity") BigDecimal quantity,
            @Param("updatedBy") UUID updatedBy,
            @Param("updatedAt") OffsetDateTime updatedAt
    );
}
