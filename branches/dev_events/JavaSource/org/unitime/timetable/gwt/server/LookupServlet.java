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
package org.unitime.timetable.gwt.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.services.LookupService;
import org.unitime.timetable.gwt.shared.LookupException;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.interfaces.ExternalUidTranslation.Source;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.EventContactDAO;
import org.unitime.timetable.model.dao.StaffDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author Tomas Muller
 */
public class LookupServlet extends RemoteServiceServlet implements LookupService {
	private static final long serialVersionUID = -7266424119672666037L;
	private static Logger sLog = Logger.getLogger(LookupServlet.class);
	private static ExternalUidTranslation iTranslation;
    
	public void init() throws ServletException {
        if (ApplicationProperties.getProperty("tmtbl.externalUid.translation")!=null) {
            try {
                iTranslation = (ExternalUidTranslation)Class.forName(ApplicationProperties.getProperty("tmtbl.externalUid.translation")).getConstructor().newInstance();
            } catch (Exception e) {
            	sLog.error("Unable to instantiate external uid translation class, "+e.getMessage(), e);
            }
        }
	}

	private Long getAcademicSessionId() {
		if (getThreadLocalRequest() == null) return null;
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null) throw new LookupException("not authenticated");
		if (user.getRole() == null) throw new LookupException("insufficient rights");
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
		if (sessionId == null) throw new LookupException("academic session not selected");
		return sessionId;
	}

	@Override
	public List<PersonInterface> lookupPeople(String query, String options) throws LookupException {
		try {
			boolean displayWithoutId = true;
			int maxResults = -1;
			boolean ldap = true, students = true, staff = true, managers = true, events = true, instructors = true;
			Long sessionId = getAcademicSessionId();
			if (options != null) {
				for (String option: options.split(",")) {
					option = option.trim();
					if (option.equals("mustHaveExternalId"))
						displayWithoutId = false;
					else if (option.equals("allowNoExternalId"))
						displayWithoutId = true;
					else if (option.startsWith("mustHaveExternalId="))
						displayWithoutId = !"true".equalsIgnoreCase(option.substring("mustHaveExternalId=".length()));
					else if (option.startsWith("maxResults="))
						maxResults = Integer.parseInt(option.substring("maxResults=".length()));
					else if (option.startsWith("session="))
						sessionId = Long.valueOf(option.substring("session=".length()));
					else if (option.startsWith("source=")) {
						ldap = students = staff = managers = events = instructors = false;
						for (String s: option.substring("source=".length()).split(":")) {
							if ("ldap".equals(s)) ldap = true;
							if ("students".equals(s)) students = true;
							if ("staff".equals(s)) staff = true;
							if ("managers".equals(s)) managers = true;
							if ("events".equals(s)) events = true;
							if ("instructors".equals(s)) instructors = true;
						}
					}
				}
			}
			Hashtable<String, PersonInterface> people = new Hashtable<String, PersonInterface>();
			TreeSet<PersonInterface> peopleWithoutId = new TreeSet<PersonInterface>();
			String q = query.trim().toLowerCase();
	        if (ldap) findPeopleFromLdap(people, peopleWithoutId, q);
	        if (students) findPeopleFromStudents(people, peopleWithoutId, q, sessionId);
	        if (instructors) findPeopleFromInstructors(people, peopleWithoutId, q, sessionId);
	        if (staff) findPeopleFromStaff(people, peopleWithoutId, q);
	        if (managers) findPeopleFromTimetableManagers(people, peopleWithoutId, q);
	        if (events) findPeopleFromEventContact(people, peopleWithoutId, q);
	        List<PersonInterface> ret = new ArrayList<PersonInterface>(people.values());
	        Collections.sort(ret);
	        if (displayWithoutId)
	        	ret.addAll(peopleWithoutId);
	        if (maxResults > 0 && ret.size() > maxResults) {
	        	return new ArrayList<PersonInterface>(ret.subList(0, maxResults));
	        }
			return ret;
		} catch (Exception e) {
			if (e instanceof LookupException) throw (LookupException)e;
			sLog.error("Lookup failed: " + e.getMessage(), e);
			throw new LookupException("Lookup failed: " + e.getMessage());
		}
	}
	
	protected void addPerson(Hashtable<String, PersonInterface> people, TreeSet<PersonInterface> peopleWithoutId, PersonInterface person) {
		if (person.getId() == null || person.getId().isEmpty() || "null".equals(person.getId())) {
			peopleWithoutId.add(person);
		} else {
			PersonInterface old = people.get(person.getId());
			if (old == null) {
				people.put(person.getId(), person);
			} else {
				old.merge(person);
			}
		}
	}
	
    protected String translate(String uid, Source source) {
        if (iTranslation == null || uid == null || source.equals(Source.User)) return uid;
        if (uid.trim().isEmpty()) return null;
        return iTranslation.translate(uid, source, Source.User);
    }

	
    protected void findPeopleFromStaff(Hashtable<String, PersonInterface> people, TreeSet<PersonInterface> peopleWithoutId, String query) throws Exception {
        String q = "select s from Staff s where ";
        for (StringTokenizer stk = new StringTokenizer(query," ,"); stk.hasMoreTokens();) {
            String t = stk.nextToken().replace("'", "''");
            q += "(lower(s.firstName) like '"+t+"%' or lower(s.middleName) like '"+t+"%' or lower(s.lastName) like '"+t+"%' or lower(s.email) like '"+t+"%')";
            if (stk.hasMoreTokens()) q += " and ";
        }
        for (Iterator i=StaffDAO.getInstance().getSession().createQuery(q).iterate();i.hasNext();) {
            Staff staff = (Staff)i.next();
            addPerson(people, peopleWithoutId, new PersonInterface(translate(staff.getExternalUniqueId(), Source.Staff), 
                    staff.getFirstName(), staff.getMiddleName(), staff.getLastName(),
                    staff.getEmail(), null, staff.getDept(), 
                    (staff.getPositionType() == null ? null : staff.getPositionType().getLabel()),
                    "Staff"));
        }
    }
    
    protected void findPeopleFromEventContact(Hashtable<String, PersonInterface> people, TreeSet<PersonInterface> peopleWithoutId, String query) throws Exception {
        String q = "select s from EventContact s where ";
        for (StringTokenizer stk = new StringTokenizer(query," ,"); stk.hasMoreTokens();) {
            String t = stk.nextToken().replace("'", "''");
            q += "(lower(s.firstName) like '"+t+"%' or lower(s.middleName) like '"+t+"%' or lower(s.lastName) like '"+t+"%' or lower(s.emailAddress) like '"+t+"%')";
            if (stk.hasMoreTokens()) q += " and ";
        }
        for (Iterator i=EventContactDAO.getInstance().getSession().createQuery(q).iterate();i.hasNext();) {
            EventContact contact = (EventContact)i.next();
            addPerson(people, peopleWithoutId, new PersonInterface(translate(contact.getExternalUniqueId(), Source.User), 
                    contact.getFirstName(), contact.getMiddleName(), contact.getLastName(),
                    contact.getEmailAddress(), contact.getPhone(), null, 
                    null,
                    "Event Contacts"));
        }
    }
    
    protected void findPeopleFromInstructors(Hashtable<String, PersonInterface> people, TreeSet<PersonInterface> peopleWithoutId, String query, Long sessionId) throws Exception {
        String q = "select s from DepartmentalInstructor s where s.department.session.uniqueId="+sessionId+" and ";
        for (StringTokenizer stk = new StringTokenizer(query," ,"); stk.hasMoreTokens();) {
            String t = stk.nextToken().replace("'", "''");
            q += "(lower(s.firstName) like '"+t+"%' or lower(s.middleName) like '"+t+"%' or lower(s.lastName) like '"+t+"%' or lower(s.email) like '"+t+"%')";
            if (stk.hasMoreTokens()) q += " and ";
        }
        for (Iterator i=DepartmentalInstructorDAO.getInstance().getSession().createQuery(q).iterate();i.hasNext();) {
        	DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
            addPerson(people, peopleWithoutId, new PersonInterface(translate(instructor.getExternalUniqueId(), Source.Staff), 
                    Constants.toInitialCase(instructor.getFirstName()),
                    Constants.toInitialCase(instructor.getMiddleName()),
                    Constants.toInitialCase(instructor.getLastName()),
                    instructor.getEmail(), null, instructor.getDepartment().getName(),
                    (instructor.getPositionType() == null ? null : instructor.getPositionType().getLabel()),
                    "Instructors"));
        }
    }

    protected void findPeopleFromStudents(Hashtable<String, PersonInterface> people, TreeSet<PersonInterface> peopleWithoutId, String query, Long sessionId) throws Exception {
        String q = "select s from Student s where s.session.uniqueId="+sessionId+" and ";
        for (StringTokenizer stk = new StringTokenizer(query," ,"); stk.hasMoreTokens();) {
            String t = stk.nextToken().replace("'", "''");
            q += "(lower(s.firstName) like '"+t+"%' or lower(s.middleName) like '"+t+"%' or lower(s.lastName) like '"+t+"%' or lower(s.email) like '"+t+"%')";
            if (stk.hasMoreTokens()) q += " and ";
        }
        for (Iterator i=StudentDAO.getInstance().getSession().createQuery(q).iterate();i.hasNext();) {
            Student student = (Student)i.next();
            addPerson(people, peopleWithoutId, new PersonInterface(translate(student.getExternalUniqueId(), Source.Student), 
                    student.getFirstName(), student.getMiddleName(), student.getLastName(),
                    student.getEmail(), null, null, 
                    "Student",
                    "Students"));
        }
    }
    
    protected void findPeopleFromTimetableManagers(Hashtable<String, PersonInterface> people, TreeSet<PersonInterface> peopleWithoutId, String query) throws Exception {
        String q = "select s from TimetableManager s where ";
        for (StringTokenizer stk = new StringTokenizer(query," ,"); stk.hasMoreTokens();) {
            String t = stk.nextToken().replace("'", "''");
            q += "(lower(s.firstName) like '"+t+"%' or lower(s.middleName) like '"+t+"%' or lower(s.lastName) like '"+t+"%' or lower(s.emailAddress) like '"+t+"%')";
            if (stk.hasMoreTokens()) q += " and ";
        }
        for (Iterator i=TimetableManagerDAO.getInstance().getSession().createQuery(q).iterate();i.hasNext();) {
            TimetableManager manager = (TimetableManager)i.next();
            addPerson(people, peopleWithoutId, new PersonInterface(translate(manager.getExternalUniqueId(), Source.User), 
                    manager.getFirstName(), manager.getMiddleName(), manager.getLastName(),
                    manager.getEmailAddress(), null, null, 
                 (manager.getPrimaryRole()==null?null:manager.getPrimaryRole().getAbbv()),
                 "Timetable Managers"));
        }
    }
    
    protected void findPeopleFromLdap(Hashtable<String, PersonInterface> people, TreeSet<PersonInterface> peopleWithoutId, String query) throws Exception {
        if (ApplicationProperties.getProperty("tmtbl.lookup.ldap")==null) return;
        InitialDirContext ctx = null;
        try {
            Hashtable<String,String> env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ApplicationProperties.getProperty("tmtbl.lookup.ldap"));
            env.put(Context.REFERRAL, "ignore");
            env.put("java.naming.ldap.version", "3");
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            ctx = new InitialDirContext(env);
            SearchControls ctls = new SearchControls();
            ctls.setCountLimit(Integer.parseInt(ApplicationProperties.getProperty("tmtbl.lookup.ldap.countLimit", "100")));
            String filter = "";
            for (StringTokenizer stk = new StringTokenizer(query," ,"); stk.hasMoreTokens();) {
                String t = stk.nextToken().replace('_', '*').replace('%', '*');
                if (filter.length()==0)
                    filter = ApplicationProperties.getProperty("tmtbl.lookup.ldap.query", "(|(|(sn=%*)(uid=%))(givenName=%*)("+ApplicationProperties.getProperty("tmtbl.lookup.ldap.email","mail")+"=%*))").replaceAll("%", t);
                else
                    filter = "(&"+filter+ApplicationProperties.getProperty("tmtbl.lookup.ldap.query", "(|(|(sn=%*)(uid=%))(givenName=%*)("+ApplicationProperties.getProperty("tmtbl.lookup.ldap.email","mail")+"=%*))").replaceAll("%", t)+")";
            }
            for (NamingEnumeration<SearchResult> e=ctx.search(ApplicationProperties.getProperty("tmtbl.lookup.ldap.name",""),filter,ctls);e.hasMore();) {
            	Attributes a = e.next().getAttributes();
                addPerson(people, peopleWithoutId, new PersonInterface(translate(getAttribute(a,"uid"), Source.LDAP),
                        Constants.toInitialCase(getAttribute(a,"givenName")),
                        Constants.toInitialCase(getAttribute(a,"cn")),
                        Constants.toInitialCase(getAttribute(a,"sn")),
                        getAttribute(a,ApplicationProperties.getProperty("tmtbl.lookup.ldap.email","mail")),
                        getAttribute(a,ApplicationProperties.getProperty("tmtbl.lookup.ldap.phone","phone,officePhone,homePhone,telephoneNumber")),
                        Constants.toInitialCase(getAttribute(a,ApplicationProperties.getProperty("tmtbl.lookup.ldap.department","department"))),
                        Constants.toInitialCase(getAttribute(a,ApplicationProperties.getProperty("tmtbl.lookup.ldap.position","position,title"))),
                        "Directory"));
            }
        } catch (NamingException e) {
        	sLog.warn("Unable to use lookup, error: " + e.getMessage(), e);
        } finally {
            try {
                if (ctx!=null) ctx.close();
            } catch (Exception e) {}
        }
    }
    
    protected static String getAttribute(Attributes attrs, String name) {
        if (attrs==null) return null;
        for (StringTokenizer stk = new StringTokenizer(name,",");stk.hasMoreTokens();) {
            Attribute a = attrs.get(stk.nextToken());
            try {
                if (a!=null && a.get()!=null) return a.get().toString();
            } catch (NamingException e) {}
        }
        return null;
    }
}
