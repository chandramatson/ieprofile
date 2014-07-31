package com.ieprofile.common;

import java.util.LinkedList;
import java.util.List;

import com.ieprofile.vo.MessageBean;

public interface EmailRepo {
	
	public List<MessageBean> getMessages();

	public void addMessage(MessageBean message);
	
	public void addAllMessages(LinkedList<MessageBean> messages);

}
