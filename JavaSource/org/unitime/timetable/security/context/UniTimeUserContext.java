/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.security.context;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.ManagerSettings;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.model.dao.UserDataDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.authority.InstructorAuthority;
import org.unitime.timetable.security.authority.RoleAuthority;
import org.unitime.timetable.security.authority.StudentAuthority;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.HasRights;
import org.unitime.timetable.security.rights.Right;

public class UniTimeUserContext extends AbstractUserContext {
	private static final long serialVersionUID = 1L;
	
	private String iId, iPassword, iName, iLogin;
	
	public UniTimeUserContext(String userId, String login, String name, String password) {
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

			Long sessionId = null;
			if ("true".equals(ApplicationProperties.getProperty(ApplicationProperty.KeepLastUsedAcademicSession))) {
				String lastSessionId = getProperty(UserProperty.LastAcademicSession);
				if (lastSessionId != null) sessionId = Long.valueOf(lastSessionId);
			}
			
			TimetableManager manager = (TimetableManager)hibSession.createQuery(
					"from TimetableManager where externalUniqueId = :id")
					.setString("id", userId).setMaxResults(1).uniqueResult();
			if (manager != null) {
				iName = manager.getName();
				Roles primary = null;
				
				TreeSet<Session> primarySessions = null;
				
				for (ManagerRole role: manager.getManagerRoles()) {
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
						
						RoleAuthority authority = new RoleAuthority(role.getRole());
						authority.addQualifier(session);
						authority.addQualifier(manager);
						for (Department department: manager.getDepartments())
							if (department.getSession().equals(session))
								authority.addQualifier(department);
						addAuthority(authority);
					}
				}
				
				if (sessionId == null && primary != null) {
					Session session = defaultSession(primarySessions, primary);
					if (session != null) sessionId = session.getUniqueId();
				}
				if (sessionId != null && primary != null) {
					List<? extends UserAuthority> authorities = getAuthorities(primary.getReference(), new SimpleQualifier("Session", sessionId));
					if (!authorities.isEmpty())
						setCurrentAuthority(authorities.get(0));
				}
			}
			
			TreeSet<Session> sessions = new TreeSet<Session>();
			for (DepartmentalInstructor instructor: (List<DepartmentalInstructor>)hibSession.createQuery(
					"from DepartmentalInstructor where externalUniqueId = :id")
					.setString("id", userId).list()) {
				if (iName == null) iName = instructor.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle);
				List<? extends UserAuthority> authorities = getAuthorities(InstructorAuthority.TYPE, instructor.getDepartment().getSession());
				InstructorAuthority authority = (authorities.isEmpty() ? null : (InstructorAuthority)authorities.get(0));
				if (authority == null) {
					authority = new InstructorAuthority(instructor);
					authority.addQualifier(instructor.getDepartment().getSession());
					addAuthority(authority);
					sessions.add(instructor.getDepartment().getSession());
				}
				authority.addQualifier(instructor.getDepartment());
			}

			for (Student student: (List<Student>)hibSession.createQuery(
					"from Student where externalUniqueId = :id")
					.setString("id", userId).list()) {
				if (iName == null) iName = student.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle);
				addAuthority(new StudentAuthority(student));
				sessions.add(student.getSession());
			}
			
			if (sessionId == null) {
				Session session = defaultSession(sessions, null);
				if (session != null) sessionId = session.getUniqueId();
			}
			if (getCurrentAuthority() == null && sessionId != null) {
				List<? extends UserAuthority> authorities = getAuthorities(InstructorAuthority.TYPE, new SimpleQualifier("Session", sessionId));
				if (!authorities.isEmpty())
					setCurrentAuthority(authorities.get(0));
			}
			if (getCurrentAuthority() == null && sessionId != null) {
				List<? extends UserAuthority> authorities = getAuthorities(StudentAuthority.TYPE, new SimpleQualifier("Session", sessionId));
				if (!authorities.isEmpty())
					setCurrentAuthority(authorities.get(0));
			}
		} finally {
			hibSession.close();
		}
	}
	
	public static Session defaultSession(TreeSet<Session> sessions, HasRights role) {
		if (sessions==null || sessions.isEmpty()) return null; // no session -> no default
		
		//try to pick among active sessions first (check that all active sessions are of the same initiative)
        String initiative = null;
        Session lastActive = null;
        Session currentActive = null;
        Session firstFutureSession = null;
        
		Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date today = cal.getTime();

        for (Session session: sessions) {
            if (session.getStatusType() == null || !session.getStatusType().isActive() || session.getStatusType().isTestSession()) continue;
            if (initiative==null)
            	initiative = session.getAcademicInitiative();
            else if (!initiative.equals(session.getAcademicInitiative()))
                return null; // multiple initiatives -> no default
            
            Date begin = session.getEventBeginDate();
			cal.setTime(session.getEventEndDate());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			Date end = cal.getTime();

            
            if (currentActive == null && begin.before(today) && today.before(end))
            	currentActive = session;
            
            if (currentActive != null && firstFutureSession == null && !currentActive.equals(session))
            	firstFutureSession = session;

            if (currentActive == null && firstFutureSession == null && today.before(begin))
            	firstFutureSession = session;

            lastActive = session;
        }
        
        
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
        	if (today.after(begin)) return session;
        	
        	lastNoTest = session;
        }
        return lastNoTest;
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
	public void setProperty(String key, String value) {
		if (value != null && value.isEmpty()) value = null;
		super.setProperty(key, value);
		org.hibernate.Session hibSession = UserDataDAO.getInstance().createNewSession();
		try {
			Settings settings = (Settings)hibSession.createQuery("from Settings where key = :key")
					.setString("key", key).setCacheable(true).setMaxResults(1).uniqueResult();
			
			if (settings != null && getCurrentAuthority() != null && !getCurrentAuthority().getQualifiers("TimetableManager").isEmpty()) {
				ManagerSettings managerData = (ManagerSettings)hibSession.createQuery(
						"from ManagerSettings where key.key = :key and manager.externalUniqueId = :id")
						.setString("key", key).setString("id", getExternalUserId()).setMaxResults(1).uniqueResult();
				
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
				UserData userData = UserDataDAO.getInstance().get(new UserData(getExternalUserId(), key));
				if (userData == null && value == null) return;
				if (userData != null && value != null && value.equals(userData.getValue())) return;
				
				if (userData == null)
					userData = new UserData(getExternalUserId(), key);
				userData.setValue(value);

				if (value == null)
					hibSession.delete(userData);
				else
					hibSession.saveOrUpdate(userData);
			}
		} finally {
			hibSession.close();
		}
	}

	@Override
	public String getPassword() { return iPassword; }

	@Override
	public String getUsername() { return iLogin; }
}
