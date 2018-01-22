package top.zfandlove.retrofitlibrary;

import android.content.Context;
import android.text.TextUtils;

import com.facebook.stetho.Stetho;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by zfan on 2018/1/16.
 */

public final class RetrofitFactory {
    public static final boolean DEBUG_ENABLE = true;
    public static final boolean DEBUG_DISABLE = false;
    Retrofit retrofit;

    RetrofitFactory(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    /**
     * 初始化facebook App听诊器
     */
    public static void initStetho(Context context, boolean debug) {
        if (!debug) return;
        Stetho.initializeWithDefaults(context);
    }

    static Retrofit getGsonRetrofit(String baseUrl, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }

    static Retrofit getStringRetrofit(String baseUrl, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }

    static Retrofit getDefaultRetrofit(String baseUrl, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }

    public static Map<String, String> getJsonHeaderMap() {
        Map<String, String> jsonHeaderMap = new HashMap();
        jsonHeaderMap.put("Content-Type", "application/json");
        jsonHeaderMap.put("Accept", "application/json");
        return jsonHeaderMap;
    }

    /**
     * 返回 interface Service Api
     */
    public <S> S create(Class<S> clazz) {
        return retrofit.create(clazz);
    }

    public enum HttpType {
        HTTP, HTTPS;

        HttpType() {
        }
    }


    public enum TransferDataType {
        STRING, GSON, DEFAULT;

        TransferDataType() {
        }
    }

    public static final class Builder {
        private Context context;
        private String baseUrl;
        private int timeoutSec = 30;
        private String[] hostnameVerifiers;
        private String assetCertificateName;
        private Map<String, String> httpHeaderMap;
        private RetrofitFactory.HttpType httpType;
        private RetrofitFactory.TransferDataType transferDataType;
        private SSLi ssLi;
        private boolean debug;

        public Builder() {
        }

        /**
         * 设置上下文对象，用于获取证书流对象
         */
        public RetrofitFactory.Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        /**
         * 设置上下文对象，用于获取证书流对象
         */
        public RetrofitFactory.Builder isDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        /**
         * 设置请求的根地址
         */
        public RetrofitFactory.Builder setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * 设置OKHttp超时时间
         */
        public RetrofitFactory.Builder setTimeoutSec(int timeoutSec) {
            this.timeoutSec = timeoutSec;
            return this;
        }

        /**
         * 设置验证的host
         */
        public RetrofitFactory.Builder setHostnameVerifiers(String[] hostnameVerifiers) {
            this.hostnameVerifiers = hostnameVerifiers;
            return this;
        }

        /**
         * 设置证书名称，证书需放在asset目录下
         */
        public RetrofitFactory.Builder setAssetCertificateName(String assetCertificateName) {
            this.assetCertificateName = assetCertificateName;
            return this;
        }

        /**
         * 设置请求头
         */
        public RetrofitFactory.Builder setHttpHeaderMap(Map<String, String> httpHeaderMap) {
            this.httpHeaderMap = httpHeaderMap;
            return this;
        }

        /**
         * 设置http类型，http/https
         */
        public RetrofitFactory.Builder setHttpType(RetrofitFactory.HttpType httpType) {
            this.httpType = httpType;
            return this;
        }

        /**
         * 设置响应数据类型 string/json/default
         */
        public RetrofitFactory.Builder setTransferDataType(RetrofitFactory.TransferDataType transferDataType) {
            this.transferDataType = transferDataType;
            return this;
        }

        /**
         * 设置ssl, 可继承SSLHelper类，实现ssl的校验操作等
         */
        public RetrofitFactory.Builder setSSLHelper(SSLi ssLi) {
            this.ssLi = ssLi;
            return this;
        }

        public RetrofitFactory build() {
            checkParams();
            Retrofit retrofit = null;
            OkHttpClient okHttpClient = null;
            if (httpHeaderMap == null) {
                httpHeaderMap = new HashMap<>();
            }
            OkHttpManager okHttpManager = new OkHttpManager(httpHeaderMap);
            if (httpType == null) {
                httpType = RetrofitFactory.HttpType.HTTP;
            }
            if (httpType == RetrofitFactory.HttpType.HTTP) {
                okHttpClient = okHttpManager.getHttpClient(timeoutSec, debug);
            } else if (httpType == RetrofitFactory.HttpType.HTTPS) {
                if (ssLi == null) {
                    ssLi = new SSLManager(context, hostnameVerifiers, assetCertificateName);
                }
                okHttpClient = okHttpManager.getHttpsClient(ssLi, timeoutSec, debug);
            }
            if (transferDataType == null) {
                transferDataType = RetrofitFactory.TransferDataType.DEFAULT;
            }
            if (transferDataType == RetrofitFactory.TransferDataType.DEFAULT) {
                retrofit = getDefaultRetrofit(baseUrl, okHttpClient);
            } else if (transferDataType == RetrofitFactory.TransferDataType.STRING) {
                retrofit = getStringRetrofit(baseUrl, okHttpClient);
            } else if (transferDataType == RetrofitFactory.TransferDataType.GSON) {
                retrofit = getGsonRetrofit(baseUrl, okHttpClient);
            }
            RetrofitFactory retrofitFactory = new RetrofitFactory(retrofit);
            return retrofitFactory;
        }

        private void checkParams() {
            if (TextUtils.isEmpty(baseUrl)) {
                throw new RuntimeException("Please call setBaseUrl method, baseUrl can't be null");
            }
            if (httpType != null && httpType == RetrofitFactory.HttpType.HTTPS && ssLi == null) {
                if (context == null) {
                    throw new RuntimeException("Please call setContext method, context can't be null");
                }
                if (TextUtils.isEmpty(assetCertificateName)) {
                    throw new RuntimeException("Please call setAssetCertificateName method, assetCertificateName can't be null");
                }
                if (hostnameVerifiers == null) {
                    throw new RuntimeException("Please call setHostnameVerifiers method, hostnameVerifiers can't be null");
                }
            }
        }
    }

}
