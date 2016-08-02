package com.avos.avoscloud;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.CookieHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.internal.AppConfiguration;
import com.avos.avoscloud.internal.InternalCache;
import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.okhttp.Call;
import com.avos.avoscloud.okhttp.Interceptor;
import com.avos.avoscloud.okhttp.MediaType;
import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.RequestBody;
import com.avos.avoscloud.okhttp.Response;
import com.avos.avoscloud.okhttp.ResponseBody;
import com.avos.avoscloud.okhttp.internal.framed.Header;
import com.avos.avoscloud.okio.Buffer;
import com.avos.avoscloud.okio.BufferedSource;
import com.avos.avoscloud.okio.ForwardingSource;
import com.avos.avoscloud.okio.Okio;
import com.avos.avoscloud.okio.Source;


public class PaasClient {
  private static final CookieHandler cookieHandler = new CookieHandler() {
    @Override
    public Map<String, List<String>> get(URI uri, Map<String, List<String>> map) throws IOException {
      return Collections.emptyMap();
    }

    @Override
    public void put(URI uri, Map<String, List<String>> map) throws IOException {

    }
  };

  private final String apiVersion;

  private static String applicationIdField = "X-LC-Id";
  private static String apiKeyField = "X-LC-Key";
  protected static String sessionTokenField = "X-LC-Session";
  private static boolean isCN = true;
  private boolean isProduction = true;

  private static final String defaultEncoding = "UTF-8";
  public static final String defaultContentType = "application/json";
  public static final String DEFAULT_FAIL_STRING = "request failed!!!";

  public static final String sdkVersion = "v3.13-SNAPSHOT";

  private AVACL defaultACL;

  private volatile AVHttpClient httpClient;
  private static boolean lastModifyEnabled = false;
  private static String REQUEST_STATIS_HEADER = "X-Android-RS";
  private String baseUrl;

  static HashMap<String, PaasClient> serviceClientMap = new HashMap<String, PaasClient>();
  static Map<String, AVObjectReferenceCount> internalObjectsForEventuallySave = Collections
      .synchronizedMap(new HashMap<String, AVObjectReferenceCount>());

  private static Map<String, String> lastModify = Collections
      .synchronizedMap(new WeakHashMap<String, String>());

  void setProduction(boolean production) {
    isProduction = production;
  }

  protected static PaasClient sharedInstance(AVOSServices service) {

    String host =
        InternalConfigurationController.globalInstance().getAppConfiguration()
            .getService(service.toString());
    PaasClient instance = serviceClientMap.get(host);
    if (instance == null) {
      instance = new PaasClient();
      instance.setBaseUrl(host);
      serviceClientMap.put(host, instance);
    }
    return instance;
  }

  public static PaasClient storageInstance() {
    return sharedInstance(AVOSServices.STORAGE_SERVICE);
  }

  public static PaasClient cloudInstance() {
    return sharedInstance(AVOSServices.FUNCTION_SERVICE);
  }

  public static PaasClient statistisInstance() {
    return sharedInstance(AVOSServices.STATISTICS_SERVICE);
  }

  AVACL getDefaultACL() {
    return defaultACL;
  }

  void setDefaultACL(AVACL acl) {
    defaultACL = acl;
  }

  public Map<String, String> userHeaderMap() {
    AVUser user = AVUser.getCurrentUser();
    if (user != null) {
      return user.headerMap();
    }
    return null;
  }

  private PaasClient() {
    apiVersion = "1.1";
    useUruluServer();
  }

  protected void updateHeaders(Request.Builder builder, Map<String, String> header,
      boolean needRequestStatistic) throws AVException {
    if (!InternalConfigurationController.globalInstance().getAppConfiguration().isConfigured()) {
      throw new AVException(AVException.NOT_INITIALIZED,
          "You must call AVOSCloud.initialize before using the AVOSCloud library");
    }
    // if the field isnt exist, the server will assume it's true
    builder.header("X-LC-Prod", isProduction ? "1" : "0");
    AVUser currAVUser = AVUser.getCurrentUser();
    builder.header(sessionTokenField,
        (currAVUser != null && currAVUser.getSessionToken() != null) ? currAVUser.getSessionToken()
            : "");
    builder.header(applicationIdField, InternalConfigurationController.globalInstance()
        .getAppConfiguration().getApplicationId());
    builder.header("Accept", defaultContentType);
    builder.header("Content-Type", defaultContentType);
    builder.header("User-Agent", InternalConfigurationController.globalInstance()
        .getClientConfiguration().getUserAgent());
    builder.header("X-LC-Sign", InternalConfigurationController.globalInstance()
        .getInternalRequestSign().requestSign());


    if (header != null) {
      for (Map.Entry<String, String> entry : header.entrySet()) {
        builder.header(entry.getKey(), entry.getValue());
      }
    }

    if (needRequestStatistic) {
      builder.header(REQUEST_STATIS_HEADER, "1");
    }
  }

