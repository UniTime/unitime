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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.dao.RoomTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=roomType]")
public class RoomTypes implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageRoomType(), MESSAGES.pageRoomTypes());
	}

	@Override
	@PreAuthorize("checkPermission('RoomTypes')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> types = new ArrayList<ListItem>();
		types.add(new ListItem("room", MESSAGES.typeRoomClass()));
		types.add(new ListItem("other", MESSAGES.typeNonUniversityLocationClass()));
		
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 140, 20, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldLabel(), FieldType.text, 400, 60, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldRoomClassType(), FieldType.list, 120, types, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldSortOrder(), FieldType.number, 80, 10),
				new Field(MESSAGES.fieldNbrRooms(), FieldType.number, 80, 10, Flag.READ_ONLY, Flag.NO_DETAIL)
				);
		data.setSortBy(3, 1);
		
		for (RoomType type: RoomType.findAll()) {
			Record r = data.addRecord(type.getUniqueId());
			boolean canEdit = context.hasPermission(Right.RoomTypes);
			r.setField(0, type.getReference(), canEdit);
			r.setField(1, type.getLabel(), canEdit);
			int nbrRooms = type.countRooms();
			r.setField(2, type.isRoom() ? "room" : "other", canEdit && (nbrRooms == 0));
			r.setField(3, type.getOrd() == null ? "" : String.valueOf(type.getOrd()), canEdit);
			r.setField(4, String.valueOf(nbrRooms), false);
			r.setDeletable(canEdit && nbrRooms == 0);
		}
		data.setAddable(context.hasPermission(Right.RoomTypes));
		data.setEditable(context.hasPermission(Right.RoomTypes));
		return data;
	}
	
	protected int nextOrd(Set<Integer> ords) {
		for (int i = 0; i < ords.size() + 1; i++) {
			if (!ords.contains(i)) {
				ords.add(i);
				return i;
			}
		}
		return ords.size();
	}
	
	protected int nextOrd() {
		Set<RoomType> types = RoomType.findAll();
		int idx = 0;
		while (true) {
			for (RoomType t: types) {
				if (idx == t.getOrd()) { idx++; continue; }
			}
			return idx;
		}
	}
	
	@Override
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		Set<Integer> ords = new HashSet<Integer>();
		List<Record> missing = new ArrayList<Record>();
		for (Record r: data.getRecords()) {
			try {
				ords.add(Integer.parseInt(r.getField(3)));
			} catch (Exception e) {
				if (!r.isEmpty()) missing.add(r);
			}
		}
		for (Record r: missing) {
			r.setField(3, String.valueOf(nextOrd(ords)));
		}
		for (RoomType type: RoomType.findAll()) {
			Record r = data.getRecord(type.getUniqueId());
			if (r == null)
				delete(type, context, hibSession);
			else
				update(type, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);		
	}
	
	@Override
	public void save(Record record, SessionContext context, Session hibSession) {
		context.checkPermission(Right.RoomTypes);
		RoomType type = new RoomType();
		type.setReference(record.getField(0));
		type.setLabel(record.getField(1));
		type.setRoom("room".equals(record.getField(2)));
		try {
			type.setOrd(Integer.valueOf(record.getField(3)));
		} catch (Exception e) {
			type.setOrd(nextOrd());
			record.setField(3, type.getOrd().toString());
		}
		record.setUniqueId((Long)hibSession.save(type));
		ChangeLog.addChange(hibSession,
				context,
				type,
				type.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(RoomType type, Record record, SessionContext context, Session hibSession) {
		if (type == null) return;
		context.checkPermission(Right.RoomTypes);
		if (ToolBox.equals(type.getOrd(), Integer.valueOf(record.getField(3))) &&
			ToolBox.equals(type.getReference(), record.getField(0)) &&
			ToolBox.equals(type.getLabel(), record.getField(1)) &&
			ToolBox.equals(type.isRoom() ? "room" : "other", record.getField(2))) return;
		type.setReference(record.getField(0));
		type.setLabel(record.getField(1));
		type.setRoom("room".equals(record.getField(2)));
		try {
			type.setOrd(Integer.valueOf(record.getField(3)));
		} catch (Exception e) {
			type.setOrd(nextOrd());
			record.setField(3, type.getOrd().toString());
		}
		hibSession.saveOrUpdate(type);
		ChangeLog.addChange(hibSession,
				context,
				type,
				type.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	public void update(Record record, SessionContext context, Session hibSession) {
		update(RoomTypeDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(RoomType type, SessionContext context, Session hibSession) {
		context.checkPermission(Right.RoomTypes);
		ChangeLog.addChange(hibSession,
				context,
				type,
				type.getReference(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(type);
	}
	
	@Override
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(RoomTypeDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}


}