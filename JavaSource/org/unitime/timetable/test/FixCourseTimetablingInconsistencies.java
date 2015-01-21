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
package org.unitime.timetable.test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.util.DistanceMetric;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.RoomSharingModel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * Fix the most common course timetabling inconsistencies in the data. These are usually caused either by the use
 * of the interactive solver (breaking hard constraints like required time or room) or by changing
 * of the input data (e.g., class limit) after a solution is committed. 
 * 
 * @author Tomas Muller
 */
public class FixCourseTimetablingInconsistencies {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static Logger sLog = Logger.getLogger(FixCourseTimetablingInconsistencies.class);

    private Long iSessionId;
    private org.hibernate.Session iHibSession;
    private DistanceMetric iDistanceMetric;
    
	public FixCourseTimetablingInconsistencies(Long sessionId) {
		iSessionId = sessionId;
		iDistanceMetric = new DistanceMetric(
				DistanceMetric.Ellipsoid.valueOf(ApplicationProperty.DistanceEllipsoid.value()));
	}
	
	public void fixAll(org.hibernate.Session hibSession) {
		iHibSession = hibSession;
		List<Assignment> assignments = (List<Assignment>)hibSession.createQuery(
				"select a from Assignment a " + 
				"where a.solution.commited = true and a.solution.owner.session.uniqueId = :sessionId")
				.setLong("sessionId", iSessionId).list();
		Hashtable<Location, List<Assignment>> roomAssignments = new Hashtable<Location, List<Assignment>>();
		Hashtable<String, List<Assignment>> instructorAssignments = new Hashtable<String, List<Assignment>>();
		for (Assignment a: assignments) {
			Class_ c = a.getClazz();
			boolean save = false;
			
			fixInstructors(c, a);
			fixRoomSharing(c, a);
			
			if (fixDatePattern(c, a)) save = true;
			if (fixRequiredTime(c, a)) save = true;
			if (fixNrAssignedRooms(c, a)) save = true;
			if (fixRequiredRoom(c, a)) save = true;
			if (fixRequiredBuilding(c, a)) save = true;
			if (fixRoomSize(c, a)) save = true;
			
			fixRequiredFeatures(c, a);
			fixRequiredGroups(c, a);
			
			for (Location loc: a.getRooms()) {
				List<Assignment> ax = roomAssignments.get(loc);
				if (ax == null) {
					ax = new ArrayList<Assignment>();
					roomAssignments.put(loc, ax);
				}
				ax.add(a);
			}
			
			for (DepartmentalInstructor ins: a.getInstructors()) {
				if (ins.getExternalUniqueId() == null || ins.getExternalUniqueId().isEmpty()) continue;
				List<Assignment> ax = instructorAssignments.get(ins.getExternalUniqueId());
				if (ax == null) {
					ax = new ArrayList<Assignment>();
					instructorAssignments.put(ins.getExternalUniqueId(), ax);
				}
				ax.add(a);
			}
			
			if (save) {
				hibSession.saveOrUpdate(c);
			}
		}
		
		checkRoomConstraints(roomAssignments);
		
		checkInstructorAssignments(instructorAssignments);
		
		hibSession.flush();
	}
	
