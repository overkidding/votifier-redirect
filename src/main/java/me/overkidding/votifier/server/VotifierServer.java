/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package me.overkidding.votifier.server;

/**
 * Votifier TCP server that operates on one listen address.
 *
 * @author yawkat
 */
public interface VotifierServer {
    void start();

    void stop();
}
