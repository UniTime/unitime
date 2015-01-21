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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * Example Java program that sets default preferences based on the committed (imported) assignment.
 * For each class, it strongly prefers its time and room assignment. If there is no matching time
 * pattern found, exact time pattern is used.
 * Date pattern and preferences are moved to subpart level, if possible. Change limit of unlimited
 * configurations to 100. Unlimited classes are changed to have a limit too, but the room ratio is set
 * to zero (any room is big enough).
 * 
 * @author Tomas Muller
 *
 */

public class SimpleDefaultPreferences {
	
	public static void main(String[] args) {
		try {
			// Configure logging
			BasicConfigurator.configure();
			Logger log = Logger.getLogger(SimpleDefaultPreferences.class);
			
			// Configure hibernate
			HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
			
			// Opens hibernate session
			org.hibernate.Session hibSession = new _RootDAO().getSession();
			
			// Finds the academic session
			Session session = Session.getSessionUsingInitiativeYearTerm(
                    ApplicationProperties.getProperty("initiative", "woebegon"),
                    ApplicationProperties.getProperty("year","2010"),
                    ApplicationProperties.getProperty("term","Fal")
                    );
			if (session == null) {
				log.fatal("Academic session not found, use properties initiative, year, and term to set academic session.");
				System.exit(0);
			}
			
			// List all visible time patterns, order by type (standard time patterns first)
			Set<TimePattern> patterns = new TreeSet<TimePattern>(TimePattern.findAll(session, true));
			
			// Iterate over all the offerings
			for (InstructionalOffering offering: session.getInstructionalOfferings()) {
				log.info("Processing " + offering.getCourseName());

				// Iterate over all courses of the offering (if needed)
				for (CourseOffering course: offering.getCourseOfferings()) {
					// Here, you can tweak what is set on the course
					
					// Only needed if there is something changed on the course...
					hibSession.saveOrUpdate(course);
				}
				
				// Iterate over all the configs of the offering
				for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
					
					// If config is unlimited -> change to limit 100
					boolean unlimited = config.isUnlimitedEnrollment();
					if (unlimited) {
						config.setLimit(100);
						config.setUnlimitedEnrollment(false);
					}
					
					// Iterate over all the subparts of the config
					for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
						
						// Subpart date pattern (to be set if all classes have the same date pattern).
						DatePattern datePattern = null;
						boolean sameDatePattern = true;
						
						// Subpart time pattern (to be set if all classes have the same time pattern).
						TimePattern timePattern = null;
						boolean sameTimePattern = true;
						List<int[]> dateTimes = new ArrayList<int[]>();
						
						// Subpart room preferences (to be set if all classes have the same rooms).
						Set<Location> rooms = null;
						boolean sameRooms = true;
						
						// Iterate over all the classes of the subpart
						for (Class_ clazz: subpart.getClasses()) {
							log.info("-- " + clazz.getClassLabel(hibSession));
							
							// If config was unlimited, set class limit to 100 and room ratio to 0.0
							if (unlimited) {
								int limit = (int)Math.ceil(100.0 / subpart.getClasses().size());
								clazz.setMaxExpectedCapacity(limit);
								clazz.setExpectedCapacity(limit);
								clazz.setRoomRatio(0f);
							}
														
							// Remove class preferences
							clazz.getPreferences().clear();
							
							// Committed (imported) assignment
							Assignment assignment = clazz.getCommittedAssignment();
							
							// Check if this class has the same date pattern as the others
							if (datePattern == null) {
								datePattern = clazz.effectiveDatePattern();
							} else if (sameDatePattern && !datePattern.equals(clazz.effectiveDatePattern())) {
								sameDatePattern = false;
							}
							
							// Find matching time pattern
							if (assignment != null) {
								TimePattern pattern = null;
								for (TimePattern p: patterns) {
									if (p.getNrMeetings() * p.getMinPerMtg() != subpart.getMinutesPerWk()) continue; // minutes per week does not match
									// Find matching days
									TimePatternDays days = null;
									for (TimePatternDays d: p.getDays())
										if (d.getDayCode().equals(assignment.getDays())) { days = d; break; }
									if (days == null) continue;
									// Find matching time
									TimePatternTime time = null;
									for (TimePatternTime t: p.getTimes())
										if (t.getStartSlot().equals(assignment.getStartSlot())) { time = t; break; }
									if (time == null) continue;
									pattern = p; break;
								}
								if (pattern == null) {
									// No pattern found, use exact time pattern
			            			pattern = TimePattern.findExactTime(session.getUniqueId());
			            			if (pattern != null) {
				            			TimePatternModel m = pattern.getTimePatternModel();
				            	    	m.setExactDays(assignment.getDays());
				            	    	m.setExactStartSlot(assignment.getStartSlot());
				                		TimePref tp = new TimePref();
				                		tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
				                		tp.setTimePatternModel(m);
				                		tp.setOwner(clazz);
				                		clazz.getPreferences().add(tp);
			            			}
								} else {
									// Strongly prefer the assigned time
			            			TimePatternModel m = pattern.getTimePatternModel();
			            			for (int d = 0; d < m.getNrDays(); d++)
			            				for (int t = 0; t < m.getNrTimes(); t++) {
			            					if (assignment.getStartSlot() == m.getStartSlot(t) &&
			            						assignment.getDays() == m.getDayCode(d)) {
			            						m.setPreference(d, t, PreferenceLevel.sStronglyPreferred);
			            						dateTimes.add(new int[] {d,t});
			            					}
			            				}
			            			// only set the time pattern on the class if there are more than one class in the subpart (otherwise it will be set on the subpart)
			            			if (subpart.getClasses().size() > 1) {
				                		TimePref tp = new TimePref();
				                		tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
				                		tp.setTimePatternModel(m);
				                		tp.setOwner(clazz);
				                		clazz.getPreferences().add(tp);
			            			}
								}
								
								if (timePattern == null) {
									timePattern = pattern;
								} else if (sameTimePattern && !timePattern.equals(pattern)) {
									sameTimePattern = false;
								}
							}
							
							// Strongly prefer assigned room(s)
							if (assignment != null) {
								// Set number of rooms to match the assignment
								clazz.setNbrRooms(assignment.getRooms().size());
								if (rooms == null) {
									rooms = new TreeSet<Location>(assignment.getRooms());
								} else if (sameRooms && !rooms.equals(new TreeSet<Location>(assignment.getRooms()))) {
									sameRooms = false;
								}
		            			// only set the time pattern on the class if there are more than one class in the subpart (otherwise it will be set on the subpart)
		            			if (subpart.getClasses().size() > 1) {
									for (Location room: assignment.getRooms()) {
										RoomPref rp = new RoomPref();
										rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
										rp.setOwner(clazz);
										rp.setRoom(room);
										clazz.getPreferences().add(rp);
									}
		            			}
							}
							
							hibSession.saveOrUpdate(clazz);
						}
						
						// If all classes have the same (not default) date pattern, set it on subpart instead
						if (datePattern != null && sameDatePattern && !datePattern.isDefault()) {
							subpart.setDatePattern(datePattern);
							for (Class_ clazz: subpart.getClasses()) {
								clazz.setDatePattern(null);
								hibSession.saveOrUpdate(clazz);
							}
						}
						
						// Remove subpart preferences
						subpart.getPreferences().clear();
						
						// Set time preferences on the subpart (if all the classes have the same time pattern)
						if (timePattern != null && sameTimePattern && timePattern.getType() != TimePattern.sTypeExactTime) {
	                		TimePref tp = new TimePref();
	                		tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
	                		TimePatternModel m = timePattern.getTimePatternModel();
	                		if (dateTimes.size() == 1)
	                			for (int[] dt: dateTimes)
	                				m.setPreference(dt[0], dt[1], PreferenceLevel.sStronglyPreferred);
	                		tp.setTimePatternModel(m);
	                		tp.setOwner(subpart);
	                		subpart.getPreferences().add(tp);
						}
						
						// Set room preferences on the subpart (if all the classes have the same room prefs)
						if (rooms != null && sameRooms) {
							for (Location room: rooms) {
								RoomPref rp = new RoomPref();
								rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
								rp.setOwner(subpart);
								rp.setRoom(room);
								subpart.getPreferences().add(rp);
							}
							// These preferences do not need to be replicated on classes, remove room preferences from all the classes
							for (Class_ clazz: subpart.getClasses()) {
								clazz.getPreferences().removeAll(clazz.getPreferences(RoomPref.class));
								hibSession.save(clazz);
							}
						}

						hibSession.saveOrUpdate(subpart);
					}
					
					hibSession.saveOrUpdate(config);
				}

				hibSession.saveOrUpdate(offering);
			}
			
			log.info("All done.");
			
			// Flush and close the hibernate session
			hibSession.flush();
			hibSession.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
