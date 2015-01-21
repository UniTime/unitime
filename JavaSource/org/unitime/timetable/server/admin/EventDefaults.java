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
import java.util.List;

import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.RoomTypeOption;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=eventDefault]")
public class EventDefaults implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageEventDefault(), MESSAGES.pageEventDefaults());
	}

	@Override
	@PreAuthorize("checkPermission('EventDefaults')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> states = new ArrayList<ListItem>();
		for (RoomTypeOption.Status state: RoomTypeOption.Status.values()) {
			states.add(new ListItem(String.valueOf(state.ordinal()), state.toString()));
		}
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldManager(), FieldType.text, 100, Flag.READ_ONLY),
				new Field(MESSAGES.fieldReference(), FieldType.text, 100, Flag.READ_ONLY, Flag.HIDDEN),
				new Field(MESSAGES.fieldAdditionalEmails(), FieldType.textarea, 50, 3, 4000)
				);
		data.setSortBy(0);
		data.setAddable(false);
		if (context.hasPermission(Right.EventDefaultsEditOther)) {
			for (TimetableManager manager: TimetableManagerDAO.getInstance().findAll()) {
				
				if (!context.hasPermission(Right.SessionIndependent)) {
					boolean hasDepartment = false;
					for (Department dept: manager.getDepartments()) {
						if (context.getUser().getCurrentAuthority().hasQualifier(dept)) { hasDepartment = true; break; }
					}
					if (!hasDepartment) continue;
				}
				
				boolean hasRole = false;
				for (ManagerRole role: manager.getManagerRoles()) {
					if (role.getRole().hasRight(Right.EventDefaults)) { hasRole = true; break; }
				}
				if (!hasRole) continue;

				Record r = data.addRecord(manager.getUniqueId(), false);
				r.setField(0, manager.getName(), false);
				r.setField(1, manager.getExternalUniqueId(), false);
				r.setField(2, UserData.getProperty(manager.getExternalUniqueId(), "Defaults[AddEvent.emails]", ""));
			}
		} else {
			data.getFields()[0] = new Field(MESSAGES.fieldManager(), FieldType.text, 100, Flag.READ_ONLY, Flag.HIDDEN);
			TimetableManager manager = TimetableManager.findByExternalId(context.getUser().getExternalUserId());
			if (manager != null) {
				Record r = data.addRecord(manager.getUniqueId(), false);
				r.setField(0, manager.getName(), false);
				r.setField(1, manager.getExternalUniqueId(), false);
				r.setField(2, UserData.getProperty(manager.getExternalUniqueId(), "Defaults[AddEvent.emails]", ""));
			}
		}
		data.setEditable(true);
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('EventDefaults')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (TimetableManager manager: TimetableManagerDAO.getInstance().findAll()) {
			Record r = data.getRecord(manager.getUniqueId());
			if (r != null)
				update(r, context, hibSession);
		}
	}

	@Override
	@PreAuthorize("checkPermission('HasRole')")
	public void save(Record record, SessionContext context, Session hibSession) {
		throw new GwtRpcException(MESSAGES.errorOperationNotSupported());
	}
	
	@Override
	@PreAuthorize("checkPermission('EventDefaults')")
	public void update(Record record, SessionContext context, Session hibSession) {
		String extId = record.getField(1);
		if (!context.getUser().getExternalUserId().equals(extId))
			context.checkPermission(Right.EventDefaultsEditOther);
		UserData.setProperty(extId, "Defaults[AddEvent.emails]", record.getField(2));
	}

	@Override
	@PreAuthorize("checkPermission('HasRole')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		throw new GwtRpcException(MESSAGES.errorOperationNotSupported());
	}
	

}
