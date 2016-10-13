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
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.RolesDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=roles]")
public class UserRoles implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageRole(), MESSAGES.pageRoles());
	}

	@Override
	@PreAuthorize("checkPermission('Roles')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 160, 20, Flag.UNIQUE),
				new Field(MESSAGES.fieldName(), FieldType.text, 250, 40, Flag.UNIQUE),
				new Field(MESSAGES.fieldInstructor(), FieldType.toggle, 40),
				new Field(MESSAGES.fieldEnabled(), FieldType.toggle, 40),
				new Field(MESSAGES.fieldSortOrder(), FieldType.text, 80, 10, Flag.READ_ONLY, Flag.HIDDEN)
				);
		data.setSortBy(4);
		int idx = 0;
		for (Roles role: Roles.findAll(false)) {
			Record r = data.addRecord(role.getRoleId(), (role.isManager() || role.isInstructor()) && !role.isUsed());
			r.setField(0, role.getReference(), role.isManager() || role.isInstructor());
			r.setField(1, role.getAbbv());
			r.setField(2, role.isInstructor() ? "true" : "false");
			r.setField(3, role.isEnabled() ? "true" : "false");
			r.setField(4, String.valueOf(idx++));
		}
		data.setEditable(context.hasPermission(Right.RoleEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('RoleEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (Roles role: RolesDAO.getInstance().findAll()) {
			Record r = data.getRecord(role.getRoleId());
			if (r == null) {
				delete(role, context, hibSession);
			} else
				update(role, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('RoleEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		Roles role = new Roles();
		role.setReference(record.getField(0));
		role.setAbbv(record.getField(1));
		role.setInstructor("true".equals(record.getField(2)));
		role.setEnabled("true".equals(record.getField(3)));
		role.setManager(true);
		record.setUniqueId((Long)hibSession.save(role));
		ChangeLog.addChange(hibSession,
				context,
				role,
				role.getAbbv() + " role",
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);		
	}

	protected void update(Roles role, Record record, SessionContext context, Session hibSession) {
		if (role == null) return;
		if (ToolBox.equals(role.getReference(), record.getField(0)) &&
				ToolBox.equals(role.getAbbv(), record.getField(1)) &&
				ToolBox.equals(role.isInstructor(), "true".equals(record.getField(2))) &&
				ToolBox.equals(role.isEnabled(), "true".equals(record.getField(3)))) return;
			role.setReference(record.getField(0));
			role.setAbbv(record.getField(1));
			role.setInstructor("true".equals(record.getField(2)));
			role.setEnabled("true".equals(record.getField(3)));
			hibSession.saveOrUpdate(role);
			ChangeLog.addChange(hibSession,
					context,
					role,
					role.getAbbv() + " role",
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);
	}
	
	@Override
	@PreAuthorize("checkPermission('RoleEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(RolesDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(Roles role, SessionContext context, Session hibSession) {
		if (role == null) return;
		if (!role.isManager())
			throw new PageAccessException(MESSAGES.failedDeleteRole(role.getAbbv()));
		ChangeLog.addChange(hibSession,
				context,
				role,
				role.getAbbv() + " role",
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(role);
	}

	@Override
	@PreAuthorize("checkPermission('RoleEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(RolesDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
