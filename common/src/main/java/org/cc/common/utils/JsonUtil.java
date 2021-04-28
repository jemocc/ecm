package org.cc.common.utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: JsonUtil
 * @Description: TODO
 * @Author: CC
 * @Date 2021/4/2 10:30
 * @ModifyRecords: v1.0 new
 */
public class JsonUtil {
    private static final Gson g1 = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeUtil.DEFAULT_DATE_TIME_FORMATTER))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeUtil.DEFAULT_DATE_TIME_FORMATTER)))
            .create();
    private static final Gson g2 = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeUtil.DEFAULT_DATE_TIME_FORMATTER))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeUtil.DEFAULT_DATE_TIME_FORMATTER)))
            .serializeNulls().create();

    //new TypeToken<Map<String, Object>>(){}.getType()
    public static <T> T json2Bean(String jsonStr, Type type) {
        return g2.fromJson(jsonStr, type);
    }

    public static String bean2Json(Object bean) {
        return g2.toJson(bean);
    }

    public static <T> List<T> json2List(String jsonStr, Type type) {
        return Arrays.asList(g2.fromJson(jsonStr, type));
    }

    public static <T> T json2Bean_FN(String jsonStr, Type type) {
        return g1.fromJson(jsonStr, type);
    }

    public static String bean2Json_FN(Object bean) {
        return g1.toJson(bean);
    }

    public static <T> List<T> json2List_FN(String jsonStr, Type type) {
        return Arrays.asList(g1.fromJson(jsonStr, type));
    }

}
