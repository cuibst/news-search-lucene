package com.rzotgorz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
public class AddNewsTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testAddInvalidNews() throws Exception {
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/index/add_from_json").contentType(MediaType.APPLICATION_JSON_UTF8).content("{}");
        MvcResult result = mockMvc.perform(post).andReturn();
        int status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        JSONObject object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",401,object.getIntValue("code"));
        Assert.assertEquals("Invalid response message","Invalid News",object.getString("data"));
    }

    @Test
    public void testActualQuery() throws Exception {
        MockHttpServletRequestBuilder del = MockMvcRequestBuilders.post("/index/delete").contentType(MediaType.APPLICATION_JSON_UTF8).content("{\"id\":\"test1\"}");
        mockMvc.perform(del).andReturn();
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
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/index/add_from_json").contentType(MediaType.APPLICATION_JSON_UTF8).content(JSON.toJSONString(map));
        MvcResult result = mockMvc.perform(post).andReturn();
        int status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        JSONObject object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",200,object.getIntValue("code"));
        Assert.assertEquals("Invalid response message","News added successfully",object.getString("data"));

        result = mockMvc.perform(post).andReturn();
        Assert.assertEquals("Invalid status code.",200,status);
        object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",401,object.getIntValue("code"));
        Assert.assertEquals("Invalid response message","News Already Exists test1",object.getString("data"));

        mockMvc.perform(del).andReturn();
    }
}
