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
package org.unitime.timetable.onlinesectioning.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider.CriticalCourses;
import org.unitime.timetable.onlinesectioning.model.XAdvisorRequest;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.onlinesectioning.updates.ReloadStudent;

/**
 * @author Tomas Muller
 */
public class CustomCriticalCoursesHolder {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	
	public static CriticalCoursesProvider getProvider() {
		return Customization.CriticalCoursesProvider.getProvider();
	}
	
	public static void release() {
		Customization.CriticalCoursesProvider.release();
	}
	
	public static boolean hasProvider() {
		return Customization.CriticalCoursesProvider.hasProvider();
	}
	
	public static class CheckCriticalCourses implements OnlineSectioningAction<Boolean> {
		private static final long serialVersionUID = 1L;
		private Collection<Long> iStudentIds = null;
		
		public CheckCriticalCourses forStudents(Long... studentIds) {
			iStudentIds = new ArrayList<Long>();
			for (Long studentId: studentIds)
				iStudentIds.add(studentId);
			return this;
		}
		
		public CheckCriticalCourses forStudents(Collection<Long> studentIds) {
			iStudentIds = studentIds;
			return this;
		}

		
		public Collection<Long> getStudentIds() { return iStudentIds; }
		
		@Override
		public Boolean execute(final OnlineSectioningServer server, final OnlineSectioningHelper helper) {
			if (!CustomCriticalCoursesHolder.hasProvider()) return false;
			final List<Long> reloadIds = new ArrayList<Long>();
			try {
				int nrThreads = server.getConfig().getPropertyInt("CheckCriticalCourses.NrThreads", 10);
				if (nrThreads <= 1 || getStudentIds().size() <= 1) {
					for (Long studentId: getStudentIds()) {
						if (recheckStudent(server, helper, studentId)) reloadIds.add(studentId);
					}
				} else {
					List<Worker> workers = new ArrayList<Worker>();
					Iterator<Long> studentIds = getStudentIds().iterator();
					for (int i = 0; i < nrThreads; i++)
						workers.add(new Worker(i, server.getAcademicSession().getUniqueId(), studentIds) {
							@Override
							protected void process(Long studentId) {
								if (recheckStudent(server, new OnlineSectioningHelper(helper), studentId)) {
									synchronized (reloadIds) {
										reloadIds.add(studentId);
									}
								}
							}
						});
					for (Worker worker: workers) worker.start();
					for (Worker worker: workers) {
						try {
							worker.join();
						} catch (InterruptedException e) {
						}
					}
				}
			} finally {
				if (!reloadIds.isEmpty() && !(server instanceof DatabaseServer))
					server.execute(server.createAction(ReloadStudent.class).forStudents(reloadIds), helper.getUser());
			}
			return !reloadIds.isEmpty();
		}
		
		protected int isCritical(CourseDemand cd, CriticalCourses critical) {
			if (critical == null || cd.isAlternative()) return 0;
			for (org.unitime.timetable.model.CourseRequest cr: cd.getCourseRequests()) {
				if (cr.getOrder() == 0 && critical.isCritical(cr.getCourseOffering()) > 0) return critical.isCritical(cr.getCourseOffering());
			}
			return 0;
		}
		
		protected int isCritical(XCourseRequest cr, CriticalCourses critical) {
			if (critical == null || cr.isAlternative()) return 0;
			for (XCourseId courseId: cr.getCourseIds()) {
				return critical.isCritical(courseId);
			}
			return 0;
		}
		
		protected int isCritical(XAdvisorRequest cr, CriticalCourses critical) {
			if (critical == null) return 0;
			if (critical instanceof CriticalCoursesProvider.AdvisorCriticalCourses) {
				return ((CriticalCoursesProvider.AdvisorCriticalCourses)critical).isCritical(cr);
			}
			if (cr.isSubstitute() || cr.getCourseId() == null) return 0;
			return critical.isCritical(cr.getCourseId());
		}
		
