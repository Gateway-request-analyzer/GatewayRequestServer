package com.Gateway_request_analyzer.starter;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import redis.clients.jedis.Jedis;

import java.util.Objects;

public class RedisHandler {

  private static Jedis jedis;
  // socket is used to communicate back on the open connection with client
  private ServerWebSocket socket;
  //put jedis and other useful globals
  private Event event;

  public RedisHandler() {
    //set up jedis connection with port 6379
    try {
      this.jedis = new Jedis("http://localhost:6379/");
    } catch (Exception e ){
      e.printStackTrace();
    }
  }

  public void eventRequest(Event event, ServerWebSocket socket){
    this.event = event;
    // Connection established to client
    this.socket = socket;

    // all the parameters for current event is available in event object
    // prints the IP of the event
    jedis.setnx(event.getIp(), "0");
    jedis.get(event.getIp());

    //TODO: do redis stuff with event

    //TODO: return appropriate response

    serverResponse("OK");
  }

  private void serverResponse(String response){
    if(Objects.equals(response, "OK")){

      System.out.println("Request was valid");
      System.out.println("No action taken");

      //creates JsonObject to be returned to client
      JsonObject json = new JsonObject();
      json.put("Action", "None");

      Buffer buf = Json.encodeToBuffer(json);
      socket.writeBinaryMessage(buf);

    }else {
      JsonObject json = new JsonObject();
      json.put("Action", response);

      Buffer buf = Json.encodeToBuffer(json);
      socket.writeBinaryMessage(buf);
    }
  }

  private void pubsubHandler(){
    // TODO: handle pub/sub redis
    // Might be appropriate to do in a different class

  }



}