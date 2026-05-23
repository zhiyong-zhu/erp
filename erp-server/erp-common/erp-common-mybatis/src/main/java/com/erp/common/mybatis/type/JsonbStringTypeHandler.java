package com.erp.common.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

public class JsonbStringTypeHandler extends BaseTypeHandler<String> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");
        jsonObject.setValue(parameter);
        ps.setObject(i, jsonObject);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toJson(rs.getObject(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toJson(rs.getObject(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toJson(cs.getObject(columnIndex));
    }

    private String toJson(Object value) throws SQLException {
        if (value == null) {
            return null;
        }
        if (value instanceof PGobject pgObject) {
            return pgObject.getValue();
        }
        if (value instanceof String text) {
            return text;
        }
        throw new SQLException("Unsupported jsonb value type: " + value.getClass().getName());
    }
}
