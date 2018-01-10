package com.creditease.monitor.http;


import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by MagicBean on 2016/01/13 10:10:37
 */
public interface Api {
    // WXAlert
    @FormUrlEncoded
    @POST("/sendMessageByProNameLimit.json")
    Observable<JSONObject> WXAlert(@Field("title") String title,
                                   @Field("remark") String remark,
                                   @Field("message") String message,
                                   @Field("alarmTime") String alarmTime,
                                   @Field("proName") String proName,
                                   @Field("url") String url,
                                   @Field("type") String type);
}