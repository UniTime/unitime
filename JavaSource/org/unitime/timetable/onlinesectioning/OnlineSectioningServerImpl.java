/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.cpsolver.coursett.Constants;
import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.DistanceMetric;
import net.sf.cpsolver.ifs.util.JProf;
import net.sf.cpsolver.ifs.util.ToolBox;
import net.sf.cpsolver.studentsct.constraint.LinkedSections;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;
import net.sf.cpsolver.studentsct.reservation.Reservation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.solver.StudentSchedulingAssistantWeights;
import org.unitime.timetable.onlinesectioning.updates.CheckAllOfferingsAction;
import org.unitime.timetable.onlinesectioning.updates.ReloadAllData;
import org.unitime.timetable.onlinesectioning.updates.StudentEmail;

/**
 * @author Tomas Muller
 */
public class OnlineSectioningServerImpl implements OnlineSectioningServer {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Log iLog = LogFactory.getLog(OnlineSectioningServerImpl.class);
	private AcademicSessionInfo iAcademicSession = null;
	private Hashtable<Long, CourseInfo> iCourseForId = new Hashtable<Long, CourseInfo>();
	private Hashtable<String, TreeSet<CourseInfo>> iCourseForName = new Hashtable<String, TreeSet<CourseInfo>>();
	private TreeSet<CourseInfo> iCourses = new TreeSet<CourseInfo>();
	private DistanceMetric iDistanceMetric = null;
	private DataProperties iConfig = null;
	
	private Hashtable<Long, Course> iCourseTable = new Hashtable<Long, Course>();
	private Hashtable<Long, Section> iClassTable = new Hashtable<Long, Section>();
	private Hashtable<Long, Student> iStudentTable = new Hashtable<Long, Student>();
	private Hashtable<Long, Offering> iOfferingTable = new Hashtable<Long, Offering>();
	private Hashtable<Long, List<LinkedSections>> iLinkedSections = new Hashtable<Long, List<LinkedSections>>();
	
	private ReentrantReadWriteLock iLock = new ReentrantReadWriteLock();
	private MultiLock iMultiLock;
	private Map<Long, Lock> iOfferingLocks = new Hashtable<Long, Lock>();
	private AsyncExecutor iExecutor;
	private Queue<Runnable> iExecutorQueue = new LinkedList<Runnable>();
	private HashSet<CacheElement<Long>> iOfferingsToPersistExpectedSpaces = new HashSet<CacheElement<Long>>();
	
