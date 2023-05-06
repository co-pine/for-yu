package com.pine.foryu;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonUtil {

    // 递归获取所有的value
    public static JSONArray getValuesByRec(JSONObject jsonObject) {
        JSONArray jsonArray = new JSONArray();
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof JSONArray) {
                JSONArray subArray = (JSONArray) value;
                for (int i = 0; i < subArray.size(); i++) {
                    Object subValue = subArray.get(i);
                    // 如果元素是JSON对象，递归调用存储过程
                    if (subValue instanceof JSONObject) {
                        jsonArray.addAll(getValuesByRec((JSONObject) subValue));
                        // 将其他类型元素直接添加到最终结果数组中
                    } else {
                        jsonArray.put(subValue);
                    }
                }
            } else if (value instanceof JSONObject) {
                jsonArray.addAll(getValuesByRec((JSONObject) value));
            } else {
                jsonArray.put(value);
            }
        }
        return jsonArray;
    }

    public static Boolean containsIgnoreCase(String ori, String des) {
        String s = ori.replace(" ", "").toUpperCase();
        String s1 = des.replace(" ", "").toUpperCase();
        return s.contains(s1);
    }

    /**
     * 循环 labels 进行比较并将结果填充进 labelToPost
     *
     * @param labelToPost 存储最终结果
     * @param realLabel   每篇文章的最终标签
     * @param labels      API 返回的标签，用于匹配比较
     * @param content     当前遍历到的预置标签
     * @param post        当前文章
     */
    public static void compareAndFill(HashMap<String, List<PostWithLabel>> labelToPost, ArrayList<String> realLabel, ArrayList<String> labels, String content, ResponseBody.Post post) {
        for (String label : labels) {
            // log.info("{}:{}", label, content);
            if (JsonUtil.containsIgnoreCase(label, content)) {
                realLabel.add(content);
                List<PostWithLabel> postByLabel = labelToPost.getOrDefault(content, new ArrayList<>());
                postByLabel.add(new PostWithLabel(post, realLabel));
                labelToPost.put(content, postByLabel);
            }
        }
    }


    public static void main(String[] args) {
        String path = "presetLabel.json";
        FileReader fileReader = new FileReader(path);
        String s = fileReader.readString();
        String[] searchArr = {"Spring Boot", "Spring Security"};
        JSONObject json = JSONUtil.parseObj(s);
        JSONObject result = new JSONObject();
        for (String searchKey : searchArr) {
            searchAndPut(json, searchKey, result);
        }
        System.out.println(result);
    }

    private static boolean searchAndPut(JSONObject currentObj, String searchKey, JSONObject result) {
        for (String key : currentObj.keySet()) {
            Object value = currentObj.get(key);
            if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                for (Object element : jsonArray) {
                    if (element instanceof JSONObject) {
                        JSONObject subResult = new JSONObject();
                        if (searchAndPut((JSONObject) element, searchKey, subResult)) {
                            JSONArray resultArray;
                            if (result.containsKey(key)) {
                                resultArray = result.getJSONArray(key);
                            } else {
                                resultArray = new JSONArray();
                                result.put(key, resultArray);
                            }
                            resultArray.add(subResult);
                            return true;
                        }
                    } else if (element instanceof String) {
                        if (element.equals(searchKey)) {
                            JSONArray resultArray;
                            if (result.containsKey(key)) {
                                resultArray = result.getJSONArray(key);
                            } else {
                                resultArray = new JSONArray();
                                result.put(key, resultArray);
                            }
                            resultArray.add(element);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // public static void main(String[] args) {
    //     String path = "presetLabel.json";
    //     FileReader fileReader = new FileReader(path);
    //     String s = fileReader.readString();
    //     JSONObject presetLabel = new JSONObject(s);
    //     String searchKey = "Spring Boot";
    //     JSONObject json = JSONUtil.parseObj(s);
    //     JSONObject result = new JSONObject();
    //     searchAndPut(json, searchKey, result);
    //     System.out.println(result);
    // }


}
