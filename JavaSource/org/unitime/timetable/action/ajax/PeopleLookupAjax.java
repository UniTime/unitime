/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.action.ajax;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
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
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.interfaces.ExternalUidTranslation.Source;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.EventContactDAO;
import org.unitime.timetable.model.dao.StaffDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;

/**
 * 
 * @author Tomas Muller
 *
 */
public class PeopleLookupAjax extends Action {
    public static ExternalUidTranslation sTranslation;
    
    static {
        if (ApplicationProperties.getProperty("tmtbl.externalUid.translation")!=null) {
            try {
                sTranslation = (ExternalUidTranslation)Class.forName(ApplicationProperties.getProperty("tmtbl.externalUid.translation")).getConstructor().newInstance();
            } catch (Exception e) { Debug.error("Unable to instantiate external uid translation class, "+e.getMessage()); }
        }
    }
    
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        response.addHeader("Content-Type", "text/xml");
        
        ServletOutputStream out = response.getOutputStream();
        
        try {
            out.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n");
            out.print("<results>");
            if (request.getParameter("query")!=null)
                for (Person p : findPeople(request.getParameter("query").toLowerCase(),request.getParameter("session")))
                    out.print(p.toXML());
            out.print("</results>");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;        

    }
    
    public static String translate(String uid, Source source) {
        if (sTranslation==null || uid==null || source.equals(Source.User)) return uid;
        return sTranslation.translate(uid, source, Source.User);
    }
    
    protected void print(ServletOutputStream out, String id, String fname, String mname, String lname, String email, String phone, String source) throws IOException {
        out.print("<result id=\""+id+"\" ");
        if (fname!=null) out.print("fname=\""+fname+"\" ");
        if (mname!=null) out.print("mname=\""+mname+"\" ");
        if (lname!=null) out.print("lname=\""+lname+"\" ");
        if (email!=null) out.print("email=\""+email+"\" ");
        if (phone!=null) out.print("phone=\""+phone+"\" ");
        if (source!=null) out.print("source=\""+source+"\" ");
        out.println("/>");
    }
    
    public TreeSet<Person> findPeople(String query, String session) throws Exception {
        TreeSet<Person> people = new TreeSet();
        people.addAll(findPeopleFromStaff(query));
        people.addAll(findPeopleFromStudents(query, session));
        people.addAll(findPeopleFromEventContact(query));
        people.addAll(findPeopleFromTimetableManagers(query));
        people.addAll(findPeopleFromLdap(query));
        return people;
    }
    
    protected TreeSet<Person> findPeopleFromStaff(String query) throws Exception {
        TreeSet<Person> people = new TreeSet();
        String q = "select s from Staff s where ";
        for (StringTokenizer stk = new StringTokenizer(query," ,"); stk.hasMoreTokens();) {
            String t = stk.nextToken();
            q += "(lower(s.firstName) like '"+t+"%' or lower(s.middleName) like '"+t+"%' or lower(s.lastName) like '"+t+"%')";
            if (stk.hasMoreTokens()) q += " and ";
        }
        for (Iterator i=StaffDAO.getInstance().getSession().createQuery(q).iterate();i.hasNext();) {
            Staff s = (Staff)i.next();
            people.add(new Person(s));
        }
        return people;
    }
    
    protected TreeSet<Person> findPeopleFromEventContact(String query) throws Exception {
        TreeSet<Person> people = new TreeSet();
        String q = "select s from EventContact s where ";
        for (StringTokenizer stk = new StringTokenizer(query," ,"); stk.hasMoreTokens();) {
            String t = stk.nextToken();
            q += "(lower(s.firstName) like '"+t+"%' or lower(s.middleName) like '"+t+"%' or lower(s.lastName) like '"+t+"%')";
            if (stk.hasMoreTokens()) q += " and ";
        }
        for (Iterator i=EventContactDAO.getInstance().getSession().createQuery(q).iterate();i.hasNext();) {
            EventContact s = (EventContact)i.next();
            people.add(new Person(s));
        }
        return people;
    }

