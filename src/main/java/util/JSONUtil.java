package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

public class JSONUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JSONUtil.class);

    static ObjectMapper mapper = new ObjectMapper();


    static {
        mapper.registerModule(new JavaTimeModule());
    }


    public static String toJSON(Object object)
    {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOG.error("JSON serialization failed", e);
        }

        return null;
    }


    public static <T> T fromJSON(String json, Class<T> clazz)
    {
        try {
            return mapper.readValue(json,clazz);
        } catch (IOException e) {
            // input may come from a peer: log the reason only, not the stack or the content
            LOG.warn("JSON parsing failed: {}", e.getClass().getSimpleName());
        }

        return null;
    }

    public static String toPrettyJSON(Object object)
    {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOG.error("JSON serialization failed", e);
        }

        return null;
    }


}
