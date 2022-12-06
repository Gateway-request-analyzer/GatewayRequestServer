package com.Gateway_request_analyzer.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.redis.client.*;

import java.lang.module.Configuration;

public class ServerVerticle extends AbstractVerticle {


  RedisAPI redis;
  Future<RedisConnection> pub;
  Future<RedisConnection> sub;


  @Override
  public void start(){
    retrieveConfig();
    subConnection(vertx);
    pubConnection(vertx);
    databaseConnection(vertx);

    RedisHandler redisHandler = new RedisHandler(this.redis, this.pub);
    GRAserver server = new GRAserver(vertx, redisHandler, this.sub, this.port);
  }
    //TODO: editconfig -> jsonfile : new port

  private void retrieveConfig(){
    ConfigRetriever.create(vertx).getConfig(jsonConfig ->{
      this.port = jsonConfig.result().getInteger("port");
    });
  }
  private void databaseConnection(Vertx vertx){

    Redis client = Redis.createClient(vertx, new RedisOptions());
    client.connect()
    .onSuccess(conn -> {

      System.out.println("Connection to Redis database established for port: " +
        port);

    })
    .onFailure(err -> {
        System.out.println("Error in establishing Redis database connection: " + err.getCause());
    });

    this.redis = RedisAPI.api(client);
  }

  private void pubConnection(Vertx vertx) {

    this.pub = Redis.createClient(vertx, new RedisOptions()).connect().onSuccess(msg ->{
      System.out.println("Connection for publish established for port: " + this.port);
    });
  }
  private void subConnection(Vertx vertx) {

    this.sub = Redis.createClient(vertx, new RedisOptions()).connect().onSuccess( msg ->{
      System.out.println("Connection for subscription established for port: " + this.port);
    });
  }

}
