package com.avos.avoscloud;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.os.Parcel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import com.avos.avoscloud.internal.InternalConfigurationController;

@JSONType(ignores = {"query", "password"}, asm = false)
public class AVUser extends AVObject {
  transient private static boolean enableAutomatic = false;
  private String sessionToken;
  transient private boolean isNew;
  private String username;
  transient private String password;
  private String mobilePhoneNumber;
  private String email;
  private transient String facebookToken;
  private transient String twitterToken;
  private transient String sinaWeiboToken;
  private transient String qqWeiboToken;
  private transient boolean needTransferFromAnonymousUser;
  private boolean anonymous;
  public static final String LOG_TAG = AVUser.class.getSimpleName();

  public static final String FOLLOWER_TAG = "follower";
  public static final String FOLLOWEE_TAG = "followee";
  public static final String SESSION_TOKEN_KEY = "sessionToken";
  private static Class<? extends AVUser> subClazz;

  // getter/setter for fastjson
  public String getFacebookToken() {
    return facebookToken;
  }

  void setFacebookToken(String facebookToken) {
    this.facebookToken = facebookToken;
  }

  public String getTwitterToken() {
    return twitterToken;
  }

  void setTwitterToken(String twitterToken) {
    this.twitterToken = twitterToken;
  }

  public String getQqWeiboToken() {
    return qqWeiboToken;
  }

  void setQqWeiboToken(String qqWeiboToken) {
    this.qqWeiboToken = qqWeiboToken;
  }

  String getPassword() {
    return password;
  }

  // end of getter/setter

  static void setEnableAutomatic(boolean enableAutomatic) {
    AVUser.enableAutomatic = enableAutomatic;
  }

  void setNew(boolean isNew) {
    this.isNew = isNew;
  }

  /**
   * Constructs a new AVUser with no data in it. A AVUser constructed in this way will not have an
   * objectId and will not persist to the database until AVUser.signUp() is called.
   */
  public AVUser() {
    super(userClassName());
  }

  public AVUser(Parcel in) {
    super(in);
  }

  @Override
  protected void rebuildInstanceData() {
    super.rebuildInstanceData();
    this.sessionToken = (String) get(SESSION_TOKEN_KEY);
    this.username = (String) get("username");
    this.processAuthData(null);
    this.email = (String) get("email");
    this.mobilePhoneNumber = (String) get("mobilePhoneNumber");
  }

  /**
   * Enables automatic creation of anonymous users. After calling this method,
   * AVUser.getCurrentUser() will always have a value. The user will only be created on the server
   * once the user has been saved, or once an object with a relation to that user or an ACL that
   * refers to the user has been saved. Note: saveEventually will not work if an item being saved
   * has a relation to an automatic user that has never been saved.
   */
  public static void enableAutomaticUser() {
    enableAutomatic = true;
  }

  public static boolean isEnableAutomatic() {
    return enableAutomatic;
  }

  public static void disableAutomaticUser() {
    enableAutomatic = false;
  }

  public static synchronized void changeCurrentUser(AVUser newUser, boolean save) {
    // clean password for security reason
    if (newUser != null) {
      newUser.password = null;
    }
    InternalConfigurationController.globalInstance().getInternalPersistence()
        .setCurrentUser(newUser, save);
  }

  /**
   * This retrieves the currently logged in AVUser with a valid session, either from memory or disk
   * if necessary.
   * 
   * @return The currently logged in AVUser
   */
  public static AVUser getCurrentUser() {
    return getCurrentUser(AVUser.class);
  }

  /**
   * This retrieves the currently logged in AVUser with a valid session, either from memory or disk
   * if necessary.
   * 
   * @param userClass subclass.
   * @param <T> subclass of AVUser or AVUser
   * @return The currently logged in AVUser subclass instance.
   */
  @SuppressWarnings("unchecked")
  public static <T extends AVUser> T getCurrentUser(Class<T> userClass) {
    T user =
        InternalConfigurationController.globalInstance().getInternalPersistence()
            .getCurrentUser(userClass);
    if (enableAutomatic && user == null) {
      user = newAVUser(userClass, null);
      AVUser.changeCurrentUser(user, false);;
    }
    return user;
  }

  static String userClassName() {
    return AVPowerfulUtils.getAVClassName(AVUser.class.getSimpleName());
  }

  void setNewFlag(boolean isNew) {
    this.isNew = isNew;
  }

  /**
   * Retrieves the email address.
   * 
   * @return user email
   */
  public String getEmail() {
    return this.email;
  }

  /**
   * Constructs a query for AVUsers subclasses.
   * 
   * @param clazz class type of AVUser subclass for query
   * @param <T> subclass of AVUser
   * @return query of AVUser
   */
  public static <T extends AVUser> AVQuery<T> getUserQuery(Class<T> clazz) {
    AVQuery<T> query = new AVQuery<T>(userClassName(), clazz);
    return query;
  }

  /**
   * Constructs a query for AVUsers.
   * 
   * @return AVQuery for AVUser
   */
  public static AVQuery<AVUser> getQuery() {
    return getQuery(AVUser.class);
  }

  public String getSessionToken() {
    return sessionToken;
  }

  void setSessionToken(String sessionToken) {
    this.sessionToken = sessionToken;
  }

  /**
   * Retrieves the username.
   * 
   * @return get username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Whether the AVUser has been authenticated on this device. This will be true if the AVUser was
   * obtained via a logIn or signUp method. Only an authenticated AVUser can be saved (with altered
   * attributes) and deleted.
   * 
   * @return Whether the AVUser has been authenticated
   */
  public boolean isAuthenticated() {
    return (!AVUtils.isBlankString(sessionToken) || !AVUtils.isBlankString(sinaWeiboToken) || !AVUtils
        .isBlankString(qqWeiboToken));
  }

  public boolean isAnonymous() {
    return this.anonymous;
  }

