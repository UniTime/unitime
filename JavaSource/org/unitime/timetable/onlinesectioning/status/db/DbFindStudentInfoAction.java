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
package org.unitime.timetable.onlinesectioning.status.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.util.DistanceMetric;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.StudentInfo;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentClassPref;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentInstrMthPref;
import org.unitime.timetable.model.StudentNote;
import org.unitime.timetable.model.StudentSectioningPref;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.status.FindStudentInfoAction;
import org.unitime.timetable.onlinesectioning.status.SectioningStatusFilterAction;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.CourseLookup;
import org.unitime.timetable.onlinesectioning.status.db.DbFindEnrollmentInfoAction.DbCourseRequestMatcher;
import org.unitime.timetable.onlinesectioning.status.db.DbFindEnrollmentInfoAction.DbFindStudentInfoMatcher;

/**
 * @author Tomas Muller
 */
public class DbFindStudentInfoAction extends FindStudentInfoAction {
	private static final long serialVersionUID = 1L;
	
	public boolean isMyStudent(Student student) {
		return iMyStudents != null && iMyStudents.contains(student.getUniqueId());
	}
	
	public boolean isCanSelect(Student student) {
		if (iIsAdmin) return true;
		if (iIsAdvisor) {
			if (iCanEditOtherStudents || (iCanEditMyStudents && isMyStudent(student))) return true;
		}
		return false;
	}

