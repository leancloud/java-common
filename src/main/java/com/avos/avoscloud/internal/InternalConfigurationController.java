package com.avos.avoscloud.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.GetDataCallback;
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

  public AppConfiguration getAppConfiguration() {
    return AVUtils.or(appConfiguration, DefaultAppConfiguration.instance());
  }

  public InternalCache getCache() {
    return AVUtils.or(cacheImplmentation, DefaultInternalCacheImpementation.instance());
  }

  public InternalCallback getInternalCallback() {
    return AVUtils.or(internalCallback, DefaultInternalCallback.instance());
  }

  public InternalLogger getInternalLogger() {
    return AVUtils.or(internalLogger, EmptyLogger.instance());
  }

  public InternalPersistence getInternalPersistence() {
    return AVUtils.or(internalPersistence, EmptyPersistence.instance());
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
    return internalRequestSign == null ? DefaultInternalRequestSign.instance()
        : internalRequestSign;
  }

  void configure(Builder builder) {
    this.clientConfiguration = builder.clientConfiguration;
    this.appConfiguration = builder.appConfiguration;
    this.cacheImplmentation = builder.cacheImplmentation;
    this.internalCallback = builder.internalCallback;
    this.internalLogger = builder.internalLogger;
    this.internalPersistence = builder.internalPersistence;
    this.internalRequestSign = builder.internalRequestSign;
    this.downloadImplementation = builder.downloadImplementation;
  }

  public static class Builder {
    InternalClientConfiguration clientConfiguration = globalInstance().getClientConfiguration();
    AppConfiguration appConfiguration = globalInstance().getAppConfiguration();
    InternalCache cacheImplmentation = globalInstance().getCache();
    InternalCallback internalCallback = globalInstance().getInternalCallback();
    InternalLogger internalLogger = globalInstance().getInternalLogger();
    InternalPersistence internalPersistence = globalInstance().getInternalPersistence();
    InternalRequestSign internalRequestSign = globalInstance().getInternalRequestSign();
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

    public void build() {
      globalInstance().configure(this);
    }
  }
}
