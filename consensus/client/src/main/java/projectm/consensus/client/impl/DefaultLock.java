package projectm.consensus.client.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.databind.ObjectMapper;

import projectm.consensus.client.Lock;
import projectm.consensus.client.LockException;

public class DefaultLock implements Lock {

	private Resource resource;
	private DefaultLockService lockService;

	public DefaultLock(DefaultLockService lockService, String key) {
		super();
		this.lockService = lockService;
		this.resource = new Resource(key, "lock");
	}

	@Override
	public void lock() throws LockException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		URI uri = null;
		try {
			uri = new URIBuilder().setScheme("http").setHost(lockService.getLeader().getIp())//
					.setPort(lockService.getLeader().getPort()).setPath("/api/consensus/resource")//
					.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		HttpPost httpPost = new HttpPost(uri);
		List<NameValuePair> parameters = new ArrayList<>();
		parameters.add(new BasicNameValuePair("key", resource.getKey()));
		parameters.add(new BasicNameValuePair("value", resource.getValue()));
		parameters.add(new BasicNameValuePair("leader", "true"));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(parameters));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try (CloseableHttpResponse resp = httpclient.execute(httpPost)) {
			HttpEntity entity = resp.getEntity();
			String body = IOUtils.toString(entity.getContent());
			ObjectMapper mapper = new ObjectMapper();
			Resource result = mapper.readValue(body, Resource.class);
			this.resource = result;
		} catch (Exception e) {
			throw new LockException(e);
		}
	}

	@Override
	public void unlock() throws LockException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		URI uri = null;
		try {
			uri = new URIBuilder().setScheme("http").setHost(lockService.getLeader().getIp())//
					.setPort(lockService.getLeader().getPort()).setPath("/api/consensus/resource")//
					.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		HttpPost httpPost = new HttpPost(uri);
		List<NameValuePair> parameters = new ArrayList<>();
		parameters.add(new BasicNameValuePair("key", resource.getKey()));
		parameters.add(new BasicNameValuePair("id", String.valueOf(resource.getId().get())));
		parameters.add(new BasicNameValuePair("delete", "true"));
		parameters.add(new BasicNameValuePair("leader", "true"));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(parameters));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try (CloseableHttpResponse resp = httpclient.execute(httpPost)) {
			HttpEntity entity = resp.getEntity();
			String body = IOUtils.toString(entity.getContent());
			ObjectMapper mapper = new ObjectMapper();
			Resource result = mapper.readValue(body, Resource.class);
			this.resource = result;
		} catch (Exception e) {
			throw new LockException(e);
		}
	}

	@Override
	public String toString() {
		return "DefaultLock [resource=" + resource + "]";
	}
}
