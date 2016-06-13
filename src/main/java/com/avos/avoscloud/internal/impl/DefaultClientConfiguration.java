package com.avos.avoscloud.internal.impl;

import java.util.LinkedList;
import java.util.List;

import com.avos.avoscloud.internal.InternalClientConfiguration;
import com.avos.avoscloud.okhttp.Interceptor;

public class DefaultClientConfiguration extends InternalClientConfiguration {
  List<Interceptor> clientInterceptors = new LinkedList<>();

  @Override
  public List<Interceptor> getClientInterceptors() {
    return clientInterceptors;
  }

  public static DefaultClientConfiguration instance() {
    synchronized (DefaultClientConfiguration.class) {
      if (instance == null) {
        instance = new DefaultClientConfiguration();
      }
    }
    return instance;
  }

  private DefaultClientConfiguration() {}

  private static DefaultClientConfiguration instance;

  @Override
  public void afterSuccess() {
    // do nothing
  }

  @Override
  public String getUserAgent() {
    return "JavaSDK";
  }
}