  protected void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }

  /**
   * <p>
   * Indicates whether this AVUser was created during this session through a call to AVUser.signUp()
   * or by logging in with a linked service such as Facebook.
   * </p>
   * 
   * @return Whether this AVUser is new created
   */
  public boolean isNew() {
    return isNew;
  }

  /**
   * @see #logIn(String, String, Class)
   * @param username user username
   * @param password user password
   * @return logined user
   * @throws AVException login exception
   */
  public static AVUser logIn(String username, String password) throws AVException {
    return logIn(username, password, AVUser.class);
  }

  /**
   * <p>
   * Logs in a user with a username and password. On success, this saves the session to disk, so you
   * can retrieve the currently logged in user using AVUser.getCurrentUser()
   * </p>
   * <p>
   * Typically, you should use AVUser.logInInBackground(java.lang.String, java.lang.String,
   * com.parse.LogInCallback,Class clazz) instead of this, unless you are managing your own
   * threading.
   * </p>
   * 
   * @param username The username to log in with.
   * @param password The password to log in with.
   * @param clazz The AVUser itself or subclass.
   * @param <T> The AVUser itself or subclass.
   * @return The user if the login was successful.
   * @throws AVException login exception
   */
  public static <T extends AVUser> T logIn(String username, String password, Class<T> clazz)
      throws AVException {
    final AVUser[] list = {null};

    logInInBackground(username, password, true, new LogInCallback<T>() {

      @Override
      public void done(T user, AVException e) {
        if (e != null) {
          AVExceptionHolder.add(e);
        } else {
          list[0] = user;
        }
      }

      @Override
      public boolean mustRunOnUIThread() {
        return false;
      }

    }, clazz);
    if (AVExceptionHolder.exists()) {
      throw AVExceptionHolder.remove();
    }
    return (T) list[0];
  }

  static private String logInPath() {
    return "login";
  }

  /**
   * @see #logInInBackground(String, String, LogInCallback, Class)
   * @param username username
   * @param password password
   * @param callback callback.done(user, e) is called when the login completes.
   */
  public static void logInInBackground(String username, String password,
      LogInCallback<AVUser> callback) {
    logInInBackground(username, password, callback, AVUser.class);
  }

  /**
   * <p>
   * Logs in a user with a username and password. On success, this saves the session to disk, so you
   * can retrieve the currently logged in user using AVUser.getCurrentUser()
   * </p>
   * <p>
   * This is preferable to using AVUser.logIn(java.lang.String, java.lang.String), unless your code
   * is already running from a background thread.
   * </p>
   * 
   * @param username The username to log in with.
   * @param password The password to log in with.
   * @param <T> The AVUser itself or subclass
   * @param clazz The AVUser itself or subclass.
   * @param callback callback.done(user, e) is called when the login completes.
   */
  public static <T extends AVUser> void logInInBackground(String username, String password,
      LogInCallback<T> callback, Class<T> clazz) {
    logInInBackground(username, password, false, callback, clazz);
  }

  static private Map<String, String> createUserMap(String username, String password, String email) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("username", username);
    if (AVUtils.isBlankString(username)) {
      throw new IllegalArgumentException("Blank username.");
    }
    if (!AVUtils.isBlankString(password)) {
      map.put("password", password);
    }
    if (!AVUtils.isBlankString(email)) {
      map.put("email", email);
    }
    return map;
  }

  static private Map<String, String> createUserMap(String username, String password, String email,
      String phoneNumber, String smsCode) {
    Map<String, String> map = new HashMap<String, String>();

    if (AVUtils.isBlankString(username) && AVUtils.isBlankString(phoneNumber)) {
      throw new IllegalArgumentException("Blank username and blank mobile phone number");
    }
    if (!AVUtils.isBlankString(username)) {
      map.put("username", username);
    }
    if (!AVUtils.isBlankString(password)) {
      map.put("password", password);
    }
    if (!AVUtils.isBlankString(email)) {
      map.put("email", email);
    }
    if (!AVUtils.isBlankString(phoneNumber)) {
      map.put("mobilePhoneNumber", phoneNumber);
    }
    if (!AVUtils.isBlankString(smsCode)) {
      map.put("smsCode", smsCode);
    }
    return map;
  }

  private static <T extends AVUser> void logInInBackground(String username, String password,
      boolean sync, LogInCallback<T> callback, Class<T> clazz) {
    Map<String, String> map = createUserMap(username, password, "");
    final LogInCallback<T> internalCallback = callback;
    final T user = newAVUser(clazz, callback);
    if (user == null) {
      return;
    }
    user.put("username", username, false);
    PaasClient.storageInstance().postObject(logInPath(), JSON.toJSONString(map), sync, false,
        new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            AVException error = e;
            T resultUser = user;
            if (!AVUtils.isBlankContent(content)) {
              AVUtils.copyPropertiesFromJsonStringToAVObject(content, user);
              user.processAuthData(null);
              AVUser.changeCurrentUser(user, true);
            } else {
              resultUser = null;
              error = new AVException(AVException.OBJECT_NOT_FOUND, "User is not found.");
            }
            if (internalCallback != null) {
              internalCallback.internalDone(resultUser, error);
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

  public static AVUser loginByMobilePhoneNumber(String phone, String password) throws AVException {
    return loginByMobilePhoneNumber(phone, password, AVUser.class);
  }

  public static <T extends AVUser> T loginByMobilePhoneNumber(String phone, String password,
      Class<T> clazz) throws AVException {

    final AVUser[] list = {null};

    loginByMobilePhoneNumberInBackground(phone, password, true, new LogInCallback<T>() {

      @Override
      public void done(T user, AVException e) {
        if (e != null) {
          AVExceptionHolder.add(e);
        } else {
          list[0] = user;
        }
      }

      @Override
      public boolean mustRunOnUIThread() {
        return false;
      }
    }, clazz);
    if (AVExceptionHolder.exists()) {
      throw AVExceptionHolder.remove();
    }
    return (T) list[0];
  }

  public static void loginByMobilePhoneNumberInBackground(String phone, String password,
      LogInCallback<AVUser> callback) {
    loginByMobilePhoneNumberInBackground(phone, password, false, callback, AVUser.class);
  }

  public static <T extends AVUser> void loginByMobilePhoneNumberInBackground(String phone,
      String password, LogInCallback<T> callback, Class<T> clazz) {
    loginByMobilePhoneNumberInBackground(phone, password, false, callback, clazz);
  }

  private static <T extends AVUser> void loginByMobilePhoneNumberInBackground(String phone,
      String password, boolean sync, LogInCallback<T> callback, Class<T> clazz) {
    Map<String, String> map = createUserMap(null, password, null, phone, null);
    final LogInCallback<T> internalCallback = callback;
    final T user = newAVUser(clazz, callback);
    if (user == null) {
      return;
    }
    user.setMobilePhoneNumber(phone);
    PaasClient.storageInstance().postObject(logInPath(), JSON.toJSONString(map), sync, false,
        new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            AVException error = e;
            T resultUser = user;
            if (!AVUtils.isBlankContent(content)) {
              AVUtils.copyPropertiesFromJsonStringToAVObject(content, user);
              AVUser.changeCurrentUser(user, true);
            } else {
              resultUser = null;
              error = new AVException(AVException.OBJECT_NOT_FOUND, "User is not found.");
            }
            if (internalCallback != null) {
              internalCallback.internalDone(resultUser, error);
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
   * 通过短信验证码和手机号码来登录用户
   * 
   * 请不要在UI线程内调用本方法
   * 
   * @param phone 收到验证码的手机号码
   * @param smsCode 收到的验证码
   * @return 登录用户
   * @throws AVException 登录异常
   */
  public static AVUser loginBySMSCode(String phone, String smsCode) throws AVException {
    return loginBySMSCode(phone, smsCode, AVUser.class);
  }

  /**
   * 通过短信验证码和手机号码来登录用户
   * 
   * 请不要在UI线程内调用本方法
   * 
   * @param phone 收到验证码的手机号码
   * @param smsCode 收到的验证码
   * @param clazz AVUser的子类对象
   * @param <T> AVUser的子类对象
   * @return 登录用户
   * @throws AVException 登录异常
   */
  public static <T extends AVUser> T loginBySMSCode(String phone, String smsCode, Class<T> clazz)
      throws AVException {
    final AVUser[] list = {null};
    loginBySMSCodeInBackground(phone, smsCode, true, new LogInCallback<T>() {

      @Override
      public void done(T user, AVException e) {
        if (e != null) {
          AVExceptionHolder.add(e);
        } else {
          list[0] = user;
        }
      }

      @Override
      public boolean mustRunOnUIThread() {
        return false;
      }
    }, clazz);
    if (AVExceptionHolder.exists()) {
      throw AVExceptionHolder.remove();
    }
    return (T) list[0];
  }

  /**
   * 通过短信验证码和手机号码来登录用户
   * 
   * 本方法为异步方法，可以在UI线程中调用
   * 
   * @param phone 收到验证码的手机号码
   * @param smsCode 收到的验证码
   * @param callback 在登录完成后，callback.done(user,e)会被调用
   */
  public static void loginBySMSCodeInBackground(String phone, String smsCode,
      LogInCallback<AVUser> callback) {
    loginBySMSCodeInBackground(phone, smsCode, false, callback, AVUser.class);
  }

  /**
   * 通过短信验证码和手机号码来登录用户
   * 
   * 本方法为异步方法，可以在UI线程中调用
   * 
   * @param phone 收到验证码的手机号码
   * @param smsCode 收到的验证码
   * @param <T> AVUser的子类
   * @param callback 在登录完成后，callback.done(user,e)会被调用
   * @param clazz AVUser的子类
   */
  public static <T extends AVUser> void loginBySMSCodeInBackground(String phone, String smsCode,
      LogInCallback<T> callback, Class<T> clazz) {
    loginBySMSCodeInBackground(phone, smsCode, false, callback, clazz);
  }

  private static <T extends AVUser> void loginBySMSCodeInBackground(String phone, String smsCode,
      boolean sync, LogInCallback<T> callback, Class<T> clazz) {
    Map<String, String> map = createUserMap(null, null, "", phone, smsCode);
    final LogInCallback<T> internalCallback = callback;
    final T user = newAVUser(clazz, callback);
    if (user == null) {
      return;
    }
    user.setMobilePhoneNumber(phone);
    PaasClient.storageInstance().postObject(logInPath(), JSON.toJSONString(map), sync, false,
        new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            AVException error = e;
            T resultUser = user;
            if (!AVUtils.isBlankContent(content)) {
              AVUtils.copyPropertiesFromJsonStringToAVObject(content, user);
              AVUser.changeCurrentUser(user, true);
            } else {
              resultUser = null;
              error = new AVException(AVException.OBJECT_NOT_FOUND, "User is not found.");
            }
            if (internalCallback != null) {
              internalCallback.internalDone(resultUser, error);
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
   * Logs in a user with a session token. this saves the session to disk, so you can retrieve the
   * currently logged in user using AVUser.getCurrentUser(). Don't call this method on the UI thread
   * 
   * @param sessionToken The sessionToken to log in with
   * @return logined user
   * @throws AVException login exception
   */
  public static AVUser becomeWithSessionToken(String sessionToken) throws AVException {
    return becomeWithSessionToken(sessionToken, AVUser.class);
  }

  /**
   * Logs in a user with a session token. this saves the session to disk, so you can retrieve the
   * currently logged in user using AVUser.getCurrentUser(). Don't call this method on the UI thread
   * 
   * @param sessionToken The sessionToken to log in with
   * @param clazz The AVUser itself or subclass.
   * @param <T> The AVUser itself or subclass.
   * @return logined user
   * 
   * @throws AVException login exception
   */
  public static <T extends AVUser> AVUser becomeWithSessionToken(String sessionToken, Class<T> clazz)
      throws AVException {
    final AVUser[] list = {null};

    becomeWithSessionTokenInBackground(sessionToken, true, new LogInCallback<T>() {
      @Override
      public void done(T user, AVException e) {
        if (e != null) {
          AVExceptionHolder.add(e);
        } else {
          list[0] = user;
        }
      }

      @Override
      public boolean mustRunOnUIThread() {
        return false;
      }
    }, clazz);

    if (AVExceptionHolder.exists()) {
      throw AVExceptionHolder.remove();
    }
    return (T) list[0];
  }

  /**
   * Logs in a user with a session token. this saves the session to disk, so you can retrieve the
   * currently logged in user using AVUser.getCurrentUser().
   * 
   * @param sessionToken The sessionToken to log in with
   * @param callback callback.done(user,e) will be called when login completes
   */
  public static void becomeWithSessionTokenInBackground(String sessionToken,
      LogInCallback<AVUser> callback) {
    becomeWithSessionTokenInBackground(sessionToken, callback, AVUser.class);
  }

  /**
   * Logs in a user with a session token. this saves the session to disk, so you can retrieve the
   * currently logged in user using AVUser.getCurrentUser().
   * 
   * @param sessionToken The sessionToken to log in with
   * @param callback callback.done(user,e) will be called when login completes
   * @param clazz subclass of AVUser
   * @param <T> subclass of AVUser
   */
  public static <T extends AVUser> void becomeWithSessionTokenInBackground(String sessionToken,
      LogInCallback<T> callback, Class<T> clazz) {
    becomeWithSessionTokenInBackground(sessionToken, false, callback, clazz);
  }

  private static <T extends AVUser> void becomeWithSessionTokenInBackground(String sessionToken,
      boolean sync, LogInCallback<T> callback, Class<T> clazz) {
    final LogInCallback<T> internalCallback = callback;
    final T user = newAVUser(clazz, callback);
    if (user == null) {
      return;
    }

    AVRequestParams params = new AVRequestParams();
    params.put("session_token", sessionToken);

    PaasClient.storageInstance().getObject("users/me", params, sync, null,
        new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            AVException error = e;
            T resultUser = user;
            if (!AVUtils.isBlankContent(content)) {
              AVUtils.copyPropertiesFromJsonStringToAVObject(content, user);
              user.processAuthData(null);
              AVUser.changeCurrentUser(user, true);
            } else {
              resultUser = null;
              error = new AVException(AVException.OBJECT_NOT_FOUND, "User is not found.");
            }
            if (internalCallback != null) {
              internalCallback.internalDone(resultUser, error);
            }
          }

          @Override
          public void onFailure(Throwable error, String content) {
            if (internalCallback != null) {
              internalCallback.internalDone(null, AVErrorUtils.createException(error, content));
            }
          }
        });
  }

  /**
   * 直接通过手机号码和验证码来创建或者登录用户。 如果手机号码已经存在则为登录，否则创建新用户
   * 
   * 请不要在UI线程中间调用此方法
   * 
   * @param mobilePhoneNumber 用户注册的手机号码
   * @param smsCode 收到的登录验证码
   * @return 登录用户
   * @throws AVException 登录异常
   * @since 2.6.10
   */
  public static AVUser signUpOrLoginByMobilePhone(String mobilePhoneNumber, String smsCode)
      throws AVException {
    return signUpOrLoginByMobilePhone(mobilePhoneNumber, smsCode, AVUser.class);
  }

  /**
   * 直接通过手机号码和验证码来创建或者登录用户。 如果手机号码已经存在则为登录，否则创建新用户
   * 
   * 请不要在UI线程中间调用此方法
   * 
   * @param mobilePhoneNumber 用户注册的手机号码
   * @param smsCode 收到的登录验证码
   * @param clazz AVUser的子类
   * @param <T> AVUser的子类
   * @return 登录用户
   * @throws AVException 登录异常
   * @since 2.6.10
   */
  public static <T extends AVUser> T signUpOrLoginByMobilePhone(String mobilePhoneNumber,
      String smsCode, Class<T> clazz) throws AVException {
    final AVUser[] list = {null};
    signUpOrLoginByMobilePhoneInBackground(mobilePhoneNumber, smsCode, true, clazz,
        new LogInCallback<T>() {

          @Override
          public void done(T user, AVException e) {
            if (e != null) {
              AVExceptionHolder.add(e);
            } else {
              list[0] = user;
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
    return (T) list[0];
  }

  /**
   * 直接通过手机号码和验证码来创建或者登录用户。 如果手机号码已经存在则为登录，否则创建新用户
   * 
   * 
   * @param mobilePhoneNumber 用户注册的手机号码
   * @param smsCode 收到的登录验证码
   * @param callback 登录或者注册成功以后,callback.done(user,e)会被调用
   * @since 2.6.10
   */
  public static void signUpOrLoginByMobilePhoneInBackground(String mobilePhoneNumber,
      String smsCode, LogInCallback<AVUser> callback) {
    signUpOrLoginByMobilePhoneInBackground(mobilePhoneNumber, smsCode, AVUser.class, callback);
  }

  /**
   * 直接通过手机号码和验证码来创建或者登录用户。 如果手机号码已经存在则为登录，否则创建新用户
   * 
   * @param mobilePhoneNumber 用户注册的手机号码
   * @param smsCode 收到的登录验证码
   * @param clazz AVUser的子类对象
   * @param <T> subclass of AVUser
   * @param callback 登录或者注册成功以后,callback.done(user,e)会被调用
   * @since 2.6.10
   */

  public static <T extends AVUser> void signUpOrLoginByMobilePhoneInBackground(
      String mobilePhoneNumber, String smsCode, Class<T> clazz, LogInCallback<T> callback) {
    signUpOrLoginByMobilePhoneInBackground(mobilePhoneNumber, smsCode, false, clazz, callback);
  }

  private static <T extends AVUser> void signUpOrLoginByMobilePhoneInBackground(
      String mobilePhoneNumber, String smsCode, boolean sync, Class<T> clazz,
      LogInCallback<T> callback) {
    if (AVUtils.isBlankString(smsCode)) {
      if (callback != null) {
        callback.internalDone(null, new AVException(AVException.OTHER_CAUSE,
            "SMS Code can't be empty"));
      } else {
        LogUtil.avlog.e("SMS Code can't be empty");
      }
      return;
    }
    Map<String, String> map = createUserMap(null, null, "", mobilePhoneNumber, smsCode);
    final LogInCallback<T> internalCallback = callback;
    final T user = newAVUser(clazz, callback);
    if (user == null) {
      return;
    }
    user.setMobilePhoneNumber(mobilePhoneNumber);
    PaasClient.storageInstance().postObject("usersByMobilePhone", JSON.toJSONString(map), sync,
        false, new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            AVException error = e;
            T resultUser = user;
            if (!AVUtils.isBlankContent(content)) {
              AVUtils.copyPropertiesFromJsonStringToAVObject(content, user);
              AVUser.changeCurrentUser(user, true);
            } else {
              resultUser = null;
              error = new AVException(AVException.OBJECT_NOT_FOUND, "User is not found.");
            }
            if (internalCallback != null) {
              internalCallback.internalDone(resultUser, error);
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

  public static <T extends AVUser> T newAVUser(Class<T> clazz, LogInCallback<T> cb) {
    try {
      final T user = clazz.newInstance();
      return user;
    } catch (Exception e) {
      if (cb != null) {
        cb.internalDone(null, AVErrorUtils.createException(e, null));
      } else {
        throw new AVRuntimeException("Create user instance failed.", e);
      }
    }
    return null;
  }

  protected static <T extends AVUser> T newAVUser() {
    return (T) newAVUser(subClazz == null ? AVUser.class : subClazz, null);
  }

  /**
   * Logs out the currently logged in user session. This will remove the session from disk, log out
   * of linked services, and future calls to AVUser.getCurrentUser() will return null.
   */
  public static void logOut() {
    AVUser.changeCurrentUser(null, true);
    PaasClient.storageInstance().setDefaultACL(null);
  }

  /**
   * Add a key-value pair to this object. It is recommended to name keys in
   * partialCamelCaseLikeThis.
   * 
   * @param key Keys must be alphanumerical plus underscore, and start with a letter.
   * @param value Values may be numerical, String, JSONObject, JSONArray, JSONObject.NULL, or other
   *        AVObjects.
   */
  @Override
  public void put(String key, Object value) {
    super.put(key, value);
  }

  /**
   * Removes a key from this object's data if it exists.
   * 
   * @param key The key to remove.
   */
  @Override
  public void remove(String key) {
    super.remove(key);
  }

  /**
   * <p>
   * Requests a password reset email to be sent to the specified email address associated with the
   * user account. This email allows the user to securely reset their password on the AVOSCloud
   * site.
   * </p>
   * <p>
   * Typically, you should use AVUser.requestPasswordResetInBackground(java.lang.String,
   * com.parse.RequestPasswordResetCallback) instead of this, unless you are managing your own
   * threading.
   * </p>
   * 
   * @param email The email address associated with the user that forgot their password.
   */
  public static void requestPasswordReset(String email) {
    requestPasswordResetInBackground(email, true, null);
  }

  /**
   * <p>
   * Requests a password reset email to be sent in a background thread to the specified email
   * address associated with the user account. This email allows the user to securely reset their
   * password on the AVOSCloud site.
   * </p>
   * <p>
   * This is preferable to using requestPasswordReset(), unless your code is already running from a
   * background thread.
   * </p>
   * 
   * @param email The email address associated with the user that forgot their password.
   * @param callback callback.done(e) is called when the request completes.
   */
  public static void requestPasswordResetInBackground(String email,
      RequestPasswordResetCallback callback) {
    requestPasswordResetInBackground(email, false, callback);
  }

  private static void requestPasswordResetInBackground(String email, boolean sync,
      RequestPasswordResetCallback callback) {
    final RequestPasswordResetCallback internalCallback = callback;
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("email", email);
    String object = AVUtils.jsonStringFromMapWithNull(map);
    PaasClient.storageInstance().postObject("requestPasswordReset", object, sync, false,
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
   * 同步方法调用修改用户当前的密码
   * 
   * 您需要保证用户有效的登录状态
   * 
   * @param oldPassword 原来的密码
   * @param newPassword 新密码
   * @throws AVException updatePassword exception
   */
  public void updatePassword(String oldPassword, String newPassword) throws AVException {
    updatePasswordInBackground(oldPassword, newPassword, new UpdatePasswordCallback() {

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
    }, true);
    if (AVExceptionHolder.exists()) {
      throw AVExceptionHolder.remove();
    }
  }

  /**
   * 异步方法调用修改用户当前的密码
   * 
   * 您需要保证用户有效的登录状态
   * 
   * @param oldPassword 原来的密码
   * @param newPassword 新密码
   * @param callback 密码更新以后 callback.done(e)会被调用
   */
  public void updatePasswordInBackground(String oldPassword, String newPassword,
      UpdatePasswordCallback callback) {
    updatePasswordInBackground(oldPassword, newPassword, callback, false);
  }

  private void updatePasswordInBackground(String oldPassword, String newPassword,
      final UpdatePasswordCallback callback, boolean sync) {
    if (!this.isAuthenticated() || AVUtils.isBlankString(getObjectId())) {
      callback.internalDone(AVErrorUtils.sessionMissingException());
    } else {
      String relativePath = String.format("users/%s/updatePassword", this.getObjectId());
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("old_password", oldPassword);
      params.put("new_password", newPassword);
      String paramsString = AVUtils.restfulServerData(params);
      PaasClient.storageInstance().putObject(relativePath, paramsString, sync, headerMap(),
          new GenericObjectCallback() {
            @Override
            public void onSuccess(String content, AVException e) {
              if (null == e && !AVUtils.isBlankString(content)) {
                sessionToken = AVUtils.getJSONValue(content, SESSION_TOKEN_KEY);
              }
              callback.internalDone(e);
            }

            @Override
            public void onFailure(Throwable error, String content) {
              callback.internalDone(AVErrorUtils.createException(error, content));
            }
          }, getObjectId(), getObjectId());
    }
  }

  /**
   * 申请通过短信重置用户密码
   * 
   * 请确保是在异步程序中调用此方法，否则请调用 requestPasswordResetBySmsCodeInBackground(String
   * mobilePhoneNumber,RequestMobileCodeCallback callback)方法
   * 
   * @param mobilePhoneNumber 用户注册时的手机号码
   * @throws AVException 如果找不到手机号码对应的用户时抛出的异常
   */

  public static void requestPasswordResetBySmsCode(String mobilePhoneNumber) throws AVException {
    requestPasswordResetBySmsCodeInBackground(mobilePhoneNumber, true,
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
   * 申请通过短信重置用户密码
   * 
   * @param mobilePhoneNumber 用户注册时的手机号码
   * @param callback 密码重置成功以后会调用 callback.done(e)
   * 
   */
  public static void requestPasswordResetBySmsCodeInBackground(String mobilePhoneNumber,
      RequestMobileCodeCallback callback) {
    requestPasswordResetBySmsCodeInBackground(mobilePhoneNumber, false, callback);
  }

  protected static void requestPasswordResetBySmsCodeInBackground(String mobilePhoneNumber,
      boolean sync, RequestMobileCodeCallback callback) {
    final RequestMobileCodeCallback internalCallback = callback;

    if (AVUtils.isBlankString(mobilePhoneNumber)
        || !AVUtils.checkMobilePhoneNumber(mobilePhoneNumber)) {
      callback.internalDone(new AVException(AVException.INVALID_PHONE_NUMBER,
          "Invalid Phone Number"));
      return;
    }

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("mobilePhoneNumber", mobilePhoneNumber);
    String object = AVUtils.jsonStringFromMapWithNull(map);
    PaasClient.storageInstance().postObject("requestPasswordResetBySmsCode", object, sync, false,
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
   * 通过短信验证码更新用户密码
   * 
   * 请确保是在异步方法中调用本方法否则请调用resetPasswordBySmsCodeInBackground(String smsCode, String newPassword,
   * UpdatePasswordCallback callback) 方法
   * 
   * @param smsCode 验证码
   * @param newPassword 新密码
   * @throws AVException 验证码错误异常
   */
  public static void resetPasswordBySmsCode(String smsCode, String newPassword) throws AVException {
    resetPasswordBySmsCodeInBackground(smsCode, newPassword, true, new UpdatePasswordCallback() {
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
   * 通过短信验证码更新用户密码
   * 
   * @param smsCode 验证码
   * @param newPassword 新密码
   * @param callback 密码重置成功以后会调用 callback.done(e)
   */
  public static void resetPasswordBySmsCodeInBackground(String smsCode, String newPassword,
      UpdatePasswordCallback callback) {
    resetPasswordBySmsCodeInBackground(smsCode, newPassword, false, callback);
  }

  protected static void resetPasswordBySmsCodeInBackground(String smsCode, String newPassword,
      boolean sync, UpdatePasswordCallback callback) {
    final UpdatePasswordCallback internalCallback = callback;

    if (AVUtils.isBlankString(smsCode) || !AVUtils.checkMobileVerifyCode(smsCode)) {
      callback
          .internalDone(new AVException(AVException.INVALID_PHONE_NUMBER, "Invalid Verify Code"));
      return;
    }

    String endpointer = String.format("resetPasswordBySmsCode/%s", smsCode);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("password", newPassword);
    PaasClient.storageInstance().putObject(endpointer, AVUtils.restfulServerData(params), sync,
        null, new GenericObjectCallback() {
          @Override
          public void onFailure(Throwable error, String content) {
            if (internalCallback != null) {
              internalCallback.internalDone(new AVException(content, error));
            }
          }

          @Override
          public void onSuccess(String content, AVException e) {
            internalCallback.internalDone(e);
          }
        }, null, null);
  }

  /**
   * <p>
   * 调用这个方法会给用户的邮箱发送一封验证邮件，让用户能够确认在AVOS Cloud网站上注册的账号邮箱
   * </p>
   * <p>
   * 除非是在一个后台线程中调用这个方法， 否则，一般情况下，请使用AVUser.requestEmailVerifyInBackground(email,callback)方法进行调用
   * 
   * </p>
   * 
   * @param email The email address associated with the user that forgot their password.
   */
  public static void requestEmailVerify(String email) {
    requestEmailVerifyInBackground(email, true, null);
  }

  /**
   * <p>
   * 调用这个方法会给用户的邮箱发送一封验证邮件，让用户能够确认在AVOS Cloud网站上注册的账号邮箱
   * </p>
   * <p>
   * 除非这个方法在一个后台线程中被调用，请勿使用 requestEmailVerify()
   * </p>
   * 
   * @param email The email address associated with the user that forgot their password.
   * @param callback callback.done(e) is called when the request completes.
   */
  public static void requestEmailVerifyInBackground(String email,
                                                    RequestEmailVerifyCallback callback) {
    requestEmailVerifyInBackground(email, false, callback);
  }

  /**
   * @deprecated Use {@link #requestEmailVerifyInBackground(String, RequestEmailVerifyCallback)} instead.
   */
  @Deprecated
  public static void requestEmailVerfiyInBackground(String email,
      RequestEmailVerifyCallback callback) {
    requestEmailVerifyInBackground(email, false, callback);
  }

  private static void requestEmailVerifyInBackground(String email, boolean sync,
      RequestEmailVerifyCallback callback) {
    final RequestEmailVerifyCallback internalCallback = callback;
    if (AVUtils.isBlankString(email) || !AVUtils.checkEmailAddress(email)) {
      callback.internalDone(new AVException(AVException.INVALID_EMAIL_ADDRESS, "Invalid Email"));
      return;
    }
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("email", email);
    String object = AVUtils.jsonStringFromMapWithNull(map);
    PaasClient.storageInstance().postObject("requestEmailVerify", object, sync, false,
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
   * 调用这个方法来请求用户的手机号码验证
   * 
   * 在发送这条请求前，请保证您已经成功保存用户的手机号码，并且在控制中心打开了“验证注册用户手机号码”选项
   * 
   * 本方法请在异步方法中调用
   * 
   * @param mobilePhoneNumber 手机号码
   * @throws AVException 请求异常
   */

  public static void requestMobilePhoneVerify(String mobilePhoneNumber) throws AVException {
    requestMobilePhoneVerifyInBackground(mobilePhoneNumber, true, new RequestMobileCodeCallback() {
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
   * 调用这个方法来请求用户的手机号码验证
   * 
   * 在发送这条请求前，请保证您已经成功保存用户的手机号码，并且在控制中心打开了“验证注册用户手机号码”选项
   * 
   * 
   * @param mobilePhoneNumber 手机号码
   * @param callback 请求成功以后会调用callback.done(e)
   */
  @Deprecated
  public static void requestMobilePhoneVerifyInBackgroud(String mobilePhoneNumber,
      RequestMobileCodeCallback callback) {
    requestMobilePhoneVerifyInBackground(mobilePhoneNumber, false, callback);
  }

  /**
   * 调用这个方法来请求用户的手机号码验证
   * 
   * 在发送这条请求前，请保证您已经成功保存用户的手机号码，并且在控制中心打开了“验证注册用户手机号码”选项
   * 
   * 
   * @param mobilePhoneNumber 手机号码
   * @param callback 请求成功以后会调用callback.done(e)
   */
  public static void requestMobilePhoneVerifyInBackground(String mobilePhoneNumber,
      RequestMobileCodeCallback callback) {
    requestMobilePhoneVerifyInBackground(mobilePhoneNumber, false, callback);
  }

  private static void requestMobilePhoneVerifyInBackground(String mobilePhoneNumber, boolean sync,
      RequestMobileCodeCallback callback) {
    final RequestMobileCodeCallback internalCallback = callback;

    if (AVUtils.isBlankString(mobilePhoneNumber)
        || !AVUtils.checkMobilePhoneNumber(mobilePhoneNumber)) {
      callback.internalDone(new AVException(AVException.INVALID_PHONE_NUMBER,
          "Invalid Phone Number"));
      return;
    }

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("mobilePhoneNumber", mobilePhoneNumber);
    String object = AVUtils.jsonStringFromMapWithNull(map);
    PaasClient.storageInstance().postObject("requestMobilePhoneVerify", object, sync, false,
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
   * 请求登录验证码
   * 
   * 请在异步任务中调用本方法，或者请使用requestLoginSmsCodeInBackground
   * 
   * @param mobilePhoneNumber 手机号码
   * @throws AVException 请求异常
   */
  public static void requestLoginSmsCode(String mobilePhoneNumber) throws AVException {
    requestLoginSmsCodeInBackground(mobilePhoneNumber, true, new RequestMobileCodeCallback() {
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

  public static void requestLoginSmsCodeInBackground(String mobilePhoneNumber,
      RequestMobileCodeCallback callback) {
    requestLoginSmsCodeInBackground(mobilePhoneNumber, false, callback);
  }

  private static void requestLoginSmsCodeInBackground(String mobilePhoneNumber, boolean sync,
      RequestMobileCodeCallback callback) {
    final RequestMobileCodeCallback internalCallback = callback;

    if (AVUtils.isBlankString(mobilePhoneNumber)
        || !AVUtils.checkMobilePhoneNumber(mobilePhoneNumber)) {
      callback.internalDone(new AVException(AVException.INVALID_PHONE_NUMBER,
          "Invalid Phone Number"));
      return;
    }

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("mobilePhoneNumber", mobilePhoneNumber);
    String object = AVUtils.jsonStringFromMapWithNull(map);
    PaasClient.storageInstance().postObject("requestLoginSmsCode", object, sync, false,
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
   * 验证手机收到的验证码
   * 
   * 请在异步方法中调用此方法，或者您可以调用verifyMobilePhoneInBackground方法
   * 
   * @param verifyCode 验证码
   * @throws AVException 请求异常
   */
  public static void verifyMobilePhone(String verifyCode) throws AVException {
    verifyMobilePhoneInBackground(true, verifyCode, new AVMobilePhoneVerifyCallback() {
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
   * 验证手机收到的验证码
   * 
   * 
   * @param verifyCode 验证码
   * @param callback 请求成功以后会调用 callback.done(e)
   */
  @Deprecated
  public static void verifyMobilePhoneInBackgroud(String verifyCode,
      AVMobilePhoneVerifyCallback callback) {
    verifyMobilePhoneInBackground(false, verifyCode, callback);
  }

  /**
   * 验证手机收到的验证码
   * 
   * 
   * @param verifyCode 验证码
   * @param callback 请求成功以后会调用 callback.done(e)
   */
  public static void verifyMobilePhoneInBackground(String verifyCode,
      AVMobilePhoneVerifyCallback callback) {
    verifyMobilePhoneInBackground(false, verifyCode, callback);
  }

  private static void verifyMobilePhoneInBackground(boolean sync, String verifyCode,
      AVMobilePhoneVerifyCallback callback) {
    final AVMobilePhoneVerifyCallback internalCallback = callback;

    if (AVUtils.isBlankString(verifyCode) || !AVUtils.checkMobileVerifyCode(verifyCode)) {
      callback
          .internalDone(new AVException(AVException.INVALID_PHONE_NUMBER, "Invalid Verify Code"));
      return;
    }

    String endpointer = String.format("verifyMobilePhone/%s", verifyCode);
    PaasClient.storageInstance().postObject(endpointer, AVUtils.restfulServerData(null), sync,
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



  /**
   * Sets the email address.
   * 
   * @param email The email address to set.
   */
  public void setEmail(String email) {
    this.email = email;
    this.put("email", email);
  }

  /**
   * Sets the password.
   * 
   * @param password The password to set.
   */
  public void setPassword(String password) {
    this.password = password;
    this.put("password", password);
    markAnonymousUserTransfer();
  }

  /**
   * Sets the username. Usernames cannot be null or blank.
   * 
   * @param username The username to set.
   */
  public void setUsername(String username) {
    this.username = username;
    this.put("username", username);
    markAnonymousUserTransfer();
  }

  public String getMobilePhoneNumber() {
    return mobilePhoneNumber;
  }

  public void setMobilePhoneNumber(String mobilePhoneNumber) {
    this.mobilePhoneNumber = mobilePhoneNumber;
    this.put("mobilePhoneNumber", mobilePhoneNumber);
  }

  public boolean isMobilePhoneVerified() {
    return this.getBoolean("mobilePhoneVerified");
  }

  void setMobilePhoneVerified(boolean mobilePhoneVerified) {
    this.put("mobileVerified", mobilePhoneVerified);
  }

  private String signUpPath() {
    return "users";
  }

  private void signUp(boolean sync, final SignUpCallback callback) {
    if (sync) {
      try {
        this.save();
        if (callback != null)
          callback.internalDone(null);
      } catch (AVException e) {
        if (callback != null)
          callback.internalDone(e);
      }

    } else {
      this.saveInBackground(new SaveCallback() {

        @Override
        public void done(AVException e) {
          if (callback != null)
            callback.internalDone(e);
        }
      });
    }
  }


  /**
   * <p>
   * Signs up a new user. You should call this instead of AVObject.save() for new AVUsers. This will
   * create a new AVUser on the server, and also persist the session on disk so that you can access
   * the user using AVUser.getCurrentUser().
   * </p>
   * <p>
   * A username and password must be set before calling signUp.
   * </p>
   * <p>
   * Typically, you should use AVUser.signUpInBackground(com.parse.SignUpCallback) instead of this,
   * unless you are managing your own threading.
   * </p>
   * 
   * @throws AVException 注册请求异常
   */
  public void signUp() throws AVException {
    signUp(true, new SignUpCallback() {

      @Override
      public void done(AVException e) {
        if (e != null) {
          AVExceptionHolder.add(e);
        }
      }

      @Override
      protected boolean mustRunOnUIThread() {
        return false;
      }
    });
    if (AVExceptionHolder.exists()) {
      throw AVExceptionHolder.remove();
    }
  }

  /**
   * <p>
   * Signs up a new user. You should call this instead of AVObject.save() for new AVUsers. This will
   * create a new AVUser on the server, and also persist the session on disk so that you can access
   * the user using AVUser.getCurrentUser().
   * </p>
   * <p>
   * A username and password must be set before calling signUp.
   * </p>
   * <p>
   * This is preferable to using AVUser.signUp(), unless your code is already running from a
   * background thread.
   * </p>
   * 
   * @param callback callback.done(user, e) is called when the signUp completes.
   */
  public void signUpInBackground(SignUpCallback callback) {
    signUp(false, callback);
  }

  void setSinaWeiboToken(String token) {
    this.sinaWeiboToken = token;
  }

  public String getSinaWeiboToken() {
    return sinaWeiboToken;
  }

  void setQQWeiboToken(String token) {
    qqWeiboToken = token;
  }

  public String getQQWeiboToken() {
    return qqWeiboToken;
  }

  @Override
  protected void onSaveSuccess() {
    super.onSaveSuccess();
    this.processAuthData(null);
    if (!AVUtils.isBlankString(sessionToken)) {
      changeCurrentUser(this, true);
    }
  }

  @Override
  protected void onDataSynchronized() {
    processAuthData(null);
    if (!AVUtils.isBlankString(sessionToken)) {
      changeCurrentUser(this, true);
    }
  }

  @Override
  protected Map<String, String> headerMap() {
    Map<String, String> map = new HashMap<String, String>();
    if (!AVUtils.isBlankString(sessionToken)) {
      map.put(PaasClient.sessionTokenField, sessionToken);
    }
    return map;
  }

  static AVUser userFromSinaWeibo(String weiboToken, String userName) {
    AVUser user = newAVUser();
    user.sinaWeiboToken = weiboToken;
    user.username = userName;
    return user;
  }

  static AVUser userFromQQWeibo(String weiboToken, String userName) {
    AVUser user = newAVUser();
    user.qqWeiboToken = weiboToken;
    user.username = userName;
    return user;
  }

  private boolean checkUserAuthentication(final AVCallback callback) {
    if (!this.isAuthenticated() || AVUtils.isBlankString(getObjectId())) {
      if (callback != null) {
        callback.internalDone(AVErrorUtils.createException(AVException.SESSION_MISSING,
            "No valid session token, make sure signUp or login has been called."));
      }
      return false;
    }
    return true;
  }

  /**
   * <p>
   * Follow the user specified by userObjectId. This will create a follow relation between this user
   * and the user specified by the userObjectId.
   * </p>
   * 
   * @param userObjectId The user objectId.
   * @param callback callback.done(user, e) is called when the follow completes.
   * @since 2.1.3
   */
  public void followInBackground(String userObjectId, final FollowCallback callback) {
    this.followInBackground(userObjectId, null, callback);
  }

  public AVUser follow(String userObjectId) throws AVException {
    return this.follow(userObjectId, null);
  }

  public AVUser follow(String userObjectId, Map<String, Object> attributes) throws AVException {
    followInBackground(true, userObjectId, attributes, new FollowCallback<AVObject>() {

      @Override
      public void done(AVObject object, AVException e) {
        if (e != null) {
          AVExceptionHolder.add(e);
        }
      }
    });
    if (AVExceptionHolder.exists()) {
      throw AVExceptionHolder.remove();
    } else {
      return AVUser.this;
    }
  }

  public void followInBackground(String userObjectId, Map<String, Object> attributes,
      final FollowCallback callback) {
    followInBackground(false, userObjectId, attributes, callback);
  }

  private void followInBackground(boolean sync, String userObjectId,
      Map<String, Object> attributes, final FollowCallback callback) {
    if (!checkUserAuthentication(callback)) {
      return;
    }
    String endPoint = AVPowerfulUtils.getFollowEndPoint(getObjectId(), userObjectId);
    String paramsString = "";
    if (attributes != null) {
      paramsString = AVUtils.restfulServerData(attributes);
    }
    PaasClient.storageInstance().postObject(endPoint, paramsString, sync,
        new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            super.onSuccess(content, e); // To change body of overridden methods use File | Settings
            // |
            // File Templates.
            if (callback != null) {
              callback.internalDone(AVUser.this, null);
            }
          }

          @Override
          public void onFailure(Throwable error, String content) {
            super.onFailure(error, content); // To change body of overridden methods use File |
            // Settings
            // | File Templates.
            if (callback != null) {
              callback.internalDone(null, AVErrorUtils.createException(error, content));
            }
          }
        });

  }

  public void unfollowInBackground(String userObjectId, final FollowCallback callback) {
    unfollow(false, userObjectId, callback);
  }

  public void unfollow(String userObjectId) throws AVException {
    unfollow(true, userObjectId, new FollowCallback() {

      @Override
      public void done(AVObject object, AVException e) {
        if (e != null) {
          AVExceptionHolder.add(AVErrorUtils.createException(e, null));
        }
      }
    });
    if (AVExceptionHolder.exists()) {
      throw AVExceptionHolder.remove();
    }
  }

  private void unfollow(boolean sync, String userObjectId, final FollowCallback callback) {
    if (!checkUserAuthentication(callback)) {
      return;
    }
    String endPoint = AVPowerfulUtils.getFollowEndPoint(getObjectId(), userObjectId);
    PaasClient.storageInstance().deleteObject(endPoint, sync, new GenericObjectCallback() {
      @Override
      public void onSuccess(String content, AVException e) {
        super.onSuccess(content, e); // To change body of overridden methods use File | Settings |
        // File Templates.
        if (callback != null) {
          callback.internalDone(AVUser.this, null);
        }
      }

      @Override
      public void onFailure(Throwable error, String content) {
        super.onFailure(error, content); // To change body of overridden methods use File | Settings
        // | File Templates.
        if (callback != null) {
          callback.internalDone(null, AVErrorUtils.createException(error, content));
        }
      }
    }, null, null);
  }

  /*
   * {"results": [{"objectId":"52bbd4f9e4b0be6d851ef395",
   * "follower":{"className":"_User","objectId":"52bbd4f8e4b0be6d851ef394","__type":"Pointer"},
   * "user":{"className":"_User","objectId":"52bbd4f3e4b0be6d851ef393","__type":"Pointer"},
   * "createdAt":"2013-12-26T15:04:25.856Z", "updatedAt":"2013-12-26T15:04:25.856Z"},
   * {"objectId":"52bbd4ffe4b0be6d851ef398",
   * "follower":{"className":"_User","objectId":"52bbd4fee4b0be6d851ef397","__type":"Pointer"},
   * "user":{"className":"_User","objectId":"52bbd4f3e4b0be6d851ef393","__type":"Pointer"},
   * "createdAt":"2013-12-26T15:04:31.066Z", "updatedAt":"2013-12-26T15:04:31.066Z"} ] }
   */
  private List<AVUser> processResultByTag(String content, String tag) {
    List<AVUser> list = new LinkedList<AVUser>();
    if (AVUtils.isBlankString(content)) {
      return list;
    }
    AVFollowResponse resp = new AVFollowResponse();
    resp = JSON.parseObject(content, resp.getClass());
    processResultList(resp.results, list, tag);
    return list;
  }

  private Map<String, List<AVUser>> processFollowerAndFollowee(String content) {
    Map<String, List<AVUser>> map = new HashMap<String, List<AVUser>>();
    if (AVUtils.isBlankString(content)) {
      return map;
    }
    AVFollowResponse resp = new AVFollowResponse();
    resp = JSON.parseObject(content, resp.getClass());
    List<AVUser> followers = new LinkedList<AVUser>();
    List<AVUser> followees = new LinkedList<AVUser>();
    processResultList(resp.followers, followers, FOLLOWER_TAG);
    processResultList(resp.followees, followees, FOLLOWEE_TAG);
    map.put(FOLLOWER_TAG, followers);
    map.put(FOLLOWEE_TAG, followees);
    return map;
  }

  // TODO, consider subclass.
  private void processResultList(Map[] results, List<AVUser> list, String tag) {
    for (Map item : results) {
      if (item != null && !item.isEmpty()) {
        AVUser user = (AVUser) AVUtils.getObjectFrom(item.get(tag));
        list.add(user);
      }
    }
  }

  /**
   * <p>
   * 创建follower查询。请确保传入的userObjectId不为空，否则会抛出IllegalArgumentException。
   * 创建follower查询后，您可以使用whereEqualTo("follower", userFollower)查询特定的follower。 您也可以使用skip和limit支持分页操作。
   * 
   * </p>
   * 
   * @param userObjectId 待查询的用户objectId。
   * @param clazz AVUser类或者其子类。
   * @param <T> subclass of AVUser
   * @return follower查询对象
   * @since 2.3.0
   */
  static public <T extends AVUser> AVQuery<T> followerQuery(final String userObjectId,
      Class<T> clazz) {
    if (AVUtils.isBlankString(userObjectId)) {
      throw new IllegalArgumentException("Blank user objectId.");
    }
    AVFellowshipQuery query = new AVFellowshipQuery<T>("_Follower", clazz);
    query.whereEqualTo("user", AVObject.createWithoutData("_User", userObjectId));
    query.setFriendshipTag(AVUser.FOLLOWER_TAG);
    return query;
  }

  /**
   * <p>
   * 创建follower查询。创建follower查询后，您可以使用whereEqualTo("follower", userFollower)查询特定的follower。
   * 您也可以使用skip和limit支持分页操作。
   * 
   * </p>
   * 
   * @param clazz AVUser类或者其子类。
   * @param <T> subclass of AVUser
   * @return follower查询对象
   * @since 2.3.0
   * @throws AVException 如果当前对象未保存过则会报错
   */
  public <T extends AVUser> AVQuery<T> followerQuery(Class<T> clazz) throws AVException {
    if (AVUtils.isBlankString(this.getObjectId())) {
      throw AVErrorUtils.sessionMissingException();
    }
    return followerQuery(getObjectId(), clazz);
  }

  /**
   * <p>
   * 创建followee查询。请确保传入的userObjectId不为空，否则会抛出IllegalArgumentException。
   * 创建followee查询后，您可以使用whereEqualTo("followee", userFollowee)查询特定的followee。 您也可以使用skip和limit支持分页操作。
   * 
   * </p>
   * 
   * @param userObjectId 待查询的用户objectId。
   * @param clazz AVUser类或者其子类。
   * @param <T> subclass of AVUser
   * @return followee查询
   * @since 2.3.0
   */
  static public <T extends AVUser> AVQuery<T> followeeQuery(final String userObjectId,
      Class<T> clazz) {
    if (AVUtils.isBlankString(userObjectId)) {
      throw new IllegalArgumentException("Blank user objectId.");
    }
    AVFellowshipQuery query = new AVFellowshipQuery<T>("_Followee", clazz);
    query.whereEqualTo("user", AVObject.createWithoutData("_User", userObjectId));
    query.setFriendshipTag(AVUser.FOLLOWEE_TAG);
    return query;
  }

  /**
   * <p>
   * 创建followee查询。 创建followee查询后，您可以使用whereEqualTo("followee", userFollowee)查询特定的followee。
   * 您也可以使用skip和limit支持分页操作。
   * 
   * </p>
   * 
   * @param clazz AVUser类或者其子类。
   * @return followee 查询
   * @param <T> AVUser的子类
   * @throws AVException 如果本对象从来没有保存过会遇到错误
   * 
   * @since 2.3.0
   */
  public <T extends AVUser> AVQuery<T> followeeQuery(Class<T> clazz) throws AVException {
    if (AVUtils.isBlankString(this.getObjectId())) {
      throw AVErrorUtils.sessionMissingException();
    }
    return followeeQuery(getObjectId(), clazz);
  }

  /**
   * 获取用户好友关系的查询条件，同时包括用户的关注和用户粉丝
   * 
   * @return 好友查询
   */
  public AVFriendshipQuery friendshipQuery() {
    return this.friendshipQuery(subClazz == null ? AVUser.class : subClazz);
  }

  /**
   * 获取用户好友关系的查询条件，同时包括用户的关注和用户粉丝
   * 
   * @param clazz 最终返回的AVUser的子类
   * @param <T> AVUser的子类
   * @return 好友查询
   */
  public <T extends AVUser> AVFriendshipQuery friendshipQuery(Class<T> clazz) {
    return new AVFriendshipQuery(this.objectId, clazz);
  }

  /**
   * 获取用户好友关系的查询条件，同时包括用户的关注和用户粉丝
   * 
   * @param userId AVUser的objectId
   * @param <T> AVUser的子类
   * @return 好友查询
   */
  public static <T extends AVUser> AVFriendshipQuery friendshipQuery(String userId) {
    return new AVFriendshipQuery(userId, subClazz == null ? AVUser.class : subClazz);
  }

  /**
   * 获取用户好友关系的查询条件，同时包括用户的关注和用户粉丝
   * 
   * @param userId AVUser的objectId
   * @param clazz 指定的AVUser或者其子类
   * @param <T> AVUser的子类
   * @return 好友查询
   */
  public static <T extends AVUser> AVFriendshipQuery friendshipQuery(String userId, Class<T> clazz) {
    return new AVFriendshipQuery(userId, clazz);
  }

  @Deprecated
  public void getFollowersInBackground(final FindCallback callback) {
    if (!checkUserAuthentication(callback)) {
      return;
    }
    String endPoint = AVPowerfulUtils.getFollowersEndPoint(getObjectId());
    PaasClient.storageInstance().getObject(endPoint, null, false, null,
        new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            super.onSuccess(content, e);
            List<AVUser> list = processResultByTag(content, FOLLOWER_TAG);
            if (callback != null) {
              callback.internalDone(list, null);
            }
          }

          @Override
          public void onFailure(Throwable error, String content) {
            super.onFailure(error, content);
            if (callback != null) {
              callback.internalDone(null, AVErrorUtils.createException(error, content));
            }
          }
        });
  }

  @Deprecated
  public void getMyFolloweesInBackground(final FindCallback callback) {
    if (!checkUserAuthentication(callback)) {
      return;
    }
    String endPoint = AVPowerfulUtils.getFolloweesEndPoint(getObjectId());
    PaasClient.storageInstance().getObject(endPoint, null, false, null,
        new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            super.onSuccess(content, e);
            List<AVUser> list = processResultByTag(content, FOLLOWEE_TAG);
            if (callback != null) {
              callback.internalDone(list, null);
            }
          }

          @Override
          public void onFailure(Throwable error, String content) {
            super.onFailure(error, content);
            if (callback != null) {
              callback.internalDone(null, AVErrorUtils.createException(error, content));
            }
          }
        });
  }

  public void getFollowersAndFolloweesInBackground(final FollowersAndFolloweesCallback callback) {
    if (!checkUserAuthentication(callback)) {
      return;
    }
    String endPoint = AVPowerfulUtils.getFollowersAndFollowees(getObjectId());
    PaasClient.storageInstance().getObject(endPoint, null, false, null,
        new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            super.onSuccess(content, e);
            Map<String, List<AVUser>> map = processFollowerAndFollowee(content);
            if (callback != null) {
              callback.internalDone(map, null);
            }
          }

          @Override
          public void onFailure(Throwable error, String content) {
            super.onFailure(error, content);
            if (callback != null) {
              callback.internalDone(null, AVErrorUtils.createException(error, content));
            }
          }
        });
  }

  /*
   * 通过这个方法可以将AVUser对象强转为其子类对象
   */
  public static <T extends AVUser> T cast(AVUser user, Class<T> clazz) {
    try {
      T newUser = AVObject.cast(user, clazz);
      return newUser;
    } catch (Exception e) {
      LogUtil.log.e("ClassCast Exception", e);
    }
    return null;
  }

  /**
   * 
   * 通过设置此方法，所有关联对象中的AVUser对象都会被强转成注册的AVUser子类对象
   * 
   * @param clazz AVUser的子类
   */

  public static void alwaysUseSubUserClass(Class<? extends AVUser> clazz) {
    subClazz = clazz;
  }

  private static Map<String, Object> authData(AVThirdPartyUserAuth userInfo) {
    Map<String, Object> result = new HashMap<String, Object>();
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(accessTokenTag, userInfo.accessToken);
    map.put(expiresAtTag, userInfo.expiredAt);
    if (!AVUtils.isBlankString(userInfo.snsType)) {
      map.put(AVThirdPartyUserAuth.platformUserIdTag(userInfo.snsType), userInfo.userId);
    }
    result.put(userInfo.snsType, map);
    return result;
  }



  /**
   * 生成一个新的AarseUser，并且将AVUser与SNS平台获取的userInfo关联。
   * 
   * @param userInfo 包含第三方授权必要信息的内部类
   * @param callback 关联完成后，调用的回调函数。
   */
  static public void loginWithAuthData(AVThirdPartyUserAuth userInfo,
      final LogInCallback<AVUser> callback) {
    loginWithAuthData(AVUser.class, userInfo, callback);
  }

  /**
   * 生成一个新的AVUser子类化对象，并且将该对象与SNS平台获取的userInfo关联。
   * 
   * @param clazz 子类化的AVUer的class对象
   * @param userInfo 在SNS登录成功后，返回的userInfo信息。
   * @param callback 关联完成后，调用的回调函数。
   * @param <T> AVUser子类
   * @since 1.4.4
   */
  static public <T extends AVUser> void loginWithAuthData(final Class<T> clazz,
      final AVThirdPartyUserAuth userInfo, final LogInCallback<T> callback) {
    if (userInfo == null) {
      if (callback != null) {
        callback.internalDone(null,
            AVErrorUtils.createException(AVException.OTHER_CAUSE, "NULL userInfo."));
      }
      return;
    }

    Map<String, Object> data = new HashMap<String, Object>();
    data.put(authDataTag, authData(userInfo));
    String jsonString = JSON.toJSONString(data);
    PaasClient.storageInstance().postObject("users", jsonString, false, false,
        new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            if (e == null) {
              T userObject = AVUser.newAVUser(clazz, callback);
              if (userObject == null) {
                return;
              }
              AVUtils.copyPropertiesFromJsonStringToAVObject(content, userObject);
              userObject.processAuthData(userInfo);
              AVUser.changeCurrentUser(userObject, true);
              if (callback != null) {
                callback.internalDone(userObject, null);
              }
            }
          }

          @Override
          public void onFailure(Throwable error, String content) {
            if (callback != null) {
              callback.internalDone(null, AVErrorUtils.createException(error, content));
            }
          }
        }, null, null);
  }

  /**
   * 将现存的AVUser与从SNS平台获取的userInfo关联起来。
   * 
   * @param user AVUser 对象。
   * @param userInfo 在SNS登录成功后，返回的userInfo信息。
   * @param callback 关联完成后，调用的回调函数。
   * @since 1.4.4
   */
  static public void associateWithAuthData(AVUser user, AVThirdPartyUserAuth userInfo,
      final SaveCallback callback) {
    if (userInfo == null) {
      if (callback != null) {
        callback.internalDone(AVErrorUtils.createException(AVException.OTHER_CAUSE,
            "NULL userInfo."));
      }
      return;
    }
    Map<String, Object> authData = authData(userInfo);
    if (user.get(authDataTag) != null && user.get(authDataTag) instanceof Map) {
      authData.putAll((Map<String, Object>) user.get(authDataTag));
    }
    user.put(authDataTag, authData);
    user.markAnonymousUserTransfer();
    user.saveInBackground(callback);
  }

  static public void dissociateAuthData(final AVUser user, final String type,
      final SaveCallback callback) {
    Map<String, Object> authData = (Map<String, Object>) user.get(authDataTag);
    if (authData != null) {
      authData.remove(type);
    }
    user.put(authDataTag, authData);
    if (user.isAuthenticated() && !AVUtils.isBlankString(user.getObjectId())) {
      user.saveInBackground(new SaveCallback() {

        @Override
        public void done(AVException e) {
          user.processAuthData(new AVThirdPartyUserAuth(null, null, type, null));
          if (callback != null) {
            callback.internalDone(e);
          }
        }
      });
    } else {
      if (callback != null) {
        callback.internalDone(new AVException(AVException.SESSION_MISSING,
            "the user object missing a valid session"));
      }
    }
  }

  private static final String accessTokenTag = "access_token";
  private static final String expiresAtTag = "expires_at";
  private static final String authDataTag = "authData";
  private static final String anonymousTag = "anonymous";

  protected void processAuthData(AVThirdPartyUserAuth auth) {
    Map<String, Object> authData = (Map<String, Object>) this.get(authDataTag);
    // 匿名用户转化为正式用户
    if (needTransferFromAnonymousUser) {
      if (authData != null && authData.containsKey(anonymousTag)) {
        authData.remove(anonymousTag);
      } else {
        anonymous = false;
      }
      needTransferFromAnonymousUser = false;
    }
    if (authData != null) {
      if (authData.containsKey(AVThirdPartyUserAuth.SNS_SINA_WEIBO)) {
        Map<String, Object> sinaAuthData =
            (Map<String, Object>) authData.get(AVThirdPartyUserAuth.SNS_SINA_WEIBO);
        this.sinaWeiboToken = (String) sinaAuthData.get(accessTokenTag);
      } else {
        this.sinaWeiboToken = null;
      }
      if (authData.containsKey(AVThirdPartyUserAuth.SNS_TENCENT_WEIBO)) {
        Map<String, Object> qqAuthData =
            (Map<String, Object>) authData.get(AVThirdPartyUserAuth.SNS_TENCENT_WEIBO);
        this.qqWeiboToken = (String) qqAuthData.get(accessTokenTag);
      } else {
        this.qqWeiboToken = null;
      }
      if (authData.containsKey(anonymousTag)) {
        this.anonymous = true;
      } else {
        this.anonymous = false;
      }
    }
    if (auth != null) {
      if (auth.snsType.equals(AVThirdPartyUserAuth.SNS_SINA_WEIBO)) {
        sinaWeiboToken = auth.accessToken;
        return;
      }
      if (auth.snsType.equals(AVThirdPartyUserAuth.SNS_TENCENT_WEIBO)) {
        qqWeiboToken = auth.accessToken;
        return;
      }
    }
  }

  public static class AVThirdPartyUserAuth {
    String accessToken;
    String expiredAt;
    String snsType;
    String userId;

    public static final String SNS_TENCENT_WEIBO = "qq";
    public static final String SNS_SINA_WEIBO = "weibo";
    public static final String SNS_TENCENT_WEIXIN = "weixin";

    public AVThirdPartyUserAuth(String accessToken, String expiredAt, String snstype, String userId) {
      this.accessToken = accessToken;
      this.snsType = snstype;
      this.expiredAt = expiredAt;
      this.userId = userId;
    }

    protected static String platformUserIdTag(String type) {
      if (SNS_TENCENT_WEIBO.equalsIgnoreCase(type) || SNS_TENCENT_WEIXIN.equalsIgnoreCase(type)) {
        return "openid";
      } else {
        return "uid";
      }
    }

    public String getAccessToken() {
      return accessToken;
    }

    public void setAccessToken(String accessToken) {
      this.accessToken = accessToken;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public String getExpireAt() {
      return expiredAt;
    }

    public void setExpireAt(String expireAt) {
      this.expiredAt = expireAt;
    }

    public String getSnsType() {
      return snsType;
    }

    public void setSnsType(String snsType) {
      this.snsType = snsType;
    }
  }

  private void markAnonymousUserTransfer() {
    if (isAnonymous()) {
      needTransferFromAnonymousUser = true;
    }
  }
}
