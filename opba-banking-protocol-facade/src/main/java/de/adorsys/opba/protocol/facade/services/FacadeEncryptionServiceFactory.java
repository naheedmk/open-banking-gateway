package de.adorsys.opba.protocol.facade.services;

import de.adorsys.opba.protocol.api.services.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class FacadeEncryptionServiceFactory {

    private final Environment env;

    public EncryptionService provideEncryptionService(byte[] key) {
        if (Arrays.asList(env.getActiveProfiles()).contains("no-enc")) {
            return new NoEncryptionServiceImpl();
        }
        return new EncryptionServiceImpl(key);
    }
}
