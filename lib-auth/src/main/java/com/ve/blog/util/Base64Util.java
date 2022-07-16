package com.ve.blog.util;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

/**
 * @Description hello word!
 * encodeBase64会对字符串3位一组自动补全，因而最后可能会出现 == 或者 =
 * 而encodeBase64URLSafe则是按照字符串实际位数进行加密，最后若为1位，则不补全，不会出现 == 或者 =
 * <p>
 * encodeBase64URLSafeString是说编码后在URL中使用是安全的，
 * 所谓安全是这种方式编码的时候会替换掉64个字符中的 ”+“替换成"-"，"/"替换成"_" ,"="替换成"" ，
 * 因为这两个字符在URL中有特殊含义，使用会出问题，也就是”不安全“，还有Base64本身和信息上的安全没有半点关系，
 * 它做的事情是编码，并不是加密，因为任何人都可以解码Base64的结果
 * 安全是指特殊符号不会被转义，不是信息安全的意思
 *      * Base64.encodeBase64URLSafeString(bytes) 与
 *      * return new String(Base64.encodeBase64(bytes))
 *      *         .replace("+", "-")
 *      *         .replace("/", "_")
 *      *         .replace("=", "");
 *      *         相等
 *
 * @Author weiyi
 * @Date 2022/1/4
 */
public class Base64Util {

    private static char[] base64EncodeChars = new char[]
            { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                    'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
                    '6', '7', '8', '9', '+', '/' };
    private static byte[] base64DecodeChars = new byte[]
            { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53,
                    54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
                    12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29,
                    30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1,
                    -1, -1, -1 };

    /**
     * 加密
     *
     * @param data
     * @return
     */
    public static String encode(byte[] data)
    {
        StringBuffer sb = new StringBuffer();
        int len = data.length;
        int i = 0;
        int b1, b2, b3;
        while (i < len)
        {
            b1 = data[i++] & 0xff;
            if (i == len)
            {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[(b1 & 0x3) << 4]);
                sb.append("==");
                break;
            }
            b2 = data[i++] & 0xff;
            if (i == len)
            {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
                sb.append(base64EncodeChars[(b2 & 0x0f) << 2]);
                sb.append("=");
                break;
            }
            b3 = data[i++] & 0xff;
            sb.append(base64EncodeChars[b1 >>> 2]);
            sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
            sb.append(base64EncodeChars[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]);
            sb.append(base64EncodeChars[b3 & 0x3f]);
        }
        return sb.toString();
    }

    /**
     * 解密
     *
     * @param str
     * @return
     */
    public static byte[] decode(String str)
    {
        try
        {
            byte[] data = null;
            data = str.getBytes("US-ASCII");
            return decodePrivate(data);
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return new byte[]{};
    }

    private static byte[] decodePrivate(byte[] data) throws UnsupportedEncodingException
    {
        StringBuffer sb = new StringBuffer();
        int len = data.length;
        int i = 0;
        int b1, b2, b3, b4;
        while (i < len)
        {
            do
            {
                b1 = base64DecodeChars[data[i++]];
            } while (i < len && b1 == -1);
            if (b1 == -1) {
                break;
            }

            do
            {
                b2 = base64DecodeChars[data[i++]];
            } while (i < len && b2 == -1);
            if (b2 == -1) {
                break;
            }
            sb.append((char) ((b1 << 2) | ((b2 & 0x30) >>> 4)));

            do
            {
                b3 = data[i++];
                if (b3 == 61) {
                    return sb.toString().getBytes("iso8859-1");
                }
                b3 = base64DecodeChars[b3];
            } while (i < len && b3 == -1);
            if (b3 == -1) {
                break;
            }
            sb.append((char) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));

            do
            {
                b4 = data[i++];
                if (b4 == 61) {
                    return sb.toString().getBytes("iso8859-1");
                }
                b4 = base64DecodeChars[b4];
            } while (i < len && b4 == -1);
            if (b4 == -1) {
                break;
            }
            sb.append((char) (((b3 & 0x03) << 6) | b4));
        }
        return sb.toString().getBytes("iso8859-1");
    }

    //base64 编码
    public static byte[] encodeBase64URLSafeString(String str) {
        return encodeBase64URLSafeString(str.getBytes()).getBytes();
    }

    //base64 解码
    public static byte[] decodeBase64URLSafeString(String str) {
        str = str.replace("-", "+")
                .replace("_", "/");
        //后三位补全"="
        int mod4 = str.getBytes().length % 4;
        if (mod4 > 0) {
            str = str + "====".substring(mod4);
        }
        return decode(str);
    }

    //base64 编码
    public static String encodeBase64URLSafeString(byte[] bytes) {
        //return new String(Base64.encodeBase64(bytes));
        return new String(Base64.encodeBase64URLSafeString(bytes));
    }

    //base64 解码
    public static String decodeBase64URLSafeString(byte[] bytes) {
        String data = new String(bytes)
                .replace("-", "+")
                .replace("_", "/");
        //后三位补全"="
        int mod4 = bytes.length % 4;
        if (mod4 > 0) {
            data = data + "====".substring(mod4);
        }
        bytes = data.getBytes();
        return new String(Base64.decodeBase64(bytes));
    }

    private static final String defaultContent = "床前明月光。";
    public static void Test(String content) {
        String string = content==null?defaultContent:content;

        System.out.println("源字符串：" + string);
        //编码
        String encode = encode(string.getBytes());
        String encode2 = encodeBase64URLSafeString(string.getBytes());
        System.out.println("编码后的字符串为：" + encode);
        System.out.println("编码后save的字符串为：" + encode2);

        //解码
        String decode = new String(decode(encode2));
        String decode2 = decodeBase64URLSafeString(encode.getBytes());

        System.out.println("解码后的字符串为：" + decode);
        System.out.println("解码后save的字符串为：" + decode2);
    }

}