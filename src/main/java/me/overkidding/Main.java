package me.overkidding;

import at.yawk.votifier.VotifierKeyPair;
import at.yawk.votifier.VotifierServerBuilder;
import me.overkidding.votifier.VotifierClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static File KEY = new File("key.json");
    public static File CONFIG = new File("config.json");
    public static VotifierClient client;

    private static final List<Thread> threads = new ArrayList<>();

    public static void main(String[] args) {
        try {
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
                        Thread t = new Thread(() -> {
                            try {
                                client.send(vote);
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                        });
                        threads.add(t);
                        t.start();
                    }).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for(Thread t : threads){
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}