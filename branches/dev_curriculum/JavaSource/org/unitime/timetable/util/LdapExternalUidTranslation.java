/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.util;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.authenticate.jaas.LdapAuthenticateModule;
import org.unitime.timetable.interfaces.ExternalUidTranslation;

public class LdapExternalUidTranslation implements ExternalUidTranslation {
    
    public String translate(String uid, Source source, Source target) {
        if (uid==null || source.equals(target)) return uid;
        if (source.equals(Source.LDAP)) return uid2ext(uid);
        if (target.equals(Source.LDAP)) return ext2uid(uid);
        return uid;
    }
    
    public String uid2ext(String uid) {
        try {
            DirContext ctx = null;
            try {
                ctx = LdapAuthenticateModule.getDirContext();
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
            Debug.error("Unable to translate uid to puid, "+e.getMessage());
        }
        return null;
    }
    
    public String ext2uid(String puid) {
        try {
            DirContext ctx = null;
            try {
                ctx = LdapAuthenticateModule.getDirContext();
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
            Debug.error("Unable to translate uid to puid, "+e.getMessage());
        }
        return null;
    }

}