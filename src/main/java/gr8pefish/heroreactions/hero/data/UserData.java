package gr8pefish.heroreactions.hero.data;

import gr8pefish.heroreactions.common.Common;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class UserData {

    public static final UserData TOKEN = new UserData("token");
    public static final UserData ACCOUNT_ID = new UserData("accountID");
    public static final UserData HASH_ID = new UserData("hashID");

    private static final String DEFAULT_DIR = getDefaultDirectory();

    /** Set the file path to something valid - depends on the operating system */
    private static String getDefaultDirectory() {
        String OS = System.getProperty("os.name").toUpperCase();
        if (OS.contains("WIN"))
            return System.getenv("APPDATA");
        else if (OS.contains("MAC"))
            return System.getProperty("user.home") + File.separatorChar + "Library" + File.separatorChar + "Application Support";
        else if (OS.contains("NUX"))
            return System.getProperty("user.home") + File.separatorChar + ".config";
        return System.getProperty("user.dir");
    }

    private final String name;
    private Path path;

    UserData(String name) {
        this.name = File.separatorChar + name + ".txt";
        this.path = Paths.get(DEFAULT_DIR, name);
    }

    public String retrieve() {
        createFile();

        try {
            if (new File(path.toString()).length() == 0)
                return ""; //empty file
            List<String> lines = Files.readAllLines(path);
            return lines.get(0); //first line
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean store(String token) {
        createFile();

        try {
            Files.write(path, Collections.singletonList(token));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void clear() {
        createFile();

        try {
            RandomAccessFile file = new RandomAccessFile(new File(path.toString()), "rw");
            file.setLength(0); //set file to be empty
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFile() {
        Path dataDir = getFilePath();
        Path filePath = Paths.get(dataDir.toString(), name);
        try {
            Files.createFile(filePath);
        } catch (FileAlreadyExistsException e) {
            Common.LOGGER.debug("File {} already exists.", filePath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.path = filePath;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

    private static Path getFilePath() {
        //get path to hero directory
        String heroDir = File.separatorChar + ".minecraft" + File.separatorChar + "hero";
        Path dirPath = Paths.get(DEFAULT_DIR, heroDir);
        //if no .minecraft/hero dir
        if (Files.notExists(dirPath)) {
            //make filepath
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dirPath;
    }
}
