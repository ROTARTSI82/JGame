package com.rotartsi.jgame.crypto;

import com.rotartsi.jgame.Constants;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.LinkedList;

import static com.rotartsi.jgame.Constants.JGameStr;
import static com.rotartsi.jgame.util.StringManager.fmt;

/**
 * Modified version of code <a href="https://introcs.cs.princeton.edu/java/99crypto/RSA.java.html">this code</a>
 * by Robert Sedgewick and Kevin Wayne that addresses the known bugs.
 * Generate an N-bit public and private RSA key and use to encrypt
 * and decrypt a random message.
 */
public class RSA implements Serializable {
    // TODO: Make these values transient?

    /**
     * The bit-length of the privateKey.
     */
    private int nBits;

    /**
     * Messages are raised to the power of the privateKey mod {@link #modulus} to decrypt them.
     * <p>
     * To encrypt, the are raised to the power of 65537 mod {@link #modulus}.
     */
    private BigInteger privateKey;
    /**
     * Messages are moded by this value to both encrypt and decrypt.
     */
    private BigInteger modulus;

    /**
     * See <a href="https://en.wikipedia.org/wiki/Golden_ratio">Phi on Wikipedia</a>
     */
    private BigInteger phi;

    /**
     * Keep generating public and private keys until the modulus is exactly N bits
     * and the {@code gcd(phi, publicKey) != 1}
     *
     * @param N number of bits for the keys.
     */
    public RSA(int N) {
        nBits = N;
        recalculateKey();
        // Calculates keys until the gcd is 1 and the bitLength is exactly N
        while (phi.gcd(Constants.universalPublic).compareTo(Constants.one) != 0 ||
                modulus.bitLength() != nBits) {
            recalculateKey();
        }
    }

    /**
     * Calculate a key. This key is not necessarily valid, and should be used in a loop.
     * <p>
     * Is called by constructor in a loop to eventually generate a valid key.
     */
    private void recalculateKey() {
        BigInteger p = BigInteger.probablePrime(nBits / 2, Constants.secureRand);
        BigInteger q = BigInteger.probablePrime(nBits / 2, Constants.secureRand);
        phi = (p.subtract(Constants.one)).multiply(q.subtract(Constants.one));

        modulus = p.multiply(q);
        privateKey = Constants.universalPublic.modInverse(phi);
    }

    /**
     * Get all the characters used in a string literal without duplicate values.
     *
     * @param str String
     * @return List of all characters
     */
    public static LinkedList<Integer> getAlphabet(String str) {
        LinkedList<Integer> ret = new LinkedList<>();
        for (char c : str.toCharArray()) {
            int intVal = (int) c;
            if (!ret.contains(intVal)) {
                ret.add(intVal);
            }
        }
        return ret;
    }

    /**
     * Encrypt a BigInteger that is smaller than modulus using the public key.
     *
     * @param message BigInteger to encrypt
     * @return encrypted BigInteger
     */
    public BigInteger rawEncrypt(BigInteger message) {
        if (message.compareTo(modulus) >= 0) {
            throw new IllegalArgumentException(JGameStr.getString("crypto.RSA.messageTooBig"));
        }
        return message.modPow(Constants.universalPublic, modulus);
    }

    /**
     * All integers to be encrypted must be smaller than this value.
     *
     * @return Modulus
     */
    public BigInteger getModulus() {
        return modulus;
    }

    /**
     * Decrypt an encrypted BigInteger using the private key.
     *
     * @param encrypted BigInteger to decrypt
     * @return decrypted Biginteger
     */
    public BigInteger rawDecrypt(BigInteger encrypted) {
        return encrypted.modPow(privateKey, modulus);
    }

