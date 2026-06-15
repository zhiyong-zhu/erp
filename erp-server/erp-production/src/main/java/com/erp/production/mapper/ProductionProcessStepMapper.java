package com.erp.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.production.domain.entity.ProductionProcessStep;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductionProcessStepMapper extends BaseMapper<ProductionProcessStep> {
    @Select("SELECT * FROM production_process_step WHERE process_id = CAST(#{processId} AS uuid) ORDER BY step_no ASC, id ASC")
    List<ProductionProcessStep> selectByProcessId(@Param("processId") UUID processId);
}
