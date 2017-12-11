package projectm.consensus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;

import projectm.consensus.service.DefaultConsensusServer;
import projectm.consensus.service.DefaultConsensusServerL2;

@SpringBootApplication
@EnableCaching
public class Application {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
		context.getBean(DefaultConsensusServer.class).startUp();
		context.getBean(DefaultConsensusServerL2.class).startUp();
	}
}
