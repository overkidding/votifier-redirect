package me.overkidding.votifier.client;

import me.overkidding.votifier.server.objects.Vote;
import com.google.gson.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Votifier implementation
 * <p>
 * This class is used to encode votes to Votifier2 format.
 * <p>
 * Credits to: https://github.com/mikroskeem/votifier2-java
 **/
public class VotifierImplementation {


    public String hmac(String key, String content)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac SHA256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        SHA256HMAC.init(keySpec);
        return new String(Base64.getEncoder().encode(SHA256HMAC.doFinal(content.getBytes())));
    }

    public String encode(Vote vote, String challengeToken){
        JsonObject voteObject = new JsonObject();
        voteObject.addProperty("challenge", challengeToken);
        voteObject.addProperty("username", vote.getUsername());
        voteObject.addProperty("address", vote.getAddress());
        voteObject.addProperty("timestamp", vote.getTimestamp());
        voteObject.addProperty("serviceName", vote.getService());
        return voteObject.toString();
    }

    public byte[] encode(Vote vote, String challengeToken, String key) throws Exception {
        String payload = encode(vote, challengeToken);
        String signature = hmac(key, payload);
        JsonObject message = new JsonObject();
        message.addProperty("payload", payload);
        message.addProperty("signature", signature);
        String finalMessage = message.toString();

        ByteBuffer bb = ByteBuffer.allocate(finalMessage.length()+4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putShort(uint16(0x733a));
        bb.putShort(uint16(finalMessage.length()));
        for(char c : finalMessage.toCharArray()) bb.put((byte)c);
        return bb.array();
    }

    private short uint16(int in){
        return (short)(in & 0xFFFF);
    }
    
}
