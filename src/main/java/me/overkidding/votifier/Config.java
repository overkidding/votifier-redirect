package me.overkidding.votifier;

import me.overkidding.votifier.server.utils.TokenUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

@Getter
public class Config {

    private String currentHost;
    private int currentPort;
    private String currentToken;

    private String hopHost;
    private int hopPort;
    private String hopToken;

    @SneakyThrows
    public Config(File file){
        if(!file.exists()){
            file.createNewFile();
            JsonObject jsonObject = new JsonObject();
            JsonObject current = new JsonObject();
            current.addProperty("host", "0.0.0.0");
            current.addProperty("port", 8192);
            current.addProperty("token", TokenUtil.newToken());
            jsonObject.add("current", current);

            JsonObject hop = new JsonObject();
            hop.addProperty("host", "insert here");
            hop.addProperty("port", 8192);
            hop.addProperty("token", "insert here");
            jsonObject.add("hop", hop);
            try(FileWriter fileWriter = new FileWriter(file)){
                fileWriter.write(jsonObject.toString());
            }
        }
        JsonObject jsonObject = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();

        JsonObject current = jsonObject.getAsJsonObject("current");
        currentHost = current.get("host").getAsString();
        currentPort = current.get("port").getAsInt();
        currentToken = current.get("token").getAsString();

        JsonObject hop = jsonObject.getAsJsonObject("hop");
        hopHost = hop.get("host").getAsString();
        hopPort = hop.get("port").getAsInt();
        hopToken = hop.get("token").getAsString();
        if(hopHost.equals("insert here") || hopToken.equals("insert here")) throw new Exception("Please configure the config.json file (in the same directory as the jar file)");
    }

}
