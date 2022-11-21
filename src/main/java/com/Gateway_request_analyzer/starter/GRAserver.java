package com.Gateway_request_analyzer.starter;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class GRAserver {

  Vertx vertx;

  public GRAserver(Vertx vertx){
    this.vertx = vertx;
    this.createServer();
  }

  public void createServer(){


    vertx.createHttpServer().webSocketHandler(handler -> {
      System.out.println("Client connected: " + handler.textHandlerID());


      handler.binaryMessageHandler(msg -> {
        JsonObject json = (JsonObject) Json.decodeValue(msg);

        Event event = new Event(vertx, json);
        event.handleRequest();

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
