package com.company.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public class Security {
    public Security() {}

    public String getHash(String passwordToHash, byte[] salt) {
        String generatedPassword = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(salt);

            byte[] bytes = md.digest(passwordToHash.getBytes());

            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return generatedPassword;
    }

    public byte[] getSalt() {
        byte[] salt = null;

        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            salt = new byte[16];
            sr.nextBytes(salt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return salt;
    }

    public boolean correctPassword(String password, byte[] salt, String hash) {
        return getHash(password, salt).equals(hash);
    }
}
