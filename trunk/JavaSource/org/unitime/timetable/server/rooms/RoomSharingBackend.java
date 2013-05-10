/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.rooms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingModel;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingOption;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingRequest;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.RequiredTimeTable;

@GwtRpcImplements(RoomSharingRequest.class)
public class RoomSharingBackend implements GwtRpcImplementation<RoomSharingRequest, RoomSharingModel> {
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public RoomSharingModel execute(RoomSharingRequest request, SessionContext context) {
		switch (request.getOperation()) {
		case LOAD:
			return (request.isEventAvailability() ? loadEventAvailability(request, context) : loadRoomSharing(request, context));
		case SAVE:
			return (request.isEventAvailability() ? saveEventAvailability(request, context) : saveRoomSharing(request, context));
		default:
			return null;
		}
	}
	
	public RoomSharingModel loadRoomSharing(RoomSharingRequest request, SessionContext context) {
		context.checkPermission(request.getLocationId(), "Location", Right.RoomDetailAvailability);
		Location location = LocationDAO.getInstance().get(request.getLocationId());
		
		RoomSharingModel model = new RoomSharingModel();
		model.setId(location.getUniqueId());
		model.setName(location.getLabel());
		for (int i = 0; true; i++) {
			String mode = ApplicationProperties.getProperty("unitime.room.sharingMode" + (1 + i), i < CONSTANTS.roomSharingModes().length ? CONSTANTS.roomSharingModes()[i] : null);
			if (mode == null || mode.isEmpty()) break;
			model.addMode(new RoomInterface.RoomSharingDisplayMode(mode));
		}
		boolean editable = context.hasPermission(location, Right.RoomEditAvailability);
		model.setDefaultEditable(editable);
		model.addOption(new RoomSharingOption(-1l, "#FFFFFF", MESSAGES.codeFreeForAll(), MESSAGES.legendFreeForAll(), editable));
		model.addOption(new RoomSharingOption(-2l, "#696969", MESSAGES.codeNotAvailable(), MESSAGES.legendNotAvailable(), editable));
		
		String defaultGridSize = RequiredTimeTable.getTimeGridSize(context.getUser());
		if (defaultGridSize != null)
			for (int i = 0; i < model.getModes().size(); i++) {
				if (model.getModes().get(i).getName().equals(defaultGridSize)) {
					model.setDefaultMode(i); break;
				}
			}
		model.setDefaultHorizontal(CommonValues.HorizontalGrid.eq(context.getUser().getProperty(UserProperty.GridOrientation)));
		model.setDefaultOption(model.getOptions().get(0));

		model.setNote(location.getShareNote());
		model.setNoteEditable(editable);

		Set<Department> current = new TreeSet<Department>();
		for (RoomDept rd: location.getRoomDepts())
			current.add(rd.getDepartment());
		
		for (Department d: current)
			model.addOption(new RoomSharingOption(d.getUniqueId(), "#" + d.getRoomSharingColor(current),
					d.getDeptCode(), d.getName() + (d.isExternalManager() ? " (EXT: " + d.getExternalMgrLabel() + ")" : ""), editable));
		
		for (Department d: Department.findAllBeingUsed(context.getUser().getCurrentAcademicSessionId()))
			model.addOther(new RoomSharingOption(d.getUniqueId(), "#" + d.getRoomSharingColor(current),
					d.getDeptCode(), d.getName() + (d.isExternalManager() ? " (EXT: " + d.getExternalMgrLabel() + ")" : ""), editable));
		

		Map<Character, Long> char2dept = new HashMap<Character, Long>(); char pref = '0';
		if (location.getManagerIds() != null) {
			for (StringTokenizer stk = new StringTokenizer(location.getManagerIds(), ","); stk.hasMoreTokens();) {
				Long id = Long.valueOf(stk.nextToken());
				char2dept.put(new Character(pref++), id);
			}
		}

        try {
            int idx = 0;
            for (int d = 0; d < Constants.NR_DAYS; d++)
                for (int t = 0; t < Constants.SLOTS_PER_DAY; t++) {
                    pref = (location.getPattern() != null && idx < location.getPattern().length() ? location.getPattern().charAt(idx) : net.sf.cpsolver.coursett.model.RoomSharingModel.sFreeForAllPrefChar);
                    idx++;
                    if (pref == net.sf.cpsolver.coursett.model.RoomSharingModel.sNotAvailablePrefChar) {
                    	model.setOption(d, t, -2l);
                    } else if (pref == net.sf.cpsolver.coursett.model.RoomSharingModel.sFreeForAllPrefChar) {
                    	model.setOption(d, t, -1l);
                    } else {
                    	Long deptId = (char2dept == null ? null : char2dept.get(pref));
                    	if (deptId == null) {
                    		try {
                    			deptId = new ArrayList<Department>(current).get(pref - '0').getUniqueId();
                    		} catch (IndexOutOfBoundsException e) {}
                    	}
                    	model.setOption(d, t, deptId);
                    }
                }
        } catch (NullPointerException e) {
        } catch (IndexOutOfBoundsException e) {
        }

		if (editable && !context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent)) {
			boolean control = false, allDept = true;
			for (RoomDept rd: location.getRoomDepts()) {
				if (rd.isControl())
					control = context.getUser().getCurrentAuthority().hasQualifier(rd.getDepartment());
				if (allDept && !context.getUser().getCurrentAuthority().hasQualifier(rd.getDepartment()))
					allDept = false;
			}
			model.setDefaultEditable(control || allDept);
			model.setNoteEditable(control || allDept);
			if (!control && !allDept) {
				for (int d = 0; d < 7; d++)
					for (int s = 0; s < 288; s ++) {
						RoomSharingOption option = model.getOption(d, s);
						model.setEditable(d, s, option != null && context.getUser().getCurrentAuthority().hasQualifier(new SimpleQualifier("Department", option.getId())));
					}
			}
		}
		
