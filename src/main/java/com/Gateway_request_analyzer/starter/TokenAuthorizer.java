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

  public TokenAuthorizer(){
    verifyToken();
  }

  public void verifyToken(){
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJraWQiOiIxMjM0IiwibmFtZSI6IkVtZWwiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0Ojg4ODgvIn0.sBi3lbElQOt03XQfYJ7Ya_AU82fr-mQkYsxhQ29qWDUMOeOa9CpJf7mjcHkkM5pM8wukURxPTaxdcY9tzkSpj9PZfQPrQ030m5o-6ZQkbtjljAGe-7yz7wj1sskFEQmhQrqgTCOY4O7PbyZ3S878uNZnjSLK34w3__CLKKo6kwQfpcCYAG1XO1GMAOI5DavZWzDEwAJwsZGgEkpGERMpkzrzw2kJwt6fsVtLNqiVduDoOLNo0gzAX2JxvDz_QEm62_811RucuiPOf7hLBPmf9lwhC32-rQ4EM5fT6VpPOWOcBeObMUyBBq2GrvbgpeddTfFiJlTman2mNguu6HBdnw";
    RSAPublicKey publicKey = getPublicKey();
    DecodedJWT decodedJWT;
    try {
      Algorithm algorithm = Algorithm.RSA256(publicKey);
      JWTVerifier verifier = JWT.require(algorithm)
        // specify a specific claim validations
        .withIssuer("http://localhost:8888/")
        .build();

      decodedJWT = verifier.verify(token);
      System.out.println("legit token");
    } catch (JWTVerificationException exception){
      // Invalid signature/claims
      System.out.println("not legit token");
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
