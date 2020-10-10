package com.rzotgorz.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rzotgorz.model.NewsModel;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CreateIndex {
    public static List<NewsModel> extractFiles() throws IOException {
        ArrayList<NewsModel> list = new ArrayList<NewsModel>();
        File fileDir = new File("news_info");
        File[] allFiles = fileDir.listFiles();
        for(File f : allFiles) {
            Long fileLength = f.length();
            byte[] fileContent = new byte[fileLength.intValue()];
            try {
                FileInputStream in = new FileInputStream(f);
                in.read(fileContent);
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String content = new String(fileContent,"UTF-8");
            //System.out.println(content);
            //break;
            JSONObject jsonObject = JSONObject.parseObject(content);
            NewsModel cur = new NewsModel();
            cur.setOriginJson(content);
            cur.setId(jsonObject.getString("news_id"));
            cur.setTitle(jsonObject.getString("title"));
            cur.setTags(jsonObject.getString("tags"));
            List<String> contents = JSON.parseArray(jsonObject.getJSONArray("content").toJSONString(),String.class);
            String textContent = "";
            for(String line : contents) {
                String tmp = line.substring(0,3);
                if(tmp.equals("img_"))
                    continue;
                textContent = textContent + line.substring(5);
            }
            //System.out.println(textContent);
            cur.setTextContents(textContent);
            list.add(cur);
        }
        return list;
    }

    public static void main(String[] args) throws IOException {
        List<NewsModel> newsList = extractFiles();
        Analyzer analyzer = new IKAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        Directory dir = null;
        IndexWriter indexWriter = null;
        Path indexPath = Paths.get("index");
        FieldType fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        fieldType.setStored(true);
        fieldType.setTokenized(true);
        fieldType.setStoreTermVectors(true);
        fieldType.setStoreTermVectorPositions(true);
        fieldType.setStoreTermVectorOffsets(true);
        if(!Files.isReadable(indexPath))
        {
            System.out.println("Can't open index directory");
            System.exit(1);
        }
        dir = FSDirectory.open(indexPath);
        indexWriter = new IndexWriter(dir,config);
        for(NewsModel model : newsList) {
            Document document = new Document();
            document.add(new Field("title",model.getTitle(),fieldType));
            document.add(new Field("content",model.getTextContents(),fieldType));
            document.add(new Field("id",model.getId(),fieldType));
            document.add(new Field("origin_json",model.getOriginJson(),fieldType));
            document.add(new Field("tags",model.getTags(),fieldType));
            indexWriter.addDocument(document);
        }
        indexWriter.commit();
        indexWriter.close();
        dir.close();
    }
}
