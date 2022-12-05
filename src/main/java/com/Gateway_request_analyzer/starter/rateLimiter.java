package com.Gateway_request_analyzer.starter;
import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;


public class rateLimiter {

  //Allow for rate limiting on IP, user identifier, and user session
  /*private static Jedis jedis = new Jedis("http://localhost:6379/");
  private static int epoch_ms;
  private static Pipeline pipe;*/
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
          "redis://:@localhost:6379/1");
        client.connect()
        .onSuccess(conn -> {
          System.out.println("connected!");
        });

    } catch (Exception e){
      System.out.println("could not connect.");
      e.printStackTrace();
    }
  }

 public static void checkDatabase(){

   redis = RedisAPI.api(client);

   redis.setnx("IP","0");
   redis.setnx("userID", "0");
   redis.setnx("session", "0");
   redis.get("IP");
 }

    public static void main(String[] args) {
      new rateLimiter();
      checkDatabase();

    }
}


