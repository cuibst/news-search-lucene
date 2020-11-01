package com.rzotgorz.controller;

import com.rzotgorz.configuration.LuceneConfig;
import com.rzotgorz.model.NewsModel;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

@Controller
@EnableAutoConfiguration
@CrossOrigin(origins = "https://news-search-system-rzotgorz.app.secoder.net")
public class NewsSearchController {
    /*
     * The controller of searching news.
     * TODO: Support offset and start parameter.
     */

    /*
     * Url: /index/search
     * Method: GET
     * Usage: Return the result of the given query.
     */
    @RequestMapping(value = "/index/search", method = RequestMethod.GET)
    public void SearchNews(@RequestParam("query") String query, @RequestParam(value="start", required = false) String start, @RequestParam(value="count", required = false) String count, HttpServletResponse response) throws Exception
    {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        if(query == null || query.equals("")) {
            writer.println("{\"code\":401,\"infolist\":\"invalid query\"}");
            return;
        }
        int s;
        if(start == null || start.equals(""))
            s = 0;
        else
            s = Integer.parseInt(start);
        int c;
        if(count == null || count.equals(""))
            c = 20;
        else
            c = Integer.parseInt(count);
        ArrayList<NewsModel> hitsList = getTopDoc(query,max(s+c,1000));
        writer.println("{\"code\":200,\n" +
                "\"count\":" + hitsList.size() + ",\n" +
                "\"infolist\":[");
        for(int i=s;i<min(min(s+c,1000),hitsList.size());i++)
        {
            writer.print(hitsList.get(i).toJSONString());
            if(i!=hitsList.size()-1)
                writer.print(',');
            writer.println();
        }
        writer.println("]}");
    }

    /*
     * Usage: Get the top document lists of a given query.
     */
    public static ArrayList<NewsModel> getTopDoc(String key,int N) throws Exception{
        ArrayList<NewsModel> hitsList = new ArrayList<>();
        String[] fields = {"title","tags","content","category","summary"};
        Directory dir;
        dir = LuceneConfig.directory();
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = LuceneConfig.analyzer();
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields,analyzer);

        Query query = parser.parse(key);
        TopDocs topDocs = searcher.search(query, N);

        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span style=\"color:#F96600\">","</span>");
        QueryScorer[] scorers = new QueryScorer[5];
        Highlighter[] highlighters = new Highlighter[5];
        for(int i=0;i<5;i++)
        {
            scorers[i] = new QueryScorer(query,fields[i]);
            highlighters[i] = new Highlighter(formatter, scorers[i]);
        }

        for(ScoreDoc scoreDoc: topDocs.scoreDocs) {
            NewsModel cur = new NewsModel();
            Document doc = searcher.doc(scoreDoc.doc);
            String[] contents = new String[5];
            String[] hlContents = new String[5];
            for(int i=0;i<5;i++) {
                contents[i] = doc.get(fields[i]);
                Fragmenter fragmenter = new SimpleSpanFragmenter(scorers[i]);
                highlighters[i].setTextFragmenter(fragmenter);
                hlContents[i] = highlighters[i].getBestFragment(LuceneConfig.analyzer(), fields[i], contents[i]);
            }
            cur.setId(doc.get("id"));
            cur.setContents(contents[2]);
            cur.setTags(contents[1]);
            cur.setTitle(hlContents[0] != null ? hlContents[0] : contents[0]);
            cur.setCategory(contents[3]);
            cur.setSummary(hlContents[2] != null ? hlContents[2] + "..." : contents[4]);
            cur.setSource(doc.get("source"));
            cur.setNews_url(doc.get("news_url"));
            cur.setMedia(doc.get("media"));
            cur.setPub_date(doc.get("pub_date"));
            cur.setImg(doc.get("img"));
            hitsList.add(cur);
        }
        dir.close();
        reader.close();
        return hitsList;
    }
}
