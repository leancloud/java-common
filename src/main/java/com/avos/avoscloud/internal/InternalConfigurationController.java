package com.avos.avoscloud.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.internal.impl.DefaultAppConfiguration;
import com.avos.avoscloud.internal.impl.DefaultClientConfiguration;
import com.avos.avoscloud.internal.impl.DefaultInternalCacheImpementation;
import com.avos.avoscloud.internal.impl.DefaultInternalCallback;
import com.avos.avoscloud.internal.impl.DefaultInternalRequestSign;
import com.avos.avoscloud.internal.impl.EmptyLogger;
import com.avos.avoscloud.internal.impl.EmptyPersistence;

public class InternalConfigurationController {
  private InternalConfigurationController() {};

  private static InternalConfigurationController instance = new InternalConfigurationController();

  public static InternalConfigurationController globalInstance() {
    return instance;
  }

  private InternalClientConfiguration clientConfiguration;
  private AppConfiguration appConfiguration;
  private InternalCache cacheImplmentation;
  private InternalCallback internalCallback;
  private InternalLogger internalLogger;
  private InternalPersistence internalPersistence;
  private InternalRequestSign internalRequestSign;

  private Class<? extends InternalFileDownloader> downloadImplementation;

  public InternalClientConfiguration getClientConfiguration() {
    return AVUtils.or(clientConfiguration, DefaultClientConfiguration.instance());
  }

  public void setClientConfiguration(InternalClientConfiguration config) {
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

  public void setInternalCallback(InternalCallback callback) {
    internalCallback = callback;
  }

  public InternalCallback getInternalCallback() {
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
      GetDataCallback getDataCallback) {
    InternalFileDownloader downloader = null;
    if (downloadImplementation != null) {
      try {
        downloader = downloadImplementation.newInstance();
        return downloader;
      } catch (IllegalAccessException e) {
        try {
          Constructor constructor = downloadImplementation.getDeclaredConstructor();
          constructor.setAccessible(true);
          downloader = (InternalFileDownloader) constructor.newInstance();
        } catch (SecurityException | NoSuchMethodException e1) {
        } catch (InstantiationException e1) {
          e1.printStackTrace();
        } catch (IllegalAccessException e1) {
          e1.printStackTrace();
        } catch (IllegalArgumentException e1) {
          e1.printStackTrace();
        } catch (InvocationTargetException e1) {
          e1.printStackTrace();
        }
      } catch (InstantiationException e) {
      }
    }
    if (downloader != null) {
      downloader.setProgressCallback(progressCallback);
      downloader.setGetDataCallback(getDataCallback);
    }

    return downloader;
  }

  public void setDownloaderImplementation(Class<? extends InternalFileDownloader> clazz) {
    this.downloadImplementation = clazz;
  }

  public InternalRequestSign getInternalRequestSign() {
    return internalRequestSign == null ? DefaultInternalRequestSign.instance()
        : internalRequestSign;
  }

  public void setInternalRequestSign(InternalRequestSign internalRequestSign) {
    this.internalRequestSign = internalRequestSign;
  }


}
