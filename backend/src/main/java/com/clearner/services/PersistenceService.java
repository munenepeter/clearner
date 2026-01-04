package com.clearner.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistenceService {

    private final DatabaseService db;
    private final ObjectMapper mapper;

    public PersistenceService(DatabaseService db) {
        this.db = db;
        this.mapper = new ObjectMapper();
    }

    /**
     * Save or update lesson progress for a user.
     * Expected JSON: { "userId": "...", "lessonId": "...", "currentStep": 0, "completed": false }
     */
    public boolean saveProgress(String jsonData) {
        try {
            JsonNode node = mapper.readTree(jsonData);
            
            // Validate required fields
            if (!node.has("userId") || node.get("userId") == null) {
                System.err.println("Missing or null userId in progress data: " + jsonData);
                return false;
            }
            if (!node.has("lessonId") || node.get("lessonId") == null) {
                System.err.println("Missing or null lessonId in progress data: " + jsonData);
                return false;
            }
            if (!node.has("currentStep") || node.get("currentStep") == null) {
                System.err.println("Missing or null currentStep in progress data: " + jsonData);
                return false;
            }
            
            String userId = node.get("userId").asText();
            String lessonId = node.get("lessonId").asText();
            int currentStep = node.get("currentStep").asInt();
            int completed = node.has("completed") && node.get("completed").asBoolean() ? 1 : 0;
            long now = System.currentTimeMillis();

            try (Connection conn = db.getConnection()) {
                // UPSERT logic
                String upsert = "INSERT INTO lessons_progress (user_id, lesson_id, current_step, completed, updated_at, sync_status) " +
                                "VALUES (?, ?, ?, ?, ?, 'DIRTY') " +
                                "ON CONFLICT(user_id, lesson_id) DO UPDATE SET " +
                                "current_step = excluded.current_step, " +
                                "completed = excluded.completed, " +
                                "updated_at = excluded.updated_at, " +
                                "sync_status = 'DIRTY'";
                
                try (PreparedStatement pstmt = conn.prepareStatement(upsert)) {
                    pstmt.setString(1, userId);
                    pstmt.setString(2, lessonId);
                    pstmt.setInt(3, currentStep);
                    pstmt.setInt(4, completed);
                    pstmt.setLong(5, now);
                    Number result = pstmt.executeUpdate();

                    return result.intValue() > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Log a paste event.
     * Expected JSON: { "userId": "...", "lessonId": "...", "stepIndex": 0, "content": "..." }
     */
    public void logPaste(String jsonData) {
        try {
            JsonNode node = mapper.readTree(jsonData);
            String userId = node.get("userId").asText();
            String lessonId = node.get("lessonId").asText();
            int stepIndex = node.get("stepIndex").asInt();
            long now = System.currentTimeMillis();

            try (Connection conn = db.getConnection()) {
                String insert = "INSERT INTO lesson_step_events (user_id, lesson_id, step_index, event_type, created_at, sync_status) " +
                                "VALUES (?, ?, ?, 'PASTE', ?, 'DIRTY')";
                
                try (PreparedStatement pstmt = conn.prepareStatement(insert)) {
                    pstmt.setString(1, userId);
                    pstmt.setString(2, lessonId);
                    pstmt.setInt(3, stepIndex);
                    pstmt.setLong(4, now);
                    pstmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all progress for a user.
     * Returns JSON array: [{ "lessonId": "...", "currentStep": 0, "completed": false }, ...]
     */
    public String getProgress(String userId) {
        List<Map<String, Object>> progressList = new ArrayList<>();
        
        try (Connection conn = db.getConnection()) {
            String query = "SELECT lesson_id, current_step, completed FROM lessons_progress WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, userId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("lessonId", rs.getString("lesson_id"));
                    item.put("currentStep", rs.getInt("current_step"));
                    item.put("completed", rs.getInt("completed") == 1);
                    progressList.add(item);
                }
            }
            
            return mapper.writeValueAsString(progressList);
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }
}
