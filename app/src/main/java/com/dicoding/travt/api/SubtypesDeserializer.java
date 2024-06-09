package com.dicoding.travt.api;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class SubtypesDeserializer implements JsonDeserializer<List<String>> {
    @Override
    public List<String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<String> subtypes = new ArrayList<>();
        if (json.isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray()) {
                subtypes.add(element.getAsString());
            }
        } else if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            subtypes.add(json.getAsString());
        }
        return subtypes;
    }
}
