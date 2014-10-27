/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.events;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
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
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(EncodeQueryRpcRequest.class)
public class QueryEncoderBackend implements GwtRpcImplementation<EncodeQueryRpcRequest, EncodeQueryRpcResponse> {
	
	@Override
	public EncodeQueryRpcResponse execute(EncodeQueryRpcRequest request, SessionContext context) {
		return new EncodeQueryRpcResponse(encode(request.getQuery() + 
				(context.getUser() == null ? "" : "&user=" + context.getUser().getExternalUserId() +
				(context.getUser() == null || context.getUser().getCurrentAuthority() == null ? "" : "&role=" + context.getUser().getCurrentAuthority().getRole()))));
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
			return new BigInteger(cipher.doFinal(text.getBytes())).toString(36);
		} catch (Exception e) {
			throw new GwtRpcException("Encoding failed: " + e.getMessage(), e);
		}
	}
	
	public static String decode(String text) {
		try {
			if (text == null || text.isEmpty()) return null;
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secret());
			return new String(cipher.doFinal(new BigInteger(text, 36).toByteArray()));
		} catch (Exception e) {
			throw new GwtRpcException("Decoding failed: " + e.getMessage(), e);
		}
	}	

}
