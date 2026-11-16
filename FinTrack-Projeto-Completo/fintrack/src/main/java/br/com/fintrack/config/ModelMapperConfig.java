package br.com.fintrack.config;

import br.com.fintrack.dto.TransacaoDTO;
import br.com.fintrack.model.Transacao;
import org.modelmapper.Converter;
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

        // O destino Transacao é mutável; campos de controle são definidos pelo servidor.
        mapper.typeMap(TransacaoDTO.class, Transacao.class).addMappings(m -> {
            m.skip(Transacao::setId);
            m.skip(Transacao::setUsuario);
            m.skip(Transacao::setCategoria);
        });

        // Records são finais e imutáveis: converte para o construtor canônico sem tentar instanciá-los.
        Converter<Transacao, TransacaoDTO> toDto = context -> {
            Transacao t = context.getSource();
            return new TransacaoDTO(t.getId(), t.getDescricao(), t.getValor(), t.getData(),
                    t.getTipo(), t.getCategoria().getId(), t.getCategoria().getNome());
        };
        mapper.addConverter(toDto);
        return mapper;
    }
}