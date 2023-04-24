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
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.dao.SponsoringOrganizationDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=sponsoringOrganizations]")
public class SponsoringOrganizations implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageSponsoringOrganization(), MESSAGES.pageSponsoringOrganizations());
	}

	@Override
	@PreAuthorize("checkPermission('SponsoringOrganizations')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldName(), FieldType.text, 350, 100, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldEmailAddress(), FieldType.text, 350, 200)
				);
		data.setSortBy(1);
		
		for (SponsoringOrganization sponsor: SponsoringOrganization.findAll()) {
			Record r = data.addRecord(sponsor.getUniqueId());
			boolean canEdit = context.hasPermission(sponsor, Right.SponsoringOrganizationEdit);
			r.setField(0, sponsor.getName(), canEdit);
			r.setField(1, sponsor.getEmail(), canEdit);
			r.setDeletable(context.hasPermission(sponsor, Right.SponsoringOrganizationDelete));
		}
		data.setAddable(context.hasPermission(Right.SponsoringOrganizationAdd));
		return data;
	}
	
	@Override
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (SponsoringOrganization sponsor: SponsoringOrganization.findAll()) {
			Record r = data.getRecord(sponsor.getUniqueId());
			if (r == null)
				delete(sponsor, context, hibSession);
			else
				update(sponsor, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);		
	}
	
	@Override
	public void save(Record record, SessionContext context, Session hibSession) {
		context.checkPermission(Right.SponsoringOrganizationAdd);
		SponsoringOrganization sponsor = new SponsoringOrganization();
		sponsor.setName(record.getField(0));
		sponsor.setEmail(record.getField(1));
		record.setUniqueId((Long)hibSession.save(sponsor));
		ChangeLog.addChange(hibSession,
				context,
				sponsor,
				sponsor.getName(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(SponsoringOrganization sponsor, Record record, SessionContext context, Session hibSession) {
		if (sponsor == null) return;
		context.checkPermission(sponsor, Right.SponsoringOrganizationEdit);
		if (ToolBox.equals(sponsor.getName(), record.getField(0)) &&
			ToolBox.equals(sponsor.getEmail(), record.getField(1))) return;
		sponsor.setName(record.getField(0));
		sponsor.setEmail(record.getField(1));
		hibSession.saveOrUpdate(sponsor);
		ChangeLog.addChange(hibSession,
				context,
				sponsor,
				sponsor.getName(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	public void update(Record record, SessionContext context, Session hibSession) {
		update(SponsoringOrganizationDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(SponsoringOrganization sponsor, SessionContext context, Session hibSession) {
		if (sponsor == null) return;
		context.checkPermission(sponsor, Right.SponsoringOrganizationDelete);
		for (Event event: hibSession.createQuery("from Event where sponsoringOrganization.uniqueId = :orgId", Event.class).setParameter("orgId", sponsor.getUniqueId(), Long.class).list()) {
    		event.setSponsoringOrganization(null);
    		hibSession.update(event);
    	}
		ChangeLog.addChange(hibSession,
				context,
				sponsor,
				sponsor.getName(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(sponsor);
	}
	
	@Override
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(SponsoringOrganizationDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}


}