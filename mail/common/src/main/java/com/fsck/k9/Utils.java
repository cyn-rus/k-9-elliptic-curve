package com.fsck.k9;


import java.math.BigInteger;


public class Utils {
    public static int binaryToNumber(String binary) {
        return Integer.parseInt(binary, 2);
    }

    public static String binaryToHex(String bin) {
        String result = "";
        for (int i = 0; i < bin.length(); i += 4) {
            String b = bin.substring(i, i + 4);
            result += Integer.parseInt(b, 2);
        }

        return result;
    }

    public static String binaryToString(String bin) {
        String result = "";
        for (int i = 0; i < bin.length(); i++) {
            int asciiCode = Integer.parseInt(bin.substring(i, i+8), 2);
            result += (char) asciiCode;
        }

        return result;
    }

    public static String numberToBinary(int num) {
        return String.format("%8s", Integer.toBinaryString(num)).replaceAll(" ", "0");
    }

    public static String numberToBinary(BigInteger num) {
        String result = num.toString(2);
        while (result.length() != 64) {
            result = '0' + result;
        }
        return result;
    }

    public static String hexToBinary(String hex) {
        return Integer.toBinaryString(Integer.parseInt(hex, 16));
    }

    public static String stringToBinary(String string) {
        String result = "";
        char[] chars = string.toCharArray();
        for (char c : chars) {
            result += String.format("%8s", Integer.toBinaryString(c)).replaceAll(" ", "0");
        }
        return result;
    }

    public static int hexToNumber(String hex) {
        return Integer.parseInt(hex, 16);
    }

    public static int byteToNumber(Byte b) {
        return b.intValue();
    }

    public static int[] stringToNumber(String string) {
        String[] bin = new String[string.length()];
        for (int i = 0; i < string.length(); i++) {
            bin[i] = stringToBinary(String.valueOf(string.charAt(i)));
        }
        
        int[] num = new int[bin.length];
        for (int i = 0; i < bin.length; i++) {
            num[i] = binaryToNumber(bin[i]);
        }
        return num;
    }

    public static String XOR(String stringA, String stringB) {
        String result = "";
        for (int i = 0; i < stringA.length(); i++) {
            if (stringA.charAt(i) == stringB.charAt(i)) {
                result += '0';
            } else {
                result += '1';
            }
        }
        return result;
    }

    public static String permutate(String string, int[] permutationMatrix) {
        String result = "";
        for (int i = 0; i < permutationMatrix.length; i++) {
            result += string.charAt(permutationMatrix[i] - 1);
        }
        return result;
    }
}