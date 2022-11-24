package com.Gateway_request_analyzer.starter;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import redis.clients.jedis.*;




public class redisDatabaseCommunication {

  //Allow for rate limiting on IP, user identifier, and user session
  private static Jedis jedis = new Jedis("http://localhost:6379/");

 public static void main(String[] args) {

 //Creating Jedis connection
   try {
// prints out "Connection Successful" if Java successfully connects to Redis server.
     System.out.println("Connection Successful");
     System.out.println("The server is running " + jedis.ping());
     //jedis.hset("IPadress", "userID", "it's me");
     //jedis.hset("IPadress", "session", "1");
     //jedis.hset("IPadress", "value", "0");
     //keys will bre replaced with values given by the request
     jedis.setnx("IP","0");
     jedis.setnx("userID", "0");
     jedis.setnx("session", "0");
     //System.out.println("Stored strings in redis:: " + jedis.hgetAll("client"));
   } catch (Exception e) {
     e.printStackTrace();
   }
   for(int i = 0; i < 11; i++){
     checkDatabase();
   }

 }
 //should rate limit be on the combination of IP, userID, and session, or on each parameter by themselves?

 public static void checkDatabase(){
    int requests = Integer.parseInt(jedis.get("IP"));
    if(requests < 10){
      jedis.incr("IP");
      jedis.expire("IP", 60);
      System.out.println("Ok!");
    }
    else{
      System.out.println("sorry!!!!!! to many requests.");
    }
 }
}
