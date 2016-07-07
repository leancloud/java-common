package com.avos.avoscloud;

import com.avos.avoscloud.okhttp.Call;
import com.avos.avoscloud.okhttp.MediaType;
import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.RequestBody;
import com.avos.avoscloud.okhttp.Response;

import java.nio.charset.Charset;

/**
 * Created by summer on 13-5-27.
 */
public class S3Uploader extends HttpClientUploader {
  private volatile Call call;
  private String uploadUrl;
  private AVFile parseFile;

  S3Uploader(AVFile parseFile, String uploadUrl, SaveCallback saveCallback, ProgressCallback progressCallback) {
    super(saveCallback, progressCallback);
    this.uploadUrl = uploadUrl;
    this.parseFile = parseFile;
  }

  @Override
  public AVException doWork() {
    final OkHttpClient httpClient = getOKHttpClient();
    Response response = null;
    String serverResponse = null;
    try {
      byte[] bytes = parseFile.getData();

      // upload to s3
      Request.Builder builder = new Request.Builder();
      builder.url(uploadUrl);
      // ================================================================================
      // setup multi part
      // ================================================================================

      Charset charset = Charset.forName("UTF-8");
      // support file for future

      RequestBody requestBody = RequestBody.create(MediaType.parse(parseFile.mimeType()), bytes);

      builder.put(requestBody);
      builder.addHeader("Content-Type", parseFile.mimeType());

      // Send it
      call = httpClient.newCall(builder.build());

      response = call.execute();
      // The 204 status code implies no response is needed
      if (2 != (response.code() / 100)) {
        serverResponse = AVUtils.stringFromBytes(response.body().bytes());
        LogUtil.avlog.e(serverResponse);
        return AVErrorUtils.createException(AVException.OTHER_CAUSE, "upload file failure:" + response.code());
      }
    } catch (Exception e) {
      return new AVException(e.getCause());
    }
    return null;
  }
}