    protected TreeSet<Person> findPeopleFromStudents(String query, String session) throws Exception {
        TreeSet<Person> people = new TreeSet();
        if (session==null || session.length()==0) return people; 
        String q = "select s from Student s where s.session.uniqueId="+session+" and ";
        for (StringTokenizer stk = new StringTokenizer(query," ,"); stk.hasMoreTokens();) {
            String t = stk.nextToken();
            q += "(lower(s.firstName) like '"+t+"%' or lower(s.middleName) like '"+t+"%' or lower(s.lastName) like '"+t+"%')";
            if (stk.hasMoreTokens()) q += " and ";
        }
        for (Iterator i=StudentDAO.getInstance().getSession().createQuery(q).iterate();i.hasNext();) {
            Student s = (Student)i.next();
            people.add(new Person(s));
        }
        return people;
    }
    
    protected TreeSet<Person> findPeopleFromTimetableManagers(String query) throws Exception {
        TreeSet<Person> people = new TreeSet();
        String q = "select s from TimetableManager s where ";
        for (StringTokenizer stk = new StringTokenizer(query," ,"); stk.hasMoreTokens();) {
            String t = stk.nextToken();
            q += "(lower(s.firstName) like '"+t+"%' or lower(s.middleName) like '"+t+"%' or lower(s.lastName) like '"+t+"%')";
            if (stk.hasMoreTokens()) q += " and ";
        }
        for (Iterator i=TimetableManagerDAO.getInstance().getSession().createQuery(q).iterate();i.hasNext();) {
            TimetableManager s = (TimetableManager)i.next();
            people.add(new Person(s));
        }
        return people;
    }
    
