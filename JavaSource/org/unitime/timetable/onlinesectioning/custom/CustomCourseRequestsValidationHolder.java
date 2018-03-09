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

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.onlinesectioning.updates.ReloadStudent;

/**
 * @author Tomas Muller
 */
public class CustomCourseRequestsValidationHolder {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	private static CourseRequestsValidationProvider sProvider = null;
	
	public synchronized static CourseRequestsValidationProvider getProvider() {
		if (sProvider == null) {
			try {
				sProvider = ((CourseRequestsValidationProvider)Class.forName(ApplicationProperty.CustomizationCourseRequestsValidation.value()).newInstance());
			} catch (Exception e) {
				throw new SectioningException(MSG.exceptionCourseRequestValidationProvider(e.getMessage()), e);
			}
		}
		return sProvider;
	}
	
	public synchronized static void release() {
		if (sProvider != null) {
			sProvider.dispose();
			sProvider = null;
		}
	}
	
	public synchronized static boolean hasProvider() {
		return sProvider != null || ApplicationProperty.CustomizationCourseRequestsValidation.value() != null;
	}
	
	public static class Check implements OnlineSectioningAction<CourseRequestInterface> {
		private static final long serialVersionUID = 1L;
		private CourseRequestInterface iRequest;
		
		public Check withRequest(CourseRequestInterface request) {
			iRequest = request;
			return this;
		}

		public CourseRequestInterface getRequest() { return iRequest; }
		
		@Override
		public CourseRequestInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
			CourseRequestInterface request = getRequest();
			
			if (CustomCourseRequestsValidationHolder.hasProvider())
				CustomCourseRequestsValidationHolder.getProvider().check(server, helper, request);
			
			return request;
		}

		@Override
		public String name() {
			return "check-overrides";
		}

	}
	
	public static class Update implements OnlineSectioningAction<Boolean> {
		private static final long serialVersionUID = 1L;
		private Collection<Long> iStudentIds = null;
		
		public Update forStudents(Long... studentIds) {
			iStudentIds = new ArrayList<Long>();
			for (Long studentId: studentIds)
				iStudentIds.add(studentId);
			return this;
		}
		
		public Update forStudents(Collection<Long> studentIds) {
			iStudentIds = studentIds;
			return this;
		}

		
		public Collection<Long> getStudentIds() { return iStudentIds; }
		
		@Override
		public Boolean execute(final OnlineSectioningServer server, final OnlineSectioningHelper helper) {
			if (!CustomCourseRequestsValidationHolder.hasProvider()) return false;
			final List<Long> reloadIds = new ArrayList<Long>();
			try {
				int nrThreads = server.getConfig().getPropertyInt("CourseRequestsValidation.NrThreads", 10);
				if (nrThreads <= 1 || getStudentIds().size() <= 1) {
					for (Long studentId: getStudentIds()) {
						if (updateStudent(server, helper, studentId)) reloadIds.add(studentId);
					}
				} else {
					List<Worker> workers = new ArrayList<Worker>();
					Iterator<Long> studentIds = getStudentIds().iterator();
					for (int i = 0; i < nrThreads; i++)
						workers.add(new Worker(i, studentIds) {
							@Override
							protected void process(Long studentId) {
								if (updateStudent(server, new OnlineSectioningHelper(helper), studentId)) {
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
		
		protected boolean updateStudent(OnlineSectioningServer server, OnlineSectioningHelper helper, Long studentId) {
			helper.beginTransaction();
			try {
				Student student = StudentDAO.getInstance().get(studentId, helper.getHibSession());
				boolean changed = false;
				
				if (student != null) {
					OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
					action.setStudent(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(studentId)
							.setExternalId(student.getExternalUniqueId())
							.setName(helper.getStudentNameFormat().format(student))
							.setType(OnlineSectioningLog.Entity.EntityType.STUDENT));
					long c0 = OnlineSectioningHelper.getCpuTime();
					try {
						if (CustomCourseRequestsValidationHolder.getProvider().updateStudent(server, helper, student, action)) {
							changed = true;
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
			return "update-overrides";
		}
		
	}
	
	public static class Validate implements OnlineSectioningAction<Boolean> {
		private static final long serialVersionUID = 1L;
		private Collection<Long> iStudentIds = null;
		
		public Validate forStudents(Long... studentIds) {
			iStudentIds = new ArrayList<Long>();
			for (Long studentId: studentIds)
				iStudentIds.add(studentId);
			return this;
		}
		
		public Validate forStudents(Collection<Long> studentIds) {
			iStudentIds = studentIds;
			return this;
		}

		
		public Collection<Long> getStudentIds() { return iStudentIds; }
		
		@Override
		public Boolean execute(final OnlineSectioningServer server, final OnlineSectioningHelper helper) {
			if (!CustomCourseRequestsValidationHolder.hasProvider()) return false;
			final List<Long> reloadIds = new ArrayList<Long>();
			try {
				int nrThreads = server.getConfig().getPropertyInt("CourseRequestsValidation.NrThreads", 10);
				if (nrThreads <= 1 || getStudentIds().size() <= 1) {
					for (Long studentId: getStudentIds()) {
						if (revalidateStudent(server, helper, studentId)) reloadIds.add(studentId);
					}
				} else {
					List<Worker> workers = new ArrayList<Worker>();
					Iterator<Long> studentIds = getStudentIds().iterator();
					for (int i = 0; i < nrThreads; i++)
						workers.add(new Worker(i, studentIds) {
							@Override
							protected void process(Long studentId) {
								if (revalidateStudent(server, new OnlineSectioningHelper(helper), studentId)) {
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
		
		protected boolean revalidateStudent(OnlineSectioningServer server, OnlineSectioningHelper helper, Long studentId) {
			helper.beginTransaction();
			try {
				Student student = StudentDAO.getInstance().get(studentId, helper.getHibSession());
				
				boolean changed = false;
				if (student != null) {
					OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
					action.setStudent(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(studentId)
							.setExternalId(student.getExternalUniqueId())
							.setName(helper.getStudentNameFormat().format(student))
							.setType(OnlineSectioningLog.Entity.EntityType.STUDENT));
					long c0 = OnlineSectioningHelper.getCpuTime();
					try {
						if (CustomCourseRequestsValidationHolder.getProvider().revalidateStudent(server, helper, student, action)) {
							changed = true;
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
			return "validate-overrides";
		}
	}
	
	protected static abstract class Worker extends Thread {
		private Iterator<Long> iStudentsIds;
		
		public Worker(int index, Iterator<Long> studentsIds) {
			setName("Validator-" + (1 + index));
			iStudentsIds = studentsIds;
		}
		
		protected abstract void process(Long studentId);
		
		@Override
	    public void run() {
			try {
				while (true) {
					Long studentId = null;
					synchronized (iStudentsIds) {
						if (!iStudentsIds.hasNext()) break;
						studentId = iStudentsIds.next();
					}
					process(studentId);
				}
			} finally {
				_RootDAO.closeCurrentThreadSessions();
			}
		}
	}
}
