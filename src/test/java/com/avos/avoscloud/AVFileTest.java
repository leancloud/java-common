package com.avos.avoscloud;

import com.alibaba.fastjson.JSON;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Map;

/**
 * Created by wli on 2017/8/11.
 */
public class AVFileTest extends TestCase {

    public void testFileFromMap() {
        String FILE_JSON_STR = "{\"mime_type\":\"image\\/png\",\"updatedAt\":\"2017-08-09T03:41:53.225Z\",\"key\":\"1bf9754d1ec4e16bb7f8.png\",\"name\":\"name.png\",\"objectId\":\"598a848161ff4b006c409d80\",\"createdAt\":\"2017-08-09T03:41:53.225Z\",\"__type\":\"File\",\"url\":\"testurl\",\"provider\":\"qiniu\",\"metaData\":{\"size\":2860076,\"owner\":\"unknown\"},\"bucket\":\"I4ObRReg\"}";
        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(FILE_JSON_STR);
        AVFile file = AVFile.fileFromMap(jsonObject);
        Assert.assertEquals(file.getUrl(), "testurl");
        Assert.assertEquals(file.getObjectId(), "598a848161ff4b006c409d80");
        Assert.assertEquals(file.getSize(), 2860076);
    }

    public void testToMap() {
        String FILE_JSON_STR = "{\"mime_type\":\"image\\/png\",\"updatedAt\":\"2017-08-09T03:41:53.225Z\",\"key\":\"1bf9754d1ec4e16bb7f8.png\",\"name\":\"name.png\",\"objectId\":\"598a848161ff4b006c409d80\",\"createdAt\":\"2017-08-09T03:41:53.225Z\",\"__type\":\"File\",\"url\":\"testurl\",\"provider\":\"qiniu\",\"metaData\":{\"size\":2860076,\"owner\":\"unknown\"},\"bucket\":\"I4ObRReg\"}";
        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(FILE_JSON_STR);
        AVFile file = AVFile.fileFromMap(jsonObject);
        Map<String, Object> map = file.toMap();

        Assert.assertEquals(map.get("url"), "testurl");
        Assert.assertEquals(map.get("objectId"), "598a848161ff4b006c409d80");
        Assert.assertEquals(((Map<String, Object>)map.get("metaData")).get("size"), 2860076);
    }
}
