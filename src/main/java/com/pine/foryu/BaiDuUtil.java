package com.pine.foryu;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

public class BaiDuUtil {

    public static String APIKey = "***";
    public static String APISecret = "***";
    public static String accessTokenUrl = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + APIKey + "&client_secret=" + APISecret;

    public static String getAccessToken() {
        String body = HttpRequest.post(accessTokenUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .execute()
                .body();
        JSONObject jsonData = new JSONObject(body);
        String accessToken = (String) jsonData.get("access_token");


        return accessToken;
    }

    /**
     * 自动分析文章标签，由于是免费试用的 API ，所以该接口的 QPS 的上线被限制为 2，因此在使用的时候可能会导致异常
     * @param content
     * @return
     */
    public static ArrayList<String> getLabel(ResponseBody.Post content) {
        String url = "https://aip.baidubce.com/rpc/2.0/nlp/v1/keyword?charset=UTF-8&access_token=";

        JSONObject param = new JSONObject(content);
        HttpResponse execute = HttpRequest.post(url + getAccessToken())
                .header("Content-Type", "application/json")
                .body(param.toString())
                .execute();
        String data = execute.body();
        JSONArray label = new JSONArray(new JSONObject(data).get("items"));
        ArrayList<String> labels = new ArrayList<>();
        for (Object o : label) {
            JSONObject tempLabel = new JSONObject(o);
            String singleLabel = (String) tempLabel.get("tag");
            labels.add(singleLabel);
        }

        return labels;
    }

    public static void main(String[] args) {
        JSONObject entries = new JSONObject();
        entries.set("content", "大家好，我是鱼皮。\n");
        entries.set("title", "送了老弟一台 Linux 服务器，它又懵了！");
    }
}
