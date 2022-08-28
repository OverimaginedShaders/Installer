package de.isuewo;

import com.google.gson.Gson;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    static Scanner in = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        URL dataURL = new URL("https://raw.githubusercontent.com/OverimaginedShaders/Secret/main/data.json");
        String installerVersion = "1.0-beta7";

        if (System.console() == null) {
            Files.write(Paths.get("README.txt"), "Please run this installer with your system console like this:\njava -jar <path to this installer>".getBytes());
            return;
        }

        System.out.println("Downloading update information...");
        Data data = new Gson().fromJson(new String(Files.readAllBytes(Paths.get(Utils.downloadFile(dataURL, new File("data.json"), true).toString()))), Data.class);

        File installer = new File(data.getInstaller());
        if (data.installerVersion.equals(installerVersion)) {
            System.out.println("No update available.");
        } else if (installer.exists()) {
            System.out.println("Updated installer already exists. Please use that version instead. Exiting...");
            return;
        } else {
            System.out.print("An updated installer is available. Do you want to download it? (Y/n) ");
            String answer = in.nextLine();
            if (answer.equals("Y") || answer.equals("y") || answer.equals("")) {
                System.out.println("Downloading updated installer...");
                Utils.downloadFile(data.getInstallerURL(), installer, false);
                if (installer.exists()) {
                    System.out.println("Download complete. You can now run the updated installer. Exiting...");
                } else {
                    System.out.println("Error: Download failed.");
                }
                return;
            } else {
                System.out.println("Update aborted.");
            }
        }

        System.out.println("\nWelcome to the " + data.editedShaderpackName + " installer!");
        System.out.println("This installer will help you to install " + data.editedShaderpackName + ".\n");

        String os = System.getProperty("os.name").toLowerCase();
        File dir;
        if (os.contains("windows")) {
            dir = new File(System.getenv("APPDATA") + "\\.minecraft\\shaderpacks");
        } else if (os.contains("mac")) {
            dir = new File(System.getProperty("user.home") + "/Library/Application Support/minecraft/shaderpacks");
        } else {
            dir = new File(System.getProperty("user.home") + "/.minecraft/shaderpacks");
        }
        dir = getDir(dir);

        File shaderpack = data.getShaderpackFile(dir);

        if (!shaderpack.exists() || !Objects.equals(Utils.getMD5(shaderpack), data.shaderpackMD5)) {
            System.out.println("\nThe required version " + data.shaderpackVersion + " of " + data.shaderpackName + " wasn't found in the shaderpacks folder! Download it from: " + data.shaderpackURL + ". Exiting...");
            return;
        }

        List<File> files = new ArrayList<>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().startsWith(data.editedShaderpackFileName) && !file.getName().endsWith(data.editedShaderpackVersion)) {
                files.add(file);
            }
        }
        if (!files.isEmpty()) {
            System.out.println("\nThe following outdated version(s) of " + data.editedShaderpackName + " was/were found in the shaderpacks folder:");
            for (File file : files) {
                System.out.println(" - " + file.getName());
            }
            System.out.print("Do you want to delete it/them? (Y/n) ");
            String answer = in.nextLine();
            if (answer.equals("Y") || answer.equals("y") || answer.equals("")) {
                for (File file : files) {
                    Utils.deleteDir(file);
                }
            } else {
                System.out.println("Deletion aborted.");
            }
        }

        File editedShaderpack = data.getEditedShaderpackFile(dir);
        String editedShaderpackNameVersion = data.getEditedShaderpackNameVersion();
        if (editedShaderpack.exists()) {
            System.out.print("\n" + editedShaderpackNameVersion + " is already installed. Do you want to reinstall it? (Y/n) ");
            String answer = in.nextLine();
            if (answer.equals("Y") || answer.equals("y") || answer.equals("")) {
                System.out.println("Deleting old version...");
                Utils.deleteDir(editedShaderpack);
            } else {
                System.out.println("Reinstallation aborted. Exiting...");
                return;
            }
        }

        Utils.extractZipFile(shaderpack, editedShaderpack);

        File editedShaderpackPatch = new File(dir + File.separator + data.getEditedShaderpackPatch());
        System.out.println("\nDownloading " + editedShaderpackPatch.getName() + "...");
        Utils.downloadFile(data.getEditedShaderpackPatchURL(), editedShaderpackPatch, true);
        if (!editedShaderpackPatch.exists()) {
            System.out.println("Error: Download failed.");
            return;
        }

        System.out.println("\nApplying patch...");
        try {
            Git.init().setDirectory(editedShaderpack).call();
            Git git = Git.open(editedShaderpack);
            git.apply().setPatch(Files.newInputStream(Paths.get(editedShaderpack + ".patch"))).call();
        } catch (Exception e) {
            System.out.println("Error: Patch failed.");
            return;
        }

        System.out.println("\nCleaning up...");
        Utils.deleteDir(new File(editedShaderpack + File.separator + ".git"));

        System.out.println("\n" + editedShaderpackNameVersion + " was successfully installed!");
        System.out.println("You can now start Minecraft with the new shaderpack.");
        System.out.println("If you want to uninstall it, just delete the folder: " + editedShaderpack);
        System.out.println("Have fun!");
        System.out.println("-" + data.editedShaderpackAuthor);
    }

    public static File getDir(File dir) {
        if (dir.isDirectory()) {
            System.out.print("Valid directory: " + dir + "\nDo you want to use it? (Y/n) ");
            String answer = in.nextLine();
            if (answer.equals("y") || answer.equals("Y") || answer.equals("")) {
                return dir;
            }
        } else System.out.println("Invalid directory: " + dir);
        System.out.print("Enter path to shaderpacks: ");
        return getDir(new File(in.nextLine()));
    }
}
