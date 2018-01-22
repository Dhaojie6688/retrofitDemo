package top.zfandlove.retrofitlibrary;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by zfan on 2017/12/27.
 */

public interface SSLi {
    /**
     * 获取证书工厂
     *
     * @return SSLSocketFactory
     */
    SSLSocketFactory getSSLCertification();

    /**
     * 域名验证
     *
     * @return HostnameVerifier
     */
    HostnameVerifier getHostnameVerifier();

    /**
     * X509TrustManager
     *
     * @return X509TrustManager
     */
    X509TrustManager getX509TrustManager();
}
