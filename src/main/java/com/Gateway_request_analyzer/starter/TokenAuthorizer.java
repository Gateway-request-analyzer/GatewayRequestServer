package com.Gateway_request_analyzer.starter;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.security.interfaces.RSAPublicKey;


public class TokenAuthorizer {
  private String kid = null;
  private RSAPublicKey publicKey;
  private Vertx vertx;
  private long timer;

  public TokenAuthorizer(Vertx vertx){
    this.vertx = vertx;
    fetchPublicKey(3);
  }

  public boolean verifyToken(String token){
    //String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJleHAiOjE2Nzc1OTkwNjF9.FZOEEMYTCff988u4jdNHAgxvsi_onOlaqFB2PrYpcUb8qYLzcbu5Ru-z9RVCNdesgtudZnOurD6eaPy5XkEE9hHN19y-TLH7ygFuufImUc1V25sYPmhcX8zvi3OI-LQwyPH0-qafqUJm3uGWW3SbuYRQ0rCwLCS_h8t3h0FBTxPBK94G5Fyeu486n6e8jc0k9Z0ncXWlb25r2DwskTqSZcpvEiT0QsE3Zi75CnpitqEvm0qmpWwLYp40Gp-Rbyz63uw6TmYRIJSvErNL4cqbiwUTdVQyUwA2RuYcdItdJATR-uJw7EzyaqN89FGdKZjvySW3GDmQUIZn_vrARPyhMg";
    DecodedJWT decodedJWT;
    try {
      Algorithm algorithm = Algorithm.RSA256(this.publicKey);
      JWTVerifier verifier = JWT.require(algorithm)
        // specify a specific claim validations
        .build();

      decodedJWT = verifier.verify(token);
      System.out.println("valid token: " + decodedJWT.getToken());
      return true;
    } catch (JWTVerificationException exception){
      // Invalid signature/claims
      return false;
    }
  }

  public void fetchPublicKey(long delay) {
    if(delay > 60){
      delay = 60;
    }
    timer = delay;
    //192.168.0.139 -> localhost
    WebClient.create(vertx)
      .get(8080, "auth-server", "/.well-known/jwks.json")
      .send()
      .onSuccess(response -> {
        timer = 1;

        JsonObject res = new JsonObject(response.bodyAsString());
        kid = res.getJsonArray("keys").getJsonObject(0).getString("kid");
        System.out.println("Key ID successfully fetched: " + kid);

        JwkProvider provider = new JwkProviderBuilder("http://auth-server:8080")
          .build();

        try {
          this.publicKey = (RSAPublicKey) provider.get(kid).getPublicKey();
          System.out.println("Public key successfully fetched!");
          System.out.println("Ready to verify tokens!");
        } catch(JwkException e){
          System.out.println("Something went wrong fetching full key " + e.getMessage());
          System.out.println("Attempting a new key fetch in " +  timer + " seconds");
          vertx.setTimer(timer * 1000, handler -> {
            fetchPublicKey(timer * 2);
          });
        }
      })
      .onFailure(err -> {
        System.out.println("Something went wrong fetching the Key ID " + err.getMessage());
        System.out.println("Attempting a new key ID fetch in " +  timer + " seconds");
        vertx.setTimer(timer * 1000, handler -> {
          fetchPublicKey(timer * 2);
        });
      });

  }
}
