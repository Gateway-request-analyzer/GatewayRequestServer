package com.Gateway_request_analyzer.starter;


import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.redis.client.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REQUIRED:
 * Server to send connections to ML-server (Websocket?)
 * Buffer to store up to 30 requests, then send continuously
 * Return value to allow blocking
 *
 * */
public class MachineLearningClient {

  private WebClient client;
  private RedisAPI redis;

  private static final int EXPIRY_3H = 10800;
  private static final long EXPIRY_3H_MILLIS = 10800000;

  public MachineLearningClient(RedisAPI redis, Vertx vertx){
    this.redis = redis;
    this.client = WebClient.create(vertx);
  }

  private JsonObject createJsonObject(String userID, String session, String URL){
    return new JsonObject()
      .put("timestamp", System.currentTimeMillis())
      .put("userID", userID)
      .put("sessionID", session)
      .put("expiring", System.currentTimeMillis() + EXPIRY_3H_MILLIS)
      .put("URL", URL);
  }

  public void insertRedisList(Event e){
    /**
     * Order to ML: timestamp, userID, sessionID, expiring, URL
     * */
    String ip = e.getIp();
    String userId = e.getUserId();
    String session = e.getSession();
    String URL = e.getURI();

    JsonObject jo = createJsonObject(userId, session, URL);
    String jsonString = jo.toString();

    List<String> insertion = new ArrayList<>();


    insertion.add(userId);
    insertion.add(jsonString);
    redis.llen(userId).onComplete(handler -> {

      System.out.println("Current length value for ML: " + handler.result().toInteger());
      if(handler.result().toInteger() >= 49){

        redis.lpush(insertion).onFailure(err -> {
          System.out.println("Error adding element to Redis");
        });

        redis.ltrim(userId, "0", "49").onFailure(err -> {
          System.out.println("Error trimming list");
        });

        sendPostRequest(userId);

      } else if(handler.result().toInteger() >= 10){
        redis.lpush(insertion, pushHandler -> {
          sendPostRequest(userId);
        });
      }
      else {

        redis.lpush(insertion, pushHandler -> {
          if(handler.result().toInteger() <= 1){
            System.out.println("Set expiry time");
            setRedisExpiry(userId);
          }
        });

      }
    }).onFailure(err -> {
      System.out.println("Error checking length of Redis List");
    });
  }

  private void setRedisExpiry(String userId){
    List<String> expTime = new ArrayList<>();
    expTime.add(userId);
    expTime.add(Integer.toString(EXPIRY_3H));
    redis.expire(expTime).onFailure(handler -> {
      System.out.println("Could not add Redis list expiry time");
    });
  }


  private void sendPostRequest(String userId){

    System.out.println("Sending to ML-server");

    redis.lrange(userId, "0", "-1").onComplete(handler -> {
      JsonObject jo = new JsonObject(handler.result().toString());
      System.out.println(jo);
      client
        .post(8090, "127.0.0.1", "/anomaly")
        .sendBuffer(handler.result().toBuffer())
        .onSuccess(res -> {
          this.handleMlResponse(res.bodyAsJsonObject());
        });

    }).onFailure(err -> {
      System.out.println("Error fetching redis list");
    });
  }

  private void handleMlResponse(JsonObject jo){
    System.out.println("Result returned from ML: " + jo.toString());
  }

}
