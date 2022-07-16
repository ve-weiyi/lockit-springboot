package com.ve.blog.util;

import com.ve.blog.exception.BizException;

import javax.crypto.Cipher;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description hello word!
 * https://blog.csdn.net/qy20115549/article/details/83105736
 * <p>
 * https://blog.csdn.net/cz0217/article/details/78426733
 * <p>
 * 缺少Base64和io类：Java：Apache Commons 工具类介绍及简单使用
 * https://www.cnblogs.com/nhdlb/p/14070643.html
 * <p>
 * *** RSA加密解密实现
 * https://blog.csdn.net/hustpzb/article/details/72734578
 * *** RSA公钥私钥提取 N、e、d这三个值
 * 原文地址： https://www.cnblogs.com/AloneSword/p/3326750.html
 * RSA是目前最有影响力的公钥加密算法，该算法基于一个十分简单的数论事实：将两个大素数相乘十分容易，但那时想要对其乘积进行因式分解却极其困难，因此可以将乘积公开作为加密密钥，即公钥，而两个大素数组合成私钥。公钥是可发布的供任何人使用，私钥则为自己所有，供解密之用。
 * 解密者拥有私钥，并且将由私钥计算生成的公钥发布给加密者。加密都使用公钥进行加密，并将密文发送到解密者，解密者用私钥解密将密文解码为明文。
 * 以甲要把信息发给乙为例，首先确定角色：甲为加密者，乙为解密者。首先由乙随机确定一个KEY，称之为密匙，将这个KEY始终保存在机器B中而不发出来；然后，由这个 KEY计算出另一个KEY，称之为公匙。这个公钥的特性是几乎不可能通过它自身计算出生成它的私钥。接下来通过网络把这个公钥传给甲，甲收到公钥后，利用公钥对信息加密，并把密文通过网络发送到乙，最后乙利用已知的私钥，就对密文进行解码了。以上就是RSA算法的工作流程。
 * 算法实现过程为：
 * <p>
 * 1. 随意选择两个大的质数p和q，p不等于q，计算N=pq。
 * 2. 根据欧拉函数，不大于N且与N互质的整数個数為(p-1)(q-1)。
 * 3. 选择一个整数e与(p-1)(q-1)互质，并且e小于(p-1)(q-1)。
 * 4. 用以下这个公式计算d：d× e ≡ 1 (mod (p-1)(q-1))。
 * 5. 将p和q的记录销毁。
 * <p>
 * 以上内容中，(N,e)是公钥，(N,d)是私钥。
 * 下面讲解RSA算法的应用。
 * RSA使用X509EncodedKeySpec、PKCS8EncodedKeySpec生成公钥和私钥
 * @Author weiyi
 * @Date 2022/1/4
 */
public final class RSAUtils {
    private static String RSA_ALGORITHM = "RSA";
    public static final String CHARSET = "UTF-8";
    public static final int defaultSize = 512;
    public static final String defaultContent = "站在大明门前守卫的禁卫军，事先没有接到\n" +
            "有关的命令，但看到大批盛装的官员来临，也就\n" +
            "以为确系举行大典，因而未加询问。进大明门即\n" +
            "为皇城。文武百官看到端门午门之前气氛平静，\n" +
            "城楼上下也无朝会的迹象，既无几案，站队点名\n" +
            "的御史和御前侍卫“大汉将军”也不见踪影，不免\n" +
            "心中揣测，互相询问：所谓午朝是否讹传？";

    /**
     * 随机生成RSA密钥对(默认密钥长度为1024)
     * @return
     */
    public static KeyPair generateRSAKeyPair() {
        return generateRSAKeyPair(defaultSize);
    }

    /**
     * 随机生成RSA密钥对
     * @param keyLength 密钥长度，范围：512～2048<br>一般1024
     * @return
     */
    public static KeyPair generateRSAKeyPair(int keyLength) {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        kpg.initialize(keyLength);
        return kpg.genKeyPair();
    }

