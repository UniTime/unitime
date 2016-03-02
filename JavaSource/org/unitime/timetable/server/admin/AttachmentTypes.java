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
import org.unitime.timetable.model.AttachmentType;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.AttachmentTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=attachmentType]")
public class AttachmentTypes implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageAttachmentType(), MESSAGES.pageAttachmentTypes());
	}

	@Override
	@PreAuthorize("checkPermission('AttachmentTypes')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 160, 20, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldAbbreviation(), FieldType.text, 160, 20, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 60, Flag.UNIQUE, Flag.NOT_EMPTY),
				new Field(MESSAGES.attachmentFlagIsImage(), FieldType.toggle, 40),
				new Field(MESSAGES.attachmentTypeFlagRoomPicture(), FieldType.toggle, 40),
				new Field(MESSAGES.attachmentTypeFlagShowRoomsTable(), FieldType.toggle, 40),
				new Field(MESSAGES.attachmentTypeFlagShowRoomTooltip(), FieldType.toggle, 40)
				);
		data.setSortBy(1);
		for (AttachmentType atype: AttachmentTypeDAO.getInstance().findAll()) {
			Record r = data.addRecord(atype.getUniqueId());
			r.setField(0, atype.getReference());
			r.setField(1, atype.getAbbreviation());
			r.setField(2, atype.getLabel());
			r.setField(3, AttachmentType.VisibilityFlag.IS_IMAGE.in(atype.getVisibility()) ? "true" : "false");
			r.setField(4, AttachmentType.VisibilityFlag.ROOM_PICTURE_TYPE.in(atype.getVisibility()) ? "true" : "false");
			r.setField(5, AttachmentType.VisibilityFlag.SHOW_ROOMS_TABLE.in(atype.getVisibility()) ? "true" : "false");
			r.setField(6, AttachmentType.VisibilityFlag.SHOW_ROOM_TOOLTIP.in(atype.getVisibility()) ? "true" : "false");
			int used =
					((Number)hibSession.createQuery(
							"select count(p) from LocationPicture p where p.type.uniqueId = :uniqueId")
							.setLong("uniqueId", atype.getUniqueId()).uniqueResult()).intValue();
			r.setDeletable(used == 0);
		}
		data.setEditable(context.hasPermission(Right.AttachmentTypeEdit));
		return data;
	}
	
	protected int getVisibility(Record record) {
		int flags = 0;
		if ("true".equals(record.getField(3))) flags = AttachmentType.VisibilityFlag.IS_IMAGE.set(flags);
		if ("true".equals(record.getField(4))) flags = AttachmentType.VisibilityFlag.ROOM_PICTURE_TYPE.set(flags);
		if ("true".equals(record.getField(5))) flags = AttachmentType.VisibilityFlag.SHOW_ROOMS_TABLE.set(flags);
		if ("true".equals(record.getField(6))) flags = AttachmentType.VisibilityFlag.SHOW_ROOM_TOOLTIP.set(flags);
		return flags;
	}

	@Override
	@PreAuthorize("checkPermission('AttachmentTypeEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (AttachmentType type: AttachmentTypeDAO.getInstance().findAll(hibSession)) {
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
	@PreAuthorize("checkPermission('AttachmentTypeEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		AttachmentType type = new AttachmentType();
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
	
	protected void update(AttachmentType type, Record record, SessionContext context, Session hibSession) {
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
	@PreAuthorize("checkPermission('AttachmentTypeEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(AttachmentTypeDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(AttachmentType type, SessionContext context, Session hibSession) {
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
	@PreAuthorize("checkPermission('AttachmentTypeEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(AttachmentTypeDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}


}
