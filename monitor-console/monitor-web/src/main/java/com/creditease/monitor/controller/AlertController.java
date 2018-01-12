package com.creditease.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.creditease.monitor.http.RetrofitProvider;
import com.creditease.monitor.response.ResponseCode;
import com.creditease.monitor.vo.AlertVO;
import com.creditease.response.Response;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlertController {
    private static Logger logger = LoggerFactory.getLogger(MonitorApplicationController.class);

    @PostMapping("/checkWebhook")
    public Response checkWebhook(@RequestBody JSONObject object) {
        if (object == null) {
            return Response.fail(ResponseCode.DATA_SOURCE_NOT_EXISTS);
        }
        AlertVO alertVO = JSON.parseObject(JSON.toJSONString(object), AlertVO.class);
        logger.info("VO {}", JSON.toJSONString(alertVO));
        sendRequest(alertVO);
        return Response.ok(object);
    }

    private void sendRequest(AlertVO alertVO) {
        String message = "";
        if (alertVO.getEvalMatches().size() != 0) {
            message = alertVO.getEvalMatches().get(0).getMetric()
                    + "="
                    + alertVO.getEvalMatches().get(0).getValue();
        }
        RetrofitProvider.getService()
                .WXAlert(alertVO.getTitle() + "",
                        alertVO.getState() + "",
                        message + "",
                        System.currentTimeMillis() + "",
                        "Grafana",
                        alertVO.getRuleUrl() + "",
                        "")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<JSONObject>() {
                    @Override
                    public void accept(JSONObject object) throws Exception {
                        logger.info("Success {}", JSON.toJSONString(object));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        logger.info("Exception {}", throwable.toString());
                    }
                });
    }
}
