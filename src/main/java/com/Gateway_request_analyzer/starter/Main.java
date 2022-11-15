package com.Gateway_request_analyzer.starter;

public class Main { // Example. Very low rate limit for test purpose. 3 requests per second per user.
  public static void main(String[] args) throws InterruptedException {
    // Pretend this is sent from the Gateway for each incoming request
    GRAClient client = new GRAClient();
    client.sendEvent("1.2.3.4", "user1", "session1", "/"); // Server logs OK
    client.sendEvent("1.2.3.4", "user1", "session1", "/"); // Server logs OK
    client.sendEvent("2.2.3.4", "user1", "session1", "/page1"); // Server logs OK

    // Server logs that the request is rate limited, but we don't yet have a way to signal this back to the client. We'll deal with this later
    client.sendEvent("3.2.3.4", "user1", "session1", "/page2");

    // We wait some to clear the rate limit
    Thread.sleep(2000);

    // The rate limit should be cleared and server should log OK
    client.sendEvent("1.2.3.4", "user1", "session1", "/page3");
  }
}

