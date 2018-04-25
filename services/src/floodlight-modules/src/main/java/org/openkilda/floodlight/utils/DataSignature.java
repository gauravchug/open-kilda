package org.openkilda.floodlight.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.openkilda.floodlight.exc.CorruptedNetworkDataException;
import org.openkilda.floodlight.exc.InvalidSingatureConfigurationException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class DataSignature {
    private final Algorithm signAlgorithm;
    private final JWTVerifier signVerification;

    public DataSignature(String secret) throws InvalidSingatureConfigurationException {
        try {
            signAlgorithm = Algorithm.HMAC256(secret);
            signVerification = JWT.require(signAlgorithm).build();
        } catch (UnsupportedEncodingException e) {
            throw new InvalidSingatureConfigurationException("Can't initialize sing/verify objects", e);
        }
    }

    public byte[] sign(JWTCreator.Builder token) {
        String payload = token.sign(signAlgorithm);
        return payload.getBytes(Charset.forName("UTF-8"));
    }

    public DecodedJWT verify(byte[] payload) throws CorruptedNetworkDataException {
        String payloadStr = new String(payload, Charset.forName("UTF-8"));
        DecodedJWT token;
        try {
            token = signVerification.verify(payloadStr);
        } catch (JWTVerificationException e) {
            throw new CorruptedNetworkDataException(String.format("Bad signature: %s", e));
        }

        return token;
    }
}
