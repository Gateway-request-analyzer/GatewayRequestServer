package com.Gateway_request_analyzer.starter;

import io.vertx.core.AbstractVerticle;

public class GRAVerticle extends AbstractVerticle {


  //Start of solution using eventBus
  @Override
  public void start() {
    vertx.eventBus().consumer("hello.vertx.addr", msg -> {
      msg.reply("Hello!");
    });
    vertx.eventBus().consumer("hello.named.addr", msg -> {
      String name =(String) msg.body();
      msg.reply(String.format("Hello %s!", name));
    });
  }

}
