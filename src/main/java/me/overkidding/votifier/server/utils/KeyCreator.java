package me.overkidding.votifier.server.utils;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

public class KeyCreator {

    private KeyCreator() {}

    public static Key createKeyFrom(String token) {
        return new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}