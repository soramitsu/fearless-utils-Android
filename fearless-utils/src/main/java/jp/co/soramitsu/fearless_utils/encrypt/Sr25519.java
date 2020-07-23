package jp.co.soramitsu.fearless_utils.encrypt;

public final class Sr25519 {

    static {
        System.loadLibrary("sr25519java");
    }

    public static native String test(String hello_what);

    /**
     * Verify a message and its corresponding against a public key;
     *
     * @param signature:  verify this signature
     * @param message:    arbitrary message
     * @param public_key: verify with this public key
     * @return true if signature is valid, false otherwise
     */
    public static native boolean verify(byte[] signature, byte[] message, byte[] public_key);

    /**
     * Sign a message
     * The combination of both public and private key must be provided.
     * This is effectively equivalent to a keypair.
     *
     * @param public_key: public key
     * @param secret:     private key (secret)
     * @param message:    Arbitrary message
     * @return the signature
     */
    public static native byte[] sign(byte[] public_key, byte[] secret, byte[] message);

    /**
     * Perform a derivation on a secret
     *
     * @param pair: existing keypair - input buffer of SR25519_KEYPAIR_SIZE bytes
     * @param cc:   chaincode - input buffer of SR25519_CHAINCODE_SIZE bytes
     * @return pre-allocated output buffer of SR25519_KEYPAIR_SIZE bytes
     */
    public static native byte[] deriveKeypairHard(byte[] pair, byte[] cc);


    /**
     * Perform a derivation on a secret
     *
     * @param pair: existing keypair - input buffer of SR25519_KEYPAIR_SIZE bytes
     * @param cc:   chaincode - input buffer of SR25519_CHAINCODE_SIZE bytes
     * @return keypair: pre-allocated output buffer of SR25519_KEYPAIR_SIZE bytes
     */
    public static native byte[] deriveKeypairSoft(byte[] pair, byte[] cc);

    /**
     * Perform a derivation on a publicKey
     *
     * @param public_key: public key - input buffer of SR25519_PUBLIC_SIZE bytes
     * @param cc:         chaincode - input buffer of SR25519_CHAINCODE_SIZE bytes
     * @return pre-allocated output buffer of SR25519_PUBLIC_SIZE bytes
     */
    public static native byte[] derivePublicSoft(byte[] public_key, byte[] cc);


    /**
     * Generate a key pair.
     *
     * @param seed: generation seed - input buffer of SR25519_SEED_SIZE bytes
     * @return keypair [32b key | 32b nonce | 32b public], pre-allocated output buffer of SR25519_KEYPAIR_SIZE bytes
     */
    public static native byte[] keypairFromSeed(byte[] seed);
}
