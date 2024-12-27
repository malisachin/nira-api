package com.dt.projects.niraproxy.controller;

import com.dt.projects.niraproxy.util.ApiResponse;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AppUtil {
    public static ApiResponse createApiResponse(boolean success, String msg, Object object) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(msg);
        apiResponse.setResult(object);
        apiResponse.setSuccess(success);
        return apiResponse;
    }

    public static Timestamp getTimeStamp() {
        Date date = new Date();
        long time = date.getTime();
//        System.out.println("Time in Milliseconds: " + time);
        Timestamp ts = new Timestamp(time);
        return ts;
    }

    public static String getTimeStampString(Date date) throws ParseException {
        Optional<Date> oDate = Optional.ofNullable(date);
        Date d = oDate.isPresent() ? oDate.get() : new Date();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return f.format(d);
    }

    public static double getDifferenceInSeconds(Date startDate, Date endDate){
        long diff = endDate.getTime()-startDate.getTime();
        return diff/1000.0;
    }

    public static String getCorrelationID(){
        return UUID.randomUUID().toString();
    }

    public static String encrypt(String plainText) {
        String secretKey = "DiGiTaLtRuStTeChNoLoGy";
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);
//			PBKDF2WithHmacSHA256
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), plainText.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }
}
