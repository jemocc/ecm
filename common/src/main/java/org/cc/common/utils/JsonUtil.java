package org.cc.common.utils;

import com.google.gson.Gson;

public class JsonUtil {

    private static class In {
        private static final Gson gson = new Gson();
    }

    public static String toJSONStr(Object obj) {
        return In.gson.toJson(obj);
    }

    public static <T> T fromJSON(String str, Class<T> c) {
        return In.gson.fromJson(str, c);
    }

    public static <T> T transfer(Object obj, Class<T> c){
        String t =  toJSONStr(obj);
        return fromJSON(t, c);
    }
}
