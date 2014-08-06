/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.admin;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
		levels.add(new ListItem(String.valueOf(Level.ALL_INT), MESSAGES.levelAll()));
		levels.add(new ListItem(String.valueOf(Level.TRACE_INT), MESSAGES.levelTrace()));
		levels.add(new ListItem(String.valueOf(Level.DEBUG_INT), MESSAGES.levelDebug()));
		levels.add(new ListItem(String.valueOf(Level.INFO_INT), MESSAGES.levelInfo()));
		levels.add(new ListItem(String.valueOf(Level.WARN_INT), MESSAGES.levelWarning()));
		levels.add(new ListItem(String.valueOf(Level.ERROR_INT), MESSAGES.levelError()));
		levels.add(new ListItem(String.valueOf(Level.FATAL_INT), MESSAGES.levelFatal()));
		levels.add(new ListItem(String.valueOf(Level.OFF_INT), MESSAGES.levelOff()));
		
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldLogger(), FieldType.text, 400, 1024, Flag.UNIQUE),
				new Field(MESSAGES.fieldLevel(), FieldType.list, 100, levels, Flag.NOT_EMPTY));
		data.setSortBy(0, 1);
		
		long id = 0;
		SimpleEditInterface.Record root = data.addRecord(id++, false);
		root.setField(0, " root", false);
		root.setField(1, String.valueOf(LogManager.getRootLogger().getLevel().toInt()));
		
		for (Enumeration e = LogManager.getCurrentLoggers(); e.hasMoreElements(); ) {
			Logger logger = (Logger)e.nextElement();
			if (logger.getLevel() == null) continue;
			ApplicationConfig config = ApplicationConfig.getConfig("log4j.logger." + logger.getName());
			SimpleEditInterface.Record record = data.addRecord(id++, ApplicationProperties.getDefaultProperties().getProperty("log4j.logger." + logger.getName()) == null && config != null);
			record.setField(0, logger.getName(), false);
			record.setField(1, String.valueOf(logger.getLevel().toInt()));
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
		
		for (Enumeration e = LogManager.getCurrentLoggers(); e.hasMoreElements(); ) {
			Logger logger = (Logger)e.nextElement();
			if (logger.getLevel() == null) continue;
			Record r = records.get(logger.getName());
			if (r == null)
				delete(r, context, hibSession);
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
		Level level = Level.toLevel(Integer.valueOf(record.getField(1)));
		solverServerService.setLoggingLevel(root ? null : record.getField(0), level.toInt());
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

}
