package com.dt.projects.niraproxy.api;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

public class PwdServiceHelper {
    public String encryptUsername(String userName)
    {
        return convertToBase64(userName);
    }

    //creates new password of size 9
//    public static String generateDynamicPwd() {
//        return newPassword(9);
//    }

    public static String generateDynamicPwd(String oldPassword) {
        StringBuffer s1 = new StringBuffer ();
        String alphabets = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String specialChars = "@!#_+$%*";
        String digits = "0123456789";
        String smallCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        s1.append(getRandomChar(alphabets));
        s1.append(getRandomChar(alphabets));
        s1.append(getRandomChar(specialChars));
        s1.append(getRandomChar(digits));
        s1.append(getRandomChar(smallCaseLetters));
        s1.append(getRandomChar(upperCaseLetters));
        s1.append(getRandomChar(digits));
        s1.append(getRandomChar(digits));
        String newPwdString = s1.toString();

        if(!oldPassword.equals(newPwdString)) {
            return s1.toString();
        } else {
            return generateDynamicPwd(oldPassword);
        }
    }
    public static char getRandomChar(String characterString){
        return characterString.charAt(new Random().nextInt(characterString.length()));
    }

    //adds random digit to unique string (follows password policy of NIRA)
    private static String newPassword(int n)
    {
        Random random=new Random();
        String string = getAlphaNumericString(n);
        return string+random.nextInt(9);
    }

    //returns a unique string of length n containing alphanumeric and special characters
    private  static String getAlphaNumericString(int n)
    {

        // length is bounded by 256 Character
        byte[] array = new byte[256];
        new Random().nextBytes(array);

        String randomString
                = new String(array, StandardCharsets.UTF_8);

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer();

        // from the generated random String into the result
        for (int k = 0; k < randomString.length(); k++) {

            char ch = randomString.charAt(k);

            if (((ch >= 'a' && ch <= 'z')
                    || (ch >= '0' && ch <= '9')
                    || (ch >= 'A' && ch <= 'Z')
                    ||checkSpChar(ch))
                    && (n > 0)) {

                r.append(ch);
                n--;
            }
        }

        // return the resultant string
        return r.toString();
    }

    //checks for special char
    private static boolean checkSpChar(char ch)
    {
        char[] arr= new char[]{'@','!','#','_','+','$','%','*'};
        for(char c: arr)
        {
            if(ch==c)
                return true;
        }
        return false;
    }

    private static String convertToBase64(String s)
    {
        return Base64.getEncoder().encodeToString(s
                .getBytes(StandardCharsets.UTF_8));
    }

}
