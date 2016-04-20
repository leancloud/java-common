package com.avos.avoscloud.internal;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;

public interface InternalCallback {

  public boolean isMainThread();

  public void internalDoneInMainThread(AVCallback parent, Object t, AVException e);

  public void internalDoneInCurrentThread(AVCallback parent, Object t, AVException e);
}
