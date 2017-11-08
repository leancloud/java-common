package com.avos.avoscloud.internal;

import java.util.HashMap;
import java.util.Map;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVExceptionHolder;
import com.avos.avoscloud.AVOSServices;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.FunctionCallback;
import com.avos.avoscloud.LogUtil;

public abstract class AppRouter {

  protected abstract void fetchServerHosts(boolean sync,
      FunctionCallback<Map<AVOSServices, String>> cb);

  public void updateServerHosts() {
    final Map<AVOSServices, String> result = new HashMap<AVOSServices, String>();
    fetchServerHosts(true, new FunctionCallback<Map<AVOSServices, String>> () {

      @Override
      public void done(Map<AVOSServices, String> hosts, AVException e) {
        if (e != null) {
          AVExceptionHolder.add(e);
        }
        result.putAll(hosts);
      }

    });
    if (AVExceptionHolder.exists()) {
      throw new RuntimeException(AVExceptionHolder.remove());
    }

    updateMapAndSaveLocal(result, AVOSServices.RTM);
    updateMapAndSaveLocal(result, AVOSServices.PUSH);
    updateMapAndSaveLocal(result, AVOSServices.API);
    updateMapAndSaveLocal(result, AVOSServices.STATS);
    updateMapAndSaveLocal(result, AVOSServices.ENGINE);
  }

  public void updateServerHostsInBackground() {
    fetchServerHosts(false, new FunctionCallback<Map<AVOSServices, String>>() {

      @Override
      public void done(Map<AVOSServices, String> hosts, AVException e) {
        if (e != null) {
          LogUtil.log.e("fetchServerHosts error ", e);
          return;
        }

        updateMapAndSaveLocal(hosts, AVOSServices.RTM);
        updateMapAndSaveLocal(hosts, AVOSServices.PUSH);
        updateMapAndSaveLocal(hosts, AVOSServices.API);
        updateMapAndSaveLocal(hosts, AVOSServices.STATS);
        updateMapAndSaveLocal(hosts, AVOSServices.ENGINE);
      }

    });
  }

  private void updateMapAndSaveLocal(Map<AVOSServices, String> hosts, AVOSServices service) {
    String host = hosts.get(service);
    if (AVUtils.isBlankString(host)) {
      return;
    }

    InternalConfigurationController.globalInstance().getInternalPersistence()
        .savePersistentSettingString(getAppRouterSPName(), service.toString(), host);
    InternalConfigurationController.globalInstance().getAppConfiguration().setServerUrl(service,
        host);
  }

  protected String getAppRouterSPName() {
    return "com.avos.avoscloud.approuter."
        + InternalConfigurationController.globalInstance().getAppConfiguration().applicationId;
  }

}
