package com.avos.avoscloud;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.Response;

/**
 * Created with IntelliJ IDEA. User: dennis (xzhuang@avos.com) Date: 13-7-26 Time: 下午3:37
 */
public abstract class HttpClientUploader implements Uploader {

  public HttpClientUploader(SaveCallback saveCallback, ProgressCallback progressCallback) {
    this.saveCallback = saveCallback;
    this.progressCallback = progressCallback;
    cancelled = false;
    InternalConfigurationController.globalInstance().getAppConfiguration()
        .setupThreadPoolExecutor(executor);
  }

  SaveCallback saveCallback;
  ProgressCallback progressCallback;
  static OkHttpClient client = new OkHttpClient();

  private volatile boolean cancelled = false;
  private volatile Future future;
  static ThreadPoolExecutor executor;

  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
  private static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;
  private static final long KEEP_ALIVE_TIME = 1L;

  static {
    executor =
        new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());
  }

  protected static synchronized OkHttpClient getOKHttpClient() {
    client.setReadTimeout(30, TimeUnit.SECONDS);
    return client;
  }

  protected Response executeWithRetry(Request request, int retry) throws AVException {
    if (retry > 0 && !isCancelled()) {
      try {
        Response response = getOKHttpClient().newCall(request).execute();
        if (response.code() / 100 == 2) {
          return response;
        } else {
          if (InternalConfigurationController.globalInstance().getInternalLogger()
              .showInternalDebugLog()) {
            LogUtil.avlog.d(AVUtils.stringFromBytes(response.body().bytes()));
          }
          return executeWithRetry(request, retry - 1);
        }
      } catch (IOException e) {
        return executeWithRetry(request, retry - 1);
      }
    } else {
      throw new AVException(AVException.OTHER_CAUSE, "Upload File failure");
    }
  }

  @Override
  public void publishProgress(int progress) {
    if (progressCallback != null)
      progressCallback.internalDone(progress, null);
  }

  @Override
  public void execute() {
    Runnable task = new Runnable() {
      @Override
      public void run() {
        AVException exception = doWork();
        if (!cancelled) {
          if (saveCallback != null) {
            saveCallback.internalDone(exception);
          }
        } else {
          if (saveCallback != null) {
            saveCallback.internalDone(AVErrorUtils.createException(AVException.UNKNOWN,
                "Uploading file task is canceled."));
          }
        }
      }
    };

    future = executor.submit(task);
  }


  // ignore interrupt so far.
  @Override
  public boolean cancel(boolean interrupt) {
    if (cancelled) {
      return false;
    }
    cancelled = true;
    if (interrupt) {
      interruptImmediately();
    } else {
      if (future != null) {
        future.cancel(false);
      }
    }
    return true;
  }

  public void interruptImmediately() {
    if (future != null) {
      // Interrupts worker thread.
      future.cancel(true);
    }
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }
}
