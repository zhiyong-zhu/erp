package com.erp.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.system.domain.entity.SysDictData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {
    @Select("""
            SELECT * FROM sys_dict_data
            WHERE dict_type_code = #{dictTypeCode}
            ORDER BY sort_order ASC, created_at ASC
            """)
    List<SysDictData> selectByDictTypeCode(@Param("dictTypeCode") String dictTypeCode);
}
