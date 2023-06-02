package me.overkidding.votifier.server.decoders.v2;

import me.overkidding.votifier.server.objects.VotifierSession;
import me.overkidding.votifier.server.utils.KeyCreator;
import me.overkidding.votifier.server.objects.Vote;
import me.overkidding.votifier.server.utils.GsonInst;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageDecoder;
import me.overkidding.votifier.Main;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.List;

/**
 * Decodes protocol 2 JSON votes.
 */
public class VotifierProtocol2Decoder extends MessageToMessageDecoder<String> {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void decode(ChannelHandlerContext ctx, String s, List<Object> list) throws Exception {
        JsonObject voteMessage = GsonInst.gson.fromJson(s, JsonObject.class);
        VotifierSession session = ctx.channel().attr(VotifierSession.KEY).get();

        String payload = voteMessage.get("payload").getAsString();
        JsonObject votePayload = GsonInst.gson.fromJson(payload, JsonObject.class);

        if (!votePayload.get("challenge").getAsString().equals(session.getChallenge())) {
            throw new CorruptedFrameException("Challenge is not valid");
        }
        Key key = KeyCreator.createKeyFrom(Main.getInstance().getConfig().getCurrentToken());

        String sigHash = voteMessage.get("signature").getAsString();
        byte[] sigBytes = Base64.getDecoder().decode(sigHash);

        if (!hmacEqual(sigBytes, payload.getBytes(StandardCharsets.UTF_8), key)) {
            throw new CorruptedFrameException("Signature is not valid (invalid token?)");
        }

        if (votePayload.get("username").getAsString().length() > 16) {
            throw new CorruptedFrameException("Username too long");
        }

        Vote vote = new Vote(votePayload);
        list.add(vote);

        ctx.pipeline().remove(this);
    }

    private boolean hmacEqual(byte[] sig, byte[] message, Key key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        byte[] calculatedSig = mac.doFinal(message);

        byte[] randomKey = new byte[32];
        RANDOM.nextBytes(randomKey);

        Mac mac2 = Mac.getInstance("HmacSHA256");
        mac2.init(new SecretKeySpec(randomKey, "HmacSHA256"));
        byte[] clientSig = mac2.doFinal(sig);
        mac2.reset();
        byte[] realSig = mac2.doFinal(calculatedSig);

        return MessageDigest.isEqual(clientSig, realSig);
    }
}