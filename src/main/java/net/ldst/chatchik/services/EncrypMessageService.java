package net.ldst.chatchik.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

@Service
@Slf4j
public class EncrypMessageService {

    @Value("${secret.pass}")
    private String sek;

    public String encode(String key, String message) {
        try {
//            if (key.length() != 36) {
//                return "key error";
//            }

            key = key.replaceAll("-", "");
            key = key.substring(0, 16);
            String base64Key = Base64.getEncoder().encodeToString(key.getBytes());

            // prepare key;
            SecretKey kkey = new SecretKeySpec(
                    Base64.getDecoder().decode(base64Key), "AES"
            );
            // prepare iv vector
            key = key.substring(0, 16);
            base64Key = Base64.getEncoder().encodeToString(key.getBytes());
            AlgorithmParameterSpec iv = new IvParameterSpec(
                    Base64.getDecoder().decode(base64Key)
            );
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, kkey, iv);
            return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception e) {
            e.printStackTrace();
            return "encrypt error";
        }
    }

    public String decode(String key, String message) {
        try {
            if (key.length() != 36) {
                return "key error";
            }

            key = key.replaceAll("-", "");
            key = key.substring(0, 16);
            String base64Key = Base64.getEncoder().encodeToString(key.getBytes());

            // prepare key;
            SecretKey kkey = new SecretKeySpec(
                    Base64.getDecoder().decode(base64Key), "AES"
            );
            // prepare iv vector
            AlgorithmParameterSpec iv = new IvParameterSpec(
                    Base64.getDecoder().decode(base64Key)
            );
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, kkey, iv);
            return new String(cipher.doFinal(Base64.getDecoder().decode(message)));
        }
        catch (Exception e) {
            e.printStackTrace();
            return "encrypt error";
        }
    }

    public String CBCEncrypter (String key, String message) {
        try {
            if (key.length() != 36) {
                return "key error";
            }

            key = key.replaceAll("-", "");
            key = key + sek;
            key = key.substring(0, 32);
            Key kkey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, kkey);
            byte[] encVal = c.doFinal(message.getBytes());
            String encryptedValue = new String(Base64.getEncoder().encode(encVal));
            return encryptedValue;
        }
        catch (Exception e) {
            e.printStackTrace();
            return "encrypt error";
        }
    }

    public String CBCDecrypter (String key, String message) {
        try {
            if (key.length() != 36) {
                return "key error";
            }

            key = key.replaceAll("-", "");
            key = key + sek;
            key = key.substring(0, 32);
            Key kkey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, kkey);
            byte[] encVal = c.doFinal(Base64.getDecoder().decode(message.getBytes()));
            String encryptedValue = new String(encVal);
            return encryptedValue;
        }
        catch (Exception e) {
            e.printStackTrace();
            return "decrypt error";
        }
    }

    public String Hash(String input)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return new String(messageDigest, StandardCharsets.UTF_8);

        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
