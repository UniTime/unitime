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
package org.unitime.timetable.security.context;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.ManagerSettings;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.model.dao.UserDataDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.authority.RoleAuthority;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.HasRights;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LoginManager;

/**
 * @author Tomas Muller
 */
public class UniTimeUserContext extends AbstractUserContext {
	private static final long serialVersionUID = 1L;
	
	private String iId, iPassword, iName, iLogin, iEmail;
	
	public UniTimeUserContext(String userId, String login, String name, String password) {
		this(userId, login, name, password, null);
	}

	protected UniTimeUserContext(String userId, String login, String name, String password, Long sessionId) {
		iLogin = login; iPassword = password; iId = userId; iName = name;
		org.hibernate.Session hibSession = TimetableManagerDAO.getInstance().createNewSession();
		try {
			for (UserData data: (List<UserData>)hibSession.createQuery(
					"from UserData where externalUniqueId = :id")
					.setString("id", userId).list()) {
				getProperties().put(data.getName(), data.getValue());
			}
			for (Settings setting: (List<Settings>)hibSession.createQuery("from Settings").list()) {
				if (setting.getDefaultValue() != null)
					getProperties().put(setting.getKey(), setting.getDefaultValue());
			}
			for (ManagerSettings setting: (List<ManagerSettings>)hibSession.createQuery(
					"from ManagerSettings where manager.externalUniqueId = :id").setString("id", userId).list()) {
				if (setting.getValue() != null)
					getProperties().put(setting.getKey().getKey(), setting.getValue());
			}

			if (sessionId == null && ApplicationProperty.KeepLastUsedAcademicSession.isTrue()) {
				String lastSessionId = getProperty(UserProperty.LastAcademicSession);
				if (lastSessionId != null) sessionId = Long.valueOf(lastSessionId);
			}
			
			TimetableManager manager = (TimetableManager)hibSession.createQuery(
					"from TimetableManager where externalUniqueId = :id")
					.setString("id", userId).setMaxResults(1).uniqueResult();
			if (manager != null) {
				iName = manager.getName();
				iEmail = manager.getEmailAddress();
				Roles primary = null;
				
				TreeSet<Session> primarySessions = null;
				
				for (ManagerRole role: manager.getManagerRoles()) {
					if (!role.getRole().isEnabled()) continue;
					TreeSet<Session> sessions = new TreeSet<Session>();
					if (role.getRole().hasRight(Right.SessionIndependent) || (sessions.isEmpty() && role.getRole().hasRight(Right.SessionIndependentIfNoSessionGiven)))
						sessions.addAll(SessionDAO.getInstance().findAll(hibSession));
					else
						for (Department department: manager.getDepartments())
							sessions.add(department.getSession());
					
					if (role.isPrimary() && primary == null) {
						primary = role.getRole();
						primarySessions = sessions;
					}
					
					for (Session session: sessions) {
						if (session.getStatusType() == null || session.getStatusType().isTestSession()) {
							if (!role.getRole().hasRight(Right.AllowTestSessions)) continue;
						}
						
						UserAuthority authority = new RoleAuthority(manager.getUniqueId(), role.getRole());
						authority.addQualifier(session);
						authority.addQualifier(manager);
						for (Department department: manager.getDepartments())
							if (department.getSession().equals(session))
								authority.addQualifier(department);
						for (SolverGroup group: manager.getSolverGroups())
							for (Department department: group.getDepartments())
								if (department.getSession().equals(session)) {
									authority.addQualifier(group); break;
								}
						addAuthority(authority);
					}
				}
				
				if (sessionId == null && primary != null) {
					Session session = defaultSession(primarySessions, primary, getProperty(UserProperty.PrimaryCampus.key()));
					if (session != null) sessionId = session.getUniqueId();
				}
				if (sessionId != null && primary != null) {
					List<? extends UserAuthority> authorities = getAuthorities(primary.getReference(), new SimpleQualifier("Session", sessionId));
					if (!authorities.isEmpty())
						setCurrentAuthority(authorities.get(0));
				}
			}
			
			TreeSet<Session> sessions = new TreeSet<Session>();
			
			for (Advisor advisor: (List<Advisor>)hibSession.createQuery(
					"from Advisor where externalUniqueId = :id")
					.setString("id", userId).list()) {
				if (advisor.getRole() == null || !advisor.getRole().isEnabled()) continue;
				if (iName == null && advisor.hasName()) iName = advisor.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle);
				if (iEmail == null) iEmail = advisor.getEmail();
				RoleAuthority authority = new RoleAuthority(advisor.getUniqueId(), advisor.getRole());
				authority.addQualifier(advisor.getSession());
				addAuthority(authority);
				sessions.add(advisor.getSession());
			}

			Roles instructorRole = Roles.getRole(Roles.ROLE_INSTRUCTOR, hibSession);
			if (instructorRole != null && instructorRole.isEnabled()) {
				for (DepartmentalInstructor instructor: (List<DepartmentalInstructor>)hibSession.createQuery(
						"from DepartmentalInstructor where externalUniqueId = :id")
						.setString("id", userId).list()) {
					if (iName == null) iName = instructor.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle);
					if (iEmail == null) iEmail = instructor.getEmail();
					List<? extends UserAuthority> authorities = getAuthorities(Roles.ROLE_INSTRUCTOR, instructor.getDepartment().getSession());
					UserAuthority authority = (authorities.isEmpty() ? null : authorities.get(0));
					if (authority == null) {
						authority = new RoleAuthority(instructor.getUniqueId(), instructorRole);
						authority.addQualifier(instructor.getDepartment().getSession());
						addAuthority(authority);
						sessions.add(instructor.getDepartment().getSession());
					}
					authority.addQualifier(instructor.getDepartment());
					if (instructor.getRole() != null) {
						List<? extends UserAuthority> instrRoleAuthorities = getAuthorities(instructor.getRole().getReference(), instructor.getDepartment().getSession());
						UserAuthority instrRoleAuthority = (instrRoleAuthorities.isEmpty() ? null : instrRoleAuthorities.get(0));
						if (instrRoleAuthority == null) {
							instrRoleAuthority = new RoleAuthority(instructor.getUniqueId(), instructor.getRole());
							instrRoleAuthority.addQualifier(instructor.getDepartment().getSession());
							addAuthority(instrRoleAuthority);
						}
						instrRoleAuthority.addQualifier(instructor.getDepartment());
						instrRoleAuthority.addQualifier(new SimpleQualifier("Role", Roles.ROLE_INSTRUCTOR));
					}
				}
			}

