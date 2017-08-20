package projectm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import projectm.service.storage.StorageException;
import projectm.service.storage.StorageService;
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

	@Autowired
	@Qualifier("S3StorageService")
	@Lazy
	private StorageService s3;
	@Autowired
	@Qualifier("OSSStorageService")
	@Lazy
	private StorageService oss;
	@Autowired
	@Qualifier("GCSStorageService")
	@Lazy
	private StorageService gcs;
	@Autowired
	@Qualifier("PrivateStorageService")
	@Lazy
	private StorageService priv;

	@Bean
	public List<StorageService> storages() {
		return Arrays.asList(s3, oss, gcs, priv);
	}

	@Bean
	public StorageService primaryStorage() {
		return priv; // for debug
	}

	@Bean
	public CacheManager getCacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		List<Cache> caches = new ArrayList<>();
		caches.add(new ConcurrentMapCache(Application.CACHE_GENERAL));
		cacheManager.setCaches(caches);
		return cacheManager;
	}

	@Bean
	public Docket apis() {
		ApiInfo apiInfo = new ApiInfoBuilder()//
				.title("Project M APIs")//
				.description("Project M APIs")//
				.version("1.0")//
				.build();
		return new Docket(DocumentationType.SWAGGER_2)//
				.groupName("project-m-apis")//
				.apiInfo(apiInfo)//
				.select()//
				.paths(PathSelectors.ant("/api/**"))//
				.build();
	}

	@ExceptionHandler(StorageException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageException exc) {
		return ResponseEntity.notFound().build();
	}

	@Bean
	public AtomixReplica getLockService() {
		AtomixReplica replica = AtomixReplica.builder(new Address("localhost", 8700)).build();
		replica.bootstrap().join();
		return replica;
	}
}
