package com.Gateway_request_analyzer.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.redis.client.*;
public class ServerVerticle extends AbstractVerticle {


  RedisAPI redis;
  Future<RedisConnection> pub;
  Future<RedisConnection> sub;


  @Override
  public void start(){
    subConnection(vertx);
    pubConnection(vertx);
    databaseConnection(vertx);

    RedisHandler redisHandler = new RedisHandler(this.redis, this.pub);
    GRAserver server = new GRAserver(vertx, redisHandler, this.sub);
  }

  private void databaseConnection(Vertx vertx){

    Redis client = Redis.createClient(vertx, new RedisOptions());
    client.connect()
    .onSuccess(conn -> {
      System.out.println("Connection to Redis database established!");
    })
    .onFailure(err -> {
        System.out.println("Error in establishing Redis database connection: " + err.getCause());
    });

    this.redis = RedisAPI.api(client);
  }

  private void pubConnection(Vertx vertx) {

    this.pub = Redis.createClient(vertx, new RedisOptions()).connect().onSuccess(msg ->{
      System.out.println("Connection for publish established!");
    });
  }
  private void subConnection(Vertx vertx) {

    this.sub = Redis.createClient(vertx, new RedisOptions()).connect().onSuccess( msg ->{
      System.out.println("Connection for subscription established!");
    });
  }

}
