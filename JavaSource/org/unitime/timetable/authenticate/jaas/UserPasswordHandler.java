/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.authenticate.jaas;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Handler for username / password
 * @author Heston Fernandes
 */
public class UserPasswordHandler implements CallbackHandler {

	private String username;
	private String password;
	
	/**
	 * Initialize handler
	 * @param username
	 * @param password
	 */
	public UserPasswordHandler (String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	
	/* (non-Javadoc)
	 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
	 */
	public void handle(Callback[] callbacks) 
		throws IOException,	UnsupportedCallbackException {

		for(int i=0;i<callbacks.length;i++){
            Callback c = callbacks[i];

            if(c instanceof NameCallback){
                NameCallback nc = (NameCallback)c;
                nc.setName(username);
            } 
            else if (c instanceof PasswordCallback){
                PasswordCallback pc = (PasswordCallback)c;
                pc.setPassword(password.toCharArray());
            } 
            else 
            	throw new UnsupportedCallbackException(
            			c,c.getClass().getName()+" callback not supported");
        }
	}
}
