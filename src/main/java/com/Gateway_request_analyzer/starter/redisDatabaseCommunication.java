package com.Gateway_request_analyzer.starter;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import redis.clients.jedis.*;




public class redisDatabaseCommunication {

  //Allow for rate limiting on IP, user identifier, and user session


 public static void main(String[] args) {

 //Creating Jedis connection
   try {
     Jedis jedis = new Jedis("http://localhost:6379/");
// prints out "Connection Successful" if Java successfully connects to Redis server.
     System.out.println("Connection Successful");
     System.out.println("The server is running " + jedis.ping());
     jedis.hset("client", "ip", "123");
     jedis.hset("client", "userID", "it's me");
     jedis.hset("client", "session", "1");


     //System.out.println("Stored strings in redis:: " + jedis.hgetAll("client"));
   } catch (Exception e) {
     e.printStackTrace();
   }
 }
}
