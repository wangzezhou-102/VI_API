package com.secusoft.web.core.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Utilities for json parser and generate use jackson.
 */
public class JacksonParser {

    private static ObjectMapper mapper = new ObjectMapper();

    public static JsonNode parse(String json) throws JsonProcessingException,
            IOException {
        return mapper.readTree(json);
    }

    public static ObjectMapper getObjectMapper() {
        return mapper;
    }
}
