package com.example.authz.repository;

import com.example.authz.model.Permission;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class PermissionRepository {
    private final JdbcTemplate jdbcTemplate;

    public PermissionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Permission> findByUserIdAndAction(String userId, String action) {
        String sql = "SELECT * FROM user_permissions WHERE user_id = ? AND action = ?";
        return jdbcTemplate.query(sql, new PermissionRowMapper(), userId, action);
    }

    private static class PermissionRowMapper implements RowMapper<Permission> {
        @Override
        public Permission mapRow(ResultSet rs, int rowNum) throws SQLException {
            Permission p = new Permission();
            p.setId(rs.getLong("id"));
            p.setUserId(rs.getString("user_id"));
            p.setAction(rs.getString("action"));
            p.setResource(rs.getString("resource"));
            p.setEffect(rs.getString("effect"));
            return p;
        }
    }
}