package com.ch;

import com.ch.utils.FileUtilsV2;
import org.junit.Test;

import java.io.File;

/**
 * desc:
 *
 * @author zhimi
 * @since 2020/10/22 8:16
 */
public class ImageTests {

    String newFilePath;

    @Test
    public void test1() {

        newFilePath = "D:\\work\\gitee\\ch-cloud\\ch-sso\\src\\main\\resources\\static\\images\\captcha";
        File dir = new File(newFilePath, "orig");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0)
                for (File f : files) {
                    System.out.println(f.getName() + " \t => " + FileUtilsV2.convertSize(f.length()));
                    File nf = new File(newFilePath + "\\scala2", f.getName());
                    FileUtilsV2.create(nf);
//                    ImageUtil.compressImage2(f.getPath(), nf.getPath(), 400, 400);
                }
        }
    }
}
