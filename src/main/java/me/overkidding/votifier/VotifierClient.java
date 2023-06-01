package me.overkidding.votifier;

import at.yawk.votifier.Vote;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.Socket;

@Getter
public class VotifierClient extends VotifierImplementation {

    private final String address;
    private final int port;
    @Setter private String token;

    public VotifierClient(String address, int port, String token) {
        this.address = address;
        this.port = port;
        this.token = token;
    }

    public void send(Vote vote) throws Exception {
        Socket socket = new Socket(address, port);

        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();

        String in = new BufferedReader(new InputStreamReader(is)).readLine();
        String[] splitIn = in.split(" ");
        String challengeToken = splitIn[2];

        byte[] message = encode(vote, challengeToken, token);
        os.write(message);
        os.flush();
        System.out.println(new BufferedReader(new InputStreamReader(is)).readLine());

        os.close();
        socket.close();
        System.out.println("Sent vote to " + address + ":" + port + " with token " + challengeToken);
    }
}
