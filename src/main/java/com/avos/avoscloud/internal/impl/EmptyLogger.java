package com.avos.avoscloud.internal.impl;

import com.avos.avoscloud.internal.InternalLogger;

public class EmptyLogger extends InternalLogger {
  private EmptyLogger() {}

  public static EmptyLogger instance() {
    synchronized (EmptyLogger.class) {
      if (instance == null) {
        instance = new EmptyLogger();
      }
    }
    return instance;
  }

  private static EmptyLogger instance;

  @Override
  public int v(String tag, String msg) {
    return 0;
  }

  @Override
  public int v(String tag, String msg, Throwable tr) {
    return 0;
  }

  @Override
  public int d(String tag, String msg) {
    return 0;
  }

  @Override
  public int d(String tag, String msg, Throwable tr) {
    return 0;
  }

  @Override
  public int i(String tag, String msg) {
    return 0;
  }

  @Override
  public int i(String tag, String msg, Throwable tr) {
    return 0;
  }

  @Override
  public int w(String tag, String msg) {
    return 0;
  }

  @Override
  public int w(String tag, String msg, Throwable tr) {
    return 0;
  }

  @Override
  public int w(String tag, Throwable tr) {
    return 0;
  }

  @Override
  public int e(String tag, String msg) {
    return 0;
  }

  @Override
  public int e(String tag, String msg, Throwable tr) {
    return 0;
  }
}
