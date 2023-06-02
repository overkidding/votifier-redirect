/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package me.overkidding.votifier.server.impl;

import me.overkidding.votifier.server.VotifierBootstrap;
import me.overkidding.votifier.server.VotifierServer;
import me.overkidding.votifier.server.objects.Vote;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author yawkat
 */
public class VotifierServerImpl implements VotifierServer {
    private final SocketAddress listenAddress;
    private final Consumer<Vote> listener;

    private final VotifierBootstrap bootstrap;
    private ServerBootstrap serverBootstrap;
    private Channel serverChannel;


    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);

    public VotifierServerImpl(
                              SocketAddress listenAddress,
                              Consumer<Vote> listener) {
        this.listenAddress = listenAddress;
        this.listener = listener;

        bootstrap = new VotifierBootstrap(listener);
    }

    public void init() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }

        System.out.println("Initializing votifier server...");
        serverBootstrap = bootstrap.start();
    }

    @Override
    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        init();
        serverChannel = serverBootstrap.bind(listenAddress)
                .addListener((ChannelFutureListener) future ->{
                    if(future.isSuccess()){
                        serverChannel = future.channel();
                        System.out.println("Votifier started on " + listenAddress);
                    }else{
                        System.out.println("Votifier was not able to bind to " + listenAddress.toString());
                        future.cause().printStackTrace();
                    }
                }).awaitUninterruptibly().channel();
    }

    @Override
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            return;
        }

        serverChannel.close();
        serverChannel = null;
    }
}
