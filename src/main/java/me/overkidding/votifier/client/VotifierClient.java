package me.overkidding.votifier.client;

import me.overkidding.votifier.server.objects.Vote;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@Getter
public class VotifierClient extends VotifierImplementation {

    private final String address;
    private final int port;
    @Setter
    private String token;

    public VotifierClient(String address, int port, String token) {
        this.address = address;
        this.port = port;
        this.token = token;
    }

    public void send(Vote vote) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(address, port), 10000);

            try (InputStream is = socket.getInputStream() ; OutputStream os = socket.getOutputStream() ; BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String in = br.readLine();
                String[] splitIn = in.split(" ");
                String challengeToken = splitIn[2];

                byte[] message = encode(vote, challengeToken, token);
                os.write(message);
                os.flush();
                System.out.println("Sent vote to " + address + ":" + port + " with token " + challengeToken);
            }
        } catch (Exception e) {
            System.out.println("Error while sending vote: " + e.getMessage());
        }
    }
}