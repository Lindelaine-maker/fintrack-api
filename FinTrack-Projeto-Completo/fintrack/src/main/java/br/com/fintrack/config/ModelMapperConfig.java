package br.com.fintrack.config;

import br.com.fintrack.dto.TransacaoDTO;
import br.com.fintrack.model.Transacao;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(AccessLevel.PRIVATE);

        // O destino é uma entidade mutável; ID, proprietário e categoria são definidos pelo serviço.
        mapper.typeMap(TransacaoDTO.class, Transacao.class).addMappings(m -> {
            m.skip(Transacao::setId);
            m.skip(Transacao::setUsuario);
            m.skip(Transacao::setCategoria);
        });
        return mapper;
    }
}