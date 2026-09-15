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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.api.ApiToken;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.User;
import org.unitime.timetable.model.dao.UserDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.spring.security.MD5PasswordEncoder;

@Service("gwtAdminTable[type=user]")
public class Users implements AdminTable {
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Autowired ApiToken apiToken;

	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageUser(), MESSAGES.pageUsers());
	}

	@Override
	@PreAuthorize("checkPermission('Users')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<Field> fields = new ArrayList<Field>();
		fields.add(new Field(MSG.columnExternalId(), FieldType.text, 200, 40, Flag.NOT_EMPTY, Flag.UNIQUE));
		fields.add(new Field(MSG.columnUserName(), FieldType.text, 200, 15, Flag.NOT_EMPTY, Flag.UNIQUE));
		fields.add(new Field(MSG.columnUserPassword(), FieldType.password, 200, 40, Flag.NO_LIST));
		fields.add(new Field(MSG.columnManager(), FieldType.text, 200, Flag.READ_ONLY, Flag.NO_DETAIL));
		boolean apiKey = ApplicationProperty.ApiCanUseAPIToken.isTrue();
		if (apiKey)
			fields.add(new Field(MSG.columnAPIKey(), FieldType.text, 400, Flag.READ_ONLY));
		SimpleEditInterface data = new SimpleEditInterface(fields.toArray(new Field[fields.size()]));
		data.setSortBy(1);
		data.setAllowMultiEdit(false);
		long id = 0;
		String nameFormat = UserProperty.NameFormat.get(context.getUser());
		for (User user: UserDAO.getInstance().findAll()) {
			Record r = data.addRecord(id++);
			r.setField(0, user.getExternalUniqueId(), false);
			r.setField(1, user.getUsername());
			r.setField(2, "");
			TimetableManager m = TimetableManager.findByExternalId(user.getExternalUniqueId());
			r.setField(3, m == null ? "" : m.getName(nameFormat));
			if (apiKey)
				r.setField(4, apiToken.getToken(user.getExternalUniqueId(), user.getPassword()));
		}
		return data;
	}

	@Override
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		throw new GwtRpcException(MESSAGES.errorOperationNotSupported());
	}

	@Override
	public void save(Record record, SessionContext context, Session hibSession) {
		User user = new User();
		user.setExternalUniqueId(record.getField(0));
		user.setUsername(record.getField(1));
		if (record.getField(2) == null || record.getField(2).isEmpty())
			throw new GwtRpcException(MSG.errorRequiredField(MSG.propUserPassword()));
		user.setPassword(MD5PasswordEncoder.getEncodedPassword(record.getField(2)));
		TimetableManager m = TimetableManager.findByExternalId(user.getExternalUniqueId());
		record.setField(3, m == null ? "" : m.getName(UserProperty.NameFormat.get(context.getUser())));
		if (ApplicationProperty.ApiCanUseAPIToken.isTrue())
			record.setField(4, apiToken.getToken(user.getExternalUniqueId(), user.getPassword()));
		hibSession.persist(user);
		record.setUniqueId(System.currentTimeMillis());
	}

	@Override
	public void update(Record record, SessionContext context, Session hibSession) {
		User user = User.findByExternalId(record.getField(0));
		if (user != null) {
			if (user.getUsername().equals(record.getField(1))) {
				if (record.getField(2) != null && !record.getField(2).isEmpty())
					user.setPassword(MD5PasswordEncoder.getEncodedPassword(record.getField(2)));
				if (ApplicationProperty.ApiCanUseAPIToken.isTrue())
					record.setField(4, apiToken.getToken(user.getExternalUniqueId(), user.getPassword()));
				hibSession.merge(user);
			} else {
				User newUser = new User();
				newUser.setExternalUniqueId(user.getExternalUniqueId());
				newUser.setUsername(record.getField(1));
				if (record.getField(2) != null && !record.getField(2).isEmpty())
					newUser.setPassword(MD5PasswordEncoder.getEncodedPassword(record.getField(2)));
				else
					newUser.setPassword(user.getPassword());
				hibSession.persist(newUser);
				hibSession.remove(user);
				if (ApplicationProperty.ApiCanUseAPIToken.isTrue())
					record.setField(4, apiToken.getToken(newUser.getExternalUniqueId(), newUser.getPassword()));
			}
		}
		record.setField(2, "");
	}

	@Override
	public void delete(Record record, SessionContext context, Session hibSession) {
		User user = User.findByExternalId(record.getField(0));
		if (user != null)
			hibSession.remove(user);
	}

}
