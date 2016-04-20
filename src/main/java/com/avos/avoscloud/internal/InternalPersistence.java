package com.avos.avoscloud.internal;

import java.io.File;

public interface InternalPersistence {
  public File getPaasDocumentDir();

  public File getCacheDir();

  public File getCommandCacheDir();

  public boolean saveContentToFile(String content, File fileForSave);

  public boolean saveContentToFile(byte[] content, File fileForSave);

  public void saveToDocumentDir(String content, String folderName, String fileName);

  public String getFromDocumentDir(String folderName, String fileName);

  public String readContentFromFile(File fileForRead);

  public byte[] readContentBytesFromFile(File fileForRead);

  public void deleteFile(File file);

  public void savePersistentSettingBoolean(String keyzone, String key, Boolean value);

  public boolean getPersistentSettingBoolean(String keyzone, String key);

  public boolean getPersistentSettingBoolean(String keyzone, String key, Boolean defaultValue);

  public void savePersistentSettingInteger(String keyzone, String key, Integer value);

  public Integer getPersistentSettingInteger(String keyzone, String key, Integer defaultValue);

  public void savePersistentSettingString(String keyzone, String key, String value);

  public String getPersistentSettingString(String keyzone, String key, String defaultValue);

  public void removePersistentSettingString(String keyzone, String key);

  public String removePersistentSettingString(String keyzone, String key, String defaultValue);

  public void removeKeyZonePersistentSettings(String keyzone);

  public String getAVFileCachePath();

  public File getAVFileCacheFile(String url);
  
  public void cleanAVFileCache(int days);
}
