package io.luowei.chatgpt.utils;

import java.security.MessageDigest;

public class SignatureUtil {

    //验证签名
    public static boolean check(String token, String signature, String timestamp, String nonce) {
        String[] arr = new String[] {token,timestamp,nonce};
        // 将三个字段按照字典进行排序
        sort(arr);

        StringBuffer buffer = new StringBuffer();
        for (String s : arr) {
            buffer.append(s);
        }

        MessageDigest md;
        String tmpStr = null;

        try {
            md = MessageDigest.getInstance("SHA-1");
            // 将拼接后的字符串进行加密
            byte[] byteArray = md.digest(buffer.toString().getBytes());
            tmpStr = byteToStr(byteArray);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 将sha1加密后的字符串可与signature对比，标识该请求来源于微信
        return tmpStr != null && tmpStr.equals(signature.toUpperCase());
    }

    private static String byteToStr(byte[] byteArray) {
        StringBuilder strDigest = new StringBuilder();
        for (byte b : byteArray) {
            strDigest.append(byteToHexStr(b));
        }
        return strDigest.toString();
    }

    /**
     * 将字节转换为十六进制字符串
     */
    private static String byteToHexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];
        return new String(tempArr);
    }

    // 进行字典排序
    private static void sort(String[] str) {
        // 冒泡
        for (int i = 0; i < str.length - 1; i++) {
            for (int j = i + 1; j < str.length;j++) {
                if (str[j].compareTo(str[i]) < 0) {
                    String temp = str[i];
                    str[i] = str[j];
                    str[j] = temp;
                }
            }
        }
    }
}
