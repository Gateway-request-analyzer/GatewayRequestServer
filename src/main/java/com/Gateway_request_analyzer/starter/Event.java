package com.Gateway_request_analyzer.starter;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonEvent;


public class Event {
  private String ip, userId, session, URI;

  //TODO: toJson, DecodeJson in this class
  //TODO: some cleaning up

  public Event(Buffer msg) {
    JsonObject json = (JsonObject) Json.decodeValue(msg);
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


  public JsonObject toJson(){
    JsonObject jsonEvent = new JsonObject();
    jsonEvent.put("IP", this.ip);
    jsonEvent.put("userId", this.userId);
    jsonEvent.put("session", this.session);
    jsonEvent.put("URI", this.URI);

    return jsonEvent;
  }
}
