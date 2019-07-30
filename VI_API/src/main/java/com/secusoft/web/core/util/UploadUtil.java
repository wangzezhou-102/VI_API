package com.secusoft.web.core.util;

import com.secusoft.web.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author huanghao
 * @date 2019-06-20
 */
public class UploadUtil {

    private static Logger log = LoggerFactory.getLogger(UploadUtil.class);
    public static String basePath = System.getProperty("user.dir") + "/resources";
    //public static String basePath = System.getProperty("user.dir") + "/src/main/resources";


    /**
     * 获得文件名
     *
     * @return
     */
    public static String getFolder() {
        // 图片保存路径为pic  图片在数据库中的存储路径为static
        String dateStr = new SimpleDateFormat("yyyyMM").format(new Date());
        String sep = "/";
        //创建文件夹的名称 类似 \pic\201906\      System.getProperty("file.separator")
        return sep + dateStr + sep;
    }

    /**
     * 通过网络地址获取字节数组
     *
     * @param strUrl
     * @return
     */
    public static byte[] getImageFromNetByUrl(String strUrl) {
        try {
            //要放到try/catch里面
            URL url = new URL(strUrl);
            // 打开连接
            URLConnection con = url.openConnection();
            // 设置请求超时为5s
            con.setConnectTimeout(5 * 1000);
            // 输入流
            InputStream is = con.getInputStream();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            is.close();
            byte[] bytes = outStream.toByteArray();

            outStream.close();
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从网络Url中下载文件
     *
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static void downLoadFromUrl(String urlStr, String fileName, String savePath) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3 * 1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);
        //文件保存位置
        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }
        File file = new File(saveDir + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if (fos != null) {
            fos.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
        log.info("info:" + url + " download success");
    }

    /**
     * 将base64存为图片文件的形式
     *
     * @param getData
     * @param type
     * @throws IOException
     */
    public static String downLoadFromBase64(String getData, String type) throws IOException {

        //图片收藏需要下载到本地
        String folderName = "/" + type + getFolder();
        String fullName = basePath + folderName;
        //创建文件名称  类似 org_ehWbXqMCZkg6KwRKsU31Cs.jpg
        String ImgName = UUIDUtil.getUid(type + "_") + ".jpg";
        try {

            //创建并下载到相应的文件夹
            Files.createDirectories(Paths.get(fullName));

            //文件保存位置
            File saveDir = new File(fullName);
            if (!saveDir.exists()) {
                saveDir.mkdir();
            }
            File file = new File(saveDir + File.separator + ImgName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(ImageUtils.decode(getData));
            if (fos != null) {
                fos.close();
            }

            log.info("info:" + ImgName + " save img success");
        } catch (Exception e) {
            e.printStackTrace();
            Path path1 = Paths.get(fullName, ImgName);
            try {
                Files.deleteIfExists(path1);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
        return "/static"+folderName + ImgName;
    }


    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }
}

