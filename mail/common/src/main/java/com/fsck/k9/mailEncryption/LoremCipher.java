package com.fsck.k9.mailEncryption;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class LoremCipher {
    private static int[] ip = {
        58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6,
        64, 56, 48, 40, 32, 24, 16, 8,
        57, 49, 41, 33, 25, 17, 9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7,
    };

    private static int[] permutateBox = {
        16, 7, 20, 21, 29, 12, 28, 17,
        1, 15, 23, 26, 5, 8, 31, 10,
        2, 8, 24, 14, 32, 27, 3, 9,
        19, 13, 30, 6, 22, 11, 4, 25,
        40, 8, 48, 16, 56, 24, 64, 32,
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 45, 13, 53, 21, 61, 29
    };

    private static int[] ipInverse = {
        40, 8, 48, 16, 56, 24, 64, 32,
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28,
        35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41, 9, 49, 17, 57, 25,
    };

    private FeistelModified f = new FeistelModified();

    private int paddingLength = 0;

    private String[] initialPermutate(String text) {
        int halfBlockLength = text.length() / 2;
        String leftBlock = text.substring(0, halfBlockLength);
        String rightBlock = text.substring(halfBlockLength);

        String permutationResult = Utils.permutate(leftBlock, ip) + Utils.permutate(rightBlock, ipInverse);

        halfBlockLength = permutationResult.length() / 2;
        String leftPermutation = permutationResult.substring(0, halfBlockLength);
        String rightPermutation = permutationResult.substring(halfBlockLength);

        return new String[] {leftPermutation, rightPermutation};
    }

    private String[] feistelFunction(String L, String R, List<String> key) {
        for (int i = 0; i < 16; i++) {
            String feistel = this.f.encrypt(R, key.get(i));
            String permutatedFeistel = Utils.permutate(feistel, permutateBox);

            String temp = L;
            L = R;
            R = Utils.XOR(temp, permutatedFeistel);
        };

        return new String[] {L, R};
    }

    private String lastPermutate(String L, String R) {
        return Utils.permutate(L, ipInverse) + Utils.permutate(R, ip);
    }

    public String encrypt(String plaintext, List<String> key) {
        String finalResult = "";

        List<String> splitPlaintext = new ArrayList<>();
        for (int i = 0; i < plaintext.length(); i += 16) {
            splitPlaintext.add(plaintext.substring(i, Math.min(i + 16, plaintext.length())));
        }

        final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random r = new Random();
        for (String pt : splitPlaintext) {
            if (pt.length() < 16) {
                this.paddingLength = 16 - pt.length();
                String padding = "";
                for (int i = 0; i < this.paddingLength; i++) {
                    padding += alphabet.charAt(r.nextInt(alphabet.length()));
                }
                pt += padding;
            }

            pt = Utils.stringToBinary(pt);
            String[] block = this.initialPermutate(pt);
            block = this.feistelFunction(block[0], block[1], key);
            finalResult += this.lastPermutate(block[0], block[1]);
        }

        return Utils.binaryToHex(finalResult);
    }

    public String decrypt(String ciphertext, List<String> key) {
        String finalResult = "";

        List<String> splitCiphertext = new ArrayList<>();
        for (int i = 0; i < ciphertext.length(); i += 32) {
            splitCiphertext.add(ciphertext.substring(i, Math.min(i + 32, ciphertext.length())));
        }

        for (String ct : splitCiphertext) {
            ct = Utils.hexToBinary(ct);
            String[] block = this.initialPermutate(ct);
            block = this.feistelFunction(block[1], block[0], key);
            finalResult += this.lastPermutate(block[1], block[0]);
        }

        finalResult = Utils.binaryToString(finalResult);
        finalResult = finalResult.substring(0, finalResult.length() - this.paddingLength);
        return finalResult;
    }
}
