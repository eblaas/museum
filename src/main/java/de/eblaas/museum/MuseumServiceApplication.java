package de.eblaas.museum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication()
public class MuseumServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuseumServiceApplication.class, args);
    }

    @Configuration
    @EnableSwagger2
    public class Config {

        @Bean
        public Docket museumApi() {
            return new Docket(DocumentationType.SWAGGER_2)
                    .apiInfo(new ApiInfoBuilder()
                            .title("MET object service API")
                            .description("Provides a API for checking if a specific MET object fits your boundaries")
                            .version("1.0")
                            .build())
                    .select()
                    .apis(RequestHandlerSelectors.basePackage("de.eblaas.museum"))
                    .build();
        }

        @Bean
        public Resource importDataSource(@Value("${import.filepath}") String importFilePath) {
            return new FileSystemResource(importFilePath);
        }

    }
}
