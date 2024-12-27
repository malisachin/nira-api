package com.dt.projects.niraproxy.api;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class ServiceHelper {
    // token credentials
    static String usernameToken = "SyzPmXlpIdI97YfYuJeO5fq53fIa";
    static String passwordToken = "eP5Sxw8WnNoViti6ZnIlUlZoITIa";
    static String baseUrl = "https://api.integration.go.ug";
    static String tokenUrl = baseUrl+"/token";
    static String getIdCardUrl = baseUrl+"/t/nira.go.ug/nira-api/1.0.0/getIdCard?cardNumber=%s";
    static String changePassword = baseUrl+"/t/nira.go.ug/nira-api/1.0.0/changePassword";
//    static String changePassword = "http://localhost:3000/changePassword";

    public static String userCredentialsToken() {
        return Base64.getEncoder().encodeToString(
                (usernameToken+":"+passwordToken)
                        .toString()
                        .getBytes()
        );
    }

    public static String getEncryptedPassword(String newPassword) throws CertificateException, IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        ClassLoader classLoader = ServiceHelper.class.getClassLoader();
        File file = new File(classLoader.getResource("_niragoug.crt").getFile());
        FileInputStream is = new FileInputStream (file);
        X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
        PublicKey key = cer.getPublicKey();
        is.close();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        String encryptedPassword = Base64.getEncoder().encodeToString(
                cipher.doFinal(newPassword.getBytes())
        );
        System.out.println(encryptedPassword);
        return encryptedPassword;
    }
}
