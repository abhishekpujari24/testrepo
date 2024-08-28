package com.test;

import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class App {

    private static final int RANDOM_STRING_LENGTH = 8;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN Number> <path to json file>");
            return;
        }

        String prnNumber = args[0].toLowerCase();
        String jsonFilePath = args[1];

        File jsonFile = new File(jsonFilePath);

        // Check if file exists, otherwise create a default JSON file
        if (!jsonFile.exists()) {
            System.out.println("File does not exist. Creating a default JSON file.");
            createDefaultJsonFile(jsonFilePath);
            return;
        }

        String destinationValue = getDestinationValue(jsonFile);

        if (destinationValue == null) {
            System.out.println("Key 'destination' not found in the JSON file.");
            return;
        }

        String randomString = generateRandomString();
        String concatenatedString = prnNumber + destinationValue + randomString;
        String md5Hash = generateMD5Hash(concatenatedString);

        System.out.println(md5Hash + ";" + randomString);
    }

    private static String getDestinationValue(File jsonFile) {
        try (FileReader reader = new FileReader(jsonFile)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);
            return findDestinationValue(jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String findDestinationValue(JSONObject jsonObject) {
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                String result = findDestinationValue((JSONObject) value);
                if (result != null) return result;
            } else if (key.equals("destination")) {
                return value.toString();
            }
        }
        return null;
    }

    private static String generateRandomString() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomString = new StringBuilder(RANDOM_STRING_LENGTH);
        Random random = new Random();
        for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
            randomString.append(characters.charAt(random.nextInt(characters.length())));
        }
        return randomString.toString();
    }

    private static String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void createDefaultJsonFile(String path) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", 10);
        jsonObject.put("key2", new JSONObject().put("key3", "value3").put("destination", "defaultValue"));
        jsonObject.put("key4", new JSONObject().put("destination", "anotherDefaultValue"));

        try (FileWriter file = new FileWriter(path)) {
            jsonObject.write(file);
            System.out.println("Default JSON file created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}