package org.apache.syncope.core.spring.security;

import org.apache.syncope.common.lib.types.CipherAlgorithm;
import org.apache.syncope.core.spring.security.utils.Algorithm;
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
public class EncryptorEncodeTest {

    private static Encryptor encryptor;
    private String valueToEncodeTest;
    private org.apache.syncope.common.lib.types.CipherAlgorithm cipherAlgorithmTest;
    private ValueStatus valueStatusTest;
    private Algorithm algorithmTest;
    private Class<? extends Exception> exceptionOutputTest;

    public EncryptorEncodeTest(ValueStatus valueStatusTest, Algorithm algorithmTest, Class<? extends Exception> exceptionOutputTest) {
        this.valueStatusTest = valueStatusTest;
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
                {ValueStatus.VALID,         Algorithm.NULL,              null},
                {ValueStatus.EMPTY,         Algorithm.SHA256,            null},
                {ValueStatus.VALID,         Algorithm.AES,               null}
        });
    }

    @BeforeClass
    public static void setUpClass() {
        encryptor = Encryptor.getInstance("Isw2024");
    }

    @Before
    public void setup() {
        setupValue(valueStatusTest);
        setupAlgo(algorithmTest);
    }

    private void setupValue(ValueStatus valueStatusTest) {
        switch (valueStatusTest) {
            case VALID:
                valueToEncodeTest = "testValue";
                break;
            case NULL:
                valueToEncodeTest = null;
                break;
            case EMPTY:
                valueToEncodeTest = "";
                break;
        }
    }

    private void setupAlgo(Algorithm algorithmTest) {
        switch (algorithmTest) {
            case AES:
                cipherAlgorithmTest = CipherAlgorithm.AES;
                break;
            case BCRYPT:
                cipherAlgorithmTest = CipherAlgorithm.BCRYPT;
                break;
            case NOT_AN_ALGO:
                cipherAlgorithmTest = Mockito.mock(CipherAlgorithm.class);
                Mockito.when(cipherAlgorithmTest.getAlgorithm()).thenReturn(null);
                break;
            case SHA256:
                cipherAlgorithmTest = CipherAlgorithm.SHA256;
                break;
            default:
                cipherAlgorithmTest = null;
                break;
        }
    }

    @Test
    public void test() {
        if (exceptionOutputTest == null) {
            // Quando non ci si aspetta un'eccezione, eseguiamo l'encode e confrontiamo l'output
            String result = null;
            Boolean resultOut;
            try {
                result = encryptor.encode(valueToEncodeTest, cipherAlgorithmTest);
            } catch (Exception e) {
                // Se è stato lanciato un errore inaspettato, falliamo il test
                Assert.fail("L'eccezione"+ e.getClass() + "non era prevista. Dati: " + valueStatusTest + " " + algorithmTest);
            }
            // Verifica che l'encode sia corretto sia
            resultOut = encryptor.verify(valueToEncodeTest, cipherAlgorithmTest, result);

            if (valueToEncodeTest!=null)
                Assert.assertTrue("Errore nella codifica con i dati: " + valueStatusTest + " " + algorithmTest, resultOut);
            else
                Assert.assertFalse("Errore non avvenuto nella codifica con i dati: " + valueStatusTest + " " + algorithmTest, resultOut);
        } else {
            // Quando ci si aspetta un'eccezione, proviamo a lanciare l'encode e verifichiamo l'eccezione
            boolean exceptionThrown = false;
            try {
                encryptor.encode(valueToEncodeTest, cipherAlgorithmTest);
            } catch (Exception e) {
                // Verifica che l'eccezione sollevata sia quella che ci aspettiamo
                if (e.getClass().equals(exceptionOutputTest)) {
                    exceptionThrown = true;
                }
            }

            // Assicura che l'eccezione prevista sia stata effettivamente lanciata
            Assert.assertTrue("Errore nella gestione dell'eccezione con i dati: " + valueStatusTest + " " + algorithmTest,
                    exceptionThrown);
        }
    }
}
