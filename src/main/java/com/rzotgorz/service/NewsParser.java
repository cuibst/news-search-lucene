package com.rzotgorz.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rzotgorz.model.NewsModel;

import java.util.List;

public class NewsParser {
    /*
     * Parser for parsing json into news model.
     */
    public static NewsModel parse(JSONObject jsonObject) {
        NewsModel ret = new NewsModel();
        ret.setOriginJson(jsonObject.toJSONString());
        ret.setId(jsonObject.getString("news_id"));
        ret.setTitle(jsonObject.getString("title"));
        ret.setTags(jsonObject.getString("tags"));
        List<String> contents = JSON.parseArray(jsonObject.getJSONArray("content").toJSONString(),String.class);
        String textContent = "";
        for(String line : contents) { //The news content should exclude headers and image part.
            if(line.indexOf("img_")==0)
                continue;
            textContent = textContent + line.substring(5);
            //FIXME: Use a string builder instead of simple string concatenation.
        }
        ret.setTextContents(textContent);
        return ret;
    }
}
