package org.apache.syncope.core.spring.policy;

import org.apache.syncope.common.lib.policy.DefaultPasswordRuleConf;
import org.apache.syncope.common.lib.types.CipherAlgorithm;
import org.apache.syncope.core.persistence.api.entity.PlainAttr;
import org.apache.syncope.core.persistence.api.entity.user.LinkedAccount;
import org.apache.syncope.core.spring.policy.utils.Algorithm;
import org.apache.syncope.core.spring.policy.utils.PlainAttrStatus;
import org.apache.syncope.core.spring.security.Encryptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@RunWith(Parameterized.class)
public class DefaultPasswordRuleEnforceTest {

    private DefaultPasswordRule passwordRule;
    private Algorithm algorithmTest;
    private PlainAttrStatus plainAttrStatusTest;
    private Class<? extends Exception> exceptionOutputTest;
    protected static final Encryptor ENCRYPTOR = Encryptor.getInstance();

    private DefaultPasswordRuleConf conf;

    public DefaultPasswordRuleEnforceTest(Algorithm algorithmTest, PlainAttrStatus plainAttrStatusTest, Class<? extends Exception> exceptionOutputTest) {
        this.algorithmTest = algorithmTest;
        this.plainAttrStatusTest = plainAttrStatusTest;
        this.exceptionOutputTest = exceptionOutputTest;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                //Algo               Plain Attr                   expectedOutput
                {Algorithm.AES,      PlainAttrStatus.VALID,       PasswordPolicyException.class},
                {Algorithm.BCRYPT,   PlainAttrStatus.EMPTY,       null},
                {Algorithm.NULL,     PlainAttrStatus.VALID,       null},
                {Algorithm.AES,      PlainAttrStatus.INVALID,     PasswordPolicyException.class},
                {Algorithm.AES,      PlainAttrStatus.NULL,        PasswordPolicyException.class}
        });
    }

    @Before
    public void setup() {
        conf = new DefaultPasswordRuleConf();
        conf.setAlphabetical(1);
        conf.setDigit(1);
        conf.setMaxLength(16);
        conf.setMinLength(8);
        conf.setRepeatSame(2);
        conf.setUppercase(1);
        conf.setUsernameAllowed(false);
        conf.getIllegalChars().add('%');
        conf.getWordsNotPermitted().add("Isw2TestPas$£!");
        passwordRule = new DefaultPasswordRule();
        passwordRule.setConf(conf);
    }

    @Test
    public void testValidPass() {
        LinkedAccount account = setupMockedAccount();

        String validPassword = "ValidPaS$wd123!";
        runTestWithPassword(account, validPassword, false);

    }
    @Test
    public void testInvalidPass() {
        LinkedAccount account = setupMockedAccount();

        String invalidPassword = "short";
        if (algorithmTest==Algorithm.AES)
            runTestWithPassword(account, invalidPassword, true);
        else
            runTestWithPassword(account, invalidPassword, false);

    }


    // Configura un mock di LinkedAccount in base ai parametri di test.
    private LinkedAccount setupMockedAccount() {
        LinkedAccount account = Mockito.mock(LinkedAccount.class);

        // Configura il mock per PlainAttr in base al parametro plainAttrStatusTest
        Mockito.when(account.getPlainAttr(Mockito.anyString())).thenAnswer(invocation -> {
            switch (plainAttrStatusTest) {
                case VALID:
                    return createMockedPlainAttr(Arrays.asList("validValue1", "validValue2"));
                case EMPTY:
                    return createMockedPlainAttr(Collections.emptyList());
                case INVALID:
                    return createMockedPlainAttr(Arrays.asList("unassociatedAttr1", "validValue1"));
                case NULL:
                    return null;
                default:
                    return Optional.empty();
            }
        });

        // Configura l'algoritmo di cifratura in base al parametro algorithmTest
        if (algorithmTest == Algorithm.NULL) {
            Mockito.when(account.getCipherAlgorithm()).thenReturn(null);
            Mockito.when(account.canDecodeSecrets()).thenReturn(false);
        } else {
            CipherAlgorithm cipherAlgorithm = CipherAlgorithm.valueOf(algorithmTest.name());
            Mockito.when(account.getCipherAlgorithm()).thenReturn(cipherAlgorithm);
            Mockito.when(account.canDecodeSecrets()).thenReturn(cipherAlgorithm.isInvertible());
        }

        Mockito.when(account.getUsername()).thenReturn("testUsername");

        return account;
    }


    // Crea un mock di PlainAttr con i valori specificati.
    private Optional<PlainAttr> createMockedPlainAttr(Collection<String> values) {
        PlainAttr attr = Mockito.mock(PlainAttr.class);
        Mockito.when(attr.getValuesAsStrings()).thenReturn(Collections.singletonList(values));
        return Optional.of(attr);
    }


    // Esegue un singolo test
    private void runTestWithPassword(LinkedAccount account, String password, boolean shouldThrow) {
        try {
            // Configurazione password
            if (algorithmTest != Algorithm.NULL) {
                CipherAlgorithm cipherAlgorithm = CipherAlgorithm.valueOf(algorithmTest.name());
                Mockito.when(account.getPassword()).thenReturn(ENCRYPTOR.encode(password, cipherAlgorithm));
            } else {
                Mockito.when(account.getPassword()).thenReturn(password);
            }

            passwordRule.enforce(account);

            // Se ci aspettavamo un'eccezione ma non viene lanciata, il test fallisce
            if (shouldThrow) {
                Assert.fail("Non è stata lanciata l'eccezione prevista "+exceptionOutputTest.getClass());
            }
        } catch (Exception e) {
            // Se non ci aspettavamo un'eccezione o l'eccezione non è quella prevista, il test fallisce
            if (!shouldThrow)
                Assert.fail("Eccezione inaspettata: " + e.getClass() + " - " + e.getMessage());
            if (exceptionOutputTest == null)
                Assert.fail("Eccezione non settata: " + e.getClass() + " - " + e.getMessage());
            if (exceptionOutputTest != e.getClass()) {
                Assert.fail("Eccezione non corrisponde: " + e.getClass() + " - " + e.getMessage());
            }
        }
    }

}
