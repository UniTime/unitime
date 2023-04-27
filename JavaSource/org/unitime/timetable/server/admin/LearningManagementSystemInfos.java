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
import org.unitime.timetable.model.LearningManagementSystemInfo;
import org.unitime.timetable.model.dao.LearningManagementSystemInfoDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Stephanie Schluttenhofer
 */
@Service("gwtAdminTable[type=lmsInfo]")
public class LearningManagementSystemInfos implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageLearningManagementSystemInfo(), MESSAGES.pageLearningManagementSystemInfos());
	}

	@Override
	@PreAuthorize("checkPermission('LearningManagementSystemInfos')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldReference(), FieldType.text, 160, 20, Flag.UNIQUE),
				new Field(MESSAGES.fieldName(), FieldType.text, 300, 60, Flag.UNIQUE),
				new Field(MESSAGES.fieldExternalId(), FieldType.text, 120, 40, Flag.UNIQUE),
				new Field(MESSAGES.fieldDefault(), FieldType.toggle, 40)
				);
		data.setSortBy(1,2,3);
		for (LearningManagementSystemInfo lmsInfo: LearningManagementSystemInfoDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(lmsInfo.getUniqueId());
			r.setField(0, lmsInfo.getReference());
			r.setField(1, lmsInfo.getLabel());
			r.setField(2, lmsInfo.getExternalUniqueId());
			r.setField(3, lmsInfo.isDefaultLms() ? "true" : "false");
			r.setDeletable(!lmsInfo.isDefaultLms()  && !lmsInfo.isUsed(hibSession));
		}
		data.setEditable(context.hasPermission(Right.LearningManagementSystemInfoEdit));
		return data;
	}
	
	@Override
	@PreAuthorize("checkPermission('LearningManagementSystemInfoEdit')")
	public void save(SimpleEditInterface data, SessionContext context, org.hibernate.Session hibSession) {
		for (LearningManagementSystemInfo lmsInfo: LearningManagementSystemInfoDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(lmsInfo.getUniqueId());
			if (r == null)
				delete(lmsInfo, context, hibSession);
			else
				update(lmsInfo, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}
	
	
	@Override
	@PreAuthorize("checkPermission('LearningManagementSystemInfoEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		LearningManagementSystemInfo lmsInfo = new LearningManagementSystemInfo();
		lmsInfo.setReference(record.getField(0));
		lmsInfo.setLabel(record.getField(1));
		lmsInfo.setExternalUniqueId(record.getField(2));
		lmsInfo.setDefaultLms("true".equalsIgnoreCase(record.getField(3)));
		lmsInfo.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
		hibSession.persist(lmsInfo);
		record.setUniqueId(lmsInfo.getUniqueId());
		ChangeLog.addChange(hibSession,
				context,
				lmsInfo,
				lmsInfo.getReference() + " " + lmsInfo.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(LearningManagementSystemInfo lmsInfo, Record record, SessionContext context, Session hibSession) {
		if (lmsInfo == null) return;
		if (ToolBox.equals(lmsInfo.getReference(), record.getField(0)) &&
				ToolBox.equals(lmsInfo.getLabel(), record.getField(1)) &&
				ToolBox.equals(lmsInfo.getExternalUniqueId(), record.getField(2)) &&
				ToolBox.equals(lmsInfo.getDefaultLms(), "true".equalsIgnoreCase(record.getField(3)))) return;
		lmsInfo.setReference(record.getField(0));
		lmsInfo.setLabel(record.getField(1));
		lmsInfo.setExternalUniqueId(record.getField(2));
		lmsInfo.setDefaultLms("true".equalsIgnoreCase(record.getField(3)));
		hibSession.merge(lmsInfo);
		ChangeLog.addChange(hibSession,
				context,
				lmsInfo,
				lmsInfo.getReference() + " " + lmsInfo.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);	
	}

	@Override
	@PreAuthorize("checkPermission('LearningManagementSystemInfoEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(LearningManagementSystemInfoDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(LearningManagementSystemInfo lmsInfo, SessionContext context, Session hibSession) {
		if (lmsInfo == null) return;
		ChangeLog.addChange(hibSession,
				context,
				lmsInfo,
				lmsInfo.getReference() + " " + lmsInfo.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.remove(lmsInfo);
	}

	@Override
	@PreAuthorize("checkPermission('LearningManagementSystemInfoEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(LearningManagementSystemInfoDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
