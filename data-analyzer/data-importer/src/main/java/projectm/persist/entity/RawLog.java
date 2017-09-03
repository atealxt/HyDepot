package projectm.persist.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class RawLog implements Serializable {

	private static final long serialVersionUID = -6305092032286601076L;
	@Id
	@GeneratedValue
	protected Long id;
	@Column(length = 102400)
	private String raw;
	@Column(name = "logTime")
	private String time;
	@Column
	private String host;
	@Column(name = "logIndex")
	private String index;
	@Column
	private String lineCount;
	@Column
	private String source;
	@Column
	private String sourceType;
	@Column
	private String server;

	public RawLog() {
		super();
	}

	public RawLog(String raw, String time, String host, String index, String lineCount, String source,
			String sourceType, String server) {
		super();
		this.raw = raw;
		this.time = time;
		this.host = host;
		this.index = index;
		this.lineCount = lineCount;
		this.source = source;
		this.sourceType = sourceType;
		this.server = server;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRaw() {
		return raw;
	}

	public void setRaw(String raw) {
		this.raw = raw;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getLineCount() {
		return lineCount;
	}

	public void setLineCount(String lineCount) {
		this.lineCount = lineCount;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
}
