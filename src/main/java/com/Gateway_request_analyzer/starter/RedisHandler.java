package com.Gateway_request_analyzer.starter;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.impl.RedisClient;
import redis.clients.jedis.Jedis;

import java.util.Objects;

public class RedisHandler {

  // socket is used to communicate back on the open connection with client
  private ServerWebSocket socket;
  //put jedis and other useful globals
  private Event event;
  //will not use jedis, deprecate this later
  private static Jedis jedis;
  private Redis redis;

  public RedisHandler(Vertx vertx) {
    //set up redis client to connect with port 6379
    try {
      Redis.createClient(vertx)
        .connect()
        .onSuccess(conn -> {
          System.out.println("SUCCESSSS!");
          //should we do Redis things here? After connection is established?
        });
    } catch (Exception e) {
      System.out.println("Could not connect.");
    }

  }

  public void eventRequest(Event event, ServerWebSocket socket){
    this.event = event;
    // Connection established to client
    this.socket = socket;

    // all the parameters for current event is available in event object
    // prints the IP of the event
    //jedis.setnx(event.getIp(), "0");
    //jedis.get(event.getIp());

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
