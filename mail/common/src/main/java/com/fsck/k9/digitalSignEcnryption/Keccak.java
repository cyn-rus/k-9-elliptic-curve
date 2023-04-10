package com.fsck.k9.digitalSignEcnryption;


import java.util.Arrays;

import com.fsck.k9.mailEncryption.Utils;


public class Keccak {
    private final int r = 1088;
    private final int c = 512;
    private final int n = 24;
    private final int l = (n - 12) / 2;
    private final int w = (int) Math.pow((double) l, 2);

    private int[][] a;
    private int[][] b;
    private int[] state = new int[200];
    {
        Arrays.fill(this.state, 0);
    }
    private void theta(int[] a) {
        int[] c = new int[5];
        for (int i = 0; i < 5; i++) {
            c[i] = this.a[i][0] ^ this.a[i][1] ^ this.a[i][2] ^ this.a[i][3] ^ this.a[i][4];
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
                this.a[i][j] ^= d[i];
            }
        }
    }

    private void rhoAndPi() {
        final int[][] rot = new int[][] {
            new int[] {0, 36, 3, 41, 18},
            new int[] {1, 44, 10, 45, 2},
            new int[] {62, 6, 43, 15, 61},
            new int[] {28, 55, 25, 56, 21},
            new int[] {27, 20, 39, 8, 14},
        };

        int k;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                k = ((2 * i) + (3 * j)) % 5;
                this.b[j][k] = this.a[i][j] << rot[i][j];
            }
        }
    }

    private void chi() {
        this.a = new int[5][5];
        int k, l;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                k = (i + 1) % 5;
                l = (i + 2) % 5;
                this.a[i][j] = this.b[i][j] ^ (~(this.b[k][j]) & this.b[l][j]);
            }
        }
    }

    private void iota() {
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
            this.a[0][0] = this.a[0][0] ^ Utils.hexToNumber(rc[i]);
        }
    }

    private void padding(byte[] sign) {

    }

    public int[] keccakF(int[] state) {
//        int[][] a = new int[5][5];
//        this.theta();
//        this.rhoAndPi();
//        this.chi();
//        this.iota();
        return state;
    }

    private void absorbing(int[] blockMessage) {
        int bytesToProcess = this.r / 8;
        int processedBytes = 0;
        int messageLength = blockMessage.length;

        while (processedBytes < messageLength) {
            int blockSize = Math.min(bytesToProcess, messageLength - processedBytes);
            for (int i = 0; i < blockSize; i++) {
                this.state[i] ^= blockMessage[i + processedBytes];
            }
            processedBytes += blockSize;
            if (blockSize == bytesToProcess) {
                this.state = this.keccakF(this.state);
            }
        }
    }

    private String squeezing() {
        int outputLen = 64;
        return "test";
    }


    public void hash(String sign) {
//        this.absorbing(sign);
    }
}
