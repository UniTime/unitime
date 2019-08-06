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
package org.unitime.timetable.server.lookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.PersonInterface.LookupRequest;
import org.unitime.timetable.interfaces.ExternalUidLookup;
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.interfaces.ExternalUidTranslation.Source;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.AdvisorDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.EventContactDAO;
import org.unitime.timetable.model.dao.StaffDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.spring.SpringApplicationContextHolder;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.NameInterface;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(PersonInterface.LookupRequest.class)
public class PeopleLookupBackend implements GwtRpcImplementation<PersonInterface.LookupRequest, GwtRpcResponseList<PersonInterface>>, ExternalUidLookup {
	private static Logger sLog = Logger.getLogger(PeopleLookupBackend.class);
	private ExternalUidTranslation iTranslation;
	private LdapTemplate iLdapTemplate;
	private SearchControls iSearchControls;
	
	private @Autowired ApplicationContext applicationContext;
    
	public PeopleLookupBackend() {
        if (ApplicationProperty.ExternalUserIdTranslation.value()!=null) {
            try {
                iTranslation = (ExternalUidTranslation)Class.forName(ApplicationProperty.ExternalUserIdTranslation.value()).getConstructor().newInstance();
            } catch (Exception e) {
            	sLog.error("Unable to instantiate external uid translation class, "+e.getMessage(), e);
            }
        }
	}
	
	protected Long getAcademicSessionId(SessionContext context) {
		if (context == null) return null;
		UserContext user = context.getUser();
		if (user == null) throw new GwtRpcException("not authenticated");
		if (user.getCurrentAuthority() == null) throw new GwtRpcException("insufficient rights");
		Long sessionId = user.getCurrentAcademicSessionId();
		if (sessionId == null) throw new GwtRpcException("academic session not selected");
		return sessionId;
	}
	
	@Override
	public GwtRpcResponseList<PersonInterface> execute(LookupRequest request, SessionContext context) {
		try {
			if (context != null) context.checkPermission(Right.HasRole);
			
			SearchContext cx = new SearchContext();
			cx.setSessionId(getAcademicSessionId(context));
			cx.setLimit(ApplicationProperty.PeopleLookupLimit.intValue());
			cx.setQuery(request.getQuery().trim().toLowerCase());
			if (cx.getQueryTokens().isEmpty()) return new GwtRpcResponseList<PersonInterface>();
			if (context != null)
				cx.setAdmin(context.hasPermission(Right.IsAdmin));
			
			boolean displayWithoutId = true;
			String[] sources = null;
			if (request.hasOptions()) {
				for (String option: request.getOptions().split(",")) {
					option = option.trim();
					if (option.equals("mustHaveExternalId"))
						displayWithoutId = false;
					else if (option.equals("allowNoExternalId"))
						displayWithoutId = true;
					else if (option.startsWith("mustHaveExternalId="))
						displayWithoutId = !"true".equalsIgnoreCase(option.substring("mustHaveExternalId=".length()));
					else if (option.startsWith("maxResults="))
						cx.setLimit(Integer.parseInt(option.substring("maxResults=".length())));
					else if (option.startsWith("session="))
						cx.setSessionId(Long.valueOf(option.substring("session=".length())));
					else if (option.startsWith("source=")) {
						sources = option.substring("source=".length()).split(":");
					}
				}
			}

			if (sources == null) {
				if (context == null || context.hasPermission(Right.CanLookupLdap)) findPeopleFromLdap(cx);
				if (context == null || context.hasPermission(Right.CanLookupStudents)) findPeopleFromStudents(cx);
				if (context == null || context.hasPermission(Right.CanLookupInstructors)) findPeopleFromInstructors(cx);
				if (context == null || context.hasPermission(Right.CanLookupStaff)) findPeopleFromStaff(cx);
				if (context == null || context.hasPermission(Right.CanLookupManagers)) findPeopleFromTimetableManagers(cx);
				if (context == null || context.hasPermission(Right.CanLookupEventContacts)) findPeopleFromEventContact(cx);
				if (context == null || context.hasPermission(Right.CanLookupAdvisors)) findPeopleFromAdvisors(cx);
			} else {
				for (String source: sources) {
					if ("ldap".equals(source) && (context == null || context.hasPermission(Right.CanLookupLdap))) findPeopleFromLdap(cx);
					if ("students".equals(source) && (context == null || context.hasPermission(Right.CanLookupStudents))) findPeopleFromStudents(cx);
					if ("staff".equals(source) && (context == null || context.hasPermission(Right.CanLookupStaff))) findPeopleFromInstructors(cx);
					if ("managers".equals(source) && (context == null || context.hasPermission(Right.CanLookupManagers))) findPeopleFromStaff(cx);
					if ("events".equals(source) && (context == null || context.hasPermission(Right.CanLookupEventContacts))) findPeopleFromTimetableManagers(cx);
					if ("instructors".equals(source) && (context == null || context.hasPermission(Right.CanLookupInstructors))) findPeopleFromEventContact(cx);
					if ("advisors".equals(source) && (context == null || context.hasPermission(Right.CanLookupAdvisors))) findPeopleFromAdvisors(cx);
				}
			}
			
			GwtRpcResponseList<PersonInterface> people =  cx.response(displayWithoutId);
			NameFormat nameFormat = NameFormat.fromReference(context != null ? context.getUser().getProperty(UserProperty.NameFormat) : NameFormat.LAST_FIRST_MIDDLE.reference());
			for (final PersonInterface person: people)
				person.setFormattedName(nameFormat.format(new NameInterface() {
					@Override
					public String getMiddleName() {
						return person.getMiddleName();
					}
					@Override
					public String getLastName() {
						return person.getLastName();
					}
					
					@Override
					public String getFirstName() {
						return person.getFirstName();
					}
					
					@Override
					public String getAcademicTitle() {
						return person.getAcademicTitle();
					}
				}));
			return people;
		} catch (GwtRpcException e) {
			throw e;
		} catch (Exception e) {
			sLog.error("Lookup failed: " + e.getMessage(), e);
			throw new GwtRpcException("Lookup failed: " + e.getMessage());
		}
	}
	
