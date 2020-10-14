package com.rzotgorz.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzotgorz.configuration.LuceneConfig;
import com.rzotgorz.model.NewsModel;
import com.rzotgorz.service.NewsParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
@EnableAutoConfiguration
public class AddNewsController {
    @RequestMapping(value = "/index/add", method = RequestMethod.POST)
    public void AddNews(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        PrintWriter printWriter = response.getWriter();
        NewsModel newsModel = null;
        try {
            newsModel = NewsParser.parse(jsonParam);
        } catch (Exception e) {
            printWriter.println("{code:401,data:\"Invalid News\"}");
            return;
        }
        Directory dir = null;
        boolean flag = false;
        if(LuceneConfig.directoryExist()) {
            try {
                dir = LuceneConfig.directory();
                IndexReader reader = DirectoryReader.open(dir);
                IndexSearcher searcher = new IndexSearcher(reader);
                Query query = new TermQuery(new Term("id",jsonParam.getString("news_id")));
                TopDocs topDocs = searcher.search(query, 10);
                dir.close();
                if(topDocs.scoreDocs.length != 0)
                    throw new Exception("News Already Exists " + jsonParam.getString("news_id"));
            } catch (IndexNotFoundException e) {
                flag = true;
            } catch (Exception e) {
                printWriter.println("{code:401,data:\"" + e.getMessage() + "\"}");
                return;
            }
        }
        else
            flag = true;
        try {
            dir = LuceneConfig.directory();
            IndexWriterConfig config = new IndexWriterConfig(LuceneConfig.analyzer());
            if(flag)
                config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(dir, config);
            Document document = new Document();
            FieldType fieldType = LuceneConfig.fieldType();
            document.add(new Field("title", newsModel.getTitle(), fieldType));
            document.add(new Field("content", newsModel.getTextContents(), fieldType));
            document.add(new StringField("id",newsModel.getId(), Field.Store.YES));
            document.add(new Field("origin_json", newsModel.getOriginJson(), fieldType));
            document.add(new Field("tags", newsModel.getTags(), fieldType));
            writer.addDocument(document);
            writer.close();
            dir.close();
        } catch (Exception e) {
            printWriter.println("{code:500,data:\"Unknown error occurred\"}");
            return;
        }
        printWriter.println("{code:200,data:\"News added successfully\"}");
    }
}
