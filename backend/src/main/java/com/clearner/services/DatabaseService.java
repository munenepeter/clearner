package com.clearner.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {

    private static final String DB_URL = "jdbc:sqlite:data/clearner.db";

    public DatabaseService() {
        initialize();
    }

    private void initialize() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                // System.out.println("Connected to SQLite database.");
                initializeSchema(conn);
            }
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initializeSchema(Connection conn) {
        // Simple schema migration: Read schema.sql and execute.
       // use if not exists for the create statements

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            
            // App Meta
            stmt.execute("CREATE TABLE IF NOT EXISTS app_meta (" +
                         "key TEXT PRIMARY KEY, " +
                         "value TEXT NOT NULL);");

            // Users
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                         "id TEXT PRIMARY KEY, " +
                         "display_name TEXT NOT NULL, " +
                         "created_at INTEGER NOT NULL, " +
                         "last_active_at INTEGER, " +
                         "sync_status TEXT NOT NULL DEFAULT 'DIRTY');");

            // Lesson Progress
            stmt.execute("CREATE TABLE IF NOT EXISTS lessons_progress (" +
                         "user_id TEXT NOT NULL, " +
                         "lesson_id TEXT NOT NULL, " +
                         "current_step INTEGER NOT NULL, " +
                         "completed INTEGER NOT NULL DEFAULT 0, " +
                         "updated_at INTEGER NOT NULL, " +
                         "sync_status TEXT NOT NULL DEFAULT 'DIRTY', " +
                         "PRIMARY KEY (user_id, lesson_id), " +
                         "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE);");
            
            // Lesson Step Events
            stmt.execute("CREATE TABLE IF NOT EXISTS lesson_step_events (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "user_id TEXT NOT NULL, " +
                         "lesson_id TEXT NOT NULL, " +
                         "step_index INTEGER NOT NULL, " +
                         "event_type TEXT NOT NULL, " +
                         "created_at INTEGER NOT NULL, " +
                         "sync_status TEXT NOT NULL DEFAULT 'DIRTY', " +
                         "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE);");

            // User Preferences
            stmt.execute("CREATE TABLE IF NOT EXISTS user_preferences (" +
                         "user_id TEXT NOT NULL, " +
                         "key TEXT NOT NULL, " +
                         "value TEXT NOT NULL, " +
                         "updated_at INTEGER NOT NULL, " +
                         "sync_status TEXT NOT NULL DEFAULT 'DIRTY', " +
                         "PRIMARY KEY (user_id, key), " +
                         "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE);");
            
            // Sync Queue
            stmt.execute("CREATE TABLE IF NOT EXISTS sync_queue (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "entity_type TEXT NOT NULL, " +
                         "entity_id TEXT NOT NULL, " +
                         "operation TEXT NOT NULL, " +
                         "payload TEXT NOT NULL, " +
                         "created_at INTEGER NOT NULL, " +
                         "retry_count INTEGER NOT NULL DEFAULT 0);");
            
            // External Notifications
            stmt.execute("CREATE TABLE IF NOT EXISTS external_notifications (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "provider TEXT NOT NULL, " +
                         "message TEXT NOT NULL, " +
                         "status TEXT NOT NULL DEFAULT 'PENDING', " +
                         "created_at INTEGER NOT NULL, " +
                         "sent_at INTEGER);");

            // Indices
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_last_active ON users(last_active_at);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_lessons_progress_updated ON lessons_progress(updated_at);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_step_events_user_lesson ON lesson_step_events(user_id, lesson_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_sync_queue_retry ON sync_queue(retry_count);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_notifications_status ON external_notifications(status);");

            // System.out.println("Schema initialized.");

        } catch (SQLException e) {
             System.err.println("Schema initialization error: " + e.getMessage());
             e.printStackTrace();
        }
    }
}
