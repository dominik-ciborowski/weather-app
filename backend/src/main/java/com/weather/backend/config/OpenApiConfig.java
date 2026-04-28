package com.weather.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI weatherApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Weather API")
                .description("Weather service backed by OpenWeather geocoding and One Call APIs.")
                .version("v1")
                .contact(new Contact().name("Weather App"))
                .license(new License().name("Internal use")));
    }
}
