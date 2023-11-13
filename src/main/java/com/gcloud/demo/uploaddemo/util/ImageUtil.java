package com.gcloud.demo.uploaddemo.util;

import com.gcloud.demo.uploaddemo.exception.GcException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
@Slf4j
public class ImageUtil {
    /**
     * 将图片转换成Base64编码
     * @param file 图片
     * @return
     */
    public static String getImgBase64(MultipartFile file) {

        // 将图片文件转化为二进制流
        InputStream in = null;
        byte[] data = null;
        // 读取图片字节数组
        try {
            in = file.getInputStream();
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 图片头
        //String imghead = "data:image/jpeg;base64,";
        return Base64.getEncoder().encodeToString(data);
    }

    public static String getImageStr(InputStream inputStream){

        String fileContentBase64 = null;
        String base64Str = "";//""data:" + fileType + ";base64,";
        String content = null;
        //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        byte[] data = null;
        //读取图片字节数组
        try {
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            //对字节数组Base64编码
            if (data == null || data.length == 0) {
                return null;
            }
            content = Base64.getEncoder().encodeToString(data);
            if (content == null || "".equals(content)) {
                return null;
            }
            fileContentBase64 = base64Str + content;
        } catch (Exception e) {
            log.error("",e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("",e);
                }
            }
        }
        return fileContentBase64;
    }

    public static String getBase64ByImgUrl(String url) {
        String suffix = url.substring(url.lastIndexOf(".") + 1);
        try {
            URL urls = new URL(url);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Image image = Toolkit.getDefaultToolkit().getImage(urls);
            BufferedImage biOut = toBufferedImage(image);
            ImageIO.write(biOut, suffix, baos);
            String base64Str = cn.hutool.core.codec.Base64.encode(baos.toByteArray());
            return base64Str;
        } catch (Exception e) {
            return "";
        }
    }

    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            int transparency = Transparency.OPAQUE;
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null),
                    image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }
        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(image.getWidth(null),
                    image.getHeight(null), type);
        }
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }

    /**
     * 通过图片的url获取图片的base64字符串
     *
     * @param imgUrl 图片url
     * @return 返回图片base64的字符串
     */
    public static String image2Base64(String imgUrl) {
        URL url = null;
        InputStream is = null;
        ByteArrayOutputStream outStream = null;
        HttpURLConnection httpUrl = null;
        try {
            url = new URL(imgUrl);
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            httpUrl.getInputStream();
            is = httpUrl.getInputStream();
            outStream = new ByteArrayOutputStream();
            //创建一个Buffer字符串
            byte[] buffer = new byte[1024];
            //每次读取的字符串长度，如果为-1，代表全部读取完毕
            int len = 0;
            //使用一个输入流从buffer里把数据读取出来
            while ((len = is.read(buffer)) != -1) {
                //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
                outStream.write(buffer, 0, len);
            }
            // 对字节数组Base64编码
            return cn.hutool.core.codec.Base64.encode(outStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpUrl != null) {
                httpUrl.disconnect();
            }
        }
        return imgUrl;
    }


    public static void main(String[] args) {
//        String imgBase = getImgBase1("D:\\uploadtest\\a63accbb-9952-452d-92e4-125010bb948d.jpeg");
        File file = new File("D:\\uploadtest\\f3c30e9e-b620-4043-888d-e864efe9fc3d.jpeg");
        //file转inputStream
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String imageBase = getImageStr(inputStream);
        System.out.println(imageBase);
    }

    /**
     * 将图片字符流传入response中，并显示指定文件名称
     * @param response
     * @param file  图片文件
     * @param showFileName  指定文件名称
     * @return
     */
    public static HttpServletResponse responsePushImage(HttpServletResponse  response, File file,String showFileName) {
        OutputStream outputStream = null;
        FileInputStream fileInput = null;

        String contentType = "image/jpeg";
        String suffix = "jpg";
        if(file.getName().lastIndexOf(".") > -1){
            suffix = file.getName().toLowerCase().substring(file.getName().lastIndexOf("."),file.getName().length());
//            contentType = "image/" + showFileName + suffix;
        }

         // image/jpg  image/jpeg  image/png

        response.addHeader("Content-Disposition", "filename=" + showFileName + suffix);
        response.setCharacterEncoding("utf-8");
        response.setContentType(contentType);

        if (file == null || !file.exists()) {
            throw new GcException("file_util_file_is_not_exists_001::文件不存在");
        }

        try {
            fileInput = new FileInputStream(file);
            outputStream = response.getOutputStream();
            int count = 0;
            byte[] buffer = new byte[1024 * 8];
            while ((count = fileInput.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
                outputStream.flush();
            }
        } catch (Exception e) {

            throw new GcException("file_util_file_stream_create_error_001::文件流获取错误");
        } finally {
            try {
                if (fileInput != null) {
                    fileInput.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {

                throw new GcException("file_util_file_stream_close_error_002::文件流关闭错误");
            }
        }

        return response;
    }
}
