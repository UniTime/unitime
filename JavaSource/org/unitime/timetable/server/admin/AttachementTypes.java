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
import org.unitime.timetable.model.AttachementType;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.AttachementTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=attachementType]")
public class AttachementTypes implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageAttachementType(), MESSAGES.pageAttachementTypes());
	}

	@Override
	@PreAuthorize("checkPermission('AttachementTypes')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 160, 20, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldAbbreviation(), FieldType.text, 160, 20, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 60, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.attachementFlagIsImage(), FieldType.toggle, 40),
				new Field(MESSAGES.attachementTypeFlagRoomPicture(), FieldType.toggle, 40),
				new Field(MESSAGES.attachementTypeFlagShowRoomsTable(), FieldType.toggle, 40),
				new Field(MESSAGES.attachementTypeFlagShowRoomTooltip(), FieldType.toggle, 40)
				);
		data.setSortBy(1);
		for (AttachementType atype: AttachementTypeDAO.getInstance().findAll()) {
			Record r = data.addRecord(atype.getUniqueId());
			r.setField(0, atype.getReference());
			r.setField(1, atype.getAbbreviation());
			r.setField(2, atype.getLabel());
			r.setField(3, AttachementType.VisibilityFlag.IS_IMAGE.in(atype.getVisibility()) ? "true" : "false");
			r.setField(4, AttachementType.VisibilityFlag.ROOM_PICTURE_TYPE.in(atype.getVisibility()) ? "true" : "false");
			r.setField(5, AttachementType.VisibilityFlag.SHOW_ROOMS_TABLE.in(atype.getVisibility()) ? "true" : "false");
			r.setField(6, AttachementType.VisibilityFlag.SHOW_ROOM_TOOLTIP.in(atype.getVisibility()) ? "true" : "false");
			int used =
					((Number)hibSession.createQuery(
							"select count(p) from LocationPicture p where p.type.uniqueId = :uniqueId")
							.setLong("uniqueId", atype.getUniqueId()).uniqueResult()).intValue();
			r.setDeletable(used == 0);
		}
		data.setEditable(context.hasPermission(Right.AttachementTypeEdit));
		return data;
	}
	
	protected int getVisibility(Record record) {
		int flags = 0;
		if ("true".equals(record.getField(3))) flags = AttachementType.VisibilityFlag.IS_IMAGE.set(flags);
		if ("true".equals(record.getField(4))) flags = AttachementType.VisibilityFlag.ROOM_PICTURE_TYPE.set(flags);
		if ("true".equals(record.getField(5))) flags = AttachementType.VisibilityFlag.SHOW_ROOMS_TABLE.set(flags);
		if ("true".equals(record.getField(6))) flags = AttachementType.VisibilityFlag.SHOW_ROOM_TOOLTIP.set(flags);
		return flags;
	}

	@Override
	@PreAuthorize("checkPermission('AttachementTypeEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (AttachementType type: AttachementTypeDAO.getInstance().findAll(hibSession)) {
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
	@PreAuthorize("checkPermission('AttachementTypeEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		AttachementType type = new AttachementType();
		type.setReference(record.getField(0));
		type.setAbbreviation(record.getField(1));
		type.setLabel(record.getField(2));
		type.setVisibility(getVisibility(record));
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
	
	protected void update(AttachementType type, Record record, SessionContext context, Session hibSession) {
		if (type == null) return;
		if (ToolBox.equals(type.getReference(), record.getField(0)) &&
			ToolBox.equals(type.getAbbreviation(), record.getField(1)) &&
			ToolBox.equals(type.getLabel(), record.getField(2)) &&
			type.getVisibility() == getVisibility(record)) return;
		type.setReference(record.getField(0));
		type.setAbbreviation(record.getField(1));
		type.setLabel(record.getField(2));
		type.setVisibility(getVisibility(record));
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
	@PreAuthorize("checkPermission('AttachementTypeEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(AttachementTypeDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(AttachementType type, SessionContext context, Session hibSession) {
		if (type == null) return;
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
	@PreAuthorize("checkPermission('AttachementTypeEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(AttachementTypeDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}


}
