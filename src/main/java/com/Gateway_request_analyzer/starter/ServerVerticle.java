package com.Gateway_request_analyzer.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocket;

import java.util.ArrayList;
import java.util.List;

public class ServerVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception{
    RedisHandler redisHandler = new RedisHandler();
    GRAserver server = new GRAserver(vertx, redisHandler);


  }



}
