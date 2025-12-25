package com.clearner;

import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            // Static files will be configured here later
        }).start(7070);

        app.get("/", ctx -> ctx.result("Hello from Local Learning System Backend"));
    }
}