	private void checkRoomConstraints(Hashtable<Location, List<Assignment>> roomAssignments) {
		for (Map.Entry<Location, List<Assignment>> entry: roomAssignments.entrySet()) {
			Location location = entry.getKey();
			if (location.isIgnoreRoomCheck()) continue;
			List<Assignment> ax = entry.getValue();
			DistributionType canShareRoomType = (DistributionType)iHibSession.createQuery(
					"select d from DistributionType d where d.reference = :type").setString("type", "CAN_SHARE_ROOM").uniqueResult();
			for (Assignment a: ax) {
				b: for (Assignment b: ax) {
					if (a.getUniqueId() >= b.getUniqueId()) continue;
					if (a.getTimeLocation().hasIntersection(b.getTimeLocation())) {
						Set<DistributionPref> dist = a.getClazz().effectivePreferences(DistributionPref.class);
						for (DistributionPref d: dist) {
							if (d.getDistributionType().getReference().equals("CAN_SHARE_ROOM") || d.getDistributionType().getReference().equals("MEET_WITH")) {
								for (DistributionObject o: d.getDistributionObjects()) {
									if (o.getPrefGroup().equals(b.getClazz()) || o.getPrefGroup().equals(b.getClazz().getSchedulingSubpart())) {
										int minSize = Math.round(a.getClazz().getRoomRatio() * a.getClazz().getExpectedCapacity() +
												b.getClazz().getRoomRatio() * b.getClazz().getExpectedCapacity());
										if (location.getCapacity() < minSize) {
											sLog.info("Allowed overlap of classes in room " + location.getLabel() + ":\n" + 
													"  " + a.getClazz().getClassLabel() + " " + a.getTimeLocation().getLongName(CONSTANTS.useAmPm()) + "\n" +
													"  " + b.getClazz().getClassLabel() + " " + b.getTimeLocation().getLongName(CONSTANTS.useAmPm()));
											sLog.warn("But the he room is too small (" + location.getCapacity() + " < " + minSize + ")");
											float ratio = ((float)location.getCapacity()) / minSize;
											a.getClazz().setRoomRatio(a.getClazz().getRoomRatio() * ratio);
											b.getClazz().setRoomRatio(b.getClazz().getRoomRatio() * ratio);
											iHibSession.saveOrUpdate(a.getClazz());
											iHibSession.saveOrUpdate(b.getClazz());
										}
										continue b;
									}
								}
							}
						}
						sLog.warn("Overlapping classes in room " + location.getLabel() + ":\n" + 
								"  " + a.getClazz().getClassLabel() + " " + a.getTimeLocation().getLongName(CONSTANTS.useAmPm()) + "\n" +
								"  " + b.getClazz().getClassLabel() + " " + b.getTimeLocation().getLongName(CONSTANTS.useAmPm()));
						/*
						for (RoomPref p: (Set<RoomPref>)a.getClazz().effectivePreferences(RoomPref.class))
							if (p.weakenHardPreferences())
								hibSession.save(p);
						for (RoomPref p: (Set<RoomPref>)b.getClazz().effectivePreferences(RoomPref.class))
							if (p.weakenHardPreferences())
								hibSession.save(p);
						*/
						int minSize = Math.round(a.getClazz().getRoomRatio() * a.getClazz().getExpectedCapacity()) + Math.round(b.getClazz().getRoomRatio() * b.getClazz().getExpectedCapacity());
						if (location.getCapacity() < minSize) {
							sLog.warn("Also the room is too small (" + location.getCapacity() + " < " + minSize + ")");
							float ratio = ((float)location.getCapacity()) / minSize;
							a.getClazz().setRoomRatio(a.getClazz().getRoomRatio() * ratio);
							b.getClazz().setRoomRatio(b.getClazz().getRoomRatio() * ratio);
							iHibSession.saveOrUpdate(a.getClazz());
							iHibSession.saveOrUpdate(b.getClazz());
						}
						DistributionPref dp = new DistributionPref();
						dp.setDistributionType(canShareRoomType);
						dp.setOwner(a.getClazz().getManagingDept());
						dp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
						dp.setDistributionObjects(new HashSet<DistributionObject>());
						dp.setGrouping(DistributionPref.sGroupingNone);
						DistributionObject o1 = new DistributionObject();
						o1.setDistributionPref(dp);
						o1.setPrefGroup(a.getClazz());
						o1.setSequenceNumber(1);
						dp.getDistributionObjects().add(o1);
						DistributionObject o2 = new DistributionObject();
						o2.setDistributionPref(dp);
						o2.setPrefGroup(b.getClazz());
						o2.setSequenceNumber(2);
						dp.getDistributionObjects().add(o2);
						iHibSession.saveOrUpdate(dp);
					}
				}
			}
		}
	}
	
