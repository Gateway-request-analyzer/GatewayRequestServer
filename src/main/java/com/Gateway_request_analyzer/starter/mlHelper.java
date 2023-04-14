package com.Gateway_request_analyzer.starter;


import io.vertx.core.http.WebSocket;

import java.util.ArrayList;

/**
 * REQUIRED:
 * Server to send connections to ML-server (Websocket?)
 * Buffer to store up to 30 requests, then send continuously
 * Return value to allow blocking
 *
 * */
public class mlHelper {

  WebSocket socket;
  ArrayList<Event> mlBuffer;

  public mlHelper(){

  }


  private void mlServerSetup(){



  }

}