    protected String translate(String uid, Source source) {
        if (iTranslation == null || uid == null || source.equals(Source.User)) return uid;
        if (uid.trim().isEmpty()) return null;
        return iTranslation.translate(uid, source, Source.User);
    }

	
    protected void findPeopleFromStaff(SearchContext context) throws Exception {
        String q = "select s from Staff s where ";
        for (int idx = 0; idx < context.getQueryTokens().size(); idx++) {
        	if (idx > 0) q += " and ";
            q += "(lower(s.firstName) like :t" + idx + " || '%' " +
            		"or lower(s.firstName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.middleName) like :t" + idx + " || '%' " +
                	"or lower(s.middleName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.lastName) like :t" + idx + " || '%' " +
                	"or lower(s.lastName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.email) like :t" + idx + " || '%'" +
                	(context.isAdmin() ? "or s.externalUniqueId = :t" + idx : "") + ")";
        }
        q += " order by s.lastName, s.firstName, s.middleName";
        Query hq = StaffDAO.getInstance().getSession().createQuery(q);
        for (int idx = 0; idx < context.getQueryTokens().size(); idx++)
        	hq.setString("t" + idx, context.getQueryTokens().get(idx));
        if (context.getLimit() > 0)
        	hq.setMaxResults(context.getLimit());
        for (Staff staff: (List<Staff>)hq.setCacheable(true).list()) {
            context.addPerson(new PersonInterface(translate(staff.getExternalUniqueId(), Source.Staff), 
                    staff.getFirstName(), staff.getMiddleName(), staff.getLastName(), staff.getAcademicTitle(),
                    staff.getEmail(), null, staff.getDept(), 
                    (staff.getPositionType() == null ? null : staff.getPositionType().getLabel()),
                    "Staff"));
        }
    }
    
