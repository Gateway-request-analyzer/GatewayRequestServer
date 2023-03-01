package com.Gateway_request_analyzer.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

/**
 * Class containing the main verticle. Each server gets its own instance of this verticle.
 */
public class MainVerticle extends AbstractVerticle {
  @Override
  public void start(){

    //amounts of servers started

    for(int i = 0; i < 1; i++){
      JsonObject jo = new JsonObject();
      jo.put("port", 3000+i).put("host", "localhost");
      vertx.deployVerticle(new ServerVerticle(), new DeploymentOptions().setConfig(jo));
    }


  }
}
