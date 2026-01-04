package com.clearner.services;

import com.clearner.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserService {

    private final DatabaseService db;
    private final ObjectMapper mapper;

    public UserService(DatabaseService db) {
        this.db = db;
        this.mapper = new ObjectMapper();
    }

    public String login(String displayName) {
        try (Connection conn = db.getConnection()) {
            String query = "SELECT * FROM users WHERE display_name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, displayName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    User user = mapUser(rs);
                    updateLastActive(conn, user.getId());
                    return mapper.writeValueAsString(user);
                }
            }

            String newId = UUID.randomUUID().toString();
            long now = System.currentTimeMillis();
            String insert = "INSERT INTO users (id, display_name, created_at, last_active_at, sync_status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insert)) {
                pstmt.setString(1, newId);
                pstmt.setString(2, displayName);
                pstmt.setLong(3, now);
                pstmt.setLong(4, now);
                pstmt.setString(5, "DIRTY");
                pstmt.executeUpdate();
            }

            User newUser = new User(newId, displayName, now, now, "DIRTY");
            return mapper.writeValueAsString(newUser);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateLastActive(Connection conn, String userId) {
        String update = "UPDATE users SET last_active_at = ?, sync_status = 'DIRTY' WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(update)) {
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getString("id"),
            rs.getString("display_name"),
            rs.getLong("created_at"),
            rs.getLong("last_active_at"),
            rs.getString("sync_status")
        );
    }
    
    public User getUser(String id) {
        try (Connection conn = db.getConnection()) {
            String query = "SELECT * FROM users WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
