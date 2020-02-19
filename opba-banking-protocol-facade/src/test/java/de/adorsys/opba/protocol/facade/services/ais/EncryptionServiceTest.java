package de.adorsys.opba.protocol.facade.services.ais;

import de.adorsys.opba.protocol.api.dto.KeyDto;
import de.adorsys.opba.protocol.api.dto.KeyWithParamsDto;
import de.adorsys.opba.protocol.api.services.EncryptionService;
import de.adorsys.opba.protocol.api.services.SecretKeyOperations;
import de.adorsys.opba.protocol.facade.config.ApplicationTest;
import de.adorsys.opba.protocol.facade.services.FacadeEncryptionServiceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = ApplicationTest.class)
public class EncryptionServiceTest {

    @Autowired
    private SecretKeyOperations secretKeyOperations;

    @Autowired
    private FacadeEncryptionServiceFactory facadeEncryptionServiceFactory;

    @Test
    void encryptDecryptPasswordTest() {
        String password = "QwE!@#";

        KeyWithParamsDto keyWithParamsDto = secretKeyOperations.generateKey(password);
        byte[] encryptedSecretKey = secretKeyOperations.encrypt(keyWithParamsDto.getKey());

        byte[] decryptedSecretKey = secretKeyOperations.decrypt(encryptedSecretKey);

        KeyWithParamsDto reCreatedFromPassword = secretKeyOperations.generateKey(
                password,
                keyWithParamsDto.getAlgorithm(),
                keyWithParamsDto.getSalt(),
                keyWithParamsDto.getIterationCount());
        assertThat(decryptedSecretKey).isEqualTo(reCreatedFromPassword.getKey());
    }

    @Test
    void encryptDecryptDataTest() {
        String password = "password";
        String data = "data to encrypt";

        KeyDto key = secretKeyOperations.generateKey(password);
        EncryptionService encryptionService = facadeEncryptionServiceFactory.provideEncryptionService(key.getKey());
        byte[] encryptedData = encryptionService.encrypt(data.getBytes());

        byte[] decryptedData = encryptionService.decrypt(encryptedData);

        assertThat(decryptedData).isEqualTo(data.getBytes());
    }
}
