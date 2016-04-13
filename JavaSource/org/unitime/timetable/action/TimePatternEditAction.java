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
package org.unitime.timetable.action;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import org.cpsolver.ifs.util.CSVFile;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.TimePatternEditForm;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.duration.DurationModel;
import org.unitime.timetable.webutil.Navigation;


/** 
 * @author Tomas Muller
 */
@Service("/timePatternEdit")
public class TimePatternEditAction extends Action {
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Autowired SessionContext sessionContext;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
		TimePatternEditForm myForm = (TimePatternEditForm) form;
		
        // Check Access
		sessionContext.checkPermission(Right.TimePatterns);
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if (request.getParameterValues("depts")!=null) {
        	String[] depts = request.getParameterValues("depts");
        	for (int i=0;i<depts.length;i++)
        		myForm.getDepartmentIds().add(new Long(depts[i]));
        }

        if (op==null) {
            myForm.load(null, null);
            myForm.setOp("List");
        }
        
    	Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();

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
        if ("Back".equals(op)) {
            if (myForm.getUniqueId()!=null)
                request.setAttribute("hash", myForm.getUniqueId());
            myForm.load(null, null);
            myForm.setOp("List");
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
            myForm.setOp(myForm.getUniqueId().longValue()<0?"Save":"Update");
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
            myForm.setOp(myForm.getUniqueId().longValue()<0?"Save":"Update");
        }
        

