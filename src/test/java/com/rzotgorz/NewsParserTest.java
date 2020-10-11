package com.rzotgorz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rzotgorz.model.NewsModel;
import com.rzotgorz.service.NewsParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Unit test for simple App.
 */
public class NewsParserTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public NewsParserTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( NewsParserTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        JSONObject jsonObject = JSON.parseObject("{" +
                "\"title\"  :\"title\"," +
                "\"tags\"   :\"tag1,tag2\"," +
                "\"news_id\":\"12345678\"," +
                "\"content\":[" +
                        "\"text_sample_text\"," +
                        "\"img_sample_image\"" +
                "]}");
        System.out.println(jsonObject.toString());
        NewsModel model = NewsParser.parse(jsonObject);
        Assert.assertEquals("Incorrect news id","12345678",model.getId());
        Assert.assertEquals("Incorrect title","title",model.getTitle());
        Assert.assertEquals("Incorrect text content","sample_text",model.getTextContents());
        Assert.assertEquals("Incorrect tags","tag1,tag2",model.getTags());
        Assert.assertEquals("Incorrect original json",jsonObject.toString(),model.getOriginJson());
    }
}
