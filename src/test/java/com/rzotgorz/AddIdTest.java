package com.rzotgorz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rzotgorz.service.DatabaseConnector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Entry.class)
@FixMethodOrder(MethodSorters.DEFAULT)
public class AddIdTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DatabaseConnector connector;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        connector.modify("DROP TABLE IF EXISTS backend_news;");
        connector.modify("CREATE TABLE backend_news" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, source STRING NOT NULL, news_url STRING NOT NULL, category STRING NOT NULL, " +
                "media STRING NOT NULL, tags STRING NOT NULL, title STRING NOT NULL, news_id STRING NOT NULL UNIQUE, " +
                "pub_date STRING NOT NULL, content STRING NOT NULL, summary STRING NOT NULL, img STRING NOT NULL)");
    }

    @Test
    public void testAddInvalidId() throws Exception{
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/index/add/").contentType(MediaType.APPLICATION_JSON_UTF8).content("{}");
        MvcResult result = mockMvc.perform(post).andReturn();
        int status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        JSONObject object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",401,object.getIntValue("code"));
        Assert.assertEquals("Invalid response message","Invalid news id",object.getString("data"));

        post = MockMvcRequestBuilders.post("/index/add/").contentType(MediaType.APPLICATION_JSON_UTF8).content("{\"news_id\":\"\"}");
        result = mockMvc.perform(post).andReturn();
        status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",401,object.getIntValue("code"));
        Assert.assertEquals("Invalid response message","Invalid news id",object.getString("data"));
    }

    @Test
    public void testAddValidId() throws Exception {
        connector.modify("DELETE FROM backend_news WHERE news_id = 'test1';");

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/index/add/").contentType(MediaType.APPLICATION_JSON_UTF8).content("{\"news_id\":\"test1\"}");
        MvcResult result = mockMvc.perform(post).andReturn();
        int status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        JSONObject object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",401,object.getIntValue("code"));
        Assert.assertEquals("Invalid response message","No news found with given id",object.getString("data"));

        connector.modify("INSERT INTO backend_news " +
                "(source, news_url, category, media, tags, title, news_id, pub_date, content, summary, img) " +
                "VALUES ('source','url','category','media','tag1,tag2','title','test1','2020-10-17','content','summary','image');");
        result = mockMvc.perform(post).andReturn();
        status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",200,object.getIntValue("code"));
        Assert.assertEquals("Invalid response message","News added successfully",object.getString("data"));

        result = mockMvc.perform(post).andReturn();
        status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",401,object.getIntValue("code"));
        Assert.assertEquals("Invalid response message","News Already Exists test1",object.getString("data"));

        MockHttpServletRequestBuilder del = MockMvcRequestBuilders.post("/index/delete").contentType(MediaType.APPLICATION_JSON_UTF8).content("{\"id\":\"test1\"}");
        mockMvc.perform(del).andReturn();

        connector.modify("DELETE FROM backend_news WHERE news_id = 'test1';");
    }
}
