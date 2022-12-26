package com.Gateway_request_analyzer.starter;
import io.vertx.core.Vertx;
import io.vertx.redis.client.*;

import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class that uses the connection to Redis database and checks if client should be rate limited
 */

public class RateLimiter {

  //Allow for rate limiting on IP, user identifier, and user session
  private RedisAPI redis;
  private RedisConnection pub;
  private static final int MAX_REQUESTS_PER_1MIN = 5;
  private static final int MAX_REQUESTS_TIMEFRAME = 12;
  private static final int EXPIRY_TIME = 180;

  /**
   * Constructor for class rateLimiter. Will have "client" as an input parameter.
   * Initializes the redis variable to interact with the database.
   *  client -  will be client
   */
  public RateLimiter(RedisAPI redis, RedisConnection pub) {
    this.redis = redis;
    this.pub = pub;
  }

  /**
   * Method for creating a key/value pair as an ArrayList of strings. First element till be the key,
   * second element will be its value. The initial value for the key will be set to 1.
   * @param s-  a string which will be the key
   * @return keyValPair- returns the list keyValPair which will contain the keys and values
   */
  //Remove?
 /*
  private List<String> createKeyValPair(String s) {
    List<String> keyValPair = new ArrayList<>();
    keyValPair.add(s);
    keyValPair.add("1");
    return keyValPair;
 }
 */
  //Do we need javadoc for private methods?
  /**
   * Method for saving a key and value in the database. If succeeded it will add an expiry time  to the key.
   * @param key- a string with a key.
   */
 private void saveKeyValue(String key) {
   redis.setnx(key, "1").onComplete(setHandler -> {
     if (setHandler.succeeded()) {
       List<String> expParams = new ArrayList<>();
       expParams.add(key);
       expParams.add(Integer.toString(EXPIRY_TIME));
       redis.expire(expParams, expHandler -> {
         if (expHandler.succeeded()) {
           System.out.println("Expiry time set for " + EXPIRY_TIME + "seconds for: " + key);
         }
         else {
           System.out.println("Could not set expiry time");
         }
       });
     }
     else {
       System.out.println("Couldn't set value at given key");
     }
   });
 }
  /**
   * Method for unpacking an event and checking the database with each parameter within the event.
   * @param event-  an event object containing identifying features for an event.
   */
 public void unpackEvent(Event event){
   checkDatabase(event.getIp());
   //checkDatabase(event.getSession());
   //checkDatabase(event.getURI());
   //checkDatabase(event.getUserId());
  }

  /**
   * Method for checking if a key exists in the database. If it exists the value is incremented. If not,
   * it calls setValue and creates a new key. If the value being incremented is more than 5, the requests are too many.
   * @param -  a string representing a key in the database. Will create a new key = s if it does not exist.
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
           System.out.print(" Too many requests for 1 minute: " + key);
           this.publish(s, "blocked");
         }
         else {
           //get/create keys for previous minutes within time frame to check if total number of requests is reached
           List<String> prevKeys = new ArrayList<>();
           for (int i = 1; i <= (EXPIRY_TIME / 60); i++) {
             int prevMinute = (Integer.parseInt(currentMinute) - i) % 60;
             String newKey = s + ":" + prevMinute;
             prevKeys.add(newKey);
           }
           //use redis multiget to get all requests
           redis.mget(prevKeys).onComplete(handler -> {
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
               System.out.print("  Total requests: " + requests);
               if (requests >= MAX_REQUESTS_TIMEFRAME) {
                 System.out.print(" Too many requests for time frame" );
                 this.publish(s, "blocked");
               }
               else {
                 redis.incr(key).onComplete(handlerIncr -> {
                   if (handlerIncr.succeeded()) {
                     System.out.println(" Allow request ");
                       //send info to gateway
                     }
                   else {
                     handlerIncr.cause();
                   }
                 });
               }
             }
           });
         }
       }
       if (getHandler.failed()) {
         System.out.println("Couldn't get value at key");
       }
     }
   });
 }

  /**
   * Method for killing all keys. Will only be used for testing purposes.
   * @param keyList-  a list of all keys we want to kill.
   */
 private void killAllKeys(List<String> keyList) {
   redis.del(keyList, killer -> {
     if (killer.succeeded()) {
       System.out.println("The keys are no more");
     }
     else {
       System.out.println("Still alive");
     }
   });
 }

  /**
   * Main method for class. Will create new instance of the class.
   * @param args-  currently has no use
   */
   public static void main (String[]args){
     //new RateLimiter();
     //List<String> keys = new ArrayList<>();
     //keys.add("myKey");

     //killAllKeys(keys);           //Uncomment when we want to flush our keys
     //checkDatabase("myKey");
   }

  private void publish(String ip, String action){

    this.pub.send(Request.cmd(Command.PUBLISH)
        .arg("channel1")
        .arg(ip + " " +  action))
      .onSuccess(res -> {
        //Published
        //System.out.println("Message successfully published to pub/sub!");

      }).onFailure(err -> {
        System.out.println("Publisher error: " + err.getCause());
      });
  }
}



/*
 Rate limiting sketch for redis

     Request: Key = IP:suffix, suffix = :minute -> IP:0 for first minute,  value incr with every request
     Total number of requests per minute = 4
     Total number of requests per every 5 minutes = 10
     Expiration for keys = 5 minutes

     Minute 0:
      Req 1 -> IP:0, value=1      /does all of these expire at the same time or does expiry time get updated with every incr?
      Req 2 -> IP:0, value=2
      Req 3 -> IP:0, value=3
      Req 4 -> IP:0, value=4
      Req 5 -> too many requests for one minute.

     Minute 1:
      Req 6 -> IP:1, value=1          /We also need to check how many requests done during minute 0 to see if limit for requests during 5 minutes has been reached.
      Req 7 -> IP:1, value=2
      Req 8 -> IP:1, value=3
      Req 9 -> IP:1, value=4
      Req 10 -> too many requests for one minute.

     Minute 2:
      Req 11 -> IP:2, value=1
      Req 12 -> IP:2, value=2
      Req 13 -> too many requests during 5 minute window -> we need to wait until 5 minutes has passed from minute 0 until more requests can be made

      Wait during minute 3 - minute 4 ...

     Minute 5:
      The key IP:0 has expired and 4 new requests can be made

     Minute 6:
      The key IP:1 has expired and 4 new requests can be made

     Minute 7:
      The key IP:2 has expired and 2 new requests can be made




     - använda time-stamp för att för att sätta suffix med minut i key
     - varje key/value ska ha en expiration
     - för att rate-limita: summera alla värden som ligger under varje key
      - sorted
 */




