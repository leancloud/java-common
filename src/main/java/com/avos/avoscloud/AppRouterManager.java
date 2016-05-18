package com.avos.avoscloud;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.okhttp.Request;


/**
 * Created by wli on 16/5/6. 注意：该类暂时仅针对 CN 节点有效果，非 CN 节点如非明确需求请不要改动
 */
public class AppRouterManager {

  /**
   * app router 地址
   */
  private static final String ROUTER_ADDRESS = " https://app-router.leancloud.cn/1/route?appId=";

  /**
   * share preference 的 key 值
   */
  private static final String PUSH_ROUTER_SERVER_KEY = "push_router_server";
  private static final String API_SERVER_KEY = "api_server";
  private static final String TTL_KEY = "ttl";
  private static final String LATEST_UPDATE_TIME_KEY = "latest_update_time";
  private static String routerSharePreferenceName = "";

  private String apiServer = "";
  private String routerServer = "";

  /**
   * api 默认的地址
   */
  private static final String DETAULT_UCLOUD_API_SERVER = "https://api.leancloud.cn";

  private static final String DEFAULT_QCLOUD_API_SERVER = "https://e1-api.leancloud.cn";

  /**
   * cn 节点（注意，只有 cn 节点）默认的 router 地址
   */
  private static final String DEFAULT_UCLOUD_ROUTER_SERVER = "https://router-g0-push.leancloud.cn";
  private static final String DEFAULT_QCLOUD_ROUTER_SERVER = "https://router-q0-push.leancloud.cn";


  public static AppRouterManager appRouterManager;

  public synchronized static AppRouterManager getInstance() {
    if (null == appRouterManager) {
      appRouterManager = new AppRouterManager();
    }
    return appRouterManager;
  }

  private AppRouterManager() {}

  /**
   * 获取 api server
   *
   * @return
   */
  public String getAPIServer() {
    if (AVUtils.isBlankContent(apiServer)) {
      return isQCloudApp(InternalConfigurationController.globalInstance().getAppConfiguration().applicationId) ? DEFAULT_QCLOUD_API_SERVER
          : DETAULT_UCLOUD_API_SERVER;
    }
    return apiServer;
  }

  /**
   * 获取 router server
   *
   * @return
   */
  public String getRouterServer() {
    if (AVUtils.isBlankContent(routerServer)) {
      return isQCloudApp(InternalConfigurationController.globalInstance().getAppConfiguration().applicationId) ? DEFAULT_QCLOUD_ROUTER_SERVER
          : DEFAULT_UCLOUD_ROUTER_SERVER;
    }
    return routerServer;
  }


  /**
   * 更新 router url 有可能因为测试或者 301 等原因需要运行过程中修改 url
   *
   * @param router
   * @param persistence 是否需要持久化存储到本地 为 true 则存到本地，app 下次打开后仍有效果，否则仅当次声明周期内有效
   */
  public void updateRouterServer(String router, boolean persistence) {
    routerServer = addHttpsPrefix(router);
    if (persistence) {
      InternalConfigurationController
          .globalInstance()
          .getInternalPersistence()
          .savePersistentSettingString(routerSharePreferenceName, PUSH_ROUTER_SERVER_KEY,
              routerServer);
    }
  }

  /**
   * 更新 api url 有可能因为测试或者 301 等原因需要运行过程中修改 url
   *
   * @param server
   * @param persistence 是否需要持久化存储到本地 为 true 则存到本地，app 下次打开后仍有效果，否则仅当次声明周期内有效
   */
  public void updateAPIServer(String server, boolean persistence) {
    apiServer = addHttpsPrefix(server);
    if (persistence) {
      InternalConfigurationController.globalInstance().getInternalPersistence()
          .savePersistentSettingString(routerSharePreferenceName, API_SERVER_KEY, apiServer);
    }
  }

