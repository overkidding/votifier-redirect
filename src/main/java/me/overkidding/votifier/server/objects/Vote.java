/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package me.overkidding.votifier.server.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author yawkat
 */
public class Vote {
    private final String username;
    private final String service;
    private final String address;
    private final String timestamp;

    public Vote(String username, String service, String address, String timestamp) {
        this.username = username;
        this.service = service;
        this.address = address;
        this.timestamp = timestamp;
    }

    public Vote(JsonObject jsonObject){
        this(
                jsonObject.get("username").getAsString(),
                jsonObject.get("serviceName").getAsString(),
                jsonObject.get("address").getAsString(),
                getTimestamp(jsonObject.get("timestamp")));

    }

    @Override
    public String toString() {
        return "Vote{" +
                "username='" + username + '\'' +
                ", service='" + service + '\'' +
                ", address='" + address + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    private static String getTimestamp(JsonElement object) {
        try {
            return Long.toString(object.getAsLong());
        } catch (Exception e) {
            return object.getAsString();
        }
    }

    /**
     * Username of the player that voted.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Name of the service the player voted on.
     */
    public String getService() {
        return service;
    }

    /**
     * IP address the player voted from.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Timestamp of the vote (usually seconds since epoch).
     */
    public String getTimestamp() {
        return timestamp;
    }
}