    protected void findPeopleFromAdvisors(SearchContext context) throws Exception {
        String q = "select s from Advisor s where s.lastName is not null and";
        for (int idx = 0; idx < context.getQueryTokens().size(); idx++) {
        	if (idx > 0) q += " and ";
            q += "(lower(s.firstName) like :t" + idx + " || '%' " +
            		"or lower(s.firstName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.middleName) like :t" + idx + " || '%' " +
                	"or lower(s.middleName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.lastName) like :t" + idx + " || '%' " +
                	"or lower(s.lastName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.email) like :t" + idx + " || '%'" + 
                	(context.isAdmin() ? "or s.externalUniqueId = :t" + idx : "") + ")";
        }
        q += " order by s.lastName, s.firstName, s.middleName";
        Query hq = AdvisorDAO.getInstance().getSession().createQuery(q);
        for (int idx = 0; idx < context.getQueryTokens().size(); idx++)
        	hq.setString("t" + idx, context.getQueryTokens().get(idx));
        if (context.getLimit() > 0)
        	hq.setMaxResults(context.getLimit());
        for (Advisor advisor: (List<Advisor>)hq.setCacheable(true).list()) {
            context.addPerson(new PersonInterface(translate(advisor.getExternalUniqueId(), Source.Staff), 
                    advisor.getFirstName(), advisor.getMiddleName(), advisor.getLastName(), advisor.getAcademicTitle(),
                    advisor.getEmail(), null, null, 
                    null,
                    "Advisors"));
        }
    }
    
    protected void findPeopleFromEventContact(SearchContext context) throws Exception {
        String q = "select s from EventContact s where ";
        for (int idx = 0; idx < context.getQueryTokens().size(); idx++) {
        	if (idx > 0) q += " and ";
            q += "(lower(s.firstName) like :t" + idx + " || '%' " +
            		"or lower(s.firstName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.middleName) like :t" + idx + " || '%' " +
                	"or lower(s.middleName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.lastName) like :t" + idx + " || '%' " +
                	"or lower(s.lastName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.emailAddress) like :t" + idx + " || '%'" +
                	(context.isAdmin() ? "or s.externalUniqueId = :t" + idx : "") + ")";
        }
        q += " order by s.lastName, s.firstName, s.middleName";
        Query hq = EventContactDAO.getInstance().getSession().createQuery(q);
        for (int idx = 0; idx < context.getQueryTokens().size(); idx++)
        	hq.setString("t" + idx, context.getQueryTokens().get(idx));
        if (context.getLimit() > 0)
        	hq.setMaxResults(context.getLimit());
        for (EventContact contact: (List<EventContact>)hq.setCacheable(true).list()) {
            context.addPerson(new PersonInterface(translate(contact.getExternalUniqueId(), Source.User), 
                    contact.getFirstName(), contact.getMiddleName(), contact.getLastName(), contact.getAcademicTitle(), 
                    contact.getEmailAddress(), contact.getPhone(), null, 
                    null,
                    "Event Contacts"));
        }
    }
    
    protected void findPeopleFromInstructors(SearchContext context) throws Exception {
        String q = "select s from DepartmentalInstructor s where s.department.session.uniqueId = :sessionId";
        for (int idx = 0; idx < context.getQueryTokens().size(); idx++) {
            q += " and (lower(s.firstName) like :t" + idx + " || '%' " +
            		"or lower(s.firstName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.middleName) like :t" + idx + " || '%' " +
                	"or lower(s.middleName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.lastName) like :t" + idx + " || '%' " +
                	"or lower(s.lastName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.email) like :t" + idx + " || '%'" +
                	(context.isAdmin() ? "or s.externalUniqueId = :t" + idx : "") + ")";
        }
        q += " order by s.lastName, s.firstName, s.middleName";
        Query hq = DepartmentalInstructorDAO.getInstance().getSession().createQuery(q);
        for (int idx = 0; idx < context.getQueryTokens().size(); idx++)
        	hq.setString("t" + idx, context.getQueryTokens().get(idx));
        hq.setLong("sessionId", context.getSessionId());
        if (context.getLimit() > 0)
        	hq.setMaxResults(context.getLimit());
        for (DepartmentalInstructor instructor: (List<DepartmentalInstructor>)hq.setCacheable(true).list()) {
            context.addPerson(new PersonInterface(translate(instructor.getExternalUniqueId(), Source.Staff), 
                    Constants.toInitialCase(instructor.getFirstName()),
                    Constants.toInitialCase(instructor.getMiddleName()),
                    Constants.toInitialCase(instructor.getLastName()),
                    instructor.getAcademicTitle(),
                    instructor.getEmail(), null, instructor.getDepartment().getName(),
                    (instructor.getPositionType() == null ? null : instructor.getPositionType().getLabel()),
                    "Instructors"));
        }
    }

