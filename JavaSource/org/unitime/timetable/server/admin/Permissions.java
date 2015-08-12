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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
@Service("gwtAdminTable[type=permissions]")
public class Permissions implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pagePermission(), MESSAGES.pagePermissions());
	}
	
	protected List<Roles> getRoles(SessionContext context, Session hibSession, boolean read) {
		if (read) {
			List<Roles> roles = new ArrayList<Roles>(Roles.findAll(false));
			List<Long> roleIds = new ArrayList<Long>();
			for (Roles role: roles)
				roleIds.add(role.getRoleId());
			context.setAttribute("Permissions.roleIds", roleIds);
			return roles;
		} else {
			List<Long> roleIds = (List<Long>)context.getAttribute("Permissions.roleIds");
			if (roleIds == null)
				return getRoles(context, hibSession, true);
			List<Roles> roles = new ArrayList<Roles>(roleIds.size());
			for (Long roleId: roleIds)
				roles.add(RolesDAO.getInstance().get(roleId, hibSession));
			return roles;
		}
	}

	@Override
	@PreAuthorize("checkPermission('Permissions')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<Roles> roles = getRoles(context, hibSession, true);
		Field[] fields = new Field[2 + roles.size()];
		fields[0] = new Field(MESSAGES.fieldName(), FieldType.text, 160, 200, Flag.READ_ONLY);
		fields[1] = new Field(MESSAGES.fieldLevel(), FieldType.text, 160, 200, Flag.READ_ONLY);
		for (int i = 0; i < roles.size(); i++) {
			fields[2 + i] = new Field(roles.get(i).getReference(), FieldType.toggle, 40, roles.get(i).isEnabled() ? null : Flag.HIDDEN);
		}
		SimpleEditInterface data = new SimpleEditInterface(fields);
		data.setSortBy(-1);
		data.setAddable(false);
		for (Right right: Right.values()) {
			Record r = data.addRecord((long)right.ordinal(), false);
			r.setField(0, right.toString(), false);
			r.setField(1, right.hasType() ? right.type().getSimpleName().replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2").replace("_", " ") : MESSAGES.levelGlobal(), false);
			for (int i = 0; i < roles.size(); i++)
				r.setField(2 + i, roles.get(i).getRights().contains(right.name()) ? "true" : "false");
		}
		data.setEditable(context.hasPermission(Right.PermissionEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('PermissionEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		List<Roles> roles = getRoles(context, hibSession, false);
		Set<Roles> changed = new HashSet<Roles>();
		for (Record r: data.getRecords()) {
			Right right = Right.values()[(int)r.getUniqueId().longValue()];
			for (int i = 0; i < roles.size(); i++) {
				Roles role = roles.get(i);
				if (role == null) continue;
				boolean newValue = "true".equals(r.getField(2 + i));
				boolean oldValue = role.getRights().contains(right.name());
				if (newValue != oldValue) {
					changed.add(role);
					if (newValue) role.getRights().add(right.name());
					else role.getRights().remove(right.name());
				}
			}
		}
		for (Roles role: changed) {
			hibSession.saveOrUpdate(role);
			ChangeLog.addChange(hibSession,
					context,
					role,
					role.getAbbv() + " permissions",
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);
		}
	}

	@Override
	@PreAuthorize("checkPermission('PermissionEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		throw new GwtRpcException(MESSAGES.errorOperationNotSupported());
	}

	@Override
	@PreAuthorize("checkPermission('PermissionEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		List<Roles> roles = getRoles(context, hibSession, false);
		Set<Roles> changed = new HashSet<Roles>();
		Right right = Right.values()[(int)record.getUniqueId().longValue()];
		for (int i = 0; i < roles.size(); i++) {
			Roles role = roles.get(i);
			if (role == null) continue;
			boolean newValue = "true".equals(record.getField(2 + i));
			boolean oldValue = role.getRights().contains(right.name());
			if (newValue != oldValue) {
				changed.add(role);
				if (newValue) role.getRights().add(right.name());
				else role.getRights().remove(right.name());
			}
		}
		for (Roles role: changed) {
			hibSession.saveOrUpdate(role);
			ChangeLog.addChange(hibSession,
					context,
					role,
					role.getAbbv() + " permissions",
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);
		}
	}

	@Override
	@PreAuthorize("checkPermission('PermissionEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		throw new GwtRpcException(MESSAGES.errorOperationNotSupported());
	}
}