		protected boolean recheckStudent(OnlineSectioningServer server, OnlineSectioningHelper helper, Long studentId) {
			helper.beginTransaction();
			try {
				XStudent student = server.getStudent(studentId);
				boolean changed = false;
				if (student != null) {
					OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
					action.setStudent(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(studentId)
							.setExternalId(student.getExternalId())
							.setName(student.getName())
							.setType(OnlineSectioningLog.Entity.EntityType.STUDENT));
					long c0 = OnlineSectioningHelper.getCpuTime();
					try {
						CriticalCourses critical = CustomCriticalCoursesHolder.getProvider().getCriticalCourses(server, helper, student, action);
						boolean courseDemandsNeedChange = false;
						for (XRequest r: student.getRequests()) {
							if (r instanceof XCourseRequest) {
								XCourseRequest cr = (XCourseRequest)r;
								if (cr.getCritical() != isCritical(cr, critical)) {
									courseDemandsNeedChange = true;
									break;
								}
							}
						}
						if (courseDemandsNeedChange) {
							Student dbStudent = StudentDAO.getInstance().get(studentId, helper.getHibSession());
							for (CourseDemand cd: dbStudent.getCourseDemands()) {
								int crit = isCritical(cd, critical);
								if (cd.getCritical() == null || cd.getCritical().intValue() != crit) {
									cd.setCritical(crit); helper.getHibSession().merge(cd); changed = true;
								}
							}
						}
						boolean advisorRecommendationsNeedChange = false;
						if (student.getAdvisorRequests() != null) {
							for (XAdvisorRequest acr: student.getAdvisorRequests()) {
								if (acr.getCritical() != isCritical(acr, critical)) {
									advisorRecommendationsNeedChange = true;
									break;
								}
							}
						}
						if (advisorRecommendationsNeedChange) {
							Student dbStudent = StudentDAO.getInstance().get(studentId, helper.getHibSession());
							if (dbStudent.getAdvisorCourseRequests() != null)
								for (AdvisorCourseRequest acr: dbStudent.getAdvisorCourseRequests()) {
									int crit = acr.isCritical(critical);
									if (acr.getCritical() == null || acr.getCritical().intValue() != crit) {
										acr.setCritical(crit); helper.getHibSession().merge(acr);
									}
								}
						}
						if (changed) {
			        		action.setResult(OnlineSectioningLog.Action.ResultType.TRUE);
			        	} else {
			        		action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
			        	}
					} catch (SectioningException e) {
						action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
						if (e.getCause() != null) {
							action.addMessage(OnlineSectioningLog.Message.newBuilder()
									.setLevel(OnlineSectioningLog.Message.Level.FATAL)
									.setText(e.getCause().getClass().getName() + ": " + e.getCause().getMessage()));
						} else {
							action.addMessage(OnlineSectioningLog.Message.newBuilder()
									.setLevel(OnlineSectioningLog.Message.Level.FATAL)
									.setText(e.getMessage() == null ? "null" : e.getMessage()));
						}
					} finally {
						action.setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
						action.setEndTime(System.currentTimeMillis());
					}
				}
				helper.commitTransaction();
				return changed;
			} catch (Exception e) {
				helper.rollbackTransaction();
				if (e instanceof SectioningException)
					throw (SectioningException)e;
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			}
		}

		@Override
		public String name() {
			return "critical-courses";
		}
	}
	
	protected static abstract class Worker extends Thread {
		private Iterator<Long> iStudentsIds;
		private Long iSessionId;
		
		public Worker(int index, Long sessionId, Iterator<Long> studentsIds) {
			setName("CriticalCourses-" + (1 + index));
			iSessionId = sessionId;
			iStudentsIds = studentsIds;
		}
		
		protected abstract void process(Long studentId);
		
		@Override
	    public void run() {
			try {
				ApplicationProperties.setSessionId(iSessionId);
				while (true) {
					Long studentId = null;
					synchronized (iStudentsIds) {
						if (!iStudentsIds.hasNext()) break;
						studentId = iStudentsIds.next();
					}
					process(studentId);
				}
			} finally {
				ApplicationProperties.setSessionId(null);
				HibernateUtil.closeCurrentThreadSessions();
			}
		}
	}

}
