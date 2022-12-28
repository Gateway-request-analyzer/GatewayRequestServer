package com.Gateway_request_analyzer.starter;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;
import io.vertx.redis.client.impl.RedisClient;

import java.util.HashMap;
import java.util.Objects;

//TODO: write javadoc for class
public class GRAserver {

  Vertx vertx;
  RateLimiter rateLimiter;
  RedisConnection sub;
  int port;

  //Make this a HashMap
  private HashMap<String, ServerWebSocket> openConnections = new HashMap<>();

  public GRAserver(Vertx vertx, RateLimiter rateLimiter, RedisConnection sub, int port){
    this.vertx = vertx;
    this.rateLimiter = rateLimiter;
    this.sub = sub;
    this.port = port;
    this.createServer();

  }

  public void createServer(){

    subscriptionSetUp();

      vertx.createHttpServer().webSocketHandler(handler -> {
        System.out.println("Client " + handler.binaryHandlerID() + " connected to port " + this.port);

        //socket = handler
        openConnections.put(handler.binaryHandlerID(), handler);

        handler.binaryMessageHandler(msg -> {
          Event event = new Event(msg);
          rateLimiter.unpackEvent(event);
        });
        handler.closeHandler(msg -> {
          openConnections.remove(handler.binaryHandlerID());
          System.out.println("Client " + handler.binaryHandlerID() + " disconnected from port " + this.port);
        });

      }).listen(this.port).onSuccess(err -> {
        System.out.println("Connection to port " + this.port + " succeeded");
      }).onFailure(err -> {
        System.out.println("Connection to port " + this.port + " refused");
      });

  }

  private void subscriptionSetUp(){
      this.sub.send(Request.cmd(Command.SUBSCRIBE).arg("channel1"));
      this.sub.handler(message -> {
        Buffer buf;
        String str = message.toString();
        System.out.println(str);
        System.out.println("Message recieved from pubsub: " + str);

        for(ServerWebSocket socket : openConnections.values()) {
          JsonObject json = new JsonObject();
          json.put("Action", str);
          buf = json.toBuffer();
          socket.writeBinaryMessage(buf);
        }
      });
  }

}
