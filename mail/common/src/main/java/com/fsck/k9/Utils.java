package com.fsck.k9;


import java.math.BigInteger;


public class Utils {
    public static int binaryToNumber(String binary) {
        return Integer.parseInt(binary, 2);
    }

    public static String binaryToHex(String bin) {
        String result = "";
        for (int i = 0; i < bin.length(); i += 4) {
            int b = binaryToNumber(bin.substring(i, i + 4));
            result += Integer.toHexString(b);
        }

        return result;
    }

    public static String binaryToString(String bin) {
        String result = "";
        for (int i = 0; i < bin.length(); i += 8) {
            int asciiCode = Integer.parseInt(bin.substring(i, i+8), 2);
            result += (char) asciiCode;
        }

        return result;
    }

    public static String numberToBinary(int num) {
        return String.format("%8s", Integer.toBinaryString(num)).replaceAll(" ", "0");
    }

    public static String numberToBinary(BigInteger num) {
        return num.toString(2);
    }

    public static String hexToBinary(String hex) {
        String result = "";
        for (int i = 0; i < hex.length(); i++) {
            String bin = Integer.toBinaryString(Integer.parseInt(String.valueOf(hex.charAt(i)), 16));
            if (bin.length() != 4) {
                bin = String.format("%4s", bin).replaceAll(" ", "0");
            }
            result += bin;
        }
        return result;
    }

    public static String stringToBinary(String string) {
        String result = "";
        char[] chars = string.toCharArray();
        for (char c : chars) {
            String bin = Integer.toBinaryString(c);
            if (bin.length() != 8) {
                bin = String.format("%8s", bin).replaceAll(" ", "0");
            }
            result += bin;
        }
        return result;
    }

    public static int hexToNumber(String hex) {
        return Integer.parseInt(hex, 16);
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