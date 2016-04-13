package com.avos.avoscloud.internal.impl;

import com.avos.avoscloud.AVErrorUtils;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.GenericObjectCallback;
import com.avos.avoscloud.internal.InternalCache;

// Default implementation for Java.... Do nothing
public class DefaultInternalCacheImpementation implements InternalCache {

  public static DefaultInternalCacheImpementation instance() {
    synchronized (DefaultAppConfiguration.class) {
      if (instance == null) {
        instance = new DefaultInternalCacheImpementation();
      }
    }
    return instance;
  }

  private DefaultInternalCacheImpementation() {

  }

  private static DefaultInternalCacheImpementation instance;

  @Override
  public boolean hasValidCache(String key, String ts, long maxAgeInMilliseconds) {
    return false;
  }

  @Override
  public void get(String key, long maxAgeInMilliseconds, String ts,
      GenericObjectCallback getCallback) {
    if (getCallback != null) {
      getCallback
          .onFailure(AVErrorUtils.createException(AVException.CACHE_MISS,
              AVException.cacheMissingErrorString), null);
    }
  }

  @Override
  public void delete(String key) {

  }

  @Override
  public boolean save(String key, String content, String lastModifyTs) {
    return false;
  }

  @Override
  public void remove(String key, String ts) {

  }

  @Override
  public void cleanCache(int numberOfDays) {

  }

  @Override
  public boolean hasCache(String key) {
    return false;
  }

  @Override
  public boolean hasCache(String key, String ts) {
    return false;
  }

  @Override
  public void cleanAll() {}

}
