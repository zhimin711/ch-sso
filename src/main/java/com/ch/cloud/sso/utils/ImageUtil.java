package com.ch.cloud.sso.utils;

import com.ch.utils.FileUtilsV2;
import com.ch.utils.IOUtils;
import com.mortennobel.imagescaling.DimensionConstrain;
import com.mortennobel.imagescaling.ResampleOp;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;

/**
 * desc:
 *
 * @author zhimi
 * @date 2020/10/20 8:38
 */
public class ImageUtil {
    /**
     * 默认缩放比例
     */
    public final static int SCALE = 10;

    /**
     * 默认缩放后图片宽度
     */
    //public final static int WIDTH = ApplicationConfig.getInstance().getThumbnailWidth();
    public final static int WIDTH = 100;

    /**
     * 默认缩放后图片高度
     */
    //public final static int HEIGHT = ApplicationConfig.getInstance().getThumbnailHeight();
    public final static int HEIGHT = 100;

    /**
     * gif
     */
    public final static String GIF = "gif";

    /**
     * png
     */
    public final static String PNG = "png";


    public static byte[] getImageByUrl(URI uri) {
        File file = new File(uri);
        if (file.exists() && file.isFile()) {
            return file2byte(file);
        }
        return null;
    }

    public static byte[] getImageByPath(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            return file2byte(file);
        }
        return null;
    }

    // public static saveImage
    public static byte[] file2byte(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * 按默认比例缩放图片。
     *
     * @param src        源图片文件流
     * @param fileSuffix 后缀名
     * @return 缩略图文件流
     */
    public static InputStream getThumbnailByDefaultScale(InputStream src,
                                                         String fileSuffix) throws Exception {

        return getThumbnailByScale(src, fileSuffix, SCALE);

    }

    /**
     * 按比例缩放图片。
     *
     * @param src        源图片文件流
     * @param fileSuffix 后缀名
     * @param scale      图片缩放比例
     * @return 缩略图文件流
     */
    public static InputStream getThumbnailByScale(InputStream src,
                                                  String fileSuffix, int scale) throws Exception {

        if (GIF.equals(fileSuffix.toLowerCase())) {
            return src;
        }
        InputStream is = null;
        try {
            BufferedImage image = ImageIO.read(src);
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            int w = width / scale;
            int h = height / scale;

            ResampleOp rsop = new ResampleOp(DimensionConstrain.createMaxDimension(w, h, true));
            BufferedImage to = rsop.filter(image, null);

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
            ImageIO.write(to, fileSuffix, imOut);

            is = new ByteArrayInputStream(bs.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return is;
    }

    /**
     * 按默认宽度高度缩放。
     *
     * @param src        源图片文件流
     * @param fileSuffix 后缀名
     * @return 缩略图文件流
     */
    public static InputStream getThumbnail(InputStream src,
                                           String fileSuffix) throws Exception {

        return compressImage(src, fileSuffix, WIDTH, HEIGHT);

    }

    /**
     * 指定宽度高度缩放。<br>
     * 图片的实际宽高不足时返回null
     *
     * @param src        源图片文件流
     * @param fileSuffix 后缀名
     * @param _width     缩略图宽度
     * @param _height    缩略图高度
     * @return 缩略图文件流
     */
    public static InputStream getThumbnail(InputStream src,
                                           String fileSuffix, int _width, int _height) throws Exception {

        if (GIF.equals(fileSuffix.toLowerCase())) {
            return src;
        }
        InputStream is = null;
        try {
            BufferedImage bi2 = ImageIO.read(src);
            //原图宽高
            int width = bi2.getWidth(null);
            int height = bi2.getHeight(null);
            if (width < _width || height < _height) {
                return null;
            }
            //缩放后宽高
            int newWidth = 0;
            int newHeight = 0;
            if (width <= _width && height <= _height) {
                _width = width;
                _height = height;
            }
            //计算按原图的横向纵向最大比例方向缩放
            //横向图片的场合
            if (width / _width > height / _height) {
                newWidth = _width;
                newHeight = _width * height / width;
            } else {
                newHeight = _height;
                newWidth = _height * width / height;
            }

            ResampleOp rsop = new ResampleOp(DimensionConstrain.createMaxDimension(newWidth, newHeight, true));
            BufferedImage to = rsop.filter(bi2, null);

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
            ImageIO.write(to, fileSuffix, imOut);

            is = new ByteArrayInputStream(bs.toByteArray());

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            IOUtils.close(src);
        }
        return is;

    }

    /**
     * 根据原图压缩为系统允许的最大的尺寸的图片。
     *
     * @param src        源图片文件流
     * @param fileSuffix 后缀名
     * @return 缩略图文件流
     */
    public static InputStream compressMaxImage(InputStream src,
                                               String fileSuffix, int maxWidth) throws Exception {

        return compressImage(src, fileSuffix, maxWidth);

    }

    /**
     * 固定图片宽高缩放（按原图的横向纵向最大比例方向缩放）。
     *
     * @param src        源图片文件流
     * @param fileSuffix 后缀名
     * @param _width     缩略图宽度
     * @return 缩略图文件流
     */
    public static InputStream compressImage(InputStream src,
                                            String fileSuffix, int _width, int _height) throws Exception {

        if (GIF.equals(fileSuffix.toLowerCase())) {
            return src;
        }
        InputStream is = null;
        try {
            BufferedImage bi2 = ImageIO.read(src);
            //原图宽高
            int width = bi2.getWidth(null);
            int height = bi2.getHeight(null);
            //缩放后宽高
            int newWidth = 0;
            int newHeight = 0;
            if (width <= _width && height <= _height) {
                _width = width;
                _height = height;
            }
            //计算按原图的横向纵向最大比例方向缩放
            //横向图片的场合
            if (width / _width > height / _height) {
                newWidth = _width;
                newHeight = _width * height / width;
            } else {
                newHeight = _height;
                newWidth = _height * width / height;
            }

            ResampleOp rsop = new ResampleOp(DimensionConstrain.createMaxDimension(newWidth, newHeight, true));
            BufferedImage to = rsop.filter(bi2, null);

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
            ImageIO.write(to, fileSuffix, imOut);

            is = new ByteArrayInputStream(bs.toByteArray());
            //关于原来的流
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            IOUtils.close(src);
        }
        return is;

    }
    /**
     * 固定图片宽高缩放（按原图的横向纵向最大比例方向缩放）。
     *
     * @param src        源图片文件流
     * @param fileSuffix 后缀名
     * @param _width     缩略图宽度
     * @return 缩略图文件流
     */
    public static InputStream compressImage2(String srcImage, int _width, int _height) {

        InputStream is = null;
        try {
            BufferedImage bi2 = ImageIO.read(new File(srcImage));
            //原图宽高
            int width = bi2.getWidth(null);
            int height = bi2.getHeight(null);
            //缩放后宽高
            int newWidth = 0;
            int newHeight = 0;
            if (width <= _width && height <= _height) {
                _width = width;
                _height = height;
            }
            //计算按原图的横向纵向最大比例方向缩放
            //横向图片的场合
            if (width / _width > height / _height) {
                newWidth = _width;
                newHeight = _width * height / width;
            } else {
                newHeight = _height;
                newWidth = _height * width / height;
            }

            ResampleOp resampleOp = new ResampleOp(DimensionConstrain.createMaxDimension(newWidth, newHeight, true));
            BufferedImage to = resampleOp.filter(bi2, null);

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
            String formatName = FileUtilsV2.getFileExtensionName(srcImage);

            ImageIO.write(to,  formatName, imOut);

            is = new ByteArrayInputStream(bs.toByteArray());
            //关于原来的流
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
//            IOUtils.close(src);
        }
        return is;

    }

    /**
     * 固定图片宽度缩放。
     *
     * @param src        源图片文件流
     * @param fileSuffix 后缀名
     * @param _width     缩略图宽度
     * @return 缩略图文件流
     */
    public static InputStream compressImage(InputStream src,
                                            String fileSuffix, int _width) throws Exception {

        if (GIF.equals(fileSuffix.toLowerCase())) {
            return src;
        }
        InputStream is = null;
        try {
            BufferedImage bi2 = ImageIO.read(src);
            //原图宽高
            int width = bi2.getWidth(null);
            int height = bi2.getHeight(null);
            //缩放后宽高
            int newWidth = 0;
            int newHeight = 0;
            if (width < _width) {
                _width = width;
            }
            newWidth = _width;
            //计算按原图的横向纵向最大比例方向缩放
            //横向图片的场合
            newHeight = height * _width / width;

            ResampleOp rsop = new ResampleOp(DimensionConstrain.createMaxDimension(newWidth, newHeight, true));
            BufferedImage to = rsop.filter(bi2, null);

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
            ImageIO.write(to, fileSuffix, imOut);

            is = new ByteArrayInputStream(bs.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            IOUtils.close(src);
        }
        return is;

    }

    public static void scaleByRatio(File srcFile, String s, int ratio) {
    }
}
