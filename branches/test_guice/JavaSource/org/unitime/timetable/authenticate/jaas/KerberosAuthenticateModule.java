/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.unitime.commons.User;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.TimetableManager;

import com.sun.security.auth.module.Krb5LoginModule;

public class KerberosAuthenticateModule implements LoginModule {
	private Krb5LoginModule kerberos = new Krb5LoginModule();
	private Subject subject = null;
	private String realm = null;

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
		kerberos.initialize(subject, new AddRealmCallbackHandler(callbackHandler), sharedState, options);
		this.subject = subject;
		this.realm = (String)options.get("realm");
	}

	@Override
	public boolean login() throws LoginException {
		return kerberos.login();
	}

	@Override
	public boolean commit() throws LoginException {
		if (!kerberos.commit()) return false;
		for (KerberosPrincipal principal: subject.getPrincipals(KerberosPrincipal.class)) {
			String user = principal.getName();
			if (user.endsWith("@" + principal.getRealm()))
				user = user.substring(0, user.length() - principal.getRealm().length() - 1);
			
            User p = new User();
            p.setLogin(user);
            p.setId(user);
            p.setName(principal.getName());
            p.setAdmin(false);
            p.setRoles(new Vector());
            p.setDepartments(new Vector());
            
            TimetableManager manager = TimetableManager.findByExternalId(user);
            
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
                throw new LoginException ("Role not found. Access denied to user: " + principal.getName());
            }
        
            // Add user object to subjects public credentials
            subject.getPublicCredentials().add(p);
		}
		return true;
	}

	@Override
	public boolean abort() throws LoginException {
		return kerberos.abort();
	}

	@Override
	public boolean logout() throws LoginException {
		return kerberos.logout();
	}
	
	private class AddRealmCallbackHandler implements CallbackHandler {
		private CallbackHandler iHandler;
		private AddRealmCallbackHandler(CallbackHandler handler) {
			iHandler = handler;
		}
		
		@Override
		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
			iHandler.handle(callbacks);
			if (realm != null)
				for (Callback callback: callbacks) {
					if (callback instanceof NameCallback) {
						NameCallback nc = (NameCallback) callback;
						if (nc.getName() != null && !nc.getName().contains("@"))
							nc.setName(nc.getName() + "@" + realm);
					}
				}
		}
	}
	
}
