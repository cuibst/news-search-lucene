package com.rzotgorz.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzotgorz.configuration.LuceneConfig;
import com.rzotgorz.model.NewsModel;
import com.rzotgorz.service.NewsParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
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
            e.printStackTrace();
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
                Analyzer analyzer = LuceneConfig.analyzer();
                QueryParser parser = new QueryParser("id", analyzer);
                Query query = parser.parse(newsModel.getId());
                TopDocs topDocs = searcher.search(query, 1);
                dir.close();
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document doc = searcher.doc(scoreDoc.doc);
                    if (doc.get("id").equals(newsModel.getId()))
                        throw new Exception("News Already Exists " + doc.get("id"));
                }
            } catch (IndexNotFoundException e) {
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
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
            document.add(new Field("id", newsModel.getId(), fieldType));
            document.add(new Field("origin_json", newsModel.getOriginJson(), fieldType));
            document.add(new Field("tags", newsModel.getTags(), fieldType));
            writer.addDocument(document);
            writer.close();
            dir.close();
        } catch (Exception e) {
            e.printStackTrace();
            printWriter.println("{code:500,data:\"Unknown error occurred\"}");
            return;
        }
        printWriter.println("{code:200,data:\"News added successfully\"}");
    }
}
