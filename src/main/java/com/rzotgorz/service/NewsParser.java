package com.rzotgorz.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rzotgorz.model.NewsModel;

import java.io.IOException;
import java.util.List;

public class NewsParser {
    /*
     * Parser for parsing json into news model.
     */
    public static NewsModel parse(JSONObject jsonObject) throws Exception{
        NewsModel ret = new NewsModel();
        ret.setId(jsonObject.getString("news_id"));
        if(ret.getId() == null)
            throw new Exception("No news id!");
        ret.setTitle(jsonObject.getString("title"));
        ret.setTags(jsonObject.getString("tags"));
        ret.setImg(jsonObject.getString("img"));
        ret.setPub_date(jsonObject.getString("pub_date"));
        ret.setMedia(jsonObject.getString("media"));
        ret.setNews_url(jsonObject.getString("news_url"));
        ret.setSource(jsonObject.getString("source"));
        ret.setSummary(jsonObject.getString("summary"));
        ret.setCategory(jsonObject.getString("category"));
        List<String> stringList = JSONObject.parseArray(jsonObject.getString("content"), String.class);
        StringBuilder builder = new StringBuilder();
        for(String content: stringList) {
            builder.append(content);
        }
        ret.setContents(builder.toString());
        return ret;
    }

    public static String parseContent(String original) {
        StringBuilder builder = new StringBuilder();
        List<String> stringList = JSONObject.parseArray(original, String.class);
        for(String content: stringList) {
            builder.append(content);
        }
        return builder.toString();
    }
}
