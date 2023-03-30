package com.Gateway_request_analyzer.starter;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;
import io.vertx.redis.client.impl.types.Multi;

import java.util.HashMap;
import java.util.Objects;

/**
 * A class containing the GRAserver.
 */
public class GRAserver {

  Vertx vertx;
  RateLimiter rateLimiter;
  RedisConnection sub;
  TokenAuthorizer tokenAuthorizer;
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
    tokenAuthorizer = new TokenAuthorizer(vertx);
    subscriptionSetUp();
    this.createServer();

  }

  /**
   * Method for creating the server
   */
  public void createServer(){


      // TODO: Kan en token autentiseras i websocket, isåfall hur?
      vertx.createHttpServer().webSocketHandler(handler -> {
        System.out.println("Client " + handler.binaryHandlerID() + " connected to port " + this.port);

        MultiMap headers = handler.headers();

        if(!tokenAuthorizer.verifyToken(headers.get("Authorization"))){
          System.out.println("Client " + handler.binaryHandlerID() + " was refused due to invalid token");
          handler.reject(407);
        }
        //New client connected, publish list of currently blocked user
        rateLimiter.getSaveState(redisdata -> {
          handler.writeBinaryMessage(redisdata);
        }, failedData -> {
          System.out.println("Message recieved from rateLimiter: " + failedData);
        });

        //socket = handler
        openConnections.put(handler.binaryHandlerID(), handler);

        // TODO: autentisera token?
        handler.binaryMessageHandler(msg -> {
          Event event = new Event(msg);
          System.out.println(msg.toString());
          if(!tokenAuthorizer.verifyToken(event.getToken())){
            //TODO: take correct action
            //TODO:

            handler.close((short) 407, "Token expired");
            System.out.println("Socket closed, token expired");
          } else {
            rateLimiter.unpackEvent(event);
          }
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

      String msgData = message.get(2).toString();

      if (!Objects.equals(msgData, "1")) {
        JsonObject dataObject = new JsonObject(msgData);
        Buffer dataBuffer = dataObject.toBuffer();

        for (ServerWebSocket socket : openConnections.values()) {
          socket.writeBinaryMessage(dataBuffer);
        }
      }
    });

  }
}
