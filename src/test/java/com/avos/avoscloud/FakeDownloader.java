package com.avos.avoscloud;

import com.avos.avoscloud.internal.InternalFileDownloader;

public class FakeDownloader implements InternalFileDownloader {

  @Override
  public AVException doWork(String url) {
    return null;
  }

  @Override
  public void execute(String url) {
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public void setProgressCallback(ProgressCallback callback) {
  }

  @Override
  public void setGetDataCallback(GetDataCallback callback) {
  }

}
