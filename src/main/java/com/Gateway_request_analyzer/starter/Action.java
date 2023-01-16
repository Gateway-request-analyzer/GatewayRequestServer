package com.Gateway_request_analyzer.starter;

import io.vertx.core.json.JsonObject;

public class Action {
  JsonObject returnJson;

  public Action(String pubSubString){
    //Remove the first part of the pub/sub message: "[message, channel1,"
    String choppedString = pubSubString.substring(20);

    //Remove the last bracket: "]"
    choppedString = choppedString.substring(0, choppedString.length() - 1);

    //", 1" if it's the subscription message
    //"block {}" if it's an empty json buffer
    if(!choppedString.equals(", 1") && !choppedString.equals("block {}")) {
      //choppedString is now a valid json string and can be placed inside a JsonObject
      this.returnJson = new JsonObject(choppedString);
    }
  }

  public JsonObject toJson(){
    return returnJson;
  }
}
