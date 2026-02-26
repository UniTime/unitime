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
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.StandardSchedulingDisclaimer;
import org.unitime.timetable.model.dao.StandardSchedulingDisclaimerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=stdSchdDisclaimers]")
public class StandardSchedulingDisclaimers implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageStandardSchedulingDisclaimer(), MESSAGES.pageStandardSchedulingDisclaimers());
	}

	@Override
	@PreAuthorize("checkPermission('SchedulingDisclaimers')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 160, 20, Flag.UNIQUE),
				new Field(MESSAGES.fieldLabel(), FieldType.text, 300, 60, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldDisclaimer(), FieldType.textarea, 50, 3, 2000, Flag.NOT_EMPTY)
				);
		data.setSortBy(1);
		for (StandardSchedulingDisclaimer disc: StandardSchedulingDisclaimerDAO.getInstance().findAll()) {
			Record r = data.addRecord(disc.getUniqueId());
			r.setField(0, disc.getReference());
			r.setField(1, disc.getLabel());
			r.setField(2, disc.getDisclaimer());
		}
		data.setEditable(context.hasPermission(Right.SchedulingDisclaimerEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('SchedulingDisclaimerEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (StandardSchedulingDisclaimer disc: StandardSchedulingDisclaimerDAO.getInstance().findAll(hibSession)) {
			Record r = data.getRecord(disc.getUniqueId());
			if (r == null)
				delete(disc, context, hibSession);
			else
				update(disc, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);		
	}

	@Override
	@PreAuthorize("checkPermission('SchedulingDisclaimerEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		StandardSchedulingDisclaimer disc = new StandardSchedulingDisclaimer();
		disc.setReference(record.getField(0));
		disc.setLabel(record.getField(1));
		disc.setDisclaimer(record.getField(2));
		hibSession.persist(disc);
		record.setUniqueId(disc.getUniqueId());
		ChangeLog.addChange(hibSession,
				context,
				disc,
				disc.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(StandardSchedulingDisclaimer disc, Record record, SessionContext context, Session hibSession) {
		if (disc == null) return;
		if (ToolBox.equals(disc.getReference(), record.getField(0)) &&
				ToolBox.equals(disc.getLabel(), record.getField(1)) &&
				ToolBox.equals(disc.getDisclaimer(), record.getField(2))) return;
		disc.setReference(record.getField(0));
		disc.setLabel(record.getField(1));
		disc.setDisclaimer(record.getField(2));
		hibSession.merge(disc);
		ChangeLog.addChange(hibSession,
				context,
				disc,
				disc.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('SchedulingDisclaimerEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(StandardSchedulingDisclaimerDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(StandardSchedulingDisclaimer disc, SessionContext context, Session hibSession) {
		if (disc == null) return;
		ChangeLog.addChange(hibSession,
				context,
				disc,
				disc.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.remove(disc);
	}
	
	@Override
	@PreAuthorize("checkPermission('SchedulingDisclaimerEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(StandardSchedulingDisclaimerDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}


}
