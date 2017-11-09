package com.avos.avoscloud.internal.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSServices;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.FunctionCallback;
import com.avos.avoscloud.GenericObjectCallback;
import com.avos.avoscloud.GetHttpResponseHandler;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.PaasClient;
import com.avos.avoscloud.internal.AppRouter;
import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.okhttp.Request;


/**
 * Created by wli on 16/5/6. 注意：该类暂时仅针对 CN 节点有效果，非 CN 节点如非明确需求请不要改动
 */
public class DefaultAppRouter extends AppRouter {

  /**
   * app router 地址
   */
  private static final String ROUTER_ADDRESS = " https://app-router.leancloud.cn/2/route?appId=";

  /**
   * share preference 的 key 值
   */
  private static final String API_SERVER_KEY = "api_server";
  private static final String STATS_SERVER_KEY = "stats_server";
  private static final String RTM_ROUTER_SERVER_KEY = "rtm_router_server";
  private static final String PUSH_SERVER_KEY = "push_server";
  private static final String ENGINE_SERVER_KEY = "engine_server";
  private static final String TTL_KEY = "ttl";

  private static final String LATEST_UPDATE_TIME_KEY = "latest_update_time";

  public static DefaultAppRouter appRouter;

  public synchronized static DefaultAppRouter instance() {
    if (null == appRouter) {
      appRouter = new DefaultAppRouter();
    }
    return appRouter;
  }

  private DefaultAppRouter() {}

  @Override
  protected void fetchServerHosts(final boolean sync,
      final FunctionCallback<Map<AVOSServices, String>> cb) {
    if (!InternalConfigurationController.globalInstance().getAppConfiguration().isUCloudApp()) {
      Map<AVOSServices, String> s = Collections.emptyMap();
      cb.done(s, null);
      return;
    }

    Long lastTime =
        InternalConfigurationController.globalInstance().getInternalPersistence()
            .getPersistentSettingLong(getAppRouterSPName(), LATEST_UPDATE_TIME_KEY, 0L);

    int ttl =
        InternalConfigurationController.globalInstance().getInternalPersistence()
            .getPersistentSettingInteger(getAppRouterSPName(), TTL_KEY, 0);

    if (System.currentTimeMillis() - lastTime > ttl * 1000) {
      PaasClient.AVHttpClient client = new PaasClient.AVHttpClient();
      Request.Builder builder = new Request.Builder();
      builder.url(
          ROUTER_ADDRESS
              + InternalConfigurationController.globalInstance().getAppConfiguration()
                  .getApplicationId()).get();
      client.execute(builder.build(), sync, new GetHttpResponseHandler(
          new GenericObjectCallback() {
            @Override
            public void onSuccess(String content, AVException e) {
              if (null == e) {
                if (InternalConfigurationController.globalInstance().getInternalLogger()
                    .showInternalDebugLog()) {
                  LogUtil.avlog.d(" fetchRouter :" + content);
                }
                cb.done(parseRouterResult(content), null);
              } else {
                cb.done(null, e);
              }
            }

            @Override
            public void onFailure(Throwable error, String content) {
              LogUtil.log.e("get router error ", new AVException(error));
            }
          }));
    }
  }

  private Map<AVOSServices, String> parseRouterResult(String result) {
    com.alibaba.fastjson.JSONObject response = null;
    response = JSON.parseObject(result);
    if (null == response) {
      return Collections.emptyMap();
    }

    Map<AVOSServices, String> hosts = new HashMap<AVOSServices, String>();
    updateMap(hosts, response, AVOSServices.RTM, RTM_ROUTER_SERVER_KEY);
    updateMap(hosts, response, AVOSServices.PUSH, PUSH_SERVER_KEY);
    updateMap(hosts, response, AVOSServices.API, API_SERVER_KEY);
    updateMap(hosts, response, AVOSServices.STATS, STATS_SERVER_KEY);
    updateMap(hosts, response, AVOSServices.ENGINE, ENGINE_SERVER_KEY);

    if (response.containsKey(TTL_KEY)) {
      InternalConfigurationController
          .globalInstance()
          .getInternalPersistence()
          .savePersistentSettingInteger(getAppRouterSPName(), TTL_KEY,
              response.getIntValue(TTL_KEY));
    }
    InternalConfigurationController.globalInstance().getInternalPersistence()
        .savePersistentSettingLong(getAppRouterSPName(), LATEST_UPDATE_TIME_KEY,
            System.currentTimeMillis());
    return hosts;
  }

  private void updateMap(Map<AVOSServices, String> hosts, JSONObject jsonObject,
      AVOSServices service, String jsonKey) {
    if (jsonObject.containsKey(jsonKey)) {
      String value = addHttpsPrefix(jsonObject.getString(jsonKey));
      hosts.put(service, value);
    }
  }

  /**
   * 添加 https 前缀 主要是因为 server 部分 url 返回数据不一致，有的有前缀，有的没有
   *
   * @param url
   * @return
   */
  private String addHttpsPrefix(String url) {
    if (!AVUtils.isBlankString(url) && !url.startsWith("http")) {
      return "https://" + url;
    }
    return url;
  }

}
