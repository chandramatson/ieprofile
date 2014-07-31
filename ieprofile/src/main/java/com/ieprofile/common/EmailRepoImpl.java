/**
 * 
 */
package com.ieprofile.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.ieprofile.vo.MessageBean;

/**
 * @author cpappala
 *
 */

@Repository
public class EmailRepoImpl implements EmailRepo {

	private LinkedList<MessageBean> messages = new LinkedList<MessageBean>();
	private List<Integer> messageIds = new ArrayList<Integer>();

	@Override
	public LinkedList<MessageBean> getMessages() {
		if (this.messages.isEmpty()) {
			return new LinkedList<MessageBean>();
		}
		return this.messages;
	}

	@Override
	public void addMessage(MessageBean message) {
		this.messages.add(message);
	}

	@Override
	public void addAllMessages(LinkedList<MessageBean> messages) {
		for (MessageBean bean : messages) {
			if (bean != null) {
				System.out.println("Subject : " + bean.getSubject());
				System.out.println("isnew : " + bean.isNewEmail());
				System.out.println("messageId : " + bean.getMsgId());
				System.out
						.println("contains : " + this.messageIds.contains(bean.getMsgId()));
				if (!this.messageIds.contains(bean.getMsgId())) {
					this.messages.addLast(bean);
					messageIds.add(bean.getMsgId());
				}
			}
		}
	}
}
