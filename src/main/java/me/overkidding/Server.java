package me.overkidding;

import at.yawk.votifier.VotifierKeyPair;
import at.yawk.votifier.VotifierServerBuilder;
import me.overkidding.votifier.VotifierClient;

import java.io.File;

public class Server {

    public File KEY = new File("key.json");
    public File CONFIG = new File("config.json");
    public VotifierClient client;

    public Server(){
        try{
            Config config = new Config(CONFIG);
            if (!KEY.exists()) {
                VotifierKeyPair keyPair = VotifierKeyPair.generate();
                keyPair.write(KEY);
            }

            try {
                client = new VotifierClient(config.getHost(), config.getPort(), config.getToken());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            new VotifierServerBuilder().port(8192)
                    .key(VotifierKeyPair.read(KEY))
                    .voteListener(vote -> {
                        System.out.println("Received vote from " + vote.getUsername() + " (" + vote.getAddress() + ") by " + vote.getService());
                        try {
                            client.send(vote);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
        }catch (Exception ex){
            System.out.println("Error while starting server: " + ex.getMessage());
        }
    }

}
