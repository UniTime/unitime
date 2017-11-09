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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
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

import biweekly.util.org.apache.commons.codec.binary.Base64;

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
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(ApplicationProperty.UrlEncoderSecret.value().toCharArray(), salt, 1024, 128);
		SecretKey key = factory.generateSecret(spec);
		return new SecretKeySpec(key.getEncoded(), "AES");
	}
	
	public static String encode(String text) {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secret());
			//return new BigInteger(cipher.doFinal(text.getBytes())).toString(36);
			return new Base64(-1, new byte[] {}, true).encodeAsString(cipher.doFinal(text.getBytes()));
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
			HashedQueryDAO.getInstance().save(hq);
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
				HashedQueryDAO.getInstance().update(hq);
				return hq.getQueryText();
			} else {
				Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
				cipher.init(Cipher.DECRYPT_MODE, secret());
				try {
					return new String(cipher.doFinal(new Base64(-1, new byte[] {}, true).decode(text)));
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
			}
		} catch (Exception e) {
			throw new GwtRpcException("Decoding failed: " + e.getMessage(), e);
		}
	}
	
	public static void main(String[] args) {
		System.out.println(encode("output=events.csv&type=PERSON&ext=1001&token=1xhp5vo3zfxrpbzjzhtanmcipolx03fv42ohz4xa507x5acydh&user=1001"));
	}

}
