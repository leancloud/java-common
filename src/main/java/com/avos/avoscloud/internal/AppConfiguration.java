package com.avos.avoscloud.internal;

import static com.avos.avoscloud.AVOSServices.API;
import static com.avos.avoscloud.AVOSServices.ENGINE;
import static com.avos.avoscloud.AVOSServices.RTM;
import static com.avos.avoscloud.AVOSServices.STATS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import com.avos.avoscloud.AVOSServices;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.LogUtil;

public abstract class AppConfiguration {

  /**
   * API 默认的地址
   */
  private static final String DEFAULT_QCLOUD_API_SERVER = "https://e1-api.leancloud.cn";
  private static final String DEFAULT_US_API_SERVER = "https://us-api.leancloud.cn";

  /**
   * push router 默认地址
   */
  private static final String DEFAULT_QCLOUD_ROUTER_SERVER = "https://router-q0-push.leancloud.cn";
  private static final String DEFAULT_US_ROUTER_SERVER = "https://router-a0-push.leancloud.cn";

  private final String applicationIdField = "X-LC-Id";
  private final String apiKeyField = "X-LC-Key";
  String applicationId;
  String clientKey;
  private boolean isCN = true;
  private Map<AVOSServices, String> serviceHostMap = new ConcurrentHashMap<AVOSServices, String>();
  protected StorageType storageType = StorageType.StorageTypeQiniu;

  public enum StorageType {
    StorageTypeQiniu, StorageTypeAV, StorageTypeS3;
  }

  public void setStorageType(StorageType type) {
    this.storageType = type;
  }

  public StorageType getStorageType() {
    return this.storageType;
  }

  public String getServerUrl(AVOSServices type) {
    return serviceHostMap.get(type);
  }

  public void setServerUrl(AVOSServices type, String host) {
    serviceHostMap.put(type, host);
  }

  public abstract boolean isConfigured();

  public abstract void setupThreadPoolExecutor(ThreadPoolExecutor excutor);

  public abstract boolean isConnected();

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
    // 美国节点
    if (isUsApp()) {
      serviceHostMap.put(API, DEFAULT_US_API_SERVER);
      serviceHostMap.put(RTM, DEFAULT_US_ROUTER_SERVER);
      serviceHostMap.put(ENGINE, DEFAULT_US_API_SERVER);
      serviceHostMap.put(STATS, DEFAULT_US_API_SERVER);
      return;
    }
    // QCloud 节点
    if (isQCloudApp()) {
      serviceHostMap.put(API, DEFAULT_QCLOUD_API_SERVER);
      serviceHostMap.put(RTM, DEFAULT_QCLOUD_ROUTER_SERVER);
      serviceHostMap.put(ENGINE, DEFAULT_QCLOUD_API_SERVER);
      serviceHostMap.put(STATS, DEFAULT_QCLOUD_API_SERVER);
      return;
    }
    // UCloud 节点
    serviceHostMap.put(API, getUcloudDefaultServer(API));
    serviceHostMap.put(RTM, getUcloudDefaultServer(RTM));
    serviceHostMap.put(ENGINE, getUcloudDefaultServer(ENGINE));
    serviceHostMap.put(STATS, getUcloudDefaultServer(STATS));
  }

  public String getClientKey() {
    return clientKey;
  }

  public void setClientKey(String clientKey) {
    this.clientKey = clientKey;
  }

  public void setIsCN(boolean isCN) {
    this.isCN = isCN;
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

  /**
   * 获取默认的 UCloud 节点的 url
   * 
   * @param type
   * @return
   */
  private String getUcloudDefaultServer(AVOSServices type) {
    if (!AVUtils.isBlankString(applicationId)) {
      return String.format("https://%s.%s.lncld.net", applicationId.substring(0, 8),
          type.toString());
    } else {
      LogUtil.avlog.e("AppId is null, Please call AVOSCloud.initialize first");
      return "";
    }
  }

  /**
   * 判断是否为 UCloud 节点
   * 
   * @return
   */
  public boolean isUCloudApp() {
    return !isQCloudApp() && !isUsApp();
  }

  /**
   * QCloud 节点的末尾是写死的，这里根据末尾后缀判断是否为 QCloud 节点
   *
   * @return
   */
  private boolean isQCloudApp() {
    return !AVUtils.isBlankString(applicationId) && applicationId.endsWith("9Nh9j0Va");
  }

  /**
   * 判断是否为 us 节点
   * 
   * @return
   */
  private boolean isUsApp() {
    return !isCN || (!AVUtils.isBlankString(applicationId) && applicationId.endsWith("MdYXbMMI"));
  }

}
