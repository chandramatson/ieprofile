package com.ieprofile.common.messageconverter;

import com.ieprofile.vo.MessageBean;

public class SSEvent {

	private String id;

	private MessageBean data;

	private Integer retry;

	private String event;

	private String comment;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public MessageBean getData() {
		return data;
	}

	public void setData(MessageBean data) {
		this.data = data;
	}

	public Integer getRetry() {
		return retry;
	}

	public void setRetry(Integer retry) {
		this.retry = retry;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}