	private void checkInstructorAssignments(Hashtable<String, List<Assignment>> instructorAssignments) {
		for (Map.Entry<String, List<Assignment>> entry: instructorAssignments.entrySet()) {
			String instructorExternalId = entry.getKey();
			List<Assignment> ax = entry.getValue();
			for (Assignment a: ax) {
				b: for (Assignment b: ax) {
					if (a.getUniqueId() >= b.getUniqueId()) continue;
					if (a.getTimeLocation().hasIntersection(b.getTimeLocation())) {
						Set<DistributionPref> dist = a.getClazz().effectivePreferences(DistributionPref.class);
						for (DistributionPref d: dist) {
							if (d.getDistributionType().getReference().equals("CAN_SHARE_ROOM") || d.getDistributionType().getReference().equals("MEET_WITH")) {
								for (DistributionObject o: d.getDistributionObjects()) {
									if (o.getPrefGroup().equals(b.getClazz()) || o.getPrefGroup().equals(b.getClazz().getSchedulingSubpart())) {
										continue b;
									}
								}
							}
						}
						ClassInstructor ca = null, cb = null;
						for (ClassInstructor ci: a.getClazz().getClassInstructors()) {
							if (instructorExternalId.equals(ci.getInstructor().getExternalUniqueId())) ca = ci;
						}
						for (ClassInstructor ci: b.getClazz().getClassInstructors()) {
							if (instructorExternalId.equals(ci.getInstructor().getExternalUniqueId())) cb = ci;
						}
						if (ca != null && ca.isLead() && cb != null && cb.isLead()) {
							sLog.warn("Overlapping classes for instructor " + instructorExternalId + ":\n" + 
									"  " + a.getClazz().getClassLabel() + " " + a.getPlacement().getLongName(CONSTANTS.useAmPm()) + "\n" +
									"  " + b.getClazz().getClassLabel() + " " + b.getPlacement().getLongName(CONSTANTS.useAmPm()));
							ca.setLead(false);
							iHibSession.saveOrUpdate(ca);
						}
					}
				}
			}
		}
	}
	
	private boolean fixDatePattern(Class_ clazz, Assignment assignment) {
		if (assignment.getDatePattern() == null) return false;
		DatePattern dp = clazz.effectiveDatePattern();
		if (dp == null || !dp.equals(assignment.getDatePattern())) {
			sLog.info("Class " + clazz.getClassLabel() + " had an inconsistent date pattern " + dp.getName() + " != " + assignment.getDatePattern().getName());
			clazz.setDatePattern(assignment.getDatePattern());
			return true;
		}
		return false;
	}
	
