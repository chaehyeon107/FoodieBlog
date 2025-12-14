package com.foodieblog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        final String schemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("FoodieBlog API")
                        .description("웹서비스설계 과제2 - 맛집 블로그 API")
                        .version("v1"))
                // 기본적으로 모든 API에 bearerAuth가 걸린 것처럼 보이게
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(schemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