  /**
   * 拉取 router 地址
   *
   * @param force 是否强制拉取，如果为 true 则强制拉取，如果为 false 则需要间隔超过 ttl 才会拉取
   */
  public void fetchRouter(boolean force) {
    updateServers();

    Long lastTime =
        InternalConfigurationController.globalInstance().getInternalPersistence()
            .getPersistentSettingLong(routerSharePreferenceName, LATEST_UPDATE_TIME_KEY, 0L);

    int ttl =
        InternalConfigurationController.globalInstance().getInternalPersistence()
            .getPersistentSettingInteger(routerSharePreferenceName, TTL_KEY, 0);

    if (force || System.currentTimeMillis() - lastTime > ttl * 1000) {
      PaasClient.AVHttpClient client = new PaasClient.AVHttpClient();
      Request.Builder builder = new Request.Builder();
      builder
          .url(
              ROUTER_ADDRESS
                  + InternalConfigurationController.globalInstance().getAppConfiguration().applicationId)
          .get();
      client.execute(builder.build(), false, new GetHttpResponseHandler(
          new GenericObjectCallback() {
            @Override
            public void onSuccess(String content, AVException e) {
              if (null == e) {
                if (InternalConfigurationController.globalInstance().getInternalLogger()
                    .showInternalDebugLog()) {
                  LogUtil.avlog.d(" fetchRouter :" + content);
                }

                com.alibaba.fastjson.JSONObject response = JSON.parseObject(content);
                if (null != response) {
                  if (response.containsKey(PUSH_ROUTER_SERVER_KEY)
                      && response.containsKey(API_SERVER_KEY)) {
                    apiServer = addHttpsPrefix(response.getString(API_SERVER_KEY));
                    routerServer = addHttpsPrefix(response.getString(PUSH_ROUTER_SERVER_KEY));
                    InternalConfigurationController
                        .globalInstance()
                        .getInternalPersistence()
                        .savePersistentSettingString(routerSharePreferenceName,
                            PUSH_ROUTER_SERVER_KEY, routerServer);
                    InternalConfigurationController
                        .globalInstance()
                        .getInternalPersistence()
                        .savePersistentSettingString(routerSharePreferenceName, API_SERVER_KEY,
                            apiServer);

                    if (response.containsKey(TTL_KEY)) {
                      InternalConfigurationController
                          .globalInstance()
                          .getInternalPersistence()
                          .savePersistentSettingInteger(routerSharePreferenceName, TTL_KEY,
                              response.getIntValue(TTL_KEY));
                    }
                    InternalConfigurationController
                        .globalInstance()
                        .getInternalPersistence()
                        .savePersistentSettingLong(routerSharePreferenceName,
                            LATEST_UPDATE_TIME_KEY, System.currentTimeMillis());

                    PaasClient.updateAPIServerWhenCN(apiServer);
                  }
                }
              } else {
                LogUtil.log.e("get router error ", e);
              }
            }

            @Override
            public void onFailure(Throwable error, String content) {
              LogUtil.log.e("get router error ", new AVException(error));
            }
          }));
    }
  }

  /**
   * 根据当前 appId 更新 shareprefenence 的 name 这样如果运行过程中动态切换了 appId，app router 仍然可以正常 work
   */
  private void updateServers() {
    routerSharePreferenceName =
        "com.avos.avoscloud.approuter."
            + InternalConfigurationController.globalInstance().getAppConfiguration().applicationId;
    routerServer =
        InternalConfigurationController
            .globalInstance()
            .getInternalPersistence()
            .getPersistentSettingString(routerSharePreferenceName, PUSH_ROUTER_SERVER_KEY,
                getAPIServer());
    apiServer =
        InternalConfigurationController
            .globalInstance()
            .getInternalPersistence()
            .getPersistentSettingString(routerSharePreferenceName, API_SERVER_KEY,
                getRouterServer());
    PaasClient.updateAPIServerWhenCN(apiServer);
  }

  /**
   * 添加 https 前缀 主要是因为 server 部分 url 返回数据不一致，有的有前缀，有的没有
   *
   * @param url
   * @return
   */
  private static String addHttpsPrefix(String url) {
    if (!AVUtils.isBlankContent(url) && !url.startsWith("http")) {
      return "https://" + url;
    }
    return url;
  }

  /**
   * QCloud 节点的末尾是写死的，这里根据末尾后缀判断是否为 QCloud 节点
   *
   * @return
   */
  private boolean isQCloudApp(String appId) {
    return !AVUtils.isBlankContent(appId) && appId.endsWith("9Nh9j0Va");
  }
}
