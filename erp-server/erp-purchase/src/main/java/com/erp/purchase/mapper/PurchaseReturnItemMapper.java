package com.erp.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.purchase.domain.entity.PurchaseReturnItem;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PurchaseReturnItemMapper extends BaseMapper<PurchaseReturnItem> {
    @Select("SELECT * FROM purchase_return_item WHERE purchase_return_id = #{purchaseReturnId} ORDER BY created_at ASC")
    List<PurchaseReturnItem> selectByPurchaseReturnId(UUID purchaseReturnId);
}