    /**
     * Encrypt string using RSA.
     *
     * Split the string into {@code chunkSize} and get the index of each character
     * and encode it using the size of the alphabet array as the base.
     * Then use {@link #rawEncrypt(BigInteger)} to encrypt each item.
     *
     * For example: msg="LOREMIPSUM" chunkSize=4 alphabet=['L', 'O', 'R', 'E', 'M', 'I', 'P', 'S', 'U']
     * <ol>
     *     <li>
     *         Split the string into chunks of 4: ["LORE", "MIPS", "UM"]
     *     </li>
     *     <li>
     *         Replace each character with it's index in the alphabet: [[0, 1, 2, 3], [4, 5, 6, 7], [8, 4]]
     *     </li>
     *     <li>
     *         Encode each item using base8 (actually interprets literal backwards so that
     *         the reverse greedy method returns the digits in the right order at
     *         {@link #decryptString(BigInteger[], LinkedList)}):
     *         [2358, 5638, 44]
     *     </li>
     *     <li>
     *         RSA encrypt each item using {@link #rawEncrypt(BigInteger)}
     *     </li>
     * </ol>
     *
     * @param msg String to encrypt
     * @param chunkSize Size to split into
     * @param alphabet Valid characters
     * @return Encrypted BigInteger[]
     */
    public BigInteger[] encryptString(String msg, int chunkSize, LinkedList<Integer> alphabet) {
        BigInteger base = new BigInteger(String.valueOf(alphabet.size() + 1));
        String[] split = msg.split(fmt("(?<=\\G.{%s})", chunkSize));

        LinkedList<BigInteger> ret = new LinkedList<>();
        for (String x : split) {
            BigInteger val = new BigInteger("0");
            int place = 0;
            for (char c : x.toCharArray()) {
                int intVal = (int) c;
                if (alphabet.contains(intVal)) {
                    intVal = 1 + alphabet.indexOf(intVal);
                } else {
                    throw new IllegalArgumentException(
                            JGameStr.getString("crypto.RSA.invalidChar"));
                }
                val = val.add(base.pow(place).multiply(new BigInteger(String.valueOf(intVal))));
                place++;
            }
            ret.add(rawEncrypt(val));
        }
        return ret.toArray(new BigInteger[0]);
    }

    /**
     * Decrypt a BigInteger[] encrypted using {@link #encryptString(String, int, LinkedList)}
     *
     * Use the reverse greedy method to interpret the base (which is the size of the alphabet) and
     * add the character values to the StringBuilder
     *
     * For example: msg=[..., ] chunkSize=4 alphabet=['L', 'O', 'R', 'E', 'M', 'I', 'P', 'S', 'U']
     * (See example at {@link #encryptString(String, int, LinkedList)}
     * <ol>
     *     <li>
     *         RSA decrypt each item using {@link #rawDecrypt(BigInteger)}: [2358, 5638, 44]
     *     </li>
     *     <li>
     *         Base8 decode each item using the reverse greedy method (this reconstructs the literal backwards because
     *         the greedy method returns the least significant digit first,
     *         but this reconstructs the literal correctly because
     *         {@link #encryptString(String, int, LinkedList)} interprets literals backwards):
     *         [[0, 1, 2, 3], [4, 5, 6, 7], [8, 4]]
     *
     *     </li>
     *     <li>
     *         Add the character values to the StringBuilder
     *     </li>
     * </ol>
     * @param encrypted ciphertext
     * @param alphabet valid chars
     * @return original string
     */
    public String decryptString(BigInteger[] encrypted, LinkedList<Integer> alphabet) {
        BigInteger base = new BigInteger(String.valueOf(alphabet.size() + 1));
        StringBuilder builder = new StringBuilder();
        for (BigInteger i : encrypted) {
            i = rawDecrypt(i);
            while (i.compareTo(Constants.zero) != 0) {
                builder.append((char) (int) alphabet.get(i.mod(base).intValue() - 1));
                i = i.divide(base);
            }
        }
        return builder.toString();
    }

    /**
     * toString() for pretty printing.
     *
     * @return String
     */
    @Override
    public String toString() {
        return fmt(JGameStr.getString("crypto.RSA.toStringFormat"),
                privateKey, modulus, nBits);
    }
}