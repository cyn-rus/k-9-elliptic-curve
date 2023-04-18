// package com.fsck.k9.digitalSignEcnryption;

// import com.fsck.k9.digitalSignEcnryption.utils.ByteString;
import utils.ByteString;

import java.util.Arrays;


public class PublicKey {
  public Point point;
  public Curve curve;

  public PublicKey(Point point, Curve curve) {
    this.point = point;
    this.curve = curve;
  }

  public ByteString toByteString() {
    return toByteString(false);
  }

  public ByteString toByteString(boolean encoded) {
    ByteString xStr = Utils.numToString(point.x, curve.length());
    ByteString yStr = Utils.numToString(point.y, curve.length());
    xStr.insert(yStr.getBytes());

    if(encoded) {
        xStr.insert(0, new byte[]{0, 4} );
    }

    return xStr;
  }

  public ByteString toDer() {
    long[] oidEcPublicKey = new long[]{1, 2, 840, 10045, 2, 1};
    ByteString encodeEcAndOid = Utils.encodeSequence(Utils.encodeOid(oidEcPublicKey), Utils.encodeOid(curve.oid));
    return Utils.encodeSequence(encodeEcAndOid, Utils.encodeBitString(this.toByteString(true)));
  }

  public String toPem() {
    return Utils.toPem(this.toDer(), "PUBLIC KEY");
  }

  public static PublicKey fromPem(String string) {
    return PublicKey.fromDer(Utils.fromPem(string));
  }

  public static PublicKey fromDer(ByteString string) {
    ByteString[] str = Utils.removeSequence(string);
    ByteString s1 = str[0];
    ByteString empty = str[1];

    if (!empty.isEmpty()) {
      throw new RuntimeException (String.format("trailing junk after DER pubkey: %s", Utils.binToHex(empty)));
    }

    str = Utils.removeSequence(s1);
    ByteString s2 = str[0];
    ByteString pointStrBitstring = str[1];
    Object[] o = Utils.removeObject(s2);
    ByteString rest = (ByteString) o[1];
    o = Utils.removeObject(rest);
    long[] oidCurve = (long[]) o[0];
    empty = (ByteString) o[1];

    if (!empty.isEmpty()) {
      throw new RuntimeException (String.format("trailing junk after DER pubkey objects: %s", Utils.binToHex(empty)));
    }

    Curve curve = (Curve) Curve.curvesByOid.get(Arrays.hashCode(oidCurve));

    if (curve == null) {
      throw new RuntimeException(String.format("Unknown curve with oid %s. I only know about these: %s", Arrays.toString(oidCurve), Arrays.toString(Curve.supportedCurves.toArray())));
    }

    str = Utils.removeBitString(pointStrBitstring);
    ByteString pointStr = str[0];
    empty = str[1];

    if (!empty.isEmpty()) {
      throw new RuntimeException (String.format("trailing junk after pubkey pointstring: %s", Utils.binToHex(empty)));
    }

    return PublicKey.fromString(pointStr.substring(2), curve);
  }

  public static PublicKey fromString(ByteString string, Curve curve, boolean validatePoint) {
    int baselen = curve.length();

    ByteString xs = string.substring(0, baselen);
    ByteString ys = string.substring(baselen);

    Point p = new Point(Utils.stringToNum(xs.getBytes()), Utils.stringToNum(ys.getBytes()));

    PublicKey publicKey = new PublicKey(p, curve);

    if (!validatePoint) {
      return publicKey;
    }
    if (p.isAtInfinity()) {
      throw new RuntimeException("Public Key point is at infinity");
    }
    if (!curve.contains(p)) {
      throw new RuntimeException(String.format("Point (%s,%s) is not valid for curve %s", p.x, p.y, curve.name));
    }
    if (!Math.multiply(p, curve.N, curve.N, curve.A, curve.P).isAtInfinity()) {
      throw new RuntimeException(String.format("Point (%s,%s) * %s.N is not at infinity", p.x, p.y, curve.name));
    }

    return publicKey;
  }

  public static PublicKey fromString(ByteString string, Curve curve) {
    return fromString(string, curve, true);
  }

  public static PublicKey fromString(ByteString string, boolean validatePoint) {
    return fromString(string, Curve.secp256k1, validatePoint);
  }

  public static PublicKey fromString(ByteString string) {
    return fromString(string, true);
  }
}