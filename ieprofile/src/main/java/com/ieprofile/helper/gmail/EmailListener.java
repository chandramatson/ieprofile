package com.ieprofile.helper.gmail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ieprofile.common.EmailRepo;
import com.ieprofile.helper.FileOperator;
import com.ieprofile.vo.MessageBean;
import com.sun.mail.imap.IMAPFolder;

/**
 * 
 * @author cpappala
 * 
 */
@Service
public class EmailListener {

	@Autowired
	EmailRepo emailRepo;

	/**
	 * Message listener that will process new emails found in the IMAP server.
	 */
	private MessageCountAdapter messageListener;
	public Folder folder;
	public boolean started = false;

	/**
	 * Opens a connection to the IMAP server and listen for new messages.
	 */
	public void start() {
		// Check that the listner service is not running
		if (started) {
			return;
		}
		Thread thread = new Thread("Email Listener Thread") {
			public void run() {
				// Open the email folder and keep it
				// folder = openFolder(getHost(), getPort(), isSSLEnabled(),
				// getUser(), getPassword(), getFolder());
				System.out.println("Thead started : folder : " + folder);
				if (folder != null) {
					// Listen for new email messages until #stop is requested
					listenMessages();
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
		started = true;
	}

	/**
	 * Closes the active connection to the IMAP server.
	 */
	public void stop() {
		closeFolder(folder, messageListener);
		started = false;
		folder = null;
		messageListener = null;
	}

	private void listenMessages() {
		try {
			// Add messageCountListener to listen for new messages
			messageListener = new MessageCountAdapter() {
				public void messagesAdded(MessageCountEvent ev) {
					Message[] msgs = ev.getMessages();
					// Send new messages to specified users
					ArrayList<String> attachments = new ArrayList<String>();
					try {
						emailRepo
								.addAllMessages(sendMessage(msgs, attachments));
					} catch (Exception e) {
						System.out
								.println("Error while sending new email message"
										+ e);
					}
				}

			};
			folder.addMessageCountListener(messageListener);

			// Check mail once in "freq" MILLIseconds
			int freq = 5000;
			boolean supportsIdle = false;
			try {
				if (folder instanceof IMAPFolder) {
					IMAPFolder f = (IMAPFolder) folder;
					f.idle();
					supportsIdle = true;
				}
			} catch (FolderClosedException fex) {
				throw fex;
			} catch (MessagingException mex) {
				supportsIdle = false;
			}
			for (;;) {
				try {
					if (supportsIdle && folder instanceof IMAPFolder) {
						IMAPFolder f = (IMAPFolder) folder;
						f.idle();
						System.out.println("IDLE done");
					} else {
						Thread.sleep(freq); // sleep for freq milliseconds

						// This is to force the IMAP server to send us
						// EXISTS notifications.
						folder.getMessageCount();
					}
				} catch (FolderClosedException fex) {
					throw fex;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Error listening new email messages" + ex);
		}
	}

	private void sendMessage(Message message) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("New email has been received\n");
		// FROM
		sb.append("From: ");
		for (Address address : message.getFrom()) {
			sb.append(address.toString()).append(" ");
		}
		sb.append("\n");
		// DATE
		Date date = message.getSentDate();
		sb.append("Received: ")
				.append(date != null ? date.toString() : "UNKNOWN")
				.append("\n");
		// SUBJECT
		sb.append("Subject: ").append(message.getSubject()).append("\n");
		// Apend body
		appendMessagePart(message, sb);

	}

	private void appendMessagePart(Part part, StringBuilder sb)
			throws Exception {
		/*
		 * Using isMimeType to determine the content type avoids fetching the
		 * actual content data until we need it.
		 */
		if (part.isMimeType("text/plain")) {
			// This is plain text"
			sb.append((String) part.getContent()).append("\n");
		} else if (part.isMimeType("multipart/*")) {
			// This is a Multipart
			Multipart mp = (Multipart) part.getContent();
			int count = mp.getCount();
			for (int i = 0; i < count; i++) {
				appendMessagePart(mp.getBodyPart(i), sb);
			}
		} else if (part.isMimeType("message/rfc822")) {
			// This is a Nested Message
			appendMessagePart((Part) part.getContent(), sb);
		} else {
			/*
			 * If we actually want to see the data, and it's not a MIME type we
			 * know, fetch it and check its Java type.
			 */
			/*
			 * Object o = part.getContent(); if (o instanceof String) { // This
			 * is a string System.out.println((String) o); } else if (o
			 * instanceof InputStream) { // This is just an input stream
			 * InputStream is = (InputStream) o; int c; while ((c = is.read())
			 * != -1) { System.out.write(c); } } else { // This is an unknown
			 * type System.out.println(o.toString()); }
			 */
		}
	}

	public static LinkedList<MessageBean> sendMessage(Message[] messages,
			List<String> attachments) throws MessagingException, IOException {
		LinkedList<MessageBean> listMessages = new LinkedList<MessageBean>();
		System.out.println("Messages **** " + messages.length);
		int length = messages.length > 10 ? messages.length - 10
				: messages.length - 1;
		for (int j = messages.length - 1; j >= length; j--) {

			attachments.clear();
			if (messages[j].isMimeType("text/plain")) {
				MessageBean message = new MessageBean(
						messages[j].getMessageNumber(),
						MimeUtility.decodeText(messages[j].getSubject()),
						messages[j].getFrom()[0].toString(),
						InternetAddress.toString(messages[j]
								.getRecipients(Message.RecipientType.TO)),
						messages[j].getSentDate(),
						(String) messages[j].getContent(), length == 0 ? true
								: false, null);
				listMessages.add(message);
			} else if (messages[j].isMimeType("multipart/*")) {
				Multipart mp = (Multipart) messages[j].getContent();
				MessageBean message = null;
				for (int i = 0; i < mp.getCount(); i++) {
					Part part = mp.getBodyPart(i);

					if ((part.getFileName() == null || part.getFileName() == "")
							&& part.isMimeType("text/plain")) {
						message = new MessageBean(
								messages[j].getMessageNumber(),
								messages[j].getSubject(),
								messages[j].getFrom()[0].toString(), null,
								messages[j].getSentDate(),
								(String) part.getContent(), length == 0 ? true
										: false, null);
					} else if (part.getFileName() != null
							|| part.getFileName() != "") {
						if (part.getDisposition() != null
								&& part.getDisposition().equalsIgnoreCase(
										Part.ATTACHMENT)) {
							attachments.add(FileOperator.saveFile(
									MimeUtility.decodeText(part.getFileName()),
									part.getInputStream()));
							if (message != null) {
								message.setAttachments(attachments);
							}
						}
					}
				}
				listMessages.add(message);
			}
		}
		return listMessages;
	}

	private static void closeFolder(Folder folder,
			MessageCountAdapter messageListener) {
		if (folder != null) {
			if (messageListener != null) {
				folder.removeMessageCountListener(messageListener);
			}
			try {
				folder.close(false);
			} catch (MessagingException e) {
				System.out.println("Error closing folder" + e);
			}
		}
	}

}
