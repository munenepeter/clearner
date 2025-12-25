package com.clearner.controllers;

import io.javalin.http.Context;

public class HealthController {
    public static void check(Context ctx) {
        ctx.json("{\"status\": \"ok\"}");
    }
}
