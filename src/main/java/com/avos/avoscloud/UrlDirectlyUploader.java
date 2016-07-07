package com.avos.avoscloud;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

class UrlDirectlyUploader extends HttpClientUploader {

  AVFile parseFile;

  protected UrlDirectlyUploader(AVFile parseFile, SaveCallback saveCallback,
                                ProgressCallback progressCallback) {
    super(saveCallback, progressCallback);
    this.parseFile = parseFile;
  }

  @Override
  public AVException doWork() {

    final AVException[] exceptionSaveFile = new AVException[1];
    PaasClient.storageInstance().postObject(AVPowerfulUtils.getEndpoint(parseFile),

        getFileRequestParameters(), true, new GenericObjectCallback() {
          @Override
          public void onSuccess(String content, AVException e) {
            if (e == null) {
              try {
                JSONObject jsonObject = new JSONObject(content);
                parseFile.handleUploadedResponse(jsonObject.getString("objectId"),
                    jsonObject.getString("objectId"), parseFile.getUrl());
                publishProgress(100);
              } catch (Exception ex) {
                exceptionSaveFile[0] = new AVException(ex);
              }
            } else {
              exceptionSaveFile[0] = AVErrorUtils.createException(e, content);
            }
          }

          @Override
          public void onFailure(Throwable error, String content) {
            exceptionSaveFile[0] = AVErrorUtils.createException(error, content);
          }

        });

    return exceptionSaveFile[0];
  }

  private String getFileRequestParameters() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("name", parseFile.getName());
    parameters.put("mime_type", parseFile.mimeType());
    parameters.put("metaData", parseFile.getMetaData());
    parameters.put("__type", AVFile.className());
    parameters.put("url", parseFile.getUrl());
    if (parseFile.getACL() != null) {
      parameters.putAll(AVUtils.getParsedMap(parseFile.getACL().getACLMap()));
    }
    return AVUtils.restfulServerData(parameters);
  }

}
