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
package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.unitime.timetable.model.User;

/** 
 * 
 * @author Tomas Muller
 * 
 */
public class UserEditForm extends ActionForm {
	private static final long serialVersionUID = 8703608968811726905L;
	private String iOp = null;
    private String iExternalId = null;
    private String iName = null;
    private String iPassword = null;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        if (iExternalId ==null || iExternalId.trim().length()==0)
            errors.add("externalId", new ActionMessage("errors.required", ""));
        else if (!"Update".equals(getOp()) && User.findByExternalId(getExternalId())!=null) {
            errors.add("externalId", new ActionMessage("errors.exists", iExternalId));
        }

        if (iName==null || iName.trim().length()==0)
            errors.add("name", new ActionMessage("errors.required", ""));
        else {
            try {
                User user = User.findByUserName(iName);
                if (user!=null && !user.getExternalUniqueId().equals(iExternalId))
                    errors.add("name", new ActionMessage("errors.exists", iName));
            } catch (Exception e) {
                errors.add("name", new ActionMessage("errors.generic", e.getMessage()));
            }
        }

        if (iPassword==null || iPassword.trim().length()==0)
            errors.add("password", new ActionMessage("errors.required", ""));
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = "List"; iExternalId = null; iName = null; iPassword = null;
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
    public String getExternalId() { return iExternalId; }
    public void setExternalId(String externalId) { iExternalId = externalId; }
    public String getName() { return iName; }
    public void setName(String name) { iName = name; }
    public String getPassword() { return iPassword; }
    public void setPassword(String password) { iPassword = password; }
    
    public void load(User user) {
        if (user==null) {
            setOp("Save");
        } else {
            setOp("Update");
            setExternalId(user.getExternalUniqueId());
            setName(user.getUsername());
            setPassword(user.getPassword());
        }
    }
    
	public static String encodePassword(String clearTextPassword) {
		return new MessageDigestPasswordEncoder("MD5", true).encodePassword(clearTextPassword, null);
	}
    
    public void saveOrUpdate(org.hibernate.Session hibSession) throws Exception {
        if ("Update".equals(getOp())) {
            User u = User.findByExternalId(getExternalId());
            if (u.getUsername().equals(getName())) {
                if (!getPassword().equals(u.getPassword())) {
                    u.setPassword(encodePassword(getPassword()));
                }
                hibSession.update(u);
            } else {
                User w = new User();
                w.setExternalUniqueId(u.getExternalUniqueId());
                w.setUsername(getName());
                if (getPassword().equals(u.getPassword())) {
                    w.setPassword(getPassword());
                } else {
                    w.setPassword(encodePassword(getPassword()));
                }
                hibSession.delete(u);
                hibSession.save(w);
            }
        } else {
            User u = new User();
            u.setExternalUniqueId(getExternalId());
            u.setUsername(getName());
            u.setPassword(encodePassword(getPassword()));
            hibSession.save(u);
        }
    }
    
    public void delete(org.hibernate.Session hibSession) {
        User u = User.findByExternalId(getExternalId());
        if (u!=null) hibSession.delete(u);
    }
}

