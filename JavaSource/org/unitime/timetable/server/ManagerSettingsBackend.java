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
package org.unitime.timetable.server;

import java.util.StringTokenizer;

import org.unitime.timetable.gwt.client.page.ManagerSettingsPage.ManagerSettingInterface;
import org.unitime.timetable.gwt.client.page.ManagerSettingsPage.ManagerSettingsRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.dao.SettingsDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(ManagerSettingsRequest.class)
public class ManagerSettingsBackend implements GwtRpcImplementation<ManagerSettingsRequest, GwtRpcResponseList<ManagerSettingInterface>>{

	@Override
	public GwtRpcResponseList<ManagerSettingInterface> execute(ManagerSettingsRequest request, SessionContext context) {
		context.checkPermission(Right.SettingsUser);
		
		if (request.getKey() != null)
			context.getUser().setProperty(request.getKey(), request.getValue());
		
		GwtRpcResponseList<ManagerSettingInterface> response = new GwtRpcResponseList<ManagerSettingInterface>();
		
		for (Settings s: SettingsDAO.getInstance().getSession().createQuery("from Settings order by key", Settings.class).setCacheable(true).list()) {
        	ManagerSettingInterface setting = new ManagerSettingInterface();
        	setting.setId(s.getUniqueId());
        	setting.setDescription(s.getDescription());
        	setting.setKey(s.getKey());
        	setting.setValue(context.getUser().getProperty(s.getKey(), s.getDefaultValue()));
        	setting.setDefaultValue(s.getDefaultValue());
        	for (StringTokenizer k = new StringTokenizer(s.getAllowedValues(), ","); k.hasMoreTokens(); ) {
    			String v = k.nextToken().trim();
    			int ch = v.indexOf(':');
    			if (ch >= 0) {
    				setting.addOption(v.substring(0, ch), v.substring(ch + 1));
    			} else {
    				setting.addOption(v, v);
    			}
    		}
        	response.add(setting);
        }
		
		return response;
	}

}
