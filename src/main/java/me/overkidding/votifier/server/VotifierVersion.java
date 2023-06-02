/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package me.overkidding.votifier.server;

/**
 * @author yawkat
 */
public class VotifierVersion {

    private static final VotifierVersion V2 = new VotifierVersion("2.0");
    private static final VotifierVersion V1 = new VotifierVersion("1.9");

    private final String name;

    public static VotifierVersion getDefault() {
        return V1;
    }

    public VotifierVersion(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
