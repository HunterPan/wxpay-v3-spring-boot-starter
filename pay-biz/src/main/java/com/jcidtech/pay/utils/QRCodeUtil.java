package com.jcidtech.pay.utils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.jcidtech.pay.common.enums.PayChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class QRCodeUtil {
    private static final int QRCOLOR = 0xFF000000; // 默认是黑色
    private static final int BGWHITE = 0xFFFFFFFF; // 背景颜色
    //设置图片的文字编码以及内边框
    private final static Map<EncodeHintType, Object> hints = new HashMap() {
        {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            //编码
            put(EncodeHintType.CHARACTER_SET, "UTF-8");
            //边框距
            put(EncodeHintType.MARGIN, 0);
        }
    };
    //设置图片的文字编码以及内边框
    private final static Map<PayChannel, String> LOGO_PATH = new HashMap() {
        {
            put(PayChannel.WX, "classpath:logo/wx-pay-logo.jpeg");
        }
    };

    public static String generateQRCode(String content, PayChannel payChannel, Integer width, Integer height) {
        BitMatrix bitMatrix;
        String logPath = LOGO_PATH.get(payChannel);
        try {
            //参数分别为：编码内容、编码类型、图片宽度、图片高度，设置参数
            bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            // 开始利用二维码数据创建Bitmap图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? QRCOLOR : BGWHITE);
                }
            }
            int realWidth = image.getWidth();
            int realHeight = image.getHeight();
            InputStream inputStream = null;
            try {
                Resource resource = new ClassPathResource(logPath.replace("classpath:", ""));
                inputStream = resource.getInputStream();
            }catch (Exception e){
            }
            if (Objects.nonNull(inputStream) && inputStream.available()>0) {
                // 构建绘图对象
                Graphics2D g = image.createGraphics();
                // 读取Logo图片
                BufferedImage logo = ImageIO.read(inputStream);
                // 开始绘制logo图片
                g.drawImage(logo, realWidth * 2 / 5, realHeight * 2 / 5, realWidth * 2 / 10, realHeight * 2 / 10, null);
                g.dispose();
                logo.flush();
            }
            ImageIO.write(image, "png", out);
            byte[] bytes = out.toByteArray();
            BASE64Encoder encoder = new BASE64Encoder();
            String binary = encoder.encodeBuffer(bytes).replaceAll("\r", "").replaceAll("\n", "").trim();
            return binary;
        } catch (WriterException e) {
            log.error("create item qr error", e);
        } catch (IOException e) {
            log.error("create item qr error", e);
        }
        return null;
    }

}