	OnlineSectioningServerImpl(Long sessionId, boolean waitTillStarted) throws SectioningException {
		iConfig = new ServerConfig();
		iDistanceMetric = new DistanceMetric(iConfig);
		TravelTime.populateTravelTimes(iDistanceMetric, sessionId);
		org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
		try {
			Session session = SessionDAO.getInstance().get(sessionId, hibSession);
			if (session == null)
				throw new SectioningException(MSG.exceptionSessionDoesNotExist(sessionId == null ? "null" : sessionId.toString()));
			iAcademicSession = new AcademicSessionInfo(session);
			iLog = LogFactory.getLog(OnlineSectioningServerImpl.class.getName() + ".server[" + iAcademicSession.toCompactString() + "]");
			iMultiLock = new MultiLock(iAcademicSession);
			iExecutor = new AsyncExecutor();
			iExecutor.start();
			final OnlineSectioningLog.Entity user = OnlineSectioningLog.Entity.newBuilder()
				.setExternalId(StudentClassEnrollment.SystemChange.SYSTEM.name())
				.setName(StudentClassEnrollment.SystemChange.SYSTEM.getName())
				.setType(OnlineSectioningLog.Entity.EntityType.OTHER).build();
			if (waitTillStarted) {
				try {
					execute(new ReloadAllData(), user);
				} catch (Throwable exception) {
					iLog.error("Failed to load server: " + exception.getMessage(), exception);
					throw exception;
				}
				if (iAcademicSession.isSectioningEnabled()) {
					try {
						execute(new CheckAllOfferingsAction(), user);
					} catch (Throwable exception) {
						iLog.error("Failed to check all offerings: " + exception.getMessage(), exception);
						throw exception;
					}
				}
			} else {
				execute(new ReloadAllData(), user, new ServerCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						if (iAcademicSession.isSectioningEnabled())
							execute(new CheckAllOfferingsAction(), user, new ServerCallback<Boolean>() {
								@Override
								public void onSuccess(Boolean result) {}
								@Override
								public void onFailure(Throwable exception) {
									iLog.error("Failed to check all offerings: " + exception.getMessage(), exception);
								}
							});
					}
					@Override
					public void onFailure(Throwable exception) {
						iLog.error("Failed to load server: " + exception.getMessage(), exception);
					}
				});
			}
		} catch (Throwable t) {
			if (t instanceof SectioningException) throw (SectioningException)t;
			throw new SectioningException(MSG.exceptionUnknown(t.getMessage()), t);
		} finally {
			hibSession.close();
		}
		iLog.info("Config: " + ToolBox.dict2string(iConfig, 2));
	}
	
	@Override
	public DistanceMetric getDistanceMetric() {
		return iDistanceMetric;
	}
	
	@Override
	public AcademicSessionInfo getAcademicSession() { return iAcademicSession; }

	@Override
	public CourseInfo getCourseInfo(String course) {
		iLock.readLock().lock();
		try {
			if (course.indexOf('-') >= 0) {
				String courseName = course.substring(0, course.indexOf('-')).trim();
				String title = course.substring(course.indexOf('-') + 1).trim();
				TreeSet<CourseInfo> infos = iCourseForName.get(courseName.toLowerCase());
				if (infos!= null && !infos.isEmpty())
					for (CourseInfo info: infos)
						if (title.equalsIgnoreCase(info.getTitle())) return info;
				return null;
			} else {
				TreeSet<CourseInfo> infos = iCourseForName.get(course.toLowerCase());
				if (infos!= null && !infos.isEmpty()) return infos.first();
				return null;
			}
		} finally {
			iLock.readLock().unlock();
		}
	}

	@Override
	public CourseInfo getCourseInfo(Long courseId) {
		iLock.readLock().lock();
		try {
			return iCourseForId.get(courseId);
		} finally {
			iLock.readLock().unlock();
		}
	}
	
	@Override
	public Student getStudent(Long studentId) {
		iLock.readLock().lock();
		try {
			return iStudentTable.get(studentId);
		} finally {
			iLock.readLock().unlock();
		}
	}
	
	@Override
	public Course getCourse(Long courseId) {
		iLock.readLock().lock();
		try {
			return iCourseTable.get(courseId);
		} finally {
			iLock.readLock().unlock();
		}
	}
	
	@Override
	public int distance(Section s1, Section s2) {
        if (s1.getPlacement()==null || s2.getPlacement()==null) return 0;
        TimeLocation t1 = s1.getTime();
        TimeLocation t2 = s2.getTime();
        if (!t1.shareDays(t2) || !t1.shareWeeks(t2)) return 0;
        int a1 = t1.getStartSlot(), a2 = t2.getStartSlot();
        if (getDistanceMetric().doComputeDistanceConflictsBetweenNonBTBClasses()) {
        	if (a1 + t1.getNrSlotsPerMeeting() <= a2) {
        		int dist = Placement.getDistanceInMinutes(getDistanceMetric(), s1.getPlacement(), s2.getPlacement());
        		if (dist > t1.getBreakTime() + Constants.SLOT_LENGTH_MIN * (a2 - a1 - t1.getLength()))
        			return dist;
        	}
        } else {
        	if (a1+t1.getNrSlotsPerMeeting()==a2)
        		return Placement.getDistanceInMinutes(getDistanceMetric(), s1.getPlacement(), s2.getPlacement());
        }
        /*
        else if (a2+t2.getNrSlotsPerMeeting()==a1) {
        	return Placement.getDistance(s1.getPlacement(), s2.getPlacement());
        }
        */
        return 0;
    }	
		
	@Override
	public Collection<CourseInfo> findCourses(String query, Integer limit, CourseInfoMatcher matcher) {
		iLock.readLock().lock();
		try {
			List<CourseInfo> ret = new ArrayList<CourseInfo>(limit == null ? 100 : limit);
			String queryInLowerCase = query.toLowerCase();
			for (CourseInfo c : iCourses) {
				if (c.matchCourseName(queryInLowerCase) && (matcher == null || matcher.match(c))) ret.add(c);
				if (limit != null && ret.size() == limit) return ret;
			}
			if (queryInLowerCase.length() > 2) {
				for (CourseInfo c : iCourses) {
					if (c.matchTitle(queryInLowerCase) && (matcher == null || matcher.match(c))) ret.add(c);
					if (limit != null && ret.size() == limit) return ret;
				}
			}
			return ret;
		} finally {
			iLock.readLock().unlock();
		}
	}
	
	@Override
	public Collection<CourseInfo> findCourses(CourseInfoMatcher matcher) {
		iLock.readLock().lock();
		try {
			List<CourseInfo> ret = new ArrayList<CourseInfo>();
			for (CourseInfo c : iCourses) {
				if (matcher.match(c)) ret.add(c);
			}
			return ret;
		} finally {
			iLock.readLock().unlock();
		}
	}
	
	@Override
	public URL getSectionUrl(Long courseId, Section section) {
		if (OnlineSectioningService.sSectionUrlProvider == null) return null;
		return OnlineSectioningService.sSectionUrlProvider.getSectionUrl(getAcademicSession(), courseId, section);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Section> getSections(CourseInfo courseInfo) {
		iLock.readLock().lock();
		try {
			ArrayList<Section> sections = new ArrayList<Section>();
			Course course = iCourseTable.get(courseInfo.getUniqueId());
			if (course == null) return sections;
			for (Iterator<Config> e=course.getOffering().getConfigs().iterator(); e.hasNext();) {
				Config cfg = e.next();
				for (Iterator<Subpart> f=cfg.getSubparts().iterator(); f.hasNext();) {
					Subpart subpart = f.next();
					for (Iterator<Section> g=subpart.getSections().iterator(); g.hasNext();) {
						Section section = g.next();
						sections.add(section);
					}
				}
			}
			return sections;
		} finally {
			iLock.readLock().unlock();
		}
	}
	
	public static class EnrollmentSectionComparator implements Comparator<Section> {
	    public boolean isParent(Section s1, Section s2) {
			Section p1 = s1.getParent();
			if (p1==null) return false;
			if (p1.equals(s2)) return true;
			return isParent(p1, s2);
		}

		public int compare(Section a, Section b) {
			if (isParent(a, b)) return 1;
	        if (isParent(b, a)) return -1;

	        int cmp = a.getSubpart().getInstructionalType().compareToIgnoreCase(b.getSubpart().getInstructionalType());
			if (cmp != 0) return cmp;
			
			return Double.compare(a.getId(), b.getId());
		}
	}
	
	@Override
	public Section getSection(Long classId) {
		iLock.readLock().lock();
		try {
			return iClassTable.get(classId);
		} finally {
			iLock.readLock().unlock();
		}
	}
	
	public static class DummyReservation extends Reservation {
		private int iPriority;
		private boolean iOver;
		private int iLimit;
		private boolean iApply;
		private boolean iMustUse;
		private boolean iAllowOverlap;
		
		public DummyReservation(long id, Offering offering, int priority, boolean over, int limit, boolean apply, boolean mustUse, boolean allowOverlap, boolean expired) {
			super(id, offering);
			iPriority = priority;
			iOver = over;
			iLimit = limit;
			iApply = apply;
			iMustUse = mustUse;
			iAllowOverlap = allowOverlap;
			setExpired(expired);
		}
		
		@Override
		public boolean canAssignOverLimit() {
			return iOver;
		}

		@Override
		public boolean mustBeUsed() {
			return iMustUse;
		}
		
		@Override
		public double getReservationLimit() {
			return iLimit;
		}

		@Override
		public int getPriority() {
			return iPriority;
		}

		@Override
		public boolean isApplicable(Student student) {
			return iApply;
		}

		@Override
		public boolean isAllowOverlap() {
			return iAllowOverlap;
		}
	}

	@Override
	public void remove(Student student) {
		iLock.writeLock().lock();
		try {
			Student s = iStudentTable.get(student.getId());
			if (s != null) {
				for (Request r: s.getRequests()) {
			        for (Request request : student.getRequests()) {
			            if (request instanceof CourseRequest) {
			                for (Course course: ((CourseRequest) request).getCourses())
			                    course.getRequests().remove(request);
			            }
					if (r.getAssignment() != null)
						r.unassign(0);
			        }
				}
				iStudentTable.remove(student.getId());
			}
		} finally {
			iLock.writeLock().unlock();
		}
	}

	@Override
	public void update(Student student) {
		iLock.writeLock().lock();
		try {
			iStudentTable.put(student.getId(), student);
			for (Request r: student.getRequests()) {
				if (r.getInitialAssignment() == null) {
					if (r.getAssignment() != null)
						r.unassign(0);
				} else {
					if (r.getAssignment() == null || !r.getAssignment().equals(r.getInitialAssignment()))
						r.assign(0, r.getInitialAssignment());
				}
			}

		} finally {
			iLock.writeLock().unlock();
		}
	}

	@Override
	public void remove(Offering offering) {
		iLock.writeLock().lock();
		try {
			for (Course course: offering.getCourses()) {
				CourseInfo ci = iCourseForId.get(course.getId());
				if (ci != null) {
					TreeSet<CourseInfo> courses = iCourseForName.get(ci.toString());
					if (courses != null) {
						courses.remove(ci);
						if (courses.isEmpty()) {
							iCourseForName.remove(ci.toString());
						} else if (courses.size() == 1) {
							for (CourseInfo x: courses)
								x.setHasUniqueName(true);
						}
					}
					iCourseForId.remove(ci.getUniqueId());
					iCourses.remove(ci);
				}
				iCourseTable.remove(course.getId());
			}
			iOfferingTable.remove(offering.getId());
			for (Config config: offering.getConfigs()) {
				for (Subpart subpart: config.getSubparts())
					for (Section section: subpart.getSections())
						iClassTable.remove(section.getId());
				for (Enrollment enrollment: new ArrayList<Enrollment>(config.getEnrollments()))
					enrollment.variable().unassign(0);
			}
		} finally {
			iLock.writeLock().unlock();
		}
	}

	@Override
	public void update(CourseInfo info) {
		iLock.writeLock().lock();
		try {
			CourseInfo old = iCourseForId.get(info.getUniqueId());
			iCourseForId.put(info.getUniqueId(), info);
			TreeSet<CourseInfo> courses = iCourseForName.get(info.toString());
			if (courses == null) {
				courses = new TreeSet<CourseInfo>();
				iCourseForName.put(info.toString(), courses);
			}
			if (old != null) {
				courses.remove(old);
				iCourses.remove(old);
			}
			courses.add(info);
			iCourses.add(info);
			if (courses.size() == 1) 
				for (CourseInfo x: courses) x.setHasUniqueName(true);
			else if (courses.size() > 1)
				for (CourseInfo x: courses) x.setHasUniqueName(false);
		} finally {
			iLock.writeLock().unlock();
		}
	}

	@Override
	public void update(Offering offering) {
		iLock.writeLock().lock();
		try {
			Offering old = iOfferingTable.get(offering.getId());
			if (old != null) remove(old);
			for (Course course: offering.getCourses())
				iCourseTable.put(course.getId(), course);
			iOfferingTable.put(offering.getId(), offering);
			for (Config config: offering.getConfigs())
				for (Subpart subpart: config.getSubparts())
					for (Section section: subpart.getSections())
						iClassTable.put(section.getId(), section);
		} finally {
			iLock.writeLock().unlock();
		}
	}

	@Override
	public Offering getOffering(Long offeringId) {
		iLock.readLock().lock();
		try {
			return iOfferingTable.get(offeringId);
		} finally {
			iLock.readLock().unlock();
		}
	}

	@Override
	public void clearAll() {
		iLock.writeLock().lock();
		try {
			iClassTable.clear();
			iStudentTable.clear();
			iOfferingTable.clear();
			iCourseTable.clear();
			iCourseForId.clear();
			iCourseForName.clear();
			iCourses.clear();	
		} finally {
			iLock.writeLock().unlock();
		}
	}
	
	@Override
    public void clearAllStudents() {
		iLock.writeLock().lock();
		try {
			for (Student student: iStudentTable.values()) {
				for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
					Request r = (Request)e.next();
					if (r.getAssignment() != null) r.unassign(0);
				}
			}
			iStudentTable.clear();
		} finally {
			iLock.writeLock().unlock();
		}
    }
	
	@Override
	public <E> E execute(OnlineSectioningAction<E> action, OnlineSectioningLog.Entity user) throws SectioningException {
		long c0 = OnlineSectioningHelper.getCpuTime();
		String cacheMode = getConfig().getProperty(action.name() + ".CacheMode", getConfig().getProperty("CacheMode"));
		OnlineSectioningHelper h = new OnlineSectioningHelper(user, cacheMode == null ? null : CacheMode.parse(cacheMode));
		try {
			h.addMessageHandler(new OnlineSectioningHelper.DefaultMessageLogger(LogFactory.getLog(OnlineSectioningServer.class.getName() + "." + action.name() + "[" + getAcademicSession().toCompactString() + "]")));
			h.addAction(action, getAcademicSession());
			E ret = action.execute(this, h);
			if (h.getAction() != null) {
				if (ret == null)
					h.getAction().setResult(OnlineSectioningLog.Action.ResultType.NULL);
				else if (ret instanceof Boolean)
					h.getAction().setResult((Boolean)ret ? OnlineSectioningLog.Action.ResultType.TRUE : OnlineSectioningLog.Action.ResultType.FALSE);
				else
					h.getAction().setResult(OnlineSectioningLog.Action.ResultType.SUCCESS);
			}
			return ret;
		} catch (Exception e) {
			if (e instanceof SectioningException) {
				if (e.getCause() == null) {
					h.info("Execution failed: " + e.getMessage());
				} else {
					h.warn("Execution failed: " + e.getMessage(), e.getCause());
				}
			} else {
				h.error("Execution failed: " + e.getMessage(), e);
			}
			if (h.getAction() != null) {
				h.getAction().setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
				if (e.getCause() != null && e instanceof SectioningException)
					h.getAction().addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.FATAL)
							.setText(e.getCause().getClass().getName() + ": " + e.getCause().getMessage()));
				else
					h.getAction().addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.FATAL)
							.setText(e.getMessage() == null ? "null" : e.getMessage()));
			}
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		} finally {
			if (h.getAction() != null) {
				h.getAction().setEndTime(System.currentTimeMillis()).setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
				if ((!h.getAction().hasStudent() || !h.getAction().getStudent().hasExternalId()) &&
					user != null && user.hasExternalId() &&
					user.hasType() && user.getType() == OnlineSectioningLog.Entity.EntityType.STUDENT) {
					if (h.getAction().hasStudent()) {
						h.getAction().getStudentBuilder().setExternalId(user.getExternalId());
					} else {
						h.getAction().setStudent(OnlineSectioningLog.Entity.newBuilder().setExternalId(user.getExternalId()));
					}
				}
			}
			iLog.debug("Executed: " + h.getLog() + " (" + h.getLog().toByteArray().length + " bytes)");
			OnlineSectioningLogger.getInstance().record(h.getLog());
		}
	}
	
	@Override
	public Lock readLock() {
		iLock.readLock().lock();
		return new Lock() {
			public void release() {
				iLock.readLock().unlock();
			}
		};
	}

	@Override
	public Lock writeLock() {
		iLock.writeLock().lock();
		return new Lock() {
			public void release() {
				iLock.writeLock().unlock();
			}
		};
	}

	@Override
	public Lock lockAll() {
		iLock.writeLock().lock();
		return new Lock() {
			public void release() {
				iLock.writeLock().unlock();
			}
		};
	}
	
	@Override
	public Lock lockStudent(Long studentId, Collection<Long> offeringIds, boolean excludeLockedOfferings) {
		Set<Long> ids = new HashSet<Long>();
		iLock.readLock().lock();
		try {
			ids.add(-studentId);
			if (offeringIds != null)
				for (Long offeringId: offeringIds)
					if (!excludeLockedOfferings || !iOfferingLocks.containsKey(offeringId))
						ids.add(offeringId);
			
			Student student = iStudentTable.get(studentId);
			
			if (student != null)
				for (Request r: student.getRequests()) {
					Offering o = (r.getAssignment() == null ? null : r.getAssignment().getOffering());
					if (o != null && (!excludeLockedOfferings || !iOfferingLocks.containsKey(o.getId()))) ids.add(o.getId());
				}
		} finally {
			iLock.readLock().unlock();
		}
		return iMultiLock.lock(ids);
	}
	
	@Override
	public Lock lockOffering(Long offeringId, Collection<Long> studentIds, boolean excludeLockedOffering) {
		Set<Long> ids = new HashSet<Long>();
		iLock.readLock().lock();
		try {
			if (!excludeLockedOffering || !iOfferingLocks.containsKey(offeringId))
				ids.add(offeringId);
			
			if (studentIds != null)
				for (Long studentId: studentIds)
				ids.add(-studentId);
			
			Offering offering = iOfferingTable.get(offeringId);
			
			if (offering != null)
				for (Course course: offering.getCourses())
					for (CourseRequest request: course.getRequests())
						ids.add(-request.getStudent().getId());
		} finally {
			iLock.readLock().unlock();
		}
		return iMultiLock.lock(ids);
	}
	
	@Override
	public Lock lockClass(Long classId, Collection<Long> studentIds) {
		Set<Long> ids = new HashSet<Long>();
		iLock.readLock().lock();
		try {
			if (studentIds != null)
				for (Long studentId: studentIds)
				ids.add(-studentId);
			
			Section section = iClassTable.get(classId);
			if (section != null) {
				for (Enrollment enrollment: section.getEnrollments())
					ids.add(-enrollment.getStudent().getId());
				ids.add(section.getSubpart().getConfig().getOffering().getId());
			}
		} finally {
			iLock.readLock().unlock();
		}
		return iMultiLock.lock(ids);
	}
	
	private Long getOfferingIdFromCourseName(String courseName) {
		if (courseName == null) return null;
		CourseInfo c = getCourseInfo(courseName);
		if (c == null) return null;
		Course course = iCourseTable.get(c.getUniqueId());
		return (course == null ? null : course.getOffering().getId());
	}
	
	public Lock lockRequest(CourseRequestInterface request) {
		Set<Long> ids = new HashSet<Long>();
		iLock.readLock().lock();
		try {
			if (request.getStudentId() != null)
				ids.add(-request.getStudentId());
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse()) {
					Long id = getOfferingIdFromCourseName(r.getRequestedCourse());
					if (id != null) ids.add(id);
				}
				if (r.hasFirstAlternative()) {
					Long id = getOfferingIdFromCourseName(r.getFirstAlternative());
					if (id != null) ids.add(id);
				}
				if (r.hasSecondAlternative()) {
					Long id = getOfferingIdFromCourseName(r.getSecondAlternative());
					if (id != null) ids.add(id);
				}
			}
			for (CourseRequestInterface.Request r: request.getAlternatives()) {
				if (r.hasRequestedCourse()) {
					Long id = getOfferingIdFromCourseName(r.getRequestedCourse());
					if (id != null) ids.add(id);
				}
				if (r.hasFirstAlternative()) {
					Long id = getOfferingIdFromCourseName(r.getFirstAlternative());
					if (id != null) ids.add(id);
				}
				if (r.hasSecondAlternative()) {
					Long id = getOfferingIdFromCourseName(r.getSecondAlternative());
					if (id != null) ids.add(id);
				}
			}
		} finally {
			iLock.readLock().unlock();
		}
		return iMultiLock.lock(ids);
	}
	
	public void notifyStudentChanged(Long studentId, List<Request> oldRequests, List<Request> newRequests, OnlineSectioningLog.Entity user) {
		Student student = getStudent(studentId);
		if (student != null) {
			String message = "Student " + student.getId() + " changed.";
			if (oldRequests != null) {
				message += "\n  Previous schedule:";
				for (Request r: oldRequests) {
					message += "\n    " + r.getName() + (r instanceof FreeTimeRequest || r.getInitialAssignment() != null ? "" : " NOT ASSIGNED");
					if (r instanceof CourseRequest && r.getInitialAssignment() != null) {
						for (Section s: r.getInitialAssignment().getSections()) {
							message += "\n      " + s.getSubpart().getName() + " " + s.getName(r.getInitialAssignment().getCourse().getId())
								+ (s.getTime() == null ? "" : " " + s.getTime().getLongName())
								+ (s.getNrRooms() == 0 ? "" : " " + s.getPlacement().getRoomName(", "));
						}
					}
				}
			}
			if (newRequests != null) {
				message += "\n  New schedule:";
				for (Request r: newRequests) {
					message += "\n    " + r.getName() + (r instanceof FreeTimeRequest || r.getInitialAssignment() != null ? "" : " NOT ASSIGNED");
					if (r instanceof CourseRequest && r.getInitialAssignment() != null) {
						for (Section s: r.getInitialAssignment().getSections()) {
							message += "\n      " + s.getSubpart().getName() + " " + s.getName(r.getInitialAssignment().getCourse().getId())
								+ (s.getTime() == null ? "" : " " + s.getTime().getLongName())
								+ (s.getNrRooms() == 0 ? "" : " " + s.getPlacement().getRoomName(", "));
						}
					}
				}
			}
			iLog.info(message);
			if (getAcademicSession().isSectioningEnabled() && "true".equals(ApplicationProperties.getProperty("unitime.enrollment.email", "true"))) {
				execute(new StudentEmail(studentId, oldRequests, newRequests), user, new ServerCallback<Boolean>() {
					@Override
					public void onFailure(Throwable exception) {
						iLog.error("Failed to notify student: " + exception.getMessage(), exception);
					}
					@Override
					public void onSuccess(Boolean result) {
					}
				});
			}
		}
	}
	
	@Override
	public void notifyStudentChanged(Long studentId, Request request, Enrollment oldEnrollment, OnlineSectioningLog.Entity user) {
		Student student = getStudent(studentId);
		if (student != null) {
			String message = "Student " + student.getId() + " changed.";
			if (oldEnrollment != null) {
				message += "\n  Previous assignment:";
				message += "\n    " + request.getName() + (request instanceof FreeTimeRequest || oldEnrollment != null ? "" : " NOT ASSIGNED");
				if (request instanceof CourseRequest && oldEnrollment != null) {
					for (Section s: oldEnrollment.getSections()) {
						message += "\n      " + s.getSubpart().getName() + " " + s.getName(oldEnrollment.getCourse().getId())
							+ (s.getTime() == null ? "" : " " + s.getTime().getLongName())
							+ (s.getNrRooms() == 0 ? "" : " " + s.getPlacement().getRoomName(", "));
					}
				}
			}
			message += "\n  New schedule:";
			message += "\n    " + request.getName() + (request instanceof FreeTimeRequest || request.getInitialAssignment() != null ? "" : " NOT ASSIGNED");
			if (request instanceof CourseRequest && request.getInitialAssignment() != null) {
				for (Section s: request.getInitialAssignment().getSections()) {
					message += "\n      " + s.getSubpart().getName() + " " + s.getName(request.getInitialAssignment().getCourse().getId())
						+ (s.getTime() == null ? "" : " " + s.getTime().getLongName())
						+ (s.getNrRooms() == 0 ? "" : " " + s.getPlacement().getRoomName(", "));
				}
			}
			iLog.info(message);
			if (getAcademicSession().isSectioningEnabled() && "true".equals(ApplicationProperties.getProperty("unitime.enrollment.email", "true"))) {
				if (oldEnrollment == null) {
					oldEnrollment = new Enrollment(request, 0, (request instanceof CourseRequest ? ((CourseRequest)request).getCourses().get(0) : null), null, null, null);
				}
				execute(new StudentEmail(studentId, oldEnrollment, student.getRequests()), user, new ServerCallback<Boolean>() {
					@Override
					public void onFailure(Throwable exception) {
						iLog.error("Failed to notify student: " + exception.getMessage(), exception);
					}
					@Override
					public void onSuccess(Boolean result) {
					}
				});
			}
		}
	}

	
	@Override
	public boolean isOfferingLocked(Long offeringId) {
		synchronized (iOfferingLocks) {
			return iOfferingLocks.containsKey(offeringId);
		}
	}

	@Override
	public void lockOffering(Long offeringId) {
		synchronized (iOfferingLocks) {
			if (iOfferingLocks.containsKey(offeringId)) return;
		}
		Lock lock = iMultiLock.lock(offeringId);
		synchronized (iOfferingLocks) {
			if (iOfferingLocks.containsKey(offeringId))
				lock.release();
			else
				iOfferingLocks.put(offeringId, lock);
		}
	}

	@Override
	public void unlockOffering(Long offeringId) {
		synchronized (iOfferingLocks) {
			Lock lock = iOfferingLocks.remove(offeringId);
			if (lock != null)
				lock.release();
		}
	}
	
	@Override
	public Collection<Long> getLockedOfferings() {
		synchronized (iOfferingLocks) {
			return new ArrayList<Long>(iOfferingLocks.keySet());
		}
	}
	
	@Override
	public void releaseAllOfferingLocks() {
		synchronized (iOfferingLocks) {
			for (Lock lock: iOfferingLocks.values())
				lock.release();
			iOfferingLocks.clear();
		}
	}

	@Override
	public <E> void execute(final OnlineSectioningAction<E> action, final OnlineSectioningLog.Entity user, final ServerCallback<E> callback) throws SectioningException {
		final String locale = Localization.getLocale();
		synchronized (iExecutorQueue) {
			iExecutorQueue.offer(new Runnable() {
				@Override
				public void run() {
					Localization.setLocale(locale);
					try {
						callback.onSuccess(execute(action, user));
					} catch (Throwable t) {
						callback.onFailure(t);
					}
				}
				
				@Override
				public String toString() {
					return action.name();
				}
			});
			iExecutorQueue.notify();
		}
	}
	
	public class AsyncExecutor extends Thread {
		private boolean iStop = false;
		
		public AsyncExecutor() {
			setName("AsyncExecutor[" + getAcademicSession() + "]");
			setDaemon(true);
		}
		
		public void run() {
			try {
				ApplicationProperties.setSessionId(getAcademicSession().getUniqueId());
				Runnable job;
				while (!iStop) {
					synchronized (iExecutorQueue) {
						job = iExecutorQueue.poll();
						if (job == null) {
							try {
								iLog.info("Executor is waiting for a new job...");
								iExecutorQueue.wait();
							} catch (InterruptedException e) {}
							continue;
						}		
					}
					job.run();
					if (_RootDAO.closeCurrentThreadSessions())
						iLog.warn("Job " + job + " did not close current-thread hibernate session.");
				}
				iLog.info("Executor stopped.");
			} finally {
				ApplicationProperties.setSessionId(null);
				Localization.removeLocale();
			}
		}
		
	}
	
	@Override
	public void unload() {
		if (iExecutor != null) {
			iExecutor.iStop = true;
			synchronized (iExecutorQueue) {
				iExecutorQueue.notify();
			}
		}
	}

	@Override
	public DataProperties getConfig() {
		return iConfig;
	}

	@Override
	public void addLinkedSections(LinkedSections link) {
		iLock.writeLock().lock();
		try {
			for (Offering offering: link.getOfferings()) {
				List<LinkedSections> list = iLinkedSections.get(offering.getId());
				if (list == null) {
					list = new ArrayList<LinkedSections>();
					iLinkedSections.put(offering.getId(), list);
				}
				list.add(link);
			}
		} finally {
			iLock.writeLock().unlock();
		}
	}
	
	@Override
	public Collection<LinkedSections> getLinkedSections(Long offeringId) {
		iLock.readLock().lock();
		try {
			return iLinkedSections.get(offeringId);

		} finally {
			iLock.readLock().unlock();
		}		
	}
	
	@Override
	public void removeLinkedSections(Long offeringId) {
		iLock.writeLock().lock();
		try {
			List<LinkedSections> list = iLinkedSections.get(offeringId);
			if (list != null && !list.isEmpty())
				for (LinkedSections link: new ArrayList<LinkedSections>(list)) {
					for (Offering offering: link.getOfferings()) {
						List<LinkedSections> l = iLinkedSections.get(offering.getId());
						if (l != null) l.remove(link);
					}
				}
		} finally {
			iLock.writeLock().unlock();
		}
	}

	@Override
	public void persistExpectedSpaces(Long offeringId) {
		synchronized(iOfferingsToPersistExpectedSpaces) {
			iOfferingsToPersistExpectedSpaces.add(new CacheElement<Long>(offeringId));
		}
	}
	
	@Override
	public List<Long> getOfferingsToPersistExpectedSpaces(long minimalAge) {
		List<Long> offeringIds = new ArrayList<Long>();
		long current = JProf.currentTimeMillis();
		synchronized (iOfferingsToPersistExpectedSpaces) {
			for (Iterator<CacheElement<Long>> i = iOfferingsToPersistExpectedSpaces.iterator(); i.hasNext(); ) {
				CacheElement<Long> c = i.next();
				if (current - c.created() >= minimalAge) {
					offeringIds.add(c.element());
					i.remove();
				}
			}
		}
		return offeringIds;
	}
	
	@Override
	public boolean needPersistExpectedSpaces(Long offeringId) {
		synchronized(iOfferingsToPersistExpectedSpaces) {
			return iOfferingsToPersistExpectedSpaces.remove(offeringId);
		}
	}

	@Override
	public Collection<Student> findStudents(StudentMatcher matcher) {
		iLock.readLock().lock();
		try {
			List<Student> ret = new ArrayList<Student>();
			for (Student s: iStudentTable.values())
				if (matcher.match(s)) ret.add(s);
			return ret;
		} finally {
			iLock.readLock().unlock();
		}
	}

	@Override
	public boolean checkDeadline(Section section, Deadline type) {
		if (!"true".equals(ApplicationProperties.getProperty("unitime.enrollment.deadline", "true"))) return true;
		
		CourseInfo info = getCourseInfo(section.getSubpart().getConfig().getOffering().getCourses().get(0).getId());
		int deadline = 0;
		switch (type) {
		case NEW:
			if (info != null && info.getLastWeekToEnroll() != null)
				deadline = info.getLastWeekToEnroll();
			else
				deadline = getAcademicSession().getLastWeekToEnroll();
			break;
		case CHANGE:
			if (info != null && info.getLastWeekToChange() != null)
				deadline = info.getLastWeekToChange();
			else
				deadline = getAcademicSession().getLastWeekToChange();
			break;
		case DROP:
			if (info != null && info.getLastWeekToDrop() != null)
				deadline = info.getLastWeekToDrop();
			else
				deadline = getAcademicSession().getLastWeekToDrop();
			break;
		}
		long start = getAcademicSession().getSessionBeginDate().getTime();
		long now = new Date().getTime();
		int week = 0;
		if (now >= start) {
			week = (int)((now - start) / (1000 * 60 * 60 * 24 * 7)) + 1;
		} else {
			week = -(int)((start - now) / (1000 * 60 * 60 * 24 * 7));
		}

		if (section.getTime() == null)
			return week <= deadline; // no time, just compare week and the deadline
		
		int offset = 0;
		long time = getAcademicSession().getDatePatternFirstDate().getTime() + (long) section.getTime().getWeekCode().nextSetBit(0) * (1000l * 60l * 60l * 24l);
		if (time >= start) {
			offset = (int)((time - start) / (1000 * 60 * 60 * 24 * 7));
		} else {
			offset = -(int)((start - time) / (1000 * 60 * 60 * 24 * 7)) - 1;
		}
		
		return week <= deadline + offset;
	}
	
	private static class ServerConfig extends DataProperties {
		private static final long serialVersionUID = 1L;

		private ServerConfig() {
			super();
			setProperty("Neighbour.BranchAndBoundTimeout", "1000");
			setProperty("Suggestions.Timeout", "1000");
			setProperty("Extensions.Classes", DistanceConflict.class.getName() + ";" + TimeOverlapsCounter.class.getName());
			setProperty("StudentWeights.Class", StudentSchedulingAssistantWeights.class.getName());
			setProperty("StudentWeights.PriorityWeighting", "true");
			setProperty("StudentWeights.LeftoverSpread", "true");
			setProperty("StudentWeights.BalancingFactor", "0.0");
			setProperty("StudentWeights.MultiCriteria", "true");
			setProperty("Reservation.CanAssignOverTheLimit", "true");
			setProperty("General.SaveDefaultProperties", "false");
			org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
			try {
				for (SolverParameterDef def: (List<SolverParameterDef>)hibSession.createQuery(
						"from SolverParameterDef x where x.group.type = :type and x.default is not null")
						.setInteger("type", SolverParameterGroup.sTypeStudent).list()) {
					setProperty(def.getName(), def.getDefault());
				}
				SolverPredefinedSetting settings = (SolverPredefinedSetting)hibSession.createQuery(
						"from SolverPredefinedSetting x where x.name = :reference")
						.setString("reference", "StudentSct.Online").setMaxResults(1).uniqueResult();
				if (settings != null) {
					for (SolverParameter param: settings.getParameters()) {
						if (!param.getDefinition().isVisible().booleanValue()) continue;
						if (param.getDefinition().getGroup().getType() != SolverParameterGroup.sTypeStudent) continue;
						setProperty(param.getDefinition().getName(), param.getValue());
					}
					setProperty("General.SettingsId", settings.getUniqueId().toString());
				}
				if (getProperty("Distances.Ellipsoid") == null || "DEFAULT".equals(getProperty("Distances.Ellipsoid")))
					setProperty("Distances.Ellipsoid", ApplicationProperties.getProperty("unitime.distance.ellipsoid", DistanceMetric.Ellipsoid.LEGACY.name()));
				if ("Priority".equals(getProperty("StudentWeights.Mode")))
					setProperty("StudentWeights.PriorityWeighting", "true");
				else if ("Equal".equals(getProperty("StudentWeights.Mode")))
					setProperty("StudentWeights.PriorityWeighting", "false");
			} finally {
				hibSession.close();
			}
		}
		
		@Override
		public String getProperty(String key) {
			String value = ApplicationProperties.getProperty("unitime.sectioning.config." + key);
			return value == null ? super.getProperty(key) : value;
		}
		
		@Override
		public String getProperty(String key, String defaultValue) {
			String value = ApplicationProperties.getProperty("unitime.sectioning.config." + key);
			return value == null ? super.getProperty(key, defaultValue) : value;
		}
	}
	
}
