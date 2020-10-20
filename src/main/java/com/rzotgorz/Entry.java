package com.rzotgorz;

import com.rzotgorz.configuration.LuceneConfig;
import com.rzotgorz.service.DatabaseConnector;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
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
        ResultSet resultSet = connector.query("SELECT * FROM backend_news;");
        int cnt = 0;
        while(resultSet.next()) {
            Directory dir = null;
            boolean flag = false;
            if(LuceneConfig.directoryExist()) {
                try {
                    dir = LuceneConfig.directory();
                    IndexReader reader = DirectoryReader.open(dir);
                    IndexSearcher searcher = new IndexSearcher(reader);
                    //Use term because the id didn't go through analyzer when added.
                    Query query = new TermQuery(new Term("id", resultSet.getString("news_id")));
                    TopDocs topDocs = searcher.search(query, 10);
                    reader.close();
                    dir.close();
                    if (topDocs.scoreDocs.length != 0)
                        continue;
                } catch (IndexNotFoundException e) {
                    flag = true;
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
                return;
            }
            cnt += 1;
            if(cnt >= 100)
                break;
        }
    }

    public static void main(String[] args) {
        try {
            initializeIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SpringApplication.run(Entry.class, args);
    }

}
