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

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.unitime.timetable.ApplicationProperties;

/**
 * @author Tomas Muller
 */
@Deprecated
public class LdapAuthenticateModule extends AuthenticateModule {
	private static Logger sLog = Logger.getLogger(LdapAuthenticateModule.class);
	private String iExternalUid;

	/**
	 * Abort authentication (when overall authentication fails)
	 */
	public boolean abort() throws LoginException {
		if (!isAuthSucceeded())
			return false;
		if (isAuthSucceeded() && !isCommitSucceeded())
			reset();
		else
			logout();
		return true;
	}

	/**
	 * Commit phase of login
	 */
	public boolean commit() throws LoginException {
		if (isAuthSucceeded()) { // Check if authentication succeeded

			// External UID must exist in order to get manager info
			if (iExternalUid == null || iExternalUid.trim().length() == 0)
				throw new LoginException("External UID not found");

			getSubject().getPrincipals().add(new AuthenticatedUser(getUser(), iExternalUid));

			setCommitSucceeded(true);
			return true;
		} else { // Authentication failed - do not commit
			reset();
			return false;
		}
	}

	/**
	 * Initialize
	 */
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map sharedState, Map options) {

		super.initialize(subject, callbackHandler, sharedState, options);
		iExternalUid = null;
	}

	/**
	 * Authenticate the user
	 */
	public boolean login() throws LoginException {
		// Skip this module when LDAP provider is not set
		if (ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.provider") == null)
			return false;

		sLog.debug("Performing ldap authentication ... ");

		// Get callback parameters
		if (getCallbackHandler() == null)
			throw new LoginException("Error: no CallbackHandler available ");

		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("User Name: ");
		callbacks[1] = new PasswordCallback("Password: ", true);

		try {
			getCallbackHandler().handle(callbacks);
			String n = ((NameCallback) callbacks[0]).getName();
			String p = String.valueOf(((PasswordCallback) callbacks[1])
					.getPassword());

			HashMap userProps = new HashMap();
			userProps.put("username", n);
			userProps.put("password", p);

			if (doAuthenticate(userProps))
				return true;

			// Authentication failed
			sLog.debug("Ldap authentication failed ... ");
			setAuthSucceeded(false);
			return false;
		} catch (Exception ex) {
			sLog.debug("Ldap authentication failed ... " + ex.getMessage(), ex);
			setAuthSucceeded(false);
			return false;
		}
	}

	/**
	 * Logs the user out
	 */
	public boolean logout() throws LoginException {
		reset();
		return true;
	}

	/**
	 * Resets user attributes and status flags
	 */
	public void reset() {
		iExternalUid = null;
		super.reset();
	}

	private static Hashtable<String, String> getEnv() {
		Hashtable<String, String> env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.ctxFactory",
						"com.sun.jndi.ldap.LdapCtxFactory"));
		env.put(Context.PROVIDER_URL, ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.provider"));
		env.put(Context.REFERRAL, ApplicationProperties.getProperty(
				"tmtbl.authenticate.ldap.referral", "ignore"));
		if (ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.version") != null)
			env.put("java.naming.ldap.version", ApplicationProperties
					.getProperty("tmtbl.authenticate.ldap.version"));
		env.put(Context.SECURITY_AUTHENTICATION, ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.security", "simple"));
		if (ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.socketFactory") != null)
			env.put("java.naming.ldap.factory.socket", ApplicationProperties
					.getProperty("tmtbl.authenticate.ldap.socketFactory"));
		if (ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.ssl.keyStore") != null)
			System.setProperty(
					"javax.net.ssl.keyStore",
					ApplicationProperties.getProperty(
							"tmtbl.authenticate.ldap.ssl.keyStore").replaceAll(
							"%WEB-INF%", ApplicationProperties.getBasePath()));
		if (ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.ssl.trustStore") != null)
			System.setProperty(
					"javax.net.ssl.trustStore",
					ApplicationProperties.getProperty(
							"tmtbl.authenticate.ldap.ssl.trustStore")
							.replaceAll("%WEB-INF%",
									ApplicationProperties.getBasePath()));
		if (ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.ssl.trustStorePassword") != null)
			System.setProperty(
					"javax.net.ssl.keyStorePassword",
					ApplicationProperties
							.getProperty("tmtbl.authenticate.ldap.ssl.keyStorePassword"));
		if (ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.ssl.trustStorePassword") != null)
			System.setProperty(
					"javax.net.ssl.trustStorePassword",
					ApplicationProperties
							.getProperty("tmtbl.authenticate.ldap.ssl.trustStorePassword"));
		if (ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.ssl.trustStoreType") != null)
			System.setProperty(
					"javax.net.ssl.trustStoreType",
					ApplicationProperties
							.getProperty("tmtbl.authenticate.ldap.ssl.trustStoreType"));
		return env;
	}

	public static DirContext getDirContext() throws NamingException {
		return new InitialDirContext(getEnv());
	}

	/**
	 * Perform actual authentication the user
	 */
	public boolean doAuthenticate(HashMap userProps) throws Exception {
		if (ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.provider") == null)
			throw new Exception("Ldap provider is not set.");

		String principal = ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.principal");
		if (principal == null)
			throw new Exception("Ldap principal is not set.");

		String query = ApplicationProperties
				.getProperty("tmtbl.authenticate.ldap.query");
		if (query == null)
			throw new Exception("Ldap query is not set.");

		String n = (String) userProps.get("username");
		String p = (String) userProps.get("password");

		Hashtable<String, String> env = getEnv();
		env.put(Context.SECURITY_PRINCIPAL, principal.replaceAll("%", n));
		env.put(Context.SECURITY_CREDENTIALS, p);
		InitialDirContext cx = new InitialDirContext(env);

		String idAttributeName = ApplicationProperties.getProperty(
				"tmtbl.authenticate.ldap.externalId", "uid");
		Attributes attributes = cx.getAttributes(query.replaceAll("%", n),
				new String[] { idAttributeName });

		Attribute idAttribute = attributes.get(idAttributeName);
		if (idAttribute != null) {
			sLog.debug("Ldap authentication passed ... ");
			setAuthSucceeded(true);
			iExternalUid = (String) idAttribute.get();
			try {
				if (iExternalUid != null
						&& ApplicationProperties
								.getProperty("tmtbl.authenticate.ldap.externalId.format") != null)
					iExternalUid = new DecimalFormat(
							ApplicationProperties
									.getProperty("tmtbl.authenticate.ldap.externalId.format"))
							.format(Long.parseLong(iExternalUid));
			} catch (NumberFormatException e) {
			}
			setUser(n);
			return true;
		}

		return false;
	}
}