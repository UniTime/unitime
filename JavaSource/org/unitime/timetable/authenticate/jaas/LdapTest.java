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
package org.unitime.timetable.authenticate.jaas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

public class LdapTest {
	
	private static class LdapProperties extends Properties {
		private Set<String> keys = new HashSet<String>();
		private static final long serialVersionUID = 1L;

		public LdapProperties() {
			super();
		}
		
		public String getProperty(String key, String defaultValue) {
			if (keys.add(key))
				System.out.println("Using " + key + "=" + super.getProperty(key, defaultValue));
			return super.getProperty(key, defaultValue);
		}

		public String getProperty(String key) {
			if (keys.add(key))
				System.out.println("Using " + key + "=" + super.getProperty(key));
			return super.getProperty(key);
		}
	}
	
	public static void main(String[] args) {
		try {
			LdapProperties prop = new LdapProperties();
			File propFile = null;
			if (args.length > 0) {
				propFile = new File(args[0]);
			} else if (System.getProperty("tmtbl.custom.properties") != null) {
				propFile = new File(System.getProperty("tmtbl.custom.properties"));
			}
			if (propFile == null || !propFile.exists()) {
				System.out.println("Usage: java -jar ldap-test.jar ldap.properties");
				if (propFile != null)
					System.err.println("File " + propFile.getAbsolutePath() + " does not exists.");
				System.exit(1);
			} else {
				System.out.println("Loading properties " + propFile.getAbsolutePath() + " ...");
				prop.load(new FileInputStream(propFile));
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Username:");
			String n = in.readLine();
			System.out.print("Password:");
			String p = in.readLine();
			
			// get principal & query
	        String principal = prop.getProperty("tmtbl.authenticate.ldap.principal");
	        if (principal==null) throw new Exception("Ldap principal is not set.");
	        
	        String query = prop.getProperty("tmtbl.authenticate.ldap.query");
	        if (query==null) throw new Exception("Ldap query is not set.");

			// create env
	        Hashtable<String,String> env = new Hashtable();
	        env.put(Context.INITIAL_CONTEXT_FACTORY, prop.getProperty("tmtbl.authenticate.ldap.ctxFactory","com.sun.jndi.ldap.LdapCtxFactory"));
	        env.put(Context.PROVIDER_URL, prop.getProperty("tmtbl.authenticate.ldap.provider"));
	        env.put(Context.REFERRAL, prop.getProperty("tmtbl.authenticate.ldap.referral","ignore"));
	        if (prop.getProperty("tmtbl.authenticate.ldap.version")!=null)
	            env.put("java.naming.ldap.version", prop.getProperty("tmtbl.authenticate.ldap.version"));
	        env.put(Context.SECURITY_AUTHENTICATION, prop.getProperty("tmtbl.authenticate.ldap.security","simple"));
	        if (prop.getProperty("tmtbl.authenticate.ldap.socketFactory")!=null)
	            env.put("java.naming.ldap.factory.socket",prop.getProperty("tmtbl.authenticate.ldap.socketFactory"));
	        if (prop.getProperty("tmtbl.authenticate.ldap.ssl.keyStore")!=null)
	            System.setProperty("javax.net.ssl.keyStore", prop.getProperty("tmtbl.authenticate.ldap.ssl.keyStore").replaceAll("%WEB-INF%", "."));
	        if (prop.getProperty("tmtbl.authenticate.ldap.ssl.trustStore")!=null)
	            System.setProperty("javax.net.ssl.trustStore", prop.getProperty("tmtbl.authenticate.ldap.ssl.trustStore").replaceAll("%WEB-INF%", "."));
	        if (prop.getProperty("tmtbl.authenticate.ldap.ssl.trustStorePassword")!=null)
	            System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("tmtbl.authenticate.ldap.ssl.keyStorePassword"));
	        if (prop.getProperty("tmtbl.authenticate.ldap.ssl.trustStorePassword")!=null)
	            System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("tmtbl.authenticate.ldap.ssl.trustStorePassword"));
	        if (prop.getProperty("tmtbl.authenticate.ldap.ssl.trustStoreType")!=null)
	            System.setProperty("javax.net.ssl.trustStoreType", prop.getProperty("tmtbl.authenticate.ldap.ssl.trustStoreType"));  
			
	        // create context
	        env.put(Context.SECURITY_PRINCIPAL, principal.replaceAll("%", n));
	        env.put(Context.SECURITY_CREDENTIALS, p);
			InitialDirContext cx = new InitialDirContext(env);

			// authenticate & retrieve external user id
			String idAttributeName = prop.getProperty("tmtbl.authenticate.ldap.externalId","uid");
			Attributes attributes = cx.getAttributes(query.replaceAll("%", n),new String[] {idAttributeName});
			
			Attribute idAttribute = attributes.get(idAttributeName);
	        if (idAttribute!=null) {
	            System.out.println("Authentication succeeded, external user id is " + idAttribute.get() + ".");
	        } else {
	        	System.out.println("Authentication succeeded, but external user id (named "+idAttributeName+") does not exists or it is not set.");
	        }
		} catch (Exception e) {
			System.err.println("Authentication failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
