package com.erp.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.inventory.domain.entity.InventoryBalance;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InventoryBalanceMapper extends BaseMapper<InventoryBalance> {
    @Update("""
            UPDATE inventory_balance
               SET available_quantity = available_quantity - #{quantity},
                   total_quantity = available_quantity - #{quantity} + frozen_quantity,
                   updated_by = #{updatedBy},
                   updated_at = #{updatedAt}
             WHERE id = #{id}
               AND available_quantity >= #{quantity}
            """)
    int decreaseAvailableIfEnough(
            @Param("id") UUID id,
            @Param("quantity") BigDecimal quantity,
            @Param("updatedBy") UUID updatedBy,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Update("""
            UPDATE inventory_balance
               SET available_quantity = available_quantity - #{quantity},
                   frozen_quantity = frozen_quantity + #{quantity},
                   total_quantity = available_quantity - #{quantity} + frozen_quantity + #{quantity},
                   updated_by = #{updatedBy},
                   updated_at = #{updatedAt}
             WHERE id = #{id}
               AND available_quantity >= #{quantity}
            """)
    int freezeQuantity(
            @Param("id") UUID id,
            @Param("quantity") BigDecimal quantity,
            @Param("updatedBy") UUID updatedBy,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Update("""
            UPDATE inventory_balance
               SET available_quantity = available_quantity + #{quantity},
                   frozen_quantity = frozen_quantity - #{quantity},
                   total_quantity = available_quantity + #{quantity} + frozen_quantity - #{quantity},
                   updated_by = #{updatedBy},
                   updated_at = #{updatedAt}
             WHERE id = #{id}
               AND frozen_quantity >= #{quantity}
            """)
    int releaseFrozenQuantity(
            @Param("id") UUID id,
            @Param("quantity") BigDecimal quantity,
            @Param("updatedBy") UUID updatedBy,
            @Param("updatedAt") OffsetDateTime updatedAt
    );
}
