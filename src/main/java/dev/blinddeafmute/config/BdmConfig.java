package dev.blinddeafmute.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public final class BdmConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("bdm.json");
    private static boolean testCommandEnabled;
    private static int voicePort = 24454;
    private static boolean voiceTestEnabled = true;
    private static String devPassword;

    private BdmConfig() {
    }

    public static void load() {
        if (!Files.exists(PATH)) {
            devPassword = generatePassword();
            save();
            return;
        }
        try {
            JsonObject json = JsonParser.parseString(Files.readString(PATH)).getAsJsonObject();
            testCommandEnabled = json.has("testCommandEnabled") && json.get("testCommandEnabled").getAsBoolean();
            voicePort = json.has("voicePort") ? clampPort(json.get("voicePort").getAsInt()) : 24454;
            voiceTestEnabled = !json.has("voiceTestEnabled") || json.get("voiceTestEnabled").getAsBoolean();
            devPassword = json.has("devPassword") ? json.get("devPassword").getAsString() : generatePassword();
            if (devPassword.isBlank() || devPassword.equals("GENERATED_ON_FIRST_SERVER_START")) {
                devPassword = generatePassword();
            }
            if (!json.has("devPassword") || json.get("devPassword").getAsString().equals("GENERATED_ON_FIRST_SERVER_START")) {
                save();
            }
        } catch (Exception exception) {
            testCommandEnabled = false;
            voicePort = 24454;
            voiceTestEnabled = true;
            devPassword = generatePassword();
            save();
        }
    }

    public static void save() {
        JsonObject json = new JsonObject();
        json.addProperty("testCommandEnabled", testCommandEnabled);
        json.addProperty("voicePort", voicePort);
        json.addProperty("voiceTestEnabled", voiceTestEnabled);
        json.addProperty("devPassword", devPassword);
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(json));
        } catch (IOException ignored) {
        }
    }

    public static boolean isTestCommandEnabled() {
        return testCommandEnabled;
    }

    public static void setTestCommandEnabled(boolean enabled) {
        testCommandEnabled = enabled;
        save();
    }

    public static int getVoicePort() {
        return voicePort;
    }

    public static void setVoicePort(int port) {
        voicePort = clampPort(port);
        save();
    }

    public static boolean isVoiceTestEnabled() {
        return voiceTestEnabled;
    }

    public static boolean matchesDevPassword(String password) {
        return devPassword != null && devPassword.equals(password);
    }

    public static String getDevPassword() {
        return devPassword;
    }

    public static void setVoiceTestEnabled(boolean enabled) {
        voiceTestEnabled = enabled;
        save();
    }

    private static int clampPort(int port) {
        return Math.max(1, Math.min(65535, port));
    }

    private static String generatePassword() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(16);
        for (int index = 0; index < 16; index++) {
            password.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return password.toString();
    }
}