	@Override
	public List<StudentInfo> execute(final OnlineSectioningServer server, final OnlineSectioningHelper helper) {
		if (iFilter == null) return super.execute(server, helper);
		
		Map<Long, StudentInfo> students = new HashMap<Long, StudentInfo>();
		
		int gEnrl = 0, gWait = 0, gRes = 0, gUnasg = 0;
		int gtEnrl = 0, gtWait = 0, gtRes = 0, gtUnasg = 0;
		int gConNeed = 0, gtConNeed = 0, gOvrNeed = 0, gtOvrNeed = 0;
		int gDist = 0, gtDist = 0, gNrDC = 0, gtNrDC = 0, gShr = 0, gtShr = 0; 
		int gFre = 0, gtFre = 0, gPIM = 0, gtPIM = 0, gPSec = 0, gtPSec = 0;
		Set<Long> unassigned = new HashSet<Long>();
		Set<Long> assignedRequests = new HashSet<Long>();
		AcademicSessionInfo session = server.getAcademicSession();
		CourseLookup lookup = new CourseLookup(session);
		DistanceMetric dm = server.getDistanceMetric();
		
		DbFindStudentInfoMatcher sm = new DbFindStudentInfoMatcher(session, iQuery, helper.getStudentNameFormat(), iMyStudents); sm.setServer(server);
		
		Map<CourseOffering, List<CourseRequest>> requests = new HashMap<CourseOffering, List<CourseRequest>>();
		for (CourseRequest cr: (List<CourseRequest>)SectioningStatusFilterAction.getCourseQuery(iFilter, server, helper).select("distinct cr").query(helper.getHibSession()).list()) {
			if (!hasMatchingSubjectArea(cr.getCourseOffering().getSubjectAreaAbbv())) continue;
			if (!isCourseVisible(cr.getCourseOffering().getUniqueId())) continue;			
			if (!query().match(new DbCourseRequestMatcher(session, cr, isConsentToDoCourse(cr.getCourseOffering()), isMyStudent(cr.getCourseDemand().getStudent()), helper.getStudentNameFormat(), lookup))) continue;
			List<CourseRequest> list = requests.get(cr.getCourseOffering());
			if (list == null) {
				list = new ArrayList<CourseRequest>();
				requests.put(cr.getCourseOffering(), list);
			}
			list.add(cr);
		}
		
		for (Map.Entry<CourseOffering, List<CourseRequest>> entry: requests.entrySet()) {
			CourseOffering course = entry.getKey();
			boolean isConsentToDoCourse = isConsentToDoCourse(course);
			
			for (CourseRequest request: entry.getValue()) {
				Student student = request.getCourseDemand().getStudent();
				StudentInfo s = students.get(student.getUniqueId());
				if (s == null) {
					s = new StudentInfo();
					students.put(student.getUniqueId(), s);
					ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student(); s.setStudent(st);
					st.setId(student.getUniqueId());
					st.setSessionId(session.getUniqueId());
					st.setExternalId(student.getExternalUniqueId());
					st.setCanShowExternalId(iCanShowExtIds);
					StudentSectioningStatus status = student.getEffectiveStatus();
					st.setCanRegister(iCanRegister && (status == null
							|| status.hasOption(StudentSectioningStatus.Option.regenabled)
							|| (iIsAdmin && status.hasOption(StudentSectioningStatus.Option.regadmin))
							|| (iIsAdvisor && status.hasOption(StudentSectioningStatus.Option.regadvisor))
							));
					st.setCanUseAssistant(iCanUseAssistant && (status == null
							|| status.hasOption(StudentSectioningStatus.Option.enabled)
							|| (iIsAdmin && status.hasOption(StudentSectioningStatus.Option.admin))
							|| (iIsAdvisor && status.hasOption(StudentSectioningStatus.Option.advisor))
							));
					st.setCanSelect(isCanSelect(student));
					st.setName(helper.getStudentNameFormat().format(student));
					for (StudentAreaClassificationMajor acm: new TreeSet<StudentAreaClassificationMajor>(student.getAreaClasfMajors())) {
						st.addArea(acm.getAcademicArea().getAcademicAreaAbbreviation());
						st.addClassification(acm.getAcademicClassification().getCode());
						st.addMajor(acm.getMajor().getCode());
					}
					for (StudentAccomodation acc: student.getAccomodations()) {
						st.addAccommodation(acc.getAbbreviation());
					}
					for (StudentGroup gr: student.getGroups()) {
						if (gr.getType() == null)
							st.addGroup(gr.getGroupAbbreviation(), gr.getGroupName());
						else
							st.addGroup(gr.getType().getReference(), gr.getGroupAbbreviation(), gr.getGroupName());
					}
	    			for (Advisor a: student.getAdvisors()) {
	    				if (a.getLastName() != null)
	    					st.addAdvisor(helper.getInstructorNameFormat().format(a));
	    			}

					int tEnrl = 0, tWait = 0, tRes = 0, tConNeed = 0, tReq = 0, tUnasg = 0, tOvrNeed = 0, ovrNeed = 0;
					float tCred = 0f;
					int nrDisCnf = 0, maxDist = 0, share = 0; 
					int ftShare = 0;
					List<Float> minsTot = new ArrayList<Float>();
					List<Float> maxsTot = new ArrayList<Float>();
					List<Float> mins = new ArrayList<Float>();
					List<Float> maxs = new ArrayList<Float>();
					int nrCoursesTot = 0, nrCourses = 0;
					for (CourseDemand demand: student.getCourseDemands()) {
						if (!demand.getCourseRequests().isEmpty()) {
							Float minTot = null, maxTot = null;
							Float min = null, max = null;
							for (CourseRequest r: demand.getCourseRequests()) {
								CourseCreditUnitConfig c = r.getCourseOffering().getCredit();
								if (c != null) {
									if (minTot == null || minTot > c.getMinCredit()) minTot = c.getMinCredit();
									if (maxTot == null || maxTot < c.getMaxCredit()) maxTot = c.getMaxCredit();
								}
								if (r.isRequestPending()) { tOvrNeed ++; gtOvrNeed ++ ; }
								if (query().match(new DbCourseRequestMatcher(session, r, isConsentToDoCourse(r.getCourseOffering()), isMyStudent(student), helper.getStudentNameFormat(), lookup))) {
									if (c != null) {
										if (min == null || min > c.getMinCredit()) min = c.getMinCredit();
										if (max == null || max < c.getMaxCredit()) max = c.getMaxCredit();
									}
									if (r.isRequestPending()) { ovrNeed ++; gOvrNeed ++ ; }
								}
							}
							if (minTot != null) {
								minsTot.add(minTot); maxsTot.add(maxTot); 
								if (!demand.isAlternative()) nrCoursesTot ++;
							}
							if (min != null) {
								mins.add(min); maxs.add(max); 
								if (!demand.isAlternative()) nrCourses ++;
							}
							if (!demand.isAlternative()) tReq ++;
							List<StudentClassEnrollment> enrollment = null;
							CourseRequest assigned = null;
							for (CourseRequest r: demand.getCourseRequests()) {
								enrollment = r.getClassEnrollments();
								if (!enrollment.isEmpty()) {
									assigned = r; break;
								}
							}
							if (enrollment.isEmpty()) {
								CourseRequest first = null;
								for (CourseRequest r: demand.getCourseRequests()) {
									if (first == null || r.getOrder() < first.getOrder()) first = r;
								}
								DbCourseRequestMatcher crm = new DbCourseRequestMatcher(session, first, isConsentToDoCourse(first.getCourseOffering()), isMyStudent(student), helper.getStudentNameFormat(), lookup);
								if (crm.canAssign()) {
									tUnasg ++; gtUnasg ++;
									if (demand.isWaitlist()) {
										tWait ++; gtWait ++;
									}
								}
							} else {
								tEnrl ++; gtEnrl ++;
								DbCourseRequestMatcher crm = new DbCourseRequestMatcher(session, assigned, isConsentToDoCourse(assigned.getCourseOffering()), isMyStudent(student), helper.getStudentNameFormat(), lookup);
								if (crm.reservation() != null) {
									tRes ++; gtRes ++;
								}
								if (assigned.getCourseOffering().getConsentType() != null && crm.approval() == null) {
									tConNeed ++; gtConNeed ++;
								}
								if (assigned.getCourseOffering().getCredit() != null) {
									tCred += assigned.getCourseOffering().getCredit().getMinCredit();
									for (StudentClassEnrollment e: enrollment) {
										InstructionalMethod im = e.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod();
										if (im != null)
											s.addIMTotalCredit(im.getReference(), assigned.getCourseOffering().getCredit().getMinCredit());
										break;
									}
								} else {
									for (StudentClassEnrollment e: enrollment) {
										if (e.getClazz().getSchedulingSubpart().getCredit() != null) {
											tCred += e.getClazz().getSchedulingSubpart().getCredit().getMinCredit();
											InstructionalMethod im = e.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod();
											if (im != null)
												s.addIMTotalCredit(im.getReference(), e.getClazz().getSchedulingSubpart().getCredit().getMinCredit());
										}
									}
								}
								
								for (StudentClassEnrollment section: enrollment) {
									Assignment assignment = section.getClazz().getCommittedAssignment();
									if (assignment == null) continue;
									for (StudentClassEnrollment otherSection: student.getClassEnrollments()) {
										if (section.equals(otherSection)) continue;
										Assignment otherAssignment = otherSection.getClazz().getCommittedAssignment();
										if (otherAssignment == null) continue;
										if (isDistanceConflict(student, assignment, otherAssignment, dm)) {
											nrDisCnf ++; gtNrDC ++;
											int d = getDistanceInMinutes(assignment, otherAssignment, dm);
											if (d > maxDist) maxDist = d;
											if (d > gtDist) gtDist = d;
										}
										if (assignment.getTimeLocation().hasIntersection(otherAssignment.getTimeLocation()) && !section.getClazz().isToIgnoreStudentConflictsWith(otherSection.getClazz()) && section.getClazz().getUniqueId() < otherSection.getClazz().getUniqueId()) {
											int sh = assignment.getTimeLocation().nrSharedDays(otherAssignment.getTimeLocation()) * assignment.getTimeLocation().nrSharedHours(otherAssignment.getTimeLocation()) * Constants.SLOT_LENGTH_MIN;
											share += sh;
											gtShr += sh;
										}
									}
									for (CourseDemand cd: student.getCourseDemands()) {
										if (cd.getFreeTime() != null) {
											TimeLocation ft = new TimeLocation(cd.getFreeTime().getDayCode(), cd.getFreeTime().getStartSlot(), cd.getFreeTime().getLength(),
													0, 0.0, 0, null, null, session.getFreeTimePattern(), 0);
											if (ft.hasIntersection(assignment.getTimeLocation())) {
												int sh = assignment.getTimeLocation().nrSharedDays(ft) * assignment.getTimeLocation().nrSharedHours(ft) * Constants.SLOT_LENGTH_MIN;
												ftShare += sh;
												gtFre += sh;
											}
										}
									}
								}
							}
						}
					}

					Collections.sort(mins);
					Collections.sort(maxs);
					float min = 0f, max = 0f;
					for (int i = 0; i < nrCourses; i++) {
						min += mins.get(i);
						max += maxs.get(maxs.size() - i - 1);
					}
					Collections.sort(minsTot);
					Collections.sort(maxsTot);
					float minTot = 0f, maxTot = 0f;
					for (int i = 0; i < nrCoursesTot; i++) {
						minTot += minsTot.get(i);
						maxTot += maxsTot.get(maxsTot.size() - i - 1);
					}
					if (student.isRequestPending()) {
						if (nrCourses == nrCoursesTot) { gOvrNeed ++; ovrNeed ++; }
						gtOvrNeed ++; tOvrNeed ++;
					}
					s.setRequestCredit(min, max);
					s.setTotalRequestCredit(minTot, maxTot);
					s.setTotalEnrollment(tEnrl);
					s.setTotalReservation(tRes);
					s.setTotalWaitlist(tWait);
					s.setTotalUnassigned(tUnasg);
					s.setTotalConsentNeeded(tConNeed);
					s.setTotalOverrideNeeded(tOvrNeed);
					s.setEnrollment(0);
					s.setReservation(0);
					s.setWaitlist(0);
					s.setUnassigned(0);
					s.setConsentNeeded(0);
					s.setOverrideNeeded(ovrNeed);
					s.setRequested(tReq);
					s.setStatus(student.getSectioningStatus() == null ? session.getDefaultSectioningStatus() : student.getSectioningStatus().getReference());
					s.setEmailDate(student.getScheduleEmailedDate() == null ? null : student.getScheduleEmailedDate());
					s.setCredit(0f);
					s.setTotalCredit(tCred);
					s.setNrDistanceConflicts(0);
					s.setLongestDistanceMinutes(0);
					s.setOverlappingMinutes(0);
					s.setTotalNrDistanceConflicts(nrDisCnf);
					s.setTotalLongestDistanceMinutes(maxDist);
					s.setTotalOverlappingMinutes(share);
					s.setFreeTimeOverlappingMins(0);
					s.setTotalFreeTimeOverlappingMins(ftShare);
					s.setPrefInstrMethConflict(0);
					s.setTotalPrefInstrMethConflict(0);
					s.setPrefSectionConflict(0);
					s.setTotalPrefSectionConflict(0);
					
					StudentNote note = null;
					for (StudentNote n: student.getNotes())
						if (note == null || note.compareTo(n) > 0) note = n;
					if (note != null) s.setNote(note.getTextNote());
				}
				DbCourseRequestMatcher crm = new DbCourseRequestMatcher(session, request, isConsentToDoCourse, isMyStudent(student), helper.getStudentNameFormat(), lookup);
				if (!crm.enrollment().isEmpty()) {
					if (assignedRequests.add(crm.request().getCourseDemand().getUniqueId())) {
						s.setEnrollment(s.getEnrollment() + 1); gEnrl ++;
						if (crm.reservation() != null) { s.setReservation(s.getReservation() + 1); gRes ++; }
						if (course.getConsentType() != null && crm.approval() == null) {
							s.setConsentNeeded(s.getConsentNeeded() + 1); gConNeed ++;
						}
						for (StudentClassEnrollment e: crm.enrollment()) {
							if (e.getTimestamp() != null) {
								if (s.getEnrolledDate() == null)
									s.setEnrolledDate(e.getTimestamp());
								else if (e.getTimestamp().after(s.getEnrolledDate()))
									s.setEnrolledDate(e.getTimestamp());
							}
						}
						if (crm.approval() != null) {
							for (StudentClassEnrollment e: crm.enrollment()) {
								if (e.getApprovedDate() != null) {
									if (s.getApprovedDate() == null)
										s.setApprovedDate(e.getApprovedDate());
									else if (e.getApprovedDate().after(s.getApprovedDate()))
										s.setApprovedDate(e.getApprovedDate());
								}
							}
						}
						if (course.getCredit() != null) {
							s.setCredit(s.getCredit() + course.getCredit().getMinCredit());
							for (StudentClassEnrollment e: crm.enrollment()) {
								InstructionalMethod im = e.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod();
								if (im != null)
									s.addIMCredit(im.getReference(), course.getCredit().getMinCredit());
								break;
							}
						} else {
							for (StudentClassEnrollment e: crm.enrollment()) {
								if (e.getClazz().getSchedulingSubpart().getCredit() != null) {
									s.setCredit(s.getCredit() + e.getClazz().getSchedulingSubpart().getCredit().getMinCredit());
									InstructionalMethod im = e.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod();
									if (im != null)
										s.addIMCredit(im.getReference(), e.getClazz().getSchedulingSubpart().getCredit().getMinCredit());
								}
							}
						}
						boolean hasIm = false;
						boolean im = false;
						Set<String> allSubpart = new HashSet<String>();
						Set<String> selectedSubparts = new HashSet<String>();
						for (StudentSectioningPref pref: request.getPreferences()) {
							if (pref instanceof StudentInstrMthPref) {
								StudentInstrMthPref imp = (StudentInstrMthPref)pref;
								InstructionalMethod method = crm.enrollment().get(0).getClazz().getSchedulingSubpart().getInstrOfferingConfig().getEffectiveInstructionalMethod();
								hasIm = true;
								if (!im && method != null && method.equals(imp.getInstructionalMethod())) { im = true; }
							}
							if (pref instanceof StudentClassPref) {
								StudentClassPref scp = (StudentClassPref)pref;
								allSubpart.add(scp.getClazz().getSchedulingSubpart().getItypeDesc());
								for (StudentClassEnrollment section: crm.enrollment()) {
									if (scp.getClazz().equals(section.getClazz()))
										selectedSubparts.add(scp.getClazz().getSchedulingSubpart().getItypeDesc());
								}
							}
						}
						if (hasIm) {
							s.setTotalPrefInstrMethConflict(s.getTotalPrefInstrMethConflict() + 1);
							gtPIM++;
							if (im) {
								s.setPrefInstrMethConflict(s.getPrefInstrMethConflict() + 1);
								gPIM++;
							}
						}
						if (!allSubpart.isEmpty()) {
							s.setTotalPrefSectionConflict(s.getTotalPrefSectionConflict() + allSubpart.size());
							gtPSec += allSubpart.size();
							s.setPrefSectionConflict(s.getPrefSectionConflict() + selectedSubparts.size());
							gPSec += selectedSubparts.size();
						}
					}
					for (StudentClassEnrollment section: crm.enrollment()) {
						Assignment assignment = section.getClazz().getCommittedAssignment();
						if (assignment == null) continue;
						for (StudentClassEnrollment otherSection: student.getClassEnrollments()) {
							if (section.equals(otherSection)) continue;
							Assignment otherAssignment = otherSection.getClazz().getCommittedAssignment();
							if (otherAssignment == null) continue;
							if (isDistanceConflict(student, assignment, otherAssignment, dm)) {
								s.setNrDistanceConflicts(s.getNrDistanceConflicts() + 1); gNrDC ++;
								int d = getDistanceInMinutes(assignment, otherAssignment, dm);
								if (d > s.getLongestDistanceMinutes()) s.setLongestDistanceMinutes(d);
								if (d > gDist) gDist = d;
							}
							if (assignment.getTimeLocation().hasIntersection(otherAssignment.getTimeLocation()) && !section.getClazz().isToIgnoreStudentConflictsWith(otherSection.getClazz())) {
								if (section.getClazz().getUniqueId() < otherSection.getClazz().getUniqueId() ||
									!query().match(new DbCourseRequestMatcher(session, otherSection.getCourseRequest(), isConsentToDoCourse(otherSection.getCourseOffering()), isMyStudent(student), helper.getStudentNameFormat(), lookup))) {
									int sh = assignment.getTimeLocation().nrSharedDays(otherAssignment.getTimeLocation()) * assignment.getTimeLocation().nrSharedHours(otherAssignment.getTimeLocation()) * Constants.SLOT_LENGTH_MIN;
									s.setOverlappingMinutes(s.getOverlappingMinutes() + sh);
									gShr += sh;
								}
							}
						}
						for (CourseDemand cd: student.getCourseDemands()) {
							if (cd.getFreeTime() != null) {
								TimeLocation ft = new TimeLocation(cd.getFreeTime().getDayCode(), cd.getFreeTime().getStartSlot(), cd.getFreeTime().getLength(),
										0, 0.0, 0, null, null, session.getFreeTimePattern(), 0);
								if (ft.hasIntersection(assignment.getTimeLocation())) {
									int sh = assignment.getTimeLocation().nrSharedDays(ft) * assignment.getTimeLocation().nrSharedHours(ft) * Constants.SLOT_LENGTH_MIN;
									s.setFreeTimeOverlappingMins(s.getFreeTimeOverlappingMins() + sh);
									gFre += sh;
								}
							}
						}
					}
				} else if (unassigned.add(crm.request().getCourseDemand().getUniqueId())) {
					if (crm.canAssign()) {
						if (crm.request().getCourseDemand().isWaitlist()) {
							s.setWaitlist(s.getWaitlist() + 1); gWait ++;
							if (s.getTopWaitingPriority() == null)
								s.setTopWaitingPriority(1 + crm.request().getCourseDemand().getPriority());
							else
								s.setTopWaitingPriority(Math.min(1 + crm.request().getCourseDemand().getPriority(), s.getTopWaitingPriority()));
						}
						s.setUnassigned(s.getUnassigned() + 1); gUnasg ++;	
					}
					for (CourseRequest c: crm.request().getCourseDemand().getCourseRequests()) {
						boolean reqIm = false;
						Set<String> reqSubparts = new HashSet<String>();
						for (StudentSectioningPref pref: c.getPreferences()) {
							if (!pref.isRequired()) continue;
							if (pref instanceof StudentInstrMthPref) {
								reqIm = true;
							}
							if (pref instanceof StudentClassPref) {
								StudentClassPref scp = (StudentClassPref)pref;
								reqSubparts.add(scp.getClazz().getSchedulingSubpart().getItypeDesc());
							}
						}
						if (reqIm) {
							s.setTotalPrefInstrMethConflict(s.getTotalPrefInstrMethConflict() + 1);
							gtPIM++;
						}
						if (!reqSubparts.isEmpty()) {
							s.setTotalPrefSectionConflict(s.getTotalPrefSectionConflict() + reqSubparts.size());
							gtPSec += reqSubparts.size();
						}
					}
				}
				if (crm.request().getCourseDemand().getTimestamp() != null) {
					if (s.getRequestedDate() == null)
						s.setRequestedDate(crm.request().getCourseDemand().getTimestamp());
					else if (crm.request().getCourseDemand().getTimestamp().after(s.getRequestedDate()))
						s.setRequestedDate(crm.request().getCourseDemand().getTimestamp());
				}
			}
		}
		
		List<StudentInfo> ret = new ArrayList<StudentInfo>(students.values());
		
		if (iSubjectAreas == null && iCoursesIcoordinate == null) {
			for (Student student: (List<Student>)SectioningStatusFilterAction.getQuery(iFilter, server, helper).select("distinct s").query(helper.getHibSession()).list()) {
				if (students.containsKey(student.getUniqueId())) continue;
				if (!sm.match(student)) continue;
				StudentInfo s = new StudentInfo();
				ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student(); s.setStudent(st);
				st.setId(student.getUniqueId());
				st.setSessionId(session.getUniqueId());
				st.setExternalId(student.getExternalUniqueId());
				st.setCanShowExternalId(iCanShowExtIds);
				StudentSectioningStatus status = student.getEffectiveStatus();
				st.setCanRegister(iCanRegister && (status == null
						|| status.hasOption(StudentSectioningStatus.Option.regenabled)
						|| (iIsAdmin && status.hasOption(StudentSectioningStatus.Option.regadmin))
						|| (iIsAdvisor && status.hasOption(StudentSectioningStatus.Option.regadvisor))
						));
				st.setCanUseAssistant(iCanUseAssistant && (status == null
						|| status.hasOption(StudentSectioningStatus.Option.enabled)
						|| (iIsAdmin && status.hasOption(StudentSectioningStatus.Option.admin))
						|| (iIsAdvisor && status.hasOption(StudentSectioningStatus.Option.advisor))
						));
				st.setCanSelect(isCanSelect(student));
				st.setName(helper.getStudentNameFormat().format(student));
				for (StudentAreaClassificationMajor acm: new TreeSet<StudentAreaClassificationMajor>(student.getAreaClasfMajors())) {
					st.addArea(acm.getAcademicArea().getAcademicAreaAbbreviation());
					st.addClassification(acm.getAcademicClassification().getCode());
					st.addMajor(acm.getMajor().getCode());
				}
				for (StudentAccomodation acc: student.getAccomodations()) {
					st.addAccommodation(acc.getAbbreviation());
				}
				for (StudentGroup gr: student.getGroups()) {
					if (gr.getType() == null)
						st.addGroup(gr.getGroupAbbreviation(), gr.getGroupName());
					else
						st.addGroup(gr.getType().getReference(), gr.getGroupAbbreviation(), gr.getGroupName());
				}
    			for (Advisor a: student.getAdvisors()) {
    				if (a.getLastName() != null)
    					st.addAdvisor(helper.getInstructorNameFormat().format(a));
    			}
				s.setStatus(student.getSectioningStatus() == null ? session.getDefaultSectioningStatus() : student.getSectioningStatus().getReference());
				s.setEmailDate(student.getScheduleEmailedDate() == null ? null : student.getScheduleEmailedDate());
				
				StudentNote note = null;
				for (StudentNote n: student.getNotes())
					if (note == null || note.compareTo(n) > 0) note = n;
				if (note != null) s.setNote(note.getTextNote());
				
				ret.add(s);
			}
		}
		
		Collections.sort(ret, new Comparator<StudentInfo>() {
			@Override
			public int compare(StudentInfo s1, StudentInfo s2) {
				int cmp = s1.getStudent().getName().compareTo(s2.getStudent().getName());
				if (cmp != 0) return cmp;
				return new Long(s1.getStudent().getId()).compareTo(s2.getStudent().getId());
			}
		});
		
		if (limit() != null && ret.size() >= limit()) {
			List<StudentInfo>  r = new ArrayList<StudentInfo>(limit());
			for (StudentInfo i: ret) {
				r.add(i);
				if (r.size() == limit()) break;
			}
			ret = r;
		}
		
		// if (students.size() > 0) {
		StudentInfo t = new StudentInfo();
		
		t.setEnrollment(gEnrl);
		t.setReservation(gRes);
		t.setWaitlist(gWait);
		t.setUnassigned(gUnasg);
		
		t.setTotalEnrollment(gtEnrl);
		t.setTotalReservation(gtRes);
		t.setTotalWaitlist(gtWait);
		t.setTotalUnassigned(gtUnasg);
		
		t.setConsentNeeded(gConNeed);
		t.setTotalConsentNeeded(gtConNeed);
		t.setOverrideNeeded(gOvrNeed);
		t.setTotalOverrideNeeded(gtOvrNeed);
		
		t.setNrDistanceConflicts(gNrDC);
		t.setTotalNrDistanceConflicts(gtNrDC);
		t.setLongestDistanceMinutes(gDist);
		t.setTotalLongestDistanceMinutes(gtDist);
		t.setOverlappingMinutes(gShr);
		t.setTotalOverlappingMinutes(gtShr);
		t.setFreeTimeOverlappingMins(gFre);
		t.setTotalFreeTimeOverlappingMins(gtFre);
		t.setPrefInstrMethConflict(gPIM);
		t.setTotalPrefInstrMethConflict(gtPIM);
		t.setPrefSectionConflict(gPSec);
		t.setTotalPrefSectionConflict(gtPSec);

		ret.add(t);				
		
		return ret;
	}
	
