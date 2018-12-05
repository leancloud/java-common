package com.avos.avoscloud;

import junit.framework.TestCase;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class RelationTest extends TestCase {
  public RelationTest(String testName) {
    super(testName);
    TestApp.init();
  }

  public void testRoleRelationQuery() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    AVQuery<AVRole> roleQuery=new AVQuery<AVRole>("_Role");
    roleQuery.whereEqualTo("name", "CTO");
    roleQuery.findInBackground(new FindCallback<AVRole>() {
      @Override
      public void done(List<AVRole> list, AVException e) {
        System.out.println("get target roles.");
        if (list.size() < 1) {
          latch.countDown();
          return;
        }
        AVRole administrator = list.get(0);
        AVRelation userRelation = administrator.getUsers();
        AVQuery<AVUser> query = userRelation.getQuery(AVUser.class);
        query.findInBackground(new FindCallback<AVUser>() {
          @Override
          public void done(List<AVUser> list, AVException e) {
            // list 就是拥有该角色权限的所有用户了。
            System.out.println(list);
            latch.countDown();
          }
        });
      }
    });
    latch.await();
  }

}
