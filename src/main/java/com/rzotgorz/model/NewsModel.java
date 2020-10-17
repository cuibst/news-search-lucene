package com.rzotgorz.model;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

public class NewsModel {
    /*
     * Model for the news.
     */
    private String title;
    private String contents;
    private String category;
    private String summary;
    private String tags;
    private String id;
    private String source;
    private String news_url;
    private String media;
    private String pub_date;
    private String img;

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getImg() {
        return img;
    }

    public String getMedia() {
        return media;
    }

    public String getNews_url() {
        return news_url;
    }

    public String getPub_date() {
        return pub_date;
    }

    public String getSource() {
        return source;
    }

    public String getSummary() {
        return summary;
    }

    public String getTags() {
        return tags;
    }

    public String getContents() {
        return contents;
    }

    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public void setNews_url(String news_url) {
        this.news_url = news_url;
    }

    public void setPub_date(String pub_date) {
        this.pub_date = pub_date;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String toJSONString() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", getTitle());
        map.put("content", getContents());
        map.put("category", getCategory());
        map.put("summary", getSummary());
        map.put("tags", getTags());
        map.put("news_id", getId());
        map.put("source", getSource());
        map.put("news_url", getNews_url());
        map.put("media", getMedia());
        map.put("pub_date", getPub_date());
        map.put("img", getImg());
        return JSON.toJSONString(map);
    }
}
