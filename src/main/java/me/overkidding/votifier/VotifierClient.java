package me.overkidding.votifier;

import at.yawk.votifier.Vote;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.InetSocketAddress;
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

    public void send(Vote vote) {
        try(Socket socket = new Socket()){
            socket.connect(new InetSocketAddress(address, port), 10000);

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
            System.out.println("Sent vote to " + address + ":" + port + " with token " + challengeToken);
        }catch (Exception e){
            System.out.println("Error while sending vote: " + e.getMessage());
        }
    }
}
