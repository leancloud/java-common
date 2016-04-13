package com.avos.avoscloud.internal;

import java.util.List;

import com.avos.avoscloud.okhttp.Interceptor;

public abstract class ClientConfiguration {

  public static final int DEFAULT_NETWORK_TIMEOUT = 15000;

  public abstract List<Interceptor> getClientInterceptors();

  public abstract void afterSuccess();

  int timeoutInMills = DEFAULT_NETWORK_TIMEOUT;


  public int getNetworkTimeoutInMills() {
    return timeoutInMills;
  }

  public void setNetworkTimeoutInMills(int timeout) {
    timeoutInMills = timeout;
  }
}
