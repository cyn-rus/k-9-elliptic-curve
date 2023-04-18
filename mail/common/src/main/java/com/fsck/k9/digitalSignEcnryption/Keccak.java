package com.fsck.k9.digitalSignEcnryption;


import java.math.BigInteger;
import java.util.Arrays;

import com.fsck.k9.Utils;
import com.fsck.k9.logging.Timber;


public class Keccak {
    private final int r = 1088;
    private final int n = 24;
    private final int l = (n - 12) / 2;

    private final int bytesToProcess = this.r / 8;

    private BigInteger[] state = new BigInteger[200];
    {
        for (int i = 0; i < 200; i++) {
            state[i] = BigInteger.valueOf(0);
        }
    }

    private final String[] rc = {
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

    private BigInteger[][] theta(BigInteger[][] a) {
        BigInteger[] c = new BigInteger[5];
        for (int i = 0; i < 5; i++) {
            c[i] = a[i][0].xor(a[i][1]).xor(a[i][2]).xor(a[i][3]).xor(a[i][4]);
        }
        BigInteger[] d = new BigInteger[5];
        int j, k;
        for (int i = 0; i < 5; i++) {
            j = ((i - 1) + 5) % 5;
            k = (i + 1) % 5;
            d[i] = c[j].xor(c[k].shiftLeft(1));
        }

        for (int i = 0; i < 5; i++) {
            for (j = 0; j < 5; j++) {
               a[i][j] = a[i][j].xor(d[i]);
            }
        }
        
        return a;
    }

    private BigInteger[][] rhoAndPi(BigInteger[][] a) {
        final int[][] rot = new int[][] {
            new int[] {0, 36, 3, 41, 18},
            new int[] {1, 44, 10, 45, 2},
            new int[] {62, 6, 43, 15, 61},
            new int[] {28, 55, 25, 56, 21},
            new int[] {27, 20, 39, 8, 14},
        };

        BigInteger[][] b = new BigInteger[5][5];
        int k;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                k = ((2 * i) + (3 * j)) % 5;
                b[j][k] = a[i][j].shiftLeft(rot[i][j]);
            }
        }
        
        return b;
    }

    private BigInteger[][] chi(BigInteger[][] b) {
        BigInteger[][] a = new BigInteger[5][5];
        int k, l;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                k = (i + 1) % 5;
                l = (i + 2) % 5;
                a[i][j] = b[i][j].xor(b[l][j].andNot(b[k][j]));
            }
        }

        return a;
    }

    private BigInteger[][] iota(BigInteger[][] a, String rc) {
        a[0][0] = a[0][0].xor(new BigInteger(rc.substring(2), 16));
        return a;
    }

    private String padding(String sign) {
        if (sign.length() % this.r == 0) return sign;

        String temp = sign + "00000110";
        while (temp.length() % this.r != 0) {
            if ((temp.length() + 1) % this.r == 0) temp += '1';
            else temp += '0';
        }
        return temp;
    }

    public BigInteger[] keccakF(BigInteger[] state) {
        String temp = "";
        BigInteger[] convertedState = new BigInteger[25];
        for (int i = 0; i < state.length; i++) {
            temp = Utils.numberToBinary(state[i]) + temp;
            if ((i + 1) % 8 == 0) {
                convertedState[(int) Math.floor((i + 1) / 8) - 1] = new BigInteger(temp, 2);
                temp = "";
            }
        }

        BigInteger[][] a = new BigInteger[5][5];
        for (int i = 0; i < convertedState.length; i++) {
            a[(int) Math.floor((double) i / 5)][i % 5] = convertedState[i];
        }

        for (int i = 0; i < 24; i++) {
            a = this.theta(a);
            BigInteger[][] b = this.rhoAndPi(a);
            a = this.chi(b);
            a = this.iota(a, this.rc[i]);
        }

        BigInteger[] result = new BigInteger[200];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                String hex = a[i][j].toString(16);
                while (hex.length() < 16) {
                    hex = '0' + hex;
                }

                for (int k = 0; k < 8; k++) {
                    final int pos = (7 - k) * 2;
                    String substring = hex.substring(pos, pos + 2);
                    result[(i * 40) + (j * 8) + k] = BigInteger.valueOf(Utils.hexToNumber(substring));
                }
           }
        }

        return result;
    }

    private void absorbing(int[] blockMessage) {
        int processedBytes = 0;
        int messageLength = blockMessage.length;

        while (processedBytes < messageLength) {
            int bytesSize = Math.min(this.bytesToProcess, messageLength - processedBytes);
            for (int i = 0; i < bytesSize; i++) {
                this.state[i] = this.state[i].xor(BigInteger.valueOf(blockMessage[i + processedBytes]));
            }
            processedBytes += bytesSize;
            if (bytesSize == this.bytesToProcess) {
                this.state = this.keccakF(this.state);
            }
        }
    }

    public String squeezing() {
        int outputLength = 64;
        String output = "";
        int i = 0;
        while (output.length() < outputLength) {
            String temp = Utils.numberToBinary(this.state[i]);
            while (temp.length() < 8) {
                temp = '0' + temp;
            }
            output += Utils.binaryToHex(temp);
            this.state = this.keccakF(this.state);
            i++;
        }

        return output;
    }

    public String hash(String sign) {
        String signBinary = Utils.stringToBinary(sign);
        String paddedSign = this.padding(signBinary);

        int paddedSignLength = paddedSign.length();
        int[] splitSign = new int[paddedSignLength / 8];
        int j = 0;
        for (int i = 0; i < paddedSignLength; i += 8) {
            String temp = paddedSign.substring(i, i + 8);
            splitSign[j] = Utils.binaryToNumber(temp);
            j++;
        }
        this.absorbing(splitSign);
        String result = this.squeezing();

        return result;
    }
}
