package com.fsck.k9.mailEncryption;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;


public class KeyExpansion {
  public byte[] externalKey;

  public byte[][] internalKeys = new byte[16][64];

  private static int totalTurn = 16;

  private static int[] permutedMatrix1 = {
    118, 45, 25, 72, 102, 22, 18, 46, 76, 33, 98, 88, 34, 96, 2,
    35, 106, 24, 50, 56, 63, 121, 51, 103, 53, 57, 58, 37, 64, 90,
    128, 89, 61, 105, 26, 95, 28, 62, 107, 123, 32, 92, 100, 113, 111,
    68, 114, 42, 6, 23, 49, 9, 48, 11, 80, 78, 16, 116, 66, 85,
    39, 93, 77, 59, 120, 86, 5, 30, 17, 19, 119, 36, 47, 15, 99,
    12, 10, 38, 29, 69, 14, 108, 7, 54, 73, 81, 71, 109, 126, 21,
    8, 79, 112, 3, 117, 104, 1, 20, 55, 84, 97, 31, 125, 127, 52,
    82, 41, 4, 91, 13, 87, 94, 75, 40, 65, 101, 110, 124, 43, 67,
    70, 44, 27, 115, 60, 122, 74, 83,
  };

  private static int[] permutedMatrix2 = {
    77, 74, 102, 45, 16, 118, 29, 99, 32, 53, 17, 65, 57, 75, 64,
    6, 103, 50, 49, 94, 8, 2, 13, 40, 24, 106, 10, 76, 31, 63, 
    35, 1, 96, 114, 62, 78, 14, 61, 104, 124, 95, 30, 100, 46, 56,
    39, 97, 70, 82, 93, 47, 18, 15, 66, 122, 126, 123, 125, 34, 27,
    23, 25, 105, 79, 73, 101, 88, 67, 60, 52, 59, 127, 109, 7, 110,
    121, 4, 87, 72, 90, 54, 12, 108, 115, 98, 43, 36, 113, 85, 20,
    68, 44, 112, 38, 21, 69, 26, 33, 117, 19, 91, 111, 84, 41, 120,
    128, 116, 28, 11, 51, 37, 92, 5, 83, 80, 81, 86, 119, 55, 42,
    48, 107, 3, 22, 71, 89, 9, 58,
  };
  
  public KeyExpansion(String externalKeyString) {
    System.out.println("externalKeyString: " + externalKeyString);
    this.externalKey = externalKeyString.getBytes();
    for (byte element: this.externalKey) {
      System.out.println("externalKey: " + element);
    }
    
    this.expandExternalKey();
  }

  public void expandExternalKey() {
    // First Permutation
    byte[] firstPermutation = this.permutate(this.externalKey, this.permutedMatrix1);
    for (byte element: firstPermutation) {
      System.out.println("firstPermutation: " + element);
    }

    byte[] A = Arrays.copyOfRange(firstPermutation, 0, 32);
    byte[] B = Arrays.copyOfRange(firstPermutation, 32, 64);
    byte[] C = Arrays.copyOfRange(firstPermutation, 64, 96);
    byte[] D = Arrays.copyOfRange(firstPermutation, 96, 128);

    for (int i = 0; i < this.totalTurn; i++) {
      // XOR Operations 
      A = this.XOR(A, B);
      B = this.XOR(B, C);
      C = this.XOR(C, D);
      D = this.XOR(D, A);

      // Left Shift Operations
      int multiplier = i % 2;
      A = this.leftShift(A, 1 * multiplier);
      for (byte element: A) {
        System.out.println("A: " + element);
      }
      B = this.leftShift(B, 2 * multiplier);
      for (byte element: B) {
        System.out.println("B: " + element);
      }
      C = this.leftShift(C, 3 * multiplier);
      for (byte element: C) {
        System.out.println("C: " + element);
      }
      D = this.leftShift(D, 4 * multiplier);
      for (byte element: D) {
        System.out.println("D: " + element);
      }

      // Second Permutation
      byte[] join = new byte[128]; // A + B + C + D

      for (int j = 0; j < A.length; j++) {
        join[j] = A[j];
      }
      for (int j = 0; j < B.length; j++) {
        join[j + 32] = B[j];
      }
      for (int j = 0; j < C.length; j++) {
        join[j + 64] = C[j];
      }
      for (int j = 0; j < D.length; j++) {
        join[j + 96] = D[j];
      }
      
      byte[] internalKey = this.permutate(join, this.permutedMatrix2);

      // XOR firstHalf and secondHalf
      byte[] firstHalf = Arrays.copyOfRange(internalKey, 0, 64);
      byte[] secondHalf = Arrays.copyOfRange(internalKey, 64, 128);
      internalKey = this.XOR(firstHalf, secondHalf);

      // Add new internal key to array
      internalKeys[i] = internalKey;

      // Left Shift Batch Operation
      byte[] temp = A;
      A = B;
      B = C;
      C = D;
      D = temp;
    }
  }

  public byte[] permutate(byte[] string, int[] permutationMatrix) {
    byte[] result = new byte[permutationMatrix.length];

    for (int i = 0; i < permutationMatrix.length; i++) {
      // there is -1 because index in permutation matrix start from 1, not 0.
      result[i] = string[permutationMatrix[i] - 1];
    }

    return result;
  }

  public byte[] XOR(byte[] stringA, byte[] stringB) {
    byte[] result = new byte[stringA.length];

    for (int i = 0; i < stringA.length; i++) {
      result[i] = (byte)(stringA[i] ^ stringB[i]);
    }

    return result;
  }

  public byte[] leftShift(byte[] array, int totalShift) {
    byte[] firstPart = Arrays.copyOfRange(array, 0, totalShift);
    byte[] secondPart = Arrays.copyOfRange(array, totalShift, 32);
    byte[] result = new byte[array.length];

    for (int i = 0; i < secondPart.length; i++) {
      result[i] = secondPart[i];
    }

    for (int i = 0; i < firstPart.length; i++) {
      result[i + (array.length - totalShift)] = firstPart[i];
    }

    return result;
  }

  // testing
  public static void main(String[] args) {
    // Generate Keys
    String keyMsg = "abcdefghijklmnopabcdefghijklmnopabcdefghijklmnopabcdefghijklmnopabcdefghijklmnopabcdefghijklmnopabcdefghijklmnopabcdefghijklmnop";
    KeyExpansion key = new KeyExpansion(keyMsg);

    System.out.println("Key: " + key.internalKeys);
    
    for (int i = 0; i < key.internalKeys.length; i++) {
      for (int j = 0; j < key.internalKeys[i].length; j++) {
        System.out.println("internalKey[" + i + "][" + j + "]: " + key.internalKeys[i][j]);
      }
    }
  }
}