  public synchronized AVHttpClient clientInstance() {
    if (httpClient == null) {
      httpClient = new AVHttpClient();
    }
    httpClient.setConnectTimeout(InternalConfigurationController.globalInstance()
        .getClientConfiguration().getNetworkTimeoutInMills(), TimeUnit.MILLISECONDS);
    return httpClient;
  }



  public void useUruluServer() {
    if (isCN) {
      useAVCloudCN();
    } else {
      useAVCloudUS();
    }
  }

  private static void switchPushRouter(String routerServer) {
    if (AVUtils.isAndroid()) {
      try {
        Class<?> avPushRouterClass = Class.forName("com.avos.avospush.push.AVPushRouter");
        Method switchMethod = avPushRouterClass.getMethod(routerServer);
        switchMethod.invoke(avPushRouterClass);
      } catch (Exception e) {
        if (InternalConfigurationController.globalInstance().getInternalLogger()
            .showInternalDebugLog()) {
          LogUtil.avlog.i("avpushRouter server didn't switched");
        }
      }
    }
  }

  public static void useAVCloudUS() {
    isCN = false;
    InternalConfigurationController.globalInstance().getAppConfiguration()
        .configureService(AVOSServices.STORAGE_SERVICE.toString(), "https://us-api.leancloud.cn");
    InternalConfigurationController.globalInstance().getAppConfiguration()
        .setStorageType(AppConfiguration.StorageType.StorageTypeS3);
    switchPushRouter("useAVOSCloudUS");
  }

  protected static void updateAPIServerWhenCN(String apiServer) {
    if (isCN) {
      InternalConfigurationController.globalInstance().getAppConfiguration()
          .configureService(AVOSServices.STORAGE_SERVICE.toString(), apiServer);
      InternalConfigurationController.globalInstance().getAppConfiguration()
          .configureService(AVOSServices.STORAGE_SERVICE.toString(), apiServer);
    }
  }

  public static void useAVCloudCN() {
    InternalConfigurationController.globalInstance().getAppConfiguration()
        .configureService(AVOSServices.STORAGE_SERVICE.toString(), "https://api.leancloud.cn");
    InternalConfigurationController.globalInstance().getAppConfiguration()
        .setStorageType(AppConfiguration.StorageType.StorageTypeQiniu);
    switchPushRouter("useAVOSCloudCN");
  }

  public static void useLocalStg() {
    InternalConfigurationController.globalInstance().getAppConfiguration()
        .configureService(AVOSServices.STORAGE_SERVICE.toString(), "https://cn-stg1.avoscloud.com");
    InternalConfigurationController.globalInstance().getAppConfiguration()
        .setStorageType(AppConfiguration.StorageType.StorageTypeQiniu);
  }

  public String buildUrl(final String path) {
    return String.format("%s/%s/%s", this.baseUrl, apiVersion, path);
  }

  public String buildUrl(final String path, AVRequestParams params) {
    String endPoint = buildUrl(path);
    if (params == null || params.isEmpty()) {
      return endPoint;
    } else {
      return params.getWholeUrl(endPoint);
    }

  }

  private String batchUrl() {
    return String.format("%s/%s/batch", this.baseUrl, apiVersion);
  }

  private String batchSaveRelativeUrl() {
    return "batch/save";
  }

  private AsyncHttpResponseHandler createGetHandler(GenericObjectCallback callback,
      AVQuery.CachePolicy policy, String absoluteURLString) {
    AsyncHttpResponseHandler handler =
        new GetHttpResponseHandler(callback, policy, absoluteURLString);
    return handler;
  }

