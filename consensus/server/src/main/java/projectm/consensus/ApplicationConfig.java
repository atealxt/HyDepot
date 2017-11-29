package projectm.consensus;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@EnableAutoConfiguration
public class ApplicationConfig {

	@Bean
	public Docket apis() {
		ApiInfo apiInfo = new ApiInfoBuilder()//
				.title("Project M consensus APIs")//
				.description("Project consensus APIs")//
				.version("1.0")//
				.build();
		return new Docket(DocumentationType.SWAGGER_2)//
				.groupName("project-m-consensus")//
				.apiInfo(apiInfo)//
				.select()//
				.paths(PathSelectors.ant("/api/**"))//
				.build();
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleGeneral(Exception exc) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}
}
