package org.apache.syncope.core.spring.security;

import org.apache.syncope.common.lib.types.CipherAlgorithm;
import org.apache.syncope.core.spring.ApplicationContextProvider;
import org.apache.syncope.core.spring.security.utils.Algorithm;
import org.apache.syncope.core.spring.security.utils.EncodedStatus;
import org.apache.syncope.core.spring.security.utils.ValueStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class EncryptorVerifyTest {

    private static Encryptor encryptor;
    private String valueToEncodeTest;
    private String encodedValueTest;
    private org.apache.syncope.common.lib.types.CipherAlgorithm cipherAlgorithmTest;
    private ValueStatus valueStatusTest;
    private Algorithm algorithmTest;
    private EncodedStatus encodedStatusTest;
    private Boolean expectedOutputTest;

    public EncryptorVerifyTest(ValueStatus valueStatusTest, Algorithm algorithmTest, EncodedStatus encodedStatusTest, Boolean expectedOutputTest) {
        this.valueStatusTest = valueStatusTest;
        this.algorithmTest = algorithmTest;
        this.encodedStatusTest = encodedStatusTest;
        this.expectedOutputTest = expectedOutputTest;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // ValueStatus              Algorithm                   EncodedStatus                   ExpectedOutput
                {ValueStatus.VALID,         Algorithm.AES,              EncodedStatus.CORRECT,          true},
                {ValueStatus.VALID,         Algorithm.BCRYPT,           EncodedStatus.CORRECT,          true},
                {ValueStatus.VALID,         Algorithm.NOT_AN_ALGO,      EncodedStatus.CORRECT,          false},
                {ValueStatus.NULL,          Algorithm.AES,              EncodedStatus.EMPTY,            false},
                {ValueStatus.VALID,         Algorithm.NULL,             EncodedStatus.CORRECT,          false},
                {ValueStatus.EMPTY,         Algorithm.SHA256,           EncodedStatus.EMPTY,            false},
                {ValueStatus.VALID,         Algorithm.AES,              EncodedStatus.NOT_CORRECT,      false}
        });
    }

    @BeforeClass
    public static void setUpClass() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        Mockito.mockStatic(ApplicationContextProvider.class);
        Mockito.when(ApplicationContextProvider.getBeanFactory()).thenReturn(beanFactory);
        beanFactory.registerSingleton("securityProperties", new SecurityProperties());
        encryptor = Encryptor.getInstance("Isw2024");
    }

    @Before
    public void setup() {
        setupValue(valueStatusTest);
        setupAlgo(algorithmTest);
        setupEncoded(encodedStatusTest);
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

    private void setupEncoded(EncodedStatus encodedStatusTest) {
        switch (encodedStatusTest) {
            case CORRECT:
                try {
                    if (cipherAlgorithmTest != null && valueToEncodeTest != null) {
                        encodedValueTest = encryptor.encode(valueToEncodeTest, cipherAlgorithmTest);
                    } else {
                        encodedValueTest = null;
                    }
                } catch (Exception e) {
                    encodedValueTest = null;
                }
                break;
            case EMPTY:
                encodedValueTest = "";
                break;
            case NOT_CORRECT:
                encodedValueTest = "invalidEncodedValue";
                break;
            default:
                encodedValueTest = null;
                break;
        }
    }

    @Test
    public void test() {
        boolean result = encryptor.verify(valueToEncodeTest, cipherAlgorithmTest, encodedValueTest);
        Assert.assertEquals("Errore nella verifica con i dati: " + valueStatusTest + " " + algorithmTest + " " + encodedStatusTest, expectedOutputTest, result);
    }
}
