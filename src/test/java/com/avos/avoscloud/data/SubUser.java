package com.avos.avoscloud.data;

import com.alibaba.fastjson.annotation.JSONType;
import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;

@JSONType(ignores = {"blackListRelation"}, asm = false)
@AVClassName("SubUser")
public class SubUser extends AVUser {
  public AVObject getArmor() {
    return getAVObject("armor");
  }

  public void setArmor(AVObject armor) {
    this.put("armor", armor);
  }

  public void setNickName(String name) {
    this.put("nickName", name);
  }

  public String getNickName() {
    return this.getString("nickName");
  }

  public AVRelation<AVObject> getBlackListRelation() {
    return this.getRelation("blacklist");
  }
}
