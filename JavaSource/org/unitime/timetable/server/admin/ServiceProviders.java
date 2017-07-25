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
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.EventServiceProviderDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=serviceProviders]")
public class ServiceProviders implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageServiceProvider(), MESSAGES.pageServiceProviders());
	}

	@Override
	@PreAuthorize("checkPermission('EventServiceProviders')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldAbbreviation(), FieldType.text, 160, 20, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 60, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldMessage(), FieldType.textarea, 50, 3, 1000),
				new Field(MESSAGES.fieldEmailAddress(), FieldType.text, 300, 200)
				);
		data.setSortBy(1);
		for (EventServiceProvider provider: EventServiceProviderDAO.getInstance().findAll()) {
			Record r = data.addRecord(provider.getUniqueId());
			r.setField(0, provider.getReference());
			r.setField(1, provider.getLabel());
			r.setField(2, provider.getNote());
			r.setField(3, provider.getEmail());
			r.setDeletable(!provider.isUsed());
		}
		data.setEditable(context.hasPermission(Right.EventServiceProviderEdit));
		return data;
	}
	
	@Override
	@PreAuthorize("checkPermission('EventServiceProviderEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (EventServiceProvider provider: EventServiceProviderDAO.getInstance().findAll(hibSession)) {
			Record r = data.getRecord(provider.getUniqueId());
			if (r == null)
				delete(provider, context, hibSession);
			else
				update(provider, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);		
	}
	
	protected static Integer options(Record record) {
		int ret = 0;
		return ret;
	}

	@Override
	@PreAuthorize("checkPermission('EventServiceProviderEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		EventServiceProvider provider = new EventServiceProvider();
		provider.setReference(record.getField(0));
		provider.setLabel(record.getField(1));
		provider.setNote(record.getField(2));
		provider.setEmail(record.getField(3));
		provider.setOptions(options(record));
		record.setUniqueId((Long)hibSession.save(provider));
		ChangeLog.addChange(hibSession,
				context,
				provider,
				provider.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(EventServiceProvider provider, Record record, SessionContext context, Session hibSession) {
		if (provider == null) return;
		if (ToolBox.equals(provider.getReference(), record.getField(0)) &&
			ToolBox.equals(provider.getLabel(), record.getField(1)) &&
			ToolBox.equals(provider.getNote(), record.getField(2)) &&
			ToolBox.equals(provider.getEmail(), record.getField(3)) &&
			ToolBox.equals(provider.getOptions(), options(record))) return;
		provider.setReference(record.getField(0));
		provider.setLabel(record.getField(1));
		provider.setNote(record.getField(2));
		provider.setEmail(record.getField(3));
		provider.setOptions(options(record));
		hibSession.saveOrUpdate(provider);
		ChangeLog.addChange(hibSession,
				context,
				provider,
				provider.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('EventServiceProviderEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(EventServiceProviderDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(EventServiceProvider provider, SessionContext context, Session hibSession) {
		if (provider == null) return;
		ChangeLog.addChange(hibSession,
				context,
				provider,
				provider.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(provider);
	}
	
	@Override
	@PreAuthorize("checkPermission('EventServiceProviderEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(EventServiceProviderDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}


}
