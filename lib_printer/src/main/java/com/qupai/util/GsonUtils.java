package com.qupai.util;


import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * gson 解析工具
 *
 */
public class GsonUtils {


    public static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static <T> T deSerializedFromJson(String json, Class<T> clazz) throws JsonSyntaxException {
        return gson.fromJson(json, clazz);
    }

    public static <T> T deSerializedFromJson(String json, Type type) throws JsonSyntaxException {
        return gson.fromJson(json, type);
    }

    public static String serializedToJson(Object object) {
        if (object != null) {
            return gson.toJson(object);
        } else {
            return "";
        }
    }

    /**
     * 获取JsonObject
     *
     * @return JsonObject
     */
    public static JsonObject parseJson(String json) {
        JsonObject jsonObj = null;
        try {
            JsonParser parser = new JsonParser();
            jsonObj = parser.parse(json).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            LogUtils.e(e);
        }
        return jsonObj;
    }

    public static <T>T joToBean(JsonObject jsonObject,Class<T> tClass){
        try {
            T t = gson.fromJson(jsonObject,tClass);
            return t;
        } catch (JsonSyntaxException e) {
            LogUtils.e(e);

        }
        return null;
    }

    /**
     * json字符串转成Bean对象
     *
     * @param str
     * @return
     */
    public static <T> T jsonToBean(String str,Class<T> tClass) {
        try {
            T t = gson.fromJson(str,tClass);
            return t;
        } catch (JsonSyntaxException e) {
            LogUtils.e(e);

        }
        return null;
    }


    public static String getStringFromJSON(String json, String key1, String key2) {
        String data = "";
        try {
            JSONObject jsonObject = new JSONObject(json).getJSONObject(key1);
            if(jsonObject.has(key2))
            data = jsonObject.getString(key2);

        } catch (JSONException e) {
            LogUtils.e(e);
            e.printStackTrace();
        }
        return data;
    }


    public static long getLongFormJSON(String json, String key1, String key2) {
        long data = 0;
        try {
            JSONObject jsonObject = new JSONObject(json).getJSONObject(key1);
            data = jsonObject.getLong(key2);

        } catch (JSONException e) {
            LogUtils.e(e);
        }
        return data;
    }

    /**
     * @param json
     * @param key1
     * @param key2
     * @return
     */
    // 现有逻辑有问题 ：更新包判断
    public static boolean getBooleanFormJSON(String json, String key1, String key2) {
        boolean data = true;
        try {
            JSONObject jsonObject = new JSONObject(json).getJSONObject(key1);
            data = jsonObject.getBoolean(key2);
        } catch (JSONException e) {
            LogUtils.e(e);
        }
        return data;
    }
    public static Map<String,String> jsonToBeanMap(String json){
        Map<String,String> map;
        if (json == null|| TextUtils.isEmpty(json)){
            return new HashMap<String, String>();
        }else { //json data 字段不为空
            map = gson.fromJson(json,new TypeToken<Map<String, String>>() {}.getType());
            if (map!=null){
                return map;
            }
        }
        return new HashMap<String,String>();
    }

    public static <T, P> Map<T, P> jsonToBeanMap(String json, Type tClass) {
        Map<T, P> map;
        if (json == null || TextUtils.isEmpty(json)) {
            return new HashMap<T, P>();
        } else { //json data 字段不为空
            map = gson.fromJson(json, tClass);
            if (map != null) {
                return map;
            }
        }
        return new HashMap<T, P>();
    }

    /**
     * 根据key获取json object 的value
     *
     * @param json
     * @param key
     * @return
     */
    public static boolean getBooleanFormJSON(String json, String key) {
        boolean data = false;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.has(key))
            data = jsonObject.getBoolean(key);
        } catch (JSONException e) {
            LogUtils.e(e);
        }
        return data;
    }


    /**
     * 从JSON字符串提取出对应 Key的 字符串
     *
     * @param json
     * @param key
     * @return
     */
    public static String getStringFromJSON(String json, String key) {
        String data = null;
        if(json==null)return null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.has(key)){
                data = jsonObject.getString(key);
            }
        } catch (Throwable e) {
            LogUtils.e(e);
//            LogUtils.e( "getStringFromJSON Exception==="+"\n"+ e.toString()+"\n【key="+key+"】");
        }
        return data;
    }

    public static JSONArray getArrayFromJSON(String json, String key) {
        JSONArray data = null;
        if(json==null)return null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.has(key))
            data = jsonObject.getJSONArray(key);
        } catch (Exception e) {
            LogUtils.e(e);

        }
        return data;
    }


    public static int getIntFromJSON(JSONObject obj, String key) {
        int data = 0;
        try {
            if (obj == null) {
                return data;
            }
            data = obj.getInt(key);
        } catch (JSONException e) {
            LogUtils.e(e);
//            LogUtils.e( "getIntFromJSON Exception===" + e.toString());
        }
        return data;
    }


    public static int getIntFromJSON(String json, String key) {
        int data = -1;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.has(key))
            data = jsonObject.getInt(key);
        } catch (JSONException e) {
            LogUtils.e(e);
//            LogUtils.e("william", "getIntFromJSON Exception===" + e.toString());
        }catch (Throwable e){
            LogUtils.e(e);
//            LogUtils.e("william", "getIntFromJSON Exception===" + e.toString());
        }
        return data;
    }
    public static double getDoubleFromJSON(String json, String key) {
        double data = 0;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.has(key))
            data = jsonObject.getDouble(key);
        } catch (JSONException e) {
            LogUtils.e(e);
//            LogUtils.e("william", "getIntFromJSON Exception===" + e.toString());
        }catch (Throwable e){
            LogUtils.e(e);
        }
        return data;
    }

    public static long getLongFromJSON(String json, String key) {
        long data = 0;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.has(key))
            data = jsonObject.getLong(key);
        } catch (JSONException e) {
            LogUtils.e(e);
//            LogUtils.e("william", "getLongFromJSON Exception===" + e.toString());
        }
        return data;
    }


    public static <T> List<T> jsonToBeanList(String json, Type type) {
        List<T> list;
        if (json == null || TextUtils.isEmpty(json)) { // json data 字段为空
            return new ArrayList<T>();
        } else { //json data 字段不为空
            try {
                list = gson.fromJson(json, type);
                if (list != null) {
                    return list;
                }
            } catch (Exception e) {
                LogUtils.e("william", "jsonToBeanList_2 Exception ===" + e.toString());
            }
        }
        return new ArrayList<T>();
    }


    public static <T> List<T> jsonToBeanListForData(String json, Type type) {
        String data1 = getStringFromJSON(json, "list");
        return jsonToBeanList(data1,type);
    }

}
