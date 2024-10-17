package me.overkidding.votifier;

import me.overkidding.votifier.server.VotifierServerBuilder;
import me.overkidding.votifier.server.objects.VotifierKeyPair;
import lombok.Getter;
import me.overkidding.votifier.client.VotifierClient;

import java.io.File;
import java.net.InetSocketAddress;

@Getter
public class Server {

    private final File KEY = new File("key.json");
    private final File CONFIG = new File("config.json");
    private VotifierClient client;
    private final Config config = new Config(CONFIG);
    private final VotifierKeyPair keyPair;

    public Config getConfig() {
        return config;
    }

    public VotifierKeyPair getKeyPair() {
        return keyPair;
    }

    public Server(){
        try{
            if (!KEY.exists()) {
                keyPair = VotifierKeyPair.generate();
                keyPair.write(KEY);
            }else{
                keyPair = VotifierKeyPair.read(KEY);
            }

            try {
                client = new VotifierClient(config.getHopHost(), config.getHopPort(), config.getHopToken());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            new VotifierServerBuilder()
                    .listenAddress(new InetSocketAddress(config.getCurrentHost(), config.getCurrentPort()))
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
            throw new RuntimeException(ex);
        }
    }

}
