package com.ch.cloud.api.utils;

/**
 * <p>
 * desc: ApiVueRecordUtils
 * </p>
 *
 * @author Zhimin.Ma
 * @since 2022/10/16
 */
public class ApiUtil {
    
    public static final String API_GROUP_DEFAULT = "#default";
    
    public final static String RESULT_SCHEMA_PROPERTIES = "{\"type\":\"object\",\"properties\":{\"code\":{\"type\":\"string\",\"example\":0,\"description\":\"代码\"},\"extra\":{\"type\":\"object\",\"description\":\"附加数据\",\"properties\":{}},\"message\":{\"type\":\"string\",\"example\":0,\"description\":\"消息\"},\"rows\":{\"type\":\"array\",\"example\":[],\"description\":\"返回数据\",\"items\":{\"type\":\"object\",\"properties\":{}},\"status\":{\"type\":\"integer\",\"format\":\"int32\",\"example\":0,\"description\":\"状态\"},\"success\":{\"type\":\"boolean\"},\"timestamp\":{\"type\":\"integer\",\"format\":\"int64\",\"description\":\"服务器时间\"}},\"title\":\"\"}}";

    
}