    protected void findPeopleFromStudents(SearchContext context) throws Exception {
        String q = "select s from Student s where s.session.uniqueId = :sessionId";
        for (int idx = 0; idx < context.getQueryTokens().size(); idx++) {
            q += " and (lower(s.firstName) like :t" + idx + " || '%' " +
            		"or lower(s.firstName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.middleName) like :t" + idx + " || '%' " +
                	"or lower(s.middleName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.lastName) like :t" + idx + " || '%' " +
                	"or lower(s.lastName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.email) like :t" + idx + " || '%'" +
                	(context.isAdmin() ? "or s.externalUniqueId = :i" + idx : "") + ")";
        }
        q += " order by s.lastName, s.firstName, s.middleName";
        Query hq = StudentDAO.getInstance().getSession().createQuery(q);
        for (int idx = 0; idx < context.getQueryTokens().size(); idx++) {
        	hq.setString("t" + idx, context.getQueryTokens().get(idx));
        	if (context.isAdmin()) {
        		if (ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue()) {
            		hq.setString("i" + idx, context.getQueryTokens().get(idx).replaceFirst("^0+(?!$)", ""));
            	} else {
            		hq.setString("i" + idx, context.getQueryTokens().get(idx));
            	}
        	}
        }
        hq.setLong("sessionId", context.getSessionId());
        if (context.getLimit() > 0)
        	hq.setMaxResults(context.getLimit());
        for (Student student: (List<Student>)hq.setCacheable(true).list()) {
            context.addPerson(new PersonInterface(translate(student.getExternalUniqueId(), Source.Student), 
                    student.getFirstName(), student.getMiddleName(), student.getLastName(), student.getAcademicTitle(),
                    student.getEmail(), null, null, 
                    "Student",
                    "Students"));
        }
    }
    
    protected void findPeopleFromTimetableManagers(SearchContext context) throws Exception {
        String q = "select s from TimetableManager s where ";
        for (int idx = 0; idx < context.getQueryTokens().size(); idx++) {
        	if (idx > 0) q += " and ";
            q += "(lower(s.firstName) like :t" + idx + " || '%' " +
            		"or lower(s.firstName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.middleName) like :t" + idx + " || '%' " +
                	"or lower(s.middleName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.lastName) like :t" + idx + " || '%' " +
                	"or lower(s.lastName) like '% ' || :t" + idx + " || '%' " +
                	"or lower(s.emailAddress) like :t" + idx + " || '%'" +
                	(context.isAdmin() ? "or s.externalUniqueId = :t" + idx : "") + ")";
        }
        q += " order by s.lastName, s.firstName, s.middleName";
        Query hq = TimetableManagerDAO.getInstance().getSession().createQuery(q);
        for (int idx = 0; idx < context.getQueryTokens().size(); idx++)
        	hq.setString("t" + idx, context.getQueryTokens().get(idx));
        if (context.getLimit() > 0)
        	hq.setMaxResults(context.getLimit());
        for (TimetableManager manager: (List<TimetableManager>)hq.setCacheable(true).list()) {
            context.addPerson(new PersonInterface(translate(manager.getExternalUniqueId(), Source.User), 
                    manager.getFirstName(), manager.getMiddleName(), manager.getLastName(), manager.getAcademicTitle(),
                    manager.getEmailAddress(), null, null, 
                 (manager.getPrimaryRole()==null?null:manager.getPrimaryRole().getAbbv()),
                 "Timetable Managers"));
        }
    }
    
