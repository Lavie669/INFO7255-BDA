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
import edu.neu.coe.info7255bda.utils.json.JsonUtil;
import edu.neu.coe.info7255bda.utils.json.JsonValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JWTUtil {

    public static final String DIR_PREFIX = "./src/main/resources";
    public static final String GOOGLE_CERT = "/public.crt";

    public static final String KEY_ALGORITHM = "RSA";
    private static final String ISSUER = "info7255bda";
    private static volatile RSA256Key rsa256Key;
    public static final int KEY_SIZE = 2048;
    private static final long EXPIRE_TIME = 30*60*1000;

    @Value("${oauth-url}")
    private String oauthUrl;

    @Value("${google.client-id}")
    private String clientID;

    @Value(("${google.issuer}"))
    private String issuer;

    public boolean validateToken(String path, String jwt) throws NoSuchAlgorithmException {
        if (oauthUrl.contains(path)){
            log.info("Checking Google JWT...");
            return validateGoogleToken(jwt);
        }
        else {
            log.info("Checking INFO7255BDA JWT...");
            return validateMyToken(jwt) != null;
        }
    }

    public boolean validateGoogleToken(String token){
        try {
            com.nimbusds.jwt.JWT jwt = JWTParser.parse(token);
            String kid = JsonValidateUtil.str2JsonNode(jwt.getHeader().toString()).get("kid").asText();
            NimbusJwtDecoder decoder = generateDecoderById(kid);
            if (decoder == null){
                log.error("Invalid token, no kid included at header!!!");
                return false;
            }
            decoder.decode(token);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
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
        }catch (Exception e){
            log.error("Invalid token!!!" + e.getMessage());
            return false;
        }
    }

    private NimbusJwtDecoder generateDecoderById(String kid) throws CertificateException {
        JsonNode jsonNode = JsonUtil.readFromFileToJson(DIR_PREFIX+GOOGLE_CERT);
        if (!jsonNode.has(kid)){
            return null;
        }
        String pk = jsonNode.get(kid).asText();
        InputStream in = new ByteArrayInputStream(pk.getBytes());
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)f.generateCertificate(in);
        PublicKey publicKey = certificate.getPublicKey();
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) publicKey).build();
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
        String uid = UUID.randomUUID().toString();
        String jwt = JWT.create().withIssuer(ISSUER)
                .withAudience(clientID)
                .withSubject(uid)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis()+ EXPIRE_TIME))
                .sign(algorithm);
        log.info(uid + " generated a new jwt...");
        return jwt;
    }

    public DecodedJWT validateMyToken(String token) throws NoSuchAlgorithmException {
        RSA256Key rsa256Key = generateRSA256Key();
        Algorithm algorithm = Algorithm.RSA256(rsa256Key.getPublicKey(), null);
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
