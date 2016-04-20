package com.avos.avoscloud;

import com.avos.avoscloud.internal.InternalConfigurationController;

/**
 * User: summer Date: 13-4-11 Time: PM3:46
 */
public abstract class AVCallback<T> {
  public void internalDone(final T t, final AVException parseException) {
    if (mustRunOnUIThread()
        && !InternalConfigurationController.globalInstance().getInternalCallback().isMainThread()) {
      InternalConfigurationController.globalInstance().getInternalCallback()
          .internalDoneInMainThread(this, t, parseException);
    } else {
      InternalConfigurationController.globalInstance().getInternalCallback()
          .internalDoneInCurrentThread(this, t, parseException);
    }
  }

  protected boolean mustRunOnUIThread() {
    return true;
  }

  public void internalDone(final AVException parseException) {
    this.internalDone(null, parseException);
  }

  /*
   * 请仔细检查所有调用到internalDone0的地方，以免出现调用线程错乱的问题，导致回调不在线程的异常
   */
  protected abstract void internalDone0(T t, AVException parseException);
}
