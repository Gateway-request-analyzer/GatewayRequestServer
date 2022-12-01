package com.Gateway_request_analyzer.starter;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import redis.clients.jedis.Jedis;

import java.util.Objects;

public class RedisHandler {

  private static Jedis jedis;
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

  public void eventRequest(Event event){
    this.event = event;
    // Connection established to client
    // all the parameters for current event is available in event object
    // prints the IP of the event
    jedis.setnx(event.getIp(), "0");
    jedis.get(event.getIp());

    //TODO: do redis stuff with event

    //this is supposed to be called when notified from pubsub
    GRAserver.notifyClients("OK");
  }

  private void pubsubHandler(){
    // TODO: handle pub/sub redis
    // Might be appropriate to do in a different class

    GRAserver.notifyClients("OK");
  }
}
