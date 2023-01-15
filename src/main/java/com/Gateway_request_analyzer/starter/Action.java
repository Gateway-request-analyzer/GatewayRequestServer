package com.Gateway_request_analyzer.starter;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class Action {
  String identifier;
  String action;

  public Action(String pubSubString){
    System.out.println(pubSubString);
    String choppedString = pubSubString.substring(20);
    String[] strings = choppedString.split(" ", 3);
    strings[1] = strings[1].substring(0, strings[1].length() - 1);

    identifier = strings[0];
    action = strings[1];
  }

  public JsonObject toJson(){
    JsonObject json = new JsonObject();
    json.put("identifier", identifier);
    json.put("action", action);

    return json;
  }
}