  private AsyncHttpResponseHandler createPostHandler(GenericObjectCallback callback) {
    AsyncHttpResponseHandler handler = new PostHttpResponseHandler(callback);
    return handler;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public void setBaseUrl(final String url) {
    this.baseUrl = url;
  }

  public String getBaseUrl() {
    return this.baseUrl;
  }

  protected static void setServiceHost(AVOSServices service, String host) {
    InternalConfigurationController.globalInstance().getAppConfiguration()
        .configureService(service.toString(), host);
  }

  public String getObject(final String relativePath, final AVRequestParams parameters,
      final boolean sync, final Map<String, String> header, final GenericObjectCallback callback,
      final AVQuery.CachePolicy policy, final long maxAgeInMilliseconds) {
    final String absoluteURLString = buildUrl(relativePath, parameters);


    final String absolutURLString = generateQueryPath(relativePath, parameters);
    final String lastModifyTime = getLastModify(absolutURLString);
    switch (policy) {
      default:
      case IGNORE_CACHE:
        getObject(relativePath, parameters, sync, header, callback, policy);
        break;
      case CACHE_ONLY:
        InternalConfigurationController.globalInstance().getCache()
            .get(absolutURLString, maxAgeInMilliseconds, lastModifyTime, callback);
        break;
      case NETWORK_ONLY:
        getObject(relativePath, parameters, sync, header, callback, policy);
        break;
      case CACHE_ELSE_NETWORK:
        InternalConfigurationController
            .globalInstance()
            .getCache()
            .get(absolutURLString, maxAgeInMilliseconds, lastModifyTime,
                new GenericObjectCallback() {
                  @Override
                  public void onSuccess(String content, AVException e) {
                    callback.onSuccess(content, e);
                  }

                  @Override
                  public void onFailure(Throwable error, String content) {
                    getObject(relativePath, parameters, sync, header, callback, policy);
                  }
                });
        break;
      case NETWORK_ELSE_CACHE:
        getObject(relativePath, parameters, sync, header, new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            callback.onSuccess(content, e);
          }

          @Override
          public void onFailure(Throwable error, String content) {
            InternalCache cacheManager =
                InternalConfigurationController.globalInstance().getCache();
            if (cacheManager.hasValidCache(absolutURLString, lastModifyTime, maxAgeInMilliseconds)) {
              cacheManager.get(absolutURLString, maxAgeInMilliseconds, lastModifyTime, callback);
            } else {
              callback.onFailure(error, content);
            }
          }
        }, policy);
        break;
      case CACHE_THEN_NETWORK:
        InternalConfigurationController
            .globalInstance()
            .getCache()
            .get(absolutURLString, maxAgeInMilliseconds, lastModifyTime,
                new GenericObjectCallback() {
                  @Override
                  public void onSuccess(String content, AVException e) {
                    callback.onSuccess(content, e);
                    getObject(relativePath, parameters, sync, header, callback, policy);
                  }

                  @Override
                  public void onFailure(Throwable error, String content) {
                    callback.onFailure(error, content);
                    getObject(relativePath, parameters, sync, header, callback, policy);
                  }
                });
        break;
    }
    return absoluteURLString;
  }

  String generateQueryPath(final String relativePath, final AVRequestParams parameters) {
    return buildUrl(relativePath, parameters);
  }

  public void getObject(final String relativePath, AVRequestParams parameters, boolean sync,
      Map<String, String> inputHeader, GenericObjectCallback callback, AVQuery.CachePolicy policy) {
    getObject(relativePath, parameters, sync, inputHeader, callback, policy,
        (!policy.equals(AVQuery.CachePolicy.CACHE_ONLY)) && isLastModifyEnabled());
  }

  public void getObject(final String relativePath, final AVRequestParams parameters,
      final boolean sync, final Map<String, String> inputHeader, GenericObjectCallback callback,
      final AVQuery.CachePolicy policy, final boolean fetchRetry) {
    Map<String, String> myHeader = inputHeader;
    if (inputHeader == null) {
      myHeader = new HashMap<String, String>();
    }
    updateHeaderForPath(relativePath, parameters, myHeader);

    String url = buildUrl(relativePath, parameters);
    AsyncHttpResponseHandler handler = createGetHandler(callback, policy, url);
    if (InternalConfigurationController.globalInstance().getInternalLogger().isDebugEnabled()) {
      dumpHttpGetRequest(buildUrl(relativePath),
          parameters == null ? null : parameters.getDumpQueryString());
    }
    AVHttpClient client = clientInstance();
    Request.Builder builder = new Request.Builder();
    builder.url(url).get();
    try {
      updateHeaders(builder, myHeader, callback != null && callback.isRequestStatisticNeed());
    } catch (AVException e) {
      processException(e, callback);
    }
    client.execute(builder.build(), sync, handler);
  }

  public void getObject(final String relativePath, AVRequestParams parameters, boolean sync,
      Map<String, String> header, GenericObjectCallback callback) {
    getObject(relativePath, parameters, sync, header, callback, AVQuery.CachePolicy.IGNORE_CACHE);
  }

  public void putObject(final String relativePath, String object, boolean sync,
      Map<String, String> header, GenericObjectCallback callback, String objectId,
      String _internalId) {
    putObject(relativePath, object, sync, false, header, callback, objectId, _internalId);
  }


  public void putObject(final String relativePath, String object, boolean sync,
      boolean isEventually, Map<String, String> header, GenericObjectCallback callback,
      String objectId, String _internalId) {
    try {
      if (isEventually) {
        File archivedFile = archiveRequest("put", relativePath, object, objectId, _internalId);
        handleArchivedRequest(archivedFile, sync, callback);
      } else {
        String url = buildUrl(relativePath);
        AsyncHttpResponseHandler handler = createPostHandler(callback);
        if (InternalConfigurationController.globalInstance().getInternalLogger().isDebugEnabled()) {
          dumpHttpPutRequest(header, url, object);
        }
        AVHttpClient client = clientInstance();
        Request.Builder builder = new Request.Builder();
        builder.url(url).put(RequestBody.create(AVHttpClient.JSON, object));
        updateHeaders(builder, header, callback != null && callback.isRequestStatisticNeed());
        client.execute(builder.build(), sync, handler);
      }
    } catch (Exception exception) {
      processException(exception, callback);
    }
  }

  private void processException(Exception e, GenericObjectCallback cb) {
    if (cb != null) {
      cb.onFailure(e, null);
    }
  }

  // path=/1/classes/Parent/a1QCssTp7r
  Map<String, Object> batchItemMap(String method, String path, Object body, Map params) {
    // String myPath = String.format("/%s/%s",
    // PaasClient.sharedInstance().apiVersion, path);
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("method", method);
    result.put("path", path);
    result.put("body", body);
    if (params != null) {
      result.put("params", params);
    }
    return result;
  }

  Map<String, Object> batchItemMap(String method, String path, Object body) {
    return this.batchItemMap(method, path, body, null);
  }

  @Deprecated
  List<Object> assembleBatchOpsList(List<Object> itemList, String path) {
    List<Object> list = new ArrayList<Object>();
    for (Object object : itemList) {
      Map<String, Object> opDict = batchItemMap("PUT", path, object);
      list.add(opDict);
    }
    return list;
  }

  private Map<String, Object> batchRequest(List<Object> list) {
    Map<String, Object> requests = new HashMap<String, Object>();
    requests.put("requests", list);
    return requests;
  }

  // only called @sendPendingOps
  public void postBatchObject(List<Object> parameters, boolean sync, Map<String, String> header,
      GenericObjectCallback callback) {
    try {
      String url = batchUrl();
      Map<String, Object> requests = batchRequest(parameters);
      String json = JSON.toJSONString(requests);
      if (InternalConfigurationController.globalInstance().getInternalLogger().isDebugEnabled()) {
        dumpHttpPostRequest(header, url, json);
      }
      AsyncHttpResponseHandler handler = createPostHandler(callback);
      AVHttpClient client = clientInstance();
      Request.Builder builder = new Request.Builder();
      builder.url(url).post(RequestBody.create(AVHttpClient.JSON, json));
      updateHeaders(builder, header, callback != null && callback.isRequestStatisticNeed());

      client.execute(builder.build(), sync, handler);
    } catch (Exception exception) {
      processException(exception, callback);
    }
  }


  public void postBatchSave(final List list, final boolean sync, final boolean isEventually,
      final Map<String, String> header, final GenericObjectCallback callback,
      final String objectId, final String _internalId) {
    try {
      Map params = new HashMap();
      params.put("requests", list);
      String paramString = AVUtils.jsonStringFromMapWithNull(params);
      if (isEventually) {
        File archivedFile =
            archiveRequest("post", batchSaveRelativeUrl(), paramString, objectId, _internalId);
        handleArchivedRequest(archivedFile, sync, callback);
      } else {
        String url = buildUrl(batchSaveRelativeUrl());
        if (InternalConfigurationController.globalInstance().getInternalLogger().isDebugEnabled()) {
          dumpHttpPostRequest(header, url, paramString);
        }
        AsyncHttpResponseHandler handler = createPostHandler(callback);
        AVHttpClient client = clientInstance();
        Request.Builder builder = new Request.Builder();
        builder.url(url).post(RequestBody.create(AVHttpClient.JSON, paramString));
        updateHeaders(builder, header, callback != null && callback.isRequestStatisticNeed());
        client.execute(builder.build(), sync, handler);
      }
    } catch (Exception exception) {
      processException(exception, callback);
    }
  }

  public void postObject(final String relativePath, String object, boolean sync,
      GenericObjectCallback callback) {
    postObject(relativePath, object, sync, false, callback, null, null);
  }

  public void postObject(final String relativePath, String object, boolean sync,
      boolean isEventually, GenericObjectCallback callback, String objectId, String _internalId) {
    try {
      if (isEventually) {
        File archivedFile = archiveRequest("post", relativePath, object, objectId, _internalId);
        handleArchivedRequest(archivedFile, sync, callback);
      } else {
        String url = buildUrl(relativePath);
        if (InternalConfigurationController.globalInstance().getInternalLogger().isDebugEnabled()) {
          dumpHttpPostRequest(null, url, object);
        }
        AsyncHttpResponseHandler handler = createPostHandler(callback);
        AVHttpClient client = clientInstance();
        Request.Builder builder = new Request.Builder();
        updateHeaders(builder, null, callback != null && callback.isRequestStatisticNeed());
        builder.url(url).post(RequestBody.create(AVHttpClient.JSON, object));
        client.execute(builder.build(), sync, handler);
      }
    } catch (Exception exception) {
      processException(exception, callback);
    }
  }

  public void deleteObject(final String relativePath, boolean sync, GenericObjectCallback callback,
      String objectId, String _internalId) {
    deleteObject(relativePath, sync, false, callback, objectId, _internalId);
  }


  public void deleteObject(final String relativePath, boolean sync, boolean isEventually,
      GenericObjectCallback callback, String objectId, String _internalId) {
    try {
      if (isEventually) {
        File archivedFile = archiveRequest("delete", relativePath, null, objectId, _internalId);
        handleArchivedRequest(archivedFile, sync, callback);
      } else {
        String url = buildUrl(relativePath);
        if (InternalConfigurationController.globalInstance().getInternalLogger().isDebugEnabled()) {
          dumpHttpDeleteRequest(null, url, null);
        }
        AsyncHttpResponseHandler handler = createPostHandler(callback);
        AVHttpClient client = clientInstance();
        Request.Builder builder = new Request.Builder();
        updateHeaders(builder, null, callback != null && callback.isRequestStatisticNeed());

        builder.url(url).delete();
        client.execute(builder.build(), sync, handler);
      }
    } catch (Exception exception) {
      processException(exception, callback);
    }
  }


  // ================================================================================
  // Archive and handle request
  // ================================================================================

  /*
   * type for archive: 1. post 2. delete
   */
  private File archiveRequest(String method, String relativePath, String paramString,
      String objectId, String _internalId) {
    File theArchivedFile =
        new File(InternalConfigurationController.globalInstance().getInternalPersistence()
            .getCommandCacheDir(), AVUtils.getArchiveRequestFileName(objectId, _internalId, method,
            relativePath, paramString));

    Map<String, String> fileMap = new HashMap<String, String>(3);
    fileMap.put("method", method);
    fileMap.put("relativePath", relativePath);
    fileMap.put("paramString", paramString);
    fileMap.put("objectId", objectId);
    fileMap.put("_internalId", _internalId);

    InternalConfigurationController.globalInstance().getInternalPersistence()
        .saveContentToFile(AVUtils.toJSON(fileMap), theArchivedFile);

    if (InternalConfigurationController.globalInstance().getInternalLogger().isDebugEnabled()) {
      LogUtil.log.d(AVUtils.restfulServerData(fileMap) + "\n" + "did save to "
          + theArchivedFile.getAbsolutePath());
    }
    return theArchivedFile;
  }

  private void handleArchivedRequest(File archivedFile, boolean sync) {
    handleArchivedRequest(archivedFile, sync, null);
  }

  private void handleArchivedRequest(final File archivedFile, boolean sync,
      final GenericObjectCallback callback) {
    try {
      String archivedFileContent =
          InternalConfigurationController.globalInstance().getInternalPersistence()
              .readContentFromFile(archivedFile);
      Map<String, String> fileMap = null;

      fileMap = AVUtils.getFromJSON(archivedFileContent, Map.class);
      if (fileMap != null && !fileMap.isEmpty()) {
        String method = fileMap.get("method");
        String relativePath = fileMap.get("relativePath");
        String paramString = fileMap.get("paramString");
        String objectId = fileMap.get("objectId");
        String _internalId = fileMap.get("_internalId");
        GenericObjectCallback newCallback = new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            if (callback != null) {
              callback.onSuccess(content, e);
            }
            try {
              Map<String, String> objectMap = AVUtils.getFromJSON(content, Map.class);
              for (String _internalId : objectMap.keySet()) {
                if (internalObjectsForEventuallySave.get(_internalId) != null) {
                  internalObjectsForEventuallySave.get(_internalId).getValue()
                      .copyFromMap(objectMap);
                  unregisterEvtuallyObject(internalObjectsForEventuallySave.get(_internalId)
                      .getValue());
                }
              }
            } catch (Exception e1) {
              LogUtil.avlog.e("parse exception during archive request" + e.getMessage());
            }
            InternalConfigurationController.globalInstance().getInternalPersistence()
                .deleteFile(archivedFile);
          }

          @Override
          public void onFailure(Throwable error, String content) {
            // handle retry
            if (callback != null)
              callback.onFailure(error, content);
          }
        };
        if (method == null) {
          newCallback.onFailure(new AVRuntimeException("Null method."), null);
        }
        if ("post".equalsIgnoreCase(method)) {
          postObject(relativePath, paramString, sync, newCallback);
        } else if ("put".equalsIgnoreCase(method)) {
          putObject(relativePath, paramString, sync, null, newCallback, objectId, _internalId);
        } else if ("delete".equalsIgnoreCase(method)) {
          deleteObject(relativePath, sync, newCallback, objectId, _internalId);
        }
      }
    } catch (Exception e) {
      return;
    }
  }

  public void handleAllArchivedRequest() {
    handleAllArchivedRequest(false);
  }

  protected void handleAllArchivedRequest(boolean sync) {
    File commandCacheDir =
        InternalConfigurationController.globalInstance().getInternalPersistence()
            .getCommandCacheDir();
    File[] archivedRequests = commandCacheDir.listFiles();
    if (archivedRequests != null && archivedRequests.length > 0) {
      Arrays.sort(archivedRequests, fileModifiedDateComparator);
      for (File file : archivedRequests) {
        if (file.isFile()) {
          handleArchivedRequest(file, sync);
        } else if (InternalConfigurationController.globalInstance().getInternalLogger()
            .showInternalDebugLog()) {
          LogUtil.avlog.e(file.getAbsolutePath() + " is a dir");
        }
      }
    }
  }

  // ================================================================================
  // For Debug
  // ================================================================================

  public void dumpHttpGetRequest(String path, String parameters) {
    String string = "";
    if (parameters != null) {
      string =
          String.format("curl -X GET -H \"%s: %s\" -H \"%s: %s\" -G --data-urlencode \'%s\' %s",
              applicationIdField, InternalConfigurationController.globalInstance()
                  .getAppConfiguration().getApplicationId(), apiKeyField, getDebugClientKey(),
              parameters, path);
    } else {
      string =
          String.format("curl -X GET -H \"%s: %s\" -H \"%s: %s\"  %s", applicationIdField,
              InternalConfigurationController.globalInstance().getAppConfiguration()
                  .getApplicationId(), apiKeyField, getDebugClientKey(), path);
    }
    LogUtil.avlog.d(string);
  }

  private String getDebugClientKey() {
    if (InternalConfigurationController.globalInstance().getInternalLogger().showInternalDebugLog()) {
      return InternalConfigurationController.globalInstance().getAppConfiguration().getClientKey();
    } else {
      return "YourAppKey";
    }
  }

  private String headerString(Map<String, String> header) {
    String string =
        String.format(" -H \"%s: %s\" -H \"%s: %s\" ", applicationIdField,
            InternalConfigurationController.globalInstance().getAppConfiguration()
                .getApplicationId(), apiKeyField, getDebugClientKey());
    StringBuilder sb = new StringBuilder(string);
    if (header != null) {
      for (Map.Entry<String, String> entry : header.entrySet()) {
        String item = String.format(" -H \"%s: %s\" ", entry.getKey(), entry.getValue());
        sb.append(item);
      }
    }
    sb.append(" -H \"Content-Type: application/json\" ");
    return sb.toString();
  }

  public void dumpHttpPutRequest(Map<String, String> header, String path, String object) {
    String string =
        String.format("curl -X PUT %s  -d \' %s \' %s", headerString(header), object, path);
    LogUtil.avlog.d(string);
  }

  public void dumpHttpPostRequest(Map<String, String> header, String path, String object) {
    String string =
        String.format("curl -X POST %s  -d \'%s\' %s", headerString(header), object, path);
    LogUtil.avlog.d(string);
  }

  public void dumpHttpDeleteRequest(Map<String, String> header, String path, String object) {
    String string =
        String.format("curl -X DELETE %s  -d \'%s\' %s", headerString(header), object, path);
    LogUtil.avlog.d(string);
  }

  public void updateHeaderForPath(final String relativePath, AVRequestParams parameters,
      final Map<String, String> header) {
    // if disabled, don't add modify to header so server side will
    // return raw data instead of flag only.
    if (PaasClient.isLastModifyEnabled() && null != header && !AVUtils.isBlankString(relativePath)) {
      final String absoluteURLString = generateQueryPath(relativePath, parameters);
      final String modify = getLastModify(absoluteURLString);
      // double check local cache
      boolean exist =
          InternalConfigurationController.globalInstance().getCache()
              .hasCache(absoluteURLString, modify);
      if (modify != null && exist) {
        header.put("If-Modified-Since", modify);
      }
    }
  }

  public static String getLastModify(final String absolutURLString) {
    if (!PaasClient.isLastModifyEnabled()) {
      return null;
    }
    return lastModify.get(absolutURLString);
  }

  public static boolean isLastModifyEnabled() {
    return lastModifyEnabled;
  }

  public static void setLastModifyEnabled(boolean e) {
    lastModifyEnabled = e;
  }

  public static void clearLastModifyCache() {
    // also clear cache files
    Iterator it = lastModify.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry) it.next();
      InternalConfigurationController.globalInstance().getCache()
          .remove((String) pairs.getKey(), (String) pairs.getValue());
    }
    lastModify.clear();
  }

  static public String lastModifyFromHeaders(Header[] headers) {
    for (Header h : headers) {
      if ("Last-Modified".equalsIgnoreCase(h.name.utf8())) {
        return h.value.utf8();
      }
    }
    return null;
  }

  public static boolean updateLastModify(final String absolutURLString, final String ts) {
    if (!isLastModifyEnabled()) {
      return false;
    }

    if (!AVUtils.isBlankString(ts)) {
      lastModify.put(absolutURLString, ts);
      return true;
    }
    return false;
  }

  public static void removeLastModifyForUrl(final String absolutURLString) {
    lastModify.remove(absolutURLString);
  }

  protected static void registerEventuallyObject(AVObject object) {
    if (object != null) {
      synchronized (object) {
        AVObjectReferenceCount counter = internalObjectsForEventuallySave.get(object.internalId());
        if (counter != null) {
          counter.increment();
        } else {
          counter = new AVObjectReferenceCount(object);
          internalObjectsForEventuallySave.put(object.internalId(), counter);
        }
      }
    }
  }

  protected static void unregisterEvtuallyObject(AVObject object) {
    if (object != null) {
      synchronized (object) {
        AVObjectReferenceCount counter =
            internalObjectsForEventuallySave.get(object.internalId()) == null ? internalObjectsForEventuallySave
                .get(object.internalId()) : internalObjectsForEventuallySave.get(object.getUuid());
        if (counter != null) {
          if (counter.desc() <= 0) {
            internalObjectsForEventuallySave.remove(object.internalId());
            internalObjectsForEventuallySave.remove(object.getUuid());
          }
        }
      }
    }
  }

  private static Comparator<File> fileModifiedDateComparator = new Comparator<File>() {
    @Override
    public int compare(File f, File s) {
      return (int) (f.lastModified() - s.lastModified());
    }
  };

  public static class AVHttpClient {
    OkHttpClient client;
    public static final MediaType JSON = MediaType.parse(defaultContentType);

    public AVHttpClient() {
      client = new OkHttpClient();
      client.setCookieHandler(cookieHandler);
      client.interceptors().addAll(
          InternalConfigurationController.globalInstance().getClientConfiguration()
              .getClientInterceptors());
      if (AVUtils.isAndroid()) {
        client.setDns(DNSAmendNetwork.getInstance());
      }
    }

    public void execute(Request request, boolean sync, ProgressListener progressListener,
        final AsyncHttpResponseHandler handler) {
      Call call = getProgressCall(request, progressListener);
      if (sync) {
        try {
          Response response = call.execute();
          handler.onResponse(response);
        } catch (IOException e) {
          handler.onFailure(request, e);
        }
      } else {
        call.enqueue(handler);
      }
    }

    public void execute(Request request, boolean sync, final AsyncHttpResponseHandler handler) {
      Call call = getCall(request);
      if (sync) {
        try {
          Response response = call.execute();
          handler.onResponse(response);
        } catch (IOException e) {
          handler.onFailure(request, e);
        }
      } else {
        call.enqueue(handler);
      }
    }

    /**
     * OkHttpClient newCall 时会调用 client.copyWithDefaults(); 也就是说 Call 中的 client 实际是 copy 出来的，而且
     * Interceptor 也会 copy 所以这里通过这种方式获得可能更新进度的 Call 而不影响其他 为了避免多线程下调用 client.newCall 而造成影响，所以加了
     * synchronized okhttp 版本为 cn.leancloud.android:okhttp:2.6.0-leancloud
     */
    private synchronized Call getProgressCall(Request request, ProgressListener progressListener) {
      ProgressInterceptor progressInterceptor = new ProgressInterceptor(progressListener);
      client.networkInterceptors().add(progressInterceptor);
      Call call = client.newCall(request);
      client.networkInterceptors().remove(progressInterceptor);
      return call;
    }

    private synchronized Call getCall(Request request) {
      return client.newCall(request);
    }

    public void setConnectTimeout(long networkTimeout, TimeUnit timeUnit) {
      client.setConnectTimeout(networkTimeout, timeUnit);
    }
  }

  public static class ProgressInterceptor implements Interceptor {
    private ProgressListener progressListener;

    public ProgressInterceptor(ProgressListener progressListener) {
      super();
      this.progressListener = progressListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
      Response originalResponse = chain.proceed(chain.request());
      return originalResponse.newBuilder()
          .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
    }
  }

  private static class ProgressResponseBody extends ResponseBody {

    private final ResponseBody responseBody;
    private final ProgressListener progressListener;
    private BufferedSource bufferedSource;

    public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
      this.responseBody = responseBody;
      this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
      return responseBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
      return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() throws IOException {
      if (bufferedSource == null) {
        bufferedSource = Okio.buffer(source(responseBody.source()));
      }
      return bufferedSource;
    }

    private Source source(Source source) {
      return new ForwardingSource(source) {
        long totalBytesRead = 0L;

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
          long bytesRead = super.read(sink, byteCount);
          totalBytesRead += bytesRead != -1 ? bytesRead : 0;
          progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
          return bytesRead;
        }
      };
    }
  }

  public interface ProgressListener {
    void update(long bytesRead, long contentLength, boolean done);
  }
}
