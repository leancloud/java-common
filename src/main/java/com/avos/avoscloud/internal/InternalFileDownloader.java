package com.avos.avoscloud.internal;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.ProgressCallback;

public interface InternalFileDownloader {

  public AVException doWork(final String url);

  public void execute(String url);

  public boolean cancel(boolean mayInterruptIfRunning);

  public void setProgressCallback(ProgressCallback callback);

  public void setGetDataCallback(GetDataCallback callback);
  
}
