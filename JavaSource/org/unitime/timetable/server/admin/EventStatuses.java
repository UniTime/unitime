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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.RoomTypeOption;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.EventServiceProviderDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("gwtAdminTable[type=eventStatus]")
public class EventStatuses implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageEventStatus(), MESSAGES.pageEventStatuses());
	}

	@Override
	@PreAuthorize("checkPermission('EventStatuses')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> states = new ArrayList<ListItem>();
		for (RoomTypeOption.Status state: RoomTypeOption.Status.values()) {
			states.add(new ListItem(String.valueOf(state.ordinal()), CONSTANTS.eventStatusName()[state.ordinal()].replace("\\,", ",")));
		}
		List<EventServiceProvider> providers = new ArrayList<EventServiceProvider>(EventServiceProvider.getServiceProviders(context.getUser()));
		List<Field> extra = new ArrayList<Field>();
		Map<Integer, Long> index2service = new HashMap<Integer, Long>();
		Map<Integer, EventServiceProvider> index2provider = new HashMap<Integer, EventServiceProvider>();
		for (EventServiceProvider provider: providers) {
			if (provider.isAllRooms()) continue;
			index2service.put(extra.size(), provider.getUniqueId());
			index2provider.put(extra.size(), provider);
			extra.add(new Field(provider.getReference(), FieldType.toggle, 40));
		}
		context.setAttribute(SessionAttribute.EventStatusServices, index2service);
		Field[] fields = new Field[7 + extra.size()];
		fields[0] = new Field("&otimes;", FieldType.parent, 50, Flag.READ_ONLY);
		fields[1] = new Field(MESSAGES.fieldDepartment() + "|" + MESSAGES.fieldType(), FieldType.text, 160, Flag.READ_ONLY);
		fields[2] = new Field(MESSAGES.fieldRoomType() + "|" + MESSAGES.fieldRoom(), FieldType.text, 100, Flag.READ_ONLY);
		fields[3] = new Field(MESSAGES.fieldEventStatus(), FieldType.list, 300, states, Flag.PARENT_NOT_EMPTY, Flag.SHOW_PARENT_IF_EMPTY);
		fields[4] = new Field(MESSAGES.fieldRoomNote(), FieldType.textarea, 50, 3, 2048, Flag.SHOW_PARENT_IF_EMPTY);
		fields[5] = new Field(MESSAGES.fieldBreakTime(), FieldType.number, 50, 10, Flag.SHOW_PARENT_IF_EMPTY);
		fields[6] = new Field(MESSAGES.fieldSortOrder(), FieldType.text, 80, 10, Flag.READ_ONLY, Flag.HIDDEN);
		for (int i = 0; i < extra.size(); i++)
			fields[7 + i] = extra.get(i);
		SimpleEditInterface data = new SimpleEditInterface(fields);
		data.setSortBy(6);
		data.setAddable(false);
		long id = 0;
		for (Department department: Department.getUserDepartments(context.getUser())) {
			if (!department.isAllowEvents()) continue;
			for (RoomType roomType: (List<RoomType>)hibSession.createQuery(
					"select distinct t from Room r inner join r.roomType t where r.eventDepartment.uniqueId = :departmentId order by t.ord, t.label")
					.setLong("departmentId", department.getUniqueId()).setCacheable(true).list()) {
				RoomTypeOption option = roomType.getOption(department);
				Record r = data.addRecord(--id, false);
				r.setField(0, "+", false);
				r.setField(1, department.getLabel(), false);
				r.setField(2, roomType.getLabel(), false);
				r.setField(3, String.valueOf(option.getStatus() == null ? RoomTypeOption.getDefaultStatus() : option.getStatus()));
				r.setField(4, option.getMessage() == null ? "" : option.getMessage());
				r.setField(5, option.getBreakTime() == null ? "0" : option.getBreakTime().toString());
				r.setField(6, department.getDeptCode() + ":" + roomType.getOrd());
				for (int i = 0; i < extra.size(); i++)
					r.setField(7 + i, MESSAGES.notApplicable(), false);
				for (Room room: (List<Room>)hibSession.createQuery(
						"select r from Room r where r.roomType.uniqueId = :roomTypeId and r.eventDepartment.uniqueId = :departmentId order by r.building.abbreviation, r.roomNumber")
						.setLong("departmentId", department.getUniqueId()).setLong("roomTypeId", roomType.getUniqueId()).setCacheable(true).list()) {
					r = data.addRecord(room.getUniqueId(), false);
					r.setField(0, String.valueOf(id));
					r.setField(1, department.getDeptCode() + " " + roomType.getLabel(), false);
					r.setField(2, room.getLabel(), false);
					r.setField(3, room.getEventStatus() == null ? "" : room.getEventStatus().toString());
					r.setField(4, room.getNote() == null ? "" : room.getNote());
					r.setField(5, room.getBreakTime() == null ? "" : room.getBreakTime().toString());
					r.setField(6, department.getDeptCode() + ":" + roomType.getOrd() + ":" + room.getLabel());
					for (int i = 0; i < extra.size(); i++) {
						EventServiceProvider provider = index2provider.get(i);
						boolean hasToggle = provider.getDepartment() == null || provider.getDepartment().equals(department);
						r.setField(7 + i, !hasToggle ? MESSAGES.notApplicable() : room.getAllowedServices().contains(provider) ? "true" : "false", hasToggle);
					}
				}
			}
			for (RoomType roomType: (List<RoomType>)hibSession.createQuery(
					"select distinct t from NonUniversityLocation r inner join r.roomType t where r.eventDepartment.uniqueId = :departmentId order by t.ord, t.label")
					.setLong("departmentId", department.getUniqueId()).setCacheable(true).list()) {
				RoomTypeOption option = roomType.getOption(department);
				Record r = data.addRecord(--id, false);
				r.setField(0, "+", false);
				r.setField(1, department.getLabel(), false);
				r.setField(2, roomType.getLabel(), false);
				r.setField(3, String.valueOf(option.getStatus() == null ? RoomTypeOption.getDefaultStatus() : option.getStatus()));
				r.setField(4, option.getMessage() == null ? "" : option.getMessage());
				r.setField(5, option.getBreakTime() == null ? "0" : option.getBreakTime().toString());
				r.setField(6, department.getDeptCode() + ":" + roomType.getOrd());
				for (int i = 0; i < extra.size(); i++)
					r.setField(7 + i, MESSAGES.notApplicable(), false);
				for (NonUniversityLocation room: (List<NonUniversityLocation>)hibSession.createQuery(
						"select r from NonUniversityLocation r where r.roomType.uniqueId = :roomTypeId and r.eventDepartment.uniqueId = :departmentId order by r.name")
						.setLong("departmentId", department.getUniqueId()).setLong("roomTypeId", roomType.getUniqueId()).setCacheable(true).list()) {
					r = data.addRecord(room.getUniqueId(), false);
					r.setField(0, String.valueOf(id));
					r.setField(1, department.getDeptCode() + " " + roomType.getLabel(), false);
					r.setField(2, room.getLabel(), false);
					r.setField(3, room.getEventStatus() == null ? "" : room.getEventStatus().toString());
					r.setField(4, room.getNote() == null ? "" : room.getNote());
					r.setField(5, room.getBreakTime() == null ? "" : room.getBreakTime().toString());
					r.setField(6, department.getDeptCode() + ":" + roomType.getOrd() + ":" + room.getLabel());
					for (int i = 0; i < extra.size(); i++) {
						EventServiceProvider provider = index2provider.get(i);
						boolean hasToggle = provider.getDepartment() == null || provider.getDepartment().equals(department);
						r.setField(7 + i, !hasToggle ? MESSAGES.notApplicable() : room.getAllowedServices().contains(provider) ? "true" : "false", hasToggle);
					}
				}
			}
		}
		data.setEditable(context.hasPermission(Right.EventStatusEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('EventStatusEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (Department department: Department.getUserDepartments(context.getUser())) {
			if (!department.isAllowEvents()) continue;
			for (RoomType roomType: (List<RoomType>)hibSession.createQuery(
					"select distinct t from Room r inner join r.roomType t where r.eventDepartment.uniqueId = :departmentId order by t.ord, t.label")
					.setLong("departmentId", department.getUniqueId()).setCacheable(true).list()) {
				RoomTypeOption option = roomType.getOption(department);
				for (Record r: data.getRecords()) {
					if (r.getField(1).equals(department.getLabel()) && r.getField(2).equals(roomType.getLabel())) {
						boolean optionChanged = 
								!ToolBox.equals(option.getStatus() == null ? RoomTypeOption.getDefaultStatus() : option.getStatus(), Integer.valueOf(r.getField(3))) ||
								!ToolBox.equals(option.getMessage(), r.getField(4)) ||
								!ToolBox.equals(option.getBreakTime() == null ? "0" : option.getBreakTime().toString(), r.getField(5));
						option.setStatus(Integer.parseInt(r.getField(3)));
						option.setMessage(r.getField(4));
						try {
							option.setBreakTime(Integer.parseInt(r.getField(5)));
						} catch (NumberFormatException e) {
							option.setBreakTime(0);
						}
						hibSession.saveOrUpdate(option);
						if (optionChanged)
							ChangeLog.addChange(hibSession,
									context,
									option.getRoomType(),
									option.getDepartment().getDeptCode() + " " + option.getRoomType().getLabel() + ": " + option.getEventStatus(),
									Source.SIMPLE_EDIT, 
									Operation.UPDATE,
									null,
									option.getDepartment());
					}
				}
			}
			for (RoomType roomType: (List<RoomType>)hibSession.createQuery(
					"select distinct t from NonUniversityLocation r inner join r.roomType t where r.eventDepartment.uniqueId = :departmentId order by t.ord, t.label")
					.setLong("departmentId", department.getUniqueId()).setCacheable(true).list()) {
				RoomTypeOption option = roomType.getOption(department);
				for (Record r: data.getRecords()) {
					if (r.getField(1).equals(department.getLabel()) && r.getField(2).equals(roomType.getLabel())) {
						boolean optionChanged = 
								!ToolBox.equals(option.getStatus() == null ? RoomTypeOption.getDefaultStatus() : option.getStatus(), Integer.valueOf(r.getField(3))) ||
								!ToolBox.equals(option.getMessage(), r.getField(4)) ||
								!ToolBox.equals(option.getBreakTime() == null ? "0" : option.getBreakTime().toString(), r.getField(5));
						option.setStatus(Integer.parseInt(r.getField(3)));
						option.setMessage(r.getField(4));
						try {
							option.setBreakTime(Integer.parseInt(r.getField(5)));
						} catch (NumberFormatException e) {
							option.setBreakTime(0);
						}
						hibSession.saveOrUpdate(option);
						if (optionChanged)
							ChangeLog.addChange(hibSession,
									context,
									option.getRoomType(),
									option.getDepartment().getDeptCode() + " " + option.getRoomType().getLabel() + ": " + option.getEventStatus(),
									Source.SIMPLE_EDIT, 
									Operation.UPDATE,
									null,
									option.getDepartment());
					}
				}
			}
			for (Location location: (List<Location>)hibSession.createQuery(
					"from Location where eventDepartment.uniqueId = :departmentId")
					.setLong("departmentId", department.getUniqueId()).setCacheable(true).list()) {
				Record r = data.getRecord(location.getUniqueId());
				if (r != null)
					update(location, r, context, hibSession);
			}
		}
	}

	@Override
	@PreAuthorize("checkPermission('EventStatusEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		throw new GwtRpcException(MESSAGES.errorOperationNotSupported());
	}
	
	protected boolean sameServiceProviders(Location location, SessionContext context, Record record) {
		Set<Long> services = new HashSet<Long>();
		if (location.getAllowedServices() != null)
			for (EventServiceProvider service: location.getAllowedServices())
				services.add(service.getUniqueId());
		Map<Integer,Long> index2service = (Map<Integer,Long>)context.getAttribute(SessionAttribute.EventStatusServices);
		for (Map.Entry<Integer, Long> e: index2service.entrySet()) {
			if ("true".equals(record.getField(7 + e.getKey()))) {
				if (!services.contains(e.getValue())) return false;
			} else {
				if (services.contains(e.getValue())) return false;
			}
		}
		return true;
	}
	
	protected void setServiceProviders(Location location, SessionContext context, Record record) {
		if (location.getAllowedServices() == null)
			location.setAllowedServices(new HashSet<EventServiceProvider>());
		Map<Integer,Long> index2service = (Map<Integer,Long>)context.getAttribute(SessionAttribute.EventStatusServices);
		for (Map.Entry<Integer, Long> e: index2service.entrySet()) {
			if ("true".equals(record.getField(7 + e.getKey()))) 
				location.getAllowedServices().add(EventServiceProviderDAO.getInstance().get(e.getValue()));
			else
				location.getAllowedServices().remove(EventServiceProviderDAO.getInstance().get(e.getValue()));
		}
	}
	
	protected void update(Location location, Record record, SessionContext context, Session hibSession) {
		if (location == null) return;
		Integer status = record.getField(3) == null || record.getField(3).isEmpty() ? null : Integer.parseInt(record.getField(3));
		String note = (record.getField(4) == null || record.getField(4).isEmpty() ? null : record.getField(4));
		Integer breakTime = null;
		try {
			breakTime = (record.getField(5) == null || record.getField(5).isEmpty() ? null : Integer.parseInt(record.getField(5)));
		} catch (NumberFormatException e) {}
		if (ToolBox.equals(location.getEventStatus(), status) &&
				ToolBox.equals(location.getNote(), note) &&
				ToolBox.equals(location.getBreakTime(), breakTime) &&
				sameServiceProviders(location, context, record)) return;
		boolean noteChanged = !ToolBox.equals(location.getNote(), note);
		location.setEventStatus(status);
		location.setNote(note);
		location.setBreakTime(breakTime);
		setServiceProviders(location, context, record);
		hibSession.saveOrUpdate(location);
		ChangeLog.addChange(hibSession,
				context,
				location,
				location.getLabel() + ": " + location.getEffectiveEventStatus() + (location.getEventStatus() == null ? " (Default)" : ""),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				location.getEventDepartment());
		if (noteChanged)
			ChangeLog.addChange(hibSession,
					context, location, (location.getNote() == null || location.getNote().isEmpty() ? "-" : location.getNote()),
					ChangeLog.Source.ROOM_EDIT, ChangeLog.Operation.NOTE, null, location.getControllingDepartment());		
	}

	@Override
	@PreAuthorize("checkPermission('EventStatusEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		if (record.getUniqueId() >= 0) {
			update(LocationDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
			return;
		}
		for (Department department: Department.getUserDepartments(context.getUser())) {
			if (!department.isAllowEvents()) continue;
			if (record.getField(1).equals(department.getLabel())) {
				for (RoomType roomType: (List<RoomType>)hibSession.createQuery(
						"select distinct t from Room r inner join r.roomType t where r.eventDepartment.uniqueId = :departmentId order by t.ord, t.label")
						.setLong("departmentId", department.getUniqueId()).setCacheable(true).list()) {
					if (record.getField(2).equals(roomType.getLabel())) {
						RoomTypeOption option = roomType.getOption(department);
						boolean optionChanged = 
								!ToolBox.equals(option.getStatus() == null ? RoomTypeOption.getDefaultStatus() : option.getStatus(), Integer.valueOf(record.getField(3))) ||
								!ToolBox.equals(option.getMessage(), record.getField(4)) ||
								!ToolBox.equals(option.getBreakTime() == null ? "0" : option.getBreakTime().toString(), record.getField(5));
						option.setStatus(Integer.parseInt(record.getField(3)));
						option.setMessage(record.getField(4));
						try {
							option.setBreakTime(Integer.parseInt(record.getField(5)));
						} catch (NumberFormatException e) {
							option.setBreakTime(0);
						}
						hibSession.saveOrUpdate(option);
						if (optionChanged)
							ChangeLog.addChange(hibSession,
									context,
									option.getRoomType(),
									option.getDepartment().getDeptCode() + " " + option.getRoomType().getLabel() + ": " + option.getEventStatus(),
									Source.SIMPLE_EDIT, 
									Operation.UPDATE,
									null,
									option.getDepartment());
						return;
					}
				}
				for (RoomType roomType: (List<RoomType>)hibSession.createQuery(
						"select distinct t from NonUniversityLocation r inner join r.roomType t where r.eventDepartment.uniqueId = :departmentId order by t.ord, t.label")
						.setLong("departmentId", department.getUniqueId()).setCacheable(true).list()) {
					if (record.getField(2).equals(roomType.getLabel())) {
						RoomTypeOption option = roomType.getOption(department);
						boolean optionChanged = 
								!ToolBox.equals(option.getStatus() == null ? RoomTypeOption.getDefaultStatus() : option.getStatus(), Integer.valueOf(record.getField(3))) ||
								!ToolBox.equals(option.getMessage(), record.getField(4)) ||
								!ToolBox.equals(option.getBreakTime() == null ? "0" : option.getBreakTime().toString(), record.getField(5));
						option.setStatus(Integer.parseInt(record.getField(3)));
						option.setMessage(record.getField(4));
						try {
							option.setBreakTime(Integer.parseInt(record.getField(5)));
						} catch (NumberFormatException e) {
							option.setBreakTime(0);
						}
						hibSession.saveOrUpdate(option);
						if (optionChanged)
							ChangeLog.addChange(hibSession,
									context,
									option.getRoomType(),
									option.getDepartment().getDeptCode() + " " + option.getRoomType().getLabel() + ": " + option.getEventStatus(),
									Source.SIMPLE_EDIT, 
									Operation.UPDATE,
									null,
									option.getDepartment());
						return;
					}
				}
			}
		}
	}

	@Override
	@PreAuthorize("checkPermission('EventStatusEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		throw new GwtRpcException(MESSAGES.errorOperationNotSupported());
	}
}
