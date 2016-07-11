package com.avos.avoscloud.internal.impl;

import java.util.concurrent.ThreadPoolExecutor;

import com.avos.avoscloud.AVOSServices;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.AppRouterManager;
import com.avos.avoscloud.internal.AppConfiguration;

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

  @Override
  public void setClientKey(String clientKey) {
    super.setClientKey(clientKey);
    this.setEnv();
  };

  @Override
  protected void setEnv() {
    serviceHostMap.put(AVOSServices.STORAGE_SERVICE.toString(), AppRouterManager.getInstance()
        .getAPIServer());
  }
}
