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

/**
 * 用于配置所有平台相关代码的具体实现
 * 
 * @author lbt05
 *
 */
public class InternalConfigurationController {
  private InternalConfigurationController() {
    this.clientConfiguration = DefaultClientConfiguration.instance();
    this.appConfiguration = DefaultAppConfiguration.instance();
    this.cacheImplmentation = DefaultInternalCacheImpementation.instance();
    this.internalCallback = DefaultInternalCallback.instance();
    this.internalLogger = EmptyLogger.instance();
    this.internalPersistence = EmptyPersistence.instance();
    this.internalRequestSign = DefaultInternalRequestSign.instance();
    this.downloadImplementation = null;
  }

  private InternalConfigurationController(Builder builder) {
    this.clientConfiguration =
        AVUtils.or(builder.clientConfiguration, DefaultClientConfiguration.instance());
    this.appConfiguration =
        AVUtils.or(builder.appConfiguration, DefaultAppConfiguration.instance());
    this.cacheImplmentation =
        AVUtils.or(builder.cacheImplmentation, DefaultInternalCacheImpementation.instance());
    this.internalCallback =
        AVUtils.or(builder.internalCallback, DefaultInternalCallback.instance());
    this.internalLogger = AVUtils.or(builder.internalLogger, EmptyLogger.instance());
    this.internalPersistence = AVUtils.or(builder.internalPersistence, EmptyPersistence.instance());
    this.internalRequestSign =
        AVUtils.or(builder.internalRequestSign, DefaultInternalRequestSign.instance());
    this.downloadImplementation = builder.downloadImplementation;
  }

  private static InternalConfigurationController instance;

  public static InternalConfigurationController globalInstance() {
    if (instance == null) {
      instance = new InternalConfigurationController();
    }
    return instance;
  }

  private final InternalClientConfiguration clientConfiguration;
  private final AppConfiguration appConfiguration;
  private final InternalCache cacheImplmentation;
  private final InternalCallback internalCallback;
  private final InternalLogger internalLogger;
  private final InternalPersistence internalPersistence;
  private final InternalRequestSign internalRequestSign;

  private final Class<? extends InternalFileDownloader> downloadImplementation;

  public InternalClientConfiguration getClientConfiguration() {
    return clientConfiguration;
  }

  public AppConfiguration getAppConfiguration() {
    return appConfiguration;
  }

  public InternalCache getCache() {
    return cacheImplmentation;
  }

  public InternalCallback getInternalCallback() {
    return internalCallback;
  }

  public InternalLogger getInternalLogger() {
    return internalLogger;
  }

  public InternalPersistence getInternalPersistence() {
    return internalPersistence;
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

  public InternalRequestSign getInternalRequestSign() {
    return internalRequestSign;
  }

  public static class Builder {
    InternalClientConfiguration clientConfiguration;
    AppConfiguration appConfiguration;
    InternalCache cacheImplmentation;
    InternalCallback internalCallback;
    InternalLogger internalLogger;
    InternalPersistence internalPersistence;
    InternalRequestSign internalRequestSign;
    Class<? extends InternalFileDownloader> downloadImplementation;

    public Builder setDownloaderImplementation(Class<? extends InternalFileDownloader> clazz) {
      this.downloadImplementation = clazz;
      return this;
    }

    public Builder setInternalRequestSign(InternalRequestSign internalRequestSign) {
      this.internalRequestSign = internalRequestSign;
      return this;
    }

    public Builder setClientConfiguration(InternalClientConfiguration config) {
      this.clientConfiguration = config;
      return this;
    }

    public Builder setInternalPersistence(InternalPersistence persistence) {
      this.internalPersistence = persistence;
      return this;
    }

    public Builder setAppConfiguration(AppConfiguration config) {
      this.appConfiguration = config;
      return this;
    }

    public Builder setCache(InternalCache cache) {
      this.cacheImplmentation = cache;
      return this;
    }

    public Builder setInternalCallback(InternalCallback callback) {
      internalCallback = callback;
      return this;
    }

    public Builder setInternalLogger(InternalLogger logger) {
      internalLogger = logger;
      return this;
    }

    public InternalConfigurationController build() {
      InternalConfigurationController configurationController =
          new InternalConfigurationController(this);
      if (InternalConfigurationController.instance != null) {
        LogUtil.log.e("Please call this method before initialize");
        return null;
      } else {
        InternalConfigurationController.instance = configurationController;
        return configurationController;
      }
    }
  }
}
