package com.Gateway_request_analyzer.starter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TokenAuthorizer {

  RSAPublicKey publicKey;

  public TokenAuthorizer(){

    publicKey = getPublicKey();
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

  public RSAPublicKey getPublicKey() {
    try {
      String publicKeyContent = new String(Files.readAllBytes(Paths.get("public_key.pem")));
      publicKeyContent = publicKeyContent.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
      KeyFactory kf = KeyFactory.getInstance("RSA");

      X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));

      return (RSAPublicKey) kf.generatePublic(keySpecX509);
    }catch (Exception e){
      System.out.println("failed to retrieve key: " + e.getMessage() + " ");
      e.printStackTrace();
    }
    return null;
  }
}
