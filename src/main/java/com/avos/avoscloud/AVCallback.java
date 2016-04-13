package com.avos.avoscloud;

import com.avos.avoscloud.internal.InternalConfigurationController;

/**
 * User: summer Date: 13-4-11 Time: PM3:46
 */
public abstract class AVCallback<T> {
  public void internalDone(final T t, final AVException parseException) {
    InternalConfigurationController.globalInstance().getInternalCallback()
        .internalDone0(this, t, parseException);
  }

  protected boolean mustRunOnUIThread() {
    return true;
  }

  public void internalDone(final AVException parseException) {
    this.internalDone(null, parseException);
  }

  protected abstract void internalDone0(T t, AVException parseException);
}
