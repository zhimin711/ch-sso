package com.ch.cloud.sso.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * desc:滑块验证码工具类
 *
 * @author zhimi
 * @date 2020/10/17 23:16
 */
@Slf4j
public class SlideCaptchaUtil {

    static int targetLength = 55;//小图长
    static int targetWidth = 45;//小图宽
    static int circleR = 6;//半径
    static int r1 = 3;//距离点

    public static SliderCheck build() {
        try {
            int max = 227;
            int min = 100;
            int x = new Random().nextInt(max - min) + min;
            // 生成base64
            int[][] blockData = getBlockData();
            int number = new Random().nextInt(6) + 1;
            BufferedImage resourceImg = ImageIO.read(new File("D:\\work\\gitee\\ch-cloud\\ch-sso\\src\\main\\resources\\static\\images\\captcha\\shoushi2.jpg"));
            String resourceImgSt = getImageBASE64(resourceImg);
            BufferedImage puzzleImg = new BufferedImage(targetLength, targetWidth, BufferedImage.TYPE_4BYTE_ABGR);

            cutByTemplate(resourceImg, puzzleImg, blockData, x, 50);

            String resourceUpImgSt = getImageBASE64(resourceImg);
            String puzzleImgSt = getImageBASE64(puzzleImg);

            SliderCheck sliderCheck = new SliderCheck();
            sliderCheck.setSourceImg(resourceImgSt);
            sliderCheck.setImgWidth("290");
            sliderCheck.setImgHeight("147");
            sliderCheck.setModifyImg(resourceUpImgSt);
            sliderCheck.setPuzzleImg(puzzleImgSt);
            sliderCheck.setPuzzleWidth(targetLength + "");
            sliderCheck.setPuzzleHeight(targetWidth + "");
            sliderCheck.setPuzzleYAxis("50");
            sliderCheck.setPuzzleXAxis(x + "");
            return sliderCheck;
        } catch (Exception e) {
            log.error("滑块", e);
            return null;
        }
    }

    public static String getImageBASE64(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        byte[] b = out.toByteArray();//转成byte数组
        return Base64Utils.encodeToString(b);//生成base64编码
    }

    /**
     * @return int[][]
     * @throws
     * @Createdate: 2019年1月24日上午10:52:42
     * @Title: getBlockData
     * @Description: 生成小图轮廓
     * @author mzl
     */
    private static int[][] getBlockData() {

        int[][] data = new int[targetLength][targetWidth];
        double x2 = targetLength - circleR;

        //随机生成圆的位置
        double h1 = circleR + Math.random() * (targetWidth - 3 * circleR - r1);
        double po = circleR * circleR;

        double xbegin = targetLength - circleR - r1;
        double ybegin = targetWidth - circleR - r1;

        for (int i = 0; i < targetLength; i++) {
            for (int j = 0; j < targetWidth; j++) {
                double d3 = Math.pow(i - x2, 2) + Math.pow(j - h1, 2);
                double d2 = Math.pow(j - 2, 2) + Math.pow(i - h1, 2);
                if ((j <= ybegin && d2 <= po) || (i >= xbegin && d3 >= po)) {
                    data[i][j] = 0;
                } else {
                    data[i][j] = 1;
                }

            }
        }
        return data;
    }

    /**
     * @param oriImage
     * @param targetImage
     * @param templateImage
     * @param x
     * @param y             void
     * @throws
     * @Createdate: 2019年1月24日上午10:51:30
     * @Title: cutByTemplate
     * @Description: 生成小图片、给大图片添加阴影
     * @author mzl
     */
    private static void cutByTemplate(BufferedImage oriImage, BufferedImage targetImage, int[][] templateImage, int x, int y) {
        for (int i = 0; i < targetLength; i++) {
            for (int j = 0; j < targetWidth; j++) {
                int rgb = templateImage[i][j];
                // 原图中对应位置变色处理
                int rgb_ori = oriImage.getRGB(x + i, y + j);

                if (rgb == 1) {
                    //抠图上复制对应颜色值
                    targetImage.setRGB(i, j, rgb_ori);
                    //原图对应位置颜色变化
                    oriImage.setRGB(x + i, y + j, rgb_ori & 0x363636);
                } else {
                    //这里把背景设为透明
                    targetImage.setRGB(i, j, rgb_ori & 0x00ffffff);
                }
            }
        }
    }

    public static class SliderCheck {
        // 原图
        private String sourceImg;
        private String imgWidth;
        private String imgHeight;
        // 扣过图的图片
        private String modifyImg;
        // 拼图
        private String puzzleImg;
        private String puzzleWidth;
        private String puzzleHeight;
        // 坐标
        private String puzzleYAxis;
        private String puzzleXAxis;

        public String getSourceImg() {
            return sourceImg;
        }

        public void setSourceImg(String sourceImg) {
            this.sourceImg = sourceImg;
        }

        public String getImgWidth() {
            return imgWidth;
        }

        public void setImgWidth(String imgWidth) {
            this.imgWidth = imgWidth;
        }

        public String getImgHeight() {
            return imgHeight;
        }

        public void setImgHeight(String imgHeight) {
            this.imgHeight = imgHeight;
        }

        public String getModifyImg() {
            return modifyImg;
        }

        public void setModifyImg(String modifyImg) {
            this.modifyImg = modifyImg;
        }

        public String getPuzzleImg() {
            return puzzleImg;
        }

        public void setPuzzleImg(String puzzleImg) {
            this.puzzleImg = puzzleImg;
        }

        public String getPuzzleWidth() {
            return puzzleWidth;
        }

        public void setPuzzleWidth(String puzzleWidth) {
            this.puzzleWidth = puzzleWidth;
        }

        public String getPuzzleHeight() {
            return puzzleHeight;
        }

        public void setPuzzleHeight(String puzzleHeight) {
            this.puzzleHeight = puzzleHeight;
        }

        public String getPuzzleYAxis() {
            return puzzleYAxis;
        }

        public void setPuzzleYAxis(String puzzleYAxis) {
            this.puzzleYAxis = puzzleYAxis;
        }

        public String getPuzzleXAxis() {
            return puzzleXAxis;
        }

        public void setPuzzleXAxis(String puzzleXAxis) {
            this.puzzleXAxis = puzzleXAxis;
        }
    }
}
