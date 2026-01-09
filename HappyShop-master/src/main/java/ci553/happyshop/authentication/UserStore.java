package ci553.happyshop.authentication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
public final class UserStore {
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path USERS_FILE = DATA_DIR.resolve("users.csv");

    private UserStore() {}

    public static Map<String, String> loadCustomers(){
        ensureFileExists();
        Map<String, String> creds = new HashMap<>();

        try {
            for(String line : Files.readAllLines(USERS_FILE, StandardCharsets.UTF_8)){
                if (line == null) continue;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 2) continue;

                String u = parts[0].trim();
                String p = parts[1].trim();

                if (!u.isEmpty()) {
                    creds.put(u,p);
                }
        }
    } catch (IOException e) {
            throw new RuntimeException("Failed to load users.csv", e);
        }
        return creds;
    }

    public static void saveCustomers(Map<String, String> customers){
        ensureFileExists();

        StringBuilder out = new StringBuilder();
        out.append("# username,password\n");

        for (Map.Entry<String, String> entry : customers.entrySet()) {
            out.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }

        try {
            Files.writeString(
                    USERS_FILE,
                    out.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to save users.csv", e);
        }
    }
    private static void ensureFileExists() {
        try {
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR); //data folder
            }
            if (!Files.exists(USERS_FILE)) {
                Files.writeString(
                        USERS_FILE,
                        "# username,password\n",
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE
                );
            }
        }catch (IOException e) {
            throw new RuntimeException("Failed to load users.csv", e);
        }
    }
}
