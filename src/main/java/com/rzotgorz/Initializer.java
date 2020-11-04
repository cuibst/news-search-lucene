package com.rzotgorz;

import com.rzotgorz.configuration.DatabaseConfig;
import com.rzotgorz.configuration.LuceneConfig;
import com.rzotgorz.service.DatabaseConnector;
import com.rzotgorz.service.NewsParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class Initializer implements ApplicationRunner {

    @Autowired
    private DatabaseConnector connector;

    @Autowired
    private DatabaseConfig config;

    public void initializeIndex() throws IOException, SQLException {
        System.err.println("Index Initializing...");
        System.err.println("Database Url:"+config.url);
        try {
            ResultSet rs = connector.query("SELECT count(1) FROM backend_news;");
            rs.next();
            System.err.println(rs.getInt(1));
            rs.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        int cnt = 1000;
        Directory dir = LuceneConfig.directory();
        IndexWriterConfig config = new IndexWriterConfig(LuceneConfig.analyzer());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        config.setMaxBufferedDocs(1000);
        config.setRAMBufferSizeMB(256);
        IndexWriter writer = new IndexWriter(dir, config);
        while(true)
        {
            String q = "SELECT * FROM backend_news WHERE id BETWEEN "+(cnt-1000)+" AND "+(cnt-1)+';';
            ResultSet resultSet = connector.query(q);
            if(resultSet == null || !resultSet.next())
                break;
            do {
                try {
                    Document document = new Document();
                    FieldType fieldType = LuceneConfig.fieldType();
                    document.add(new Field("title", resultSet.getString("title"), fieldType));
                    document.add(new Field("content", NewsParser.parseContent(resultSet.getString("content")), fieldType));
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
                } catch (Exception e) {
                    writer.close();
                    dir.close();
                    return;
                }
            } while(resultSet.next());
            if(cnt % 50000 == 0) {
                System.err.println(cnt);
            }
            cnt += 1000;
        }
        writer.close();
        dir.close();
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            initializeIndex();
        } catch (IOException | SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