	private boolean fixRequiredTime(Class_ clazz, Assignment assignment) {
		TimeLocation time = assignment.getTimeLocation();
		if (time == null) return false;
        Set<TimePref> timePrefs = clazz.effectivePreferences(TimePref.class);
        boolean onlyReq = false;
        int nrExact = 0;
        for (TimePref p: timePrefs) {
        	TimePatternModel pattern = p.getTimePatternModel();
        	if (pattern.isExactTime() || pattern.countPreferences(PreferenceLevel.sRequired)>0) onlyReq = true;
        	if (pattern.isExactTime()) {
        		if (assignment.getDays() == pattern.getExactDays() && assignment.getStartSlot() == pattern.getExactStartSlot()) return false;
        		nrExact ++;
        	}
            for (int t = 0; t < pattern.getNrTimes(); t++) {
                for (int d = 0; d < pattern.getNrDays(); d++) {
                    String pref = pattern.getPreference(d,t);
                    if (pattern.getDayCode(d) == assignment.getDays() && pattern.getStartSlot(t) == assignment.getStartSlot()) {
                    	if (onlyReq) {
                        	if (PreferenceLevel.sRequired.equals(pref)) return false;
                    	} else {
                        	if (!PreferenceLevel.sProhibited.equals(pref)) return false;
                        	sLog.warn("Clazz " + clazz.getClassLabel() + " prohibits assigned time " + time.getName(CONSTANTS.useAmPm()));
                        	pattern.setPreference(d, t, PreferenceLevel.sStronglyDiscouraged);
                        	p.setPreference(pattern.getPreferences());
                        	if (p.getOwner() == null) p.setOwner(clazz);
                        	iHibSession.saveOrUpdate(p);
                        	return true;
                    	}
                    }
                }
            }
        }
        if (onlyReq) {
        	boolean found = false;
            for (TimePref p: timePrefs) {
            	TimePatternModel pattern = p.getTimePatternModel();
            	if (pattern.isExactTime()) continue;
            	pattern.clear();
                for (int t = 0; t < pattern.getNrTimes(); t++) {
                    for (int d = 0; d < pattern.getNrDays(); d++) {
                        if (pattern.getDayCode(d) == assignment.getDays() && pattern.getStartSlot(t) == assignment.getStartSlot()) {
                        	sLog.warn("Clazz " + clazz.getClassLabel() + " requires a different time " + 
                        			pattern.getDayHeader(d) + " " + pattern.getTimeHeaderShort(t) + " than assigned " + time.getName(CONSTANTS.useAmPm()));
                        	pattern.setPreference(d, t, PreferenceLevel.sRequired);
                        	found = true;
                        }
                    }
                }
            	p.setPreference(pattern.getPreferences());
            	if (p.getOwner() == null) p.setOwner(clazz);
            	iHibSession.saveOrUpdate(p);
            }
            if (found) return true;
        }
        if (timePrefs.isEmpty()) {
        	sLog.warn("Clazz " + clazz.getClassLabel() + " has no time preferences but assigned time " + time.getLongName(CONSTANTS.useAmPm()));
        } else {
        	sLog.warn("Clazz " + clazz.getClassLabel() + " has no time pattern for the assigned time " + time.getLongName(CONSTANTS.useAmPm()));
        	if (nrExact == 1 && timePrefs.size() == 1) {
        		TimePref p = timePrefs.iterator().next();
            	TimePatternModel pattern = p.getTimePatternModel();
            	pattern.setExactDays(assignment.getDays());
            	pattern.setExactStartSlot(assignment.getStartSlot());
            	p.setPreference(pattern.getPreferences());
            	iHibSession.saveOrUpdate(p);
        		return true;
        	}
        }
    	TimePref p = new TimePref();
    	p.setOwner(clazz);
    	p.setTimePattern(TimePattern.findExactTime(iSessionId));
    	TimePatternModel pattern = p.getTimePatternModel();
    	pattern.setExactDays(assignment.getDays());
    	pattern.setExactStartSlot(assignment.getStartSlot());
    	p.setPreference(pattern.getPreferences());
    	p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
    	clazz.getPreferences().add(p);
    	return true;
	}
	
	private boolean fixNrAssignedRooms(Class_ clazz, Assignment assignment) {
		if (clazz.getNbrRooms() != assignment.getRooms().size()) {
        	sLog.warn("Clazz " + clazz.getClassLabel() + " has assigned a wrong number of rooms " + clazz.getNbrRooms() + " != " + assignment.getRooms().size());
        	clazz.setNbrRooms(assignment.getRooms().size());
			return true;
		}
		return false;
	}
	
