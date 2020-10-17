package com.rzotgorz;

import com.rzotgorz.service.DatabaseConnector;
import org.junit.*;
import org.junit.runners.MethodSorters;

import javax.sound.midi.SysexMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

@FixMethodOrder(MethodSorters.DEFAULT)
public class DatabaseTest {
    private DatabaseConnector connector = null;

    @Before
    public void initTest() {
        connector = new DatabaseConnector();
    }

    @Test
    public void testQuery() throws Exception{
        boolean result = connector.modify("INSERT INTO backend_news (news_id) VALUES ('test1');");
        Assert.assertFalse("That should be invalid", result);
        result = connector.modify("INSERT INTO backend_news " +
                "(source, news_url, category, media, tags, title, news_id, pub_date, content, summary, img) " +
                "VALUES ('source','url','category','media','tag1,tag2','title','test1','2020-10-17','content','summary','image');");
        Assert.assertTrue("That should be valid", result);
        ResultSet resultSet = connector.query("SELECT news_id FROM backend_news WHERE news_id = 'test1';");
        boolean flag = false;
        try {
            if(!resultSet.next())
            {
                Assert.assertTrue("No result found", flag);
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
