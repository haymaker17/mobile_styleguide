package com.concur.mobile.core.util;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

public class Crypt {

    // ///////////////////////////////////////////////////////////////////////////////////
    // Begin encryption code
    // ///////////////////////////////////////////////////////////////////////////////////

    private final static char[] HEX = "0123456789ABCDEF".toCharArray();

    private String password;
    private byte[] key;
    private SecureRandom ivRandom;

    @SuppressWarnings("unused")
    private Crypt() {
        // No default
    }

    public Crypt(String password) {
        this.password = password;
    }

    /**
     * Convert an array of bytes to a hex encoded string
     */
    public static String byteToHex(byte[] buf) {
        String hexString = "";
        if (buf != null) {
            StringBuilder result = new StringBuilder(2 * buf.length);
            for (int i = 0; i < buf.length; i++) {
                result.append(HEX[(buf[i] >> 4) & 0x0f]).append(HEX[buf[i] & 0x0f]);
            }
            hexString = result.toString();
        }

        return hexString;
    }

    /**
     * Will convert two byte buffers into one hex-encoded string.
     * 
     * @param buf1
     *            the first byte buffer.
     * @param buf2
     *            the second byte buffer.
     * @return a string containing the hex-encoded bytes from first <code>buf1</code>, then <code>buf2</code>.
     */
    protected String byteToHex(byte[] buf1, byte[] buf2) {
        String hexString = "";
        int bufLen = 0;
        if (buf1 != null) {
            bufLen = buf1.length * 2;
        }
        if (buf2 != null) {
            bufLen += (buf2.length * 2);
        }
        StringBuilder strBldr = new StringBuilder(bufLen);
        if (buf1 != null) {
            for (int i = 0; i < buf1.length; ++i) {
                strBldr.append(HEX[(buf1[i] >> 4) & 0x0f]).append(HEX[buf1[i] & 0x0f]);
            }
        }
        if (buf2 != null) {
            for (int i = 0; i < buf2.length; ++i) {
                strBldr.append(HEX[(buf2[i] >> 4) & 0x0f]).append(HEX[buf2[i] & 0x0f]);
            }
        }
        hexString = strBldr.toString();
        return hexString;
    }

    private static byte getNibble(final char c) {
        switch (c) {
        case '0':
            return 0x00;
        case '1':
            return 0x01;
        case '2':
            return 0x02;
        case '3':
            return 0x03;
        case '4':
            return 0x04;
        case '5':
            return 0x05;
        case '6':
            return 0x06;
        case '7':
            return 0x07;
        case '8':
            return 0x08;
        case '9':
            return 0x09;
        case 'A':
            return 0x0A;
        case 'B':
            return 0x0B;
        case 'C':
            return 0x0C;
        case 'D':
            return 0x0D;
        case 'E':
            return 0x0E;
        case 'F':
            return 0x0F;
        default:
            return 0x00;
        }
    }

