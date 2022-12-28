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

//TODO: remove class if not used.
public class RedisHandler {

  // socket is used to communicate back on the open connection with client
  //put jedis and other useful globals
  private Event event;
  private RedisAPI redis;
  private int requestCounter = 0;
  //private RedisConnection pubsubConnection;
  RedisConnection pub;

//suggestion for rateLimiter: create new instance of rateLimiter and send redis as the parameter.
// redis = connection to DB we want to work with.
//redis should save actions it takes
  public RedisHandler(RedisAPI redis, RedisConnection pub) {
    this.redis = redis;
    this.pub = pub;

    //suggestion:
    //rateLimiter ratelimiter = new rateLimiter(redis);

  }

  public void eventRequest(Event event){
    this.event = event;
    this.requestCounter++;
    //TODO: do redis stuff with event
    redis.setnx("lel", "s");
    redis.get("lel").onSuccess(lol ->{
      System.out.println(lol);
    });


    //TODO: return appropriate response
    if(this.requestCounter > 9) {
      this.requestCounter = 0;
    }
  }


}

