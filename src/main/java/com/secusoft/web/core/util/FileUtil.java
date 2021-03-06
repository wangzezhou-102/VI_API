package com.secusoft.web.core.util;

import com.secusoft.web.core.exception.BizException;
import com.secusoft.web.core.exception.BizExceptionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtil {

    private static Logger log = LoggerFactory.getLogger(FileUtil.class);

    /**
     * NIO way
     */
    public static byte[] toByteArray(String filename) {

        File f = new File(filename);
        if (!f.exists()) {
            log.error("文件未找到！" + filename);
            throw new BizException(BizExceptionEnum.FILE_NOT_FOUND);
        }
        FileChannel channel = null;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(f);
            channel = fs.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
            while ((channel.read(byteBuffer)) > 0) {
                // do nothing
                // System.out.println("reading");
            }
            return byteBuffer.array();
        } catch (IOException e) {
            throw new BizException(BizExceptionEnum.FILE_READING_ERROR);
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                throw new BizException(BizExceptionEnum.FILE_READING_ERROR);
            }
            try {
                fs.close();
            } catch (IOException e) {
                throw new BizException(BizExceptionEnum.FILE_READING_ERROR);
            }
        }
    }

    /**
     * 删除目录
     *
     *
     * @Date 2017/10/30 下午4:15
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    //i don't konw why ,but it work
    public static boolean forceDelete(File f)
    {
        boolean result = false;
        int tryCount = 0;
        while(!result && tryCount++ <10)
        {
            System.gc();
            result = f.delete();
        }
        return result;
    }
    
    public static void deleteFolder(File folder) {
    	File[] files = folder.listFiles();
		if(files!=null) {
			for(File f:files) {
				if(f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
    }
}