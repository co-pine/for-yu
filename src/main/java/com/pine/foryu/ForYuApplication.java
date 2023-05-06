package com.pine.foryu;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ForYuApplication {

    private static final Log log = LogFactory.get();

    public static void main(String[] args) {
        // 存储所有文章
        List<ResponseBody.Post> postList = new ArrayList<>();

        String url = "https://www.code-nav.cn/api/post/list/page/vo";
        String cookie = "***";
        String referer = "https://www.code-nav.cn/user/1626574509983178753/post";
        String origin = "https://www.code-nav.cn";
        ForYuApplication forYuApplication = new ForYuApplication();
        // current 、pageSize 的值需加上引号 "1"，否则会转换异常
        JSONObject params = forYuApplication.getParams("***");

        ResponseBody responseBody = forYuApplication.getPost(url, cookie, referer, origin, params);
        List<ResponseBody.Post> records = responseBody.getData().getRecords();
        postList.addAll(records);
        // 当数据较多时，不断增加页码以获得全部数据
        Integer current = Integer.valueOf((String) params.get("current"));
        Integer size = responseBody.getData().getRecords().size();
        while (size > 0) {
            params.set("current", ++current);
            ResponseBody pageData = forYuApplication.getPost(url, cookie, referer, origin, params);
            postList.addAll(pageData.getData().getRecords());

            size = pageData.getData().getRecords().size();
        }

        HashMap<String, List<PostWithLabel>> labelToPost = forYuApplication.makeLabel(postList);

        System.out.println(labelToPost);
    }

    /**
     * 将参数转换为 json 对象，方便局部修改参数
     *
     * @param stringParams
     * @return
     */
    public JSONObject getParams(String stringParams) {
        JSONObject params = null;
        try {
            params = new JSONObject(stringParams);
        } catch (JSONException e) {
            log.error("参数转换异常");
            throw new RuntimeException("参数转换异常");
        }

        return params;
    }

    /**
     * 获取帖子数据
     *
     * @param url     "https://www.code-nav.cn/api/post/list/page/vo"
     * @param cookie
     * @param referer 防盗链 "https://www.code-nav.cn/user/1626574509983178753/post"
     * @param origin  "https://www.code-nav.cn"
     * @param params  参数
     * @return
     */
    public ResponseBody getPost(String url, String cookie, String referer, String origin, JSONObject params) {
        HttpResponse data = HttpRequest.post(url)
                .body(params.toString())
                .cookie(cookie)
                .header("referer", referer)
                .header("origin", origin)
                .contentType("application/json")
                .execute();
        ResponseBody responseBody = JSONUtil.toBean(data.body(), ResponseBody.class);
        return responseBody;
    }

    /**
     * 为所有文章提取对应的标签，BaiDuUtil.getLabel的 QPS 上线为2（免费试用的限制）
     * @param postList
     * @return
     */
    public HashMap<String, List<PostWithLabel>> makeLabel(List<ResponseBody.Post> postList){
        HashMap<String, List<PostWithLabel>> labelToPost = new HashMap<>();
        // 遍历所有文章，为其打上标签
        postList.forEach(post -> {
            // 存储文章真实标签，便于后期文章标签展示
            ArrayList<String> realLabel = new ArrayList<>();
            // API 返回的标签
            ArrayList<String> labels = BaiDuUtil.getLabel(post);

            log.info("labels:{}", labels);
            String path = "presetLabel.json";
            FileReader fileReader = new FileReader(path);
            String s = fileReader.readString();
            JSONObject presetLabel = new JSONObject(s);

            // 遍历 labels 匹配 presetLabel
            for (String label : labels) {
                if (presetLabel.containsKey(label) || presetLabel.containsValue(label)) {
                    realLabel.add(label);
                    List<PostWithLabel> postByLabel = labelToPost.getOrDefault(label, new ArrayList<>());
                    postByLabel.add(new PostWithLabel(post, realLabel));
                    labelToPost.put(label, postByLabel);
                }
            }

            // 遍历 presetLabel 匹配 labels
            for (String keyLabel : presetLabel.keySet()) {
                JsonUtil.compareAndFill(labelToPost, realLabel, labels, keyLabel, post);
            }
            JSONArray values = JsonUtil.getValuesByRec(presetLabel);
            for (Object valueObj : values) {
                String value = (String) valueObj;
                if (StrUtil.isNotBlank(value)) {
                    JsonUtil.compareAndFill(labelToPost, realLabel, labels, value, post);
                }
            }

        });

        return labelToPost;
    }


}
