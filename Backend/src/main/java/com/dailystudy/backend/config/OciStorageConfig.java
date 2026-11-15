package com.dailystudy.backend.config;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;

import java.io.IOException;

@Configuration(proxyBeanMethods = false)
public class OciStorageConfig {

    @Bean
    public ObjectStorageClient objectStorageClient(OciStorageProperties props) throws IOException {
        ConfigFileReader.ConfigFile configFile = ConfigFileReader.parse(props.configFile(), props.profile());

        AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);

        return ObjectStorageClient.builder().build(provider);
    }
}
