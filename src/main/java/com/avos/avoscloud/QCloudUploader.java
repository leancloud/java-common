package com.avos.avoscloud;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.okhttp.MediaType;
import com.avos.avoscloud.okhttp.MultipartBuilder;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.RequestBody;
import com.avos.avoscloud.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * Created by lbt05 on 4/20/16.
 */
class QCloudUploader extends HttpClientUploader {

  private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
  private static final String FILE_CONTENT = "filecontent";
  private static final String PARAM_OP = "op";
  private static final String PARAM_SHA = "sha";
  private static final String MULTIPART_FORM_DATA = "multipart/form-data";
  private static final String HEADER_AUTHORIZATION = "Authorization";
  private static final String HEADER_CONTENT_TYPE = "Content-Type";
  private static final String PARAM_FILE_SIZE = "filesize";
  private static final String PARAM_SLICE_SIZE = "slice_size";
  private static final String PARAM_OFFSET = "offset";
  private static final String PARAM_SESSION = "session";
  private static final String OP_UPLOAD_SLICE = "upload_slice";
  private static final String OP_UPLOAD = "upload";
  private static final String PARAM_ACCESS_URL = "access_url";
  private static final int RETRY_TIMES = 5;


  private volatile Future[] tasks;
  boolean SHOULD_UPLOAD_SLICE_PARALL = false;
  private String fileSha;
  private String uploadUrl;
  private String fileKey;
  private String token;
  private AVFile parseFile;

  protected QCloudUploader(AVFile parseFile, String fileKey, String token, String uploadUrl,
      SaveCallback saveCallback, ProgressCallback progressCallback) {
    super(saveCallback, progressCallback);
    this.parseFile = parseFile;
    this.uploadUrl = uploadUrl;
    this.token = token;
    this.fileKey = fileKey;
  }

  private static final int DEFAULT_SLICE_LEN = 512 * 1024;

  @Override
  public AVException doWork() {
    try {

      byte[] bytes = parseFile.getData();
      int sliceCount =
          (bytes.length / DEFAULT_SLICE_LEN) + (bytes.length % DEFAULT_SLICE_LEN == 0 ? 0 : 1);
      // 如果文件太小就没必要分片了
      if (sliceCount > 1) {
        JSONObject result = uploadControlSlice(token, uploadUrl, bytes);
        if (result.containsKey(PARAM_ACCESS_URL)) {
          return null;
        }
        String sessionId = result.getString("session");

        FileUploader.ProgressCalculator progressCalculator =
            new FileUploader.ProgressCalculator(sliceCount,
                new FileUploader.FileUploadProgressCallback() {
                  @Override
                  public void onProgress(int progress) {
                    publishProgress(progress);
                  }
                });
        if (SHOULD_UPLOAD_SLICE_PARALL) {
          // QCloud那边现在对于并行上传分片的支持还没有完成,暂时先通过boolean值来屏蔽这个功能
          CountDownLatch latch = new CountDownLatch(sliceCount);
          tasks = new Future[sliceCount];
          synchronized (tasks) {
            for (int sliceOffset = 0; sliceOffset < sliceCount; sliceOffset++) {
              tasks[sliceOffset - 1] =
                  executor.submit(new SliceUploadTask(this, fileKey, token, uploadUrl, bytes,
                      sliceOffset, sessionId, progressCalculator, latch));
            }
          }
          latch.await();
        } else {
          for (int sliceOffset = 0; sliceOffset < sliceCount && !AVExceptionHolder.exists(); sliceOffset++) {
            new SliceUploadTask(this, fileKey, token, uploadUrl, bytes, sliceOffset, sessionId,
                progressCalculator, null).upload();
          }
        }
        if (AVExceptionHolder.exists()) {
          if (tasks != null) {
            for (Future task : tasks) {
              if (!task.isDone()) {
                task.cancel(true);
              }
            }
          }
          throw AVExceptionHolder.remove();
        }
      } else {
        uploadFile();
      }
    } catch (Exception e) {
      return new AVException(e);
    }

    return null;
  }

  private void uploadFile() throws AVException {

    try {
      if (InternalConfigurationController.globalInstance().getInternalLogger()
          .showInternalDebugLog()) {
        LogUtil.log.d("upload as whole file");
      }
      byte[] bytes = parseFile.getData();
      fileSha = AVUtils.SHA1(bytes);
      MultipartBuilder builder = new MultipartBuilder();
      RequestBody fileBody =
          RequestBody.create(MediaType.parse(APPLICATION_OCTET_STREAM), bytes, 0,
              getCurrentSliceLength(0, bytes.length));
      builder.addFormDataPart(FILE_CONTENT, fileKey, fileBody);
      builder.addFormDataPart(PARAM_OP, OP_UPLOAD);
      builder.addFormDataPart(PARAM_SHA, fileSha);
      builder.type(MediaType.parse(MULTIPART_FORM_DATA));

      Request.Builder requestBuilder = new Request.Builder();
      requestBuilder.url(uploadUrl);
      requestBuilder.header(HEADER_AUTHORIZATION, token);
      requestBuilder.header(HEADER_CONTENT_TYPE, MULTIPART_FORM_DATA);
      requestBuilder.post(builder.build());
      try {
        Request request = requestBuilder.build();
        Response response = executeWithRetry(request, RETRY_TIMES);
        if (response.code() != 200) {
          throw AVErrorUtils.createException(AVException.OTHER_CAUSE,
              AVUtils.stringFromBytes(response.body().bytes()));
        }
      } catch (IOException e) {
        throw AVErrorUtils.createException(e, "Exception during file upload");
      }

    } catch (AVException e) {
      throw e;
    } catch (NoSuchAlgorithmException e) {
      throw AVErrorUtils.createException(e, "Exception during file upload");
    } catch (UnsupportedEncodingException e) {
      throw AVErrorUtils.createException(e, "Exception during file upload");
    }
  }

