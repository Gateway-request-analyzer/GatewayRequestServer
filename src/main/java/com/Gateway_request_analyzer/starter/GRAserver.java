package com.Gateway_request_analyzer.starter;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class GRAserver {

  Vertx vertx;
  RedisHandler redisHandler;
  ServerWebSocket socket;

  public GRAserver(Vertx vertx){
    this.vertx = vertx;
    //change: we send the vertx to RedisHandler
    this.redisHandler = new RedisHandler(vertx);
    this.createServer();
  }

  public void createServer(){

    vertx.createHttpServer().webSocketHandler(handler -> {
      System.out.println("Client connected: " + handler.textHandlerID());

      socket = handler;

      handler.binaryMessageHandler(msg -> {
        JsonObject json = (JsonObject) Json.decodeValue(msg);
        Event event = new Event(json);
        //send event to redisHandler
        redisHandler.eventRequest(event, socket);
      });

      handler.closeHandler(msg -> {
        System.out.println("Client disconnected" + handler.textHandlerID());
      });
      handler.end();

    }).listen(3000).onSuccess(err -> {
      System.out.println("Connection succeeded");
    }).onFailure(err -> {
      System.out.println("Connection refused");
    });
  }

}
