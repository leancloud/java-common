package com.avos.avoscloud;

/**
 * 本类是用于Status中的收件箱查询
 * 
 * 增加了一个end属性，来判断是否查询到最老的一页数据
 * 
 * @author lbt05
 * 
 */
public abstract class InboxStatusFindCallback extends FindCallback<AVStatus> {
  boolean end;

  /**
   * 判断是否查询到最老的一页数据
   * 
   * @return 是否已经到最后一页
   */
  public boolean isEnd() {
    return end;
  }

  protected void setEnd(boolean end) {
    this.end = end;
  }
}
