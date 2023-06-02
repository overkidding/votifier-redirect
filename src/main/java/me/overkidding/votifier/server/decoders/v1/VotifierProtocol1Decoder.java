package me.overkidding.votifier.server.decoders.v1;

import me.overkidding.votifier.server.exceptions.QuietException;
import me.overkidding.votifier.server.objects.Vote;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.overkidding.votifier.Main;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Decodes original protocol votes.
 */
public class VotifierProtocol1Decoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        if (!ctx.channel().isActive()) {
            buf.skipBytes(buf.readableBytes());
            return;
        }

        if (buf.readableBytes() < 256) {
            // The client might have not sent all the data yet, so don't eject the connection.
            return;
        }

        if (buf.readableBytes() > 256) {
            // They sent too much data.
            throw new QuietException("Could not decrypt data from " + ctx.channel().remoteAddress() + " as it is too long. Attack?");
        }

        byte[] block = ByteBufUtil.getBytes(buf);
        buf.skipBytes(buf.readableBytes());

        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, Main.getInstance().getKeyPair().getPair().getPrivate());
            block = cipher.doFinal(block);
        } catch (Exception e) {
            throw new QuietException("Could not decrypt data from " + ctx.channel().remoteAddress() + ". Make sure the public key on the list is correct.");
        }

        // Parse the string we received.
        String all = new String(block, StandardCharsets.US_ASCII);
        String[] split = all.split("\n");
        if (split.length < 5) {
            throw new QuietException("Not enough fields specified in vote. This is not a NuVotifier issue. Got " + split.length + " fields, but needed 5.");
        }

        if (!split[0].equals("VOTE")) {
            throw new QuietException("The VOTE opcode was not present. This is not a NuVotifier issue, but a bug with the server list.");
        }
        Vote vote = new Vote(split[2], split[1], split[3], split[4]);
        list.add(vote);

        // We are done, remove ourselves. Why? Sometimes, we will decode multiple vote messages.
        // Netty doesn't like this, so we must remove ourselves from the pipeline. With Protocol 1,
        // ending votes is a "fire and forget" operation, so this is safe.
        ctx.pipeline().remove(this);
    }
}