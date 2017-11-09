package com.avos.avoscloud;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.avos.avoscloud.internal.InternalPersistence;

public class SimplePersistence implements InternalPersistence {

  public File getPaasDocumentDir() {
    return null;
  }

  public File getCacheDir() {
    return null;
  }

  public File getCommandCacheDir() {
    return null;
  }

  public boolean saveContentToFile(String content, File fileForSave) {
    return saveContentToFile(content.getBytes(), fileForSave);
  }

  public boolean saveContentToFile(byte[] content, File fileForSave) {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(fileForSave);
      fos.write(content);
      return true;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      AVUtils.closeQuietly(fos);
    }
  }

  public void saveToDocumentDir(String content, String folderName, String fileName) {
    saveContentToFile(content.getBytes(), new File(folderName, fileName));
  }

  public String getFromDocumentDir(String folderName, String fileName) {
    return readContentFromFile(new File(folderName, fileName));
  }

  public String readContentFromFile(File fileForRead) {
    return new String(readContentBytesFromFile(fileForRead));
  }

  public byte[] readContentBytesFromFile(File fileForRead) {
    byte[] buffer = null;
    FileInputStream fis = null;
    ByteArrayOutputStream bos = null;
    try {
      fis = new FileInputStream(fileForRead);
      bos = new ByteArrayOutputStream();
      byte[] b = new byte[1024 * 5];
      int n;
      while ((n = fis.read(b)) != -1) {
        bos.write(b, 0, n);
      }
      buffer = bos.toByteArray();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      AVUtils.closeQuietly(fis);
      AVUtils.closeQuietly(bos);
    }
    return buffer;
  }

  public void deleteFile(File file) {
    try {
      file.deleteOnExit();
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  public void savePersistentSettingBoolean(String keyzone, String key, Boolean value) {

  }

  public boolean getPersistentSettingBoolean(String keyzone, String key) {
    return false;
  }

  public boolean getPersistentSettingBoolean(String keyzone, String key, Boolean defaultValue) {
    return defaultValue;
  }

  public void savePersistentSettingInteger(String keyzone, String key, Integer value) {
  }

  public Integer getPersistentSettingInteger(String keyzone, String key, Integer defaultValue) {
    return defaultValue;
  }

  public Long getPersistentSettingLong(String keyzone, String key, Long defaultValue) {
    return defaultValue;
  }

  public void savePersistentSettingLong(String keyzone, String key, Long value) {
  }

  public void savePersistentSettingString(String keyzone, String key, String value) {

  }

  public String getPersistentSettingString(String keyzone, String key, String defaultValue) {
    return defaultValue;
  }

  public void removePersistentSettingString(String keyzone, String key) {

  }

  public String removePersistentSettingString(String keyzone, String key, String defaultValue) {
    return null;
  }

  public void removeKeyZonePersistentSettings(String keyzone) {

  }

  public String getAVFileCachePath() {
    return null;
  }

  public File getAVFileCacheFile(String url) {
    return null;
  }

  public void cleanAVFileCache(int days) {
  }

  private AVUser currentUser;

  public void setCurrentUser(AVUser user, boolean clean) {
    this.currentUser = user;
  }

  public <T extends AVUser> T getCurrentUser(Class<T> userClass) {
    if (currentUser != null) {
      return (T) AVUser.cast(currentUser, userClass);
    } else {
      return null;
    }

  }

}
