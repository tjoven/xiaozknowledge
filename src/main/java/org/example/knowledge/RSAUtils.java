package org.example.knowledge;



import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * 登陆密码公钥工具类6.0
 * Created by yangrui on 2019/9/25.
 */

public class RSAUtils {
    /**RSA算法*/
    public static final String RSA = "RSA";
    /**加密方式，android的*/
//  public static final String TRANSFORMATION = "RSA/None/NoPadding";
    /**加密方式，标准jdk的*/
    public static final String TRANSFORMATION = "RSA/None/PKCS1Padding";

    /** 使用公钥加密 */
    public static String encryptByPublicKey(String pwd, String key) throws Exception {
        byte[] publicKey = Base64.decodeBase64(key);
        // 得到公钥对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
//        PKCS8EncodedKeySpec keySpec=new PKCS8EncodedKeySpec(publicKey);
//        byte[] buffer =getDecoder().; // 解开公钥的base4
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        // 加密数据
        Cipher cp = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cp.init(Cipher.ENCRYPT_MODE, pubKey);

        byte[] plaintext = pwd.getBytes();
        byte[] output = cp.doFinal(plaintext);

        return  java.util.Base64.getUrlEncoder().encodeToString(output);
    }

}
