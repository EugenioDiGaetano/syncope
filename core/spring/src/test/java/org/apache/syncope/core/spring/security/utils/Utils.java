package org.apache.syncope.core.spring.security.utils;

import java.util.Random;

public class Utils {
    private final Random random; // Un'unica istanza Random per garantire numeri progressivi
    private final long seed; // Seed per garantire ripetibilità

    // Costruttore: inizializza il seed una volta sola
    public Utils(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    // Metodo con valori di default per minLength e maxLength
    public String UnicodeGeneratorString() {
        return UnicodeGenerator(50, 2500); // Valori di default
    }

    // Metodo principale con parametri espliciti
    public String UnicodeGenerator(int minLength, int maxLength) {

        StringBuilder unicodeString = new StringBuilder();

        // Determina una lunghezza casuale tra minLength e maxLength
        int stringLength = minLength + random.nextInt(maxLength - minLength + 1);

        // Genera i caratteri Unicode non-ASCII
        for (int i = 0; i < stringLength; i++) {
            int randomCodePoint = 0x0080 + random.nextInt(0x07FF - 0x0080); // Intervallo U+0080 a U+07FF
            unicodeString.append((char) randomCodePoint);
        }

        // Inserisce almeno uno spazio a una o più posizioni casuali
        insertRandomSpaces(unicodeString);
        return unicodeString.toString();
    }

    // Metodo per inserire spazi casuali
    private void insertRandomSpaces(StringBuilder unicodeString) {
        int length = unicodeString.length();

        // Inserisce almeno uno spazio
        int spacePosition = random.nextInt(length);
        unicodeString.setCharAt(spacePosition, ' '); // Inserisce spazio U+0020

        // Possibilità di aggiungere altri spazi
        int additionalSpaces = 1 + random.nextInt(3); // Numero casuale di spazi extra (1-3)
        for (int i = 0; i < additionalSpaces; i++) {
            int randomPosition = random.nextInt(length);
            unicodeString.setCharAt(randomPosition, ' ');
        }
    }

}
