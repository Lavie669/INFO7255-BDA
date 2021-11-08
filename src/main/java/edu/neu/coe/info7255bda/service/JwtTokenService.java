package edu.neu.coe.info7255bda.service;

import com.auth0.jwt.interfaces.DecodedJWT;

public interface JwtTokenService {

    boolean validateToken(String path, String jwt);

    String creatToken();

    DecodedJWT validateMyToken(String token);
}