	public int getDistanceInMinutes(Assignment as1, Assignment as2, DistanceMetric m) {
		Placement p1 = as1.getPlacement(), p2 = as2.getPlacement();
		if (p1.getNrRooms() == 0 || p2.getNrRooms() == 0) return 0;
		TimeLocation t1 = p1.getTimeLocation(), t2 = p2.getTimeLocation();
		if (t1 == null || t2 == null || !t1.shareDays(t2) || !t1.shareWeeks(t2)) return 0;
        int a1 = t1.getStartSlot(), a2 = t2.getStartSlot();
        if (m.doComputeDistanceConflictsBetweenNonBTBClasses()) {
        	if (a1 + t1.getLength() <= a2) {
        		int dist = Placement.getDistanceInMinutes(m, p1, p2);
        		if (dist > t1.getBreakTime() + Constants.SLOT_LENGTH_MIN * (a2 - a1 - t1.getLength()))
        			return dist;
        	}
        } else {
        	if (a1 + t1.getLength() == a2)
        		return Placement.getDistanceInMinutes(m, p1, p2);
        }
        return 0;
    }
	
	public boolean isDistanceConflict(Student student, Assignment as1, Assignment as2, DistanceMetric m) {
		Placement p1 = as1.getPlacement(), p2 = as2.getPlacement();
		if (p1.getNrRooms() == 0 || p2.getNrRooms() == 0) return false;
		TimeLocation t1 = p1.getTimeLocation(), t2 = p2.getTimeLocation();
		if (t1 == null || t2 == null || !t1.shareDays(t2) || !t1.shareWeeks(t2)) return false;
		int a1 = t1.getStartSlot(), a2 = t2.getStartSlot();
		boolean sd = false;
		for (StudentAccomodation a: student.getAccomodations()) {
			if (m.getShortDistanceAccommodationReference().equals(a.getAbbreviation())) { sd = true; break; }
		}
        if (sd) {
        	if (m.doComputeDistanceConflictsBetweenNonBTBClasses()) {
	        	if (a1 + t1.getLength() <= a2) {
	        		int dist = Placement.getDistanceInMinutes(m, p1, p2);
	        		return (dist > Constants.SLOT_LENGTH_MIN * (a2 - a1 - t1.getLength()));
	        	}
	        } else {
	        	if (a1 + t1.getLength() == a2)
	        		return Placement.getDistanceInMinutes(m, p1, p2) > 0;
	        }
		} else {
	        if (m.doComputeDistanceConflictsBetweenNonBTBClasses()) {
	        	if (a1 + t1.getLength() <= a2) {
	        		int dist = Placement.getDistanceInMinutes(m, p1, p2);
	        		return (dist > t1.getBreakTime() + Constants.SLOT_LENGTH_MIN * (a2 - a1 - t1.getLength()));
	        	}
	        } else {
	        	if (a1 + t1.getLength() == a2)
	        		return Placement.getDistanceInMinutes(m, p1, p2) > t1.getBreakTime();
	        }
		}
        return false;
	}
	
	public boolean isConsentToDoCourse(CourseOffering course) {
		return iCoursesIcanApprove != null && course.getConsentType() != null && iCoursesIcanApprove.contains(course.getUniqueId());
	}
}
