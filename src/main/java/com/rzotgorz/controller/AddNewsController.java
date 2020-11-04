package com.rzotgorz.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rzotgorz.configuration.LuceneConfig;
import com.rzotgorz.model.NewsModel;
import com.rzotgorz.service.DatabaseConnector;
import com.rzotgorz.service.NewsParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Controller
@EnableAutoConfiguration
public class AddNewsController {
    /*
     * The controller for add news.
     */

    @Autowired
    private DatabaseConnector connector;

    private Directory dir = null;
    private IndexWriter indexWriter = null;

    private void getWriter() throws IOException {
        if(dir == null)
            dir = LuceneConfig.directory();
        if(indexWriter == null)
            indexWriter = new IndexWriter(dir, LuceneConfig.getWriterConfig());
    }

    private void closeWriter() throws IOException {
        if(indexWriter != null) {
            indexWriter.commit();
            indexWriter.close();
            indexWriter = null;
        }
        if(dir != null) {
            dir.close();
            dir = null;
        }

    }

    /*
     * DEPRECATED
     * Url: /index/add_from_json
     * Method: POST
     * Usage: Receive the json of the news to be added and add it to the index (if it doesn't exist).
     */
    @Deprecated
    @RequestMapping(value = "/index/add_from_json", method = RequestMethod.POST)
    public void addNewsFromJson(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        PrintWriter printWriter = response.getWriter();
        NewsModel newsModel = null;
        try {
            newsModel = NewsParser.parse(jsonParam);
        } catch (Exception e) { //If some of the parameters doesn't exist, the parser will throw a exception of cannot read parameters.
            printWriter.println("{\"code\":401,\"data\":\"Invalid News\"}");
            return;
        }
        IndexSearcher searcher = null;
        if(LuceneConfig.directoryExist()) {
            try {
                searcher = LuceneConfig.getIndexSearcher();
                if(searcher != null) {
                    //Use term because the id didn't go through analyzer when added.
                    Query query = new TermQuery(new Term("id", jsonParam.getString("news_id")));
                    TopDocs topDocs = searcher.search(query, 10);
                    if (topDocs.scoreDocs.length != 0)
                        throw new Exception("News Already Exists " + jsonParam.getString("news_id"));
                    LuceneConfig.closeIndexSearch(searcher);
                }
            } catch (IndexNotFoundException e) { //Although the directory exists, it doesn't have the index. It's empty.
                System.err.println(e.getMessage());
            } catch (Exception e) {  //Catch the exception of duplicate news.
                printWriter.println("{\"code\":401,\"data\":\"" + e.getMessage() + "\"}");
                LuceneConfig.closeIndexSearch(searcher);
                return;
            }
        }
        try {
            addNewsToIndex(newsModel);
        } catch (Exception e) {
            closeWriter();
            printWriter.println("{\"code\":500,\"data\":\"Unknown error occurred\"}");
            return;
        }
        closeWriter();
        printWriter.println("{\"code\":200,\"data\":\"News added successfully\"}");
    }

    @RequestMapping(value = "/index/add_from_id", method = RequestMethod.POST)
    public void addNews(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        PrintWriter printWriter = response.getWriter();
        String newsId = jsonParam.getString("news_id");
        if(newsId == null || newsId.equals(""))
        {
            printWriter.println("{\"code\":401,\"data\":\"Invalid news id\"}");
            return;
        }
        IndexSearcher searcher = null;
        if(LuceneConfig.directoryExist()) {
            try {
                searcher = LuceneConfig.getIndexSearcher();
                if(searcher != null) {
                    //Use term because the id didn't go through analyzer when added.
                    Query query = new TermQuery(new Term("id", newsId));
                    TopDocs topDocs = searcher.search(query, 10);
                    if (topDocs.scoreDocs.length != 0)
                        throw new Exception("News Already Exists " + newsId);
                    LuceneConfig.closeIndexSearch(searcher);
                }
            } catch (IndexNotFoundException e) { //Although the directory exists, it doesn't have the index. It's empty.
                System.err.println(e.getMessage());
            } catch (Exception e) {  //Catch the exception of duplicate news.
                LuceneConfig.closeIndexSearch(searcher);
                printWriter.println("{\"code\":401,\"data\":\"" + e.getMessage() + "\"}");
                return;
            }
        }
        try {
            ResultSet resultSet = connector.query("SELECT * FROM backend_news WHERE news_id = '"+newsId+"'");
            if (!resultSet.next())
            {
                printWriter.println("{\"code\":401,\"data\":\"No news found with given id\"}");
                return;
            }
            addNewsFromDatabase(resultSet);
        } catch (Exception e) {
            closeWriter();
            printWriter.println("{\"code\":500,\"data\":\"Unknown error occurred"+e.getMessage()+"\"}");
            return;
        }
        closeWriter();
        printWriter.println("{\"code\":200,\"data\":\"News added successfully\"}");
    }

