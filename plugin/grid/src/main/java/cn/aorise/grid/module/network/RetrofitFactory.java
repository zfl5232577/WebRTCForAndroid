package cn.aorise.grid.module.network;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import cn.aorise.common.core.config.AoriseConfig;
import cn.aorise.common.core.util.AoriseLog;
import cn.aorise.grid.module.cache.UserInfoCache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pc on 2017/3/8.
 */
public class RetrofitFactory {
    private static RetrofitFactory sInstance;
    // private OkHttpClient mOkHttpClient;
    private OkHttpClient.Builder mHttpBuilder;
    private Retrofit.Builder mRetrofitBuilder;
    private Retrofit mRetrofit;


    public synchronized static RetrofitFactory getInstance() {
        if (sInstance == null) {
            sInstance = new RetrofitFactory();
        }
        return sInstance;
    }


    private RetrofitFactory() {
        mHttpBuilder = new OkHttpClient().newBuilder();
        mRetrofitBuilder = new Retrofit.Builder();
    }

    @Deprecated
    public OkHttpClient getHttpsClient(boolean debug) {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            mHttpBuilder = new OkHttpClient.Builder()
                    .connectTimeout(AoriseConfig.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(AoriseConfig.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(AoriseConfig.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .sslSocketFactory(sslSocketFactory)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });

            if (debug) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                mHttpBuilder.addInterceptor(interceptor);
            }

            return mHttpBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OkHttpClient getOkHttpsClient(boolean debug) {
        try {
            X509TrustManager manager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                }

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }

                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            };
            TrustManager[] trustAllCerts = new TrustManager[]{manager};

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory factory = sc.getSocketFactory();

            mHttpBuilder
                    .connectTimeout(AoriseConfig.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(AoriseConfig.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(AoriseConfig.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(mSessionInterceptor)
                    .addInterceptor(sLoggingInterceptor)
                    .sslSocketFactory(factory, manager)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
//                    .addInterceptor(new Interceptor() {
//                        @Override
//                        public Response intercept(Chain chain) throws IOException {
//                            Request request = chain.request()
//                                    .newBuilder()
//                                    .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
//                                    .addHeader("Accept-Encoding", "gzip, deflate")
//                                    .addHeader("Connection", "keep-alive")
//                                    .addHeader("Accept", "*/*")
//                                    .addHeader("Cookie", "add cookies here")
//                                    .build();
//                            return chain.proceed(request);
//                        }
//                    });

            if (debug) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                mHttpBuilder.addInterceptor(interceptor);
            }

            return mHttpBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T create(boolean debug, final Class<T> service, String uri) {
        AoriseLog.i("RetrofitFactory", "debug = " + debug);

        if (null != mHttpBuilder && null != mRetrofitBuilder) {
            OkHttpClient client = getOkHttpsClient(debug);

            mRetrofit = mRetrofitBuilder.baseUrl(uri)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return mRetrofit.create(service);
    }

    //创建Session的拦截器
    private final Interceptor mSessionInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request.Builder builder = originalRequest.newBuilder();
            if (!TextUtils.isEmpty(UserInfoCache.getSetCookie())) {
                builder.header("cookie", UserInfoCache.getSetCookie());
                AoriseLog.i("cookie---", UserInfoCache.getSetCookie());
            }
            AoriseLog.e("HEADER", UserInfoCache.getSetCookie());
            Request authorised = builder.build();
            return chain.proceed(authorised);
        }
    };

    /**
     * 打印返回的json数据拦截器
     */
    private static final Interceptor sLoggingInterceptor = new Interceptor() {

        @Override
        public Response intercept(Chain chain) throws IOException {
            final Request request = chain.request();
            Buffer requestBuffer = new Buffer();
            if (request.body() != null) {
                request.body().writeTo(requestBuffer);
            } else {
                AoriseLog.d("LogTAG", "request.body() == null");
            }
            //打印url信息
            AoriseLog.w("打印url信息:" + request.url() + (request.body() != null ? "?" + _parseParams(request.body(), requestBuffer) : ""));
            final Response response = chain.proceed(request);
            return response;
        }
    };

    @NonNull
    private static String _parseParams(RequestBody body, Buffer requestBuffer) throws UnsupportedEncodingException {
        if (body.contentType() != null && !body.contentType().toString().contains("multipart")) {
            return URLDecoder.decode(requestBuffer.readUtf8(), "UTF-8");
        }
        return "null";
    }
}
