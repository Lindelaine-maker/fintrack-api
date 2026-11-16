package br.com.fintrack.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    ModelMapper modelMapper() {
        // Mantém o mapeamento simples entre campos escalares. O DTO de resposta é
        // um record imutável e é construído explicitamente no serviço.
        return new ModelMapper();
    }
}
