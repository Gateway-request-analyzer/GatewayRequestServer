package com.Gateway_request_analyzer.starter;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;
import io.vertx.redis.client.impl.RedisClient;

import java.util.HashMap;
import java.util.Map;


public class RedisHandler {

  // socket is used to communicate back on the open connection with client
  //put jedis and other useful globals
  private Event event;
  private RedisAPI redis;
  private int requestCounter = 0;
  //private RedisConnection pubsubConnection;
  Future<RedisConnection> pubFuture;


  public RedisHandler(RedisAPI redis, Future<RedisConnection> pubFuture) {
    this.redis = redis;
    this.pubFuture = pubFuture;

  }

  public void eventRequest(Event event){
    this.event = event;
    this.requestCounter++;
    //TODO: do redis stuff with event
    //redis.setnx("key", "val");
    //redis.get("key").onSuccess(val ->{
      //System.out.println(val);
    //});




    //TODO: return appropriate response
    if(this.requestCounter > 999) {
      this.requestCounter = 0;
      publish(event);
    }
  }

  private void publish(Event event){
     this.pubFuture.onSuccess(conn -> {
       conn.send(Request.cmd(Command.PUBLISH)
           .arg("channel1")
           .arg(event.toJson().toString()))
         .onSuccess(res -> {
           //Published
           //System.out.println("Message successfully published to pub/sub!");

         }).onFailure(err -> {
           System.out.println("Publisher error: " + err.getCause());
         });

     });
  }
}