	protected LdapTemplate getLdapTemplate() {
		if (iLdapTemplate == null) {
			try {
				if (applicationContext != null) {
					iLdapTemplate = applicationContext.getBean("ldapPeopleLookupTemplate", LdapTemplate.class);
				} else {
					iLdapTemplate = (LdapTemplate)SpringApplicationContextHolder.getBean("ldapPeopleLookupTemplate");
				}
				if (iLdapTemplate != null) return iLdapTemplate;
			} catch (BeansException e) {}
			String url = ApplicationProperty.PeopleLookupLdapUrl.value();
			if (url == null) return null;
			sLog.warn("Failed to locate bean ldapPeopleLookupTemplate, creating the template manually.");
			LdapContextSource source = new LdapContextSource();
			source.setUrl(url);
			source.setBase(ApplicationProperty.PeopleLookupLdapBase.value());
			String user = ApplicationProperty.PeopleLookupLdapUser.value();
			if (user != null) {
				source.setUserDn(user);
				String password = ApplicationProperty.PeopleLookupLdapPassword.value();
				if (password != null) source.setPassword(password);
			} else {
				source.setAnonymousReadOnly(true);
			}
			try {
				source.afterPropertiesSet();
			} catch (Exception e) {
				sLog.error("Failed to initialze LDAP context source: " + e.getMessage(), e);
			}
			iLdapTemplate = new LdapTemplate(source);
		}
		return iLdapTemplate;
	}
	
	protected SearchControls getSearchControls() {
		if (iSearchControls == null) {
			iSearchControls = new SearchControls();
			iSearchControls.setCountLimit(ApplicationProperty.PeopleLookupLdapLimit.intValue());
		}
		return iSearchControls;
	}
    
    protected void findPeopleFromLdap(final SearchContext context) throws Exception {
    	try {
        	if (getLdapTemplate() == null) return;
            String filter = "";
            for (String token: context.getQueryTokens()) {
                String t = token.replace('_', '*').replace('%', '*');
                if (filter.length()==0)
                    filter = ApplicationProperty.PeopleLookupLdapQuery.value().replaceAll("%", t);
                else
                    filter = "(&"+filter+ApplicationProperty.PeopleLookupLdapQuery.value().replaceAll("%", t)+")";
            }
            getLdapTemplate().search("", filter, getSearchControls(), new AttributesMapper() {
        		protected String getAttribute(Attributes attrs, String name) {
        	        if (attrs==null) return null;
        	        if (name == null || name.isEmpty()) return null;
        	        for (StringTokenizer stk = new StringTokenizer(name,",");stk.hasMoreTokens();) {
        	            Attribute a = attrs.get(stk.nextToken());
        	            try {
        	                if (a!=null && a.get()!=null) return a.get().toString();
        	            } catch (NamingException e) {
        	            }
        	        }
        	        return null;
        	    }
    			@Override
    			public Object mapFromAttributes(Attributes a) throws NamingException {
    				PersonInterface person = new PersonInterface(translate(getAttribute(a,"uid"), Source.LDAP),
                            Constants.toInitialCase(getAttribute(a,"givenName")),
                            Constants.toInitialCase(getAttribute(a,"cn")),
                            Constants.toInitialCase(getAttribute(a,"sn")),
                            getAttribute(a, ApplicationProperty.PeopleLookupLdapAcademicTitleAttribute.value()),
                            getAttribute(a, ApplicationProperty.PeopleLookupLdapEmailAttribute.value()),
                            getAttribute(a, ApplicationProperty.PeopleLookupLdapPhoneAttribute.value()),
                            Constants.toInitialCase(getAttribute(a, ApplicationProperty.PeopleLookupLdapDepartmentAttribute.value())),
                            Constants.toInitialCase(getAttribute(a, ApplicationProperty.PeopleLookupLdapPositionAttribute.value())),
                            "Directory");
    				context.addPerson(person);
    				return person;
    			}
    		});
    	} catch (Exception e) {
    		sLog.warn(e.getMessage());
    	}
    }
    
    protected static class SearchContext {
    	private Hashtable<String, PersonInterface> iPeople = new Hashtable<String, PersonInterface>();
		private TreeSet<PersonInterface> iPeopleWithoutId = new TreeSet<PersonInterface>();
		private int iLimit = -1;
		private Long iSessionId = null;
		private String iQuery = null;
		private List<String> iTokens = null;
		private boolean iAdmin = false;
		
		SearchContext() {}
		
		public void setLimit(int limit) { iLimit = limit; }
		public int getLimit() { return iLimit; }
		
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getSessionId() { return iSessionId; }
		
