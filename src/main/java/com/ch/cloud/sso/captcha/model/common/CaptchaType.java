package com.ch.cloud.sso.captcha.model.common;

/**
 * decs:验证码类型
 *
 * @author zhimin.ma
 * @date 2021/1/1
 */
public enum CaptchaType {
    /**
     * 滑块拼图.
     */
    BLOCK_PUZZLE("blockPuzzle", "滑块拼图"),
    /**
     * 文字点选.
     */
    CLICK_WORD("clickWord", "文字点选"),
    /**
     * 默认.
     */
    DEFAULT("default", "默认");

    private String codeValue;
    private String codeDesc;

    CaptchaType(String codeValue, String codeDesc) {
        this.codeValue = codeValue;
        this.codeDesc = codeDesc;
    }

    public String getCodeValue() {
        return this.codeValue;
    }

    public String getCodeDesc() {
        return this.codeDesc;
    }

    //根据codeValue获取枚举
    public static CaptchaType parseFromCodeValue(String codeValue) {
        for (CaptchaType e : CaptchaType.values()) {
            if (e.codeValue.equals(codeValue)) {
                return e;
            }
        }
        return null;
    }

    //根据codeValue获取描述
    public static String getCodeDescByCodeBalue(String codeValue) {
        CaptchaType enumItem = parseFromCodeValue(codeValue);
        return enumItem == null ? "" : enumItem.getCodeDesc();
    }

    //验证codeValue是否有效
    public static boolean validateCodeValue(String codeValue) {
        return parseFromCodeValue(codeValue) != null;
    }

    //列出所有值字符串
    public static String getString() {
        StringBuffer buffer = new StringBuffer();
        for (CaptchaType e : CaptchaType.values()) {
            buffer.append(e.codeValue).append("--").append(e.getCodeDesc()).append(", ");
        }
        buffer.deleteCharAt(buffer.lastIndexOf(","));
        return buffer.toString().trim();
    }

}
