package com.Gateway_request_analyzer.starter;
import io.vertx.redis.client.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class that uses the connection to Redis database and checks if client should be rate limited.
 * The result is published using the Pub/Sub-connection
 */

public class RateLimiter {

  //Allow for rate limiting on IP, user identifier, and user session
  private RedisAPI redis;
  private RedisConnection pub;
  private static final int MAX_REQUESTS_PER_1MIN = 5;
  private static final int MAX_REQUESTS_TIMEFRAME = 12;
  private static final int EXPIRY_TIME = 180;

  /**
   * Constructor for class RateLimiter.
   * @param redis- initializes the redis variable to interact with the database.
   * @param pub- initializes the pub variable to publish actions.
   */
  public RateLimiter(RedisAPI redis, RedisConnection pub) {
    this.redis = redis;
    this.pub = pub;
  }


  /**
   * Method for saving a key and value in the database. If succeeded it will add an expiry time to the key.
   * @param key- a new key not already existing in the database.
   */
 private void saveKeyValue(String key) {
   redis.setnx(key, "0").onComplete(setHandler -> {
     if (setHandler.succeeded()) {
       List<String> expParams = new ArrayList<>();
       expParams.add(key);
       expParams.add(Integer.toString(EXPIRY_TIME));
       redis.expire(expParams, expHandler -> {
         if (!expHandler.succeeded()) {
           System.out.println("Could not set expiry time for: " + key);
         }
       });
     }
     else {
       System.out.println("Couldn't set value at given key");
     }
   });
 }

  /**
   * Method for unpacking an incoming event and checking the database with each parameter within the event.
   * @param event- an event object containing identifying features for an event.
   */
 public void unpackEvent(Event event){
   checkDatabase(event.getIp());
   //checkDatabase(event.getSession());
   //checkDatabase(event.getURI());
   //checkDatabase(event.getUserId());
  }

  /**
   * Method for checking if a key exists in the database. If it exists the value is incremented. If not,
   * it calls setValue and creates a new key. If the value being incremented is more than expiry time, the requests are too many.
   * @param s-  a string representing a key in the database. Will create a new key = s if it does not exist.
   */
 private void checkDatabase(String s) {
   AtomicInteger value = new AtomicInteger();
   String currentMinute = new SimpleDateFormat("mm").format(new java.util.Date());
   String key = s + ":" + currentMinute;

   redis.get(key).onComplete( getHandler -> {
     if (getHandler.succeeded()) {

       //Create key for current minute if it does not exist
       if (getHandler.result() == null){
           System.out.println("Key created: " + key);
           saveKeyValue(key);
       }
       else {

         //Checks if limit for requests are reached the current minute
         value.set(Integer.parseInt(getHandler.result().toString()));
         if (value.get() >= MAX_REQUESTS_PER_1MIN) {
           this.publish(s, "blocked");
           return;
         }
       }
       //use redis multiget to get all requests, the createPrevKeyList is used to create the list used as a parameter.
       List<String> prevKeys = new ArrayList<>();
       redis.mget(createPrevKeyList(1, s, currentMinute, prevKeys)).onComplete(handler -> {
         if(handler.succeeded()) {
           int requests = value.get();

           // Summation of all number of requests within time frame
           Iterator<Response> it = handler.result().iterator();
           while (it.hasNext()) {
             Response r = it.next();
             if (r == null) {
               requests += 0;
             }
             else {
               requests += r.toInteger();
             }
           }
           if (requests >= MAX_REQUESTS_TIMEFRAME) {
             this.publish(s, "blocked");
           }
           else {
             redis.incr(key).onComplete(handlerIncr -> {
               if (handlerIncr.succeeded()) {
                 this.publish(s, "Allow");
               }
               else {
                 handlerIncr.cause();
               }
             });
           }
         }
       });
     }
     if (getHandler.failed()) {
       System.out.println("Couldn't get value at key");
     }
   });
 }

  /**
   * Method for creating a list of previous keys within the time frame using recursion
   * @param i - integer representing number of minutes from baseMinutes
   * @param s - String representing the parameter being rate limited
   * @param baseMinute - minute when current request was made
   * @param list - a list for storing all previous keys
   * @return list of all possible previous keys within the time frame
   */
 private List<String> createPrevKeyList (int i, String s, String baseMinute, List<String> list) {
   if ( i > (EXPIRY_TIME / 60)) {
     return list;
   }
   else {
     int prevMinute = (Integer.parseInt(baseMinute) - i) % 60;
     String newKey =  s + ":" + prevMinute;
     list.add(newKey);
     return createPrevKeyList(i+1, s, baseMinute, list);
   }
 }

  /**
   * Method for publishing action decided by the rate limiter with pub/sub.
   * @param param- a string representing the rate limited parameter.
   * @param action- a string representing the action.
   */
  private void publish(String param, String action){
    this.pub.send(Request.cmd(Command.PUBLISH)
        .arg("channel1")
        .arg(param + " " +  action))
      .onSuccess(res -> {
        //Published
        //System.out.println("Message successfully published to pub/sub!");
      }).onFailure(err -> {
        System.out.println("Publisher error: " + err.getCause());
      });
  }
}




