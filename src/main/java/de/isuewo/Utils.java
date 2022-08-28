package de.isuewo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {
    public static File downloadFile(URL website, File file, boolean deleteOnExit) {
        try {
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (deleteOnExit) {
            file.deleteOnExit();
        }
        return file;
    }

    public static String getMD5(File file) {
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = new byte[8192];
            int read;
            while ((read = inputStream.read(bytes)) != -1) {
                digest.update(bytes, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            StringBuilder output = new StringBuilder(bigInt.toString(16));
            while (output.length() < 32) {
                output.insert(0, "0");
            }
            return output.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteDir(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    public static void extractZipFile(File zipFilePath, File outputDirectory) {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Path outputPath = Paths.get(String.valueOf(outputDirectory));
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path entryPath = outputPath.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.copy(zipFile.getInputStream(entry), entryPath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}