    protected TreeSet<Person> findPeopleFromLdap(String query) throws Exception {
        TreeSet<Person> people = new TreeSet();
        if (ApplicationProperties.getProperty("tmtbl.lookup.ldap")==null) return people;
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
            ctls.setCountLimit(100);
            String filter = "";
            for (StringTokenizer stk = new StringTokenizer(query," ,"); stk.hasMoreTokens();) {
                String t = stk.nextToken();
                if (filter.length()==0)
                    filter = "(|(|(sn="+t+"*)(uid="+t+"))(givenName="+t+"*))";
                else
                    filter = "(&"+filter+"(|(|(sn="+t+"*)(uid="+t+"))(givenName="+t+"*)))";
            }
            for (NamingEnumeration<SearchResult> e=ctx.search(ApplicationProperties.getProperty("tmtbl.lookup.ldap.name",""),filter,ctls);e.hasMore();)
                people.add(new Person(e.next().getAttributes()));
        } catch (NamingException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ctx!=null) ctx.close();
            } catch (Exception e) {}
        }
        return people;
    }
    
    public static String getAttribute(Attributes attrs, String name) {
        if (attrs==null) return null;
        for (StringTokenizer stk = new StringTokenizer(name,",");stk.hasMoreTokens();) {
            Attribute a = attrs.get(stk.nextToken());
            try {
                if (a!=null && a.get()!=null) return a.get().toString();
            } catch (NamingException e) {}
        }
        return null;
    }

    public static class Person implements Comparable<Person> {
        private String iId, iFName, iMName, iLName, iEmail, iPhone, iDept, iPos, iSource;
        private Person(String id, String fname, String mname, String lname, String email, String phone, String dept, String pos, String source) {
            iId = id; iSource = source;
            iFName = fname; iMName = mname; iLName = lname;
            if (iMName!=null && iFName!=null && iMName.indexOf(iFName)>=0) iMName = iMName.replaceAll(iFName+" ?", "");
            if (iMName!=null && iLName!=null && iMName.indexOf(iLName)>=0) iMName = iMName.replaceAll(" ?"+iLName, "");
            iEmail = email; iPhone = phone; iDept = dept; iPos = pos;
            //if (iPhone!=null) iPhone = iPhone.replaceAll("\\+? ?\\-?\\(?\\)?","");
        }
        private Person(String id, String cname, String email, String phone, String dept, String pos, String source) {
            iId = id; iSource = source;
            iLName = cname;
            if (iLName!=null && iLName.indexOf(' ')>0) {
                iFName = iLName.substring(0, iLName.indexOf(' ')); iLName = iLName.substring(iLName.indexOf(' ')+1);
            }
            if (iLName!=null && iLName.indexOf(' ')>0) {
                iMName = iLName.substring(0, iLName.lastIndexOf(' ')); iLName = iLName.substring(iLName.lastIndexOf(' ')+1);
            }
            iEmail = email; iPhone = phone; iDept = dept; iPos = pos;
            //if (iPhone!=null) iPhone = iPhone.replaceAll("\\+? ?\\-?\\(?\\)?","");
        }
        public Person(Staff staff) {
            this(translate(staff.getExternalUniqueId(), Source.Staff), 
                 staff.getFirstName(), staff.getMiddleName(), staff.getLastName(),
                 staff.getEmail(), null, staff.getDept(), 
                 (staff.getPositionCode()==null?null:staff.getPositionCode().getPositionType()==null?staff.getPositionCode().getPositionCode():staff.getPositionCode().getPositionType().getLabel()),
                 "Staff");
        }
        public Person(Student student) {
            this(translate(student.getExternalUniqueId(), Source.Student), 
                 student.getFirstName(), student.getMiddleName(), student.getLastName(),
                 student.getEmail(), null, null, 
                 "Student",
                 "Students");
        }
        public Person(EventContact contact) {
            this(translate(contact.getExternalUniqueId(), Source.User), 
                    contact.getFirstName(), contact.getMiddleName(), contact.getLastName(),
                 contact.getEmailAddress(), contact.getPhone(), null, 
                 null,
                 "Event Contacts");
        }
        public Person(TimetableManager manager) {
            this(translate(manager.getExternalUniqueId(), Source.User), 
                    manager.getFirstName(), manager.getMiddleName(), manager.getLastName(),
                    manager.getEmailAddress(), null, null, 
                 (manager.getPrimaryRole()==null?null:manager.getPrimaryRole().getAbbv()),
                 "Timetable Managers");
        }
        public Person(Attributes a) throws NamingException{
            this(translate(getAttribute(a,"uid"), Source.LDAP),
                 Constants.toInitialCase(getAttribute(a,"givenName")),
                 Constants.toInitialCase(getAttribute(a,"cn")),
                 Constants.toInitialCase(getAttribute(a,"sn")),
                 getAttribute(a,ApplicationProperties.getProperty("tmtbl.lookup.ldap.email","mail")),
                 getAttribute(a,ApplicationProperties.getProperty("tmtbl.lookup.ldap.phone","phone,officePhone,homePhone,telephoneNumber")),
                 Constants.toInitialCase(getAttribute(a,ApplicationProperties.getProperty("tmtbl.lookup.ldap.department","department"))),
                 Constants.toInitialCase(getAttribute(a,ApplicationProperties.getProperty("tmtbl.lookup.ldap.position","position,title"))),
                 "Directory");
        }
        public String getId() { return iId; }
        public String getSource() { return iSource; }
        public String getFirstName() { return iFName; }
        public String getMiddleName() { return iMName; }
        public String getLastName() { return iLName; }
        public String getPhone() { return iPhone; }
        public String getEmail() { return iEmail; }
        public String getDepartment() { return iDept; }
        public String getPosition() { return iPos; }
        public int compareTo(Person p) {
            int cmp = (getLastName()==null?"":getLastName()).compareToIgnoreCase(p.getLastName()==null?"":p.getLastName());
            if (cmp!=0) return cmp;
            cmp = (getFirstName()==null?"":getFirstName()).compareToIgnoreCase(p.getFirstName()==null?"":p.getFirstName());
            if (cmp!=0) return cmp;
            cmp = (getMiddleName()==null?"":getMiddleName()).compareToIgnoreCase(p.getMiddleName()==null?"":p.getMiddleName());
            if (cmp!=0) return cmp;
            cmp = getSource().compareToIgnoreCase(p.getSource());
            if (cmp!=0) return cmp;
            return getId().compareTo(p.getId());
        }
        public String toXML() {
            return "<result id=\""+getId()+"\" "+
                (getFirstName()!=null?"fname=\""+getFirstName()+"\" ":"")+
                (getMiddleName()!=null?"mname=\""+getMiddleName()+"\" ":"")+
                (getLastName()!=null?"lname=\""+getLastName()+"\" ":"")+
                (getEmail()!=null?"email=\""+getEmail()+"\" ":"")+
                (getPhone()!=null?"phone=\""+getPhone()+"\" ":"")+
                (getDepartment()!=null?"dept=\""+getDepartment()+"\" ":"")+
                (getPosition()!=null?"pos=\""+getPosition()+"\" ":"")+
                (getSource()!=null?"source=\""+getSource()+"\" ":"")+
                "/>";
        }
    }

}
