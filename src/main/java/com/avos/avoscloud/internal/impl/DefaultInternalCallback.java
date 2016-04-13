package com.avos.avoscloud.internal.impl;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUtils;

public class DefaultInternalCallback {
  public static DefaultInternalCallback instance() {
    synchronized (DefaultAppConfiguration.class) {
      if (instance == null) {
        instance = new DefaultInternalCallback();
      }
    }
    return instance;
  }

  private static DefaultInternalCallback instance;

  public void internalDone0(AVCallback parent, Object t, AVException e) {
    AVUtils.callCallback(parent, t, e);
  }
}
