/**
 * 
 */
package com.ieprofile.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author chandrasekharpappala
 * 
 */
public class MessageBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String subject;
	private String from;
	private String to;
	private Date dateSent;
	private String content;
	private boolean isNewEmail;
	private int msgId;
	private List<String> attachments;

	public MessageBean(int msgId, String subject, String from, String to,
			Date dateSent, String content, boolean isNew,
			List<String> attachments) {
		this.subject = subject;
		this.from = from;
		this.to = to;
		this.dateSent = dateSent;
		this.content = content;
		this.isNewEmail = isNew;
		this.msgId = msgId;
		this.attachments = attachments;
	}

	public MessageBean() {
		// TODO Auto-generated constructor stub
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public Date getDateSent() {
		return dateSent;
	}

	public void setDateSent(Date dateSent) {
		this.dateSent = dateSent;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public boolean isNewEmail() {
		return isNewEmail;
	}

	public void setNewEmail(boolean aNew) {
		isNewEmail = aNew;
	}

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public List<String> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<String> attachments) {
		this.attachments = new ArrayList<String>(attachments);
	}

}
