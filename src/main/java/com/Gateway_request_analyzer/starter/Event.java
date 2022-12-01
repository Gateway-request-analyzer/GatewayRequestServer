package com.Gateway_request_analyzer.starter;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;


public class Event {
  private String ip, userId, session, URI;

  //toJson, DecodeJson in this class

  public Event(JsonObject json) {
    this.ip = json.getString("ip");
    this.userId = json.getString("userId");
    this.session = json.getString("session");
    this.URI = json.getString("URI");
  }

  public String getIp() {
    return ip;
  }

  public String getSession() {
    return session;
  }

  public String getURI() {
    return URI;
  }

  public String getUserId() {
    return userId;
  }
}
