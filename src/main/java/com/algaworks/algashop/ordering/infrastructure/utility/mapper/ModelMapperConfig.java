package com.algaworks.algashop.ordering.infrastructure.utility.mapper;

import com.algaworks.algashop.ordering.application.utility.Mapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NamingConventions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public Mapper mapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setSourceNamingConvention(NamingConventions.NONE)
                .setDestinationNamingConvention(NamingConventions.NONE)
                .setMatchingStrategy(MatchingStrategies.STRICT);
        return new Mapper() {
            @Override
            public <D> D convert(Object source, Class<D> destinationType) {
                return modelMapper.map(source, destinationType);
            }
        };

    }

}
