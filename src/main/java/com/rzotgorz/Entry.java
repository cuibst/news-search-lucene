package com.rzotgorz;

import com.rzotgorz.configuration.LuceneConfig;
import com.rzotgorz.service.DatabaseConnector;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public static void main(String[] args){
        SpringApplication.run(Entry.class, args);
    }

}
