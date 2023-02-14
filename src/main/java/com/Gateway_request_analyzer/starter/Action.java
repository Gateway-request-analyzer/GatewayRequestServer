package com.Gateway_request_analyzer.starter;

import io.vertx.core.json.JsonObject;

public class Action {
  JsonObject returnJson = new JsonObject();
  String value;
  String actionType;
  long timeBlocked;
  String publishType;
  String blockSource;

  public Action(String value, String actionType, long timeBlocked, String publishType, String blockSource){
    this.value = value;
    this.actionType = actionType;
    this.timeBlocked = System.currentTimeMillis() + timeBlocked;
    this.publishType = publishType;
    this.blockSource = blockSource;

  }

  public JsonObject toJson(){
    returnJson.put("blockedSource", blockSource);
    returnJson.put("actionType", actionType);
    returnJson.put("blockedTime", String.valueOf(timeBlocked));
    returnJson.put("value", value);
    returnJson.put("publishType", publishType);
    return returnJson;
  }

  public void setPublishType(String publishType){
    this.publishType = publishType;
  }
  public String timeString(){
    return String.valueOf(timeBlocked);
  }
}
