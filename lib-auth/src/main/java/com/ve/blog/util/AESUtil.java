package com.ve.blog.util;


import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


/**
 * @Description create for locker-plus .
 * java AES加密工具类AESUtil
 * https://blog.csdn.net/qaz__01/article/details/122809113
 *
 * aes key长度_实用算法之AES加密/解密
 * https://blog.csdn.net/weixin_39954487/article/details/111172586
 *
 * 常见加密算法DES、AES和RSA的原理和特点
 * https://baijiahao.baidu.com/s?id=1687921257620518892&wfr=spider&for=pc
 *
 * AES加密算法原理分析
 * https://blog.csdn.net/yanhaijunyan/article/details/104037400
 * @Author weiyi
 * @Date 2022/4/10
 */
public class AESUtil {
    private static final String ENCODING = "utf-8";
    private static final String KEY_ALGORITHM = "AES";

    /**
     * AES 加密操作
     * @param content 待加密内容
     * @param key     加密密码
     * @return 返回Base64转码后的加密数据
     */
    public static String ecbEncrypt(String content, String key) {
        byte[] data = null;
        try {
            byte[] contentBytes = content.getBytes(ENCODING);
            data = encryptOrDecrypt(Cipher.ENCRYPT_MODE, contentBytes, key, null, EncodeType.AES_ECB_PKCS5Padding);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return data == null ? null : Base64.encodeBase64String(data);
    }

    /**
     * AES 加密操作
     * @param content 待加密内容
     * @param key     加密密码
     * @return 返回Base64转码后的加密数据
     */
    public static String cbcEncrypt(String content, String key, String iv) {
        byte[] data = null;
        try {
            byte[] contentBytes = content.getBytes(ENCODING);
            data = encryptOrDecrypt(Cipher.ENCRYPT_MODE, contentBytes, key, iv, EncodeType.AES_CBC_PKCS5Padding);
        } catch (Exception e) {

            LogUtil.error(e.getMessage(), e);
        }
        return data == null ? null : Base64.encodeBase64String(data);
    }

    /**
     * AES 解密操作
     * @param content
     * @param key
     * @return
     */
    public static String ecbDecrypt(String content, String key) {
        try {
            byte[] contentBytes = Base64.decodeBase64(content);
            byte[] data = encryptOrDecrypt(Cipher.DECRYPT_MODE, contentBytes, key, null, EncodeType.AES_ECB_PKCS5Padding);
            return new String(data, ENCODING);
        } catch (Exception e) {

            LogUtil.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * AES 解密操作
     * @param content
     * @param key
     * @return
     */
    public static String cbcDecrypt(String content, String key, String iv) {
        try {
            byte[] contentBytes = Base64.decodeBase64(content);
            byte[] data = encryptOrDecrypt(Cipher.DECRYPT_MODE, contentBytes, key, iv, EncodeType.AES_CBC_PKCS5Padding);
            return new String(data, ENCODING);
        } catch (Exception e) {

            LogUtil.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 根据时间戳13位，生成aes cbc 加密向量 vi
     * 生成规则先把时间戳反向，用零补足16位
     * @param timestamp
     * @return
     */
    public static String genTimestampIV(String timestamp) {
        return new StringBuffer(timestamp).reverse() + "000";
    }

    private static byte[] encryptOrDecrypt(int mode, byte[] contentBytes, String key, String iv, String modeAndPadding) throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        byte[] keyBytes = key.getBytes(ENCODING);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(modeAndPadding);// 创建密码器
        if (null != iv) {
            //指定一个初始化向量 (Initialization vector，IV)， IV 必须是16位
            byte[] ivBytes = iv.getBytes(ENCODING);
            cipher.init(mode, keySpec, new IvParameterSpec(ivBytes));
        } else {
            cipher.init(mode, keySpec);
        }
        return cipher.doFinal(contentBytes);
    }

    /**
     * 3、加密方式
     * 加密方式分为五种：电码本模式(Electronic Codebook Book (ECB))、密码分组链接模式(Cipher Block Chaining (CBC))、计算器模式(Counter (CTR))、密码反馈模式(Cipher FeedBack (CFB))、输出反馈模式(Output FeedBack (OFB))。实际应用比较多的是ECB和CBC。
     *
     * ECB：将明文按16字节分组，每组分别加密后拼接。
     *
     * CBC：上面ECB缺点是明文内相同的明文块，最终的密文也是相同的，为了更好的隐藏明文信息，针对这个问题就有了CBC模式，
     * 每一小段明文先与初始块向量或者上一段的密文段进行异或运算后，再与密钥进行加密。
     */
    public class EncodeType {
        //    算法/模式/填充                 16字节加密后数据长度       不满16字节加密后长度
        //    AES/CBC/NoPadding                   16                          不支持
        //    AES/CBC/PKCS5Padding                32                          16
        //    AES/CBC/ISO10126Padding             32                          16
        //    AES/CFB/NoPadding                   16                          原始数据长度
        //    AES/CFB/PKCS5Padding                32                          16
        //    AES/CFB/ISO10126Padding             32                          16
        //    AES/ECB/NoPadding                   16                          不支持
        //    AES/ECB/PKCS5Padding                32                          16
        //    AES/ECB/ISO10126Padding             32                          16
        //    AES/OFB/NoPadding                   16                          原始数据长度
        //    AES/OFB/PKCS5Padding                32                          16
        //    AES/OFB/ISO10126Padding             32                          16
        //    AES/PCBC/NoPadding                  16                          不支持
        //    AES/PCBC/PKCS5Padding               32                          16
        //    AES/PCBC/ISO10126Padding            32                          16
        //    默认为 ECB/PKCS5Padding
        public final static String AES_DEFAULT = "AES";
        public final static String AES_CBC_NoPadding = "AES/CBC/NoPadding";
        public final static String AES_CBC_PKCS5Padding = "AES/CBC/PKCS5Padding";
        public final static String AES_CBC_ISO10126Padding = "AES/CBC/ISO10126Padding";
        public final static String AES_CFB_NoPadding = "AES/CFB/NoPadding";
        public final static String AES_CFB_PKCS5Padding = "AES/CFB/PKCS5Padding";
        public final static String AES_CFB_ISO10126Padding = "AES/CFB/ISO10126Padding";
        public final static String AES_ECB_NoPadding = "AES/ECB/NoPadding";
        public final static String AES_ECB_PKCS5Padding = "AES/ECB/PKCS5Padding";
        public final static String AES_ECB_ISO10126Padding = "AES/ECB/ISO10126Padding";
        public final static String AES_OFB_NoPadding = "AES/OFB/NoPadding";
        public final static String AES_OFB_PKCS5Padding = "AES/OFB/PKCS5Padding";
        public final static String AES_OFB_ISO10126Padding = "AES/OFB/ISO10126Padding";
        public final static String AES_PCBC_NoPadding = "AES/PCBC/NoPadding";
        public final static String AES_PCBC_PKCS5Padding = "AES/PCBC/PKCS5Padding";
        public final static String AES_PCBC_ISO10126Padding = "AES/PCBC/ISO10126Padding";
    }

    /**
     * AES加密按秘钥的长度分为128位(比特)、192位和256位，一般记为AES-128、AES-192和AES-256。
     * 一般简短数据采用AES-128，也就是秘钥是16字节，少部分采用AES-256。
     *   中文=3个字节=1个英文或数字符号
     * @param args
     */
    public static void main(String[] args) {
        //String key = "4e8dec21a70d1e0891539b487009b04f";
        String key = "12345678901234561234567890123456";
        String iv = key.substring(0, 16);
        String content = "hello,您好";//ce214b206f650f32

        //使用CBC模式，需要一个向量iv，可增加加密算法的强度
        String encrypt = cbcEncrypt(content, key, iv);
        LogUtil.info("cbc encrypt:{}", encrypt);
        String decrypt = cbcDecrypt(encrypt, key, iv);
        LogUtil.info("cbc decrypt:{}", decrypt);

        encrypt = ecbEncrypt(content, key);
        LogUtil.info("cbc encrypt:{}", encrypt);
        decrypt = ecbDecrypt(encrypt, key);
        LogUtil.info("cbc decrypt:{}", decrypt);

        for (int i = 0; i < 5; i++) {
            iv = genTimestampIV(String.valueOf(System.currentTimeMillis()));
            encrypt = cbcEncrypt(content, key, iv);

            LogUtil.info("for cbc encrypt:{}", encrypt);
            decrypt = cbcDecrypt(encrypt, key, iv);

            LogUtil.info("for cbc decrypt:{}", decrypt);
        }


    }
}