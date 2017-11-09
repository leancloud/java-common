package com.avos.avoscloud;

import com.avos.avoscloud.internal.InternalConfigurationController;

import junit.framework.Assert;
import junit.framework.TestCase;

public class BasicTest extends TestCase {
  @Override
  public void setUp() {
    TestApp.init();
  }

  public void testAppConfiguration() {
    Assert.assertEquals("uu2P5gNTxGhjyaJGAPPnjCtJ-gzGzoHsz", InternalConfigurationController
        .globalInstance().getAppConfiguration().getApplicationId());
    Assert.assertEquals("j5lErUd6q7LhPD8CXhfmA2Rg", InternalConfigurationController
        .globalInstance().getAppConfiguration().getClientKey());
  }

  public void testDownloader() {
    Assert.assertNotNull(InternalConfigurationController.globalInstance().getDownloaderInstance(
        null, null));
  }

}
