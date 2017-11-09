package com.avos.avoscloud;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.avos.avoscloud.internal.InternalLogger;

public class Log4j2Implementation extends InternalLogger {

  private static final Logger logger = LogManager.getLogger("AVOSCloud");

  @Override
  public int v(String tag, String msg) {
    logger.info(msg);
    return 0;
  }

  @Override
  public int v(String tag, String msg, Throwable tr) {
    logger.info(msg, tr);
    return 0;
  }

  @Override
  public int d(String tag, String msg) {
    logger.debug(msg);
    return 0;
  }

  @Override
  public int d(String tag, String msg, Throwable tr) {
    logger.debug(msg, tr);
    return 0;
  }

  @Override
  public int i(String tag, String msg) {
    logger.info(msg);
    return 0;
  }

  @Override
  public int i(String tag, String msg, Throwable tr) {
    logger.info(msg, tr);
    return 0;
  }

  @Override
  public int w(String tag, String msg) {
    logger.warn(msg);
    return 0;
  }

  @Override
  public int w(String tag, String msg, Throwable tr) {
    logger.warn(msg, tr);
    return 0;
  }

  @Override
  public int w(String tag, Throwable tr) {
    logger.warn(tr);
    return 0;
  }

  @Override
  public int e(String tag, String msg) {
    logger.error(msg);
    return 0;
  }

  @Override
  public int e(String tag, String msg, Throwable tr) {
    logger.error(msg, tr);
    return 0;
  }

}
