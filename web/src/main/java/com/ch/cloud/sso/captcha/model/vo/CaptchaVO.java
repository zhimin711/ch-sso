
package com.ch.cloud.sso.captcha.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.awt.*;
import java.io.Serializable;
import java.util.List;

/**
 * decs:验证码信息.
 *
 * @author zhimin.ma
 * @since 2021/1/1
 */
@Data
//@JsonIgnoreProperties(value = {"captchaId", "projectCode", "points", "pointList"})
public class CaptchaVO implements Serializable {

    /**
     * 验证码id(后台申请)
     */
    private String captchaId;

    private String projectCode;

    /**
     * 验证码类型:(clickWord,blockPuzzle)
     */
    private String captchaType;

//    private String captchaOriginalPath;

//    private String captchaFontType;

//    private Integer captchaFontSize;

    private String secretKey;

    /**
     * 原生图片base64
     */
    private String originalImageBase64;

    /**
     * 滑块点选坐标
     */
    private List<PointVO> points;

    /**
     * 滑块图片base64
     */
    private String jigsawImageBase64;

    /**
     * 点选文字
     */
    private List<String> wordList;

    /**
     * 点选坐标
     */
    private List<Point> pointList;


    /**
     * 点坐标(base64加密传输)
     */
    private String pointJson;


    /**
     * UUID(每次请求的验证码唯一标识)
     */
    private String token;

    /**
     * 校验结果
     */
//    private Boolean result = false;

    /**
     * 后台二次校验参数
     */
    private String captchaVerification;

}
