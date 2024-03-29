package com.Gateway_request_analyzer.starter;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.*;
import io.vertx.ext.web.client.WebClient;
import io.vertx.redis.client.*;
import io.vertx.redis.client.RedisOptions;

import java.util.List;


/**
 * Class that starts the application and establishes connections with the database.
 */
public class ServerVerticle extends AbstractVerticle {

  RedisAPI redis;

  RedisConnection pub;
  RedisConnection sub;
  int port;


  /**
   * Start method. Will set up connections with the database for pub/sub and the rate limiter.
   * Creates new instances of RateLimiter and GRAserver.
   */
  @Override
  public void start(){

    retrieveConfig();
    System.out.println("config retrieved");
    CompositeFuture.all(List.of(
      subConnection(vertx),
      pubConnection(vertx)
      )
    ).onComplete(handler -> {
      databaseConnection(vertx);
      RateLimiter rateLimiter = new RateLimiter(this.redis, this.pub, WebClient.create(vertx));
      GRAserver server = new GRAserver(vertx, rateLimiter, this.sub, this.port);

    }).onFailure(error -> {
      System.out.println("Error establishing pub/sub and/or redis connection: " + error.getMessage());
    });
  }

  private void retrieveConfig(){
    ConfigRetriever.create(vertx).getConfig(jsonConfig ->{
      this.port = jsonConfig.result().getInteger("port");
    });
  }

  private RedisOptions clusterOptions(){
    return new RedisOptions()
      .setType(RedisClientType.CLUSTER)
      .addConnectionString("redis://redis-node-1:6380")
      .addConnectionString("redis://redis-node-2:6381")
      .addConnectionString("redis://redis-node-3:6382")
      .addConnectionString("redis://redis-node-4:6383")
      .addConnectionString("redis://redis-node-5:6384")
      .addConnectionString("redis://redis-node-6:6385");
  }


  /**
   * Method for creating a connection with the database for the rate limiter. Initializes the "redis" object
   * with the connection.
   * @param vertx- a vertx object.
   */
  private void databaseConnection(Vertx vertx){

    Redis client = Redis.createClient(vertx, clusterOptions());
    client.connect()
    .onComplete(conn -> {
      System.out.println("Redis established with class: " + conn.result());
      System.out.println("Connection to Redis database established for port: " + port);
    })
    .onFailure(err -> {
        System.out.println("Error in establishing Redis database connection: " + err);
    });
    this.redis = RedisAPI.api(client);
  }

  /**
   * Method for creating pub connection. Initializes the "pub" object
   * with the connection.
   * @param vertx- a vertx object.
   */
  private Future<RedisConnection> pubConnection(Vertx vertx) {
//Redis.createClient(vertx, new RedisOptions().setConnectionString("redis://:123@redis:6379")).connect()
    System.out.println("from pubConnection");
     return Redis.createClient(vertx, clusterOptions()).connect()
       .onSuccess(conn ->{
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
    System.out.println("from subConnection");
    return Redis.createClient(vertx, clusterOptions()).connect()
      .onSuccess( conn ->{
      System.out.println("Connection for subscription established for port: " + this.port);
      this.sub = conn;
    }).onFailure(error -> {
      System.out.println("Connection for subscription establishment failed: " + error.getMessage());
    });
  }

}
