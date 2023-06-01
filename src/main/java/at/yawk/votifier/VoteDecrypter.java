/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package at.yawk.votifier;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.util.List;

/**
 * @author yawkat
 */
class VoteDecrypter extends ByteToMessageDecoder {
    public static final int MESSAGE_SIZE = 256;
    private final PrivateKey key;

    public VoteDecrypter(PrivateKey key) {
        this.key = key;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int readable = in.readableBytes();
        if (readable < MESSAGE_SIZE) {
            return;
        }
        if(readable > MESSAGE_SIZE){
            in.clear();
            return;
        }

        byte[] encrypted = new byte[MESSAGE_SIZE];
        in.readBytes(encrypted);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(encrypted);
        out.add(Unpooled.wrappedBuffer(decrypted));
    }
}
