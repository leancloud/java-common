package com.avos.avoscloud.internal.impl;

import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.internal.InternalRequestSign;

public class DefaultInternalRequestSign implements InternalRequestSign {

  public static DefaultInternalRequestSign instance() {
    synchronized (DefaultInternalRequestSign.class) {
      if (instance == null) {
        instance = new DefaultInternalRequestSign();
      }
    }
    return instance;
  }

  private DefaultInternalRequestSign() {}

  private static DefaultInternalRequestSign instance;

  @Override
  public String requestSign() {
    StringBuilder builder = new StringBuilder();
    long ts = AVUtils.getCurrentTimestamp();
    StringBuilder result = new StringBuilder();
    result.append(AVUtils.md5(
        builder
            .append(ts)
            .append(
                InternalConfigurationController.globalInstance().getAppConfiguration().clientKey)
            .toString()).toLowerCase());
    return result.append(',').append(ts).toString();
  }
}
