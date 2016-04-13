package com.avos.avoscloud.internal;

import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.internal.impl.DefaultAppConfiguration;
import com.avos.avoscloud.internal.impl.DefaultClientConfiguration;
import com.avos.avoscloud.internal.impl.DefaultInternalCacheImpementation;
import com.avos.avoscloud.internal.impl.DefaultInternalCallback;
import com.avos.avoscloud.internal.impl.EmptyLogger;
import com.avos.avoscloud.internal.impl.EmptyPersistence;

public class InternalConfigurationController {
  private InternalConfigurationController() {};

  private static InternalConfigurationController instance = new InternalConfigurationController();

  public static InternalConfigurationController globalInstance() {
    return instance;
  }

  private ClientConfiguration clientConfiguration;
  private AppConfiguration appConfiguration;
  private InternalCache cacheImplmentation;
  private DefaultInternalCallback internalCallback;
  private InternalLogger internalLogger;
  private InternalPersistence internalPersistence;

  private Class<? extends InternalFileDownloader> downloadImplementation;

  public ClientConfiguration getClientConfiguration() {
    return AVUtils.or(clientConfiguration, DefaultClientConfiguration.instance());
  }

  public void setClientConfiguration(ClientConfiguration config) {
    this.clientConfiguration = config;
  }

  public AppConfiguration getAppConfiguration() {
    return AVUtils.or(appConfiguration, DefaultAppConfiguration.instance());
  }

  public void setAppConfiguration(AppConfiguration config) {
    this.appConfiguration = config;
  }

  public InternalCache getCache() {
    return AVUtils.or(cacheImplmentation, DefaultInternalCacheImpementation.instance());
  }

  public void setCache(InternalCache cache) {
    this.cacheImplmentation = cache;
  }

  public void setInternalCallback(DefaultInternalCallback callback) {
    internalCallback = callback;
  }

  public DefaultInternalCallback getInternalCallback() {
    return AVUtils.or(internalCallback, DefaultInternalCallback.instance());
  }

  public InternalLogger getInternalLogger() {
    return AVUtils.or(internalLogger, EmptyLogger.instance());
  }

  public void setInternalLogger(InternalLogger logger) {
    internalLogger = logger;
  }

  public InternalPersistence getInternalPersistence() {
    return AVUtils.or(internalPersistence, EmptyPersistence.instance());
  }

  public void setInternalPersistence(InternalPersistence persistence) {
    this.internalPersistence = persistence;
  }

  public InternalFileDownloader getDownloaderInstance(ProgressCallback progressCallback,
      GetDataCallback getDataCallback){
    if (downloadImplementation != null) {
      try {
        InternalFileDownloader downloader = downloadImplementation.newInstance();
        downloader.setProgressCallback(progressCallback);
        downloader.setGetDataCallback(getDataCallback);
        return downloader;
      } catch (Exception e) {
       
      }
    }
    return null;
  }

  public void setDownloaderImplementation(Class<? extends InternalFileDownloader> clazz) {
    this.downloadImplementation = clazz;
  }


}
