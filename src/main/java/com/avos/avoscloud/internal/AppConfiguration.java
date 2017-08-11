package com.avos.avoscloud.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import com.avos.avoscloud.AVOSServices;
import com.avos.avoscloud.AVUtils;

public abstract class AppConfiguration {

  private final String applicationIdField = "X-LC-Id";
  private final String apiKeyField = "X-LC-Key";
  String applicationId;
  String clientKey;
  protected StorageType storageType = StorageType.StorageTypeQiniu;

  protected static Map<String, String> serviceHostMap = Collections
      .synchronizedMap(new HashMap<String, String>());

  public enum StorageType {
    StorageTypeQiniu, StorageTypeAV, StorageTypeS3;
  }

  public void setStorageType(StorageType type) {
    this.storageType = type;
  }

  public StorageType getStorageType() {
    return this.storageType;
  }

  public String getService(String service) {
    return AVUtils.isBlankString(serviceHostMap.get(service)) ? serviceHostMap
        .get(AVOSServices.STORAGE_SERVICE.toString()) : serviceHostMap.get(service);
  }

  public void configureService(String service, String host) {
    serviceHostMap.put(service, host);
  }

  public abstract boolean isConfigured();

  public abstract void setupThreadPoolExecutor(ThreadPoolExecutor excutor);

  public abstract boolean isConnected();

  // 我们在这里做一些额外的配置
  protected abstract void setEnv();

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public String getClientKey() {
    return clientKey;
  }

  public void setClientKey(String clientKey) {
    this.clientKey = clientKey;
  }

  public Map<String, String> getRequestHeaders() {
    Map<String, String> result = new HashMap<String, String>();
    result.put(applicationIdField, applicationId);
    return result;
  }

  public String dumpRequestHeaders() {
    return String.format("-H \"%s: %s\" -H \"%s: %s\"", applicationIdField, applicationId,
        apiKeyField, dumpKey(clientKey, "YourAppKey"));
  }

  protected String dumpKey(String key, String mask) {
    return InternalConfigurationController.globalInstance().getInternalLogger()
        .showInternalDebugLog() ? 
      key : mask;
  }

}
