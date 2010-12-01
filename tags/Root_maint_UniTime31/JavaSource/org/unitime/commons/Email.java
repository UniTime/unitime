/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.commons;

/**
 * Sends anonymous emails
 * @author Heston Fernandes
 */
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

public class Email {

	// Constants
	private static final int SMTP_PORT = 25;
	private static final char SMTP_ERROR_CODE1 = '4';
	private static final char SMTP_ERROR_CODE2 = '5';


	/**
	 * Send Email
	 * @param host IP Address or Host name
	 * @param domain Host Domain
	 * @param sender Sender - must be an email address only (no names)
	 * @param replyTo Usually same as sender but can be of the form Name&lt;email&gt;
	 *                 Example: ABC &lt;abc@xyz.com&gt;
	 * @param recipients Recipients (multiple recipients separated by ;)
	 * @param subject Subject
	 * @param maildata Content
	 * @param sessionTrace Stores transcript of conversation with mail server
	 * @throws IOException
	 */
	public void sendMail(	String host,
							String domain,
							String sender,
							String replyTo,
							String recipients,
							String subject,
							String maildata,
							Vector sessionTrace ) throws IOException {

		Socket mailSocket;
		BufferedReader socketIn;
		DataOutputStream socketOut;
		String address;
		StringTokenizer tokenizer;

		mailSocket = new Socket(host, SMTP_PORT);
		socketIn =new BufferedReader(
					new InputStreamReader(mailSocket.getInputStream()));
		socketOut = new DataOutputStream(mailSocket.getOutputStream());

		readReply(socketIn, sessionTrace);

		sendCommand(socketOut, "HELO " + domain, sessionTrace);
		readReply(socketIn, sessionTrace);

		sendCommand(socketOut, "MAIL FROM: " + sender, sessionTrace);
		readReply(socketIn, sessionTrace);

		tokenizer = new StringTokenizer(recipients, ";");

		while (tokenizer.hasMoreElements()) {
			sendCommand( socketOut,
						 "RCPT TO: " + tokenizer.nextToken(),
						 sessionTrace );
			readReply(socketIn, sessionTrace);
		}

		maildata =  "Date: " + (new java.util.Date()).toString() + "\r\n" +
					"To: " + recipients + "\r\n" +
					"From: " + replyTo + "\r\n" +
					"Reply-To: " + replyTo + "\r\n" +
					"Subject: " + subject + "\r\n" +
					"\r\n" +
					maildata + "\r\n";

		sendCommand(socketOut, "DATA", sessionTrace);
		readReply(socketIn, sessionTrace);

		sendCommand(socketOut, maildata + "\n.", sessionTrace);
		readReply(socketIn, sessionTrace);

		sendCommand(socketOut, "QUIT", sessionTrace);
		readReply(socketIn, sessionTrace);
	}

	/**
	 * Sends command to server
	 * @param out Output Stream
	 * @param command Command
	 * @param sessionTrace Stores transcript of conversation with mail server
	 * @throws IOException
	 */
	private void sendCommand(	DataOutputStream out,
								String command,
								Vector sessionTrace) throws IOException {
		out.writeBytes(command + "\r\n");
		sessionTrace.addElement(command + "\r\n");
	}

	/**
	 * Reads server responses to commands
	 * @param reader Object to read response from server
	 * @param sessionTrace Stores transcript of conversation with mail server
	 * @throws IOException
	 */
	private void readReply(BufferedReader reader, Vector sessionTrace)
		throws IOException {

		String reply;
		char statusCode;

		reply = reader.readLine();
		statusCode = reply.charAt(0);
		sessionTrace.addElement(reply + "\r\n");

		if ((statusCode == SMTP_ERROR_CODE1) || (statusCode == SMTP_ERROR_CODE2))
			throw (new IOException("SMTP: " + reply));
	}

}
