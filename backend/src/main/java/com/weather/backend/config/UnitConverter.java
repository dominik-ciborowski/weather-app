package com.weather.backend.config;

import com.weather.backend.model.Unit;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UnitConverter implements Converter<String, Unit> {

    @Override
    public Unit convert(String source) {
        return Unit.from(source);
    }
}
