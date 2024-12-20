package org.apache.syncope.core.spring.policy;

import org.apache.syncope.common.lib.policy.DefaultPasswordRuleConf;
import org.apache.syncope.core.spring.policy.utils.IllegalCharType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class DefaultPasswordRuleSetConfTest {

    private DefaultPasswordRule passwordRule;
    private final int alphabetical;
    private final int digit;
    private final int maxLength;
    private final int minLength;
    private final int repeatSame;
    private final int special;
    private final int upperCase;
    private final boolean usernameAllowed;
    private final IllegalCharType illegalCharsType;
    private Class<? extends Exception> exceptionOutputTest;

    private DefaultPasswordRuleConf conf;

    public DefaultPasswordRuleSetConfTest(
            int alphabetical, int digit, int maxLength, int minLength,
            int repeatSame, int special, int upperCase,
            boolean usernameAllowed, IllegalCharType illegalChars,
            Class<? extends Exception> exceptionOutputTest) {
        this.alphabetical = alphabetical;
        this.digit = digit;
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.repeatSame = repeatSame;
        this.special = special;
        this.upperCase = upperCase;
        this.usernameAllowed = usernameAllowed;
        this.illegalCharsType = illegalChars;
        this.exceptionOutputTest = exceptionOutputTest;
    }

    // Parametri del test
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // alphabetical,    digit,      maxLength,  minLength,      repeatSame,     special,    upperCase,      usernameAllowed,    illegalChars,                expectedOutput
                {0,                 0,          16,         0,              16,             0,          0,              true,               IllegalCharType.NULL,        java.lang.IllegalStateException.class},
                {1,                 1,          16,         1,              0,              1,          1,              true,               IllegalCharType.EMPTY,       null},
                {2,                 2,          16,         16,             2,              2,          2,              false,              IllegalCharType.ONE,         null},
                {2,                 2,          16,         1,              1,              2,          2,              false,              IllegalCharType.VALID,       java.lang.IllegalStateException.class},
                //{0,                 0,          0,          1,              0,              0,          0,              false,              IllegalCharType.VALID,       java.lang.IllegalStateException.class},
                {0,                 0,          1,          1,              0,              1,          0,              true,               IllegalCharType.VALID,       null},
                {0,                 2,          1,          0,              16,             1,          1,              true,               IllegalCharType.VALID,       null}
        });
    }

    @Before
    public void setUp() {
        passwordRule = new DefaultPasswordRule();

        conf = org.mockito.Mockito.spy(new DefaultPasswordRuleConf());
        conf.setAlphabetical(alphabetical);
        conf.setDigit(digit);
        conf.setMaxLength(maxLength);
        conf.setMinLength(minLength);
        conf.setRepeatSame(repeatSame);
        conf.setSpecial(special);
        conf.setUppercase(upperCase);
        conf.setUsernameAllowed(usernameAllowed);
        conf.getIllegalChars().clear();
        setUpIllegalChar();
    }

    private void setUpIllegalChar() {
        conf.getIllegalChars().clear();
        switch (illegalCharsType) {
            case VALID:
                conf.getIllegalChars().addAll(Arrays.asList('A', 'B', 'C', ' ', '!', '@'));
                break;
            case ONE:
                conf.getIllegalChars().add('%');
                break;
            case NULL:
                org.mockito.Mockito.when(conf.getIllegalChars()).thenReturn(null);
                break;
            default:
                break;
        }
    }

    @Test
    public void test() {
        if (exceptionOutputTest == null) {
            try {
                passwordRule.setConf(conf);
            } catch (Exception e) {
                // Se è stato lanciato un errore inaspettato, falliamo il test
                Assert.fail("L'eccezione " + e.getClass() + " non era prevista");
            }
            try {
                DefaultPasswordRuleConf retrievedConf = (DefaultPasswordRuleConf) passwordRule.getConf();
                Assert.assertNotNull("La configurazione restituita è null", retrievedConf);
                Assert.assertEquals(conf.getAlphabetical(), retrievedConf.getAlphabetical());
                Assert.assertEquals(conf.getDigit(), retrievedConf.getDigit());
                Assert.assertEquals(conf.getMaxLength(), retrievedConf.getMaxLength());
                Assert.assertEquals(conf.getMinLength(), retrievedConf.getMinLength());
                Assert.assertEquals(conf.getRepeatSame(), retrievedConf.getRepeatSame());
                Assert.assertEquals(conf.getSpecial(), retrievedConf.getSpecial());
                Assert.assertEquals(conf.getUppercase(), retrievedConf.getUppercase());
                Assert.assertEquals(conf.isUsernameAllowed(), retrievedConf.isUsernameAllowed());
                Assert.assertEquals(conf.getIllegalChars(), retrievedConf.getIllegalChars());
            } catch (Exception e) {
                Assert.fail("Errore durante la verifica dei campi di configurazione: " + e.getMessage());
            }
        }
        else {
            // Quando ci si aspetta un'eccezione, proviamo a lanciare la funzione e verifichiamo l'eccezione
            boolean exceptionThrown = false;
            try {
                passwordRule.setConf(conf);
            } catch (Exception e) {
                // Verifica che l'eccezione sollevata sia quella che ci aspettiamo
                if (e.getClass().equals(exceptionOutputTest)) {
                    exceptionThrown = true;
                }
            }
            // Assicura che l'eccezione prevista sia stata effettivamente lanciata
            Assert.assertTrue("Errore nella gestione dell'eccezione", exceptionThrown);
        }
    }
}