package com.Gateway_request_analyzer.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class ServerVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception{

    startServer(vertx);


  }

  private void startServer(Vertx vertx){

    vertx.createHttpServer().webSocketHandler(handler -> {
      System.out.println("Client connected: " + handler.textHandlerID());

      vertx.eventBus().consumer("Incoming messages", message -> {
        handler.writeTextMessage( message.toString());
        System.out.println("Hello from server");
      });
      System.out.println("Read between the lines");
      handler.textMessageHandler(msg -> {
        vertx.eventBus().publish("Incoming messages", msg);
      });

      handler.closeHandler(msg -> {
        System.out.println("Client disconnected" + handler.textHandlerID());
      });

    }).listen(3000);

  }

}
