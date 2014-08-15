/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.unitime.timetable.model.User;
import org.unitime.timetable.model.dao.UserDAO;

/**
 * Authenticates a user by looking up username/password in the database Options:
 * debug=true/false
 *
 * @author Tomas Muller
 */
@Deprecated
public class DbAuthenticateModule extends AuthenticateModule {
	private static Logger sLog = Logger.getLogger(DbAuthenticateModule.class);

	private String iExternalUid;

	/**
	 * Abort authentication (when overall authentication fails)
	 */
	public boolean abort() throws LoginException {
		if (!isAuthSucceeded()) {
			return false;
		} else {
			if (isAuthSucceeded() && !isCommitSucceeded()) {
				reset();
			} else {
				logout();
			}
		}

		return true;
	}

	/**
	 * Commit phase of login
	 */
	public boolean commit() throws LoginException {
		if (isAuthSucceeded()) {
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
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {

		super.initialize(subject, callbackHandler, sharedState, options);
		iExternalUid = null;
	}

	/**
	 * Authenticate the user
	 */
	public boolean login() throws LoginException {

		sLog.debug("Performing db authentication ... ");

		// Get callback parameters
		if (getCallbackHandler() == null)
			throw new LoginException("Error: no CallbackHandler available ");

		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("User Name: ");
		callbacks[1] = new PasswordCallback("Password: ", true);

		try {
			getCallbackHandler().handle(callbacks);
			String n = ((NameCallback) callbacks[0]).getName();
			String p = String.valueOf(((PasswordCallback) callbacks[1]).getPassword());

			HashMap userProps = new HashMap();
			userProps.put("username", n);
			userProps.put("password", p);

			if (doAuthenticate(userProps)) return true;

			// Authentication failed
			sLog.debug("Db authentication failed ... ");
			setAuthSucceeded(false);
			return false;
		} catch (Exception ex) {
			sLog.debug("Db authentication failed ... " + ex.getMessage(), ex);
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

	/**
	 * Perform actual authentication the user
	 */
	public boolean doAuthenticate(HashMap userProps) throws Exception {

		String n = (String) userProps.get("username");
		String p = (String) userProps.get("password");

		// Check username/password with DB
		User u = new UserDAO().get(n);
		if (u != null) {
			String pwd = u.getPassword();

			// Authentication succeeded
			if (checkPassword(p, pwd)) {
				sLog.debug("Db authentication passed ... ");
				setAuthSucceeded(true);
				iExternalUid = u.getExternalUniqueId();
				setUser(n);
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets the MD5 hash and encodes it in Base 64 notation
	 * 
	 * @param clearTextPassword
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String getEncodedPassword(String clearTextPassword) {
		return new MessageDigestPasswordEncoder("MD5", true).encodePassword(clearTextPassword, null);
	}

	/**
	 * Checks a password with the MD5 hash
	 * 
	 * @param clearTextTestPassword
	 * @param encodedActualPassword
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static boolean checkPassword(String clearTextTestPassword, String encodedActualPassword) throws NoSuchAlgorithmException {
		String encodedTestPassword = getEncodedPassword(clearTextTestPassword);
		return (encodedTestPassword.equals(encodedActualPassword));
	}

	/**
	 * Generate passwords
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(DbAuthenticateModule.getEncodedPassword(args[0]));
	}
}