	private boolean fixRequiredRoom(Class_ clazz, Assignment assignment) {
		if (clazz.getNbrRooms() == 0) return false;
		Set<RoomPref> roomPrefs = clazz.effectivePreferences(RoomPref.class);
		Set<Location> remaining = new HashSet<Location>(assignment.getRooms());
		boolean hasReq = false;
		for (RoomPref p: roomPrefs) {
			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired)) {
				hasReq = true;
				if (!remaining.remove(p.getRoom())) {
					sLog.warn("Clazz " + clazz.getClassLabel() + " requires a room " + p.getRoom().getLabel() + " different from the assigned room " + assignment.getPlacement().getRoomName(","));
		        	p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
		        	if (p.getOwner() == null) p.setOwner(clazz);
		        	iHibSession.save(p);
				}
			}
			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sProhibited)) {
				if (assignment.getRooms().contains(p.getRoom())) {
					sLog.warn("Clazz " + clazz.getClassLabel() + " prohibits the assigned room " + p.getRoom().getLabel());
					p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
		        	if (p.getOwner() == null) p.setOwner(clazz);
		        	iHibSession.save(p);
				}
			}
		}
		for (RoomPref p: roomPrefs) {
			if (hasReq && !p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired) && remaining.remove(p.getRoom())) {
	        	p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
	        	if (p.getOwner() == null) p.setOwner(clazz);
	        	iHibSession.save(p);
			}
		}
		boolean ret = false;
		if (hasReq && !remaining.isEmpty()) {
			for (Location room: remaining) {
				RoomPref p = new RoomPref();
				p.setOwner(clazz);
				p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
				p.setRoom(room);
				clazz.getPreferences().add(p);
				roomPrefs.add(p);
			}
			ret = true;
		}
		loc: for (Location loc: assignment.getRooms()) {
			for (RoomPref px: (Set<RoomPref>)clazz.getManagingDept().getPreferences(RoomPref.class)) {
				if (px.getRoom().equals(loc)) {
					if (px.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sProhibited)) {
						sLog.info("Class " + clazz.getClassLabel() + " is assigned to a room " + loc.getLabel() + " that is prohibited on a departmental level.");
						px.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
						iHibSession.saveOrUpdate(px);
					}
					if (px.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sStronglyDiscouraged)) {
						for (RoomPref p: roomPrefs)
							if (p.getRoom().equals(loc)) continue loc;
						sLog.info("Class " + clazz.getClassLabel() + " is assigned to a room " + loc.getLabel() + " that is strongly discouraged on a departmental level.");
						RoomPref p = new RoomPref();
						p.setOwner(clazz);
						p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
						p.setRoom(loc);
						clazz.getPreferences().add(p);
						ret = true;
					}
				}
			}
		}
		return ret;
	}
	
	private boolean fixRoomSize(Class_ clazz, Assignment assignment) {
		if (clazz.getNbrRooms() == 0) return false;
		int minSize = Math.round(clazz.getRoomRatio() * clazz.getExpectedCapacity());
		boolean ret = false;
		for (Location loc: assignment.getRooms()) {
			if (loc.getCapacity() < minSize) {
				sLog.warn("Clazz " + clazz.getClassLabel() + " has assigned a room " + loc.getLabel() + " that is too small (" + loc.getCapacity() + " < " + minSize + ")");
				minSize = loc.getCapacity();
				clazz.setRoomRatio(((float)loc.getCapacity()) / clazz.getExpectedCapacity());
				ret = true;
			}
		}
		return ret;
	}
	
	private boolean fixRequiredBuilding(Class_ clazz, Assignment assignment) {
		if (clazz.getNbrRooms() == 0) return false;
		Set<BuildingPref> bldgPrefs = clazz.effectivePreferences(BuildingPref.class);
		Set<Building> remaining = new HashSet<Building>();
		for (Location room: assignment.getRooms()) {
			if (room instanceof Room) remaining.add(((Room)room).getBuilding());
		}
		boolean hasReq = false;
		for (BuildingPref p: bldgPrefs) {
			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired)) {
				hasReq = true;
				if (!remaining.remove(p.getBuilding())) {
					sLog.warn("Clazz " + clazz.getClassLabel() + " requires a building " + p.getBuilding().getAbbreviation() + " different from the assigned building " + assignment.getPlacement().getRoomName(","));
		        	p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
		        	if (p.getOwner() == null) p.setOwner(clazz);
		        	iHibSession.save(p);
				}
			}
			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sProhibited)) {
				if (remaining.contains(p.getBuilding())) {
					sLog.warn("Clazz " + clazz.getClassLabel() + " prohibits the assigned building " + p.getBuilding().getAbbreviation());
					p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
		        	if (p.getOwner() == null) p.setOwner(clazz);
		        	iHibSession.save(p);
				}
			}
		}
		for (BuildingPref p: bldgPrefs) {
			if (hasReq && !p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired) && remaining.remove(p.getBuilding())) {
	        	p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
	        	if (p.getOwner() == null) p.setOwner(clazz);
	        	iHibSession.save(p);
			}
		}
		if (hasReq && !remaining.isEmpty()) {
			for (Building bldg: remaining) {
				BuildingPref p = new BuildingPref();
				p.setOwner(clazz);
				p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
				p.setBuilding(bldg);
				clazz.getPreferences().add(p);
			}
			return true;
		}
		return false;
	}
	
    public boolean isBackToBackTooFar(Placement p1, Placement p2) {
        if (!p1.getTimeLocation().shareDays(p2.getTimeLocation()))
            return false;
        if (!p1.getTimeLocation().shareWeeks(p2.getTimeLocation()))
            return false;
        int s1 = p1.getTimeLocation().getStartSlot() % Constants.SLOTS_PER_DAY;
        int s2 = p2.getTimeLocation().getStartSlot() % Constants.SLOTS_PER_DAY;
        if (s1 + p1.getTimeLocation().getLength() != s2 && s2 + p2.getTimeLocation().getLength() != s1)
            return false;
        double distance = Placement.getDistanceInMeters(iDistanceMetric, p1, p2);
        return (distance > iDistanceMetric.getInstructorProhibitedLimit());
    }
	
	private void fixInstructors(Class_ clazz, Assignment a) {
		for (ClassInstructor ci: clazz.getClassInstructors()) {
			if (ci.getLead() != a.getInstructors().contains(ci.getInstructor())) {
				sLog.info("Correcting lead info from assignment for " + clazz.getClassLabel() + ", instructor " +
						ci.getInstructor().getName(DepartmentalInstructor.sNameFormatShort) + " (" + 
						ci.getLead() + " -> " + a.getInstructors().contains(ci.getInstructor()) + ")");
				ci.setLead(a.getInstructors().contains(ci.getInstructor()));
				iHibSession.saveOrUpdate(ci);
			}
		}
		for (ClassInstructor ci: clazz.getClassInstructors()) {
			if (!ci.isLead()) continue;
			b: for (ClassInstructor other: ci.getInstructor().getClasses()) {
				if (other.equals(ci) || !other.isLead()) continue;
				Assignment b = other.getClassInstructing().getCommittedAssignment();
				if (b == null) continue;
				if (b.getTimeLocation().hasIntersection(a.getTimeLocation())) {
					sLog.info("Class " + clazz.getClassLabel() + " " + a.getTimeLocation().getName(CONSTANTS.useAmPm()) + " " + a.getPlacement().getRoomName(",") + 
							" has an instructor " + ci.getInstructor().getName(DepartmentalInstructor.sNameFormatShort)+ " overlapping with " + 
							other.getClassInstructing().getClassLabel() + " " + b.getTimeLocation().getName(CONSTANTS.useAmPm()) + "  " + b.getPlacement().getRoomName(",") + ".");
					if (a.getTimeLocation().getLongName(CONSTANTS.useAmPm()).equals(b.getTimeLocation().getLongName(CONSTANTS.useAmPm())) &&
						a.getPlacement().getRoomName(",").equals(b.getPlacement().getRoomName(","))) {
						sLog.info("  checking meet with constraint...");
						Set<DistributionPref> dist = a.getClazz().effectivePreferences(DistributionPref.class);
						for (DistributionPref d: dist) {
							if (d.getDistributionType().getReference().equals("CAN_SHARE_ROOM") || d.getDistributionType().getReference().equals("MEET_WITH")) {
								for (DistributionObject o: d.getDistributionObjects()) {
									if (o.getPrefGroup().equals(b.getClazz()) || o.getPrefGroup().equals(b.getClazz().getSchedulingSubpart())) {
										int minSize = Math.round(a.getClazz().getRoomRatio() * a.getClazz().getExpectedCapacity() + 
												b.getClazz().getRoomRatio() * b.getClazz().getExpectedCapacity());
										for (Location location: a.getRooms()) {
											sLog.info("Allowed overlap of classes in room " + location.getLabel() + ":\n" + 
													"  " + a.getClazz().getClassLabel() + " " + a.getTimeLocation().getLongName(CONSTANTS.useAmPm()) + "\n" +
													"  " + b.getClazz().getClassLabel() + " " + b.getTimeLocation().getLongName(CONSTANTS.useAmPm()));
											if (location.getCapacity() < minSize) {
												sLog.warn("But the he room is too small (" + location.getCapacity() + " < " + minSize + ")");
												float ratio = ((float)location.getCapacity()) / minSize;
												a.getClazz().setRoomRatio(a.getClazz().getRoomRatio() * ratio);
												b.getClazz().setRoomRatio(b.getClazz().getRoomRatio() * ratio);
												iHibSession.saveOrUpdate(a.getClazz());
												iHibSession.saveOrUpdate(b.getClazz());
											}
										}
										continue b;
									}
								}
							}
						}
						int minSize = Math.round(a.getClazz().getRoomRatio() * a.getClazz().getExpectedCapacity()) + Math.round(b.getClazz().getRoomRatio() * b.getClazz().getExpectedCapacity());
						for (Location location: a.getRooms()) {
							if (location.getCapacity() < minSize) {
								sLog.warn("Also the room is too small (" + location.getCapacity() + " < " + minSize + ")");
								float ratio = ((float)location.getCapacity()) / minSize;
								a.getClazz().setRoomRatio(a.getClazz().getRoomRatio() * ratio);
								b.getClazz().setRoomRatio(b.getClazz().getRoomRatio() * ratio);
								iHibSession.saveOrUpdate(a.getClazz());
								iHibSession.saveOrUpdate(b.getClazz());
							}
						}
						DistributionType meetWithType = (DistributionType)iHibSession.createQuery(
							"select d from DistributionType d where d.reference = :type").setString("type", "MEET_WITH").uniqueResult();
						DistributionPref dp = new DistributionPref();
						dp.setDistributionType(meetWithType);
						dp.setOwner(a.getClazz().getManagingDept());
						dp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
						dp.setDistributionObjects(new HashSet<DistributionObject>());
						dp.setGrouping(DistributionPref.sGroupingNone);
						DistributionObject o1 = new DistributionObject();
						o1.setDistributionPref(dp);
						o1.setPrefGroup(a.getClazz());
						o1.setSequenceNumber(1);
						dp.getDistributionObjects().add(o1);
						DistributionObject o2 = new DistributionObject();
						o2.setDistributionPref(dp);
						o2.setPrefGroup(b.getClazz());
						o2.setSequenceNumber(2);
						dp.getDistributionObjects().add(o2);
						iHibSession.saveOrUpdate(dp);						
					} else {
						ci.setLead(false);
						iHibSession.saveOrUpdate(ci);
					}
				} else if (!ci.getInstructor().isIgnoreToFar() && isBackToBackTooFar(a.getPlacement(), b.getPlacement())) {
					sLog.info("Class " + clazz.getClassLabel() + " " + a.getTimeLocation().getName(CONSTANTS.useAmPm()) + " " + a.getPlacement().getRoomName(",") + 
							" has an instructor " + ci.getInstructor().getName(DepartmentalInstructor.sNameFormatShort)+ " and is too far back-to-back with " + 
							other.getClassInstructing().getClassLabel() + " " + b.getTimeLocation().getName(CONSTANTS.useAmPm()) + "  " + b.getPlacement().getRoomName(",") + ".");
					ci.getInstructor().setIgnoreToFar(true);
					iHibSession.saveOrUpdate(ci.getInstructor());
				}
			}
		}
	}
	
	public void fixRequiredFeatures(Class_ clazz, Assignment a) {
		Set<RoomFeaturePref> featurePrefs = clazz.effectivePreferences(RoomFeaturePref.class);
		for (RoomFeaturePref p: featurePrefs) {
			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired)) {
				for (Location loc: a.getRooms()) {
					if (!loc.getFeatures().contains(p.getRoomFeature())) {
						sLog.info("Class " + clazz.getClassLabel() + " requires feature " + p.getRoomFeature().getLabel() + " but assigned room " + loc.getLabel() + " does not have it.");
						p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
			        	if (p.getOwner() == null) p.setOwner(clazz);
						iHibSession.saveOrUpdate(p);
					}
				}
			}
			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sProhibited)) {
				for (Location loc: a.getRooms()) {
					if (loc.getFeatures().contains(p.getRoomFeature())) {
						sLog.info("Class " + clazz.getClassLabel() + " prohibits feature " + p.getRoomFeature().getLabel() + " but assigned room " + loc.getLabel() + " does have it.");
						p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
			        	if (p.getOwner() == null) p.setOwner(clazz);
						iHibSession.saveOrUpdate(p);
					}
				}
			}
		}
	}
	
	public void fixRequiredGroups(Class_ clazz, Assignment a) {
		Set<RoomGroupPref> roomGroopPrefs = clazz.effectivePreferences(RoomGroupPref.class);
		for (RoomGroupPref p: roomGroopPrefs) {
			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired)) {
				for (Location loc: a.getRooms()) {
					if (!loc.getRoomGroups().contains(p.getRoomGroup())) {
						sLog.info("Class " + clazz.getClassLabel() + " requires feature " + p.getRoomGroup().getName() + " but assigned room " + loc.getLabel() + " does not have it.");
						p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
			        	if (p.getOwner() == null) p.setOwner(clazz);
						iHibSession.saveOrUpdate(p);
					}
				}
			}
			if (p.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sProhibited)) {
				for (Location loc: a.getRooms()) {
					if (loc.getRoomGroups().contains(p.getRoomGroup())) {
						sLog.info("Class " + clazz.getClassLabel() + " prohibits feature " + p.getRoomGroup().getName() + " but assigned room " + loc.getLabel() + " does have it.");
						p.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
			        	if (p.getOwner() == null) p.setOwner(clazz);
						iHibSession.saveOrUpdate(p);
					}
				}
			}
		}
	}
	
	public void fixRoomSharing(Class_ clazz, Assignment a) {
		Set<Location> locs = clazz.getAvailableRooms();
		for (Location loc: a.getRooms()) {
			if (!locs.contains(loc)) {
				sLog.info("Class " + clazz.getClassLabel() + " is assigned into a room " + loc.getLabel() +
						" that is not available for " + clazz.getManagingDept().getDeptCode() + " - " + clazz.getManagingDept().getName());
				RoomDept rd = new RoomDept();
				rd.setControl(false);
				rd.setDepartment(clazz.getManagingDept());
				rd.setRoom(loc);
				clazz.getManagingDept().getRoomDepts().add(rd);
				loc.getRoomDepts().add(rd);
				iHibSession.saveOrUpdate(rd);
				RoomPref rp = new RoomPref();
				rp.setOwner(clazz.getManagingDept());
				rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
				rp.setRoom(loc);
				clazz.getManagingDept().getPreferences().add(rp);
				iHibSession.saveOrUpdate(clazz.getManagingDept());
			}
			RoomSharingModel m = loc.getRoomSharingModel();
			boolean changed = false;
			Department d = clazz.getManagingDept();
			for (Enumeration<Integer> e = a.getTimeLocation().getSlots(); e.hasMoreElements(); ) {
				int slot = e.nextElement();
				if (m.isFreeForAll(slot)) continue;
				if (m.isNotAvailable(slot) || !d.getUniqueId().equals(m.getDepartmentId(slot))) {
					m.setPreference(slot / Constants.SLOTS_PER_DAY, (slot % Constants.SLOTS_PER_DAY) / 6, d.getUniqueId().toString());
					changed = true;
				}
			}
			if (changed) {
				sLog.info("Room sharing changed for room " + loc.getLabel() + " to allow class " + clazz.getClassLabel() + " " + a.getTimeLocation().getLongName(CONSTANTS.useAmPm()) + " in.");
				loc.setRoomSharingModel(m);
				iHibSession.saveOrUpdate(loc);
			}
		}
	}

	public static void main(String args[]) {
        try {
            Properties props = new Properties();
            props.setProperty("log4j.rootLogger", "DEBUG, A1");
            props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
            props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
            props.setProperty("log4j.appender.A1.layout.ConversionPattern","%-5p %c{2}: %m%n");
            props.setProperty("log4j.logger.org.hibernate","INFO");
            props.setProperty("log4j.logger.org.hibernate.cfg","WARN");
            props.setProperty("log4j.logger.org.hibernate.cache.EhCacheProvider","ERROR");
            props.setProperty("log4j.logger.org.unitime.commons.hibernate","INFO");
            props.setProperty("log4j.logger.net","INFO");
            PropertyConfigurator.configure(props);
            
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

            org.hibernate.Session hibSession = new _RootDAO().getSession();
            
            Transaction tx = null;
            try {
                tx = hibSession.beginTransaction();

                Session session = Session.getSessionUsingInitiativeYearTerm(
                        ApplicationProperties.getProperty("initiative", "PWL"),
                        ApplicationProperties.getProperty("year","2014"),
                        ApplicationProperties.getProperty("term","Spring")
                        );
                
                if (session==null) {
                    sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                    System.exit(0);
                } else {
                    sLog.info("Session: "+session);
                }
                
                new FixCourseTimetablingInconsistencies(session.getUniqueId()).fixAll(hibSession);
                
                tx.commit();
            } catch (Exception e) {
                if (tx!=null) tx.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
