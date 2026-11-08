package com.carlosalbertoxw.jwt.core.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hashing de contrasenas con PBKDF2-HMAC-SHA256.
 *
 * <p>El resultado se codifica como {@code iteraciones.salBase64.hashBase64}: guardar
 * las iteraciones junto al hash permite subirlas en el futuro sin invalidar las
 * contrasenas existentes (cada hash se verifica con las iteraciones con que se creo).
 */
public class Pbkdf2PasswordHasher {

    /** Recomendacion de OWASP (2023) para PBKDF2-HMAC-SHA256. */
    public static final int DEFAULT_ITERATIONS = 600_000;

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final String SEPARATOR = ".";

    private final int iterations;
    private final SecureRandom random = new SecureRandom();
    private volatile String decoyHash;

    public Pbkdf2PasswordHasher() {
        this(DEFAULT_ITERATIONS);
    }

    /** Permite bajar las iteraciones en pruebas para que corran rapido. */
    public Pbkdf2PasswordHasher(int iterations) {
        this.iterations = iterations;
    }

    /** Deriva el hash codificado (con sal aleatoria) de la contrasena. */
    public String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] key = derive(password, salt, iterations);
        Base64.Encoder b64 = Base64.getEncoder();
        return iterations + SEPARATOR + b64.encodeToString(salt) + SEPARATOR + b64.encodeToString(key);
    }

    /** Verifica la contrasena en tiempo constante frente al hash codificado. */
    public boolean verify(String password, String encoded) {
        if (password == null || encoded == null) {
            return false;
        }
        String[] parts = encoded.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        try {
            int storedIterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            return MessageDigest.isEqual(derive(password, salt, storedIterations), expected);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Hash "senuelo" de una cadena descartable. Cuando el usuario no existe se
     * verifica contra el para que el tiempo de respuesta sea el mismo que con un
     * usuario real: si no, la diferencia revelaria que usuarios existen.
     */
    public String decoyHash() {
        String hash = decoyHash;
        if (hash == null) {
            hash = hash("senuelo-" + random.nextLong());
            decoyHash = hash;
        }
        return hash;
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        try {
            var spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo derivar el hash de la contrasena", e);
        }
    }
}
