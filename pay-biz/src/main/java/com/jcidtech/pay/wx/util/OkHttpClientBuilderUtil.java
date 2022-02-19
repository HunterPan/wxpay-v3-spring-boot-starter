package com.jcidtech.pay.wx.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.internal.platform.Platform;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttpClientBuilderUtil {

    private OkHttpClientBuilderUtil() {

    }

    public static OkHttpClient.Builder wxPayOkHttpClient(Interceptor interceptor) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.hostnameVerifier((hostname, session) -> hostname.endsWith(".mch.weixin.qq.com"))
                .readTimeout(6000, TimeUnit.MILLISECONDS)
                .writeTimeout(10000, TimeUnit.MILLISECONDS)
                .connectTimeout(3000, TimeUnit.MILLISECONDS);
        if (interceptor != null) {
            builder.addInterceptor(interceptor);
        }
        return builder;
    }
    public static OkHttpClient.Builder wxPayOkHttpClientWithSsl(Interceptor interceptor,String mchId,String p12Path){
        SSLContext sslContext = getSSLContextByAppKey(mchId,p12Path);
        if (sslContext == null) {
            log.error("获取证书失败");
            return null;
        }
        return wxPayOkHttpClient(interceptor).sslSocketFactory(sslContext.getSocketFactory(),
                (X509TrustManager) buildTrustManager()[0]);
    }

    private static  SSLContext getSSLContextByAppKey(String mchId,String p12Path) {
        // 设置证书路径
        String certPath = p12Path;
        // 设置证书密码
        String certPass = mchId;
        // 获取证书
        return getSSLContext(certPath, certPass);
    }
    private static SSLContext getSSLContext(String certPath, String certPass) {
        try {
            KeyStore clientStore = KeyStore.getInstance("PKCS12");
            // 读取resource下的文件 支持jar方式启动
            Resource resource = new ClassPathResource(certPath.replace("classpath:", ""));
            InputStream inputStream = resource.getInputStream();
            char[] passArray = certPass.toCharArray();
            clientStore.load(inputStream, passArray);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(clientStore, passArray);
            KeyManager[] kms = kmf.getKeyManagers();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kms, null, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            log.error("设置证书出错",e);
        }
        return null;
    }
    private static TrustManager[] buildTrustManager(){
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }
}
