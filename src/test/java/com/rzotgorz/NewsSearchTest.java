package com.rzotgorz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import junit.framework.TestCase;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Entry.class)
public class NewsSearchTest extends TestCase {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testNullQuery() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/index/search").param("query","");
        MvcResult result = mockMvc.perform(get).andReturn();
        int status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        JSONObject object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",401,object.getIntValue("code"));
        Assert.assertEquals("Invalid response message","invalid query",object.getString("infolist"));
    }

    @Test
    public void testActualQuery() throws Exception {
        MockHttpServletRequestBuilder del1 = MockMvcRequestBuilders.post("/index/delete").contentType(MediaType.APPLICATION_JSON_UTF8).content("{\"id\":\"test1\"}");
        MockHttpServletRequestBuilder del2 = MockMvcRequestBuilders.post("/index/delete").contentType(MediaType.APPLICATION_JSON_UTF8).content("{\"id\":\"test2\"}");
        mockMvc.perform(del1).andReturn();
        mockMvc.perform(del2).andReturn();
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
        MockHttpServletRequestBuilder post1 = MockMvcRequestBuilders.post("/index/add_from_json").contentType(MediaType.APPLICATION_JSON_UTF8).content(JSON.toJSONString(map));
        map.put("news_id","test2");
        MockHttpServletRequestBuilder post2 = MockMvcRequestBuilders.post("/index/add_from_json").contentType(MediaType.APPLICATION_JSON_UTF8).content(JSON.toJSONString(map));
        mockMvc.perform(post1).andReturn();
        mockMvc.perform(post2).andReturn();
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/index/search").param("query","title");
        MvcResult result = mockMvc.perform(get).andReturn();
        int status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        JSONObject object = JSONObject.parseObject(result.getResponse().getContentAsString());
        System.out.println(object.toString());
        Assert.assertEquals("Invalid response code.",200,object.getIntValue("code"));
        try {
            List<JSONObject> contents = JSON.parseArray(object.getJSONArray("infolist").toJSONString(),JSONObject.class);
            Assert.assertTrue("Search results is not enough",contents.size()>=2);
        } catch (Exception e) {
            fail("Invalid search results");
        }
        get = MockMvcRequestBuilders.get("/index/search").param("query","title").param("count","1");
        result = mockMvc.perform(get).andReturn();
        status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",200,object.getIntValue("code"));
        try {
            List<JSONObject> contents = JSON.parseArray(object.getJSONArray("infolist").toJSONString(),JSONObject.class);
            Assert.assertEquals("Too many results!",1,contents.size());
        } catch (Exception e) {
            fail("Invalid search results");
        }
        get = MockMvcRequestBuilders.get("/index/search").param("query","title").param("start","20");
        result = mockMvc.perform(get).andReturn();
        status = result.getResponse().getStatus();
        Assert.assertEquals("Invalid status code.",200,status);
        object = JSONObject.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals("Invalid response code.",200,object.getIntValue("code"));
        try {
            List<JSONObject> contents = JSON.parseArray(object.getJSONArray("infolist").toJSONString(),JSONObject.class);
            Assert.assertEquals("Too many results!",0,contents.size());
        } catch (Exception e) {
            fail("Invalid search results");
        }
        mockMvc.perform(del1).andReturn();
        mockMvc.perform(del2).andReturn();
    }
}
