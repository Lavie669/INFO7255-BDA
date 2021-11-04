package edu.neu.coe.info7255bda.utils.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import edu.neu.coe.info7255bda.model.VO.RSA256Key;
import edu.neu.coe.info7255bda.utils.exception.Customer401Exception;
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

    @Value("${oauth-url}")
    private String oauthUrl;

    @Value("${google.client-id}")
    private String clientID;

    @Value(("${google.issuer}"))
    private String issuer;

    public boolean validateToken(String path, String jwt) throws ParseException, NoSuchAlgorithmException {
        if (path.equals(oauthUrl)){
            return validateGoogleToken(jwt);
        }
        else {
            return validateMyToken(jwt) != null;
        }
    }

    private boolean validateGoogleToken(String jwt){
        try {
            JWTClaimsSet claims = JWTParser.parse(jwt).getJWTClaimsSet();
            if (claims.getExpirationTime().compareTo(new Date()) < 0){
                log.error("Token has expired!!!");
                return false;
            }
            if (!claims.getIssuer().equals(issuer)){
                log.error("Invalid issuer");
                return false;
            }
            if (!claims.getAudience().contains(clientID)){
                log.error("Invalid client_id!!!");
                return false;
            }
            return true;
        }catch (ParseException e){
            log.error("Invalid token!!!");
            return false;
        }
    }

    public static RSA256Key generateRSA256Key() throws NoSuchAlgorithmException {
        if (rsa256Key == null) {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            rsa256Key = new RSA256Key();
            rsa256Key.setPublicKey(publicKey);
            rsa256Key.setPrivateKey(privateKey);
        }
        return rsa256Key;
    }

    public String creatTokenByRS256() throws NoSuchAlgorithmException {
        RSA256Key rsa256Key = generateRSA256Key();
        Algorithm algorithm = Algorithm.RSA256(rsa256Key.getPublicKey(), rsa256Key.getPrivateKey());
        return JWT.create()
                .withIssuer(ISSUER)
                .withAudience(clientID)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis()+ EXPIRE_TIME))
                .sign(algorithm);
    }

    private DecodedJWT validateMyToken(String token) throws NoSuchAlgorithmException {
        RSA256Key rsa256Key = generateRSA256Key();
        Algorithm algorithm = Algorithm.RSA256(rsa256Key.getPublicKey(), rsa256Key.getPrivateKey());
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();

        try {
            return verifier.verify(token);
        }catch (JWTVerificationException e){
            log.error("Invalid token! " + e.getMessage());
            return null;
        }
    }
}