    /**
     *
     * @param data 数据
     * @param keyStr 秘钥串
     * @param keyType 秘钥类型
     * @param model 加解密模式
     * @return 加密后的字符串
     */
    public static String parsingRSAString(String data, String keyStr, Integer keyType, Integer model){
        try{
            if(keyType==0&&model==0){
                //私钥解密
                RSAPrivateKey privateKey=(RSAPrivateKey)RSAUtils.getPrivateKey(keyStr);
                return  privateDecrypt(data, privateKey);
            }else if(keyType==0&&model==1){
                //私钥加密
                RSAPrivateKey privateKey=(RSAPrivateKey)RSAUtils.getPrivateKey(keyStr);
                return  privateEncrypt(data, privateKey);
            }else if(keyType==1&&model==0){
                //公钥解密
                RSAPublicKey publicKey = (RSAPublicKey) RSAUtils.getPublicKey(keyStr);
                return  publicDecrypt(data,publicKey);
            }else if(keyType==1&&model==1){
                //公钥加密
                RSAPublicKey publicKey = (RSAPublicKey) RSAUtils.getPublicKey(keyStr);
                return  publicEncrypt(data, publicKey);
            }{
                throw new BizException("秘钥类型或加密模式错误");
            }
        }catch (Exception e) {
            throw new BizException("加密字符串[" + data + "]时遇到异常", e);
        }
    }

    /**
     * 公钥加密
     * @param data
     * @param publicKeyStrSave
     * @return
     */
    public static String publicEncrypt(String data, String publicKeyStrSave) {
        try {
            RSAPublicKey publicKey = (RSAPublicKey) RSAUtils.getPublicKey(publicKeyStrSave);
            return publicEncrypt(data, publicKey);
        } catch (Exception e) {
            throw new BizException("加密字符串[" + data + "]时遇到异常", e);
        }
    }

    /**
     * 私钥解密
     * @param data
     * @param privateKeyStrSave
     * @return
     */
    public static String privateDecrypt(String data, String privateKeyStrSave) {
        try {
            RSAPrivateKey privateKey=(RSAPrivateKey) RSAUtils.getPrivateKey(privateKeyStrSave);
            return privateDecrypt(data,privateKey);
        } catch (Exception e) {
            throw new BizException("解密字符串[" + data + "]时遇到异常", e);
        }
    }





