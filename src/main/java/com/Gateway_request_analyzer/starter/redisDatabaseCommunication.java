package com.Gateway_request_analyzer.starter;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import redis.clients.jedis.*;

import java.util.Set;


public class redisDatabaseCommunication {


 public static void main(String[] args) {

 //Creating Jedis connection
   try {
     Jedis jedis = new Jedis("http://localhost:6379/");
// prints out "Connection Successful" if Java successfully connects to Redis server.
     System.out.println("Connection Successful");
     System.out.println("The server is running " + jedis.ping());
     jedis.set("company-name", "500Rockets.io");
     System.out.println("Stored string in redis:: " + jedis.get("company-name"));
   } catch (Exception e) {
     e.printStackTrace();
   }


 }
}
