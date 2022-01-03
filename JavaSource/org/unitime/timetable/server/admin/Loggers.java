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
package org.unitime.timetable.server.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ApplicationConfig;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=logging]")
public class Loggers implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Autowired SolverServerService solverServerService;
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageLoggingLevel(), MESSAGES.pageLoggingLevels());
	}

	@Override
	@PreAuthorize("checkPermission('ApplicationConfig')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> levels = new ArrayList<ListItem>();
		levels.add(new ListItem(Level.ALL.name(), MESSAGES.levelAll()));
		levels.add(new ListItem(Level.TRACE.name(), MESSAGES.levelTrace()));
		levels.add(new ListItem(Level.DEBUG.name(), MESSAGES.levelDebug()));
		levels.add(new ListItem(Level.INFO.name(), MESSAGES.levelInfo()));
		levels.add(new ListItem(Level.WARN.name(), MESSAGES.levelWarning()));
		levels.add(new ListItem(Level.ERROR.name(), MESSAGES.levelError()));
		levels.add(new ListItem(Level.FATAL.name(), MESSAGES.levelFatal()));
		levels.add(new ListItem(Level.OFF.name(), MESSAGES.levelOff()));
		
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldLogger(), FieldType.text, 400, 1024, Flag.UNIQUE),
				new Field(MESSAGES.fieldLevel(), FieldType.list, 100, levels, Flag.NOT_EMPTY));
		data.setSortBy(0, 1);
		
		long id = 0;
		SimpleEditInterface.Record root = data.addRecord(id++, false);
		root.setField(0, " root", false);
		root.setField(1, LogManager.getRootLogger().getLevel().name());
		
		LoggerContext cx = LoggerContext.getContext(false);
		for (Map.Entry<String, LoggerConfig> e: cx.getConfiguration().getLoggers().entrySet()) {
			String name = e.getKey();
			Level level = e.getValue().getLevel();
			if (level == null || name.isEmpty()) continue;
			ApplicationConfig config = ApplicationConfig.getConfig("log4j.logger." + name);
			SimpleEditInterface.Record record = data.addRecord(id++, ApplicationProperties.getDefaultProperties().getProperty("log4j.logger." + name) == null && config != null);
			record.setField(0, name, false);
			record.setField(1, level.name());	
		}
		data.setEditable(context.hasPermission(Right.ApplicationConfig));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('ApplicationConfig')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		Map<String, SimpleEditInterface.Record> records = new HashMap<String, SimpleEditInterface.Record>();
		for (Record r: data.getRecords()) {
			if (r.isEmpty()) continue;
			boolean root = r.getUniqueId() != null && r.getUniqueId() == 0;
			if (root) {
				update(r, context, hibSession);
			} else {
				records.put(r.getField(0), r);
			}
		}
		
		LoggerContext cx = LoggerContext.getContext(false);
		for (Map.Entry<String, LoggerConfig> e: cx.getConfiguration().getLoggers().entrySet()) {
			String name = e.getKey();
			Level level = e.getValue().getLevel();
			if (level == null || name.isEmpty()) continue;
			Record r = records.get(name);
			if (r == null)
				delete(name, context, hibSession);
			else
				update(r, context, hibSession);
		}
		
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('ApplicationConfig')")
	public void save(Record record, SessionContext context, Session hibSession) {
		update(record, context, hibSession);
		record.setUniqueId(System.currentTimeMillis());
	}

	@Override
	@PreAuthorize("checkPermission('ApplicationConfig')")
	public void update(Record record, SessionContext context, Session hibSession) {
		boolean root = record.getUniqueId() != null && record.getUniqueId() == 0;
		Level level = Level.getLevel(record.getField(1));
		solverServerService.setLoggingLevel(root ? null : record.getField(0), level == null ? null : level.name());
		record.setUniqueId(root ? 0 : System.currentTimeMillis());
		
		String defaultValue = ApplicationProperties.getDefaultProperties().getProperty(root ? "log4j.rootLogger" : "log4j.logger." + record.getField(0));
		if (defaultValue != null && defaultValue.indexOf(',') > 0)
			defaultValue = defaultValue.substring(0, defaultValue.indexOf(',')).trim();

		ApplicationConfig config = ApplicationConfig.getConfig(root ? "log4j.logger.root" : "log4j.logger." + record.getField(0));
		if (level.toString().equalsIgnoreCase(defaultValue)) {
			if (config != null)
				hibSession.delete(config);
		} else {
			if (config == null) {
				config = new ApplicationConfig();
				config.setKey(root ? "log4j.logger.root" : "log4j.logger." + record.getField(0));
				config.setDescription(MESSAGES.descriptionLoggingLevelFor(record.getField(0)));
			}
			config.setValue(level.toString());
			hibSession.saveOrUpdate(config);
		}
	}

	@Override
	@PreAuthorize("checkPermission('ApplicationConfig')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		boolean root = record.getUniqueId() != null && record.getUniqueId() == 0;
		solverServerService.setLoggingLevel(root ? null : record.getField(0), null);
		ApplicationConfig config = ApplicationConfig.getConfig(root ? "log4j.logger.root" : "log4j.logger." + record.getField(0));
		if (config != null)
			hibSession.delete(config);
	}
	
	protected void delete(String name, SessionContext context, Session hibSession) {
		boolean root = " root".equals(name);
		solverServerService.setLoggingLevel(root ? null : name, null);
		ApplicationConfig config = ApplicationConfig.getConfig(root ? "log4j.logger.root" : "log4j.logger." + name);
		if (config != null)
			hibSession.delete(config);
	}

}
