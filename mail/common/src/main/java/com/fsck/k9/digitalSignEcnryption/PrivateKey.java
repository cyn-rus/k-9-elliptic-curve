// package com.fsck.k9.digitalSignEcnryption;

// import com.fsck.k9.digitalSignEcnryption.utils.ByteString;
import utils.ByteString;

import java.math.BigInteger;
import java.util.Arrays;


public class PrivateKey {
  public Curve curve;
  public BigInteger secret;

  public PrivateKey() {
    this(Curve.secp256k1, null);
    secret = Utils.between(BigInteger.ONE, curve.N);
  }

  public PrivateKey(Curve curve, BigInteger secret) {
    this.curve = curve;
    this.secret = secret;
  }

  public PublicKey publicKey() {
    Curve curve = this.curve;
    Point publicPoint = Math.multiply(curve.G, this.secret, curve.N, curve.A, curve.P);
    return new PublicKey(publicPoint, curve);
  }

  public ByteString toByteString() {
    return Utils.numToString(this.secret, this.curve.length());
  }

  public ByteString toDer() {
    ByteString encodedPublicKey = this.publicKey().toByteString(true);
    return Utils.encodeSequence(
          Utils.encodeInteger(BigInteger.valueOf(1)),
          Utils.encodeOctetString(this.toByteString()),
          Utils.encodeConstructed(0, Utils.encodeOid(this.curve.oid)),
          Utils.encodeConstructed(1, Utils.encodeBitString(encodedPublicKey)));
  }

  public String toPem() {
    return Utils.toPem(this.toDer(), "EC PRIVATE KEY");
  }

  public static PrivateKey fromPem(String string) {
    String privkeyPem = string.substring(string.indexOf("-----BEGIN EC PRIVATE KEY-----"));
    return PrivateKey.fromDer(Utils.fromPem(privkeyPem));
  }

  public static PrivateKey fromDer(String string) {
    return fromDer(new ByteString(string.getBytes()));
  }

  public static PrivateKey fromDer(ByteString string) {
    ByteString[] str = Utils.removeSequence(string);
    ByteString s = str[0];
    ByteString empty = str[1];

    if (!empty.isEmpty()) {
      throw new RuntimeException(String.format("trailing junk after DER privkey: %s", Utils.binToHex(empty)));
    }

    Object[] o = Utils.removeInteger(s);
    long one = Long.valueOf(o[0].toString());
    s = (ByteString) o[1];

    if (one != 1) {
      throw new RuntimeException(String.format("expected '1' at start of DER privkey, got %d", one));
    }

    str = Utils.removeOctetString(s);
    ByteString privkeyStr = str[0];
    s = str[1];
    Object[] t = Utils.removeConstructed(s);
    long tag = Long.valueOf(t[0].toString());
    ByteString curveOidStr = (ByteString) t[1];
    s = (ByteString) t[2];

    if (tag != 0) {
      throw new RuntimeException(String.format("expected tag 0 in DER privkey, got %d", tag));
    }

    o = Utils.removeObject(curveOidStr);
    long[] oidCurve = (long[]) o[0];
    empty = (ByteString) o[1];

    if (!"".equals(empty.toString())) {
      throw new RuntimeException(String.format("trailing junk after DER privkey curve_oid: %s", Utils.binToHex(empty)));
    }

    Curve curve = (Curve) Curve.curvesByOid.get(Arrays.hashCode(oidCurve));

    if (curve == null) {
      throw new RuntimeException(String.format("Unknown curve with oid %s. I only know about these: %s", Arrays.toString(oidCurve), Arrays.toString(Curve.supportedCurves.toArray())));
    }

    if (privkeyStr.length() < curve.length()) {
      int l = curve.length() - privkeyStr.length();
      byte[] bytes = new byte[l + privkeyStr.length()];
      for (int i = 0; i < curve.length() - privkeyStr.length(); i++) {
          bytes[i] = 0;
      }
      byte[] privateKey = privkeyStr.getBytes();
      System.arraycopy(privateKey, 0, bytes, l, bytes.length - l);
      privkeyStr = new ByteString(bytes);
    }

    return PrivateKey.fromString(privkeyStr, curve);
  }

  public static PrivateKey fromString(ByteString string, Curve curve) {
    return new PrivateKey(curve, Utils.stringToNum(string.getBytes()));
  }

  public static PrivateKey fromString(String string) {
    return fromString(new ByteString(string.getBytes()));
  }

  public static PrivateKey fromString(ByteString string) {
    return PrivateKey.fromString(string, Curve.secp256k1);
  }
}