package com.Gateway_request_analyzer.starter;
import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;



public class rateLimiter {

  //Allow for rate limiting on IP, user identifier, and user session
  private Vertx vertx;
  private static Redis client;
  private static RedisAPI redis;

  public rateLimiter() {
    vertx = Vertx.vertx();
    try {
       client = Redis.createClient(
          vertx,
          // The client handles REDIS URLs. The select database as per spec is the
          // numerical path of the URL and the password is the password field of
          // the URL authority
          new RedisOptions());
        client.connect()
        .onSuccess(conn -> {
          System.out.println("connected!");
        });
    } catch (Exception e){
      System.out.println("could not connect.");
      e.printStackTrace();
    }
    redis = RedisAPI.api(client);
  }

  //Creates a key/value-pair as an ArrayList of strings
 private static List<String> createKeyValPair(String s) {
    List<String> keyValPair = new ArrayList<>();
    keyValPair.add(s);
    keyValPair.add("1");
    return keyValPair;
 }

 //Saves key and value in database. If success, it adds an expiry time of 20 seconds.
 private static void setValue(List<String> keyValuePair) {
   redis.setnx(keyValuePair.get(0), keyValuePair.get(1), setHandler -> {
     if (setHandler.succeeded()) {
       List<String> expParams = new ArrayList<>();
       expParams.add(keyValuePair.get(0));
       expParams.add("20");
       redis.expire(expParams, expHandler -> {
         if (expHandler.succeeded()) {
           System.out.println("Expiry time set for 20 seconds for: " + keyValuePair.get(0));
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

 //Checks if a key exists. If the key exits the value is incremented. If not, it calls setValue to create a key.
 private static void checkDatabase(String s) {
   AtomicInteger value = new AtomicInteger();
   redis.get(s, getHandler -> {
     if (getHandler.succeeded()) {
       if(getHandler.result() == null) {
         System.out.println("Key created: " + s);
         setValue(createKeyValPair(s));
       }
       else {
         value.set(Integer.parseInt(getHandler.result().toString()));
         if(value.get() > 5) {
           System.out.print("Too many requests: " + s);
         }
         else {
           System.out.println(value.get());
           redis.incr(s);
         }
       }
     }
     if (getHandler.failed()) {
       System.out.println("Couldn't get value at key");
     }
   });
 }
 //For testing only!
 private static void killAllKeys(List<String> keyList) {
   redis.del(keyList, killer -> {
     if (killer.succeeded()) {
       System.out.println("The keys are no more");
     }
     else {
       System.out.println("Still alive");
     }
   });
 }

   //set exp-time for vertx, create Key IP:suffix

   public static void main (String[]args){
     new rateLimiter();
     List<String> keys = new ArrayList<>();
     keys.add("myKey");

     //killAllKeys(keys);           //Uncomment when we want to flush our keys
     checkDatabase("myKey");

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