        // Add / Update
        if ("Update".equals(op) || "Save".equals(op) || "Previous".equals(op) || "Next".equals(op)) {
            // Validate input
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
            } else {
        		Transaction tx = null;
        		
                try {
                	org.hibernate.Session hibSession = (new TimePatternDAO()).getSession();
                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                		tx = hibSession.beginTransaction();
                	
                	myForm.saveOrUpdate(sessionContext, hibSession);
                	
        			if (tx!=null) tx.commit();
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        	    	throw e;
        	    }

                if ("Next".equals(op) && myForm.getNextId() != null) {
                	response.sendRedirect(response.encodeURL("timePatternEdit.do?op=Edit&id="+myForm.getNextId()));
                } else if ("Previous".equals(op) && myForm.getPreviousId() != null) {
                	response.sendRedirect(response.encodeURL("timePatternEdit.do?op=Edit&id="+myForm.getPreviousId()));
                } else {
                	if (myForm.getUniqueId()!=null)
                		request.setAttribute("hash", myForm.getUniqueId());
                	myForm.setOp("List");
                }
            }
        }

        // Edit
        if("Edit".equals(op)) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("key", new ActionMessage("errors.invalid", "Unique Id : " + id));
                saveErrors(request, errors);
                return mapping.findForward("list");
            } else {
            	TimePattern pattern = (new TimePatternDAO()).get(new Long(id));
            	myForm.setPreviousId(Navigation.getPrevious(sessionContext, Navigation.sInstructionalOfferingLevel, new Long(id)));
            	myForm.setNextId(Navigation.getNext(sessionContext, Navigation.sInstructionalOfferingLevel, new Long(id)));
                if(pattern==null) {
                    errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
                    saveErrors(request, errors);
                    return mapping.findForward("list");
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
            	
            	myForm.delete(sessionContext, hibSession);
            	
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

    	    myForm.load(null, null);
    	    myForm.setOp("List");
        }
        
        if ("Exact Times CSV".equals(op)) {
    		Transaction tx = null;
    		
    		try {
            	org.hibernate.Session hibSession = (new TimePatternDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	TimePattern tp = TimePattern.findExactTime(sessionId);
            	
            	if (tp == null) {
                    myForm.load(null, null);
                    myForm.setOp("List");
                    getTimePatterns(request, sessionId);
                    
            		ActionMessages errors = new ActionMessages();
                    errors.add("key", new ActionMessage("errors.generic", "There is no Exact Time time pattern defined."));
                    saveErrors(request, errors);

                    return mapping.findForward("list");
            	}
            	
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
            		if (!clazz.getSessionId().equals(sessionId)) continue;
            		int dayCode = tpref.getTimePatternModel().getExactDays();
            		String name = "";
            		int nrDays = 0;
            		for (int j=0;j<Constants.DAY_CODES.length;j++) {
            			if ((Constants.DAY_CODES[j]&dayCode)!=0) { 
            				name += CONSTANTS.shortDays()[j];
            				nrDays ++;
            			}
            		}
            		name += " ";
            		int startSlot = tpref.getTimePatternModel().getExactStartSlot();
                    name+= Constants.toTime(Constants.FIRST_SLOT_TIME_MIN + (Constants.SLOT_LENGTH_MIN*startSlot));
                    DurationModel dm = clazz.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
            		int minPerMtg = (nrDays==0?0:dm.getExactTimeMinutesPerMeeting(clazz.getSchedulingSubpart().getMinutesPerWk(), clazz.effectiveDatePattern(), dayCode)); 
            		if (nrDays==0)
	                    Debug.warning("Class "+clazz.getClassLabel()+" has zero number of days.");
            		
                	csv.addLine(
                			new CSVFile.CSVField[] {
	            					new CSVFile.CSVField(clazz.getClassLabel()),
	            					new CSVFile.CSVField(nrDays+" x "+minPerMtg),
	            					new CSVFile.CSVField(name)
                			});
            	}
            	
    			tx.commit();
            	ExportUtils.exportCSV(csv, response, "exact");
            	return null;
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }
        }
        
        if ("Generate SQL".equals(op)) {
            PrintWriter out = ExportUtils.getPlainTextWriter(response, "tp.sql");
            try {
                TreeSet patterns = new TreeSet(TimePattern.findAll(sessionId,null));
                
                boolean mysql = false;

                int line = 0;
                if (mysql) out.println("INSERT INTO `timetable`.`time_pattern`(`uniqueid`, `name`, `mins_pmt`, `slots_pmt`, `nr_mtgs`, `visible`, `type`, `break_time`, `session_id`)");
                else out.println("prompt Loading TIME_PATTERN...");
                for (Iterator i=patterns.iterator();i.hasNext();) {
                    TimePattern tp = (TimePattern)i.next();
                    
                    if (tp.getType()==TimePattern.sTypeExtended) continue;
                    if (!tp.isVisible()) continue;
                    
                    if (mysql) {
                        if (line==0) out.print("VALUES"); else out.println(",");
                    
                        out.print(" ("+tp.getUniqueId()+", '"+tp.getName()+"', "+tp.getMinPerMtg()+", "+
                            tp.getSlotsPerMtg()+", "+tp.getNrMeetings()+", "+(tp.isVisible()?"1":"0")+", "+
                            tp.getType()+", "+tp.getBreakTime()+", "+sessionId+")");
                    } else {
                        out.println("insert into TIME_PATTERN (UNIQUEID, NAME, MINS_PMT, SLOTS_PMT, NR_MTGS, VISIBLE, TYPE, BREAK_TIME, SESSION_ID)");
                        out.println("values ("+tp.getUniqueId()+", '"+tp.getName()+"', "+tp.getMinPerMtg()+", "+
                                tp.getSlotsPerMtg()+", "+tp.getNrMeetings()+", "+(tp.isVisible()?"1":"0")+", "+
                                tp.getType()+", "+tp.getBreakTime()+", "+sessionId+");");
                    }
                    
                    line++;
                }
                if (mysql) {
                    out.println(";");
                } else {
                    out.println("commit;");
                    out.println("prompt "+line+" records loaded");
                }
                
                out.println();

                line = 0;
                if (mysql) {
                    out.println("INSERT INTO `timetable`.`time_pattern_days`(`uniqueid`, `day_code`, `time_pattern_id`)");
                } else {
                    out.println("prompt Loading TIME_PATTERN_DAYS...");
                }
                for (Iterator i=patterns.iterator();i.hasNext();) {
                    TimePattern tp = (TimePattern)i.next();
                    
                    if (tp.getType()==TimePattern.sTypeExtended) continue;
                    if (!tp.isVisible()) continue;
                    
                    for (Iterator j=tp.getDays().iterator();j.hasNext();) {
                        TimePatternDays d = (TimePatternDays)j.next();
                        
                        if (mysql) {
                            if (line==0) out.print("VALUES"); else out.println(",");
                            out.print(" ("+d.getUniqueId()+", "+d.getDayCode()+", "+tp.getUniqueId()+")");
                        } else {
                            out.println("insert into TIME_PATTERN_DAYS (UNIQUEID, DAY_CODE, TIME_PATTERN_ID)");
                            out.println("values ("+d.getUniqueId()+", "+d.getDayCode()+", "+tp.getUniqueId()+");");
                        }
                                
                        line++;
                        
                    }
                }
                if (mysql) {
                    out.println(";");
                } else {
                    out.println("commit;");
                    out.println("prompt "+line+" records loaded");
                }
                out.println();
                
                line = 0;
                if (mysql) {
                    out.println("INSERT INTO `timetable`.`time_pattern_time`(`uniqueid`, `start_slot`, `time_pattern_id`)");
                } else {
                    out.println("prompt Loading TIME_PATTERN_TIME...");
                }
                for (Iterator i=patterns.iterator();i.hasNext();) {
                    TimePattern tp = (TimePattern)i.next();
                    
                    if (tp.getType()==TimePattern.sTypeExtended) continue;
                    if (!tp.isVisible()) continue;
                    
                    for (Iterator j=tp.getTimes().iterator();j.hasNext();) {
                        TimePatternTime t = (TimePatternTime)j.next();
                        
                        if (mysql) {
                            if (line==0) out.print("VALUES"); else out.println(",");
                            out.print(" ("+t.getUniqueId()+", "+t.getStartSlot()+", "+tp.getUniqueId()+")");
                        } else {
                            out.println("insert into TIME_PATTERN_TIME (UNIQUEID, START_SLOT, TIME_PATTERN_ID)");
                            out.println("values ("+t.getUniqueId()+", "+t.getStartSlot()+", "+tp.getUniqueId()+");");
                        }
                                
                        line++;
                    }
                }
                if (mysql) {
                    out.println(";");
                } else {
                    out.println("commit;");
                    out.println("prompt "+line+" records loaded");
                }
                
                out.flush(); out.close(); out = null;
                return null;
            } catch (Exception e) {
                throw e;
            } finally {
                if (out!=null) out.close();
            }
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
            	
            	TreeSet patterns = new TreeSet(TimePattern.findAll(sessionId,null));

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
            				if (!c.getSession().getUniqueId().equals(sessionId)) continue;
            				depts.add(c.getManagingDept());
            			} else if (timePref.getOwner() instanceof SchedulingSubpart) {
            				SchedulingSubpart s = (SchedulingSubpart)timePref.getOwner();
            				if (!s.getSession().getUniqueId().equals(sessionId)) continue;
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
    	    myForm.setOp("List");
        }

        if ("Export CSV".equals(op)) {
    		Transaction tx = null;
    		
    		try {
            	org.hibernate.Session hibSession = (new TimePatternDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
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
            	
            	TreeSet patterns = new TreeSet(TimePattern.findAll(sessionId,null));

            	for (Iterator i=patterns.iterator();i.hasNext();) {
            		TimePattern tp = (TimePattern)i.next();
            		
            		String deptStr = "";
                	TreeSet depts = new TreeSet(tp.getDepartments()); 
                	for (Iterator j=depts.iterator();j.hasNext();) {
                		Department d = (Department)j.next();
                		if (!d.getSessionId().equals(sessionId)) continue;
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
	            				if (!c.getSession().getUniqueId().equals(sessionId)) continue;
                                allOwners.add(c.getClassLabel());
	            			} else if (owner instanceof SchedulingSubpart) {
	            				SchedulingSubpart s = (SchedulingSubpart)owner;
	            				if (!s.getSession().getUniqueId().equals(sessionId)) continue;
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
            	
    			tx.commit();
    			
    			ExportUtils.exportCSV(csv, response, "timePatterns");
    			return null;
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }
        }

        if ("Add Time Pattern".equals(op)) {
            myForm.load(null, null);
            myForm.setOp("Save");
        }

        if ("List".equals(myForm.getOp())) {
            // Read all existing settings and store in request
            getTimePatterns(request, sessionId);
            return mapping.findForward("list");
        }

        String example = myForm.getExample();
        if (example!=null) {
            request.setAttribute("TimePatterns.example", example);
        }
        return mapping.findForward(myForm.getUniqueId().longValue()<0?"add":"edit");
        
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}
	
    private void getTimePatterns(HttpServletRequest request, Long sessionId) throws Exception {
		WebTable.setOrder(sessionContext,"timePatterns.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = new WebTable( 10,
			    null, "timePatternEdit.do?ord=%%",
			    new String[] {"Name", "Type", "Used", "NrMtgs", "MinPerMtg", "SlotsPerMtg", "Break Time", "Days", "Times", "Departments"},
			    new String[] {"left", "left", "left", "left", "left","left", "left", "left", "left", "left"},
			    null );
        
        List<TimePattern> patterns = TimePattern.findAll(sessionId,null);
		if(patterns.isEmpty()) {
		    webTable.addLine(null, new String[] {"No time pattern defined for this academic initiative and term."}, null, null );			    
		}
		
		Set used = TimePattern.findAllUsed(sessionId);

        for (TimePattern pattern: patterns) {
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
        	        (pattern.isVisible()?"":"<font color='grey'>")+
        	            pattern.getName().replaceAll(" ","&nbsp;")+
        	        (pattern.isVisible()?"":"</font>"),
        	        (pattern.isVisible()?"":"<font color='gray'>")+
        	            TimePattern.sTypes[pattern.getType().intValue()].replaceAll(" ","&nbsp;")+
        	        (pattern.isVisible()?"":"</font>"),
        			(isUsed?"<IMG border='0' title='This time pattern is being used.' alt='Default' align='absmiddle' src='images/accept.png'>":""),
        			(pattern.isVisible()?"":"<font color='gray'>")+pattern.getNrMeetings().toString()+(pattern.isVisible()?"":"</font>"),
        			(pattern.isVisible()?"":"<font color='gray'>")+pattern.getMinPerMtg().toString()+(pattern.isVisible()?"":"</font>"),
        			(pattern.isVisible()?"":"<font color='gray'>")+pattern.getSlotsPerMtg().toString()+(pattern.isVisible()?"":"</font>"),
        			(pattern.isVisible()?"":"<font color='gray'>")+pattern.getBreakTime().toString()+(pattern.isVisible()?"":"</font>"),
        			(pattern.isVisible()?"":"<font color='gray'>")+TimePatternEditForm.dayCodes2str(pattern.getDays(),", ")+(pattern.isVisible()?"":"</font>"),
        			(pattern.isVisible()?"":"<font color='gray'>")+TimePatternEditForm.startSlots2str(pattern.getTimes(),", ")+(pattern.isVisible()?"":"</font>"),
        			(pattern.isVisible()?"":"<font color='gray'>")+deptStr+(pattern.isVisible()?"":"</font>")
        		},new Comparable[] {
        			pattern.getName(),
        			pattern.getType(),
        			(isUsed?"0":"1"),
        			pattern.getNrMeetings(),
        			pattern.getMinPerMtg(),
        			pattern.getSlotsPerMtg(),
        			pattern.getBreakTime(),
        			TimePatternEditForm.dayCodes2str(pattern.getDays(),", "),
        			TimePatternEditForm.startSlots2str(pattern.getTimes(),", "),
        			deptCmp
        		}, pattern.getUniqueId().toString());
        }
        
	    request.setAttribute("TimePatterns.table", webTable.printTable(WebTable.getOrder(sessionContext,"timePatterns.ord")));
	    
	    List<Long> ids = new ArrayList<Long>();
	    for (Enumeration<WebTableLine> e = webTable.getLines().elements(); e.hasMoreElements(); ) {
	    	WebTableLine line = e.nextElement();
	    	if (line.getUniqueId() != null)
	    		ids.add(Long.parseLong(line.getUniqueId()));
	    }
	    Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, ids);
    }	
}

