package projectm.controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.DeleteResponse;
import com.coreos.jetcd.kv.GetResponse;

import projectm.ApplicationConfig;

@RestController
@RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/html")
public class HomeController {

	@Resource
	private ApplicationConfig applicationConfig;

	@RequestMapping
	public String home() {
		
		try {
			testEtcd();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "Project M is Online!";
	}

	private void testEtcd() throws InterruptedException, ExecutionException {

		// create client
		Client client = Client.builder().endpoints("http://localhost:2379").build();
		KV kvClient = client.getKVClient();

		ByteSequence key = ByteSequence.fromString("test_key");
		ByteSequence value = ByteSequence.fromString("test_value");

		// put the key-value
		kvClient.put(key, value).get();
		// get the CompletableFuture
		CompletableFuture<GetResponse> getFuture = kvClient.get(key);
		// get the value from CompletableFuture
		GetResponse response = getFuture.get();
		// delete the key
		DeleteResponse deleteRangeResponse = kvClient.delete(key).get();
	}
}
