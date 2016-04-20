package com.avos.avoscloud.internal.impl;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.internal.InternalCallback;

public class DefaultInternalCallback implements InternalCallback {
  public static DefaultInternalCallback instance() {
    synchronized (DefaultAppConfiguration.class) {
      if (instance == null) {
        instance = new DefaultInternalCallback();
      }
    }
    return instance;
  }

  private static DefaultInternalCallback instance;

  @Override
  public boolean isMainThread() {
    return true;
  }

  @Override
  public void internalDoneInMainThread(AVCallback parent, Object t, AVException e) {
    AVUtils.callCallback(parent, t, e);
  }

  @Override
  public void internalDoneInCurrentThread(AVCallback parent, Object t, AVException e) {
    AVUtils.callCallback(parent, t, e);
  }
}