		public void setAdmin(boolean admin) { iAdmin = admin; }
		public boolean isAdmin() { return iAdmin; }
		
		public void setQuery(String query) {
			iQuery = query;
			if (iTokens == null)
				iTokens = new ArrayList<String>();
			else
				iTokens.clear();
			for (StringTokenizer stk = new StringTokenizer(query," ,"); stk.hasMoreTokens();) {
				String t = stk.nextToken();
				iTokens.add(t.toLowerCase());
            }
		}
		public String getQuery() { return iQuery; }
		public List<String> getQueryTokens() { return iTokens; }
		
		public void addPerson(PersonInterface person) {
			if (person.getId() == null || person.getId().isEmpty() || "null".equals(person.getId())) {
				iPeopleWithoutId.add(person);
			} else {
				PersonInterface old = iPeople.get(person.getId());
				if (old == null) {
					iPeople.put(person.getId(), person);
				} else {
					old.merge(person);
				}
			}
		}
		
		public GwtRpcResponseList<PersonInterface> response(boolean displayWithoutId) {
	        GwtRpcResponseList<PersonInterface> ret = new GwtRpcResponseList<PersonInterface>(iPeople.values());
	        Collections.sort(ret);
	        if (displayWithoutId)
	        	ret.addAll(iPeopleWithoutId);
	        if (getLimit() > 0 && ret.size() > getLimit()) {
	        	return new GwtRpcResponseList<PersonInterface>(ret.subList(0, getLimit()));
	        } else {
	        	return ret;
	        }
		}
    	
    }

	@Override
	public UserInfo doLookup(String uid) throws Exception {
		try {
			if (uid == null || uid.isEmpty()) return null;
        	if (getLdapTemplate() == null) return null;
        	
			if (iTranslation != null)
				uid = iTranslation.translate(uid, Source.User, Source.LDAP);
			
			return (UserInfo)getLdapTemplate().lookup("uid=" + uid, new AttributesMapper() {
        		protected String getAttribute(Attributes attrs, String name) {
        	        if (attrs==null) return null;
        	        if (name == null || name.isEmpty()) return null;
        	        for (StringTokenizer stk = new StringTokenizer(name,",");stk.hasMoreTokens();) {
        	            Attribute a = attrs.get(stk.nextToken());
        	            try {
        	                if (a!=null && a.get()!=null) return a.get().toString();
        	            } catch (NamingException e) {
        	            }
        	        }
        	        return null;
        	    }
				@Override
				public Object mapFromAttributes(Attributes a) throws NamingException {
		        	UserInfo info = new UserInfo();
		        	info.setUserName(getAttribute(a,"uid"));
		        	if (iTranslation == null)
		        		info.setExternalId(info.getUserName());
		        	else
		        		info.setExternalId(iTranslation.translate(info.getUserName(), Source.LDAP, Source.User));
		        	info.setFirstName(Constants.toInitialCase(getAttribute(a,"givenName")));
		        	info.setName(Constants.toInitialCase(getAttribute(a,"cn")));
		        	info.setLastName(Constants.toInitialCase(getAttribute(a,"sn")));
		        	info.setEmail(getAttribute(a, ApplicationProperty.PeopleLookupLdapEmailAttribute.value()));
		        	info.setPhone(getAttribute(a, ApplicationProperty.PeopleLookupLdapPhoneAttribute.value()));
		        	info.setAcademicTitle(getAttribute(a, ApplicationProperty.PeopleLookupLdapAcademicTitleAttribute.value()));
		        	if (info.getName() != null) {
		        		String middle = info.getName();
		        		if (info.getFirstName() != null && middle.indexOf(info.getFirstName()) >= 0)
		        			middle = middle.replaceAll(info.getFirstName() + " ?", "");
		        		if (info.getLastName() != null && middle.indexOf(info.getLastName()) >= 0)
		        			middle = middle.replaceAll(" ?" + info.getLastName(), "");
		        		info.setMiddleName(middle);
		        	}
					return info;
				}
			});
    	} catch (Exception e) {
    		sLog.warn("Failed to lookup a person: " + e.getMessage(), e);
    		return null;
    	}
	}
}
