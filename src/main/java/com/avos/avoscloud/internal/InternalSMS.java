package com.avos.avoscloud.internal;

import java.util.HashMap;
import java.util.Map;

import com.avos.avoscloud.AVErrorUtils;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVExceptionHolder;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.GenericObjectCallback;
import com.avos.avoscloud.PaasClient;
import com.avos.avoscloud.RequestMobileCodeCallback;

public class InternalSMS {
  /**
   * 请求发送短信验证码
   * 
   * 请在异步任务中调用本方法，或者调用requestSMSCodeInBackgroud(String phone, String name, String op, int ttl,
   * RequestMobileCodeCallback callback)方法
   * 
   * 短信示范为: 您正在{name}中进行{op}，您的验证码是:{Code}，请输入完整验证，有效期为:{ttl}
   * 
   * 
   * @param phone　目标手机号码(必选)
   * @param name　应用名,值为null 则默认是您的应用名
   * @param op　　验证码的目标操作，值为null,则默认为“短信验证”
   * @param ttl　验证码过期时间,单位分钟。如果是0，则默认为10分钟
   * @throws AVException 请求异常
   */
  public static void requestSMSCode(String phone, String name, String op, int ttl)
      throws AVException {

    requestSMSCodeInBackground(phone, null, getSMSCodeEnv(name, op, ttl), true,
        new RequestMobileCodeCallback() {
          @Override
          public void done(AVException e) {
            if (e != null) {
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
  }

  private static Map<String, Object> getSMSCodeEnv(String name, String op, int ttl) {
    Map<String, Object> map = new HashMap<String, Object>();
    if (!AVUtils.isBlankString(op)) {
      map.put("op", op);
    }
    if (!AVUtils.isBlankString(name)) {
      map.put("name", name);
    }
    if (ttl > 0) {
      map.put("ttl", ttl);
    }
    return map;
  }

  private static Map<String, Object> getVoiceCodeEnv(String countryCode) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("smsType", "voice");
    if (!AVUtils.isBlankString(countryCode)) {
      map.put("IDD", countryCode);
    }
    return map;
  }


  @Deprecated
  public static void requestSMSCodeInBackgroud(String phone, String name, String op, int ttl,
      RequestMobileCodeCallback callback) {
    requestSMSCodeInBackground(phone, null, getSMSCodeEnv(name, op, ttl), false, callback);
  }

  /**
   * 请求发送短信验证码
   *
   * 短信示范为: 您正在{name}中进行{op}，您的验证码是:{Code}，请输入完整验证，有效期为:{ttl}
   *
   *
   * @param phone　目标手机号码(必选)
   * @param name　应用名,值为null 则默认是您的应用名
   * @param op　　验证码的目标操作，值为null,则默认为“短信验证”
   * @param ttl　验证码过期时间,单位分钟。如果是0，则默认为10分钟
   * @param callback 请求成功以后 callback.done(e)会被调用
   */
  public static void requestSMSCodeInBackground(String phone, String name, String op, int ttl,
      RequestMobileCodeCallback callback) {
    requestSMSCodeInBackground(phone, null, getSMSCodeEnv(name, op, ttl), false, callback);
  }

  /**
   * 通过短信模板来发送短信验证码
   * 
   * 请在异步任务中调用本方法,或者调用 public static void requestSMSCodeInBackground(String phone, String template,
   * Map env, RequestMobileCodeCallback callback)
   * 
   * @param phone 目标手机号码(必选)
   * @param templateName 短信模板名称
   * @param env 需要注入的变量env
   * @throws AVException 请求异常
   */
  public static void requestSMSCode(String phone, String templateName, Map<String, Object> env)
      throws AVException {
    requestSMSCodeInBackground(phone, templateName, env, true, new RequestMobileCodeCallback() {
      @Override
      public void done(AVException e) {
        if (e != null) {
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
  }

  @Deprecated
  public static void requestSMSCodeInBackgroud(String phone, String templateName,
      Map<String, Object> env, RequestMobileCodeCallback callback) {
    requestSMSCodeInBackground(phone, templateName, env, false, callback);
  }

  /**
   * 通过短信模板来发送短信验证码
   *
   * @param phone 目标手机号码(必选)
   * @param templateName 短信模板名称
   * @param env 需要注入的变量env
   * @param callback 请求完成以后 callback.done(e)会被调用
   */
  public static void requestSMSCodeInBackground(String phone, String templateName,
      Map<String, Object> env, RequestMobileCodeCallback callback) {
    requestSMSCodeInBackground(phone, templateName, env, false, callback);
  }

  private static void requestSMSCodeInBackground(String phone, String templateName,
      Map<String, Object> env, boolean sync, RequestMobileCodeCallback callback) {
    final RequestMobileCodeCallback internalCallback = callback;

    if (AVUtils.isBlankString(phone) || !AVUtils.checkMobilePhoneNumber(phone)) {
      callback.internalDone(new AVException(AVException.INVALID_PHONE_NUMBER,
          "Invalid Phone Number"));
    }

    if (env == null) {
      env = new HashMap<String, Object>();
    }
    env.put("mobilePhoneNumber", phone);
    if (!AVUtils.isBlankString(templateName)) {
      env.put("template", templateName);
    }
    String object = AVUtils.jsonStringFromMapWithNull(env);
    PaasClient.storageInstance().postObject("requestSmsCode", object, sync, false,
        new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            if (internalCallback != null) {
              internalCallback.internalDone(null, null);
            }
          }

          @Override
          public void onFailure(Throwable error, String content) {
            if (internalCallback != null) {
              internalCallback.internalDone(null, AVErrorUtils.createException(error, content));
            }
          }
        }, null, null);
  }

  /**
   * 请求发送短信验证码
   * 
   * 请在异步任务中调用本方法，或者调用requestSMSCodeInBackground(String phone)方法
   * 
   * 短信示范为: 您正在{应用名称}中进行短信验证，您的验证码是:{Code}，请输入完整验证，有效期为:10分钟
   * 
   * 
   * @param phone　目标手机号码
   * 
   * @throws AVException 请求异常
   */
  public static void requestSMSCode(String phone) throws AVException {
    requestSMSCode(phone, null, null, 0);
  }

  @Deprecated
  public static void requestSMSCodeInBackgroud(String phone, RequestMobileCodeCallback callback) {
    requestSMSCodeInBackgroud(phone, null, null, 0, callback);
  }

  /**
   * 请求发送短信验证码
   *
   * 短信示范为: 您正在{应用名称}中进行短信验证，您的验证码是:{Code}，请输入完整验证，有效期为:10分钟
   *
   *
   * @param phone　目标手机号码
   * @param callback 请求成功以后，会调用 callback.done(e)
   */
  public static void requestSMSCodeInBackground(String phone, RequestMobileCodeCallback callback) {
    requestSMSCodeInBackgroud(phone, null, null, 0, callback);
  }

  /**
   * 请求发送语音验证码，验证码会以电话形式打给目标手机
   *
   * @param phoneNumber 目标手机号
   * @throws AVException 请求异常
   */
  public static void requestVoiceCode(String phoneNumber) throws AVException {
    requestVoiceCode(phoneNumber, null);
  }

  /**
   * 请求发送语音验证码，验证码会以电话形式打给目标手机
   * 
   * @param phoneNumber 目标手机号
   * @param idd 电话的国家区号
   * @throws AVException 请求异常
   */

  public static void requestVoiceCode(String phoneNumber, String idd) throws AVException {
    requestSMSCodeInBackground(phoneNumber, null, getVoiceCodeEnv(idd), true,
        new RequestMobileCodeCallback() {
          @Override
          public void done(AVException e) {
            if (e != null) {
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
  }

  /**
   * 请求发送语音验证码，验证码会以电话形式打给目标手机
   * 
   * @param phoneNumber 目标手机号
   * @param callback 请求成功以后，会调用 callback.done(e)
   */
  public static void requestVoiceCodeInBackground(String phoneNumber,
      RequestMobileCodeCallback callback) {
    requestSMSCodeInBackground(phoneNumber, null, getVoiceCodeEnv(null), callback);
  }

  /**
   * 请求发送语音验证码，验证码会以电话形式打给目标手机
   * 
   * @param phoneNumber 目标手机号
   * @param idd 电话的国家区号
   * @param callback 请求成功以后，会调用 callback.done(e)
   */
  private static void requestVoiceCodeInBackground(String phoneNumber, String idd,
      RequestMobileCodeCallback callback) {
    requestSMSCodeInBackground(phoneNumber, null, getVoiceCodeEnv(idd), callback);
  }

  /**
   * 验证验证码
   * 
   * @param code 验证码
   * @param mobilePhoneNumber 手机号码
   * @throws AVException 请求异常
   */
  public static void verifySMSCode(String code, String mobilePhoneNumber) throws AVException {
    verifySMSCodeInBackground(code, mobilePhoneNumber, true, new AVMobilePhoneVerifyCallback() {
      @Override
      public void done(AVException e) {
        if (e != null) {
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
  }

  /**
   * 验证验证码
   *
   * @param code 验证码
   * @param mobilePhoneNumber 手机号码
   * @throws AVException 请求异常
   */
  public static void verifyCode(String code, String mobilePhoneNumber) throws AVException {
    verifySMSCode(code, mobilePhoneNumber);
  }

  /**
   * 验证验证码
   * 
   * @param code 验证码
   * @param mobilePhoneNumber 手机号
   * @param callback 请求成功以后，会调用 callback.done(e)
   */
  public static void verifySMSCodeInBackground(String code, String mobilePhoneNumber,
      AVMobilePhoneVerifyCallback callback) {
    verifySMSCodeInBackground(code, mobilePhoneNumber, false, callback);
  }

  /**
   * 验证验证码
   *
   * @param code 验证码
   * @param mobilePhoneNumber 手机号
   * @param callback 请求成功以后，会调用 callback.done(e)
   */
  public static void verifyCodeInBackground(String code, String mobilePhoneNumber,
      AVMobilePhoneVerifyCallback callback) {
    verifySMSCodeInBackground(code, mobilePhoneNumber, false, callback);
  }

  private static void verifySMSCodeInBackground(String code, String mobilePhoneNumber,
      boolean sync, AVMobilePhoneVerifyCallback callback) {
    final AVMobilePhoneVerifyCallback internalCallback = callback;

    if (AVUtils.isBlankString(code) || !AVUtils.checkMobileVerifyCode(code)) {
      callback
          .internalDone(new AVException(AVException.INVALID_PHONE_NUMBER, "Invalid Verify Code"));
    }
    String endpointer = String.format("verifySmsCode/%s", code);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("mobilePhoneNumber", mobilePhoneNumber);
    PaasClient.storageInstance().postObject(endpointer, AVUtils.restfulServerData(params), sync,
        false, new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            if (internalCallback != null) {
              internalCallback.internalDone(null, null);
            }
          }

          @Override
          public void onFailure(Throwable error, String content) {
            if (internalCallback != null) {
              internalCallback.internalDone(null, AVErrorUtils.createException(error, content));
            }
          }
        }, null, null);
  }
}
