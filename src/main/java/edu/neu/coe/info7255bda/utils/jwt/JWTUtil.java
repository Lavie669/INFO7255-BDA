package edu.neu.coe.info7255bda.utils.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import edu.neu.coe.info7255bda.model.VO.RSA256Key;
import edu.neu.coe.info7255bda.utils.exception.Customer401Exception;
import edu.neu.coe.info7255bda.utils.json.JsonValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;

@Slf4j
@Component
public class JWTUtil {

    public static final String KEY_ALGORITHM = "RSA";
    public static final int KEY_SIZE = 2048;
    private static final String ISSUER = "info7255bda";
    private static volatile RSA256Key rsa256Key;
    private static final long EXPIRE_TIME = 30*60*1000;

    @Value("${google.client-id}")
    private String clientID;

    public boolean validateToken(String jwt) throws NoSuchAlgorithmException {
        return verifierToken(jwt);
    }

    public static RSA256Key generateRSA256Key() throws NoSuchAlgorithmException {
        if (rsa256Key == null) {
            //密钥生成所需的随机数源
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(KEY_SIZE);
            //通过KeyPairGenerator生成密匙对KeyPair
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            rsa256Key = new RSA256Key();
            rsa256Key.setPublicKey(publicKey);
            rsa256Key.setPrivateKey(privateKey);
        }
        return rsa256Key;
    }

    public String creatTokenByRS256(String clientId) throws NoSuchAlgorithmException {
        if (!clientId.equals(clientID)){
            throw new Customer401Exception("Invalid client_id!!!");
        }
        RSA256Key rsa256Key = generateRSA256Key();
        Algorithm algorithm = Algorithm.RSA256(rsa256Key.getPublicKey(), rsa256Key.getPrivateKey());
        return JWT.create()
                .withIssuer(ISSUER)
                .withAudience(clientId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis()+ EXPIRE_TIME))
                .sign(algorithm);
    }

    private boolean verifierToken(String token) throws NoSuchAlgorithmException {
        RSA256Key rsa256Key = generateRSA256Key();

        Algorithm algorithm = Algorithm.RSA256(rsa256Key.getPublicKey(), null);

        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();

        try {
            DecodedJWT jwt = verifier.verify(token);
            return true;
        }catch (JWTVerificationException e){
            log.error("Invalid token! " + e.getMessage());
            return false;
        }
    }
}
