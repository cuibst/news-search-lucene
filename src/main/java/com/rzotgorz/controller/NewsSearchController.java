package com.rzotgorz.controller;

import com.rzotgorz.configuration.LuceneConfig;
import com.rzotgorz.model.NewsModel;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@Controller
@EnableAutoConfiguration
public class NewsSearchController {
    @RequestMapping(value = "/index/search", method = RequestMethod.GET)
    public void SearchNews(@RequestParam String query, HttpServletResponse response) throws Exception
    {
        System.out.println(query);
        response.setCharacterEncoding("gbk");
        PrintWriter writer = response.getWriter();
        if(query == null || query.equals("")) {
            writer.println("{code:401,data:\"invalid query\"}");
            return;
        }
        ArrayList<NewsModel> hitsList = getTopDoc(query,10);
        writer.println("{code:200,\ndata:[");
        for(int i=0;i<hitsList.size();i++)
        {
            //System.out.println(new String(hitsList.get(i).getOriginJson().getBytes("utf-8"),"gbk"));
            writer.print(hitsList.get(i).getOriginJson());
            if(i!=hitsList.size()-1)
                writer.print(',');
            writer.println();
        }
        writer.println("]}");
    }
    public static ArrayList<NewsModel> getTopDoc(String key,int N) throws Exception{
        ArrayList<NewsModel> hitsList = new ArrayList<>();
        String[] fields = {"title","tags","content"};
        Directory dir;
        dir = LuceneConfig.directory();
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = LuceneConfig.analyzer();
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields,analyzer);

        Query query = parser.parse(key);
        TopDocs topDocs = searcher.search(query, N);

        for(ScoreDoc scoreDoc: topDocs.scoreDocs) {
            NewsModel cur = new NewsModel();
            Document doc = searcher.doc(scoreDoc.doc);
            cur.setId(doc.get("id"));
            cur.setTextContents(doc.get("content"));
            cur.setTags(doc.get("tags"));
            cur.setTitle(doc.get("title"));
            cur.setOriginJson(doc.get("origin_json"));
            hitsList.add(cur);
        }
        dir.close();
        reader.close();
        return hitsList;
    }
}
