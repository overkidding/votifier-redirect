package me.overkidding;

import at.yawk.votifier.VotifierKeyPair;
import at.yawk.votifier.VotifierServerBuilder;
import me.overkidding.votifier.VotifierClient;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static File KEY = new File("key.json");
    public static File CONFIG = new File("config.json");
    public static VotifierClient client;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

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
                        executorService.submit(() -> {
                            try {
                                client.send(vote);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdownAndAwaitTermination();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

    private static final int n = 60;

    private static void shutdownAndAwaitTermination() throws InterruptedException {
        executorService.shutdown();
        if (!executorService.awaitTermination(n, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
            if (!executorService.awaitTermination(n, TimeUnit.SECONDS)) {
                System.out.println("The pool did not terminate");
            }
        }
    }
}