    /**
     * Convert a hex encoded string to an array of bytes;
     */
    public static byte[] hexToByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];

        for (int i = 0; i < len; i++) {
            byte hiNib = getNibble(hexString.charAt(i * 2));
            byte loNib = getNibble(hexString.charAt(i * 2 + 1));
            result[i] = (byte) ((hiNib << 4) + loNib);
        }
        return result;
    }

    /**
     * Generate the secret key bytes for all encryption/decryption
     */
    protected byte[] getKey() {

        if (key == null) {

            KeyGenerator keyGen;
            try {
                keyGen = KeyGenerator.getInstance("AES");
            } catch (NoSuchAlgorithmException e) {
                Log.e(Const.LOG_TAG, "Error getting key generator", e);
                return null;
            }

            SecureRandom sr;
            try {
                sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
            } catch (NoSuchAlgorithmException e) {
                Log.e(Const.LOG_TAG, "Error getting secure random", e);
                return null;
            } catch (NoSuchProviderException nspExc) {
                Log.e(Const.LOG_TAG, "Error getting secure random", nspExc);
                return null;
            }

            sr.setSeed(password.getBytes());
            keyGen.init(256, sr);
            SecretKey skey = keyGen.generateKey();

            key = skey.getEncoded();
        }

        return key;
    }

    /**
     * Return a random 16 byte initialization vector
     */
    protected byte[] getIv() {

        byte[] iv = new byte[16];

        if (ivRandom == null) {
            ivRandom = new SecureRandom();
        }

        ivRandom.nextBytes(iv);

        return iv;
    }

    /**
     * Generate a hash of the given text. Prefer SHA-2 and default to SHA-1 if needed.
     * 
     * @param plainText
     * 
     * @return If successful, a hex-encoded String of the hash. If unsuccesful, a blank string.
     */
    public String hash(String message) {

        String digest = "";

        if (message == null) {
            return digest;
        }

        MessageDigest digester = null;
        try {
            digester = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException nsae) {
            // Fall through and try SHA-1
        }

        if (digester == null) {
            try {
                digester = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException nsae) {
                // Well....
                Log.wtf(Const.LOG_TAG, "Hash alogrithms unavailable.  No hashing done.");
            }
        }

        if (digester != null) {
            byte[] digestBytes = digester.digest(message.getBytes());
            digest = byteToHex(digestBytes);
        }

        return digest;
    }

    /**
     * AES-256 (in CBC mode with PKCS7 padding) encryption of a string.
     * 
     * @param data
     *            the data byte array to be encrypted.
     * 
     * @return If succesful, a byte array consisting of the 16 byte initialization vector followed by the cipher bytes. If
     *         unsuccesful, <code>null</code> will be returned.
     */
    public byte[] encrypt(byte[] data) {
        byte[] cipherBytes = null;

        if (data != null && data.length > 0) {
            // Get the bytes of the secret key
            byte[] key = getKey();

            if (key != null) {
                SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

                try {

                    // Get our random initialization vector
                    byte[] iv = getIv();

                    // Setup our algo params with the IV.
                    AlgorithmParameters ap = AlgorithmParameters.getInstance("AES");
                    ap.init(new IvParameterSpec(iv));

                    // Get our cipher, init it, and encrypt
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ap);
                    byte[] encrypted = cipher.doFinal(data);

                    // Construct the resulting byte array.
                    cipherBytes = new byte[iv.length + encrypted.length];
                    System.arraycopy(iv, 0, cipherBytes, 0, iv.length);
                    System.arraycopy(encrypted, 0, cipherBytes, iv.length, encrypted.length);

                } catch (NoSuchAlgorithmException e) {
                    Log.e(Const.LOG_TAG, "Cipher algorithm not available", e);
                } catch (NoSuchPaddingException e) {
                    Log.e(Const.LOG_TAG, "Cipher padding not available", e);
                } catch (InvalidKeyException e) {
                    Log.e(Const.LOG_TAG, "Invalid key spec during init", e);
                } catch (IllegalBlockSizeException e) {
                    Log.e(Const.LOG_TAG, "Illegal block size during encrypt", e);
                } catch (BadPaddingException e) {
                    Log.e(Const.LOG_TAG, "Bad padding during encrypt", e);
                } catch (InvalidAlgorithmParameterException e) {
                    Log.e(Const.LOG_TAG, "Alogrithm parameter invalid", e);
                } catch (InvalidParameterSpecException e) {
                    Log.e(Const.LOG_TAG, "IV parameter invalid", e);
                }

            }
        } else {
            cipherBytes = data;
        }
        return cipherBytes;
    }

    /**
     * AES-256 (in CBC mode with PKCS7 padding) encryption of a string.
     * 
     * @param plainText
     * @return If succesful, a hex encoded string consisting of the 16 byte initialization vector followed by the cipher text. If
     *         unsuccesful, a blank string.
     */
    public String encrypt(String plainText) {
        String cipherText = "";

        if (plainText == null) {
            return cipherText;
        }

        // Get the bytes of the secret key
        byte[] key = getKey();

        if (key != null) {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            try {

                // Get our random initialization vector
                byte[] iv = getIv();

                // Setup our algo params with the IV.
                AlgorithmParameters ap = AlgorithmParameters.getInstance("AES");
                ap.init(new IvParameterSpec(iv));

                // Get our cipher, init it, and encrypt
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ap);
                byte[] encrypted = cipher.doFinal(plainText.getBytes());

                // Create the hex encoded cipher text string with the IV prepended to it.
                cipherText = byteToHex(iv, encrypted);

            } catch (NoSuchAlgorithmException e) {
                Log.e(Const.LOG_TAG, "Cipher algorithm not available", e);
            } catch (NoSuchPaddingException e) {
                Log.e(Const.LOG_TAG, "Cipher padding not available", e);
            } catch (InvalidKeyException e) {
                Log.e(Const.LOG_TAG, "Invalid key spec during init", e);
            } catch (IllegalBlockSizeException e) {
                Log.e(Const.LOG_TAG, "Illegal block size during encrypt", e);
            } catch (BadPaddingException e) {
                Log.e(Const.LOG_TAG, "Bad padding during encrypt", e);
            } catch (InvalidAlgorithmParameterException e) {
                Log.e(Const.LOG_TAG, "Alogrithm parameter invalid", e);
            } catch (InvalidParameterSpecException e) {
                Log.e(Const.LOG_TAG, "IV parameter invalid", e);
            }

        }

        return cipherText;
    }

    /**
     * AES-256 decryption of an encrypted hex string. The first 16 bytes of the hex string must be the IV used to encrypt it.
     * 
     * @param cipherText
     * @return The decrypted plain text if successful, otherwise a blank string.
     */
    public String decrypt(String cipherText) {
        String plainText = "";

        // Check for both null and an empty string.
        if (cipherText == null || cipherText.length() == 0) {
            return plainText;
        }

        // Get the bytes of the secret key
        byte[] key = getKey();

        if (key != null) {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            try {
                // Convert everything back to straight bytes. It is required that the first 16
                // bytes be the IV.
                byte[] cipherWithIv = hexToByte(cipherText);

                // Setup our algo params with the IV.
                AlgorithmParameters ap = AlgorithmParameters.getInstance("AES");
                ap.init(new IvParameterSpec(cipherWithIv, 0, 16));

                // Get our cipher, init it, and decrypt
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ap);
                byte[] decrypted = cipher.doFinal(cipherWithIv, 16, cipherWithIv.length - 16);

                plainText = new String(decrypted);

            } catch (NoSuchAlgorithmException e) {
                Log.e(Const.LOG_TAG, "Cipher algorithm not available", e);
            } catch (NoSuchPaddingException e) {
                Log.e(Const.LOG_TAG, "Cipher padding not available", e);
            } catch (InvalidKeyException e) {
                Log.e(Const.LOG_TAG, "Invalid key spec during init", e);
            } catch (IllegalBlockSizeException e) {
                Log.e(Const.LOG_TAG, "Illegal block size during decrypt", e);
            } catch (BadPaddingException e) {
                Log.e(Const.LOG_TAG, "Bad padding during decrypt", e);
            } catch (InvalidAlgorithmParameterException e) {
                Log.e(Const.LOG_TAG, "Alogrithm parameter invalid", e);
            } catch (InvalidParameterSpecException e) {
                Log.e(Const.LOG_TAG, "IV parameter invalid", e);
            }

        }

        return plainText;
    }

    /**
     * AES-256 decryption of encrypted bytes. The first 16 bytes of <code>cipherBytes</code> must be the IV used to encrypt it.
     * 
     * @param cipherBytes
     *            the encrypted cipher byte data.
     * @return The decrypted byte data if successful, otherwise <code>null</code>.
     */
    public byte[] decrypt(byte[] cipherBytes) {
        byte[] data = null;

        if (cipherBytes != null && cipherBytes.length > 0) {
            // Get the bytes of the secret key
            byte[] key = getKey();

            if (key != null) {
                SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

                try {
                    // Setup our algo params with the IV.
                    AlgorithmParameters ap = AlgorithmParameters.getInstance("AES");
                    ap.init(new IvParameterSpec(cipherBytes, 0, 16));

                    // Get our cipher, init it, and decrypt
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, ap);
                    data = cipher.doFinal(cipherBytes, 16, cipherBytes.length - 16);
                } catch (NoSuchAlgorithmException e) {
                    Log.e(Const.LOG_TAG, "Cipher algorithm not available", e);
                } catch (NoSuchPaddingException e) {
                    Log.e(Const.LOG_TAG, "Cipher padding not available", e);
                } catch (InvalidKeyException e) {
                    Log.e(Const.LOG_TAG, "Invalid key spec during init", e);
                } catch (IllegalBlockSizeException e) {
                    Log.e(Const.LOG_TAG, "Illegal block size during decrypt", e);
                } catch (BadPaddingException e) {
                    Log.e(Const.LOG_TAG, "Bad padding during decrypt", e);
                } catch (InvalidAlgorithmParameterException e) {
                    Log.e(Const.LOG_TAG, "Alogrithm parameter invalid", e);
                } catch (InvalidParameterSpecException e) {
                    Log.e(Const.LOG_TAG, "IV parameter invalid", e);
                }
            } else {
                data = cipherBytes;
            }
        }
        return data;
    }

}
