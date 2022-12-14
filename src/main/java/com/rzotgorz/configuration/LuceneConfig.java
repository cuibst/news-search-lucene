package com.rzotgorz.configuration;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LuceneConfig {
    /*
     * Some basic configurations for the whole lucene index server.
     */

    //The directory of the index
    private static final String LUCENE_INDEX_PATH = "/index";

    //The unified analyzer to split words.
    public static Analyzer analyzer() {
        return new IKAnalyzer();
    }

    //Return the index directory and create directory if needed.
    public static Directory directory() throws IOException {
        Path path = Paths.get(LUCENE_INDEX_PATH);
        File file = path.toFile();
        if(!file.exists())
            file.mkdirs();
        return FSDirectory.open(path);
    }

    //Return whether the index directory exists.
    public static boolean directoryExist() throws IOException {
        Path path = Paths.get(LUCENE_INDEX_PATH);
        File file = path.toFile();
        if(!file.exists())
            return false;
        return true;
    }

    //Return the common field type for the lucene index fields.
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

    private static SearcherManager manager = null;
    private static final byte[] synchronized_r = new byte[0];

    public static IndexSearcher getIndexSearcher() throws IOException {
        IndexSearcher indexSearcher = null;
        synchronized (synchronized_r) {
            if(manager == null) {
                try {
                    manager = new SearcherManager(directory(), new SearcherFactory());
                } catch (IndexNotFoundException e) {
                    System.err.println(e.getMessage());
                    manager = null;
                    return null;
                }
            }
            manager.maybeRefresh();
            indexSearcher = manager.acquire();
        }
        return indexSearcher;
    }

    public static void closeIndexSearch(IndexSearcher indexSearcher) throws IOException {
        if(indexSearcher!=null)
            manager.release(indexSearcher);
        indexSearcher = null;
    }

    public static IndexWriterConfig getWriterConfig() {
        IndexWriterConfig config = new IndexWriterConfig(LuceneConfig.analyzer());
        config.setMaxBufferedDocs(1000);
        config.setRAMBufferSizeMB(256);
        return config;
    }
}
