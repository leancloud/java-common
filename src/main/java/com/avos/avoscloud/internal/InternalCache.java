package com.avos.avoscloud.internal;

import com.avos.avoscloud.GenericObjectCallback;

public interface InternalCache {
  public boolean hasCache(String key);

  public boolean hasCache(String key, String ts);

  /**
   * 是否有有效的 cache，本地有相应缓存并且没有超过缓存限制及认为有效
   *
   * @param key 缓存的key
   * @param ts 当前时间
   * @param maxAgeInMilliseconds 最大缓存有效时间
   * @return 是否有有效缓存
   */
  public boolean hasValidCache(String key, String ts, long maxAgeInMilliseconds);

  public void get(String key, long maxAgeInMilliseconds, String ts,
      GenericObjectCallback getCallback);

  public void delete(String key);

  public boolean save(String key, String content, String lastModifyTs);

  public void remove(String key, String ts);

  public void cleanCache(int numberOfDays);

  public void cleanAll();
}
