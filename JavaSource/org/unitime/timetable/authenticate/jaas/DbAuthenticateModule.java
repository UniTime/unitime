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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import org.unitime.commons.Base64;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.User;
import org.unitime.timetable.model.dao.UserDAO;


/**
 * Authenticates a user by looking up username/password in the database
 * Options: debug=true/false
 * @author Heston Fernandes
 */
public class DbAuthenticateModule 
	extends AuthenticateModule {

    // --------------------------------------------------------- Instance Variables

	private String iExternalUid;
	
    // --------------------------------------------------------- Methods
	
	/**
	 * Abort authentication (when overall authentication fails)
	 */
	public boolean abort() throws LoginException {
		if (!isAuthSucceeded()) {
			return false;
		}
		else {
			if (isAuthSucceeded() && !isCommitSucceeded()) {
				reset();
			}
			else {
				logout();
			}
		}
		
		return true;
	}

	/**
	 * Commit phase of login
	 */
	public boolean commit() throws LoginException {
		if (isAuthSucceeded()) {      // Check if authentication succeeded		
			
			// External UID must exist in order to get manager info
			if (iExternalUid==null || iExternalUid.trim().length()==0)
				throw new LoginException ("External UID not found");

			org.unitime.commons.User p = new org.unitime.commons.User();
			
		    p.setLogin(getUser());
		    p.setId(iExternalUid);
		    p.setName(getUser());
		    p.setAdmin(false);
		    p.setRoles(new Vector());
		    p.setDepartments(new Vector());
		    
            TimetableManager manager = TimetableManager.findByExternalId(iExternalUid);
            
            if (manager!=null) {
                p.setName(manager.getName());
                
                // Get roles
                for (Iterator i=manager.getManagerRoles().iterator();i.hasNext();) {
                    ManagerRole role = (ManagerRole)i.next();
                    p.getRoles().add(role.getRole().getReference());
                }
                
                // Get departments
                for (Iterator i=manager.getDepartments().iterator();i.hasNext();) {
                    Department dept = (Department)i.next();
                    p.getDepartments().add(dept.getDeptCode());
                }
            }

            // Check at least one role is found
            if (p.getRoles().isEmpty() && !"true".equals(ApplicationProperties.getProperty("tmtbl.authentication.norole","false"))) {
                if (isDebug()) System.out.println("Role not found. Access Denied to User: " + getUser());
                throw new LoginException ("Role not found. Access Denied to User: " + getUser());
            }
		
            // Create user principal
            if (isDebug()) System.out.println("User Roles: " + p.getRoles());
            if (isDebug()) System.out.println("User Depts: " + p.getDepartments());


			// Add user object to subjects public credentials
			getSubject().getPublicCredentials().add(p);
			
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
	public void initialize(
			Subject subject, 
			CallbackHandler callbackHandler,
			Map sharedState, 
			Map options ) {
		
		super.initialize(subject, callbackHandler, sharedState, options);
		iExternalUid = null;
	}

	/**
	 * Authenticate the user
	 */
	public boolean login() throws LoginException {

		if (isDebug()) System.out.println("Performing db authentication ... ");

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
			
			if (doAuthenticate(userProps))
				return true;
			
			// Authentication failed
			if (isDebug()) System.out.println("Db authentication failed ... ");
			setAuthSucceeded(false);
			return false;
		} 
		catch (Exception ex) {
			if (isDebug()) System.out.println("Db authentication failed ... " + ex.getMessage());
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
		if (u!=null) {
			String pwd = u.getPassword();
			
			// Authentication succeeded
			if (checkPassword(p, pwd)) {
				if (isDebug()) System.out.println("Db authentication passed ... ");
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
	 * @param clearTextPassword
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String getEncodedPassword(String clearTextPassword)
			throws NoSuchAlgorithmException {
		
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(clearTextPassword.getBytes());
		return Base64.encodeBytes(md.digest());
	}

	/**
	 * Checks a password with the MD5 hash
	 * @param clearTextTestPassword
	 * @param encodedActualPassword
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static boolean checkPassword(
			String clearTextTestPassword,
			String encodedActualPassword ) throws NoSuchAlgorithmException {
		
		String encodedTestPassword = getEncodedPassword(clearTextTestPassword);
		return (encodedTestPassword.equals(encodedActualPassword));
	}
	
	/**
	 * Generate passwords
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(DbAuthenticateModule.getEncodedPassword(args[0]));
	}
}
