/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.dataexchange;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Element;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomSharingModel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

/**
 * 
 * @author Tomas Muller
 *
 */
public class RoomSharingImport  extends BaseImport {
	protected static Formats.Format<Date> sTimeFormat = Formats.getDateFormat("HHmm");
	private String iTimeFormat = null;

    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("roomSharing")) {
        	throw new Exception("Given XML file is not a Room Sharing load file.");
        }
        try {
            beginTransaction();

            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");
            iTimeFormat = root.attributeValue("timeFormat");
            if (iTimeFormat == null) iTimeFormat = "HHmm";
            
            Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            if(session == null) {
                throw new Exception("No session found for the given campus, year, and term.");
            }

            if (!"true".equals(root.attributeValue("force")) && Solution.hasTimetable(session.getUniqueId())) {
            	info("Note: set the attribute force='true' of the root element to override the following import eligibility check.");
            	throw new Exception("Room sharing import is disabled: " + session.getLabel() + " already has a committed timetable.");
            }

            info("Loading rooms...");
            Set<String> avoidRoomId = new HashSet<String>();
            Set<String> avoidRoomName = new HashSet<String>();
            Map<String, Location> id2location = new HashMap<String, Location>();
            Map<String, Location> name2location = new HashMap<String, Location>();
            for (Location location: (List<Location>)getHibSession().createQuery("from Location where session.uniqueId = :sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	if (location.getExternalUniqueId() != null && !avoidRoomId.contains(avoidRoomId)) {
            		Location old = id2location.put(location.getExternalUniqueId(), location);
            		if (old != null) {
            			warn("There are two or more rooms with the same external id " + location.getExternalUniqueId() + ": " + location.getLabel() + " and " + old.getLabel() + ".");
            			avoidRoomId.add(location.getExternalUniqueId());
            		}
            	}
            	if (!avoidRoomName.contains(location.getLabel())) {
                	Location old = name2location.put(location.getLabel(), location);
                	if (old != null) {
            			warn("There are two or more rooms with the same name " + location.getLabel() + ".");
            			avoidRoomName.add(location.getLabel());
            		}
            	}
            }
            
            info("Loading departments...");
            Map<String, Department> id2department = new HashMap<String, Department>();
            Map<String, Department> code2department = new HashMap<String, Department>();
            for (Department dept: (List<Department>)getHibSession().createQuery("from Department where session.uniqueId = :sessionId").setLong("sessionId", session.getUniqueId()).list()) {
            	if (dept.getExternalUniqueId() != null) {
            		Department old = id2department.put(dept.getExternalUniqueId(), dept);
            		if (old != null) {
            			warn("There are two departments with the same external id " + dept.getExternalUniqueId() + ": " + dept.getLabel() + " and " + old.getLabel() + ".");
            		}
            	}
            	Department old = code2department.put(dept.getDeptCode(), dept);
            	if (old != null) {
        			warn("There are two rooms with the same code " + dept.getDeptCode() + ": " + dept.getName() + " and " + old.getName() + ".");
        		}
            }
            
        	info("Importing room sharing...");
        	int nrChanged = 0;
        	for (Iterator i = root.elementIterator("location"); i.hasNext(); ) {
                Element locEl = (Element) i.next();
                Location location = null;

                String locId = locEl.attributeValue("id");
                if (locId != null && !avoidRoomId.contains(locId)) {
                	location = id2location.get(locId);
                	if (location == null) warn("Location of id " + locId + " does not exist.");
                }

                if (location == null) {
                	String building = locEl.attributeValue("building") ;
                	String roomNbr = locEl.attributeValue("roomNbr");
                	if (building != null && roomNbr != null && !avoidRoomName.contains(building + " " + roomNbr)) {
                		location = name2location.get(building + " " + roomNbr);
                		if (location == null) warn("Location of building " + building + " and room number " + roomNbr + " does not exist.");
                	}
                }
                
                if (location == null) {
                	String name = locEl.attributeValue("name");
                	if (name != null && !avoidRoomName.contains(name)) {
                		location = name2location.get(name);
            			if (location == null) warn("Location of name " + name + " does not exist.");
                	}
                }
                
                if (location == null) continue;
                
                Set<RoomDept> existing = new HashSet<RoomDept>(location.getRoomDepts());
                Set<Department> departments = new HashSet<Department>();
                boolean changed = false;
                
                String note = locEl.attributeValue("note");
                if (note == null && location.getShareNote() != null) {
                	location.setShareNote(null);
                	info(location.getLabel() + ": share note removed.");
                	changed = true;
                } else if (note != null && !note.equals(location.getShareNote())) {
                	location.setShareNote(note);
                	info(location.getLabel() + ": share note changed to '" + note + "'.");
                	changed = true;
                }
                
                department: for (Iterator j = locEl.elementIterator("department"); j.hasNext(); ) {
                    Element deptEl = (Element) j.next();
                    Department dept = null;

                    String deptId = deptEl.attributeValue("id");
                    if (deptId != null) {
                    	dept = id2department.get(deptId);
                    	if (dept == null) warn(location.getLabel() + ": Department of id " + deptId + " does not exist.");
                    }
                    
                    String deptCode = deptEl.attributeValue("code");
                    if (deptCode != null) {
                    	dept = code2department.get(deptCode);
                    	if (dept == null) warn(location.getLabel() + ": Department of code " + deptCode + " does not exist.");
                    }
                    
                    if (dept == null) continue;
                    Boolean control = "true".equals(deptEl.attributeValue("control"));
                    
                    for (Iterator<RoomDept> k = existing.iterator(); k.hasNext(); ) {
                    	RoomDept rd = k.next();
                    	if (rd.getDepartment().equals(dept)) {
                    		if (!control.equals(rd.getControl())) {
                    			rd.setControl(control);
                    			getHibSession().update(rd);
                    			info(location.getLabel() + ": " + (control ? " control moved to " + dept.getLabel() : " control removed from " + dept.getLabel()));
                    			changed = true;
                    		}
                    		k.remove();
                            departments.add(dept);
                    		continue department;
                    	}
                    }
                    
                    RoomDept rd = new RoomDept();
                    rd.setControl(control);
                    rd.setDepartment(dept);
                    rd.setRoom(location);
                    location.getRoomDepts().add(rd);
                    dept.getRoomDepts().add(rd);
                    getHibSession().save(rd);
                    departments.add(dept);
                    info(location.getLabel() + ": added " + (control ? "controlling " : "") + " department" + dept.getLabel());
                    changed = true;
                }
                
                for (RoomDept rd: existing) {
                	info(location.getLabel() + ": removed " + (rd.isControl() ? "controlling " : "") + " department" + rd.getDepartment().getLabel());
                	location.getRoomDepts().remove(rd);
                	rd.getDepartment().getRoomDepts().remove(rd);
                	getHibSession().delete(rd);
                	changed = true;
                }
                
                RoomSharingModel model = location.getRoomSharingModel();
                String oldModel = model.toString();
                model.setPreferences(null);
                
                Element sharingEl = locEl.element("sharing");
                if (sharingEl != null) {
                	for (Iterator j = sharingEl.elementIterator(); j.hasNext(); ) {
                		Element el = (Element) j.next();
                		TimeObject time = new TimeObject(el.attributeValue("start"), el.attributeValue("end"), el.attributeValue("days"));
                		
                		String pref = null;
                		if ("unavailable".equals(el.getName())) {
                			pref = RoomSharingModel.sNotAvailablePref.toString();
                		} else if ("assigned".equals(el.getName())) {
                            Department dept = null;

                            String deptId = el.attributeValue("id");
                            if (deptId != null) {
                            	dept = id2department.get(deptId);
                            	if (dept == null) warn(location.getLabel() + ": Department of id " + deptId + " does not exist.");
                            }
                            
                            String deptCode = el.attributeValue("code");
                            if (deptCode != null) {
                            	dept = code2department.get(deptCode);
                            	if (dept == null) warn(location.getLabel() + ": Department of code " + deptCode + " does not exist.");
                            }
                            
                            if (dept == null) continue;
                            if (!departments.contains(dept)) {
                            	warn(location.getLabel() + ": Department " + dept.getLabel() + " is not among the room sharing departments.");
                            	continue;
                            }
                           
                            pref = dept.getUniqueId().toString();
                		}
                		
                		if (pref == null) continue;
                		
            			if (time.hasDays()) {
            				for (int d: time.getDays())
            					for (int t = time.getStartPeriod(); t < time.getEndPeriod(); t++)
            						model.setPreference(d, t, pref);
            			} else {
            				for (int d = 0; d < model.getNrDays(); d++)
            					for (int t = time.getStartPeriod(); t < time.getEndPeriod(); t++)
            						model.setPreference(d, t, pref); 
            			}
                	}
                }
                
                String newModel = model.toString();
                if (!oldModel.equals(newModel)) {
                	info(location.getLabel() + ": room sharing changed to " + (newModel.isEmpty() ? "free for all" : newModel));
                	changed = true;
                }
                
                location.setRoomSharingModel(model);
                getHibSession().update(location);
                
                if (changed)
                	nrChanged ++;
        	}

        	if (nrChanged == 0) {
        		info("No change detected.");
        	} else {
        		info(nrChanged + " locations have changed.");
        	}
        	info("All done.");
        	
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
	
	protected class TimeObject {
		private Integer iStartPeriod;
		private Integer iEndPeriod;
		private Set<Integer> iDays = null;
		
		TimeObject(String startTime, String endTime, String daysOfWeek) throws Exception{
			iStartPeriod = (startTime == null ? 0 : str2Slot(startTime));
			if (iStartPeriod < 0 || iStartPeriod >= Constants.SLOTS_PER_DAY)
				iStartPeriod = 0;
			iEndPeriod = (endTime == null ? Constants.SLOTS_PER_DAY : str2Slot(endTime));
			if (iEndPeriod < 0 || iEndPeriod > Constants.SLOTS_PER_DAY) {
				iEndPeriod = Constants.SLOTS_PER_DAY;
			}
			if (iStartPeriod > iEndPeriod) {
				error("Invalid time '"+startTime+"' -- must be before " + endTime + ".");
				int x = iStartPeriod; iStartPeriod = iEndPeriod; iEndPeriod = x;
			}
			if (daysOfWeek != null)
				setDaysOfWeek(daysOfWeek);	
		}
		
		private void setDaysOfWeek(String daysOfWeek){
			iDays = new TreeSet<Integer>();
			String tmpDays = daysOfWeek;
			if (tmpDays.contains("Th")) {
				iDays.add(Constants.DAY_THU);
				tmpDays = tmpDays.replace("Th", "..");
			}
			if(tmpDays.contains("R")) {
				iDays.add(Constants.DAY_THU);
				tmpDays = tmpDays.replace("R", "..");
			}
			if (tmpDays.contains("Su")) {
				iDays.add(Constants.DAY_SUN);
				tmpDays = tmpDays.replace("Su", "..");
			}
			if (tmpDays.contains("U")) {
				iDays.add(Constants.DAY_SUN);
				tmpDays = tmpDays.replace("U", "..");
			}
			if (tmpDays.contains("M")) {
				iDays.add(Constants.DAY_MON);
				tmpDays = tmpDays.replace("M", ".");
			}
			if (tmpDays.contains("T")) {
				iDays.add(Constants.DAY_TUE);
				tmpDays = tmpDays.replace("T", ".");
			}
			if (tmpDays.contains("W")) {
				iDays.add(Constants.DAY_WED);
				tmpDays = tmpDays.replace("W", ".");
			}
			if (tmpDays.contains("F")) {
				iDays.add(Constants.DAY_FRI);
				tmpDays = tmpDays.replace("F", ".");
			}
			if (tmpDays.contains("S")) {
				iDays.add(Constants.DAY_SAT);
				tmpDays = tmpDays.replace("S", ".");
			}						
		}
		
		public Integer getStartPeriod() {
			return iStartPeriod;
		}
		
		public Integer getEndPeriod() {
			return iEndPeriod;
		}
		
		public Set<Integer> getDays() {
			return iDays;
		}
		
		public boolean hasDays() {
			return iDays != null;
		}
		
		public Integer str2Slot(String timeString) {
			try {
				int time = 0;
				if ("HHmm".equals(iTimeFormat)) {
					time = Integer.parseInt(timeString);
				} else {
					Date date = CalendarUtils.getDate(timeString, iTimeFormat);
					time = Integer.parseInt(sTimeFormat.format(date));
				}
				int hour = time / 100;
				int min = time % 100;
				if (hour > 24 || (hour == 24 && min > 0)) {
					error("Invalid time '"+timeString+"' -- hour (" + hour + ") must be between 0 and 23.");
				}
				if (min >= 60) {
					error("Invalid time '"+timeString+"' -- minute (" + min + ") must be between 0 and 59.");
				}
				if ((min % Constants.SLOT_LENGTH_MIN) != 0){
					warn("Invalid time '" + timeString + "' -- minute ("+min+") must be divisible by " + Constants.SLOT_LENGTH_MIN + ".");
					min -= (min % Constants.SLOT_LENGTH_MIN);
				}
				return (hour * 60 + min - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
			} catch (NumberFormatException ex) {
				error("Invalid time '" + timeString + "' -- not a number.");
				return -1;
			} catch (Exception e) {
				error("Invalid time '" + timeString + "' -- does not meet the format: " + iTimeFormat);
				return -1;
			}
		}
	}


}