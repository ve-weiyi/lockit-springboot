package com.ve.blog.util;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: yeb
 * @description: RSAEncryptor 对类中所有String 字段进行加密
 * JsonMappingException: (was java.lang.NullPointerException)
 * @author: Honors
 * @create: 2021-08-02 16:00
 */
public class RSAEncryptor {


    public static Map parsingRSAMap(Map mapType, String keyStr, Integer keyType, Integer model) {
        Map map = new HashMap();
        for (Object obj : mapType.keySet()){
            Object val=mapType.get(obj);
//            LogUtil.println("key为："+obj+"值为："+val);
            if(val instanceof String){
                String encryptStr = RSAUtils.parsingRSAString((String) val, keyStr, keyType, model);
//                LogUtil.println("encryptStr为："+encryptStr);
                map.put(obj,encryptStr);
            }else if(val instanceof Map){
                map.put(obj,parsingRSAMap(mapType,keyStr,keyType,model));
            }else{
                map.put(obj,val);
            }
        }
        return map;
    }



    /**
     * 把对象中String类型属性进行加密
     * @param object 对象
     * @param keyStr 秘钥串
     * @param keyType 秘钥类型
     * @param model 加解密模式
     * @return 加密后的对象
     */
    public static <T> T parsingRSAObject(T object, String keyStr, Integer keyType, Integer model) {
        // 得到类对象
        Class objectClass = object.getClass();
        T reslute = null;
        try {
            //反射构造类
            reslute = (T) objectClass.getDeclaredConstructor().newInstance();
            //复制
            reslute = (T) BeanCopyUtils.copyObject(object, reslute.getClass());
            /* 得到类中的所有属性集合 */
            Field[] fs = objectClass.getDeclaredFields();
            for (int i = 0; i < fs.length; i++) {
                Field f = fs[i];
                f.setAccessible(true);
                // 设置这些属性是可以访问的
                Object val = f.get(reslute);
                String encryptStr = null;
                if (val != null) {
                    //得到类型 f.getType().toString();//得到此属性的类型
                    String type = val.getClass().getName();
                    if (type == "java.lang.String") {
                        encryptStr = RSAUtils.parsingRSAString((String) val, keyStr, keyType, model);
                        f.set(reslute, encryptStr);
                    }
//                    LogUtil.println(val+" is "+type );
                }
                // 得到此属性的值
                 LogUtil.println(f.getName() + ":" + val);
                // 设置键值
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reslute;
    }

    /**
     * 使用公钥加密类的String字段属性
     * @param plaintext    明文
     * @param publicKeyStr 公钥
     * @param <T>
     * @return 加密后的密文
     */
    public static <T> T encodeRSAObject(T plaintext, String publicKeyStr) {
        // 得到类对象
        Class objectClass = (Class) plaintext.getClass();
        T privacy = null;
        try {
            //反射构造类
            privacy = (T) objectClass.getDeclaredConstructor().newInstance();
            //复制
            privacy = (T) BeanCopyUtils.copyObject(plaintext, privacy.getClass());
//            LogUtil.println(privacy.toString());
            /* 得到类中的所有属性集合 */
            Field[] fs = objectClass.getDeclaredFields();
            for (int i = 0; i < fs.length; i++) {
                Field f = fs[i];
                f.setAccessible(true);
                // 设置这些属性是可以访问的
                Object val = f.get(privacy);
                String encryptStr = null;
                if (val != null) {
                    //得到类型 f.getType().toString();//得到此属性的类型
                    String type = val.getClass().getName();
                    if (type == "java.lang.String") {
                        encryptStr = RSAUtils.publicEncrypt((String) val, publicKeyStr);
                        f.set(privacy, encryptStr);
                    }
//                    LogUtil.println(val+" is "+type );
                }
                // 得到此属性的值
                // LogUtil.println(f.getName() + ":" + val);
                // 设置键值
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return privacy;
    }

    /**
     * 使用公钥加密类的String字段属性
     * @param privacy       明文
     * @param privateKeyStr 公钥
     * @param <T>
     * @return 加密后的密文
     */
    public static <T> T dncodeRSAObject(T privacy, String privateKeyStr) {

        // 得到类对象
        Class objectClass = (Class) privacy.getClass();
        T plaintext = null;
        try {
            //反射构造类
            plaintext = (T) objectClass.getDeclaredConstructor().newInstance();
            //复制
            plaintext = (T) BeanCopyUtils.copyObject(privacy, plaintext.getClass());
//            LogUtil.println(plaintext.toString());
            /* 得到类中的所有属性集合 */
            Field[] fs = objectClass.getDeclaredFields();
            for (int i = 0; i < fs.length; i++) {
                Field f = fs[i];
                f.setAccessible(true);
                // 设置这些属性是可以访问的
                Object val = f.get(plaintext);
                String encryptStr = null;
                if (val != null) {
                    //得到类型 f.getType().toString();//得到此属性的类型
                    String type = val.getClass().getName();
                    if (type == "java.lang.String") {
                        encryptStr = RSAUtils.privateDecrypt((String) val, privateKeyStr);
                        f.set(plaintext, encryptStr);
                    }
//                    LogUtil.println(val+" is "+type );
                }
                // 得到此属性的值
                // LogUtil.println(f.getName() + ":" + val);
                // 设置键值
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plaintext;
    }


    public static void main(String[] args) {
        Map privacyInfo= JSON.parseObject("{\n" +
                "        \"id\": 13,\n" +
                "        \"account\": \"OF5P5CaaBHHQ0PSu939r-x_Wexk1TbxllysJO9H2XDmpn96iJXuywU51EN1wKzf8dNeAZUw9VEwq76f0dnYslQ\",\n" +
                "        \"password\": \"VUBdrOtoGiiixZwBm4cmuRh4f6WQtAlmpgoyM94ywhj0B7dh9yc2ntoyuCgtCMU9ToCZCSSEPFMJS4t9eGX4Fg\",\n" +
                "        \"number\": \"M_coyHTuWPQ4JxX4-5hhs1BkO-8aYCicrYaPKN5db4yagWSKWoV1j9zWCu7o-CH0gQnfrIUwe0h2O3_Pghz_0g\",\n" +
                "        \"phone\": \"B6jrdz_bPyk51KrDyB6qd3GTQvJYaWmmzZAOlEIs9g7FE9sfJqaln-j43rAgoxtiqWn6BlbFZopa0Fw-NokNgQ\",\n" +
                "        \"address\": \"dhD4AAOZele4XX8GSGfWaw1GqvReNg5-YlGebok0zSOzVoMxWPjj3Nk4BabIYQ3Vep-ef8gPyxnQ8DjHM8wyGw\",\n" +
                "        \"url\": \"FvG9_ozqHZg2fpq18BPQYFpQhikRfkbd4oDCrVGDMEkJ4kVwdIyROsl-KeKcXHeEujMh7rFNGInzT3Gjz8jhMA\",\n" +
                "        \"signDate\": \"AV6FiI_CM5hnH6u6yeuuAA99XOhk2zhHe3aZuaNhwYlYpT1NFyZKqFA-RXKibim-vdJCeAfouq6qe9BL9i9OpQ\",\n" +
                "        \"validDate\": \"el5qC9m7uEjA5ywQ7OD1cqH1mv-URZyakXdkAaljkVfiuFw3m41DFSzBZB-_GnDswlaHPg05SQmmsxRF6HSfQw\",\n" +
                "        \"remark\": \"dyB5C2JzHMv6jknfAljXuKpb2RyzGaae6iMhpuaLXlProSIonS8UbMNrFpGgba0JCj6dYMdIK2OOMuRo6Rpkkx2dUsFzOu5wh5NFna37V_-t7Jit7CRBij9kprurLzIz-6XJyvOXf5a1ibCLkYElVMEOOhbN_D34WsouygwQfOM\",\n" +
                "        \"enable_encrypt\": 1\n" +
                "      }");
        String str=("{" +
                "id=13, " +
                "account=OF5P5CaaBHHQ0PSu939r-x_Wexk1TbxllysJO9H2XDmpn96iJXuywU51EN1wKzf8dNeAZUw9VEwq76f0dnYslQ, " +
                "password=VUBdrOtoGiiixZwBm4cmuRh4f6WQtAlmpgoyM94ywhj0B7dh9yc2ntoyuCgtCMU9ToCZCSSEPFMJS4t9eGX4Fg, " +
                "number=M_coyHTuWPQ4JxX4-5hhs1BkO-8aYCicrYaPKN5db4yagWSKWoV1j9zWCu7o-CH0gQnfrIUwe0h2O3_Pghz_0g, " +
                "phone=B6jrdz_bPyk51KrDyB6qd3GTQvJYaWmmzZAOlEIs9g7FE9sfJqaln-j43rAgoxtiqWn6BlbFZopa0Fw-NokNgQ, " +
                "address=dhD4AAOZele4XX8GSGfWaw1GqvReNg5-YlGebok0zSOzVoMxWPjj3Nk4BabIYQ3Vep-ef8gPyxnQ8DjHM8wyGw, " +
                "url=FvG9_ozqHZg2fpq18BPQYFpQhikRfkbd4oDCrVGDMEkJ4kVwdIyROsl-KeKcXHeEujMh7rFNGInzT3Gjz8jhMA, " +
                "signDate=AV6FiI_CM5hnH6u6yeuuAA99XOhk2zhHe3aZuaNhwYlYpT1NFyZKqFA-RXKibim-vdJCeAfouq6qe9BL9i9OpQ, " +
                "validDate=el5qC9m7uEjA5ywQ7OD1cqH1mv-URZyakXdkAaljkVfiuFw3m41DFSzBZB-_GnDswlaHPg05SQmmsxRF6HSfQw, " +
                "remark=dyB5C2JzHMv6jknfAljXuKpb2RyzGaae6iMhpuaLXlProSIonS8UbMNrFpGgba0JCj6dYMdIK2OOMuRo6Rpkkx2dUsFzOu5wh5NFna37V_-t7Jit7CRBij9kprurLzIz-6XJyvOXf5a1ibCLkYElVMEOOhbN_D34WsouygwQfOM, " +
                "enable_encrypt=1}").replace("=",": ");
        LogUtil.println(privacyInfo.toString());
        LogUtil.println(str);
        Map privacyInfo1= JSON.parseObject(str);

        String keyString = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAlALkhD0A" +
                "wJyRhVXFEYqlM5w7vs-SG9JwwLeKboREYVPAYqMCs72U3kbYqlC8YcLyH0IGM_Q07bbQjc" +
                "NIvHNOgwIDAQABAkA9TZVoT_vnyvFa0FN3GH2kCqmUNlTFwTuCFoL3k0DfLjaXxvzahPiRm51d34" +
                "FVEqJQidbxjMwDwessnvwmO1tBAiEA3IU-7zcYnvjRyTKYm65yWboaQ1JQaX16Yxk5Re2nLK8CI" +
                "QCr0ycRvMJbUFRmhbT3Yux6yf33lq7oodNpeF3dozE4bQIgVCag0oZy5c9LtvyQ0e_rASSMex5B" +
                "Q1A30PNCMNY00k8CIQCNrJPZPy9uBUUiJ3r4Q9vCvswmGltruwrLOxECT62k3QIgVVSDlWcO1565" +
                "bWgOZCmaL0wmJ1BAgGpdpY4JtDuaJyY";
        Integer keyType = 0;
        Integer model = 0;

        LogUtil.println(privacyInfo.get("id").toString());

        Map map=parsingRSAMap(privacyInfo,keyString,keyType,model);
        LogUtil.println(map.toString());
//        OperationLog operationLog = OperationLog.builder()
//                .nickname("rftguhinjac")
//                .ipAddress("edtrfytguhijok")
//                .optDesc("yguhjnsegdsfd")
//                .build();
//
//
//        KeyPair keyPair = RSAUtils.generateRSAKeyPair();
//
//        /******************* key->keyData->keyStr ********************/
//        //key
//        PublicKey publicKey = keyPair.getPublic();
//        PrivateKey privateKey = keyPair.getPrivate();
//        //keyData
//        byte[] publicKeyData = publicKey.getEncoded();
//        byte[] privateKeyData = privateKey.getEncoded();
//        //keyStr
//        String publicKeyStrSave = Base64Util.encodeBase64URLSafeString(publicKeyData);
//        String privateKeyStrSave = Base64Util.encodeBase64URLSafeString(privateKeyData);
//        /******************* key->keyData->keyStr ********************/
//
//        OperationLog n1 = parsingRSAObject(operationLog,publicKeyStrSave,
//                1,1);
//        OperationLog n2 = parsingRSAObject(n1,privateKeyStrSave,
//                0,0);
//        LogUtil.println(n1.toString());
//        LogUtil.println(n2.toString());
//
//        OperationLog nb1 = encodeRSAObject(operationLog, publicKeyStrSave);
//        LogUtil.println(nb1.toString());
//        OperationLog nb2 = dncodeRSAObject(nb1, privateKeyStrSave);
//        LogUtil.println(nb2.toString());

    }
}