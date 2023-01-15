package com.Gateway_request_analyzer.starter;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;
import java.util.HashMap;

/**
 * A class containing the GRAserver.
 */
public class GRAserver {

  Vertx vertx;
  RateLimiter rateLimiter;
  RedisConnection sub;
  int port;


  private HashMap<String, ServerWebSocket> openConnections = new HashMap<>();

  /**
   * Contructor setting up the server. Calls createServer()
   * @param vertx
   * @param rateLimiter
   * @param sub
   * @param port
   */
  public GRAserver(Vertx vertx, RateLimiter rateLimiter, RedisConnection sub, int port){
    this.vertx = vertx;
    this.rateLimiter = rateLimiter;
    this.sub = sub;
    this.port = port;
    this.createServer();

  }

  /**
   * Method for creating the server
   */
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
        //System.out.println(str);
        System.out.println("Message recieved from pubsub: " + str);

        for(ServerWebSocket socket : openConnections.values()) {
          Action action = new Action(str);
          buf = action.toJson().toBuffer();
          socket.writeBinaryMessage(buf);
        }
      });
  }

}
