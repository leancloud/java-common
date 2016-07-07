package com.avos.avoscloud;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import java.util.HashMap;
import java.util.Map;

import android.util.SparseArray;

/**
 * User: summer Date: 13-4-16 Time: AM10:43
 */
class FileUploader extends HttpClientUploader {
  protected AVFile parseFile;

  static final int PROGRESS_GET_TOKEN = 10;
  static final int PROGRESS_UPLOAD_FILE = 90;
  static final int PROGRESS_COMPLETE = 100;

  private String token;
  private String url;
  private String objectId;
  private String bucket;
  private String uploadUrl;
  private String provider;

  protected static final int defaultFileKeyLength = 40;

  protected FileUploader(AVFile parseFile, SaveCallback saveCallback,
                         ProgressCallback progressCallback) {
    super(saveCallback, progressCallback);
    this.parseFile = parseFile;
  }

  public AVException doWork() {
    // fileKey 是随机值，在 fileTokens 请求与真正的 upload 请求时都会用到，这里要保证是同一个值
    String fileKey = AVUtils.parseFileKey(parseFile.getName());
    if (AVUtils.isBlankString(uploadUrl)) {
      AVException getBucketException = fetchUploadBucket("fileTokens", fileKey, true, new AVCallback<String>() {
        @Override
        protected void internalDone0(String s, AVException parseException) {
          if (null == parseException) {
            handleGetBucketResponse(s);
          }
        }
      });
      if (getBucketException != null) {
        return getBucketException;
      }
    }
    publishProgress(PROGRESS_GET_TOKEN);
    Uploader uploader = getUploaderImplementation(fileKey);

    AVException uploadException = uploader.doWork();
    if (uploadException == null) {
      parseFile.handleUploadedResponse(objectId, objectId, url);
      publishProgress(PROGRESS_COMPLETE);
      return null;
    } else {
      destroyFileObject(objectId);
      return uploadException;
    }
  }


  private Uploader getUploaderImplementation(String fileKey) {
    switch (provider) {
      case "qcloud":
        return new QCloudUploader(parseFile, fileKey, token, uploadUrl, saveCallback, progressCallback);
      case "s3":
        return new S3Uploader(parseFile, uploadUrl, saveCallback, progressCallback);
      default:
        return new QiniuUploader(parseFile, token, fileKey, saveCallback, progressCallback);
    }

  }

  private AVException fetchUploadBucket(String path, String fileKey, boolean sync, final AVCallback<String> callback) {
    final AVException[] exceptionWhenGetBucket = new AVException[1];
    PaasClient.storageInstance().postObject(path, getGetBucketParameters(fileKey), sync,
        new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            callback.internalDone0(content, e);
            exceptionWhenGetBucket[0] = e;
          }

          @Override
          public void onFailure(Throwable error, String content) {
            callback.internalDone0(null, AVErrorUtils.createException(error, content));
            exceptionWhenGetBucket[0] = AVErrorUtils.createException(error, content);
          }
        });
    if (null != exceptionWhenGetBucket[0]) {
      return exceptionWhenGetBucket[0];
    }
    return null;
  }

  private AVException handleGetBucketResponse(String responseStr) {
    if (!AVUtils.isBlankContent(responseStr)) {
      try {
        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(responseStr);
        this.bucket = jsonObject.getString("bucket");
        this.objectId = jsonObject.getString("objectId");
        this.uploadUrl = jsonObject.getString("upload_url");
        this.provider = jsonObject.getString("provider");
        this.token = jsonObject.getString("token");
        url = jsonObject.getString("url");
      } catch (JSONException e) {
        return new AVException(e);
      }
    }
    return null;
  }

  private String getGetBucketParameters(String fileKey) {
    Map<String, Object> parameters = new HashMap<String, Object>(3);
    parameters.put("key",  fileKey);
    parameters.put("name", parseFile.getName());
    parameters.put("mime_type", parseFile.mimeType());
    parameters.put("metaData", parseFile.getMetaData());
    parameters.put("__type", AVFile.className());
    if (parseFile.getACL() != null) {
      parameters.putAll(AVUtils.getParsedMap(parseFile.getACL().getACLMap()));
    }
    return AVUtils.restfulServerData(parameters);
  }


  private void destroyFileObject(String objectId) {
    if (!AVUtils.isBlankString(objectId)) {
      try {
        AVObject fileObject = AVObject.createWithoutData("_File", objectId);
        fileObject.deleteInBackground(new DeleteCallback() {

          @Override
          public void done(AVException e) {
          }
        });
      } catch (Exception e) {
        // ignore
      }
    }
  }

  protected static class ProgressCalculator {
    SparseArray<Integer> blockProgress = new SparseArray<Integer>();
    FileUploadProgressCallback callback;
    int fileBlockCount = 0;

    public ProgressCalculator(int blockCount, FileUploadProgressCallback callback) {
      this.callback = callback;
      this.fileBlockCount = blockCount;
    }

    public synchronized void publishProgress(int offset, int progress) {
      blockProgress.put(offset, progress);
      if (callback != null) {
        int progressSum = 0;
        for (int index = 0; index < blockProgress.size(); index++) {
          progressSum += blockProgress.valueAt(index);
        }
        callback.onProgress(PROGRESS_GET_TOKEN + (PROGRESS_UPLOAD_FILE - PROGRESS_GET_TOKEN)
            * progressSum / (100 * fileBlockCount));
      }
    }
  }

  public interface FileUploadProgressCallback {
    void onProgress(int progress);
  }
}
