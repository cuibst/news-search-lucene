package com.rzotgorz.controller;

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

@Controller
@EnableAutoConfiguration
public class AddNewsController {
    /*
     * The controller for add news.
     */

    @Autowired
    private DatabaseConnector connector;

    /*
     * DEPRECATED
     * Url: /index/add_from_json
     * Method: POST
     * Usage: Receive the json of the news to be added and add it to the index (if it doesn't exist).
     */
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
        Directory dir = null;
        boolean flag = false;
        if(LuceneConfig.directoryExist()) {
            try {
                dir = LuceneConfig.directory();
                IndexReader reader = DirectoryReader.open(dir);
                IndexSearcher searcher = new IndexSearcher(reader);
                //Use term because the id didn't go through analyzer when added.
                Query query = new TermQuery(new Term("id",jsonParam.getString("news_id")));
                TopDocs topDocs = searcher.search(query, 10);
                reader.close();
                dir.close();
                if(topDocs.scoreDocs.length != 0)
                    throw new Exception("News Already Exists " + jsonParam.getString("news_id"));
            } catch (IndexNotFoundException e) { //Although the directory exists, it doesn't have the index. It's empty.
                flag = true;
            } catch (Exception e) {  //Catch the exception of duplicate news.
                printWriter.println("{\"code\":401,\"data\":\"" + e.getMessage() + "\"}");
                return;
            }
        }
        else
            flag = true;

        try {
            dir = LuceneConfig.directory();
            IndexWriterConfig config = new IndexWriterConfig(LuceneConfig.analyzer());
            if(flag) //When the index doesn't exist, create it instead of add into it.
                config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(dir, config);
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
            writer.addDocument(document);
            writer.close();
            dir.close();
        } catch (Exception e) {
            printWriter.println("{\"code\":500,\"data\":\"Unknown error occurred\"}");
            return;
        }
        printWriter.println("{\"code\":200,\"data\":\"News added successfully\"}");
    }

    @RequestMapping(value = "/index/add", method = RequestMethod.POST)
    public void addNews(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        PrintWriter printWriter = response.getWriter();
        String newsId = jsonParam.getString("news_id");
        if(newsId == null || newsId.equals(""))
        {
            printWriter.println("{\"code\":401,\"data\":\"Invalid news id\"}");
            return;
        }
        Directory dir = null;
        boolean flag = false;
        if(LuceneConfig.directoryExist()) {
            try {
                dir = LuceneConfig.directory();
                IndexReader reader = DirectoryReader.open(dir);
                IndexSearcher searcher = new IndexSearcher(reader);
                //Use term because the id didn't go through analyzer when added.
                Query query = new TermQuery(new Term("id", newsId));
                TopDocs topDocs = searcher.search(query, 10);
                reader.close();
                dir.close();
                if(topDocs.scoreDocs.length != 0)
                    throw new Exception("News Already Exists " + newsId);
            } catch (IndexNotFoundException e) { //Although the directory exists, it doesn't have the index. It's empty.
                flag = true;
            } catch (Exception e) {  //Catch the exception of duplicate news.
                printWriter.println("{\"code\":401,\"data\":\"" + e.getMessage() + "\"}");
                return;
            }
        }
        else
            flag = true;
        try {
            dir = LuceneConfig.directory();
            IndexWriterConfig config = new IndexWriterConfig(LuceneConfig.analyzer());
            if(flag) //When the index doesn't exist, create it instead of add into it.
                config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(dir, config);
            Document document = new Document();
            FieldType fieldType = LuceneConfig.fieldType();
            ResultSet resultSet = connector.query("SELECT * FROM backend_news WHERE news_id = '"+newsId+"'");
            if (!resultSet.next())
            {
                writer.close();
                dir.close();
                printWriter.println("{\"code\":401,\"data\":\"No news found with given id\"}");
                return;
            }
            document.add(new Field("title", resultSet.getString("title"), fieldType));
            document.add(new Field("content", resultSet.getString("content"), fieldType));
            document.add(new Field("category", resultSet.getString("category"), fieldType));
            document.add(new Field("summary", resultSet.getString("summary"), fieldType));
            document.add(new Field("tags", resultSet.getString("tags"), fieldType));
            document.add(new StringField("id", resultSet.getString("news_id"), Field.Store.YES));
            document.add(new StringField("source", resultSet.getString("source"), Field.Store.YES));
            document.add(new StringField("news_url", resultSet.getString("news_url"), Field.Store.YES));
            document.add(new StringField("media", resultSet.getString("media"), Field.Store.YES));
            document.add(new StringField("pub_date", resultSet.getString("pub_date"), Field.Store.YES));
            document.add(new StringField("img", resultSet.getString("img"), Field.Store.YES));
            writer.addDocument(document);
            writer.close();
            dir.close();
        } catch (Exception e) {
            printWriter.println("{\"code\":500,\"data\":\"Unknown error occurred"+e.getMessage()+"\"}");
            return;
        }
        printWriter.println("{\"code\":200,\"data\":\"News added successfully\"}");
    }
}
