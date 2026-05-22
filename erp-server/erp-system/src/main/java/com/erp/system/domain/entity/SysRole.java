package com.erp.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.UUID;

@TableName("sys_role")
public class SysRole {
    @TableId(type = IdType.ASSIGN_UUID)
    private UUID id;
    private String name;
    private String code;
    private String description;
    private Integer dataScope;
    private Integer status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDataScope() { return dataScope; }
    public void setDataScope(Integer dataScope) { this.dataScope = dataScope; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
