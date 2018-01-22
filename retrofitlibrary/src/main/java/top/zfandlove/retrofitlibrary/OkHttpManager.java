package top.zfandlove.retrofitlibrary;

import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by zfan on 2017/12/27.
 */

public class OkHttpManager {
    Map<String, String> httpHeaderMap;

    public OkHttpManager(Map<String, String> httpHeaderMap) {
        this.httpHeaderMap = httpHeaderMap;
    }

    public OkHttpClient getHttpsClient(SSLi sslManager, int timeoutSec,boolean debug) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(getHttpLoggingInterceptor(debug))
                .addInterceptor(getHttpHeaderInterceptor())
                .addNetworkInterceptor(new StethoInterceptor())
                .hostnameVerifier(sslManager.getHostnameVerifier())
                .sslSocketFactory(sslManager.getSSLCertification(), sslManager.getX509TrustManager())
                .connectTimeout(timeoutSec, TimeUnit.SECONDS)
                .writeTimeout(timeoutSec, TimeUnit.SECONDS)
                .readTimeout(timeoutSec, TimeUnit.SECONDS)
                .build();
        return client;
    }

    public OkHttpClient getHttpClient(int timeoutSec,boolean debug) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(getHttpLoggingInterceptor(debug))
                .addInterceptor(getHttpHeaderInterceptor())
                .addNetworkInterceptor(new StethoInterceptor())
                .connectTimeout(timeoutSec, TimeUnit.SECONDS)
                .writeTimeout(timeoutSec, TimeUnit.SECONDS)
                .readTimeout(timeoutSec, TimeUnit.SECONDS)
                .build();
        return client;
    }

    public HttpLoggingInterceptor getHttpLoggingInterceptor(final boolean debug) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                if(!debug)return;
                Log.d("zfan_http", "OkHttpManager:" + message);
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return loggingInterceptor;
    }

    public Interceptor getHttpHeaderInterceptor() {
        Interceptor httpHeaderInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder();
                for (String key : httpHeaderMap.keySet()) {
                    builder.addHeader(key, httpHeaderMap.get(key));
                }
                Request request = builder.build();
                return chain.proceed(request);
            }
        };
        return httpHeaderInterceptor;
    }

}
