package com.rzotgorz.model;

public class NewsModel {
    private String title;
    private String tags;
    private String textContents;
    private String originJson;
    private String id;

    public String getTitle() {
        return title;
    }

    public String getOriginJson() {
        return originJson;
    }

    public String getTags() {
        return tags;
    }

    public String getTextContents() {
        return textContents;
    }

    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOriginJson(String originJson) {
        this.originJson = originJson;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setTextContents(String textContents) {
        this.textContents = textContents;
    }

    public void setId(String id) {
        this.id = id;
    }
}
