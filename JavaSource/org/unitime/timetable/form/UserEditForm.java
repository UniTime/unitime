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

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.User;
import org.unitime.timetable.spring.security.MD5PasswordEncoder;

/** 
 * 
 * @author Tomas Muller
 * 
 */
public class UserEditForm implements UniTimeForm {
	private static final long serialVersionUID = 8703608968811726905L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private String iOp = null;
    private String iExternalId = null;
    private String iName = null;
    private String iPassword = null;
    private String iToken = null;
    
    public UserEditForm() {
    	reset();
    }

    @Override
	public void validate(UniTimeAction action) {
        if (iExternalId ==null || iExternalId.trim().isEmpty()) {
        	action.addFieldError("form.externalId", MSG.errorRequiredField(MSG.columnExternalId()));
        } else if (!MSG.actionUpdateUser().equals(getOp()) && User.findByExternalId(getExternalId())!=null) {
        	action.addFieldError("form.externalId", MSG.errorAlreadyExists(iExternalId));
        }

        if (iName==null || iName.trim().isEmpty()) {
        	action.addFieldError("form.name", MSG.errorRequiredField(MSG.columnUserName()));
        } else {
            try {
                User user = User.findByUserName(iName);
                if (user!=null && !user.getExternalUniqueId().equals(iExternalId))
                	action.addFieldError("form.name", MSG.errorAlreadyExists(iName));
            } catch (Exception e) {
            	action.addFieldError("form.name", e.getMessage());
            }
        }

        if (MSG.actionSaveUser().equals(getOp()) && (iPassword==null || iPassword.trim().length()==0))
        	action.addFieldError("form.password", MSG.errorRequiredField(MSG.columnUserPassword()));
	}

    @Override
	public void reset() {
		iOp = "List"; iExternalId = null; iName = null; iPassword = null; iToken = null;
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
    public String getExternalId() { return iExternalId; }
    public void setExternalId(String externalId) { iExternalId = externalId; }
    public String getName() { return iName; }
    public void setName(String name) { iName = name; }
    public String getPassword() { return iPassword; }
    public void setPassword(String password) { iPassword = password; }
    public String getToken() { return iToken; }
    public void setToken(String token) { iToken = token; }
    
    public void load(User user) {
        if (user==null) {
            setOp(MSG.actionSaveUser());
        } else {
            setOp(MSG.actionUpdateUser());
            setExternalId(user.getExternalUniqueId());
            setName(user.getUsername());
            setPassword(user.getPassword());
        }
    }
    
	public static String encodePassword(String clearTextPassword) {
		return MD5PasswordEncoder.getEncodedPassword(clearTextPassword);
	}
    
    public void saveOrUpdate(org.hibernate.Session hibSession) throws Exception {
        if (MSG.actionUpdateUser().equals(getOp())) {
            User u = User.findByExternalId(getExternalId());
            if (u.getUsername().equals(getName())) {
                if (getPassword() != null && !getPassword().equals(u.getPassword()) && !getPassword().isEmpty()) {
                    u.setPassword(encodePassword(getPassword()));
                }
                hibSession.update(u);
            } else {
                User w = new User();
                w.setExternalUniqueId(u.getExternalUniqueId());
                w.setUsername(getName());
                if (getPassword() == null || getPassword().equals(u.getPassword()) || getPassword().isEmpty()) {
                    w.setPassword(u.getPassword());
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

