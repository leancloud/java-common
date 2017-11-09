package com.avos.avoscloud;

import junit.framework.TestCase;

public class QCloudTest extends TestCase {

  public void testQCloudEndPoint() throws AVException {
    TestApp.init("DV8dqMSRqujdz6hI7NFtCfEq-9Nh9j0Va", "IPvWcVpYBvkHuk6QYc9Jvg3F", true);
    AVQuery<AVObject> query = new AVQuery<AVObject>("Validation");
    AVObject object = query.getFirst();
    assertTrue(object.getBoolean("isQCloud"));
  }

}
