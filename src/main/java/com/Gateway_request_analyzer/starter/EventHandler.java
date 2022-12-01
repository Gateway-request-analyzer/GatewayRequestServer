package com.Gateway_request_analyzer.starter;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;

public class EventHandler {
  private Vertx vertx;
  private HttpClient client;

  void EventHandler(Vertx vertx){
    this.vertx = vertx;
    this.client = vertx.createHttpClient();
  }

}
