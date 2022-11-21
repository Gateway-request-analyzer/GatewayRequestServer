package com.Gateway_request_analyzer.starter;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;


public class Event {
  private String ip, userId, session, URI;
  private Vertx vertx;

  public Event(Vertx vertx, JsonObject json) {
    this.ip = json.getString("ip");
    this.userId = json.getString("userId");
    this.session = json.getString("session");
    this.URI = json.getString("URI");
    this.vertx = vertx;
  }

  public void handleRequest(){

    System.out.println("Received ip: " + ip);
    serverResponse();

  }

  private void serverResponse(){
    HttpClient client = vertx.createHttpClient();

    client.webSocket(3500, "localhost", "/", webSocket -> {
      if(webSocket.succeeded()){
        System.out.println("Connection established.");

        WebSocket socket = webSocket.result();
        socket.writeTextMessage("Returning IP: " + this.ip, handler -> {
          socket.end();
        });


      } else{
        System.out.println("Connection to client refused: " + webSocket.cause());
      }
    });

  }

}
