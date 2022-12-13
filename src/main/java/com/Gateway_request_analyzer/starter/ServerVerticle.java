package com.Gateway_request_analyzer.starter;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.*;
import io.vertx.redis.client.*;

import java.lang.module.Configuration;
import java.util.List;

public class ServerVerticle extends AbstractVerticle {


  RedisAPI redis;
  RedisConnection pub;
  RedisConnection sub;
  AsyncResult<RedisConnection> asyncSub;
  AsyncResult<RedisConnection> asyncPub;
  int port;


  @Override
  public void start(){
    retrieveConfig();

    CompositeFuture.all(List.of(
      subConnection(vertx),
      pubConnection(vertx))
    ).onComplete(handler -> {

      System.out.println("Return value: " + this.pub);
     // System.out.println("Sub = " + this.sub);
      RedisHandler redisHandler = new RedisHandler(this.redis, this.pub);
      GRAserver server = new GRAserver(vertx, redisHandler, this.sub, this.port);

      databaseConnection(vertx);
    }).onFailure(error -> {
      System.out.println("Error establishing pub/sub connection: " + error.getMessage());
    });

    /*
    När detta körs måste alla connections vara öppna, kör dessa i en callback
    Tänk på att allt händer asynkront.
     */

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

  private Future<RedisConnection> pubConnection(Vertx vertx) {

     return Redis.createClient(vertx, new RedisOptions()).connect().onSuccess(conn ->{
      System.out.println("Connection for publish established for port: " + this.port);
       this.pub = conn;
     }).onFailure(error -> {
       System.out.println("Pub connection establishment failed: " + error.getMessage());
     });
  }
  private Future<RedisConnection> subConnection(Vertx vertx) {

    return Redis.createClient(vertx, new RedisOptions()).connect().onSuccess( conn ->{
      System.out.println("Connection for subscription established for port: " + this.port);
      this.sub = conn;
    }).onFailure(error -> {
      System.out.println("Connection for subscription establishment fialed: " + error.getMessage());
    });
  }

}
/*
* Deklarera connection i pub/sub connection synkront. Koppla inte för varje.
* Skicka inte vidare en future.
* Vänta tills futures är klara i startmetoden, assignera dom efteråt.
*
* */
