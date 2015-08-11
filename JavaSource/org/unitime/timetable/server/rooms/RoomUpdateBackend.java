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
package org.unitime.timetable.server.rooms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FutureOperation;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PeriodPreferenceModel;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomException;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingOption;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomUpdateRpcRequest;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExamLocationPref;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.LocationPicture;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.NonUniversityLocationPicture;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomPicture;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.NonUniversityLocationPictureDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.RoomPictureDAO;
import org.unitime.timetable.model.dao.RoomTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LocationPermIdGenerator;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(RoomUpdateRpcRequest.class)
public class RoomUpdateBackend implements GwtRpcImplementation<RoomUpdateRpcRequest, RoomDetailInterface> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public RoomDetailInterface execute(RoomUpdateRpcRequest request, SessionContext context) {
		Location location = null;
		RoomException exception = null;
		try {
			switch (request.getOperation()) {
			case DELETE:
				Long permId = delete(request.getLocationId(), context, false);
				if (permId != null && request.hasFutureFlags()) {
					List<Location> futureLocations = LocationDAO.getInstance().getSession().createQuery(
							"select l from Location l, Session s where " +
							"l.permanentId = :permanentId and s.uniqueId = :sessionId and s.sessionBeginDateTime < l.session.sessionBeginDateTime " + 
							"order by l.session.sessionBeginDateTime")
							.setLong("permanentId", permId).setLong("sessionId", context.getUser().getCurrentAcademicSessionId()).list();
					for (Location loc: futureLocations)
						if (request.getFutureFlag(loc.getUniqueId()) != null)
							delete(loc.getUniqueId(), new EventContext(context, context.getUser(), loc.getSession().getUniqueId()), true);
				}
				break;
			case UPDATE:
				location = update(request.getRoom(), context, false, FutureOperation.getFlagAllEnabled());
				if (location != null && request.hasFutureFlags()) {
					List<Location> futureLocations = LocationDAO.getInstance().getSession().createQuery(
							"select l from Location l, Session s where " +
							"l.permanentId = :permanentId and s.uniqueId = :sessionId and s.sessionBeginDateTime < l.session.sessionBeginDateTime " + 
							"order by l.session.sessionBeginDateTime")
							.setLong("permanentId", location.getPermanentId()).setLong("sessionId", context.getUser().getCurrentAcademicSessionId()).list();
					for (Location loc: futureLocations) {
						Integer flags = request.getFutureFlag(loc.getUniqueId());
						if (flags != null) {
							request.getRoom().setUniqueId(loc.getUniqueId());
							update(request.getRoom(), new EventContext(context, context.getUser(), loc.getSession().getUniqueId()), true, flags);
						}
					}
				}
				break;
			case CREATE:
				location = create(request.getRoom(), context, null, null, FutureOperation.getFlagAllEnabled());
				if (location != null && request.hasFutureFlags()) {
					List<Long> futureSessionIds = LocationDAO.getInstance().getSession().createQuery(
							"select f.uniqueId from Session f, Session s where " +
							"s.uniqueId = :sessionId and s.sessionBeginDateTime < f.sessionBeginDateTime and s.academicInitiative = f.academicInitiative " +
							"order by f.sessionBeginDateTime")
							.setLong("sessionId",context.getUser().getCurrentAcademicSessionId()).list();
					for (Long id: futureSessionIds) {
						Integer flags = request.getFutureFlag(-id);
						if (flags != null)
							create(request.getRoom(), new EventContext(context, context.getUser(), id), id, location.getPermanentId(), flags);
					}
				}
				break;
			}
		} catch (RoomException e) {
			exception = e;
		}
		if (location != null) {
	    	List<ExamType> types = ExamType.findAllApplicable(context.getUser(), DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable);
	    	RoomDetailInterface detail = new RoomDetailsBackend().load(location, null, true, context,
	    			!context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent),
	    			types,
	    			context.hasPermission(Right.InstructionalOfferings) || context.hasPermission(Right.Classes),
	    			context.hasPermission(Right.Examinations),
	    			context.hasPermission(Right.Events),
	    			true);
	    	
