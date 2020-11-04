package com.rzotgorz;

import com.rzotgorz.service.DatabaseConnector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Entry.class)
@FixMethodOrder(MethodSorters.DEFAULT)
public class EntryTest {

    @Autowired
    private DatabaseConnector connector;

    @Autowired
    private Initializer initializer;

    @Before
    public void setUp() {
        connector.modify("DROP TABLE IF EXISTS backend_news;");
        connector.modify("CREATE TABLE backend_news" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, source STRING NOT NULL, news_url STRING NOT NULL, category STRING NOT NULL, " +
                "media STRING NOT NULL, tags STRING NOT NULL, title STRING NOT NULL, news_id STRING NOT NULL UNIQUE, " +
                "pub_date STRING NOT NULL, content STRING NOT NULL, summary STRING NOT NULL, img STRING NOT NULL)");
    }

    @Test
    public void testEmptyEntry() {
        try {
            initializer.initializeIndex();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Entry is not working properly when empty");
        }
    }

    @Test
    public void testNotEmptyEntry() {
        connector.modify("INSERT INTO backend_news " +
                "(source, news_url, category, media, tags, title, news_id, pub_date, content, summary, img) " +
                "VALUES ('source','url','category','media','tag1,tag2','title','test1','2020-10-17','content','summary','image');");
        try {
            initializer.initializeIndex();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Entry is not working properly when not empty");
        }
    }

    @Test
    public void testMoreEntry() {
        connector.modify("INSERT INTO backend_news " +
                "(source, news_url, category, media, tags, title, news_id, pub_date, content, summary, img) " +
                "VALUES ('source','url','category','media','tag1,tag2','title','test2','2020-10-17','content','summary','image');");
        try {
            initializer.initializeIndex();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Entry is not working properly when not empty");
        }
    }
}
