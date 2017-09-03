package projectm.persist.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes = { @Index(name = "docId", columnList = "docId"), //
		@Index(name = "fileId", columnList = "fileId") })
public class Log implements Serializable {

	private static final long serialVersionUID = -6305092032286601076L;
	@Id
	@GeneratedValue
	protected Long id;
	@Column(nullable = false)
	private String docId;
	@Column
	private Long fileId;
	@Column(length = 102400)
	private String raw;
	@Column(name = "logTime")
	private String time;

	public Log() {
		super();
	}

	public Log(String docId, Long fileId, String raw, String time) {
		super();
		this.docId = docId;
		this.fileId = fileId;
		this.raw = raw;
		this.time = time;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
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
}