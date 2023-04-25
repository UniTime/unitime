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
import java.util.Collections;
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
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMajorConcentration;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.PosMajorConcentrationDAO;
import org.unitime.timetable.model.dao.PosMajorDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=concentration]")
public class Concentrations implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageConcentration(), MESSAGES.pageConcentrations());
	}

	@Override
	@PreAuthorize("checkPermission('Concentrations')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> majors = new ArrayList<ListItem>();
		for (PosMajor major: PosMajorDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			AcademicArea area = null;
			for (AcademicArea a: major.getAcademicAreas()) {
				area = a; break;
			}
			majors.add(new ListItem(major.getUniqueId().toString(), (area != null ? area.getAcademicAreaAbbreviation() + "/": "") + major.getCode() + " - " + major.getName()));
		}
		Collections.sort(majors);
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldExternalId(), FieldType.text, 120, 40, Flag.READ_ONLY),
				new Field(MESSAGES.fieldCode(), FieldType.text, 120, 40, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 100, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldMajor(), FieldType.list, 300, majors));
		data.setSortBy(3,1,2);
		for (PosMajorConcentration conc: PosMajorConcentration.findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(conc.getUniqueId());
			r.setField(0, conc.getExternalUniqueId());
			r.setField(1, conc.getCode());
			r.setField(2, conc.getName());
			r.setDeletable(conc.getExternalUniqueId() == null);
			r.setField(3, conc.getMajor().getUniqueId().toString(), false);
		}
		data.setEditable(context.hasPermission(Right.ConcentrationEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('ConcentrationEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (PosMajorConcentration conc: PosMajorConcentration.findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(conc.getUniqueId());
			if (r == null)
				delete(conc, context, hibSession);
			else
				update(conc, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('ConcentrationEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		PosMajorConcentration conc = new PosMajorConcentration();
		conc.setExternalUniqueId(record.getField(0));
		conc.setCode(record.getField(1));
		conc.setName(record.getField(2));
		PosMajor major = PosMajorDAO.getInstance().get(Long.valueOf(record.getField(3)), hibSession); 
		conc.setMajor(major); major.getConcentrations().add(conc);
		record.setUniqueId((Long)hibSession.save(conc));
		ChangeLog.addChange(hibSession,
				context,
				conc,
				conc.getCode() + " " + conc.getName(),
				Source.SIMPLE_EDIT,
				Operation.CREATE,
				null,
				null);
	}

	protected void update(PosMajorConcentration conc, Record record, SessionContext context, Session hibSession) {
		if (conc == null) return;
		if (!ToolBox.equals(conc.getExternalUniqueId(), record.getField(0)) ||
				!ToolBox.equals(conc.getCode(), record.getField(1)) ||
				!ToolBox.equals(conc.getName(), record.getField(2))) {
			conc.setExternalUniqueId(record.getField(0));
			conc.setCode(record.getField(1));
			conc.setName(record.getField(2));
			hibSession.saveOrUpdate(conc);
			ChangeLog.addChange(hibSession,
					context,
					conc,
					conc.getCode() + " " + conc.getName(),
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);
		}
	}
	
	@Override
	@PreAuthorize("checkPermission('ConcentrationEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(PosMajorConcentrationDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(PosMajorConcentration conc, SessionContext context, Session hibSession) {
		if (conc == null) return;
		ChangeLog.addChange(hibSession,
				context,
				conc,
				conc.getCode() + " " + conc.getName(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		conc.getMajor().getConcentrations().remove(conc);
		hibSession.remove(conc);		
	}

	@Override
	@PreAuthorize("checkPermission('ConcentrationEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(PosMajorConcentrationDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
