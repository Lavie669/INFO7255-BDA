package edu.neu.coe.info7255bda.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import edu.neu.coe.info7255bda.service.JwtTokenService;
import edu.neu.coe.info7255bda.utils.jwt.JWTUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;


@Service("tokenService")
public class JwtTokenServiceImpl implements JwtTokenService {

    @Autowired
    private JWTUtil jwtUtil;

    @Value("${oauth-url}")
    private String oauthUrl;



    @SneakyThrows
    @Override
    public boolean validateToken(String path, String jwt) {
        if (path.equals(oauthUrl)){
            return jwtUtil.validateGoogleToken(jwt);
        }
        else {
            return jwtUtil.validateMyToken(jwt) != null;
        }
    }

    @SneakyThrows
    @Override
    public String creatToken() {
        return jwtUtil.creatTokenByRS256();
    }

    @SneakyThrows
    @Override
    public DecodedJWT validateMyToken(String token) {
        return jwtUtil.validateMyToken(token);
    }


}
