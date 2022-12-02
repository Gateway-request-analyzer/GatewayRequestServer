package com.Gateway_request_analyzer.starter;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import redis.clients.jedis.*;




public class redisDatabaseCommunication {

  //Allow for rate limiting on IP, user identifier, and user session
  /*private static Jedis jedis = new Jedis("http://localhost:6379/");
  private static int epoch_ms;
  private static Pipeline pipe;*/
  private Vertx vertx;
  private static Redis client;
  private static RedisAPI redis;

  public redisDatabaseCommunication() {
    vertx = Vertx.vertx();
    try {
       client = Redis.createClient(
          vertx,
          // The client handles REDIS URLs. The select database as per spec is the
          // numerical path of the URL and the password is the password field of
          // the URL authority
          "redis://:@localhost:6379/1");
        client.connect()
        .onSuccess(conn -> {
          System.out.println("connected!");
        });

    } catch (Exception e){
      System.out.println("could not connect.");
      e.printStackTrace();
    }

    //for(int i = 0; i < 11; i++){
      //checkDatabase();
    //}

  }
  //should rate limit be on the combination of IP, userID, and session, or on each parameter by themselves?


    //Creating Jedis connection
   /*try {
     prints out "Connection Successful" if Java successfully connects to Redis server.
     System.out.println("Connection Successful");
     System.out.println("The server is running " + jedis.ping());
     jedis.hset("IPadress", "userID", "it's me");
     jedis.hset("IPadress", "session", "1");
     jedis.hset("IPadress", "value", "0");
     keys will bre replaced with values given by the request

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

 } */
 //should rate limit be on the combination of IP, userID, and session, or on each parameter by themselves?

 public static void checkDatabase(){

   redis = RedisAPI.api(client);

   redis.setnx("IP","0");
   redis.setnx("userID", "0");
   redis.setnx("session", "0");
   redis.get("IP");
 }


    /*

    if(requests < 10){
      jedis.incr("IP");
      jedis.expire("IP", 60);
      System.out.println("Ok!");
    }
    else{
      System.out.println("sorry!!!!!! to many requests.");
    }*/
    public static void main(String[] args) {
      new redisDatabaseCommunication();
      checkDatabase();

    }
}


