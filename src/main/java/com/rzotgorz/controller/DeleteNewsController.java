package com.rzotgorz.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzotgorz.configuration.LuceneConfig;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
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
public class DeleteNewsController {
    @RequestMapping(value="index/delete", method = RequestMethod.POST)
    public void deleteNews(@RequestBody JSONObject jsonParam, HttpServletResponse response) throws IOException {
        PrintWriter printWriter = response.getWriter();
        String id = jsonParam.getString("id");
        if(id == null || id.equals("")) {
            printWriter.println("{code:401,data:\"Invalid news id\"}");
            return;
        }
        Directory dir = null;
        boolean flag = true;
        if(LuceneConfig.directoryExist()) {
            try {
                dir = LuceneConfig.directory();
                IndexReader reader = DirectoryReader.open(dir);
                IndexSearcher searcher = new IndexSearcher(reader);
                Query query = new TermQuery(new Term("id",id));
                TopDocs topDocs = searcher.search(query,10);
                dir.close();
                if(topDocs.scoreDocs.length == 0)
                    flag=false;
            } catch (IndexNotFoundException e) {
                flag = false;
            }
        }
        else
            flag = false;
        if (!flag) {
            printWriter.println("{code:401,data:\"News doesn't exist\"}");
            return;
        }
        IndexWriterConfig config = new IndexWriterConfig(LuceneConfig.analyzer());
        IndexWriter writer = new IndexWriter(LuceneConfig.directory(),config);
        writer.deleteDocuments(new Term("id",id));
        writer.close();
        printWriter.println("{code:200,data:\"News deleted successfully\"}");
    }
}
