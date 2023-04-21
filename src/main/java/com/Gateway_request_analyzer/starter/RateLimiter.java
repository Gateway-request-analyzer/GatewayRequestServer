package com.Gateway_request_analyzer.starter;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;
import io.vertx.redis.client.impl.RedisClient;

import java.security.Provider;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class that uses the connection to Redis database and checks if client should be rate limited.
 * The result is published using the Pub/Sub-connection
 */

public class RateLimiter {

  //Allow for rate limiting on IP, user identifier, and user session
  private RedisAPI redis;
  private RedisConnection pub;
  private static final int MAX_REQUESTS_PER_1MIN = 100;
  private static final int MAX_REQUESTS_TIMEFRAME = 300;
  private static final long ONE_MINUTE_MILLIS = 60000;
  private static final long FIVE_MINUTES_MILLIS = 300000;

  private static final int EXPIRY_TIME = 180;
  private JsonObject completeSaveState = new JsonObject();
  private MachineLearningClient MlClient;

  /**
   * Constructor for class RateLimiter.
   * @param redis- initializes the redis variable to interact with the database.
   * @param pub- initializes the pub variable to publish actions.
   */
  public RateLimiter(RedisAPI redis, RedisConnection pub, MachineLearningClient MlClient) {
    this.redis = redis;
    this.pub = pub;
    this.MlClient = MlClient;
  }

  /**
   * Function for setting expiry times to Redis key sets
   * @param key - Name of data set
   * @param time - Time of expiry
   * */

  private void setExpiry(String key, int time){
    List<String> expParams = new ArrayList<>();
    expParams.add(key);
    expParams.add(Integer.toString(time));
    redis.expire(expParams, expHandler -> {
      if (!expHandler.succeeded()) {
        System.out.println("Could not set expiry time");
      }
    });
  }


  protected void insertDB(Event e){

    MlClient.insertRedisList(e);
    insertDBValue(e.getIp(), "Ip");
    insertDBValue(e.getSession(), "Session");
    insertDBValue(e.getUserId(), "UserId");

  }

  private void insertDBValue(String s, String type){
    String currentMinute = new SimpleDateFormat("mm").format(new java.util.Date());
    String eventMinute = s + ':' + currentMinute;
    redis.incr(eventMinute).onComplete(handler -> {

      if(handler.result().toInteger() >= MAX_REQUESTS_PER_1MIN){
        Action currentAction = new Action(s, "blockedBy" + type, ONE_MINUTE_MILLIS, "single", "rateLimiter");
        setSortedBlocked(currentAction);
        this.publish(currentAction);
      } else if(handler.result().toInteger() == 1){
        System.out.println("Adding block timer for: " + eventMinute);
        setExpiry(eventMinute, EXPIRY_TIME);
      }

    }).onFailure(err -> {
      System.out.println("Error adding " + s + " to database: " + err.getMessage());
    });
    checkDBTimeframe(s, type, currentMinute, eventMinute);

  }



  private void checkDBTimeframe(String s, String type, String currentMinute, String key){

    AtomicInteger value = new AtomicInteger();
    List<String> prevKeys = new ArrayList<>();
    //use redis multiget to get all requests, the createPrevKeyList is used to create the list used as a parameter.
    redis.mget(createPrevKeyList(1, s, currentMinute, prevKeys)).onComplete(handler -> {
      if(handler.succeeded()) {
        int requests = value.get();
        // Summation of all number of requests within time frame
        Iterator<Response> it = handler.result().iterator();
        while (it.hasNext()) {
          Response r = it.next();
          if (r == null) {
            // requests += 0;
          }
          else {
            requests += r.toInteger();
          }
        }
        if (requests >= MAX_REQUESTS_TIMEFRAME) {
          //add user to redis block-list
          Action currentAction = new Action(s, "blockedBy" + type, FIVE_MINUTES_MILLIS, "single", "rateLimiter");
          setSortedBlocked(currentAction);
          this.publish(currentAction);
        }
        else {
          redis.incr(key).onComplete(handlerIncr -> {
            if (handlerIncr.succeeded()) {
              System.out.println(" Allow request for " + key);
            }
            else {
              System.out.println("Increment failed in Redis: " + handlerIncr.cause());
            }
          });
        }
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


  private void setSortedBlocked(Action action){

    String currentTime = String.valueOf(System.currentTimeMillis());
    List<String> params = new ArrayList<>();

    //ZADD saveState 100 '{"actionType":"blockByUserId","value":"user1","source":"rateLimiter"}'



    params.add("saveState");
    params.add(action.timeString());
    params.add(action.toJson().toString());

    redis.zadd(params);
    redis.zremrangebyscore("saveState", "-inf", currentTime);

  }


  public void getSaveState(Consumer<Buffer> onTest, Consumer<String> onTestFailure){

    JsonObject saveState = new JsonObject();
    saveState.put("publishType", "saveState");
    List<String> params = new ArrayList<>();

    params.add("saveState");
    params.add("0");
    params.add("-1");

    redis.zrange(params).onSuccess(handler -> {

      Iterator<Response> it = handler.iterator();

      while(it.hasNext()){
        Response response = it.next();
        JsonObject value = new JsonObject(String.valueOf(response));
        saveState.put(value.getString("value"), value);
      }
      onTest.accept(saveState.toBuffer());

    }).onFailure(err -> {
      System.out.println("Failed to retrieve sorted set: " + err);
      onTestFailure.accept("Failed to fetch saveState: " + err.getCause());

    });
  }

  /**
   * Method for publishing action decided by the rate limiter with pub/sub.
   */
  private void publish(Action action){
    System.out.println("Action taken: " + action.value + " has been " + action.actionType);

    Buffer buf = action.toJson().toBuffer();
    this.pub.send(Request.cmd(Command.PUBLISH)
        .arg("channel1").arg(buf))
      .onFailure(err -> {
        System.out.println("Failed to publish single action to pub/sub: " + err.getCause());
      });
  }
}




