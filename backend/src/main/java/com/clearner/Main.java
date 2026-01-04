package com.clearner;

import com.clearner.services.ContentService;
import com.clearner.services.DatabaseService;
import com.clearner.services.PersistenceService;
import com.clearner.services.UserService;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        DatabaseService databaseService = new DatabaseService();
        ContentService contentService = new ContentService();
        PersistenceService persistenceService = new PersistenceService(databaseService);
        UserService userService = new UserService(databaseService);

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        }).start(8080);

        app.get("/", ctx -> ctx.result("Clearner Backend Running"));

        app.post("/api/auth/login", ctx -> {
            String body = ctx.body();
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(body);
            String displayName = node.get("displayName").asText();

            if (displayName == null) {
                ctx.status(400).result("Missing display name");
                return;
            }

            String userJson = userService.login(displayName);
            if (userJson != null) {
                ctx.contentType("application/json").result(userJson);
            } else {
                ctx.status(500).result("Login failed");
            }
        });

        app.get("/api/lessons/{course}/{id}", ctx -> {
            String course = ctx.pathParam("course");
            String id = ctx.pathParam("id");
            String json = contentService.getLesson(course, id);
            if (json != null) {
                ctx.contentType("application/json").result(json);
            } else {
                ctx.status(404).result("Lesson not found");
            }
        });

        app.post("/api/progress", ctx -> {
            String body = ctx.body();
            if (persistenceService.saveProgress(body)) {
                ctx.status(200).result("Progress saved");
            } else {
                ctx.status(500).result("Failed to save progress");
            }
        });

        app.get("/api/progress/{userId}", ctx -> {
            String userId = ctx.pathParam("userId");
            String progressJson = persistenceService.getProgress(userId);
            ctx.contentType("application/json").result(progressJson);
        });

        app.post("/api/log/paste", ctx -> {
            String body = ctx.body();
            persistenceService.logPaste(body);
            ctx.status(200).result("Log saved");
        });
    }
}
