package com.dat.backend.movied.auth.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
@Data
@AllArgsConstructor
public class VaultProperties {
    private String fe_url;
}
