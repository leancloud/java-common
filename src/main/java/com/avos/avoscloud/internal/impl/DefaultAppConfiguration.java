package com.avos.avoscloud.internal.impl;

import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.internal.AppConfiguration;

import java.util.concurrent.ThreadPoolExecutor;

public class DefaultAppConfiguration extends AppConfiguration {

  public static DefaultAppConfiguration instance() {
    synchronized (DefaultAppConfiguration.class) {
      if (instance == null) {
        instance = new DefaultAppConfiguration();
      }
    }
    return instance;
  }

  private DefaultAppConfiguration() {}

  private static DefaultAppConfiguration instance;


  @Override
  public boolean isConfigured() {
    return !(AVUtils.isBlankString(this.getApplicationId()) || AVUtils.isBlankString(this
        .getClientKey()));
  }

  @Override
  public void setupThreadPoolExecutor(ThreadPoolExecutor excutor) {
    // do nothing
  }

  @Override
  public boolean isConnected() {
    return true;
  }

}
