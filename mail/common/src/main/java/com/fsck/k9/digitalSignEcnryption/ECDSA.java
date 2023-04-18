// package com.fsck.k9.digitalSignEcnryption;

// import com.fsck.k9.digitalSignEcnryption.utils.BinaryAscii;
import utils.BinaryAscii;
// import com.fsck.k9.digitalSignEcnryption.utils.RandomInteger;
import utils.RandomInteger;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class ECDSA {
  public static Signature sign(String msg, PrivateKey key, MessageDigest hash) {
    byte[] hashValue = hash.digest(msg.getBytes());
    BigInteger rawMsg = BinaryAscii.stringToNum(hashValue);
    Curve curve = key.curve;
    BigInteger random = RandomInteger.between(BigInteger.ONE, curve.N);
    Point randomSignPoint = Math.multiply(curve.G, random, curve.N, curve.A, curve.P);
    BigInteger r = randomSignPoint.x.mod(curve.N);
    BigInteger s = ((rawMsg.add(r.multiply(key.secret))).multiply(Math.inv(random, curve.N))).mod(curve.N);

    return new Signature(r, s);
  }

  public static boolean verify(String msg, Signature signature, PublicKey key, MessageDigest hash) {
    byte[] hashValue = hash.digest(msg.getBytes());
    BigInteger rawMsg = BinaryAscii.stringToNum(hashValue);
    Curve curve = key.curve;
    BigInteger r = signature.r;
    BigInteger s = signature.s;

    if (r.compareTo(new BigInteger(String.valueOf(1))) < 0) {
      return false;
    }
    if (r.compareTo(curve.N) >= 0) {
      return false;
    }
    if (s.compareTo(new BigInteger(String.valueOf(1))) < 0) {
      return false;
    }
    if (s.compareTo(curve.N) >= 0) {
      return false;
    }
    
    BigInteger w = Math.inv(s, curve.N);
    Point u1 =Math.multiply(curve.G, rawMsg.multiply(w).mod(curve.N), curve.N, curve.A, curve.P);
    Point u2 = Math.multiply(key.point, r.multiply(w).mod(curve.N), curve.N, curve.A, curve.P);
    Point v = Math.add(u1, u2, curve.A, curve.P);

    if (v.isAtInfinity()) {
      return false;
    }

    return v.x.mod(curve.N).equals(r);
  }

  // testing ecdsa
  public static void main(String[] args) {
    // Generate Keys
    PrivateKey privateKey = new PrivateKey();
    PublicKey publicKey = privateKey.publicKey();

    String exampleMsg = "Tubes susah :(";
    
    try {
      Signature signature = sign(exampleMsg, privateKey, MessageDigest.getInstance("SHA-256"));

      // Verify if signature is valid
      boolean verified = verify(exampleMsg, signature, publicKey, MessageDigest.getInstance("SHA-256"));

      // Return the signature verification status
      System.out.println("Kecocokan pesan dgn signature: " + verified);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Could not find SHA-256 message digest in provided java environment");
    }
  }
}