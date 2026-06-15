package com.erp.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.sales.domain.entity.SaleReturnItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SaleReturnItemMapper extends BaseMapper<SaleReturnItem> {
    @Select("SELECT * FROM sale_return_item WHERE sale_return_id = #{saleReturnId} ORDER BY created_at ASC")
    List<SaleReturnItem> selectBySaleReturnId(java.util.UUID saleReturnId);
}
