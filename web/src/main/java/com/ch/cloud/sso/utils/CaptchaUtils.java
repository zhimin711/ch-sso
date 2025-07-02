package com.ch.cloud.sso.utils;

import com.ch.cloud.sso.pojo.SlideCaptcha;
import com.ch.e.PubError;
import com.ch.e.ExUtils;
import com.ch.utils.FileUtilsV2;
import com.google.common.collect.Lists;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class CaptchaUtils {


    public static String drawRandomText(int width, int height, BufferedImage verifyImg) {

        Graphics2D graphics = (Graphics2D) verifyImg.getGraphics();

        graphics.setColor(Color.WHITE);//设置画笔颜色-验证码背景色

        graphics.fillRect(0, 0, width, height);//填充背景
        graphics.setFont(new Font("微软雅黑", Font.BOLD, 40));

        //数字和字母的组合
        String baseNumLetter = "123456789abcdefghijklmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";

        StringBuilder sBuffer = new StringBuilder();

        int x = 10;  //旋转原点的 x 坐标

        String ch = "";

        Random random = new Random();

        for (int i = 0; i < 4; i++) {
            graphics.setColor(getRandomColor());

            //设置字体旋转角度
            int degree = random.nextInt() % 30;  //角度小于30度
            int dot = random.nextInt(baseNumLetter.length());
            ch = baseNumLetter.charAt(dot) + "";
            sBuffer.append(ch);

            //正向旋转
            graphics.rotate(degree * Math.PI / 180, x, 45);
            graphics.drawString(ch, x, 45);

            //反向旋转
            graphics.rotate(-degree * Math.PI / 180, x, 45);
            x += 48;

        }

        //画干扰线
        for (int i = 0; i < 6; i++) {
            // 设置随机颜色
            graphics.setColor(getRandomColor());

            // 随机画线
            graphics.drawLine(random.nextInt(width), random.nextInt(height),
                    random.nextInt(width), random.nextInt(height));

        }

        //添加噪点
        for (int i = 0; i < 30; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);

            graphics.setColor(getRandomColor());
            graphics.fillRect(x1, y1, 2, 2);
        }

        return sBuffer.toString();

    }

    /**
     * 随机取色
     */
    private static Color getRandomColor() {
        Random ran = new Random();

        return new Color(ran.nextInt(256), ran.nextInt(256), ran.nextInt(256));

    }

    //  默认图片宽度
    private static final int DEFAULT_IMAGE_WIDTH = 280;

    //  默认图片高度
    private static final int DEFAULT_IMAGE_HEIGHT = 171;

    private static final List<String> verifyImages = Lists.newArrayList();

    /**
     * 获取滑动验证码
     * //     * @param imageVerificationDto 验证码参数
     *
     * @return 滑动验证码
     */
    public static SlideCaptcha selectSlideVerificationCode() {

        SlideCaptcha slideCaptcha;
        try {
//            //  原图路径，这种方式不推荐。当运行jar文件的时候，路径是找不到的，我的路径是写到配置文件中的。
//            String verifyImagePath = URLDecoder.decode(this.getClass().getResource("/").getPath() + "static/targets", "UTF-8");

//            获取模板文件，。推荐文件通过流读取， 因为文件在开发中的路径和打成jar中的路径是不一致的
//            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("static/template/1.png");
            if (verifyImages.isEmpty()) {
                synchronized (verifyImages) {
                    initCaptcha();
                }
            }

            Random random = new Random(System.currentTimeMillis());
            //  随机取得原图文件夹中一张图片
            String origImageFilePath = verifyImages.get(random.nextInt(verifyImages.size()));
            File originImageFile = new File(origImageFilePath);

            String path = CaptchaUtils.class.getResource("/static/images/captcha").getPath();
            //  获取模板图片文件
            File templateImageFile = new File(path + "/template.png");

            //  获取描边图片文件
            File borderImageFile = new File(path + "/border.png");
            //  获取描边图片类型
            String borderImageFileType = borderImageFile.getName().substring(borderImageFile.getName().lastIndexOf(".") + 1);

            //  获取原图文件类型
            String originImageFileType = originImageFile.getName().substring(originImageFile.getName().lastIndexOf(".") + 1);
            //  获取模板图文件类型
            String templateImageFileType = templateImageFile.getName().substring(templateImageFile.getName().lastIndexOf(".") + 1);

            //  读取原图
            BufferedImage verificationImage = ImageIO.read(originImageFile);
            //  读取模板图
            BufferedImage readTemplateImage = ImageIO.read(templateImageFile);

            //  读取描边图片
            BufferedImage borderImage = ImageIO.read(borderImageFile);


            //  获取原图感兴趣区域坐标
            slideCaptcha = ImageVerifyUtil.generateCutoutCoordinates(verificationImage, readTemplateImage);

            int Y = slideCaptcha.getY();
            //  在分布式应用中，可将session改为redis存储
//            getRequest().getSession().setAttribute("imageVerificationVo", imageVerificationVo);

            //  根据原图生成遮罩图和切块图
            slideCaptcha = ImageVerifyUtil.pictureTemplateCutout(originImageFile, originImageFileType, templateImageFile, templateImageFileType, slideCaptcha.getX(), slideCaptcha.getY());

            //   剪切图描边
            slideCaptcha = ImageVerifyUtil.cutoutImageEdge(slideCaptcha, borderImage, borderImageFileType);
            slideCaptcha.setY(Y);

            return slideCaptcha;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return null;
    }

    private static void initCaptcha() {
        if (!verifyImages.isEmpty()) return;
        String path = CaptchaUtils.class.getResource("/static/images/captcha").getPath();
        File origImageDir = new File(path, "orig");
        File verifyImageDir = new File(path, "verify");
        if (!verifyImageDir.exists()) {
            FileUtilsV2.create(verifyImageDir);
        }
        File[] verifyImageFiles = origImageDir.listFiles();
        if (verifyImageFiles == null || verifyImageFiles.length == 0) {
            ExUtils.throwError(PubError.NOT_EXISTS, "验证码图片文件不存在！");
        }
        for (File f : verifyImageFiles) {
            File vFile = new File(verifyImageDir.getPath(), f.getName());
            if (vFile.exists()) {
                continue;
            }
            try {
//                ImageUtils.reduceImageBy(f.getPath(), vFile.getPath(), DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
                ImageUtil.scaleByRatio(f, "", DEFAULT_IMAGE_WIDTH / DEFAULT_IMAGE_HEIGHT);
                verifyImages.add(vFile.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
