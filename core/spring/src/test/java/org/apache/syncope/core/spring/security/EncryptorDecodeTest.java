package org.apache.syncope.core.spring.security;

import org.apache.syncope.common.lib.types.CipherAlgorithm;
import org.apache.syncope.core.spring.security.utils.Algorithm;
import org.apache.syncope.core.spring.security.utils.Utils;
import org.apache.syncope.core.spring.security.utils.ValueStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class EncryptorDecodeTest {

    private static Encryptor encryptor;
    private String valueEncodedTest;
    private String valueDecodedTest;
    private org.apache.syncope.common.lib.types.CipherAlgorithm cipherAlgorithmTest;
    private Algorithm algorithmTest;
    private ValueStatus encodedStatusTest;
    private Class<? extends Exception> exceptionOutputTest;
    private static Utils utils;

    public EncryptorDecodeTest(ValueStatus encodedStatusTest,Algorithm algorithmTest, Class<? extends Exception> exceptionOutputTest) {
        this.encodedStatusTest = encodedStatusTest;
        this.algorithmTest = algorithmTest;
        this.exceptionOutputTest = exceptionOutputTest;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // ValueStatus              Algorithm                    Exception
                {ValueStatus.VALID,         Algorithm.AES,               null},
                {ValueStatus.VALID,         Algorithm.BCRYPT,            null},
                {ValueStatus.VALID,         Algorithm.NOT_AN_ALGO,       NullPointerException.class},
                {ValueStatus.NULL,          Algorithm.AES,               null},
                //{ValueStatus.VALID,         Algorithm.NULL,              null},
                {ValueStatus.EMPTY,         Algorithm.SHA256,            null},
                {ValueStatus.VALID,         Algorithm.AES,               null},
                {ValueStatus.UNICODE,       Algorithm.AES,               null},
                {ValueStatus.UNICODE,       Algorithm.SHA256,            null}
        });
    }

    @BeforeClass
    public static void setUpClass() {
        encryptor = Encryptor.getInstance("Isw2024");
        utils = new Utils(42);
    }

    @Before
    public void setup() {
        setupEncode(encodedStatusTest);
        setupAlgo(algorithmTest);
    }

    private void setupEncode(ValueStatus valueStatusTest) {
        switch (valueStatusTest) {
            case VALID:
                valueDecodedTest = "testValue";
                break;
            case NULL:
                valueDecodedTest = null;
                break;
            case EMPTY:
                valueDecodedTest = "";
                break;
            case UNICODE:
                valueDecodedTest = utils.UnicodeGeneratorString();
        }
    }

    private void setupAlgo(Algorithm algorithmTest) {
        switch (algorithmTest) {
            case AES:
                cipherAlgorithmTest = CipherAlgorithm.AES;
                break;
            case NOT_AN_ALGO:
                cipherAlgorithmTest = Mockito.mock(CipherAlgorithm.class);
                Mockito.when(cipherAlgorithmTest.getAlgorithm()).thenReturn(null);
                break;
            case SHA256:
                cipherAlgorithmTest = CipherAlgorithm.SHA256;
                break;
            case BCRYPT:
                cipherAlgorithmTest = CipherAlgorithm.BCRYPT;
                break;
            default:
                cipherAlgorithmTest = null;
                break;
        }
    }

    @Test
    public void test() {
        if (exceptionOutputTest == null) {
            // Quando non ci si aspetta un'eccezione
            String decodedResult = null;
            try {
                // Codifica il valore
                valueEncodedTest = encryptor.encode(valueDecodedTest, cipherAlgorithmTest);
                // Decodifica il valore codificato
                decodedResult = encryptor.decode(valueEncodedTest, cipherAlgorithmTest);
            } catch (Exception e) {
                Assert.fail("L'eccezione " + e.getClass() + " non era prevista. Dati: " + encodedStatusTest + " " + algorithmTest);
            }
            if (cipherAlgorithmTest != null && !cipherAlgorithmTest.isInvertible()) {
                valueDecodedTest=null;
            }
            Assert.assertEquals("Errore nella decodifica con i dati: " + valueEncodedTest + " " + algorithmTest + " " + valueDecodedTest + " = " + decodedResult,
                    valueDecodedTest, decodedResult);
        } else {
            // Quando aspettiamo un'eccezione
            boolean exceptionThrown = false;
            try {
                // Prova ad eseguire encode e decode
                String encodedResult = encryptor.encode(valueDecodedTest, cipherAlgorithmTest);
                encryptor.decode(encodedResult, cipherAlgorithmTest);
            } catch (Exception e) {
                if (e.getClass().equals(exceptionOutputTest)) {
                    exceptionThrown = true;
                }
            }

            Assert.assertTrue("Errore nella gestione dell'eccezione con i dati: " + encodedStatusTest + " " + algorithmTest,
                    exceptionThrown);
        }
    }
}