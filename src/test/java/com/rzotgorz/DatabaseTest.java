package com.rzotgorz;

import com.rzotgorz.service.DatabaseConnector;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sound.midi.SysexMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Entry.class)
@FixMethodOrder(MethodSorters.DEFAULT)
public class DatabaseTest {

    @Autowired
    private DatabaseConnector connector;

    @Before
    public void initTest() {
        connector.modify("DROP TABLE IF EXISTS backend_news;");
        connector.modify("CREATE TABLE backend_news" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, source STRING NOT NULL, news_url STRING NOT NULL, category STRING NOT NULL, " +
                "media STRING NOT NULL, tags STRING NOT NULL, title STRING NOT NULL, news_id STRING NOT NULL UNIQUE, " +
                "pub_date STRING NOT NULL, content STRING NOT NULL, summary STRING NOT NULL, img STRING NOT NULL)");
    }

    @Test
    public void testQuery() throws Exception{
        boolean result = connector.modify("INSERT INTO backend_news (news_id) VALUES ('test1');");
        Assert.assertFalse("That should be invalid", result);
        System.out.println("Second modification!======");
        result = connector.modify("INSERT INTO backend_news " +
                "(source, news_url, category, media, tags, title, news_id, pub_date, content, summary, img) " +
                "VALUES ('source', 'url', 'category', 'media', 'tag1,tag2', 'title', 'test1', '2020-10-17', 'content', 'summary', 'image');");
        Assert.assertTrue("That should be valid", result);
        ResultSet tmp = connector.query("SELECT count(1) FROM BACKEND_NEWS ;");
        System.out.println(tmp.getInt(1));
        ResultSet resultSet = connector.query("SELECT news_id FROM BACKEND_NEWS WHERE news_id = 'test1' ;");
        boolean flag = false;
        try {
            if(!resultSet.next())
            {
                Assert.fail("No result found empty set");
                return;
            }
            String id = resultSet.getString("news_id");
            System.out.println(id);
            if (id.equals("test1"))
                flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail("SQLException caught");
        }
        Assert.assertTrue("No result found", flag);
        connector.modify("DELETE FROM backend_news WHERE news_id = 'test1';");
    }
}
