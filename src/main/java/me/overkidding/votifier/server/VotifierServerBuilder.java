/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package me.overkidding.votifier.server;

import me.overkidding.votifier.server.impl.VotifierServerImpl;
import me.overkidding.votifier.server.objects.Vote;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Builder for a VotifierServer instance.
 *
 * @author yawkat
 */
public class VotifierServerBuilder {
    private Logger logger = Logger.getLogger("at.yawk.votifier");
    private InetSocketAddress listenAddress = InetSocketAddress.createUnresolved("0.0.0.0", 8192);
    private Consumer<Vote> voteListener =
            e -> logger.warning("Received vote event, but no listener is registered!");

    public VotifierServerBuilder logger(Logger logger) {
        Objects.requireNonNull(logger);
        this.logger = logger;
        return this;
    }

    public VotifierServerBuilder listenAddress(InetSocketAddress address) {
        Objects.requireNonNull(address);
        this.listenAddress = address;
        return this;
    }

    public VotifierServerBuilder address(InetAddress address) {
        return listenAddress(new InetSocketAddress(address, this.listenAddress.getPort()));
    }

    public VotifierServerBuilder port(int port) {
        return listenAddress(new InetSocketAddress(this.listenAddress.getAddress(), port));
    }

    public VotifierServerBuilder voteListener(Consumer<Vote> listener) {
        Objects.requireNonNull(listener);
        this.voteListener = listener;
        return this;
    }

    public VotifierServer build() {
        return new VotifierServerImpl(listenAddress, voteListener);
    }

    /**
     * build() and start the server.
     */
    public VotifierServer start() {
        VotifierServer server = build();
        server.start();
        return server;
    }
}
