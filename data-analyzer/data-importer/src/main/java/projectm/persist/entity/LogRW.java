package projectm.persist.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes = { @Index(name = "docId", columnList = "docId") })
public class LogRW implements Serializable {

	private static final long serialVersionUID = 120668892661506024L;
	@Id
	@GeneratedValue
	protected Long id;
	@Column(nullable = false)
	private String docId;
	@Column(nullable = false)
	private Integer rwDays;
	@Column(length = 1024)
	private String rwDetail;

	public LogRW() {
		super();
	}

	public LogRW(String docId, Integer rwDays, String rwDetail) {
		super();
		this.docId = docId;
		this.rwDays = rwDays;
		this.rwDetail = rwDetail;
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
}