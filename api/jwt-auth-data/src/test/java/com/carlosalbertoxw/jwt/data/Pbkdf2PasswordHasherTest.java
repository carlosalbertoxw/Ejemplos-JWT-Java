package com.carlosalbertoxw.jwt.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carlosalbertoxw.jwt.core.security.Pbkdf2PasswordHasher;
import org.junit.jupiter.api.Test;

class Pbkdf2PasswordHasherTest {

    private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher(1_000);

    @Test
    void verifiesTheRightPasswordOnly() {
        String hash = hasher.hash("Secreta123!");

        assertTrue(hasher.verify("Secreta123!", hash));
        assertFalse(hasher.verify("otra", hash));
        assertFalse(hasher.verify("Secreta123!", "formato-invalido"));
    }

    @Test
    void usesARandomSaltAndStoresTheIterations() {
        String first = hasher.hash("igual");
        String second = hasher.hash("igual");

        assertNotEquals(first, second);
        assertTrue(first.startsWith("1000."));
        // Un hasher con otras iteraciones sigue verificando hashes antiguos.
        assertTrue(new Pbkdf2PasswordHasher(2_000).verify("igual", first));
    }
}
