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
 *
 * @author Tomas Muller
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