    @RequestMapping(value = "/index/add", method = RequestMethod.POST)
    public void addIdList(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        PrintWriter printWriter = response.getWriter();
        List<String> idList = null;
        try {
            idList = JSON.parseArray(jsonParam.getJSONArray("news_id").toJSONString(),String.class);
        } catch (Exception e) {
            printWriter.println("{\"code\":401,\"data\":\"Invalid id list\"}");
            return;
        }
        List<String> addList = new LinkedList<>();
        if(LuceneConfig.directoryExist())
        {
            IndexSearcher searcher = LuceneConfig.getIndexSearcher();
            if(searcher == null)
                addList = idList;
            else {
                for (String newsId : idList) {
                    Query query = new TermQuery(new Term("id", newsId));
                    TopDocs topDocs = searcher.search(query, 10);
                    if (topDocs.scoreDocs.length != 0)
                        continue;
                    addList.add(newsId);
                }
            }
        } else {
            addList = idList;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        boolean flag = false;
        for(String newsId: addList) {
            if(!flag)
                flag=true;
            else
                builder.append(',');
            builder.append('\'').append(newsId).append('\'');
        }
        builder.append(')');
        try {
            ResultSet resultSet = connector.query("SELECT * FROM backend_news WHERE news_id in "+builder.toString()+";");
            while(resultSet.next()) {
                addNewsFromDatabase(resultSet);
            }
        } catch (Exception e) {
            closeWriter();
            printWriter.println("{\"code\":500,\"data\":\"Unknown error occurred"+e.getMessage()+"\"}");
            return;
        }
        closeWriter();
        printWriter.println("{\"code\":200,\"data\":\"Process finished.\"}");
        System.err.println("Successfully add "+String.valueOf(addList.size())+" of "+String.valueOf(idList.size())+" news.");
    }

    private void addNewsToIndex(NewsModel newsModel) throws Exception{
        getWriter();
        Document document = new Document();
        FieldType fieldType = LuceneConfig.fieldType();
        document.add(new Field("title", newsModel.getTitle(), fieldType));
        document.add(new Field("content", newsModel.getContents(), fieldType));
        document.add(new Field("category", newsModel.getCategory(), fieldType));
        document.add(new Field("summary", newsModel.getSummary(), fieldType));
        document.add(new Field("tags", newsModel.getTags(), fieldType));
        document.add(new StringField("id", newsModel.getId(), Field.Store.YES));
        document.add(new StringField("source", newsModel.getSource(), Field.Store.YES));
        document.add(new StringField("news_url", newsModel.getNews_url(), Field.Store.YES));
        document.add(new StringField("media", newsModel.getMedia(), Field.Store.YES));
        document.add(new StringField("pub_date", newsModel.getPub_date(), Field.Store.YES));
        document.add(new StringField("img", newsModel.getImg(), Field.Store.YES));
        indexWriter.addDocument(document);
    }

    private void addNewsFromDatabase(ResultSet resultSet) throws Exception{
        NewsModel newsModel = new NewsModel();
        newsModel.setTitle(resultSet.getString("title"));
        newsModel.setContents(NewsParser.parseContent(resultSet.getString("content")));
        newsModel.setCategory(resultSet.getString("category"));
        newsModel.setSummary(resultSet.getString("summary"));
        newsModel.setTags(resultSet.getString("tags"));
        newsModel.setId(resultSet.getString("news_id"));
        newsModel.setSource(resultSet.getString("source"));
        newsModel.setNews_url(resultSet.getString("news_url"));
        newsModel.setMedia(resultSet.getString("media"));
        newsModel.setPub_date(resultSet.getString("pub_date"));
        newsModel.setImg(resultSet.getString("img"));
        addNewsToIndex(newsModel);
    }
}
