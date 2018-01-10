package com.creditease.monitor.http;


import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import okhttp3.OkHttpClient;
import org.springframework.util.StringUtils;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.util.concurrent.TimeUnit;

/**
 * Target: 提供Retrofit的接口
 */
public class RetrofitProvider {
	private static String ENDPOINT = "http://10.100.130.110:8080";

	private RetrofitProvider() {
	}

	// 1.
	public static Api getService() {
		return RetrofitProvider.getInstance().create(Api.class);
	}

	public static Retrofit setBaseUrl(String endpoint) {
		ENDPOINT = endpoint;
		return SingletonHolder.INSTANCE;
	}

	// 2.
	public static Retrofit getInstance() {
		if (StringUtils.isEmpty(ENDPOINT)){
			throw new RuntimeException("Retrofit not init api server!");
		}
		return SingletonHolder.INSTANCE;
	}


	/**
	 * Target: 提供唯一的Retrofit单例
	 */
	private static class SingletonHolder {
		private static final Retrofit INSTANCE = create();
		private static final int TIME_OUT = 20;

		private static Retrofit create() {
			OkHttpClient okHttpClient = new OkHttpClient.Builder()
					.readTimeout(TIME_OUT, TimeUnit.SECONDS)
					.connectTimeout(TIME_OUT, TimeUnit.SECONDS)
					.writeTimeout(TIME_OUT, TimeUnit.SECONDS)
					.hostnameVerifier(new HostnameVerifier() {
						@Override
						public boolean verify(String hostname, SSLSession session) {
							return true;
						}
					})
//					.addInterceptor(new InterceptorProvider().init())
					.build();
			return new Retrofit.Builder()
					.baseUrl(ENDPOINT)
					.client(okHttpClient)
					.addConverterFactory(GsonConverterFactory.create())
					.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
					.build();
		}
	}
}
