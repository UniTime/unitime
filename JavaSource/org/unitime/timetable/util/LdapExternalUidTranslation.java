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