			Roles studentRole = Roles.getRole(Roles.ROLE_STUDENT, hibSession);
			if (studentRole != null && studentRole.isEnabled()) {
				for (Student student: (List<Student>)hibSession.createQuery(
						"from Student where externalUniqueId = :id")
						.setString("id", userId).list()) {
					if (iName == null) iName = student.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle);
					if (iEmail == null) iEmail = student.getEmail();
					UserAuthority authority = new RoleAuthority(student.getUniqueId(), studentRole);
					authority.addQualifier(student.getSession());
					addAuthority(authority);
					sessions.add(student.getSession());
				}				
			}
			
			if (sessionId == null) {
				Session session = defaultSession(sessions, null, getProperty(UserProperty.PrimaryCampus.key()));
				if (session != null) sessionId = session.getUniqueId();
			}
			if (getCurrentAuthority() == null && sessionId != null) {
				List<? extends UserAuthority> authorities = getAuthorities(Roles.ROLE_INSTRUCTOR, new SimpleQualifier("Session", sessionId));
				if (!authorities.isEmpty())
					setCurrentAuthority(authorities.get(0));
			}
			if (getCurrentAuthority() == null && sessionId != null) {
				List<? extends UserAuthority> authorities = getAuthorities(Roles.ROLE_STUDENT, new SimpleQualifier("Session", sessionId));
				if (!authorities.isEmpty())
					setCurrentAuthority(authorities.get(0));
			}
			
			Roles noRole = Roles.getRole(Roles.ROLE_NONE, hibSession);
			if (noRole != null && noRole.isEnabled()) {
				for (Session session: new TreeSet<Session>(SessionDAO.getInstance().findAll(hibSession))) {
					if (session.getStatusType() == null || !session.getStatusType().isAllowNoRole() || session.getStatusType().isTestSession()) continue;
					List<? extends UserAuthority> authorities = getAuthorities(null, new SimpleQualifier("Session", session.getUniqueId()));
					if (authorities.isEmpty()) {
						UserAuthority authority = new RoleAuthority(-1l, noRole);
						authority.addQualifier(session);
						addAuthority(authority);
						sessions.add(session);
					}
				}
			}
			
			if (getCurrentAuthority() == null) {
				Session session = defaultSession(sessions, null, getProperty(UserProperty.PrimaryCampus.key()));
				if (session != null) {
					List<? extends UserAuthority> authorities = getAuthorities(null, new SimpleQualifier("Session", session.getUniqueId()));
					if (!authorities.isEmpty())
						setCurrentAuthority(authorities.get(0));
				}
			}
			
		} finally {
			hibSession.close();
		}
		if (iName == null) iName = iLogin;
	}
	
	public static Session defaultSession(TreeSet<Session> sessions, HasRights role, String primaryCampus) {
		if (sessions==null || sessions.isEmpty()) return null; // no session -> no default
		
		//try to pick among active sessions first (check that all active sessions are of the same initiative)
        String initiative = null;
        Session lastActive = null;
        Session currentActive = null;
        Session firstFutureSession = null;
        boolean multipleInitiatives = false;
        
		Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Integer shift = ApplicationProperty.SessionDefaultShiftDays.intValue();
		if (shift != null && shift.intValue() != 0)
			cal.add(Calendar.DAY_OF_YEAR, shift);
		Date today = cal.getTime();

        for (Session session: sessions) {
            if (session.getStatusType() == null || !session.getStatusType().isActive() || session.getStatusType().isTestSession()) continue;
            if (initiative==null)
            	initiative = session.getAcademicInitiative();
            else if (!initiative.equals(session.getAcademicInitiative())) {
            	if (initiative.equals(primaryCampus)) {
            		continue; // skip other campuses
            	} else if (session.getAcademicInitiative().equals(primaryCampus)) {
            		initiative = session.getAcademicInitiative();
            		currentActive = null;
            		firstFutureSession = null;
            		lastActive = null;
            	} else {
            		multipleInitiatives = true;
            		currentActive = null;
            		firstFutureSession = null;
            		lastActive = null;
            		continue;
            	}
            }
            
            Date begin = session.getEventBeginDate();
			cal.setTime(session.getEventEndDate());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			Date end = cal.getTime();
            
            if (currentActive == null && !begin.after(today) && today.before(end))
            	currentActive = session;
            
            if (currentActive != null && firstFutureSession == null && !currentActive.equals(session))
            	firstFutureSession = session;

            if (currentActive == null && firstFutureSession == null && today.before(begin))
            	firstFutureSession = session;

            lastActive = session;
        }
        
        // multiple initiatives & no matching primary -> no default
        if (multipleInitiatives && lastActive == null) return null;
        
        if (role != null && role.hasRight(Right.SessionDefaultFirstFuture)) {
        	if (firstFutureSession != null) return firstFutureSession;
        	if (currentActive != null) return currentActive;
        }
        
        if (role != null && role.hasRight(Right.SessionDefaultFirstExamination)) {
        	if (currentActive != null && !currentActive.getStatusType().canNoRoleReportExamFinal()) return currentActive;
        	if (firstFutureSession != null) return firstFutureSession;
        }
        
        if (currentActive != null) return currentActive;
        if (firstFutureSession != null) return firstFutureSession;
        if (lastActive != null) return lastActive;
        
        Session lastNoTest = null;
        for (Session session: sessions) {
        	if (session.getStatusType() == null || session.getStatusType().isTestSession()) continue;
        	
        	Date begin = session.getEventBeginDate();
        	if (!begin.after(today)) return session;
        	
        	lastNoTest = session;
        }
        return lastNoTest;
	}
	
	public static Session defaultSession(TreeSet<Session> sessions, HasRights role) {
		return defaultSession(sessions, role, null);
	}
	
	@Override
	public void setCurrentAuthority(UserAuthority authority) {
		super.setCurrentAuthority(authority);
		if (authority.getAcademicSession() != null)
			setProperty(UserProperty.LastAcademicSession, authority.getAcademicSession().getQualifierId().toString());
	}

	@Override
	public String getExternalUserId() { return iId; }

	@Override
	public String getName() { return iName; }
	
	@Override
	public String getEmail() { return iEmail; }

	@Override
	public void setProperty(String key, String value) {
		if (value != null && value.isEmpty()) value = null;
		super.setProperty(key, value);
		if (getExternalUserId() == null || getExternalUserId().isEmpty()) return;
		org.hibernate.Session hibSession = UserDataDAO.getInstance().createNewSession();
		try {
			Settings settings = (Settings)hibSession.createQuery("from Settings where key = :key")
					.setString("key", key).setCacheable(true).setMaxResults(1).uniqueResult();
			
			if (settings != null && getCurrentAuthority() != null && !getCurrentAuthority().getQualifiers("TimetableManager").isEmpty()) {
				ManagerSettings managerData = (ManagerSettings)hibSession.createQuery(
						"from ManagerSettings where key.key = :key and manager.externalUniqueId = :id")
						.setString("key", key).setString("id", getExternalUserId()).setCacheable(true).setMaxResults(1).uniqueResult();
				
				if (value == null && managerData == null) return;
				if (value != null && managerData != null && value.equals(managerData.getValue())) return;
				
				if (managerData == null) {
					managerData = new ManagerSettings();
					managerData.setKey(settings);
					managerData.setManager(TimetableManagerDAO.getInstance().get((Long)getCurrentAuthority().getQualifiers("TimetableManager").get(0).getQualifierId(), hibSession));
				}
				managerData.setValue(value);
				
				if (value == null)
					hibSession.delete(managerData);
				else
					hibSession.saveOrUpdate(managerData);
			} else {
				UserData userData = UserDataDAO.getInstance().get(new UserData(getExternalUserId(), key), hibSession);
				if (userData == null && value == null) return;
				if (userData != null && value != null && value.equals(userData.getValue())) return;
				
				if (userData == null)
					userData = new UserData(getExternalUserId(), key);
				
				if (value == null) {
					hibSession.delete(userData);
				} else {
					userData.setValue(value);
					hibSession.saveOrUpdate(userData);
				}
			}
			hibSession.flush();
		} finally {
			hibSession.close();
		}
	}

	@Override
	public String getPassword() { return iPassword; }

	@Override
	public String getUsername() { return iLogin; }
	
	@Override
	public boolean isAccountNonLocked() { return !LoginManager.isUserLockedOut(getUsername(), new Date()); }
}
