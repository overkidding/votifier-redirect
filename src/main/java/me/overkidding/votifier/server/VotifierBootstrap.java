package me.overkidding.votifier.server;

import me.overkidding.votifier.server.handlers.VoteInboundHandler;
import me.overkidding.votifier.server.handlers.VotifierGreetingHandler;
import me.overkidding.votifier.server.handlers.VotifierProtocolDifferentiator;
import me.overkidding.votifier.server.objects.Vote;
import me.overkidding.votifier.server.objects.VotifierSession;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.function.Consumer;

public class VotifierBootstrap {

    private static final boolean USE_EPOLL = Epoll.isAvailable();

    private final EventLoopGroup bossLoopGroup;
    private final EventLoopGroup eventLoopGroup;
    private final boolean v1Disable;
    private final Consumer<Vote> listener;

    public VotifierBootstrap(Consumer<Vote> listener) {
        this.listener = listener;
        this.v1Disable = false;
        if (USE_EPOLL) {
            this.bossLoopGroup = new EpollEventLoopGroup();
            this.eventLoopGroup = new EpollEventLoopGroup();
            System.out.println("Using epoll transport to accept votes.");
        } else {
            this.bossLoopGroup = new NioEventLoopGroup();
            this.eventLoopGroup = new NioEventLoopGroup();
            System.out.println("Using NIO transport to accept votes.");
        }
    }

    public ServerBootstrap start() {
        VoteInboundHandler voteInboundHandler = new VoteInboundHandler(listener);

        return new ServerBootstrap()
                .channel(USE_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .group(bossLoopGroup, eventLoopGroup)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        channel.attr(VotifierSession.KEY).set(new VotifierSession());
                        channel.pipeline().addLast("greetingHandler", VotifierGreetingHandler.INSTANCE);
                        channel.pipeline().addLast("protocolDifferentiator", new VotifierProtocolDifferentiator(false, !v1Disable));
                        channel.pipeline().addLast("voteHandler", voteInboundHandler);
                    }
                });
    }

}
