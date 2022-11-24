package com.Gateway_request_analyzer.starter;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import redis.clients.jedis.*;




public class redisDatabaseCommunication {

  //Allow for rate limiting on IP, user identifier, and user session
  private static Jedis jedis = new Jedis("http://localhost:6379/");
  private static int epoch_ms;
  private static Pipeline pipe;

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


     /*

     Rate limiting sketch :)

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
