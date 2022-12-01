package com.Gateway_request_analyzer.starter;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Objects;

public class GRAserver {

  Vertx vertx;
  RedisHandler redisHandler;
  //Make this a HashMap
  private static HashMap<String, ServerWebSocket> openConnections = new HashMap<>();

  public GRAserver(Vertx vertx, RedisHandler redisHandler){
    this.vertx = vertx;
    this.redisHandler = redisHandler;
    this.createServer();
  }

  public void createServer(){


    vertx.createHttpServer().webSocketHandler(handler -> {
      System.out.println("Client connected: " + handler.binaryHandlerID());

      //socket = handler;
      openConnections.put(handler.binaryHandlerID(), handler);

      handler.binaryMessageHandler(msg -> {
        JsonObject json = (JsonObject) Json.decodeValue(msg);
        Event event = new Event(json);
        //send reference to this instead of socket
        redisHandler.eventRequest(event);
      });
/*
This is used when client disconnects,
Remove connection from HashMap
*/
      handler.closeHandler(msg -> {
        openConnections.remove(handler.binaryHandlerID());
        System.out.println("Client disconnected" + handler.binaryHandlerID());
      });

    }).listen(3000).onSuccess(err -> {
      System.out.println("Connection succeeded");
    }).onFailure(err -> {
      System.out.println("Connection refused");
    });
  }

  public static void notifyClients(String response){
      //loop over sockets
      //send to clients connected to this socket
    for (ServerWebSocket socket : openConnections.values()) {
      if(Objects.equals(response, "OK")){

        System.out.println("Request was valid");
        System.out.println("No action taken");

        //creates JsonObject to be returned to client
        JsonObject json = new JsonObject();
        json.put("Action", "None");

        Buffer buf = Json.encodeToBuffer(json);
        socket.writeBinaryMessage(buf);

      }else {
        JsonObject json = new JsonObject();
        json.put("Action", response);

        Buffer buf = Json.encodeToBuffer(json);
        socket.writeBinaryMessage(buf);
      }
    }

  }

}
