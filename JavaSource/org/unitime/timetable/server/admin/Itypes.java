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
import java.util.Set;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.ItypeDescDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@Service("gwtAdminTable[type=itype]")
public class Itypes implements AdminTable {
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageInstructionalType(), MESSAGES.pageInstructionalTypes());
	}

	@Override
	@PreAuthorize("checkPermission('InstructionalTypes')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> baseTypes = new ArrayList<ListItem>();
		baseTypes.add(new ListItem("", "-"));
		Set<ItypeDesc> itypes = ItypeDesc.findAll(false);
		for (ItypeDesc itype: itypes)
			if (itype.isBasic())
				baseTypes.add(new ListItem(itype.getItype().toString(), itype.getDesc()));
		List<ListItem> types = new ArrayList<ListItem>();
		types.add(new ListItem("true", MSG.itypeBasic()));
		types.add(new ListItem("false", MSG.itypeExtended()));
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MSG.fieldIType(), FieldType.number, 25, 2, Flag.NOT_EMPTY, Flag.UNIQUE),
				new Field(MSG.fieldAbbreviation(), FieldType.text, 70, 7, Flag.NOT_EMPTY, Flag.UNIQUE),
				new Field(MSG.fieldName(), FieldType.text, 250, 50, Flag.NOT_EMPTY, Flag.UNIQUE),
				new Field(MSG.fieldReference(), FieldType.text, 100, 20, Flag.NOT_EMPTY),
				new Field(MSG.fieldType(), FieldType.list, 100, types, Flag.NOT_EMPTY).withDefault("true"),
				new Field(MSG.fieldParent(), FieldType.list, 100, baseTypes),
				new Field(MSG.fieldOrganized(), FieldType.toggle, 50).withDefault("true"));
		data.setSortBy(0);
		data.setEditable(false);
		data.setAddable(context.hasPermission(Right.InstructionalTypeAdd));
		for (ItypeDesc itype: itypes) {
			boolean edit = context.hasPermission(itype, Right.InstructionalTypeEdit);
			if (edit) data.setEditable(true);
			Record r = data.addRecord(itype.getItype().longValue());
			r.setField(0, itype.getItype().toString(), false);
			r.setField(1, itype.getAbbv(), edit);
			r.setField(2, itype.getDesc(), edit);
			r.setField(3, itype.getSis_ref(), edit);
			r.setField(4, itype.isBasic() ? "true" : "false", edit);
			r.setField(5, itype.getParent() == null ? "" : itype.getParent().getItype().toString(), edit);
			r.setField(6, itype.isOrganized() ? "true" : "false", edit);
			r.setDeletable(context.hasPermission(itype, Right.InstructionalTypeDelete));
		}
		return data;
	}

	@Override
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (ItypeDesc itype: ItypeDesc.findAll(false)) {
			Record r = data.getRecord(itype.getItype().longValue());
			if (r == null) {
				context.checkPermission(itype, Right.InstructionalTypeDelete);
				delete(itype, context, hibSession);
			} else {
				context.checkPermission(itype, Right.InstructionalTypeEdit);
				update(itype, r, context, hibSession);
			}
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	public void save(Record record, SessionContext context, Session hibSession) {
		context.checkPermission(Right.InstructionalTypeAdd);
		ItypeDesc itype = new ItypeDesc();
		itype.setItype(Integer.valueOf(record.getField(0)));
		itype.setAbbv(record.getField(1));
		itype.setDesc(record.getField(2));
		itype.setSis_ref(record.getField(3));
		itype.setBasic(!"false".equals(record.getField(4)));
		itype.setParent(record.getField(5) == null || record.getField(5).isEmpty() ? null : ItypeDescDAO.getInstance().get(Integer.valueOf(record.getField(5))));
		if (itype.getBasic() && itype.getParent() != null)
			throw new GwtRpcException(MSG.errorItypeBasicCannotHaveParent());
		if (!itype.getBasic() && itype.getParent() == null)
			throw new GwtRpcException(MSG.errorItypeExtendedMustHaveParent());
		if (itype.getParent() != null && !itype.getParent().isBasic())
			throw new GwtRpcException(MSG.errorItypeParentMustBeBasic());
		itype.setOrganized("true".equals(record.getField(6)));
		hibSession.persist(itype);
		record.setUniqueId(itype.getItype().longValue());
		ChangeLog.addChange(hibSession,
				context,
				itype,
				itype.getAbbv() + " - " + itype.getDesc(),
				Source.SIMPLE_EDIT,
				Operation.CREATE,
				null,
				null);
	}

	@Override
	public void update(Record record, SessionContext context, Session hibSession) {
		update(ItypeDescDAO.getInstance().get(record.getUniqueId().intValue()), record, context, hibSession);
	}
	
	public void update(ItypeDesc itype, Record record, SessionContext context, Session hibSession) {
		if (itype == null) return;
		if (ToolBox.equals(itype.getAbbv(), record.getField(1)) &&
			ToolBox.equals(itype.getDesc(), record.getField(2)) &&
			ToolBox.equals(itype.getSis_ref(), record.getField(3)) &&
			ToolBox.equals(itype.getBasic() ? "true" : "false", record.getField(4)) &&
			ToolBox.equals(itype.getParent() == null ? "" : itype.getParent().getItype().toString(), record.getField(5)) &&
			ToolBox.equals(itype.getOrganized() ? "true" : "false", record.getField(6)))
			return;
		context.checkPermission(itype, Right.InstructionalTypeEdit);
		itype.setAbbv(record.getField(1));
		itype.setDesc(record.getField(2));
		itype.setSis_ref(record.getField(3));
		itype.setBasic(!"false".equals(record.getField(4)));
		itype.setParent(record.getField(5) == null || record.getField(5).isEmpty() ? null : ItypeDescDAO.getInstance().get(Integer.valueOf(record.getField(5))));
		if (itype.getBasic() && itype.getParent() != null)
			throw new GwtRpcException(MSG.errorItypeBasicCannotHaveParent());
		if (!itype.getBasic() && itype.getParent() == null)
			throw new GwtRpcException(MSG.errorItypeExtendedMustHaveParent());
		if (itype.getParent() != null && !itype.getParent().isBasic())
			throw new GwtRpcException(MSG.errorItypeParentMustBeBasic());
		itype.setOrganized("true".equals(record.getField(6)));
		hibSession.persist(itype);
		record.setUniqueId(itype.getItype().longValue());
		ChangeLog.addChange(hibSession,
				context,
				itype,
				itype.getAbbv() + " - " + itype.getDesc(),
				Source.SIMPLE_EDIT,
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(ItypeDescDAO.getInstance().get(record.getUniqueId().intValue()), context, hibSession);
	}
	
	public void delete(ItypeDesc itype, SessionContext context, Session hibSession) {
		context.checkPermission(itype, Right.InstructionalTypeDelete);
		ChangeLog.addChange(hibSession,
				context,
				itype,
				itype.getAbbv() + " - " + itype.getDesc(),
				Source.SIMPLE_EDIT,
				Operation.DELETE,
				null,
				null);
		hibSession.remove(itype);
	}

}
