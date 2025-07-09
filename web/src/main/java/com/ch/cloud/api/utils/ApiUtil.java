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

    /**
     * 处理前缀方便拼接，去除结尾的斜框，若不是斜杠开头则加上斜杠
     *
     * @param prefix 前缀
     * @return 处理后的前缀
     */
    public static String handlePrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return "";
        }
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        return prefix;
    }

    /**
     * 处理域名，添加协议，去除尾部的斜杠
     *
     * @param domain 域名
     * @return 处理后的域名
     */
    public static String handleDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return "";
        }
        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
            domain = "http://" + domain;
        }
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        return domain;
    }
}