	    	RoomSharingBackend rsb = new RoomSharingBackend();
			if (detail.isCanSeeEventAvailability())
				detail.setEventAvailabilityModel(rsb.loadEventAvailability(location, context));
			if (detail.isCanSeeAvailability())
				detail.setRoomSharingModel(rsb.loadRoomSharing(location, true, context));
			if (detail.isCanSeePeriodPreferences()) {
				PeriodPreferencesBackend ppb = new PeriodPreferencesBackend();
				for (ExamType type: types) {
					detail.setPeriodPreferenceModel(ppb.loadPeriodPreferences(location, type, context));
				}
			}
			context.setAttribute(RoomPictureServlet.TEMP_ROOM_PICTURES, null);
			if (exception != null) throw exception.withRoom(detail);
			return detail;
		} else {
			if (exception != null) throw exception;
			return null;
		}
	}
	
	protected Long delete(Long locationId, SessionContext context, boolean future) {
		if (locationId == null) return null;
		Transaction tx = null;
		Long permId = null;
		String roomName = null, sessionLabel = null;
		org.hibernate.Session hibSession = LocationDAO.getInstance().getSession();
		try {
			tx = hibSession.beginTransaction();
			Location location = LocationDAO.getInstance().get(locationId, hibSession);
			if (location == null) return null;
			roomName = location.getLabel();
			sessionLabel = location.getSession().getLabel();
			if (future) {
				if (location instanceof Room) {
					if (!context.hasPermission((Room)location, Right.RoomDelete)) return null;
				} else {
					if (!context.hasPermission((NonUniversityLocation)location, Right.NonUniversityLocationDelete)) return null;
				}
			} else {
				if (location instanceof Room)
					context.checkPermission((Room)location, Right.RoomDelete);
				else
					context.checkPermission((NonUniversityLocation)location, Right.NonUniversityLocationDelete);
			}
			permId = location.getPermanentId();
			ChangeLog.addChange(
                    hibSession, 
                    context, 
                    location, 
                    ChangeLog.Source.ROOM_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    location.getControllingDepartment());
			List roomPrefs = hibSession.createCriteria(RoomPref.class).add(Restrictions.eq("room.uniqueId", location.getUniqueId())).list();
			for (Iterator i=location.getRoomDepts().iterator();i.hasNext();) {
				RoomDept rd = (RoomDept)i.next();
				Department d = rd.getDepartment();
				d.getRoomDepts().remove(rd);
				hibSession.delete(rd);
				hibSession.saveOrUpdate(d);
			}
			for (Iterator i=roomPrefs.iterator();i.hasNext();) {
				RoomPref rp = (RoomPref)i.next();
				rp.getOwner().getPreferences().remove(rp);
				hibSession.delete(rp);
				hibSession.saveOrUpdate(rp.getOwner());
			}
			for (Iterator i=location.getAssignments().iterator();i.hasNext();) {
                Assignment a = (Assignment)i.next();
                a.getRooms().remove(location);
                hibSession.saveOrUpdate(a);
                i.remove();
            }
			hibSession.delete(location);
			tx.commit(); tx = null;
			return permId;
		} catch (Throwable t) {
			if (future)
				throw new RoomException(MESSAGES.failedDeleteLocation(roomName, sessionLabel, t.getMessage()), t);
			else
				throw new GwtRpcException(t.getMessage(), t);
		} finally {
			if (tx != null && tx.isActive()) tx.rollback();
			
		}
	}
	
	protected Location update(RoomDetailInterface room, SessionContext context, boolean future, int flags) {
		Transaction tx = null;
		String roomName = null, sessionLabel = null;
		org.hibernate.Session hibSession = LocationDAO.getInstance().getSession();
		try {
			tx = hibSession.beginTransaction();
			Location location = LocationDAO.getInstance().get(room.getUniqueId(), hibSession);
			if (location == null) return null;
			sessionLabel = location.getSession().getLabel();
			if (future) {
				if (location instanceof Room) {
					if (!context.hasPermission((Room)location, Right.RoomEdit)) return null;
				} else {
					if (!context.hasPermission((NonUniversityLocation)location, Right.NonUniversityLocationEdit)) return null;
				}
			} else {
				if (location instanceof Room)
					context.checkPermission((Room)location, Right.RoomEdit);
				else
					context.checkPermission((NonUniversityLocation)location, Right.NonUniversityLocationEdit);
			}
			
			if (context.hasPermission(location, Right.RoomEditChangeRoomProperties) && FutureOperation.ROOM_PROPERTIES.in(flags)) {
				if (location instanceof Room) {
					Building building = lookupBuilding(hibSession, room.getBuilding(), future, location.getSession().getUniqueId());
					if (future && room.getBuilding() != null && building == null)
						throw new RoomException(MESSAGES.errorBuildingNotExist(room.getBuilding().getAbbreviation()));
					Room other = Room.findByBldgIdRoomNbr(building != null ? building.getUniqueId() : ((Room) location).getBuilding().getUniqueId(), room.getName(), location.getSession().getUniqueId());
					if (other != null && !location.getUniqueId().equals(other.getUniqueId()))
						throw new RoomException(MESSAGES.errorRoomAlreadyExists(roomName));
					if (building != null) {
						((Room)location).setBuilding(building);
						((Room)location).setBuildingAbbv(building.getAbbreviation());
					}
					((Room)location).setRoomNumber(room.getName());
				} else {
					if (!room.getName().equals(location.getLabel())) {
						String nonUniversityLocationRegex = ApplicationProperty.NonUniversityLocationPattern.value();
				    	String nonUniversityLocationInfo = ApplicationProperty.NonUniversityLocationPatternInfo.value();
				    	if (nonUniversityLocationRegex != null && nonUniversityLocationRegex.trim().length() > 0) {
					    	try { 
						    	Pattern pattern = Pattern.compile(nonUniversityLocationRegex);
						    	Matcher matcher = pattern.matcher(room.getName());
						    	if (!matcher.find()) {
						    		throw new RoomException(nonUniversityLocationInfo == null || nonUniversityLocationInfo.isEmpty() ?
						    				MESSAGES.errorLocationNameDoesNotMeetRequiredPattern(room.getName(), nonUniversityLocationRegex) : nonUniversityLocationInfo);
						    	}
					    	} catch (RoomException e) {
					    		throw e;
					    	} catch (Exception e) {
					    		throw new RoomException(MESSAGES.errorLocationNameDoesNotMeetRequiredPatternWithReason(room.getName(), nonUniversityLocationRegex, e.getMessage()), e);
					    	}
				    	}
					}
					((NonUniversityLocation)location).setName(room.getName());
				}
				location.setIgnoreTooFar(room.isIgnoreTooFar());
				location.setIgnoreRoomCheck(room.isIgnoreRoomCheck());
				location.setCoordinateX(room.getX());
				location.setCoordinateY(room.getY());
				location.setArea(room.getArea());
				location.setDisplayName(room.getDisplayName());
			}
			
			if (context.hasPermission(location, Right.RoomEditChangeCapacity) && FutureOperation.ROOM_PROPERTIES.in(flags)) {
				location.setCapacity(room.getCapacity());
			}
			
			if (context.hasPermission(location, Right.RoomEditChangeExternalId) && FutureOperation.ROOM_PROPERTIES.in(flags)) {
				location.setExternalUniqueId(room.getExternalId());
			}
			
			if (context.hasPermission(location, Right.RoomEditChangeType) && FutureOperation.ROOM_PROPERTIES.in(flags)) {
				RoomType type = (room.getRoomType() == null ? null : RoomTypeDAO.getInstance().get(room.getRoomType().getId(), hibSession));
				if (type != null && ((type.isRoom() && location instanceof Room) || !(type.isRoom() && location instanceof NonUniversityLocation)))
					location.setRoomType(type);
			}

            String oldNote = location.getNote();
            if (context.hasPermission(location, Right.RoomEditChangeEventProperties) && FutureOperation.EVENT_PROPERTIES.in(flags)) {
            	Department eventDepartment = lookuDepartment(hibSession, room.getEventDepartment(), future, location.getSession().getUniqueId());
            	if (!future || room.getEventDepartment() == null || eventDepartment != null) {
            		location.setEventDepartment(eventDepartment);
            	}
            	location.setBreakTime(room.getBreakTime());
            	if (!future)
            		location.setEventStatus(room.getEventStatus());
            	location.setNote(room.getEventNote() == null ? "" : room.getEventNote().length() > 2048 ? room.getEventNote().substring(0, 2048) : room.getEventNote());
            }
			
			if (context.hasPermission(location, Right.RoomEditChangeExaminationStatus) && FutureOperation.EXAM_PROPERTIES.in(flags)) {
				location.setExamCapacity(room.getExamCapacity());
				boolean examTypesChanged = false;
				List<ExamType> types = ExamType.findAll(hibSession);
				for (ExamType type: types) {
					if ((room.getExamType(type.getUniqueId()) != null) != location.getExamTypes().contains(type)) {
						examTypesChanged = true;
						break;
					}
				}
				if (examTypesChanged) {
	        		// Examination types has changed -- apply brute force to avoid unique constraint (PK_ROOM_EXAM_TYPE) violation
	            	if (!location.getExamTypes().isEmpty()) {
	            		location.getExamTypes().clear();
	            		hibSession.update(location); hibSession.flush();
	            	}
	            	for (ExamType type: types) {
	            		if (room.getExamType(type.getUniqueId()) != null)
	            			location.getExamTypes().add(type);
	            	}
	            }
			}
			
			if (context.hasPermission(Right.EditRoomDepartmentsExams) && FutureOperation.EXAM_PREFS.in(flags)) {
				location.getExamPreferences().clear();
				for (ExamType type: location.getExamTypes()) {
					PeriodPreferenceModel model = room.getPeriodPreferenceModel(type.getUniqueId());
					if (model != null)
						for (ExamPeriod period: (List<ExamPeriod>)hibSession.createQuery(
								"from ExamPeriod ep where ep.session.uniqueId=:sessionId and ep.examType.uniqueId=:typeId"
								).setLong("sessionId", location.getSession().getUniqueId()).setLong("typeId", type.getUniqueId()).setCacheable(true).list()) {
							PreferenceInterface pref = model.getPreference(period.getDateOffset(), period.getStartSlot());
							if (pref != null && !PreferenceLevel.sNeutral.equals(pref.getCode())) {
								ExamLocationPref ep = new ExamLocationPref();
								ep.setExamPeriod(period);
								ep.setPrefLevel(PreferenceLevel.getPreferenceLevel(pref.getCode()));
						        ep.setLocation(location);
						        location.getExamPreferences().add(ep);
							}
						}
				}
			}
			
			if (context.hasPermission(location, Right.RoomEditAvailability) && room.hasRoomSharingModel() && FutureOperation.ROOM_SHARING.in(flags)) {
				Map<Long, Character> dept2char = new HashMap<Long, Character>();
				dept2char.put(-1l, org.cpsolver.coursett.model.RoomSharingModel.sFreeForAllPrefChar);
				dept2char.put(-2l, org.cpsolver.coursett.model.RoomSharingModel.sNotAvailablePrefChar);
				String managerIds = ""; char pref = '0';
				Set<Department> add = new HashSet<Department>();
				Map<Long, Department> id2dept = new HashMap<Long, Department>();
				for (RoomSharingOption option: room.getRoomSharingModel().getOptions()) {
					if (option.getId() >= 0) {
						Department d = lookuDepartment(hibSession, option.getId(), future, location.getSession().getUniqueId());
						if (d != null) {
							id2dept.put(option.getId(), d);
							managerIds += (managerIds.isEmpty() ? "" : ",") + d.getUniqueId();
							dept2char.put(option.getId(), new Character(pref++));
							add.add(d);
						} else {
							dept2char.put(option.getId(), org.cpsolver.coursett.model.RoomSharingModel.sFreeForAllPrefChar);
						}
					}
				}
				
				String pattern = "";
				for (int d = 0; d < 7; d++)
					for (int s = 0; s < 288; s ++) {
						RoomSharingOption option = room.getRoomSharingModel().getOption(d, s);
						pattern += dept2char.get(option.getId());
					}
				
				location.setManagerIds(managerIds);
				location.setPattern(pattern);
					
				for (Iterator<RoomDept> i = location.getRoomDepts().iterator(); i.hasNext(); ) {
					RoomDept rd = (RoomDept)i.next();
					if (!add.remove(rd.getDepartment())) {
						rd.getDepartment().getRoomDepts().remove(rd);
						i.remove();
						hibSession.delete(rd);
					}
				}
				for (Department d: add) {
					RoomDept rd = new RoomDept();
					rd.setControl(false);
					rd.setDepartment(d);
					rd.getDepartment().getRoomDepts().add(rd);
					rd.setRoom(location);
					location.getRoomDepts().add(rd);
					hibSession.saveOrUpdate(rd);
				}
				
				if (room.getRoomSharingModel().isNoteEditable()) {
					if (room.getRoomSharingModel().hasNote())
						location.setShareNote(room.getRoomSharingModel().getNote().length() > 2048 ? room.getRoomSharingModel().getNote().substring(0, 2048) : room.getRoomSharingModel().getNote());
					else
						location.setShareNote(null);
				}
			}
			
			if (context.hasPermission(location, Right.RoomEditPreference) && room.hasRoomSharingModel() && FutureOperation.ROOM_SHARING.in(flags)) {
				Map<Long, PreferenceLevel> dept2pref = new HashMap<Long, PreferenceLevel>();
				for (RoomSharingOption option: room.getRoomSharingModel().getOptions()) {
					if (option.getId() >= 0 && option.isEditable() && option.hasPreference()) {
						Department d = lookuDepartment(hibSession, option.getId(), future, location.getSession().getUniqueId());
						if (d != null)
							dept2pref.put(d.getUniqueId(), PreferenceLevelDAO.getInstance().get(option.getPreference()));
					}
				}
				for (RoomDept rd: location.getRoomDepts()) {
					PreferenceLevel pref = dept2pref.get(rd.getDepartment().getUniqueId());
					
					RoomPref rp = null;
					for (RoomPref x: (Set<RoomPref>)rd.getDepartment().getRoomPreferences()) {
						if (x.getRoom().equals(location)) {
							rp = x; break;
						}
					}
					
					if (rp == null && pref != null && !pref.getPrefProlog().equals(PreferenceLevel.sNeutral)) {
						rp = new RoomPref();
						rp.setRoom(location);
						rp.setPrefLevel(pref);
						rp.setOwner(rd.getDepartment());
						rd.getDepartment().getPreferences().add(rp);
						rd.setPreference(pref);
						hibSession.saveOrUpdate(rp);
						hibSession.saveOrUpdate(rd.getDepartment());
					} else if (rp != null && (pref == null || pref.getPrefProlog().equals(PreferenceLevel.sNeutral))) {
						rd.getDepartment().getPreferences().remove(rp);
						rd.setPreference(null);
						hibSession.delete(rp);
						hibSession.saveOrUpdate(rd.getDepartment());
					} else if (rp != null && !rp.getPrefLevel().equals(pref)) {
						rp.setPrefLevel(pref);
						rd.setPreference(pref);
						hibSession.saveOrUpdate(rp);
					}
				}
			}
			
			if (context.hasPermission(location, Right.RoomEditFeatures) && FutureOperation.FEATURES.in(flags)) {
				boolean editGlobalFeatures = context.hasPermission(location, Right.RoomEditGlobalFeatures);
				boolean deptIndependent = context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
				Set<RoomFeature> features = new HashSet<RoomFeature>(location.getFeatures());
				for (FeatureInterface f: room.getFeatures()) {
					RoomFeature feature = lookupFeature(hibSession, f, future, location.getSession().getUniqueId());
					if (feature != null && !features.remove(feature)) {
						if (feature instanceof GlobalRoomFeature && !editGlobalFeatures) continue;
						if (feature instanceof DepartmentRoomFeature && !deptIndependent && !context.getUser().getCurrentAuthority().hasQualifier(((DepartmentRoomFeature)feature).getDepartment())) continue;
						location.getFeatures().add(feature);
						feature.getRooms().add(location);
						hibSession.saveOrUpdate(feature);
					}
				}
				for (RoomFeature feature: features) {
					if (feature instanceof GlobalRoomFeature && !editGlobalFeatures) continue;
					if (feature instanceof DepartmentRoomFeature && !deptIndependent && !context.getUser().getCurrentAuthority().hasQualifier(((DepartmentRoomFeature)feature).getDepartment())) continue;
					if (future) {
						FeatureInterface f = new FeatureInterface(feature.getUniqueId(), feature.getAbbv(), feature.getLabel());
						if (feature instanceof DepartmentRoomFeature)
							f.setDepartment(new DepartmentInterface());
						if (lookupFeature(hibSession, f, future, context.getUser().getCurrentAcademicSessionId()) == null) continue;
					}
					location.getFeatures().remove(feature);
					feature.getRooms().remove(location);
					hibSession.saveOrUpdate(feature);
				}
			}
			
			if (context.hasPermission(location, Right.RoomEditGroups) && FutureOperation.GROUPS.in(flags)) {
				boolean editGlobalGroups = context.hasPermission(location, Right.RoomEditGlobalGroups);
				boolean deptIndependent = context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
				Set<RoomGroup> groups = new HashSet<RoomGroup>(location.getRoomGroups());
				for (GroupInterface g: room.getGroups()) {
					RoomGroup group = lookupGroup(hibSession, g, future, location.getSession().getUniqueId());
					if (group != null && !groups.remove(group)) {
						if (group.isGlobal() && !editGlobalGroups) continue;
						if (!group.isGlobal() && !deptIndependent && !context.getUser().getCurrentAuthority().hasQualifier(group.getDepartment())) continue;
						location.getRoomGroups().add(group);
						group.getRooms().add(location);
						hibSession.saveOrUpdate(group);
					}
				}
				for (RoomGroup group: groups) {
					if (group.isGlobal() && !editGlobalGroups) continue;
					if (!group.isGlobal() && !deptIndependent && !context.getUser().getCurrentAuthority().hasQualifier(group.getDepartment())) continue;
					if (future) {
						GroupInterface g = new GroupInterface(group.getUniqueId(), group.getAbbv(), group.getName());
						if (!group.isGlobal()) g.setDepartment(new DepartmentInterface());
						if (lookupGroup(hibSession, g, future, context.getUser().getCurrentAcademicSessionId()) == null) continue;
					}
					location.getRoomGroups().remove(group);
					group.getRooms().remove(location);
					hibSession.saveOrUpdate(group);
				}
			}
			
			if (context.hasPermission(location, Right.RoomEditChangeControll) && FutureOperation.ROOM_PROPERTIES.in(flags)) {
				Department d = lookuDepartment(hibSession, room.getControlDepartment(), future, location.getSession().getUniqueId());
				if (!future || room.getControlDepartment() == null || d != null)
					for (RoomDept rd: location.getRoomDepts())
						rd.setControl(rd.getDepartment().equals(d));
			}
			
			if (context.hasPermission(location, Right.RoomEditEventAvailability) && room.hasEventAvailabilityModel() && FutureOperation.EVENT_AVAILABILITY.in(flags)) {
				String availability = "";
				for (int d = 0; d < 7; d++)
					for (int s = 0; s < 288; s ++) {
						RoomSharingOption option = room.getEventAvailabilityModel().getOption(d, s);
						availability += (option.getId() == -1l ? '0' : '1');
					}
				location.setEventAvailability(availability);
			}
			
			hibSession.saveOrUpdate(location);
			for (RoomDept rd: location.getRoomDepts())
				hibSession.saveOrUpdate(rd);
			
        	if (context.hasPermission(location, Right.RoomEditChangeEventProperties) && !ToolBox.equals(oldNote, location.getNote()) && FutureOperation.EVENT_PROPERTIES.in(flags))
        		ChangeLog.addChange(hibSession, context, location, (location.getNote() == null || location.getNote().isEmpty() ? "-" : location.getNote()), ChangeLog.Source.ROOM_EDIT, ChangeLog.Operation.NOTE, null, location.getControllingDepartment());
			
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    (Location)location, 
                    ChangeLog.Source.ROOM_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    location.getControllingDepartment());
            
			if (context.hasPermission(location, Right.RoomEditChangePicture) && FutureOperation.PICTURES.in(flags)) {
				Map<Long, LocationPicture> temp = (Map<Long, LocationPicture>)context.getAttribute(RoomPictureServlet.TEMP_ROOM_PICTURES);
				Map<Long, LocationPicture> pictures = new HashMap<Long, LocationPicture>();
				if (future) {
					Set<LocationPicture> otherPictures = new HashSet<LocationPicture>(location.getPictures());
					p1: for (RoomPictureInterface p1: room.getPictures()) {
						for (Iterator<LocationPicture> i = otherPictures.iterator(); i.hasNext(); ) {
							LocationPicture p2 = i.next();
							if (samePicture(p1, p2)) {
								i.remove();
								continue p1;
							}
						}
						if (location instanceof Room) {
							RoomPicture original = RoomPictureDAO.getInstance().get(p1.getUniqueId());
							if (original != null) {
								RoomPicture p2 = original.clonePicture();
								p2.setLocation(location);
								((Room)location).getPictures().add(p2);
								hibSession.saveOrUpdate(p2);
							}
						} else {
							NonUniversityLocationPicture original = NonUniversityLocationPictureDAO.getInstance().get(p1.getUniqueId());
							if (original != null) {
								NonUniversityLocationPicture p2 = original.clonePicture();
								p2.setLocation(location);
								((NonUniversityLocation)location).getPictures().add(p2);
								hibSession.saveOrUpdate(p2);
							}
						}
					}
					for (LocationPicture picture: otherPictures) {
						location.getPictures().remove(picture);
						hibSession.delete(picture);
					}
				} else {
					for (LocationPicture p: location.getPictures())
						pictures.put(p.getUniqueId(), p);
					for (RoomPictureInterface p: room.getPictures()) {
						LocationPicture picture = pictures.remove(p.getUniqueId());
						if (picture == null && temp != null) {
							picture = temp.get(p.getUniqueId());
							if (picture != null) {
								if (location instanceof Room) {
									((RoomPicture)picture).setLocation((Room)location);
									((Room)location).getPictures().add((RoomPicture)picture);
								} else {
									((NonUniversityLocationPicture)picture).setLocation((NonUniversityLocation)location);
									((NonUniversityLocation)location).getPictures().add((NonUniversityLocationPicture)picture);
								}
								hibSession.saveOrUpdate(picture);
								p.setUniqueId(picture.getUniqueId());
							}
						}
					}
					for (LocationPicture picture: pictures.values()) {
						location.getPictures().remove(picture);
						hibSession.delete(picture);
					}
				}
			}

            hibSession.flush();
			
			tx.commit(); tx = null;
			return location;
		} catch (Throwable t) {
			if (future)
				throw new RoomException(MESSAGES.failedUpdateLocation(roomName, sessionLabel, t.getMessage()), t);
			else
				throw new GwtRpcException(t.getMessage(), t);
		} finally {
			if (tx != null && tx.isActive()) tx.rollback();
		}
	}
	
	protected Location create(RoomDetailInterface room, SessionContext context, Long sessionId, Long permId, int flags) {
		Transaction tx = null;
		String roomName = room.getLabel(), sessionLabel = null;
		boolean future = (sessionId != null);
		org.hibernate.Session hibSession = LocationDAO.getInstance().getSession();
		try {
			tx = hibSession.beginTransaction();
			Session session = SessionDAO.getInstance().get(sessionId == null ? context.getUser().getCurrentAcademicSessionId() : sessionId, hibSession);
			if (session == null) return null;
			sessionLabel = session.getLabel();
			if (future) {
				if (room.getRoomType().isRoom()) {
					if (!context.hasPermission(Right.AddRoom)) return null;
				} else {
					if (!context.hasPermission(Right.AddNonUnivLocation)) return null;
				}
			} else {
				if (room.getRoomType().isRoom())
					context.checkPermission(Right.AddRoom);
				else
					context.checkPermission(Right.AddNonUnivLocation);
			}
			
			if (room.getRoomType().isRoom()) {
				
			}
			
			Location location = null;
			if (room.getRoomType().isRoom()) {
				Room r = new Room();
				Building b = lookupBuilding(hibSession, room.getBuilding(), future, session.getUniqueId());
				if (b == null) throw new RoomException(MESSAGES.errorBuildingNotExist(room.getBuilding().getAbbreviation()));
				Room other = Room.findByBldgIdRoomNbr(b.getUniqueId(), room.getName(), session.getUniqueId());
				if (other != null) throw new RoomException(MESSAGES.errorRoomAlreadyExists(roomName));
				r.setBuilding(b);
				r.setBuildingAbbv(b.getAbbreviation());
				r.setRoomNumber(room.getName());
				r.setPictures(new HashSet<RoomPicture>());
				location = r;
			} else {
				String nonUniversityLocationRegex = ApplicationProperty.NonUniversityLocationPattern.value();
		    	String nonUniversityLocationInfo = ApplicationProperty.NonUniversityLocationPatternInfo.value();
		    	if (nonUniversityLocationRegex != null && nonUniversityLocationRegex.trim().length() > 0) {
			    	try { 
				    	Pattern pattern = Pattern.compile(nonUniversityLocationRegex);
				    	Matcher matcher = pattern.matcher(room.getName());
				    	if (!matcher.find()) {
				    		throw new RoomException(nonUniversityLocationInfo == null || nonUniversityLocationInfo.isEmpty() ?
				    				MESSAGES.errorLocationNameDoesNotMeetRequiredPattern(room.getName(), nonUniversityLocationRegex) : nonUniversityLocationInfo);
				    	}
			    	} catch (RoomException e) {
			    		throw e;
			    	} catch (Exception e) {
			    		throw new RoomException(MESSAGES.errorLocationNameDoesNotMeetRequiredPatternWithReason(room.getName(), nonUniversityLocationRegex, e.getMessage()), e);
			    	}
		    	}
				NonUniversityLocation r = new NonUniversityLocation();
				r.setPictures(new HashSet<NonUniversityLocationPicture>());
				r.setName(room.getName());
			}
			location.setSession(session);
			location.setIgnoreTooFar(room.isIgnoreTooFar());
			location.setIgnoreRoomCheck(room.isIgnoreRoomCheck());
			location.setCoordinateX(room.getX());
			location.setCoordinateY(room.getY());
			location.setArea(room.getArea());
			location.setDisplayName(room.getDisplayName());
			location.setCapacity(room.getCapacity());
			location.setRoomDepts(new HashSet<RoomDept>());
			location.setExamPreferences(new HashSet<ExamLocationPref>());
			location.setExamTypes(new HashSet<ExamType>());
			location.setRoomDepts(new HashSet<RoomDept>());
			location.setRoomGroups(new HashSet<RoomGroup>());
			location.setFeatures(new HashSet<RoomFeature>());
			location.setRoomType(room.getRoomType() == null ? null : RoomTypeDAO.getInstance().get(room.getRoomType().getId(), hibSession));
			
			if (permId == null)
				LocationPermIdGenerator.setPermanentId(location);
			else
				location.setPermanentId(permId);
			
			if (room.hasRoomSharingModel() && FutureOperation.ROOM_SHARING.in(flags)) {
				Map<Long, Character> dept2char = new HashMap<Long, Character>();
				dept2char.put(-1l, org.cpsolver.coursett.model.RoomSharingModel.sFreeForAllPrefChar);
				dept2char.put(-2l, org.cpsolver.coursett.model.RoomSharingModel.sNotAvailablePrefChar);
				String managerIds = ""; char pref = '0';
				Set<Department> add = new HashSet<Department>();
				Map<Long, Department> id2dept = new HashMap<Long, Department>();
				for (RoomSharingOption option: room.getRoomSharingModel().getOptions()) {
					if (option.getId() >= 0) {
						Department d = lookuDepartment(hibSession, option.getId(), future, session.getUniqueId());
						if (d != null) {
							id2dept.put(option.getId(), d);
							managerIds += (managerIds.isEmpty() ? "" : ",") + d.getUniqueId();
							dept2char.put(option.getId(), new Character(pref++));
							add.add(d);
						} else {
							dept2char.put(option.getId(), org.cpsolver.coursett.model.RoomSharingModel.sFreeForAllPrefChar);
						}
					}
				}
				
				String pattern = "";
				for (int d = 0; d < 7; d++)
					for (int s = 0; s < 288; s ++) {
						RoomSharingOption option = room.getRoomSharingModel().getOption(d, s);
						pattern += dept2char.get(option.getId());
					}
				
				location.setManagerIds(managerIds);
				location.setPattern(pattern);
					
				Department control = lookuDepartment(hibSession, room.getControlDepartment(), future, session.getUniqueId());
				for (Department d: add) {
					RoomDept rd = new RoomDept();
					rd.setControl(d.equals(control));
					rd.setDepartment(d);
					rd.getDepartment().getRoomDepts().add(rd);
					rd.setRoom(location);
					location.getRoomDepts().add(rd);
				}
				
				if (room.getRoomSharingModel().isNoteEditable()) {
					if (room.getRoomSharingModel().hasNote())
						location.setShareNote(room.getRoomSharingModel().getNote().length() > 2048 ? room.getRoomSharingModel().getNote().substring(0, 2048) : room.getRoomSharingModel().getNote());
					else
						location.setShareNote(null);
				}
			} else if (room.getControlDepartment() != null) {
				Department dept = lookuDepartment(hibSession, room.getControlDepartment(), future, session.getUniqueId());
				if (dept != null) {
					RoomDept rd = new RoomDept();
					rd.setControl(true);
					rd.setDepartment(dept);
					rd.getDepartment().getRoomDepts().add(rd);
					rd.setRoom(location);
					location.getRoomDepts().add(rd);
				}
			}
			
			hibSession.save(location);
			
			if (context.hasPermission(location, Right.RoomEditChangeExternalId) && FutureOperation.ROOM_PROPERTIES.in(flags)) {
				location.setExternalUniqueId(room.getExternalId());
			}
			
            String oldNote = location.getNote();
            if (context.hasPermission(location, Right.RoomEditChangeEventProperties) && FutureOperation.EVENT_PROPERTIES.in(flags)) {
            	Department eventDepartment = lookuDepartment(hibSession, room.getEventDepartment(), future, session.getUniqueId());
            	if (!future || room.getEventDepartment() == null || eventDepartment != null) {
            		location.setEventDepartment(eventDepartment);
            	}
            	location.setBreakTime(room.getBreakTime());
            	if (!future)
            		location.setEventStatus(room.getEventStatus());
            	location.setNote(room.getEventNote() == null ? "" : room.getEventNote().length() > 2048 ? room.getEventNote().substring(0, 2048) : room.getEventNote());
            }
			
			if (context.hasPermission(location, Right.RoomEditChangeExaminationStatus) && FutureOperation.EXAM_PROPERTIES.in(flags)) {
				location.setExamCapacity(room.getExamCapacity());
				List<ExamType> types = ExamType.findAll(hibSession);
            	for (ExamType type: types) {
            		if (room.getExamType(type.getUniqueId()) != null)
            			location.getExamTypes().add(type);
            	}
			}
			
			if (context.hasPermission(Right.EditRoomDepartmentsExams) && FutureOperation.EXAM_PREFS.in(flags)) {
				location.getExamPreferences().clear();
				for (ExamType type: location.getExamTypes()) {
					PeriodPreferenceModel model = room.getPeriodPreferenceModel(type.getUniqueId());
					if (model != null)
						for (ExamPeriod period: (List<ExamPeriod>)hibSession.createQuery(
								"from ExamPeriod ep where ep.session.uniqueId=:sessionId and ep.examType.uniqueId=:typeId"
								).setLong("sessionId", session.getUniqueId()).setLong("typeId", type.getUniqueId()).setCacheable(true).list()) {
							PreferenceInterface pref = model.getPreference(period.getDateOffset(), period.getStartSlot());
							if (pref != null && !PreferenceLevel.sNeutral.equals(pref.getCode())) {
								ExamLocationPref ep = new ExamLocationPref();
								ep.setExamPeriod(period);
								ep.setPrefLevel(PreferenceLevel.getPreferenceLevel(pref.getCode()));
						        ep.setLocation(location);
						        location.getExamPreferences().add(ep);
							}
						}
				}
			}
			
			if (context.hasPermission(location, Right.RoomEditPreference) && room.hasRoomSharingModel() && FutureOperation.ROOM_SHARING.in(flags)) {
				Map<Long, PreferenceLevel> dept2pref = new HashMap<Long, PreferenceLevel>();
				for (RoomSharingOption option: room.getRoomSharingModel().getOptions()) {
					if (option.getId() >= 0 && option.isEditable() && option.hasPreference()) {
						Department d = lookuDepartment(hibSession, option.getId(), future, location.getSession().getUniqueId());
						if (d != null)
							dept2pref.put(d.getUniqueId(), PreferenceLevelDAO.getInstance().get(option.getPreference()));
					}
				}
				for (RoomDept rd: location.getRoomDepts()) {
					PreferenceLevel pref = dept2pref.get(rd.getDepartment().getUniqueId());
					
					if (pref != null && !pref.getPrefProlog().equals(PreferenceLevel.sNeutral)) {
						RoomPref rp = new RoomPref();
						rp.setRoom(location);
						rp.setPrefLevel(pref);
						rp.setOwner(rd.getDepartment());
						rd.getDepartment().getPreferences().add(rp);
						rd.setPreference(pref);
						hibSession.saveOrUpdate(rp);
						hibSession.saveOrUpdate(rd.getDepartment());
					}
				}
			}
			
			if (context.hasPermission(location, Right.RoomEditFeatures) && FutureOperation.FEATURES.in(flags)) {
				boolean editGlobalFeatures = context.hasPermission(location, Right.RoomEditGlobalFeatures);
				boolean deptIndependent = context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
				Set<RoomFeature> features = new HashSet<RoomFeature>(location.getFeatures());
				for (FeatureInterface f: room.getFeatures()) {
					RoomFeature feature = lookupFeature(hibSession, f, future, location.getSession().getUniqueId());
					if (feature != null && !features.remove(feature.getUniqueId())) {
						if (feature instanceof GlobalRoomFeature && !editGlobalFeatures) continue;
						if (feature instanceof DepartmentRoomFeature && !deptIndependent && !context.getUser().getCurrentAuthority().hasQualifier(((DepartmentRoomFeature)feature).getDepartment())) continue;
						location.getFeatures().add(feature);
						feature.getRooms().add(location);
						hibSession.saveOrUpdate(feature);
					}
				}
				for (RoomFeature feature: features) {
					if (feature instanceof GlobalRoomFeature && !editGlobalFeatures) continue;
					if (feature instanceof DepartmentRoomFeature && !deptIndependent && !context.getUser().getCurrentAuthority().hasQualifier(((DepartmentRoomFeature)feature).getDepartment())) continue;
					if (future && lookupFeature(hibSession, new FeatureInterface(feature.getUniqueId(), feature.getAbbv(), feature.getLabel()), future, context.getUser().getCurrentAcademicSessionId()) == null) continue;
					location.getFeatures().remove(feature);
					feature.getRooms().remove(location);
					hibSession.saveOrUpdate(feature);
				}
			}
			
			if (context.hasPermission(location, Right.RoomEditGroups) && FutureOperation.GROUPS.in(flags)) {
				boolean editGlobalGroups = context.hasPermission(location, Right.RoomEditGlobalGroups);
				boolean deptIndependent = context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
				Set<RoomGroup> groups = new HashSet<RoomGroup>(location.getRoomGroups());
				for (GroupInterface g: room.getGroups()) {
					RoomGroup group = lookupGroup(hibSession, g, future, location.getSession().getUniqueId());
					if (group != null && !groups.remove(group)) {
						if (group.isGlobal() && !editGlobalGroups) continue;
						if (!group.isGlobal() && !deptIndependent && !context.getUser().getCurrentAuthority().hasQualifier(group.getDepartment())) continue;
						location.getRoomGroups().add(group);
						group.getRooms().add(location);
						hibSession.saveOrUpdate(group);
					}
				}
				for (RoomGroup group: groups) {
					if (group.isGlobal() && !editGlobalGroups) continue;
					if (!group.isGlobal() && !deptIndependent && !context.getUser().getCurrentAuthority().hasQualifier(group.getDepartment())) continue;
					if (future && lookupGroup(hibSession, new GroupInterface(group.getUniqueId(), group.getAbbv(), group.getName()), future, context.getUser().getCurrentAcademicSessionId()) == null) continue;
					location.getRoomGroups().remove(group);
					group.getRooms().remove(location);
					hibSession.saveOrUpdate(group);
				}
			}
			
			if (context.hasPermission(location, Right.RoomEditEventAvailability) && room.hasEventAvailabilityModel() && FutureOperation.EVENT_AVAILABILITY.in(flags)) {
				String availability = "";
				for (int d = 0; d < 7; d++)
					for (int s = 0; s < 288; s ++) {
						RoomSharingOption option = room.getEventAvailabilityModel().getOption(d, s);
						availability += (option.getId() == -1l ? '0' : '1');
					}
				location.setEventAvailability(availability);
			}
			
			hibSession.saveOrUpdate(location);
			for (RoomDept rd: location.getRoomDepts())
				hibSession.saveOrUpdate(rd);
			
        	if (context.hasPermission(location, Right.RoomEditChangeEventProperties) && !ToolBox.equals(oldNote, location.getNote()) && FutureOperation.EVENT_PROPERTIES.in(flags))
        		ChangeLog.addChange(hibSession, context, location, (location.getNote() == null || location.getNote().isEmpty() ? "-" : location.getNote()), ChangeLog.Source.ROOM_EDIT, ChangeLog.Operation.NOTE, null, location.getControllingDepartment());
			
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    (Location)location, 
                    ChangeLog.Source.ROOM_EDIT, 
                    ChangeLog.Operation.CREATE, 
                    null, 
                    location.getControllingDepartment());
            
			if (context.hasPermission(location, Right.RoomEditChangePicture) && FutureOperation.PICTURES.in(flags)) {
				Map<Long, LocationPicture> temp = (Map<Long, LocationPicture>)context.getAttribute(RoomPictureServlet.TEMP_ROOM_PICTURES);
				Map<Long, LocationPicture> pictures = new HashMap<Long, LocationPicture>();
				if (future) {
					Set<LocationPicture> otherPictures = new HashSet<LocationPicture>(location.getPictures());
					p1: for (RoomPictureInterface p1: room.getPictures()) {
						for (Iterator<LocationPicture> i = otherPictures.iterator(); i.hasNext(); ) {
							LocationPicture p2 = i.next();
							if (samePicture(p1, p2)) {
								i.remove();
								continue p1;
							}
						}
						if (location instanceof Room) {
							RoomPicture original = RoomPictureDAO.getInstance().get(p1.getUniqueId());
							if (original != null) {
								RoomPicture p2 = original.clonePicture();
								p2.setLocation(location);
								((Room)location).getPictures().add(p2);
								hibSession.saveOrUpdate(p2);
							}
						} else {
							NonUniversityLocationPicture original = NonUniversityLocationPictureDAO.getInstance().get(p1.getUniqueId());
							if (original != null) {
								NonUniversityLocationPicture p2 = original.clonePicture();
								p2.setLocation(location);
								((NonUniversityLocation)location).getPictures().add(p2);
								hibSession.saveOrUpdate(p2);
							}
						}
					}
					for (LocationPicture picture: otherPictures) {
						location.getPictures().remove(picture);
						hibSession.delete(picture);
					}
				} else {
					for (LocationPicture p: location.getPictures())
						pictures.put(p.getUniqueId(), p);
					for (RoomPictureInterface p: room.getPictures()) {
						LocationPicture picture = pictures.remove(p.getUniqueId());
						if (picture == null && temp != null) {
							picture = temp.get(p.getUniqueId());
							if (picture != null) {
								if (location instanceof Room) {
									((RoomPicture)picture).setLocation((Room)location);
									((Room)location).getPictures().add((RoomPicture)picture);
								} else {
									NonUniversityLocationPicture np = new NonUniversityLocationPicture();
									np.setDataFile(picture.getDataFile());
									np.setFileName(picture.getFileName());
									np.setContentType(picture.getContentType());
									np.setTimeStamp(picture.getTimeStamp());
									np.setLocation((NonUniversityLocation)location);
									((NonUniversityLocation)location).getPictures().add(np);
								}
								hibSession.saveOrUpdate(picture);
								p.setUniqueId(picture.getUniqueId());
							}
						}
					}
					for (LocationPicture picture: pictures.values()) {
						location.getPictures().remove(picture);
						hibSession.delete(picture);
					}
				}
			}

            hibSession.flush();
			
			tx.commit(); tx = null;
			return location;
		} catch (Throwable t) {
			if (future)
				throw new RoomException(MESSAGES.failedCreateLocation(roomName, sessionLabel, t.getMessage()), t);
			else
				throw new GwtRpcException(t.getMessage(), t);
		} finally {
			if (tx != null && tx.isActive()) tx.rollback();
		}
	}

	protected boolean samePicture(RoomPictureInterface p1, LocationPicture p2) {
		return p1.getName().equals(p2.getFileName()) && Math.abs(p1.getTimeStamp() - p2.getTimeStamp().getTime()) < 1000 && p1.getType().equals(p2.getContentType());
	}
	
	protected Building lookupBuilding(org.hibernate.Session hibSession, BuildingInterface original, boolean future, Long sessionId) {
		if (original == null) return null;
		if (future) {
			return (Building)hibSession.createQuery(
					"select b from Building b where b.abbreviation = :abbreviation and b.session.uniqueId = :sessionId")
					.setLong("sessionId", sessionId).setString("abbreviation", original.getAbbreviation()).setCacheable(true).setMaxResults(1).uniqueResult();
		} else {
			return BuildingDAO.getInstance().get(original.getId(), hibSession);
		}
	}
	
	protected Department lookuDepartment(org.hibernate.Session hibSession, DepartmentInterface original, boolean future, Long sessionId) {
		if (original == null) return null;
		if (future) {
			return Department.findByDeptCode(original.getDeptCode(), sessionId, hibSession);
		} else {
			return DepartmentDAO.getInstance().get(original.getId(), hibSession);
		}
	}
	
	protected RoomFeature lookupFeature(org.hibernate.Session hibSession, FeatureInterface original, boolean future, Long sessionId) {
		if (original == null) return null;
		if (future) {
			if (original.isDepartmental())
				return (DepartmentRoomFeature)hibSession.createQuery(
					"select f from DepartmentRoomFeature f, DepartmentRoomFeature o where o.uniqueId = :originalId and f.department.session.uniqueId = :sessionId " +
					"and f.abbv = o.abbv and f.department.deptCode = o.department.deptCode")
					.setLong("sessionId", sessionId).setLong("originalId", original.getId()).setCacheable(true).setMaxResults(1).uniqueResult();
			else
				return (GlobalRoomFeature)hibSession.createQuery(
					"select f from GlobalRoomFeature f, GlobalRoomFeature o where o.uniqueId = :originalId and f.session.uniqueId = :sessionId " +
					"and f.abbv = o.abbv")
					.setLong("sessionId", sessionId).setLong("originalId", original.getId()).setCacheable(true).setMaxResults(1).uniqueResult();
		} else {
			return RoomFeatureDAO.getInstance().get(original.getId(), hibSession);
		}
	}
	
	protected RoomGroup lookupGroup(org.hibernate.Session hibSession, GroupInterface original, boolean future, Long sessionId) {
		if (original == null) return null;
		if (future) {
			if (original.isDepartmental())
				return (RoomGroup)hibSession.createQuery(
					"select g from RoomGroup g, RoomGroup o where o.uniqueId = :originalId and g.department.session.uniqueId = :sessionId " +
					"and g.abbv = o.abbv and g.department.deptCode = o.department.deptCode and g.global = false")
					.setLong("sessionId", sessionId).setLong("originalId", original.getId()).setCacheable(true).setMaxResults(1).uniqueResult();
			else
				return (RoomGroup)hibSession.createQuery(
					"select g from RoomGroup g, RoomGroup o where o.uniqueId = :originalId and g.session.uniqueId = :sessionId " +
					"and g.abbv = o.abbv and g.global = true")
					.setLong("sessionId", sessionId).setLong("originalId", original.getId()).setCacheable(true).setMaxResults(1).uniqueResult();
		} else {
			return RoomGroupDAO.getInstance().get(original.getId(), hibSession);
		}
	}
	
	protected Department lookuDepartment(org.hibernate.Session hibSession, Department original, boolean future, Long sessionId) {
		if (original == null) return null;
		if (future) {
			return (Department)hibSession.createQuery(
				"select d from Department d, Department o where d.deptCode = o.deptCode and d.session.uniqueId = :sessionId and o.uniqueId = :originalId")
				.setLong("sessionId", sessionId).setLong("originalId", original.getUniqueId()).setCacheable(true).setMaxResults(1).uniqueResult();
		} else {
			return original;
		}
	}
	
	protected Department lookuDepartment(org.hibernate.Session hibSession, Long originalId, boolean future, Long sessionId) {
		if (originalId == null) return null;
		if (future) {
			return (Department)hibSession.createQuery(
				"select distinct d from Department d, Department o where d.deptCode = o.deptCode and d.session.uniqueId = :sessionId and o.uniqueId = :originalId")
				.setLong("sessionId", sessionId).setLong("originalId", originalId).setCacheable(true).setMaxResults(1).uniqueResult();
		} else {
			return DepartmentDAO.getInstance().get(originalId, hibSession);
		}
	}
}
