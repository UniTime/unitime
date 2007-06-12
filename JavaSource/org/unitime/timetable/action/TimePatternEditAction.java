/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.action;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.TimePatternEditForm;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.ifs.util.CSVFile;

/** 
 * @author Tomas Muller
 */
public class TimePatternEditAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
		TimePatternEditForm myForm = (TimePatternEditForm) form;
		
        // Check Access
        if (!Web.isLoggedIn( request.getSession() )
               || !Web.hasRole(request.getSession(), Roles.getAdminRoles()) ) {
            throw new Exception ("Access Denied.");
        }
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if (request.getParameterValues("depts")!=null) {
        	String[] depts = request.getParameterValues("depts");
        	for (int i=0;i<depts.length;i++)
        		myForm.getDepartmentIds().add(new Long(depts[i]));
        }

        if (op==null) {
            myForm.load(null, null);
        }
        
    	User user = Web.getUser(request.getSession());
    	Long sessionId = Session.getCurrentAcadSession(user).getSessionId();

    	List list = (new DepartmentDAO()).getSession()
					.createCriteria(Department.class)
					.add(Restrictions.eq("session.uniqueId", sessionId))
					.addOrder(Order.asc("deptCode"))
					.list();
    	Vector availableDepts = new Vector();
    	for (Iterator iter = list.iterator();iter.hasNext();) {
    		Department d = (Department) iter.next();
    		availableDepts.add(new LabelValueBean(d.getDeptCode() + "-" + d.getName(), d.getUniqueId().toString()));
    	}
    	request.setAttribute(Department.DEPT_ATTR_NAME, availableDepts);
        
        // Reset Form
        if ("Clear".equals(op)) {
            myForm.load(null, null);
        }
        
        if ("Add Department".equals(op)) {
            ActionMessages errors = new ActionErrors();
			if (myForm.getDepartmentId()==null || myForm.getDepartmentId().longValue()<0)
				errors.add("department", new ActionMessage("errors.generic", "No department selected."));
			else {
				boolean contains = myForm.getDepartmentIds().contains(myForm.getDepartmentId());
				if (contains)
					errors.add("department", new ActionMessage("errors.generic", "Department already present in the list of departments."));
			}
            if(errors.size()>0) {
                saveErrors(request, errors);
            } else {
            	myForm.getDepartmentIds().add(myForm.getDepartmentId());
            }
            myForm.setOp(myForm.getUniqueId().longValue()<0?"Add New":"Update");
            mapping.findForward("showTimePatterns");
        }

        if ("Remove Department".equals(op)) {
            ActionMessages errors = new ActionErrors();
			if (myForm.getDepartmentId()==null || myForm.getDepartmentId().longValue()<0)
				errors.add("department", new ActionMessage("errors.generic", "No department selected."));
			else {
				boolean contains = myForm.getDepartmentIds().contains(myForm.getDepartmentId());
				if (!contains)
					errors.add("department", new ActionMessage("errors.generic", "Department not present in the list of departments."));
			}
            if(errors.size()>0) {
                saveErrors(request, errors);
            } else {
            	myForm.getDepartmentIds().remove(myForm.getDepartmentId());
            }	
            myForm.setOp(myForm.getUniqueId().longValue()<0?"Add New":"Update");
            mapping.findForward("showTimePatterns");
        }
        

        // Add / Update
        if ("Update".equals(op) || "Add New".equals(op)) {
            // Validate input
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                mapping.findForward("showTimePatterns");
            } else {
        		Transaction tx = null;
        		
                try {
                	org.hibernate.Session hibSession = (new TimePatternDAO()).getSession();
                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                		tx = hibSession.beginTransaction();
                	
                	myForm.saveOrUpdate(request, hibSession, sessionId);
                	
        			if (tx!=null) tx.commit();
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        	    	throw e;
        	    }

                myForm.setOp("Update");
            }
        }

        // Edit
        if("Edit".equals(op)) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("key", new ActionMessage("errors.invalid", "Unique Id : " + id));
                saveErrors(request, errors);
                return mapping.findForward("showTimePatterns");
            } else {
            	TimePattern pattern = (new TimePatternDAO()).get(new Long(id));
            	
                if(pattern==null) {
                    errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
                    saveErrors(request, errors);
                    return mapping.findForward("showTimePatterns");
                } else {
                	myForm.load(pattern, sessionId);
                }
            }
        }

        // Delete 
        if("Delete".equals(op)) {
    		Transaction tx = null;
    		
            try {
            	org.hibernate.Session hibSession = (new TimePatternDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	myForm.delete(hibSession, request);
            	
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

    	    myForm.load(null, null);
            myForm.setOp("Add New");
        }
        
        if ("Exact Times".equals(op)) {
    		Transaction tx = null;
    		
    		try {
            	org.hibernate.Session hibSession = (new TimePatternDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	Session session = Session.getCurrentAcadSession(user);

            	TimePattern tp = TimePattern.findByName(session, "Exact Time");
            	
            	List timePrefs = 
            			hibSession.
                		createQuery("select distinct p from TimePref as p inner join p.timePattern as tp where tp.uniqueId=:uniqueid").
        				setLong("uniqueid",tp.getUniqueId().longValue()).
                		list();
            	
            	CSVFile csv = new CSVFile();
            	csv.setHeader(
            			new CSVFile.CSVField[] {
            					new CSVFile.CSVField("Class"),
            					new CSVFile.CSVField("Pattern"),
            					new CSVFile.CSVField("Time")
            			});
            	
            	for (Iterator i=timePrefs.iterator();i.hasNext();) {
            		TimePref tpref = (TimePref)i.next();
            		if (!(tpref.getOwner() instanceof Class_)) continue;
            		Class_ clazz = (Class_)tpref.getOwner();
            		if (!clazz.getSessionId().equals(session.getUniqueId())) continue;
            		int dayCode = tpref.getTimePatternModel().getExactDays();
            		String name = "";
            		int nrDays = 0;
            		for (int j=0;j<Constants.DAY_CODES.length;j++) {
            			if ((Constants.DAY_CODES[j]&dayCode)!=0) { 
            				name += Constants.DAY_NAMES_SHORT[j];
            				nrDays ++;
            			}
            		}
            		name += " ";
            		int startSlot = tpref.getTimePatternModel().getExactStartSlot();
            		int startTime = Constants.FIRST_SLOT_TIME_MIN + (Constants.SLOT_LENGTH_MIN*startSlot);
                    int startHour = startTime / 60;
                    int startMinute = startTime % 60;
                    name+= (startHour>12?startHour-12:startHour)+":"+(startMinute<10?"0":"")+startMinute+(startHour>=12?"p":"a");
            		int minPerMtg = (nrDays==0?0:clazz.getSchedulingSubpart().getMinutesPerWk().intValue()/nrDays);
            		if (nrDays==0)
	                    Debug.warning("Class "+clazz.getClassLabel()+" has zero number of days.");
            		
                	csv.addLine(
                			new CSVFile.CSVField[] {
	            					new CSVFile.CSVField(clazz.getClassLabel()),
	            					new CSVFile.CSVField(nrDays+" x "+minPerMtg),
	            					new CSVFile.CSVField(name)
                			});
            	}
            	
            	File file = ApplicationProperties.getTempFile("exact", "csv");
           		csv.save(file);
           		request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

    	    myForm.load(null, null);
            myForm.setOp("Add New");
        }
        
        
        if ("Assign Departments".equals(op)) {
    		Transaction tx = null;
    		
        	PrintWriter out = null;
        	HashSet refresh = new HashSet();

        	try {
            	org.hibernate.Session hibSession = (new TimePatternDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	File file = ApplicationProperties.getTempFile("assigndept", "txt");
            	out = new PrintWriter(new FileWriter(file));
            	
            	Session session = Session.getCurrentAcadSession(user);
            	TreeSet patterns = new TreeSet(TimePattern.findAll(request,null));

            	for (Iterator i=patterns.iterator();i.hasNext();) {
            		TimePattern tp = (TimePattern)i.next();
            		
            		if (tp.getType().intValue()!=TimePattern.sTypeExtended) continue;
            		
            		out.println("Checking "+tp.getName()+" ...");
            		
                	List timePrefs = 
            			hibSession.
                		createQuery("select distinct p from TimePref as p inner join p.timePattern as tp where tp.uniqueId=:uniqueid").
        				setInteger("uniqueid",tp.getUniqueId().intValue()).
                		list();
            		
            		HashSet depts = new HashSet();
            		
            		for (Iterator j=timePrefs.iterator();j.hasNext();) {
            			TimePref timePref = (TimePref)j.next();
            			if (timePref.getOwner() instanceof Class_) {
            				Class_ c = (Class_)timePref.getOwner();
            				if (!c.getSession().getUniqueId().equals(session.getUniqueId())) continue;
            				depts.add(c.getManagingDept());
            			} else if (timePref.getOwner() instanceof SchedulingSubpart) {
            				SchedulingSubpart s = (SchedulingSubpart)timePref.getOwner();
            				if (!s.getSession().getUniqueId().equals(session.getUniqueId())) continue;
            				depts.add(s.getManagingDept());
            			}
            		}
            		
            		out.println("  -- departments: "+depts);
            		
            		boolean added = false;
            		for (Iterator j=depts.iterator();j.hasNext();) {
            			Department d = (Department)j.next();
            			if (d.isExternalManager().booleanValue()) {
            				/*
            				if (tp.getDepartments().contains(d)) {
            					tp.getDepartments().remove(d);
            					d.getTimePatterns().remove(tp);
            					hibSession.saveOrUpdate(d);
                				out.println("    -- department "+d+" removed from "+tp.getName());
                				added=true;
            				}
            				*/
            				continue;
            			}
            			if (!tp.getDepartments().contains(d)) {
            				tp.getDepartments().add(d);
            				d.getTimePatterns().add(tp);
            				hibSession.saveOrUpdate(d);
            				out.println("    -- department "+d+" added to "+tp.getName());
            				added=true;
            			}
            		}
            		if (added) {
            			hibSession.saveOrUpdate(tp);
            			refresh.add(tp);
            		}
            	}

            	out.flush(); out.close(); out = null;
            	request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        	
    			tx.commit();
    			
    			for (Iterator i=refresh.iterator();i.hasNext();) {
    				hibSession.refresh(i.next());
    			}
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    } finally {
    	    	if (out!=null) out.close();
    	    }
    	    
    	    myForm.load(null, null);
            myForm.setOp("Add New");
        }

        if ("Export CSV".equals(op)) {
    		Transaction tx = null;
    		
    		try {
            	org.hibernate.Session hibSession = (new TimePatternDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	Session session = Session.getCurrentAcadSession(user);
            	
            	CSVFile csv = new CSVFile();
            	csv.setHeader(
            			new CSVFile.CSVField[] {
            					new CSVFile.CSVField("Name"),
            					new CSVFile.CSVField("Type"),
            					new CSVFile.CSVField("Visible"),
                                new CSVFile.CSVField("Used"),
            					new CSVFile.CSVField("NbrMtgs"),
            					new CSVFile.CSVField("MinPerMtg"),
            					new CSVFile.CSVField("SlotsPerMtg"),
                                new CSVFile.CSVField("BreakTime"),
            					new CSVFile.CSVField("Days"),
            					new CSVFile.CSVField("Times"),
            					new CSVFile.CSVField("Departments"),
            					new CSVFile.CSVField("Classes")
            			});
            	
            	TreeSet patterns = new TreeSet(TimePattern.findAll(request,null));

            	for (Iterator i=patterns.iterator();i.hasNext();) {
            		TimePattern tp = (TimePattern)i.next();
            		
            		String deptStr = "";
                	TreeSet depts = new TreeSet(tp.getDepartments()); 
                	for (Iterator j=depts.iterator();j.hasNext();) {
                		Department d = (Department)j.next();
                		if (!d.getSessionId().equals(session.getUniqueId())) continue;
                		if (deptStr.length()>0) { deptStr += ", "; }
                		deptStr += d.getShortLabel().trim();
                	}
                	
                	String classStr = "";
                	if (tp.getType().intValue()!=TimePattern.sTypeStandard) {
                    	List timePrefs = 
                			hibSession.
                    		createQuery("select distinct p.owner from TimePref as p inner join p.timePattern as tp where tp.uniqueId=:uniqueid").
            				setInteger("uniqueid",tp.getUniqueId().intValue()).
                    		list();
	            		
	            		TreeSet allOwners = new TreeSet();
	            		
	            		for (Iterator j=timePrefs.iterator();j.hasNext();) {
	            			Object owner = j.next();
	            			if (owner instanceof Class_) {
	            				Class_ c = (Class_)owner;
	            				if (!c.getSession().getUniqueId().equals(session.getUniqueId())) continue;
                                allOwners.add(c.getClassLabel());
	            			} else if (owner instanceof SchedulingSubpart) {
	            				SchedulingSubpart s = (SchedulingSubpart)owner;
	            				if (!s.getSession().getUniqueId().equals(session.getUniqueId())) continue;
                                allOwners.add(s.getSchedulingSubpartLabel());
	            			}
	            		}
	            		
                        if (allOwners.isEmpty()) {
                            classStr += "not used";
                        } else {
                            int idx = 0;
                            classStr += allOwners.size()+" / "; 
                            for (Iterator j=allOwners.iterator();j.hasNext();idx++) {
                                if (idx==20) {
                                    classStr += "..."; break;
                                }
                                classStr += (String)j.next(); 
                                if (j.hasNext()) classStr += ", ";
                            }
                        }
                	}
                	
                	csv.addLine(
                			new CSVFile.CSVField[] {
	            					new CSVFile.CSVField(tp.getName()),
	            					new CSVFile.CSVField(TimePattern.sTypes[tp.getType().intValue()]),
	            					new CSVFile.CSVField(tp.isVisible().booleanValue()?"Y":"N"),
                                    new CSVFile.CSVField(tp.isEditable()?"N":"Y"),
	            					new CSVFile.CSVField(tp.getNrMeetings()),
	            					new CSVFile.CSVField(tp.getMinPerMtg()),
	            					new CSVFile.CSVField(tp.getSlotsPerMtg()),
                                    new CSVFile.CSVField(tp.getBreakTime()),
	            					new CSVFile.CSVField(TimePatternEditForm.dayCodes2str(tp.getDays(),", ")),
	            					new CSVFile.CSVField(TimePatternEditForm.startSlots2str(tp.getTimes(),", ")),
	            					new CSVFile.CSVField(deptStr),
	            					new CSVFile.CSVField(classStr)
	            			});
            	}
            	
            	File file = ApplicationProperties.getTempFile("timePatterns", "csv");
           		csv.save(file);
           		request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }
    	    
    	    myForm.load(null, null);
            myForm.setOp("Add New");
        }

        // Read all existing settings and store in request
        getTimePatterns(request, sessionId);    
        
        String example = myForm.getExample();
        if (example!=null) {
        	request.setAttribute("TimePatterns.example", example);
        }
        
        return mapping.findForward("showTimePatterns");
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}
	
    private void getTimePatterns(HttpServletRequest request, Long sessionId) throws Exception {
		WebTable.setOrder(request.getSession(),"timePatterns.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = new WebTable( 11,
			    "Time Patterns", "timePatternEdit.do?ord=%%",
			    new String[] {"Name", "Type", "Visible", "Used", "NrMtgs", "MinPerMtg", "SlotPerMtg", "Break Time", "Days", "Times", "Departments"},
			    new String[] {"left", "left", "left","left", "left", "left","left", "left", "left", "left", "left"},
			    null );
        
        Vector patterns = TimePattern.findAll(request,null);
		if(patterns.isEmpty()) {
		    webTable.addLine(null, new String[] {"No time pattern defined for this academic initiative and term."}, null, null );			    
		}
		
    	User user = Web.getUser(request.getSession());
    	Session session = Session.getCurrentAcadSession(user);
		Set used = TimePattern.findAllUsed(session);

        for (Enumeration e=patterns.elements();e.hasMoreElements();) {
        	TimePattern pattern = (TimePattern)e.nextElement();
        	String onClick = "onClick=\"document.location='timePatternEdit.do?op=Edit&id=" + pattern.getUniqueId() + "';\"";
        	String deptStr = "";
        	String deptCmp = "";
        	for (Iterator i=pattern.getDepartments(sessionId).iterator();i.hasNext();) {
        		Department d = (Department)i.next();
        		deptStr += d.getManagingDeptAbbv().trim();
        		deptCmp += d.getDeptCode();
        		if (i.hasNext()) { deptStr += ", "; deptCmp += ","; }
        	}
        	boolean isUsed = used.contains(pattern);
        	webTable.addLine(onClick, new String[] {
        			pattern.getName().replaceAll(" ","&nbsp;"),
        			TimePattern.sTypes[pattern.getType().intValue()].replaceAll(" ","&nbsp;"),
        			pattern.isVisible().toString(),
        			(new Boolean(isUsed)).toString(),
        			pattern.getNrMeetings().toString(),
        			pattern.getMinPerMtg().toString(),
        			pattern.getSlotsPerMtg().toString(),
        			pattern.getBreakTime().toString(),
        			TimePatternEditForm.dayCodes2str(pattern.getDays(),", "),
        			TimePatternEditForm.startSlots2str(pattern.getTimes(),", "),
        			deptStr
        		},new Comparable[] {
        			pattern.getName(),
        			pattern.getType(),
        			(pattern.isVisible().booleanValue()?"1":"0"),
        			(isUsed?"0":"1"),
        			pattern.getNrMeetings(),
        			pattern.getMinPerMtg(),
        			pattern.getSlotsPerMtg(),
        			pattern.getBreakTime(),
        			TimePatternEditForm.dayCodes2str(pattern.getDays(),", "),
        			TimePatternEditForm.startSlots2str(pattern.getTimes(),", "),
        			deptCmp
        		});
        }
        
	    request.setAttribute("TimePatterns.table", webTable.printTable(WebTable.getOrder(request.getSession(),"timePatterns.ord")));
    }	
}

