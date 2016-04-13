package com.avos.avoscloud.internal.impl;

import java.io.File;

import com.avos.avoscloud.internal.InternalPersistence;

public class EmptyPersistence implements InternalPersistence {
  private EmptyPersistence() {}

  public static EmptyPersistence instance() {
    synchronized (EmptyLogger.class) {
      if (instance == null) {
        instance = new EmptyPersistence();
      }
    }
    return instance;
  }

  private static EmptyPersistence instance;

  @Override
  public File getPaasDocumentDir() {
    return null;
  }

  @Override
  public File getCacheDir() {
    return null;
  }

  @Override
  public File getCommandCacheDir() {
    return null;
  }

  @Override
  public boolean saveContentToFile(byte[] content, File fileForSave) {
    return false;
  }

  @Override
  public void saveToDocumentDir(String content, String folderName, String fileName) {

  }

  @Override
  public String getFromDocumentDir(String folderName, String fileName) {
    return null;
  }

  @Override
  public String readContentFromFile(File fileForRead) {
    return null;
  }

  @Override
  public byte[] readContentBytesFromFile(File fileForRead) {
    return null;
  }

  @Override
  public void savePersistentSettingBoolean(String keyzone, String key, Boolean value) {

  }

  @Override
  public boolean getPersistentSettingBoolean(String keyzone, String key) {
    return false;
  }

  @Override
  public boolean getPersistentSettingBoolean(String keyzone, String key, Boolean defaultValue) {
    return false;
  }

  @Override
  public void savePersistentSettingInteger(String keyzone, String key, Integer value) {

  }

  @Override
  public Integer getPersistentSettingInteger(String keyzone, String key, Integer defaultValue) {
    return null;
  }

  @Override
  public void savePersistentSettingString(String keyzone, String key, String value) {

  }

  @Override
  public String getPersistentSettingString(String keyzone, String key, String defaultValue) {
    return null;
  }

  @Override
  public void removePersistentSettingString(String keyzone, String key) {

  }

  @Override
  public String removePersistentSettingString(String keyzone, String key, String defaultValue) {
    return null;
  }

  @Override
  public void removeKeyZonePersistentSettings(String keyzone) {

  }

  @Override
  public boolean saveContentToFile(String content, File fileForSave) {
    return false;
  }

  @Override
  public void deleteFile(File file) {

  }

  @Override
  public String getAVFileCachePath() {
    return null;
  }

  @Override
  public File getAVFileCacheFile(String url) {
    return null;
  }
}
