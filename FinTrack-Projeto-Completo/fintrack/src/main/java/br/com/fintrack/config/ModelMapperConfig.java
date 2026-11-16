package br.com.fintrack.config;

import br.com.fintrack.dto.TransacaoDTO;
import br.com.fintrack.model.Transacao;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // O cliente nunca define o ID, o proprietário ou a categoria como entidade.
        mapper.typeMap(TransacaoDTO.class, Transacao.class).addMappings(m -> {
            m.skip(Transacao::setId);
            m.skip(Transacao::setUsuario);
            m.skip(Transacao::setCategoria);
        });

        // Record DTO de resposta: conversão explícita registrada no próprio ModelMapper.
        Converter<Transacao, TransacaoDTO> toDto = context -> {
            Transacao t = context.getSource();
            return new TransacaoDTO(t.getId(), t.getDescricao(), t.getValor(), t.getData(),
                    t.getTipo(), t.getCategoria().getId(), t.getCategoria().getNome());
        };
        mapper.createTypeMap(Transacao.class, TransacaoDTO.class).setConverter(toDto);
        return mapper;
    }
}
