/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.events;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.model.HashedQuery;
import org.unitime.timetable.model.dao.HashedQueryDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(EncodeQueryRpcRequest.class)
public class QueryEncoderBackend implements GwtRpcImplementation<EncodeQueryRpcRequest, EncodeQueryRpcResponse> {
	
	@Override
	public EncodeQueryRpcResponse execute(EncodeQueryRpcRequest request, SessionContext context) {
		String query = request.getQuery() + 
				(context.getUser() == null ? "" : "&user=" + context.getUser().getExternalUserId() +
				(context.getUser() == null || context.getUser().getCurrentAuthority() == null ? "" : "&role=" + context.getUser().getCurrentAuthority().getRole()));
		if (request.isHash() && ApplicationProperty.UrlEncoderHashQueryWhenAsked.isTrue()) {
			return new EncodeQueryRpcResponse(encode(query), hash(query));
		} else {
			return new EncodeQueryRpcResponse(encode(query));
		}
	}
	
	private static SecretKey secret() throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte salt[] = new byte[] { (byte)0x33, (byte)0x7b, (byte)0x09, (byte)0x0e, (byte)0xcf, (byte)0x5a, (byte)0x58, (byte)0xd9 };
		SecretKeyFactory factory = SecretKeyFactory.getInstance(ApplicationProperty.UrlEncoderSecretAlgorithm.value());
		KeySpec spec = new PBEKeySpec(ApplicationProperty.UrlEncoderSecret.value().toCharArray(), salt,
				ApplicationProperty.UrlEncoderSecretIterationCount.intValue(),
				ApplicationProperty.UrlEncoderSecretKeyLength.intValue());
		SecretKey key = factory.generateSecret(spec);
		return new SecretKeySpec(key.getEncoded(), "AES");
	}
	
	public static String encode(String text) {
		try {
			if (ApplicationProperty.UrlEncoderCipher.value().contains("/ECB/")) { // no IV needed
				Cipher cipher = Cipher.getInstance(ApplicationProperty.UrlEncoderCipher.value());
				cipher.init(Cipher.ENCRYPT_MODE, secret());
				return Base64.getUrlEncoder().withoutPadding().encodeToString(cipher.doFinal(text.getBytes()));
			} else {
				byte[] iv = new byte[16];
				SecureRandom random = new SecureRandom();
				random.nextBytes(iv);
				IvParameterSpec ivSpec = new IvParameterSpec(iv);
				Cipher cipher = Cipher.getInstance(ApplicationProperty.UrlEncoderCipher.value());
				cipher.init(Cipher.ENCRYPT_MODE, secret(), ivSpec);
				byte[] encrypted = cipher.doFinal(text.getBytes());
				byte[] encryptedIVAndText = new byte[iv.length + encrypted.length];
		        System.arraycopy(iv, 0, encryptedIVAndText, 0, iv.length);
		        System.arraycopy(encrypted, 0, encryptedIVAndText, iv.length, encrypted.length);
				return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedIVAndText);
			}
		} catch (Exception e) {
			throw new GwtRpcException("Encoding failed: " + e.getMessage(), e);
		}
	}
	
	public static String hash(String text) {
		try {
			if (text.length() > 2048) return null;
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			Date ts = new Date();
			String hash = new BigInteger(md5.digest(text.getBytes())).toString(36) + Long.toString(ts.getTime(), 36);
			HashedQuery hq = new HashedQuery();
			hq.setQueryHash(hash);
			hq.setQueryText(text);
			hq.setCreated(ts);
			hq.setNbrUsed(0l);
			hq.setLastUsed(ts);
			HashedQueryDAO.getInstance().getSession().persist(hq);
			HashedQueryDAO.getInstance().getSession().flush();
			return hash;
		} catch (Exception e) {
			throw new GwtRpcException("Hashing failed: " + e.getMessage(), e);
		}
	}
	
	public static String decode(String text) {
		return decode(text, false);
	}
	
	public static String decode(String text, boolean hash) {
		try {
			if (text == null || text.isEmpty()) return null;
			if (hash) {
				HashedQuery hq = HashedQueryDAO.getInstance().get(text);
				if (hq == null)
					throw new GwtRpcException("The query hash " + text + " no longer exists. Please create a new URL.");
				hq.setNbrUsed(1 + hq.getNbrUsed());
				hq.setLastUsed(new Date());
				HashedQueryDAO.getInstance().getSession().merge(hq);
				HashedQueryDAO.getInstance().getSession().flush();
				return hq.getQueryText();
			} else if (ApplicationProperty.UrlEncoderCipher.value().contains("/ECB/")) { // no IV needed
				Cipher cipher = Cipher.getInstance(ApplicationProperty.UrlEncoderCipher.value());
				cipher.init(Cipher.DECRYPT_MODE, secret());
				try {
					return new String(cipher.doFinal(Base64.getUrlDecoder().decode(text)));
				} catch (Exception e) {
					try {
						return new String(cipher.doFinal(new BigInteger(text, 36).toByteArray()));
					} catch (IllegalBlockSizeException x) {
						byte[] bytes = new BigInteger(text, 36).toByteArray();
						byte[] fixed = new byte[(1 + bytes.length / cipher.getBlockSize()) * cipher.getBlockSize()];
						for (int i = 0; i < fixed.length - bytes.length; i++) fixed[i] = -1;
						System.arraycopy(bytes, 0, fixed, fixed.length - bytes.length, bytes.length);
						return new String(cipher.doFinal(fixed));
					}
				}
			} else {
				byte[] iv = new byte[16];
				byte[] encryptedIvTextBytes = Base64.getUrlDecoder().decode(text);
				System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);
		        IvParameterSpec ivSpec = new IvParameterSpec(iv);
				Cipher cipher = Cipher.getInstance(ApplicationProperty.UrlEncoderCipher.value());
				cipher.init(Cipher.DECRYPT_MODE, secret(), ivSpec);
				int encryptedSize = encryptedIvTextBytes.length - iv.length;
		        byte[] encryptedBytes = new byte[encryptedSize];
		        System.arraycopy(encryptedIvTextBytes, iv.length, encryptedBytes, 0, encryptedSize);
				return new String(cipher.doFinal(encryptedBytes));
			}
		} catch (Exception e) {
			throw new GwtRpcException("Decoding failed: " + e.getMessage(), e);
		}
	}
	
	public static void main(String[] args) {
		System.out.println(encode("output=events.csv&type=PERSON&ext=1001&token=1xhp5vo3zfxrpbzjzhtanmcipolx03fv42ohz4xa507x5acydh&user=1001"));
	}

}
