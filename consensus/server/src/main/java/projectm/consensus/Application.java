package projectm.consensus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableCaching
public class Application {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
		context.getBean(ConsensusServer.class).startUp();
	}
}
