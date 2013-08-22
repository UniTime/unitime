/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.authenticate.jaas;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.security.auth.module.Krb5LoginModule;

public class KerberosAuthenticateModule extends Krb5LoginModule {
	private String iRealm = null;

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
		super.initialize(subject, new AddRealmCallbackHandler(callbackHandler), sharedState, options);
		iRealm = (String)options.get("realm");
	}

	private class AddRealmCallbackHandler implements CallbackHandler {
		private CallbackHandler iHandler;

		private AddRealmCallbackHandler(CallbackHandler handler) {
			iHandler = handler;
		}

		@Override
		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
			iHandler.handle(callbacks);
			if (iRealm != null)
				for (Callback callback : callbacks) {
					if (callback instanceof NameCallback) {
						NameCallback nc = (NameCallback) callback;
						if (nc.getName() != null && !nc.getName().contains("@"))
							nc.setName(nc.getName() + "@" + iRealm);
					}
				}
		}
	}

}