		return model;
	}

	public RoomSharingModel saveRoomSharing(RoomSharingRequest request, SessionContext context) {
		context.checkPermission(request.getLocationId(), "Location", Right.RoomEditAvailability);
		
		Map<Long, Character> dept2char = new HashMap<Long, Character>();
		dept2char.put(-1l, net.sf.cpsolver.coursett.model.RoomSharingModel.sFreeForAllPrefChar);
		dept2char.put(-2l, net.sf.cpsolver.coursett.model.RoomSharingModel.sNotAvailablePrefChar);
		String managerIds = ""; char pref = '0';
		Set<Long> add = new HashSet<Long>();
		for (RoomSharingOption option: request.getModel().getOptions()) {
			if (option.getId() >= 0) {
				managerIds += (managerIds.isEmpty() ? "" : ",") + option.getId();
				dept2char.put(option.getId(), new Character(pref++));
				add.add(option.getId());
			}
		}
		
		String pattern = "";
		for (int d = 0; d < 7; d++)
			for (int s = 0; s < 288; s ++) {
				RoomSharingOption option = request.getModel().getOption(d, s);
				pattern += dept2char.get(option.getId());
			}
		
		org.hibernate.Session hibSession = LocationDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
		
			Location location = LocationDAO.getInstance().get(request.getLocationId(), hibSession);
			location.setManagerIds(managerIds);
			location.setPattern(pattern);
			
			for (Iterator<RoomDept> i = location.getRoomDepts().iterator(); i.hasNext(); ) {
				RoomDept rd = (RoomDept)i.next();
				if (!add.remove(rd.getDepartment().getUniqueId())) {
					rd.getDepartment().getRoomDepts().remove(rd);
					i.remove();
					hibSession.delete(rd);
				}
			}
			for (Long id: add) {
				RoomDept rd = new RoomDept();
				rd.setControl(false);
				rd.setDepartment(DepartmentDAO.getInstance().get(id, hibSession));
				rd.getDepartment().getRoomDepts().add(rd);
				rd.setRoom(location);
				location.getRoomDepts().add(rd);
				hibSession.saveOrUpdate(rd);
			}
			
			hibSession.saveOrUpdate(location);
			
			if (request.getModel().isNoteEditable()) {
				if (request.getModel().hasNote())
					location.setShareNote(request.getModel().getNote().length() > 2048 ? request.getModel().getNote().substring(0, 2048) : request.getModel().getNote());
				else
					location.setShareNote(null);
			}
			
			ChangeLog.addChange(hibSession, context, location, ChangeLog.Source.ROOM_DEPT_EDIT, ChangeLog.Operation.UPDATE, null, location.getControllingDepartment());
			
			tx.commit();
			
			return null;
			
		} catch (Exception ex) {
			tx.rollback();
			if (ex instanceof GwtRpcException) throw (GwtRpcException)ex;
			throw new GwtRpcException(ex.getMessage(), ex);
		}
	}
	
	public RoomSharingModel loadEventAvailability(RoomSharingRequest request, SessionContext context) {
		context.checkPermission(request.getLocationId(), "Location", Right.RoomDetailEventAvailability);
		Location location = LocationDAO.getInstance().get(request.getLocationId());
		
		RoomSharingModel model = new RoomSharingModel();
		model.setId(location.getUniqueId());
		model.setName(location.getLabel());
		for (int i = 0; true; i++) {
			String mode = ApplicationProperties.getProperty("unitime.room.sharingMode" + (1 + i), i < CONSTANTS.roomSharingModes().length ? CONSTANTS.roomSharingModes()[i] : null);
			if (mode == null || mode.isEmpty()) break;
			model.addMode(new RoomInterface.RoomSharingDisplayMode(mode));
		}
		boolean editable = context.hasPermission(location, Right.RoomEditEventAvailability);
		model.setDefaultEditable(editable);
		model.addOption(new RoomSharingOption(-1l, "#FFFFFF", MESSAGES.codeAvailable(), MESSAGES.legendAvailable(), editable));
		model.addOption(new RoomSharingOption(-2l, "#696969", MESSAGES.codeNotAvailable(), MESSAGES.legendNotAvailable(), editable));
		
		String defaultGridSize = RequiredTimeTable.getTimeGridSize(context.getUser());
		if (defaultGridSize != null)
			for (int i = 0; i < model.getModes().size(); i++) {
				if (model.getModes().get(i).getName().equals(defaultGridSize)) {
					model.setDefaultMode(i); break;
				}
			}
		model.setDefaultHorizontal(CommonValues.HorizontalGrid.eq(context.getUser().getProperty(UserProperty.GridOrientation)));
		model.setDefaultOption(model.getOptions().get(0));
		
		int idx = 0;
        for (int d = 0; d < Constants.NR_DAYS; d++)
            for (int t = 0; t < Constants.SLOTS_PER_DAY; t++) {
                char pref = (location.getEventAvailability() != null && idx < location.getEventAvailability().length() ? location.getEventAvailability().charAt(idx) : '0');
                idx++;
                model.setOption(d, t, pref == '0' ? -1l : -2l);
            }

        return model;
	}
	
	public RoomSharingModel saveEventAvailability(RoomSharingRequest request, SessionContext context) {
		context.checkPermission(request.getLocationId(), "Location", Right.RoomEditEventAvailability);
		
		String availability = "";
		for (int d = 0; d < 7; d++)
			for (int s = 0; s < 288; s ++) {
				RoomSharingOption option = request.getModel().getOption(d, s);
				availability += (option.getId() == -1l ? '0' : '1');
			}
		
		org.hibernate.Session hibSession = LocationDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
		
			Location location = LocationDAO.getInstance().get(request.getLocationId(), hibSession);
			location.setEventAvailability(availability);
			hibSession.save(location);
			
			ChangeLog.addChange(hibSession, context, location, ChangeLog.Source.ROOM_DEPT_EDIT, ChangeLog.Operation.UPDATE, null, location.getControllingDepartment());
			
			tx.commit();
			
			return null;
			
		} catch (Exception ex) {
			tx.rollback();
			if (ex instanceof GwtRpcException) throw (GwtRpcException)ex;
			throw new GwtRpcException(ex.getMessage(), ex);
		}
	}
}
