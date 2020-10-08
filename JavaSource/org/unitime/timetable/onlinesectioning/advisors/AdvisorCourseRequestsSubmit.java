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
package org.unitime.timetable.onlinesectioning.advisors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.events.QueryEncoderBackend;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisorCourseRequestSubmission;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomCriticalCoursesHolder;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider.CriticalCourses;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
public class AdvisorCourseRequestsSubmit implements OnlineSectioningAction<AdvisorCourseRequestSubmission> {
	private static final long serialVersionUID = 1L;
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);
	private AdvisingStudentDetails iDetails;
	
	public AdvisorCourseRequestsSubmit withDetails(AdvisingStudentDetails details) {
		iDetails = details;
		return this;
	}
	
	public AdvisingStudentDetails getDetails() { return iDetails; }
	
	@Override
	public AdvisorCourseRequestSubmission execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		try {
			AdvisorCourseRequestSubmission ret = new AdvisorCourseRequestSubmission();
			ret.setName("crf-" + server.getAcademicSession().getTerm() + server.getAcademicSession().getYear() + "-" + getDetails().getStudentName().replaceAll("[&$\\+,/:;=\\?@<>\\[\\]\\{\\}\\|\\^\\~%#`\\t\\s\\n\\r \\\\]", "") + "-" + getDetails().getStudentExternalId());
			ret.setLink("export?q=" + QueryEncoderBackend.encode("output=acrf.pdf&sid=" + server.getAcademicSession().getUniqueId() + "&user=" + helper.getUser().getExternalId() + "&id=" + getDetails().getStudentExternalId()));
			
			OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
			action.setStudent(OnlineSectioningLog.Entity.newBuilder()
				.setUniqueId(getDetails().getStudentId())
				.setExternalId(getDetails().getStudentExternalId())
				.setName(getDetails().getStudentName()));
			if (getDetails().getStatus() != null && getDetails().getStatus().getUniqueId() != null) {
				action.addOther(OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(getDetails().getStatus().getUniqueId())
						.setName(getDetails().getStatus().getLabel())
						.setExternalId(getDetails().getStatus().getReference())
						.setType(OnlineSectioningLog.Entity.EntityType.OTHER));
			}
			if (getDetails().getRequest() != null)
				for (OnlineSectioningLog.Request r: OnlineSectioningHelper.toProto(getDetails().getRequest()))
					action.addRequest(r);

			if (getDetails().isCanUpdate()) {
				Date ts = new Date();
				Lock lock = server.lockStudent(getDetails().getStudentId(), null, name());
				try {
					helper.beginTransaction();
					XStudent student = server.getStudent(getDetails().getStudentId());
					Student dbStudent = StudentDAO.getInstance().get(getDetails().getStudentId(), helper.getHibSession());
					if (dbStudent != null) {
						
						CriticalCourses critical = null;
						try {
							if (CustomCriticalCoursesHolder.hasProvider())
								critical = CustomCriticalCoursesHolder.getProvider().getCriticalCourses(server, helper, new XStudentId(dbStudent, helper));
						} catch (Exception e) {
							helper.warn("Failed to lookup critical courses: " + e.getMessage(), e);
						}
					
						List<AdvisorCourseRequest> acrs = new ArrayList<AdvisorCourseRequest>();
						if (dbStudent.getAdvisorCourseRequests() == null) {
							dbStudent.setAdvisorCourseRequests(new HashSet<AdvisorCourseRequest>());
						} else {
							acrs.addAll(dbStudent.getAdvisorCourseRequests());
							Collections.sort(acrs);
						}
						
						if (getDetails().getRequest() != null) {
							int priority = 0;
							for (Request request: getDetails().getRequest().getCourses()) {
								if (request.hasRequestedCourse()) {
									int alt = 0;
									for (RequestedCourse rc: request.getRequestedCourse()) {
										if (rc.isFreeTime()) {
											for (CourseRequestInterface.FreeTime ft: rc.getFreeTime()) {
												AdvisorCourseRequest acr = null;
												for (Iterator<AdvisorCourseRequest> i = acrs.iterator(); i.hasNext(); ) {
													AdvisorCourseRequest adept = i.next();
													if (adept.getFreeTime() != null) {
														acr = adept; i.remove(); break;
													}
												}
												if (acr == null) {
													acr = new AdvisorCourseRequest();
													acr.setStudent(dbStudent);
													acr.setChangedBy(helper.getUser().getExternalId());
													acr.setTimestamp(ts);
													dbStudent.getAdvisorCourseRequests().add(acr);
												} else if (acr.getPreferences() != null) {
													acr.getPreferences().clear();
												}
												FreeTime free = acr.getFreeTime();
												if (free == null) {
													free = new FreeTime();
													acr.setFreeTime(free);
												}
												free.setCategory(0);
												free.setDayCode(DayCode.toInt(DayCode.toDayCodes(ft.getDays())));
												free.setStartSlot(ft.getStart());
												free.setLength(ft.getLength());
												free.setSession(dbStudent.getSession());
												free.setName(ft.toString());
												acr.setCourseOffering(null);
												acr.setCourse(CONST.freePrefix() + ft.toString(CONST.shortDays(), CONST.useAmPm()));
												acr.setPriority(priority); acr.setAlternative(alt); acr.setSubstitute(false);
												if (alt == 0) {
													acr.setCredit(request.getAdvisorCredit());
													acr.setNotes(request.getAdvisorNote());
												} else {
													acr.setCredit(null); acr.setNotes(null); 
												}
												acr.setCritical(0);
												helper.getHibSession().saveOrUpdate(free);
												helper.getHibSession().saveOrUpdate(acr);
												alt++;
											}
											continue;
										}
										AdvisorCourseRequest acr = null;
										for (Iterator<AdvisorCourseRequest> i = acrs.iterator(); i.hasNext(); ) {
											AdvisorCourseRequest adept = i.next();
											if (rc.hasCourseId() && adept.getCourseOffering() != null && rc.getCourseId().equals(adept.getCourseOffering().getUniqueId())) {
												acr = adept; i.remove(); break;
											} else if (!rc.hasCourseId() && adept.getCourseOffering() == null && rc.getCourseName().equals(adept.getCourse())) {
												acr = adept; i.remove(); break;
											}
										}
										if (acr == null) {
											acr = new AdvisorCourseRequest();
											acr.setStudent(dbStudent);
											acr.setChangedBy(helper.getUser().getExternalId());
											acr.setTimestamp(ts);
											dbStudent.getAdvisorCourseRequests().add(acr);
										}
										acr.setCourseOffering(rc.hasCourseId() ? CourseOfferingDAO.getInstance().get(rc.getCourseId(), helper.getHibSession()) : null);
										if (acr.getCourseOffering() != null && !acr.getCourseOffering().getInstructionalOffering().getSessionId().equals(server.getAcademicSession().getUniqueId())) {
											XCourseId course = server.getCourse(acr.getCourseOffering().getCourseName());
											helper.warn(acr.getCourseOffering().getCourseName() + " is from a wrong academic session" + (course == null ? ", course link removed." : ", found " + course.getCourseName() + " instead."));
											acr.setCourseOffering(course == null ? null : CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession()));
										}
										acr.setCourse(rc.getCourseName());
										acr.setPriority(priority); acr.setAlternative(alt); acr.setSubstitute(false);
										if (acr.getFreeTime() != null) {
											helper.getHibSession().delete(acr.getFreeTime());
											acr.setFreeTime(null);
										}
										if (alt == 0) {
											acr.setCredit(request.getAdvisorCredit());
											acr.setNotes(request.getAdvisorNote());
										} else {
											acr.setCredit(null); acr.setNotes(null); 
										}
										acr.setCritical(acr.isCritical(critical));
										acr.updatePreferences(rc, helper.getHibSession());
										helper.getHibSession().saveOrUpdate(acr);
										alt++;
									}
								} else {
									AdvisorCourseRequest acr = null;
									for (Iterator<AdvisorCourseRequest> i = acrs.iterator(); i.hasNext(); ) {
										AdvisorCourseRequest adept = i.next();
										if (adept.getCourseOffering() == null && adept.getFreeTime() == null && adept.getCourse() == null) {
											acr = adept; i.remove(); break;
										}
									}
									if (acr == null) {
										acr = new AdvisorCourseRequest();
										acr.setStudent(dbStudent);
										dbStudent.getAdvisorCourseRequests().add(acr);
									} else if (acr.getPreferences() != null) {
										acr.getPreferences().clear();
									}
									if (acr.getFreeTime() != null) {
										helper.getHibSession().delete(acr.getFreeTime());
										acr.setFreeTime(null);
									}
									acr.setFreeTime(null);
									acr.setCourse(null); acr.setCourseOffering(null);
									acr.setPriority(priority); acr.setAlternative(0); acr.setSubstitute(false);
									acr.setCredit(request.getAdvisorCredit());
									acr.setNotes(request.getAdvisorNote());
									acr.setChangedBy(helper.getUser().getExternalId());
									acr.setTimestamp(ts);
									acr.setCritical(0);
									helper.getHibSession().saveOrUpdate(acr);
								}
								priority ++;
							}
							// substitutes
							for (Request request: getDetails().getRequest().getAlternatives()) {
								if (request.hasRequestedCourse()) {
									int alt = 0;
									for (RequestedCourse rc: request.getRequestedCourse()) {
										AdvisorCourseRequest acr = null;
										for (Iterator<AdvisorCourseRequest> i = acrs.iterator(); i.hasNext(); ) {
											AdvisorCourseRequest adept = i.next();
											if (rc.hasCourseId() && adept.getCourseOffering() != null && rc.getCourseId().equals(adept.getCourseOffering().getUniqueId())) {
												acr = adept; i.remove(); break;
											} else if (!rc.hasCourseId() && adept.getCourseOffering() == null && rc.getCourseName().equals(adept.getCourse())) {
												acr = adept; i.remove(); break;
											}
										}
										if (acr == null) {
											acr = new AdvisorCourseRequest();
											acr.setStudent(dbStudent);
											acr.setChangedBy(helper.getUser().getExternalId());
											acr.setTimestamp(ts);
											dbStudent.getAdvisorCourseRequests().add(acr);
										}
										acr.setCourseOffering(rc.hasCourseId() ? CourseOfferingDAO.getInstance().get(rc.getCourseId(), helper.getHibSession()) : null);
										if (acr.getCourseOffering() != null && !acr.getCourseOffering().getInstructionalOffering().getSessionId().equals(server.getAcademicSession().getUniqueId())) {
											XCourseId course = server.getCourse(acr.getCourseOffering().getCourseName());
											helper.warn(acr.getCourseOffering().getCourseName() + " is from a wrong academic session" + (course == null ? ", course link removed." : ", found " + course.getCourseName() + " instead."));
											acr.setCourseOffering(course == null ? null : CourseOfferingDAO.getInstance().get(course.getCourseId(), helper.getHibSession()));
										}
										acr.setCourse(rc.getCourseName());
										acr.setPriority(priority); acr.setAlternative(alt); acr.setSubstitute(true);
										if (alt == 0) {
											acr.setCredit(request.getAdvisorCredit());
											acr.setNotes(request.getAdvisorNote());
										} else {
											acr.setCredit(null); acr.setNotes(null); 
										}
										if (acr.getFreeTime() != null) {
											helper.getHibSession().delete(acr.getFreeTime());
											acr.setFreeTime(null);
										}
										acr.setCritical(acr.isCritical(critical));
										acr.updatePreferences(rc, helper.getHibSession());
										helper.getHibSession().saveOrUpdate(acr);
										alt++;
									}
								} else {
									AdvisorCourseRequest acr = null;
									for (Iterator<AdvisorCourseRequest> i = acrs.iterator(); i.hasNext(); ) {
										AdvisorCourseRequest adept = i.next();
										if (adept.getCourseOffering() == null && adept.getFreeTime() == null && adept.getCourse() == null) {
											acr = adept; i.remove(); break;
										}
									}
									if (acr == null) {
										acr = new AdvisorCourseRequest();
										acr.setStudent(dbStudent);
										dbStudent.getAdvisorCourseRequests().add(acr);
									} else if (acr.getPreferences() != null) {
										acr.getPreferences().clear();
									}
									if (acr.getFreeTime() != null) {
										helper.getHibSession().delete(acr.getFreeTime());
										acr.setFreeTime(null);
									}
									acr.setCourse(null); acr.setCourseOffering(null);
									acr.setPriority(priority); acr.setAlternative(0); acr.setSubstitute(true);
									acr.setCredit(request.getAdvisorCredit());
									acr.setNotes(request.getAdvisorNote());
									acr.setChangedBy(helper.getUser().getExternalId());
									acr.setTimestamp(ts);
									acr.setCritical(0);
									helper.getHibSession().saveOrUpdate(acr);
								}
								priority ++;
							}
							if (getDetails().getRequest().hasCreditNote()) {
								AdvisorCourseRequest acr = null;
								for (Iterator<AdvisorCourseRequest> i = acrs.iterator(); i.hasNext(); ) {
									AdvisorCourseRequest adept = i.next();
									if (adept.getPriority() == -1) {
										acr = adept; i.remove(); break;
									}
								}
								if (acr == null) {
									acr = new AdvisorCourseRequest();
									acr.setStudent(dbStudent);
									dbStudent.getAdvisorCourseRequests().add(acr);
								} else if (acr.getPreferences() != null) {
									acr.getPreferences().clear();
								}
								if (acr.getFreeTime() != null) {
									helper.getHibSession().delete(acr.getFreeTime());
									acr.setFreeTime(null);
								}
								acr.setCourse(null); acr.setCourseOffering(null);
								acr.setPriority(-1); acr.setAlternative(0); acr.setSubstitute(false);
								acr.setCredit(null);
								acr.setNotes(getDetails().getRequest().getCreditNote());
								acr.setChangedBy(helper.getUser().getExternalId());
								acr.setTimestamp(ts);
								acr.setCritical(0);
								helper.getHibSession().saveOrUpdate(acr);
							}
						}
						
						
						for (AdvisorCourseRequest acr: acrs) {
							if (acr.getFreeTime() != null)
								helper.getHibSession().delete(acr.getFreeTime());
							helper.getHibSession().delete(acr);
							dbStudent.getAdvisorCourseRequests().remove(acr);
						}
						
						// change status
						if (getDetails().getStatus() != null && student != null) {
							String current = (student.getStatus() == null ? "" : student.getStatus());
							if (!getDetails().getStatus().getReference().equals(current)) {
								//status change
								StudentSectioningStatus status = (getDetails().getStatus().getReference().isEmpty() ? null :
									StudentSectioningStatus.getStatus(getDetails().getStatus().getReference(), server.getAcademicSession().getUniqueId(), helper.getHibSession()));

								String oldStatus = (dbStudent.getSectioningStatus() != null ? dbStudent.getSectioningStatus().getReference() :
									dbStudent.getSession().getDefaultSectioningStatus() != null ? MSG.studentStatusSessionDefault(dbStudent.getSession().getDefaultSectioningStatus().getReference())
									: MSG.studentStatusSystemDefault());
								
								if (dbStudent.getSectioningStatus() != null)
									action.addOptionBuilder().setKey("old-status").setValue(dbStudent.getSectioningStatus().getReference());

								student.setStatus(status == null ? null : status.getReference());
								dbStudent.setSectioningStatus(status);
								
								String newStatus = (dbStudent.getSectioningStatus() != null ? dbStudent.getSectioningStatus().getReference() :
									dbStudent.getSession().getDefaultSectioningStatus() != null ? MSG.studentStatusSessionDefault(dbStudent.getSession().getDefaultSectioningStatus().getReference())
									: MSG.studentStatusSystemDefault());
								if (dbStudent.getSectioningStatus() != null)
									action.addOptionBuilder().setKey("new-status").setValue(dbStudent.getSectioningStatus().getReference());
								if (oldStatus.equals(newStatus))
									action.addMessage(OnlineSectioningLog.Message.newBuilder().setText(oldStatus).setTimeStamp(ts.getTime()).setLevel(OnlineSectioningLog.Message.Level.INFO));
								else
									action.addMessage(OnlineSectioningLog.Message.newBuilder().setText(oldStatus + " &rarr; " + newStatus).setTimeStamp(ts.getTime()).setLevel(OnlineSectioningLog.Message.Level.INFO));
								
								helper.getHibSession().saveOrUpdate(dbStudent);
								action.setResult(OnlineSectioningLog.Action.ResultType.TRUE);
							} else {
								action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
							}
						}
						
						if (getDetails().getRequest().hasPin()) {
							student.setPinReleased(getDetails().getRequest().isPinReleased());
							student.setPin(getDetails().getRequest().getPin());
							dbStudent.setPinReleased(getDetails().getRequest().isPinReleased());
							dbStudent.setPin(getDetails().getRequest().getPin());
							action.addOptionBuilder().setKey("PIN").setValue(getDetails().getRequest().getPin() + (getDetails().getRequest().isPinReleased() ? "" : " NOT RELEASED"));
							helper.getHibSession().saveOrUpdate(dbStudent);
						}
						
						if (student != null) {
							student.setAdvisorRequests(dbStudent, helper, server.getAcademicSession().getFreeTimePattern());
							server.update(student, false);
						}
						
						ret.setName("crf-" + server.getAcademicSession().getTerm() + server.getAcademicSession().getYear() + "-" +
								dbStudent.getLastName() + (dbStudent.getFirstName() == null ? "" : "-" + dbStudent.getFirstName()) + (dbStudent.getMiddleName() == null ? "" : "-" + dbStudent.getMiddleName()) +
								"-" + dbStudent.getExternalUniqueId());
					}
					helper.commitTransaction();
					ret.setUpdated(true);
				} catch (Exception e) {
					helper.rollbackTransaction();
					if (e instanceof SectioningException) throw (SectioningException)e;
					throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
				} finally {
					lock.release();
				}
			}

			generatePdfConfirmation(ret, server, helper);
			return ret;
		} catch (Exception e) {
			helper.error("Failed to produce confirmation PDF: " + e.getMessage(), e);
			throw new SectioningException("Failed to produce confirmation PDF: " + e.getMessage(), e);
		}
	}
	
	protected void generatePdfConfirmation(AdvisorCourseRequestSubmission ret, OnlineSectioningServer server, OnlineSectioningHelper helper) throws IOException, DocumentException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new AdvisorConfirmationPDF(getDetails()).generatePdfConfirmation(out);
		ret.setPdf(out.toByteArray());
	}

	@Override
	public String name() {
		return "advisor-submit";
	}
}
