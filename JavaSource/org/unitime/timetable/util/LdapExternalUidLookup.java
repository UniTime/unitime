/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.ExternalUidLookup;

@Deprecated
/**
 * @deprecated Use {@link org.unitime.timetable.spring.ldap.SpringLdapExternalUidLookup} instead.
 */
public class LdapExternalUidLookup implements ExternalUidLookup {
	
    public DirContext getDirContext() throws NamingException {
        Hashtable<String,String> env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, ApplicationProperties.getProperty("tmtbl.authenticate.ldap.ctxFactory","com.sun.jndi.ldap.LdapCtxFactory"));
        env.put(Context.PROVIDER_URL, ApplicationProperties.getProperty("tmtbl.authenticate.ldap.provider"));
        env.put(Context.REFERRAL, ApplicationProperties.getProperty("tmtbl.authenticate.ldap.referral","ignore"));
        if (ApplicationProperties.getProperty("tmtbl.authenticate.ldap.version")!=null)
            env.put("java.naming.ldap.version", ApplicationProperties.getProperty("tmtbl.authenticate.ldap.version"));
        env.put(Context.SECURITY_AUTHENTICATION, ApplicationProperties.getProperty("tmtbl.authenticate.ldap.security","simple"));
        if (ApplicationProperties.getProperty("tmtbl.authenticate.ldap.socketFactory")!=null)
            env.put("java.naming.ldap.factory.socket",ApplicationProperties.getProperty("tmtbl.authenticate.ldap.socketFactory"));
        if (ApplicationProperties.getProperty("tmtbl.authenticate.ldap.ssl.keyStore")!=null)
            System.setProperty("javax.net.ssl.keyStore", ApplicationProperties.getProperty("tmtbl.authenticate.ldap.ssl.keyStore").replaceAll("%WEB-INF%", ApplicationProperties.getBasePath()));
        if (ApplicationProperties.getProperty("tmtbl.authenticate.ldap.ssl.trustStore")!=null)
            System.setProperty("javax.net.ssl.trustStore", ApplicationProperties.getProperty("tmtbl.authenticate.ldap.ssl.trustStore").replaceAll("%WEB-INF%", ApplicationProperties.getBasePath()));
        if (ApplicationProperties.getProperty("tmtbl.authenticate.ldap.ssl.trustStorePassword")!=null)
            System.setProperty("javax.net.ssl.keyStorePassword", ApplicationProperties.getProperty("tmtbl.authenticate.ldap.ssl.keyStorePassword"));
        if (ApplicationProperties.getProperty("tmtbl.authenticate.ldap.ssl.trustStorePassword")!=null)
            System.setProperty("javax.net.ssl.trustStorePassword", ApplicationProperties.getProperty("tmtbl.authenticate.ldap.ssl.trustStorePassword"));
        if (ApplicationProperties.getProperty("tmtbl.authenticate.ldap.ssl.trustStoreType")!=null)
            System.setProperty("javax.net.ssl.trustStoreType", ApplicationProperties.getProperty("tmtbl.authenticate.ldap.ssl.trustStoreType"));
    	return new InitialDirContext(env);
    }

	@Override
	public UserInfo doLookup(String searchId) throws Exception {
		
		String query = ApplicationProperties.getProperty("tmtbl.authenticate.ldap.identify");
		if (query == null) return null;
		
        DirContext ctx = null;
        try {
            ctx = getDirContext();
            
    		String idAttributeName = ApplicationProperties.getProperty("tmtbl.authenticate.ldap.externalId","uid");
    		String loginAttributeName = ApplicationProperties.getProperty("tmtbl.authenticate.ldap.login", "uid");
    		Attributes attributes = ctx.getAttributes(query.replaceAll("%", searchId), new String[] {idAttributeName, loginAttributeName, "cn", "givenName", "sn", "mail"});
            Attribute idAttribute = attributes.get(idAttributeName);
            if (idAttribute == null) return null;
            
        	UserInfo user = new UserInfo();
        	user.setExternalId((String)idAttribute.get());
        	user.setUserName((String)attributes.get(loginAttributeName).get());
        	if (attributes.get("cn") != null)
        		user.setName((String)attributes.get("cn").get());
        	if (attributes.get("givenName") != null)
        		user.setFirstName((String)attributes.get("givenName").get());
        	if (attributes.get("cn") != null)
        		user.setName((String)attributes.get("cn").get());
        	if (attributes.get("sn") != null)
        		user.setLastName((String)attributes.get("sn").get());
        	if (attributes.get("mail") != null) {
        		user.setEmail((String)attributes.get("mail").get());
        	} else {
            	String email = user.getUserName() + "@";
            	for (String x: query.split(","))
            		if (x.startsWith("dc=")) email += (email.endsWith("@") ? "" : ".") + x.substring(3);
            	if (!email.endsWith("@")) user.setEmail(email);
        	}
        	
        	return user;			
		} finally {
			if (ctx != null) ctx.close();
		}
	}

}
