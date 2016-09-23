package com.avos.avoscloud;

/**
 * AVDeleteOption is a option value for AVObject delete operation
 */
public class AVDeleteOption {
  AVQuery matchQuery;

  /**
   * Only delete object when query matches AVObject instance data
   *
   * @param query 用于匹配当前对象，只有满足才删除当前对象
   * @return AVDeleteOption 返回对象本身
   */

  public AVDeleteOption query(AVQuery query) {
    this.matchQuery = query;
    return this;
  }
}
