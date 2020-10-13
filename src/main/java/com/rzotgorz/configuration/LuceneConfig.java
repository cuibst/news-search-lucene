package com.rzotgorz.configuration;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class LuceneConfig {
    private static final String LUCENE_INDEX_PATH = "/index";
    @Bean
    public static Analyzer analyzer() {
        return new IKAnalyzer();
    }

    @Bean
    public static Directory directory() throws IOException {
        Path path = Paths.get(LUCENE_INDEX_PATH);
        File file = path.toFile();
        if(!file.exists())
            file.mkdirs();
        return FSDirectory.open(path);
    }

    @Bean
    public static boolean directoryExist() throws IOException {
        Path path = Paths.get(LUCENE_INDEX_PATH);
        File file = path.toFile();
        if(!file.exists())
            return false;
        return true;
    }

    @Bean
    public static FieldType fieldType() throws Exception {
        FieldType fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        fieldType.setStored(true);
        fieldType.setTokenized(true);
        fieldType.setStoreTermVectors(true);
        fieldType.setStoreTermVectorPositions(true);
        fieldType.setStoreTermVectorOffsets(true);
        return fieldType;
    }
}
