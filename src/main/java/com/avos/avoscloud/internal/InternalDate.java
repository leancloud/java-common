package com.avos.avoscloud.internal;

import java.util.Date;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVErrorUtils;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVExceptionHolder;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.GenericObjectCallback;
import com.avos.avoscloud.PaasClient;
import com.avos.avoscloud.callback.AVServerDateCallback;

public class InternalDate {

  /**
   * 获取服务器端当前时间
   * 
   * @param callback 请求成功以后,会调用 callback.done(date,e)
   */
  public static void getServerDateInBackground(AVServerDateCallback callback) {
    getServerDateInBackground(false, callback);
  }

  /**
   * 获取服务器端当前时间
   * 
   * @return 服务器时间
   * @throws AVException 请求异常
   */
  public static Date getServerDate() throws AVException {
    final Date[] results = {null};
    getServerDateInBackground(true, new AVServerDateCallback() {
      @Override
      public void done(Date serverDate, AVException e) {
        if (e == null) {
          results[0] = serverDate;
        } else {
          AVExceptionHolder.add(e);
        }
      }

      @Override
      public boolean mustRunOnUIThread() {
        return false;
      }
    });
    if (AVExceptionHolder.exists()) {
      throw AVExceptionHolder.remove();
    }
    return results[0];
  }

  private static void getServerDateInBackground(boolean sync, final AVServerDateCallback callback) {
    PaasClient.storageInstance().getObject("date", null, sync, null, new GenericObjectCallback() {
      @Override
      public void onSuccess(String content, AVException e) {
        try {
          Date date = AVUtils.dateFromMap(JSON.parseObject(content, Map.class));
          if (callback != null) {
            callback.internalDone(date, null);
          }
        } catch (Exception ex) {
          if (callback != null) {
            callback.internalDone(null, AVErrorUtils.createException(ex, null));
          }
        }
      }

      @Override
      public void onFailure(Throwable error, String content) {
        if (callback != null) {
          callback.internalDone(null, AVErrorUtils.createException(error, content));
        }
      }
    });
  }
}
