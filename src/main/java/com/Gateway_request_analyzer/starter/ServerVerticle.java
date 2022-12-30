package com.Gateway_request_analyzer.starter;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.*;
import io.vertx.redis.client.*;

import java.lang.module.Configuration;
import java.util.List;


/**
 * Class that starts the application and establishes connections with the database.
 */
public class ServerVerticle extends AbstractVerticle {

  RedisAPI redis;
  RedisConnection pub;
  RedisConnection sub;
  //TODO: remove these two below if not used?
  AsyncResult<RedisConnection> asyncSub;
  AsyncResult<RedisConnection> asyncPub;
  int port;


  /**
   * Start method. Will set up connections with the database for pub/sub and the rate limiter.
   * Creates new instances of RateLimiter and GRAserver.
   */
  @Override
  public void start(){
    retrieveConfig();
    CompositeFuture.all(List.of(
      subConnection(vertx),
      pubConnection(vertx))
    ).onComplete(handler -> {
      databaseConnection(vertx);
      System.out.println("Return value: " + this.pub);
     // System.out.println("Sub = " + this.sub);
      RateLimiter rateLimiter = new RateLimiter(this.redis, this.pub);
      GRAserver server = new GRAserver(vertx, rateLimiter, this.sub, this.port);

    }).onFailure(error -> {
      System.out.println("Error establishing pub/sub and/or redis connection: " + error.getMessage());
    });

    //TODO: remove this below?
    /*
    När detta körs måste alla connections vara öppna, kör dessa i en callback
    Tänk på att allt händer asynkront.
     */

  }
    //TODO: editconfig -> jsonfile : new port
    // Write javadoc for retrieveConfig() method.

  private void retrieveConfig(){
    ConfigRetriever.create(vertx).getConfig(jsonConfig ->{
      this.port = jsonConfig.result().getInteger("port");
    });
  }


  /**
   * Method for creating a connection with the database for the rate limiter. Initializes the "redis" object
   * with the connection.
   * @param vertx- a vertx object.
   */
  private void databaseConnection(Vertx vertx){
    Redis client = Redis.createClient(vertx, new RedisOptions());
    client.connect()
    .onSuccess(conn -> {
      System.out.println("Connection to Redis database established for port: " + port);
    })
    .onFailure(err -> {
        System.out.println("Error in establishing Redis database connection: " + err.getCause());
    });
    this.redis = RedisAPI.api(client);
  }

  /**
   * Method for creating pub connection. Initializes the "pub" object
   * with the connection.
   * @param vertx- a vertx object.
   */
  private Future<RedisConnection> pubConnection(Vertx vertx) {
     return Redis.createClient(vertx, new RedisOptions()).connect().onSuccess(conn ->{
      System.out.println("Connection for publish established for port: " + this.port);
       this.pub = conn;
     }).onFailure(error -> {
       System.out.println("Pub connection establishment failed: " + error.getMessage());
     });
  }

  /**
   * Method for creating sub connection. Initializes the "sub" object
   * with the connection.
   * @param vertx- a vertx object.
   */
  private Future<RedisConnection> subConnection(Vertx vertx) {
    return Redis.createClient(vertx, new RedisOptions()).connect().onSuccess( conn ->{
      System.out.println("Connection for subscription established for port: " + this.port);
      this.sub = conn;
    }).onFailure(error -> {
      System.out.println("Connection for subscription establishment fialed: " + error.getMessage());
    });
  }

}

//TODO: remove this below?
/*
* Deklarera connection i pub/sub connection synkront. Koppla inte för varje.
* Skicka inte vidare en future.
* Vänta tills futures är klara i startmetoden, assignera dom efteråt.
*
* */