    /**
     * 公钥加密
     * @param data
     * @param publicKey
     * @return
     */
    public static String publicEncrypt(String data, RSAPublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return encoder.encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), publicKey.getModulus().bitLength()));
        } catch (Exception e) {
            throw new BizException("加密字符串[" + data + "]时遇到异常", e);
        }
    }

    /**
     * 私钥解密
     * @param data
     * @param privateKey
     * @return
     */
    public static String privateDecrypt(String data, RSAPrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, decoder.decode(data), privateKey.getModulus().bitLength()), CHARSET);
        } catch (Exception e) {
            throw new BizException("解密字符串[" + data + "]时遇到异常", e);
        }
    }

    /**
     * 私钥加密
     * @param data
     * @param privateKey
     * @return
     */
    public static String privateEncrypt(String data, RSAPrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return encoder.encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), privateKey.getModulus().bitLength()));
        } catch (Exception e) {
            throw new BizException("加密字符串[" + data + "]时遇到异常", e);
        }
    }

    /**
     * 公钥解密
     * @param data
     * @param publicKey
     * @return
     */
    public static String publicDecrypt(String data, RSAPublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            // 编码前设定编码方式及密钥
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, decoder.decode(data), publicKey.getModulus().bitLength()), CHARSET);
        } catch (Exception e) {
            throw new BizException("解密字符串[" + data + "]时遇到异常", e);
        }
    }

    /**
     * 每次加密的字节数，不能超过密钥的长度值除以 8 再减去 11，所以采取分段加密的方式规避
     * @return 加密后的byte型数据
     */
    private static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas, int keySize) {
        int maxBlock = 0;
        if (opmode == Cipher.DECRYPT_MODE) {
            //解密模式
            maxBlock = keySize / 8;
        } else {
            maxBlock = keySize / 8 - 11;
        }
        //如果明文长度大于模长-11则要分组加密
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] buff;
        int i = 0;
        try {
            // 对数据分段加密
            while (datas.length > offSet) {
                if (datas.length - offSet > maxBlock) {
                    buff = cipher.doFinal(datas, offSet, maxBlock);
                } else {
                    buff = cipher.doFinal(datas, offSet, datas.length - offSet);
                }
                out.write(buff, 0, buff.length);
                i++;
                offSet = i * maxBlock;
            }
            byte[] resultDatas = out.toByteArray();
            out.close();
            return resultDatas;

        } catch (Exception e) {
            throw new BizException("加/解密阀值为[" + maxBlock + "]的数据时发生异常", e);
        }
    }

    /**
     * 通过公钥byte[](publicKey.getEncoded())将公钥还原，适用于RSA算法
     * RSA使用X509EncodedKeySpec生成公钥、 PKCS8EncodedKeySpec生成私钥
     * @param keyBytes
     * @return
     *
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PublicKey getPublicKey(byte[] keyBytes) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        X509EncodedKeySpec pkcs8KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(pkcs8KeySpec);
        return publicKey;
    }

    /**
     * 通过私钥byte[]将公钥还原，适用于RSA算法
     * RSA使用X509EncodedKeySpec生成公钥、 PKCS8EncodedKeySpec生成私钥
     * @param keyBytes
     * @return
     *
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey getPrivateKey(byte[] keyBytes) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        return privateKey;
    }

    /**
     * 使用N、e值还原公钥
     * @param modulus
     * @param publicExponent
     * @return
     *
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PublicKey getPublicKey(String modulus, String publicExponent)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger bigIntModulus = new BigInteger(modulus);
        BigInteger bigIntPrivateExponent = new BigInteger(publicExponent);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(bigIntModulus, bigIntPrivateExponent);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    /**
     * 使用N、d值还原私钥
     * @param modulus
     * @param privateExponent
     * @return
     *
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey getPrivateKey(String modulus, String privateExponent)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger bigIntModulus = new BigInteger(modulus);
        BigInteger bigIntPrivateExponent = new BigInteger(privateExponent);
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(bigIntModulus, bigIntPrivateExponent);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    /**
     * 从字符串中加载公钥
     * RSA使用X509EncodedKeySpec生成公钥、 PKCS8EncodedKeySpec生成私钥
     * @param publicKeyStr 公钥数据字符串
     * @throws Exception 加载公钥时产生的异常
     */
    public static PublicKey getPublicKey(String publicKeyStr) throws Exception {
        try {
             byte[] buffer = decoder.decode(publicKeyStr);
            //byte[] buffer = decoder.decode(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(buffer);
            return (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }

    /**
     * 从字符串中加载私钥<br>
     * 加载时使用的是PKCS8EncodedKeySpec（PKCS#8编码的Key指令）。
     * RSA使用X509EncodedKeySpec生成公钥、 PKCS8EncodedKeySpec生成私钥
     * @param privateKeyStr
     * @return
     *
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String privateKeyStr) throws Exception {
        try {
            //使用encode加密需要以decode还原
            //使用encodeSave加密需要decodeSave还原
            byte[] buffer = decoder.decode(privateKeyStr);
            //byte[] buffer = decoder.decode(privateKeyStr);
            //X509EncodedKeySpec pkcs8KeySpec = new X509EncodedKeySpec(buffer);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("私钥非法");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }

    /**
     * 从文件中输入流中加载公钥
     * @param in 公钥输入流
     * @throws Exception 加载公钥时产生的异常
     */
    public static PublicKey getPublicKey(InputStream in) throws Exception {
        try {
            return getPublicKey(readKey(in));
        } catch (IOException e) {
            throw new Exception("公钥数据流读取错误");
        } catch (NullPointerException e) {
            throw new Exception("公钥输入流为空");
        }
    }

    /**
     * 从文件中加载私钥
     * @param in
     * @return 私钥
     *
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(InputStream in) throws Exception {
        try {
            return getPrivateKey(readKey(in));
        } catch (IOException e) {
            throw new Exception("私钥数据读取错误");
        } catch (NullPointerException e) {
            throw new Exception("私钥输入流为空");
        }
    }

    /**
     * 读取密钥信息
     * @param in
     * @return
     *
     * @throws IOException
     */
    private static String readKey(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String readLine = null;
        StringBuilder sb = new StringBuilder();
        while ((readLine = br.readLine()) != null) {
            if (readLine.charAt(0) == '-') {
                continue;
            } else {
                sb.append(readLine);
                sb.append('\r');
            }
        }
        return sb.toString();
    }

    public static Map<String, String> generateRSAKeyStrMap() {
        KeyPair keyPair = generateRSAKeyPair();
        /******************* key->keyData->keyStr ********************/
        //key
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        //keyData
        byte[] publicKeyData = publicKey.getEncoded();
        byte[] privateKeyData = privateKey.getEncoded();
        //keyStr
        String publicKeyStrSave = saveEncoder.encodeToString(publicKeyData);
        String privateKeyStrSave = saveEncoder.encodeToString(privateKeyData);
        /******************* key->keyData->keyStr ********************/
        HashMap<String, String> keyStrMap = new HashMap<>();
        keyStrMap.put("publicKeyStr", publicKeyStrSave);
        keyStrMap.put("privateKeyStr", privateKeyStrSave);
        return keyStrMap;
    }
    
    private static final Encoder encoder= Base64.getEncoder();
    private static final Decoder decoder= Base64.getDecoder();
    private static final Encoder saveEncoder= Base64.getUrlEncoder();
    private static final Decoder saveDecoder= Base64.getUrlDecoder();

    /**
     * 使用encode加密需要以decode还原
     * 使用encodeSave加密需要decodeSave还原
     * @throws Exception
     */
    public static void main(String... args) throws Exception {
        KeyPair keyPair = RSAUtils.generateRSAKeyPair();

        /******************* key->keyData->keyStr ********************/
        //key
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        //keyData
        byte[] publicKeyData = publicKey.getEncoded();
        byte[] privateKeyData = privateKey.getEncoded();
        //keyStr
        String publicKeyStrSave = saveEncoder.encodeToString(publicKeyData);
        String privateKeyStrSave = saveEncoder.encodeToString(privateKeyData);
        /******************* key->keyData->keyStr ********************/

        /******************* keyStr->keyData->key ********************/
        //keyData  publicKeyData=publicKeyData2
        byte[] publicKeyData2Save = saveDecoder.decode(publicKeyStrSave);
        byte[] privateKeyData2Save = saveDecoder.decode(privateKeyStrSave);
        //key
        PublicKey publicKey2 = RSAUtils.getPublicKey(publicKeyData2Save);
        PrivateKey privateKey2 = RSAUtils.getPrivateKey(privateKeyData2Save);

        LogUtil.println("public key: \n" + publicKey.toString());
        LogUtil.println("public keyData: \n" + Arrays.toString(publicKeyData));
        LogUtil.println("public save keyStr: \n" + publicKeyStrSave);
        LogUtil.println("public save keyData: \n" + Arrays.toString(publicKeyData2Save));

        LogUtil.println("private key: \n" + privateKey.toString());
        LogUtil.println("private keyData: \n" + privateKeyData);
        LogUtil.println("private save keyStr: \n" + privateKeyStrSave);
        LogUtil.println("private save keyData: \n" + Arrays.toString(privateKeyData2Save));

        LogUtil.println("还原后的公钥: \n" + publicKey2.toString());
        LogUtil.println("还原后的私钥: \n" + privateKey2.toString());
        /******************* keyStr->keyData->key ********************/
        LogUtil.println("by decoder.decode\n");
        LogUtil.println("public keyDataSave 还原：" + (Arrays.equals(publicKeyData, publicKeyData2Save)));
        LogUtil.println("private keyDataSave 还原：" + (Arrays.equals(privateKeyData, privateKeyData2Save)));
        LogUtil.println("public key 还原：" + (publicKey.equals(publicKey2)));
        LogUtil.println("private key 还原：" + (privateKey.equals(privateKey2)));

        Test(null);
    }

    public static void Test(String content) throws Exception {
        String string = content == null ? defaultContent : content;

        KeyPair keyPair = RSAUtils.generateRSAKeyPair(512);

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        LogUtil.println(publicKey.toString());
        LogUtil.println(privateKey.toString());

        LogUtil.println("公钥加密——私钥解密");
        LogUtil.println("明文：\n" + string);
        LogUtil.println("明文大小：" + string.getBytes().length);
        /***********************  加密    ********************/
        TimeOfRunning t1 = new TimeOfRunning("加密算法").start();
        String encodedData = publicEncrypt(string, (RSAPublicKey) publicKey);
        LogUtil.println("密文：\n" + encodedData);
        LogUtil.println("密文大小：" + encodedData.getBytes().length);
        t1.end();
        /***********************      ********************/

        /***********************  解密    ********************/
        TimeOfRunning t2 = new TimeOfRunning("解密算法").start();
        String decodedData = privateDecrypt(encodedData, (RSAPrivateKey) privateKey);
        LogUtil.println("解密后文字: \n" + decodedData);
        t2.end();
        /***********************      ********************/
    }

}