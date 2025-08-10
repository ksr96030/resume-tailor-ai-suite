package com.resumetailor.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "AI Resume Tailoring API", version = "1.0"))
public class OpenAPIConfig {}
