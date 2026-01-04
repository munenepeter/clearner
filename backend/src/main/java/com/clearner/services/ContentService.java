package com.clearner.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;

public class ContentService {
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public String getLesson(String course, String lessonId) {
        try {
            // Sanitize input to prevent traversal
            String filename = lessonId.replaceAll("[^a-zA-Z0-9-]", "") + ".yaml";
            InputStream is = getClass().getResourceAsStream("/content/" + course + "/" + filename);
            if (is == null) {
                return null;
            }
            // Read YAML and convert to JSON object
            Object obj = yamlMapper.readValue(is, Object.class);
            // Convert to JSON string
            return jsonMapper.writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
