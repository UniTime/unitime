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
package org.unitime.timetable.authenticate.jaas;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.security.auth.module.Krb5LoginModule;

/**
 * @author Tomas Muller
 */
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
