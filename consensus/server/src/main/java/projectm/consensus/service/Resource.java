package projectm.consensus.service;

import java.util.concurrent.atomic.AtomicLong;

public class Resource {

	private AtomicLong id = new AtomicLong();
	private String key;
	private String value;

	public Resource() {
		super();
	}

	public Resource(String key) {
		super();
		this.key = key;
	}

	public Resource(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public AtomicLong getId() {
		return id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Resource [id=" + id + ", key=" + key + ", value=" + value + "]";
	}
}
