package me.overkidding;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

@Getter
public class Config {

    private String host;
    private int port;
    private String token;

    @SneakyThrows
    public Config(File file){
        if(!file.exists()){
            file.createNewFile();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("host", "insert here");
            jsonObject.addProperty("port", 8192);
            jsonObject.addProperty("token", "insert here");
            try(FileWriter fileWriter = new FileWriter(file)){
                fileWriter.write(jsonObject.toString());
            }
        }
        JsonObject jsonObject = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
        host = jsonObject.get("host").getAsString();
        port = jsonObject.get("port").getAsInt();
        token = jsonObject.get("token").getAsString();

        if(host.equals("insert here") || token.equals("insert here")) throw new Exception("Please configure the config.json file (in the same directory as the jar file)");
    }

}
