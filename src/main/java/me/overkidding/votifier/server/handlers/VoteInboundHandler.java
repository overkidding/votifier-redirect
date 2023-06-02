package me.overkidding.votifier.server.handlers;

import me.overkidding.votifier.server.objects.VotifierSession;
import me.overkidding.votifier.server.objects.Vote;
import me.overkidding.votifier.server.utils.GsonInst;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@ChannelHandler.Sharable
public class VoteInboundHandler extends SimpleChannelInboundHandler<Vote> {
    private final AtomicLong lastError;
    private final AtomicLong errorsSent;

    private final Consumer<Vote> listener;

    public VoteInboundHandler(Consumer<Vote> listener) {
        this.listener = listener;
        this.lastError = new AtomicLong();
        this.errorsSent = new AtomicLong();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, final Vote vote) throws Exception {
        VotifierSession session = ctx.channel().attr(VotifierSession.KEY).get();

        session.completeVote();
        listener.accept(vote);

        if (session.getVersion() == VotifierSession.ProtocolVersion.ONE) {
            ctx.close();
        } else {
            JsonObject object = new JsonObject();
            object.addProperty("status", "ok");
            ctx.writeAndFlush(GsonInst.gson.toJson(object) + "\r\n").addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        VotifierSession session = ctx.channel().attr(VotifierSession.KEY).get();

        String remoteAddr = ctx.channel().remoteAddress().toString();
        boolean hasCompletedVote = session.hasCompletedVote();

        if (session.getVersion() == VotifierSession.ProtocolVersion.TWO) {
            JsonObject object = new JsonObject();
            object.addProperty("status", "error");
            object.addProperty("cause", cause.getClass().getSimpleName());
            object.addProperty("error", cause.getMessage());
            ctx.writeAndFlush(GsonInst.gson.toJson(object) + "\r\n").addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.close();
        }
    }

    private boolean willThrottleErrorLogging() {
        long lastErrorAt = this.lastError.get();
        long now = System.currentTimeMillis();

        if (lastErrorAt + 2000 >= now) {
            return this.errorsSent.incrementAndGet() >= 5;
        } else {
            this.lastError.set(now);
            this.errorsSent.set(0);
            return false;
        }
    }
}