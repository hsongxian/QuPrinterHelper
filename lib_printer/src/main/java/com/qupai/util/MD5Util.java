package com.qupai.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;  

public class MD5Util {  

    public static String getMD5(String str) {  
        try {  
            MessageDigest md = MessageDigest.getInstance("MD5");  
            md.update(str.getBytes());  
            byte[] digest = md.digest();  
            return new BigInteger(1, digest).toString(16);  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
            return null;  
        }  
    }  

}