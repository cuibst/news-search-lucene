package com.rzotgorz;

import com.rzotgorz.configuration.LuceneConfig;
import com.rzotgorz.service.DatabaseConnector;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The Entry class for the whole website.
 */
@SpringBootApplication
public class Entry
{
    public static void initializeIndex() throws SQLException, IOException {
        System.out.println("Index Initializing...");
        DatabaseConnector connector = new DatabaseConnector();
        int cnt = 1000;
        Directory dir = LuceneConfig.directory();
        IndexWriterConfig config = new IndexWriterConfig(LuceneConfig.analyzer());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, config);
        while(true)
        {
            String q = "SELECT * FROM backend_news WHERE id BETWEEN "+(cnt-1000)+" AND "+(cnt-1)+';';
            ResultSet resultSet = connector.query(q);
            if(!resultSet.next())
                break;
            do {
                try {
                    Document document = new Document();
                    FieldType fieldType = LuceneConfig.fieldType();
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
                } catch (Exception e) {
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

    public static void main(String[] args) {
        try {
            initializeIndex();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        SpringApplication.run(Entry.class, args);
    }

}
