package com.rzotgorz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.web.servlet.config.MvcNamespaceHandler;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Entry.class)
public class DeleteNewsTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testDeleteInvalidId() throws Exception {
        MockHttpServletRequestBuilder del = MockMvcRequestBuilders.post("/index/delete").contentType(MediaType.APPLICATION_JSON_UTF8).content("{\"id\":\"\"}");
        MvcResult result = mockMvc.perform(del).andReturn();
        int status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        JSONObject object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",401,object.getIntValue("code"));
        Assert.assertEquals("Invalid response message","Invalid news id",object.getString("data"));
    }

    @Test
    public void testActualQuery() throws Exception {
        MockHttpServletRequestBuilder del = MockMvcRequestBuilders.post("/index/delete").contentType(MediaType.APPLICATION_JSON_UTF8).content("{\"id\":\"test1\"}");
        mockMvc.perform(del).andReturn();
        MvcResult result = mockMvc.perform(del).andReturn();
        int status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        JSONObject object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",401,object.getIntValue("code"));
        Assert.assertEquals("Invalid response message","News doesn't exist",object.getString("data"));
        Map<String, Object> map = new HashMap<>();
        map.put("title", "title");
        map.put("content", "Sample content");
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
        mockMvc.perform(post).andReturn();
        result = mockMvc.perform(del).andReturn();
        status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",200,object.getIntValue("code"));
        Assert.assertEquals("Invalid response message","News deleted successfully",object.getString("data"));
    }
}
