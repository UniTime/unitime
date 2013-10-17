/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.ExternalUidTranslation;

@Deprecated
/**
 * @deprecated Use {@link org.unitime.timetable.spring.ldap.SpringLdapExternalUidTranslation} instead.
 *
 * @author Tomas Muller
 */
public class LdapExternalUidTranslation implements ExternalUidTranslation {
    
    public String translate(String uid, Source source, Source target) {
        if (uid==null || source.equals(target)) return uid;
        if (source.equals(Source.LDAP)) return uid2ext(uid);
        if (target.equals(Source.LDAP)) return ext2uid(uid);
        return uid;
    }
    
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
    
    public String uid2ext(String uid) {
        try {
            DirContext ctx = null;
            try {
                ctx = getDirContext();
                Attributes attributes = ctx.getAttributes(
                		ApplicationProperties.getProperty("tmtbl.authenticate.ldap.uid2ext").replaceAll("%", uid),
                		new String[] {
                			ApplicationProperties.getProperty("tmtbl.authenticate.ldap.externalId", "puid")
                		});
                if (attributes!=null) {
                    Attribute puid = attributes.get(ApplicationProperties.getProperty("tmtbl.authenticate.ldap.externalId", "puid"));
                    if (puid!=null) return (String)puid.get();
                }
            } finally {
                if (ctx!=null) ctx.close();
            }
        } catch (Exception e) {
            Debug.error("Unable to translate uid to ext, "+e.getMessage());
        }
        return null;
    }
    
    public String ext2uid(String puid) {
        try {
            DirContext ctx = null;
            try {
                ctx = getDirContext();
                Attributes attributes = ctx.getAttributes(
                		ApplicationProperties.getProperty("tmtbl.authenticate.ldap.ext2uid").replaceAll("%", puid),
                		new String[] {
                			ApplicationProperties.getProperty("tmtbl.authenticate.ldap.login", "uid")
                		});
                if (attributes!=null) {
                    Attribute uid = attributes.get(ApplicationProperties.getProperty("tmtbl.authenticate.ldap.login", "uid"));
                    if (uid!=null) return (String)uid.get();
                }
            } finally {
                if (ctx!=null) ctx.close();
            }
        } catch (Exception e) {
            Debug.error("Unable to translate ext to uid, "+e.getMessage());
        }
        return null;
    }

}