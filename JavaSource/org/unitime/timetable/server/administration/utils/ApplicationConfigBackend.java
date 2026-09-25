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
package org.unitime.timetable.server.administration.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.admin.ApplicationConfigPage.ApplicationConfigRequest;
import org.unitime.timetable.gwt.client.admin.ApplicationConfigPage.ApplicationConfigResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.ApplicationConfig;
import org.unitime.timetable.model.SessionConfig;
import org.unitime.timetable.model.dao.ApplicationConfigDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(ApplicationConfigRequest.class)
public class ApplicationConfigBackend implements GwtRpcImplementation<ApplicationConfigRequest, ApplicationConfigResponse> {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public ApplicationConfigResponse execute(ApplicationConfigRequest request, SessionContext context) {
		context.checkPermission(Right.ApplicationConfig);
		
		ApplicationConfigResponse response = new ApplicationConfigResponse();
		response.setCanAdd(context.hasPermission(Right.ApplicationConfigEdit));
		if (request.hasShowAllProperties()) {
			response.setShowAllProperties(request.isShowAllProperties());
			context.getUser().setProperty("ApplicationConfig.showAll", request.isShowAllProperties() ? "1" : "0");
		} else {
			response.setShowAllProperties("1".equals(context.getUser().getProperty("ApplicationConfig.showAll", "0")));
		}
		
		TableInterface table = new TableInterface();
		table.setId("ApplicationConfig");
		table.setDefaultSortCookie(MSG.columnAppConfigKey());
		table.setName(MSG.sectAppSettings());

		LineInterface header = table.addHeader();
		header.addCell(MSG.columnAppConfigKey());
		header.addCell(MSG.columnAppConfigValue());
		header.addCell(MSG.columnAppConfigDescription());
		for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}

		Map<String, Object> configs = new HashMap<String, Object>();
		for (ApplicationConfig config: ApplicationConfigDAO.getInstance().findAll())
			configs.put(config.getKey(), config);
		Map<String, String> properties = new HashMap<String, String>();
		for (Map.Entry<Object, Object> p: ApplicationProperties.getProperties().entrySet())
			properties.put(p.getKey().toString(), p.getValue().toString());

		for (ApplicationProperty property: ApplicationProperty.values()) {
			if (properties.containsKey(property.key()) || property.isSecret() || property.isDeprecated()) continue;
			if (property.reference() == null) {
				properties.put(property.key(), property.defaultValue() == null ? "" : property.defaultValue());
			} else {
				boolean nomatch = true;
				for (Object key: properties.keySet()) {
					if (property.matches(key.toString())) { nomatch = false; break; }
				}
				if (nomatch)
					properties.put(property.key(), property.defaultValue() == null ? "" : property.defaultValue());
			}
		}
				
		if (context.getUser().getCurrentAcademicSessionId() != null) {
			for (SessionConfig config: SessionConfig.findAll(context.getUser().getCurrentAcademicSessionId()))
				configs.put(config.getKey(), config);
		}
		
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(ApplicationProperty.ApplicationConfigPattern.value());
		} catch (Exception e) {
			pattern = Pattern.compile(ApplicationProperty.ApplicationConfigPattern.defaultValue());
		}

		boolean editable = context.hasPermission(Right.ApplicationConfigEdit);
		if (properties.isEmpty())
			table.setErrorMessage(MSG.messageNoAppConfKeys());
		
		for (String key: new TreeSet<String>(properties.keySet())) {
			String value = properties.get(key);
			Object o = configs.get(key);
			ApplicationProperty p = ApplicationProperty.fromKey(key);
			String description = ApplicationProperty.getDescription(key);
			if (description == null) description = "";
			
			if (o == null) {
				if (!pattern.matcher(key).matches()) continue;
				if (!response.isShowAllProperties() && (p != null && (value == null ? "": value).equals(p.value() == null ? "" : p.value()))) continue;
				
				String reference = null;
				if (p != null && p.reference() != null) {
					reference = p.reference();
				}
				
				if (p != null && p.isSecret()) continue;

				LineInterface line = table.addLine();
				if (editable && (p == null || !p.isReadOnly()))
					line.setURL("#" + key);
				
				line.addCell(reference == null ? key : key.replace("%", "<" + reference + ">"))
					.addStyle(reference == null || key.indexOf('%') < 0 ? null : "font-style: italic;text-decoration: underline;");
				line.addCell(value).setColor("#5a5a5a")
					.addStyle("font-style: italic;");
				line.addCell(reference == null ? description : description.replace("%", "<" + reference + ">"));
				if (key.equals(request.getHash())) line.setBgColor("#b7d4fb");
				continue;
			}
			
			if (o instanceof SessionConfig) {
				SessionConfig config = (SessionConfig)o;
				if (config.getDescription() != null && !config.getDescription().isEmpty())
					description = config.getDescription();
				LineInterface line = table.addLine();
				if (editable && (p == null || !p.isReadOnly()))
					line.setURL("#" + key);
				CellInterface c = line.addCell(key);
				c.add(" " + MSG.supAppConfigSessionOnly())
					.addStyle("vertical-align: super; font-size: 0.75em;")
					.setColor("#2066CE")
					.setTitle(MSG.hintAppConfigAppliesTo(config.getSession().getLabel()));
				line.addCell(value);
				line.addCell(description);
				if (key.equals(request.getHash())) line.setBgColor("#b7d4fb");
			} else {
				ApplicationConfig config = (ApplicationConfig)o;
				if (config.getDescription() != null && !config.getDescription().isEmpty())
					description = config.getDescription();
				LineInterface line = table.addLine();
				if (editable && (p == null || !p.isReadOnly()))
					line.setURL("#" + key);
				line.addCell(key);
				line.addCell(value);
				line.addCell(description);
				if (key.equals(request.getHash())) line.setBgColor("#b7d4fb");
			}
		}
		
		response.setTable(table);
		
		return response;
	}

}
