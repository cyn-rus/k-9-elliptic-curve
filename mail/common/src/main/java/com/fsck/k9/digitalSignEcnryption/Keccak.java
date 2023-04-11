package com.fsck.k9.digitalSignEcnryption;


import java.util.Arrays;

import com.fsck.k9.mailEncryption.Utils;


public class Keccak {
    private final int r = 1088;
    private final int c = 512;
    private final int n = 24;
    private final int l = (n - 12) / 2;
    private final int w = (int) Math.pow((double) l, 2);

    private int[] state = new int[200];
    {
        Arrays.fill(this.state, 0);
    }

    private int[] theta(int[] a) {
        int[] c = new int[5];
        for (int i = 0; i < 5; i++) {
            c[i] = a[i][0] ^ a[i][1] ^ a[i][2] ^ a[i][3] ^ a[i][4];
        }
        int[] d = new int[5];
        int j, k;
        for (int i = 0; i < 5; i++) {
            j = ((i - 1) + 5) % 5;
            k = (i + 1) % 5;
            d[i] = c[j] ^ (c[k] << 1);
        }

        for (int i = 0; i < 5; i++) {
            for (j = 0; j < 5; j++) {
                a[i][j] ^= d[i];
            }
        }
        
        return a;
    }

    private int[] rhoAndPi(int[] a) {
        final int[][] rot = new int[][] {
            new int[] {0, 36, 3, 41, 18},
            new int[] {1, 44, 10, 45, 2},
            new int[] {62, 6, 43, 15, 61},
            new int[] {28, 55, 25, 56, 21},
            new int[] {27, 20, 39, 8, 14},
        };

        int[][] b = new int[5][5];
        int k;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                k = ((2 * i) + (3 * j)) % 5;
                b[j][k] = a[i][j] << rot[i][j];
            }
        }
        
        return b;
    }

    private int[][] chi(int[] b) {
        int[][] a = new int[5][5];
        int k, l;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                k = (i + 1) % 5;
                l = (i + 2) % 5;
                a[i][j] = b[i][j] ^ (~(b[k][j]) & b[l][j]);
            }
        }

        return a;
    }

    private int[][] iota(int[][] a) {
        final String[] rc = {
            "0x0000000000000001", "0x0000000000008082",
            "0x800000000000808A", "0x8000000080008000",
            "0x000000000000808B", "0x0000000080000001",
            "0x8000000080008081", "0x8000000000008009",
            "0x000000000000008A", "0x0000000000000088",
            "0x0000000080008009", "0x000000008000000A",
            "0x000000008000808B", "0x800000000000008B",
            "0x8000000000008089", "0x8000000000008003",
            "0x8000000000008002", "0x8000000000000080",
            "0x000000000000800A", "0x800000008000000A",
            "0x8000000080008081", "0x8000000000008080",
            "0x0000000080000001", "0x8000000080008008",
        };

        for (int i = 0; i < this.n; i++) {
            a[0][0] = a[0][0] ^ Utils.hexToNumber(rc[i]);
        }
    }

    private int[] padding(int[] sign) {
        if (sign.length() % this.r == 0) return sign;

        String pad = '';
        while ((sign.length() + pad.length + 1) % this.r != 0) {
            if (pad.length == 0) pad += '1';
            pad += '0';
        }
        pad += '1';

        int[] result = new int[pad.length];
        for (int i = 0; i < pad.length; i++) {
            result[i] = Integer.parseInt(pad[i]);
        }

        return result;
    }

    public int[] keccakF(int[] state) {
        StringBuilder[] temp = new StringBuilder();
        int[] convertedState = new int[25];
        for (int i = 0; i < state.length(); i++) {
            if (i % 8 == 0) {
                convertedState = Utils.binaryToNumber(temp);
                temp = new StringBuilder();
            }
        }
        
        int[][] a = new int[5][5];
        for (int i = 0; i < convertedState.length(); i++) {
            a[(int) Math.floor((double) i / 5)][i % 5] = convertedState[i];
        }
        
        a = this.theta(a);
        int[] b = this.rhoAndPi(a);
        a = this.chi(b);
        a = this.iota(a);
        
        return a;
    }

    private void absorbing(int[] blockMessage) {
        int bytesToProcess = this.r / 8;
        int processedBytes = 0;
        int messageLength = blockMessage.length;

        while (processedBytes < messageLength) {
            int bitsSize = Math.min(bytesToProcess, messageLength - processedBytes);
            for (int i = 0; i < bitsSize; i++) {
                this.state[i] ^= blockMessage[i + processedBytes];
            }
            processedBytes += blockSize;
            if (bitsSize == bytesToProcess) {
                this.state = this.keccakF(this.state);
            }
        }
    }

    private String squeezing() {
        int outputLen = 64;
        return "test";
    }


    public void hash(String sign) {
        int[] signBinary = Utils.stringToNumber(sign);
        int[] paddedSign = this.padding(signBinary);
        this.absorbing(paddedSign);
    }
}
