package com.Gateway_request_analyzer.starter;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * Class that takes a message containing all request information and converts it into a JsonObject.
 * Each parameter is unpacked and saved as Strings.
 */

public class Event {
  private String ip, userId, session, URI;

  /**
   * Method for unpacking a request into strings for each parameter.
   * @param msg - message received from the gateway
   */
  public Event(Buffer msg) {
    JsonObject json = (JsonObject) Json.decodeValue(msg);
    this.ip = json.getString("ip");
    this.userId = json.getString("userId");
    this.session = json.getString("session");
    this.URI = json.getString("URI");
  }

  /**
   * @return a string containing IP address
   */
  public String getIp() {
    return ip;
  }

  /**
   * @return a string containing session
   */
  public String getSession() {
    return session;
  }

  /**
   * @return a string containing URI
   */
  public String getURI() {
    return URI;
  }

  /**
   * @return a string containing UserId
   */
  public String getUserId() {
    return userId;
  }


  /**
   * Method for packing an event into a JsonObject
   * @return a JsonObject
   */
  public JsonObject toJson(){
    JsonObject jsonEvent = new JsonObject();
    jsonEvent.put("IP", this.ip);
    jsonEvent.put("userId", this.userId);
    jsonEvent.put("session", this.session);
    jsonEvent.put("URI", this.URI);

    return jsonEvent;
  }
}