  protected static JSONObject parseSliceUploadResponse(String resp) {
    com.alibaba.fastjson.JSONObject object = JSON.parseObject(resp);
    com.alibaba.fastjson.JSONObject data = object.getJSONObject("data");
    return data;
  }

  protected static int getCurrentSliceLength(int sliceCount, int totalSize) {
    int leftSize = totalSize - sliceCount * DEFAULT_SLICE_LEN;
    return leftSize >= DEFAULT_SLICE_LEN ? DEFAULT_SLICE_LEN : leftSize;
  }

  private JSONObject uploadControlSlice(String token, String url, byte[] wholeFile)
      throws AVException {
    MultipartBuilder builder = new MultipartBuilder();
    try {
      String fileSha = AVUtils.SHA1(wholeFile);
      builder.addFormDataPart(PARAM_SHA, fileSha);
      builder.addFormDataPart(PARAM_OP, OP_UPLOAD_SLICE);
      builder.addFormDataPart(PARAM_FILE_SIZE, String.valueOf(wholeFile.length));
      builder.addFormDataPart(PARAM_SLICE_SIZE, String.valueOf(DEFAULT_SLICE_LEN));
      builder.type(MediaType.parse(MULTIPART_FORM_DATA));

      Request.Builder requestBuilder = new Request.Builder();
      requestBuilder.url(url);
      requestBuilder.header(HEADER_AUTHORIZATION, token);
      requestBuilder.header(HEADER_CONTENT_TYPE, MULTIPART_FORM_DATA);
      requestBuilder.post(builder.build());

      Request request = requestBuilder.build();
      Response response = executeWithRetry(request, RETRY_TIMES);
      if (response != null) {
        byte[] responseBody = response.body().bytes();
        return parseSliceUploadResponse(AVUtils.stringFromBytes(responseBody));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new AVException(AVException.OTHER_CAUSE, "Upload file failure");
    }
    return null;
  }

  public static class SliceUploadTask implements Runnable {
    byte[] data;
    int sliceOffset;
    FileUploader.ProgressCalculator progress;
    String session;
    CountDownLatch latch;
    String key;
    String token;
    String url;
    QCloudUploader parent;

    public SliceUploadTask(QCloudUploader parent, String key, String token, String url,
        byte[] wholeFile, int sliceOffset, String session,
        FileUploader.ProgressCalculator progressCalculator, CountDownLatch latch) {
      this.data = wholeFile;
      this.sliceOffset = sliceOffset;
      this.progress = progressCalculator;
      this.session = session;
      this.latch = latch;
      this.key = key;
      this.token = token;
      this.url = url;
      this.parent = parent;
    }

    @Override
    public void run() {
      this.upload();
    }

    public String upload() {
      try {
        MultipartBuilder builder = new MultipartBuilder();
        RequestBody fileBody =
            RequestBody.create(MediaType.parse(APPLICATION_OCTET_STREAM), data, sliceOffset
                * DEFAULT_SLICE_LEN, getCurrentSliceLength(sliceOffset, data.length));
        builder.addFormDataPart(FILE_CONTENT, key, fileBody);
        builder.addFormDataPart(PARAM_OP, OP_UPLOAD_SLICE);
        builder.addFormDataPart(PARAM_OFFSET, String.valueOf(sliceOffset * DEFAULT_SLICE_LEN));
        builder.addFormDataPart(PARAM_SESSION, session);
        builder.type(MediaType.parse(MULTIPART_FORM_DATA));

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        requestBuilder.header(HEADER_AUTHORIZATION, token);
        requestBuilder.header(HEADER_CONTENT_TYPE, MULTIPART_FORM_DATA);
        requestBuilder.post(builder.build());
        Request request = requestBuilder.build();
        Response response = parent.executeWithRetry(request, RETRY_TIMES);
        if (response != null) {
          byte[] responseBody = response.body().bytes();
          if (progress != null) {
            progress.publishProgress(sliceOffset, 100);
          }
          return AVUtils.stringFromBytes(responseBody);
        }
      } catch (Exception e) {
        AVExceptionHolder.add(new AVException(e));
        if (latch != null) {
          long count = latch.getCount();
          for (; count > 0; count--) {
            latch.countDown();
          }
        }
      }
      return null;
    }
  }

  @Override
  public void interruptImmediately() {
    super.interruptImmediately();

    if (tasks != null && tasks.length > 0) {
      synchronized (tasks) {
        for (int index = 0; index < tasks.length; index++) {
          Future task = tasks[index];
          if (task != null && !task.isDone() && !task.isCancelled()) {
            task.cancel(true);
          }
        }
      }
    }
  }
}
