package com.rzotgorz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rzotgorz.model.NewsModel;
import com.rzotgorz.service.NewsParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

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
    public void testNewsParser()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("title", "title");
        map.put("content", "['Sample content']");
        map.put("category", "category");
        map.put("summary", "summary");
        map.put("tags", "tag1,tag2");
        map.put("news_id", "test1");
        map.put("source", "Sample source");
        map.put("news_url", "Sample url");
        map.put("media", "Sample media");
        map.put("pub_date", "2020-10-17");
        map.put("img", "Sample Image");
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(map));
        System.out.println(jsonObject.toString());
        NewsModel model = null;
        try {
            model = NewsParser.parse(jsonObject);
        } catch (Exception e) {
            fail("Parsing failed");
        }
        Assert.assertEquals("Incorrect news id","test1",model.getId());
        Assert.assertEquals("Incorrect title","title",model.getTitle());
        Assert.assertEquals("Incorrect text content","Sample content",model.getContents());
        Assert.assertEquals("Incorrect tags","tag1,tag2",model.getTags());
        Assert.assertEquals("Incorrect category","category",model.getCategory());
        Assert.assertEquals("Incorrect summary","summary",model.getSummary());
        Assert.assertEquals("Incorrect source","Sample source",model.getSource());
        Assert.assertEquals("Incorrect url","Sample url",model.getNews_url());
        Assert.assertEquals("Incorrect media","Sample media",model.getMedia());
        Assert.assertEquals("Incorrect publish date","2020-10-17",model.getPub_date());
        Assert.assertEquals("Incorrect img","Sample Image",model.getImg());
    }
}
