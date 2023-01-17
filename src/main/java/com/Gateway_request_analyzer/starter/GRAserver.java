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
    subscriptionSetUp();
    this.createServer();

  }

  /**
   * Method for creating the server
   */
  public void createServer(){
      vertx.createHttpServer().webSocketHandler(handler -> {
        System.out.println("Client " + handler.binaryHandlerID() + " connected to port " + this.port);

        //New client connected, publish list of currently blocked user
        //Currently publishes to ALL connected clients, might need improvement
        //Unnecessary overhead
        rateLimiter.publishBlockedSet();

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

  /**
   * Handles messages published to pub/sub
   *
   * Lets Action-class parse the message into a JsonObject
   * If message does not contain a string on valid JsonFormat, a NullPointerException is thrown
   * Package JsonObject to buffer and send message to all connected clients
   */
  private void subscriptionSetUp(){
    this.sub.send(Request.cmd(Command.SUBSCRIBE).arg("channel1"));
    this.sub.handler(message -> {

      Action action = new Action(message.toString());

      try{
        Buffer buf = action.toJson().toBuffer();
        System.out.println("buffer successful: " + buf);

        for(ServerWebSocket socket : openConnections.values()) {
          socket.writeBinaryMessage(buf);
        }
      }catch(NullPointerException e){
        System.out.println("Not a valid json string");
      }
    });
  }
}
