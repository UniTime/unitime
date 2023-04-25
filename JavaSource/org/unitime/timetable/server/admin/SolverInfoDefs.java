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

import java.util.List;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.SolverInfoDef;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.SolverInfoDefDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=solverInfoDef]")
public class SolverInfoDefs implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageSolverInfoDef(), MESSAGES.pageSolverInfoDefs());
	}

	@Override
	@PreAuthorize("checkPermission('SolutionInformationDefinitions')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 300, 100, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 1000, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldImplementation(), FieldType.text, 300, 250, Flag.NOT_EMPTY)
				);
		data.setSortBy(0);
		
		for (SolverInfoDef def: (List<SolverInfoDef>)SolverInfoDefDAO.getInstance().findAll()) {
			boolean implementationExist = false;
			try {
				if (def.getImplementation() != null && def.getImplementation().startsWith("org.unitime.timetable.solver.ui.") && Class.forName(def.getImplementation()) != null)
					implementationExist = true;
			} catch (Exception e) {}
			Record r = data.addRecord(def.getUniqueId());
			r.setField(0, def.getName(), !implementationExist);
			r.setField(1, def.getDescription());
			r.setField(2, def.getImplementation(), !implementationExist);
			r.setDeletable(!implementationExist);
		}
		return data;
	}
	
	@Override
	@PreAuthorize("checkPermission('SolutionInformationDefinitions')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (SolverInfoDef def: (List<SolverInfoDef>)SolverInfoDefDAO.getInstance().findAll()) {
			Record r = data.getRecord(def.getUniqueId());
			if (r == null)
				delete(def, context, hibSession);
			else
				update(def, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);		
	}
	
	@Override
	@PreAuthorize("checkPermission('SolutionInformationDefinitions')")
	public void save(Record record, SessionContext context, Session hibSession) {
		SolverInfoDef def = new SolverInfoDef();
		def.setName(record.getField(0));
		def.setDescription(record.getField(1));
		def.setImplementation(record.getField(2));
		record.setUniqueId((Long)hibSession.save(def));
		ChangeLog.addChange(hibSession,
				context,
				def,
				def.getName(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(SolverInfoDef def, Record record, SessionContext context, Session hibSession) {
		if (def == null) return;
		if (ToolBox.equals(def.getName(), record.getField(0)) &&
			ToolBox.equals(def.getDescription(), record.getField(1)) &&
			ToolBox.equals(def.getImplementation(), record.getField(2))) return;
		def.setName(record.getField(0));
		def.setDescription(record.getField(1));
		def.setImplementation(record.getField(2));
		hibSession.saveOrUpdate(def);
		ChangeLog.addChange(hibSession,
				context,
				def,
				def.getName(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('SolutionInformationDefinitions')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(SolverInfoDefDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(SolverInfoDef def, SessionContext context, Session hibSession) {
		if (def == null) return;
		ChangeLog.addChange(hibSession,
				context,
				def,
				def.getName(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.remove(def);
	}
	
	@Override
	@PreAuthorize("checkPermission('SolutionInformationDefinitions')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(SolverInfoDefDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}


}