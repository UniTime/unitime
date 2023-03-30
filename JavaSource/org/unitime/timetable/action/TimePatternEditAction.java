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

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.cpsolver.ifs.util.CSVFile;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.TimePatternEditForm;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePattern.TimePatternType;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.IdValue;
import org.unitime.timetable.util.duration.DurationModel;
import org.unitime.timetable.webutil.Navigation;


/** 
 * @author Tomas Muller
 */
@Action(value = "timePatternEdit", results = {
		@Result(name = "list", type = "tiles", location = "timePatternList.tiles"),
		@Result(name = "add", type = "tiles", location = "timePatternAdd.tiles"),
		@Result(name = "edit", type = "tiles", location = "timePatternEdit.tiles")
	})
@TilesDefinitions({
	@TilesDefinition(name = "timePatternList.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Time Patterns"),
			@TilesPutAttribute(name = "body", value = "/admin/timePatterns.jsp")
		}),
	@TilesDefinition(name = "timePatternAdd.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Add Time Pattern"),
			@TilesPutAttribute(name = "body", value = "/admin/timePatterns.jsp")
		}),
	@TilesDefinition(name = "timePatternEdit.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Edit Time Pattern"),
			@TilesPutAttribute(name = "body", value = "/admin/timePatterns.jsp")
		})
})
public class TimePatternEditAction extends UniTimeAction<TimePatternEditForm> {
	private static final long serialVersionUID = 4079082627336504012L;
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private Long id;
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	@Override
	public String execute() throws Exception {
		if (form == null)
			form = new TimePatternEditForm();
		
        // Check Access
		sessionContext.checkPermission(Right.TimePatterns);
        
        // Read operation to be performed
		if (op == null) op = form.getOp();

        if (op==null) {
            form.reset();
        }
        
    	Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();

    	List<Department> list = DepartmentDAO.getInstance().getSession()
    			.createQuery("from Department where session.uniqueId = :sessionId order by deptCode", Department.class)
    			.setParameter("sessionId", sessionId)
    			.list();
    	List<IdValue> availableDepts = new ArrayList<IdValue>();
    	for (Department d: list) {
    		availableDepts.add(new IdValue(d.getUniqueId(), d.getLabel()));
    	}
    	request.setAttribute(Department.DEPT_ATTR_NAME, availableDepts);
        
        // Reset Form
        if (MSG.actionBackToTimePatterns().equals(op)) {
            if (form.getUniqueId()!=null)
                request.setAttribute("hash", form.getUniqueId());
            form.reset();
        }
        
        if (MSG.actionAddDepartment().equals(op)) {
			if (form.getDepartmentId()==null || form.getDepartmentId() < 0)
				addFieldError("form.department", MSG.errorNoDepartmentSelected());
			else {
				boolean contains = form.getDepartmentIds().contains(form.getDepartmentId());
				if (contains)
					addFieldError("form.department", MSG.errorDepartmentAlreadyListed());
			}
            if (!hasFieldErrors())
            	form.getDepartmentIds().add(form.getDepartmentId());
            form.setOp(form.getUniqueId().longValue() < 0 ? MSG.actionSaveTimePattern() : MSG.actionUpdateTimePattern());
        }

        if (MSG.actionRemoveDepartment().equals(op)) {
			if (form.getDepartmentId()==null || form.getDepartmentId() < 0)
				addFieldError("form.department", MSG.errorNoDepartmentSelected());
			else {
				boolean contains = form.getDepartmentIds().contains(form.getDepartmentId());
				if (!contains)
					addFieldError("form.department", MSG.errorDepartmentNotListed());
			}
			if (!hasFieldErrors())
            	form.getDepartmentIds().remove(form.getDepartmentId());
        	form.setOp(form.getUniqueId().longValue() < 0 ? MSG.actionSaveTimePattern() : MSG.actionUpdateTimePattern());
        }
        

        // Add / Update
        if (MSG.actionSaveTimePattern().equals(op) || MSG.actionUpdateTimePattern().equals(op) ||
        		MSG.actionPreviousTimePattern().equals(op) || MSG.actionNextTimePattern().equals(op)) {
            // Validate input
        	form.validate(this);
        	if (hasFieldErrors()) {
            	form.setOp(form.getUniqueId() < 0 ? MSG.actionSaveDatePattern() : MSG.actionUpdateDatePattern());
            } else {
        		Transaction tx = null;
        		
                try {
                	org.hibernate.Session hibSession = (new TimePatternDAO()).getSession();
                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                		tx = hibSession.beginTransaction();
                	
                	form.saveOrUpdate(sessionContext, hibSession);
                	
        			if (tx!=null) tx.commit();
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        	    	throw e;
        	    }

                if (MSG.actionNextTimePattern().equals(op) && form.getNextId() != null) {
                	response.sendRedirect(response.encodeURL("timePatternEdit.action?op=Edit&id="+form.getNextId()));
                } else if (MSG.actionPreviousTimePattern().equals(op) && form.getPreviousId() != null) {
                	response.sendRedirect(response.encodeURL("timePatternEdit.action?op=Edit&id="+form.getPreviousId()));
                } else {
                	if (form.getUniqueId()!=null)
                		request.setAttribute("hash", form.getUniqueId());
                	form.reset();
                }
            }
        }

        // Edit
        if ("Edit".equals(op)) {
        	if (id == null && form.getUniqueId() != null) id = form.getUniqueId();
            if (id==null) {
            	addFieldError("form.uniqueId", MSG.errorRequiredField("Id"));
                return "list";
            } else {
            	TimePattern pattern = (new TimePatternDAO()).get(Long.valueOf(id));
            	form.setPreviousId(Navigation.getPrevious(sessionContext, Navigation.sInstructionalOfferingLevel, Long.valueOf(id)));
            	form.setNextId(Navigation.getNext(sessionContext, Navigation.sInstructionalOfferingLevel, Long.valueOf(id)));
                if(pattern==null) {
                	addFieldError("form.uniqueId", MSG.errorDoesNotExists(MSG.columnTimePattern()));
                    return "list";
                } else {
                	form.load(pattern, sessionId);
                }
            }
        }

        // Delete 
        if (MSG.actionDeleteTimePattern().equals(op)) {
    		Transaction tx = null;
    		
            try {
            	org.hibernate.Session hibSession = (new TimePatternDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	form.delete(sessionContext, hibSession);
            	
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

    	    form.reset();
        }
        
        if (MSG.actionExactTimesCSV().equals(op)) {
    		Transaction tx = null;
    		
    		try {
            	org.hibernate.Session hibSession = (new TimePatternDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	TimePattern tp = TimePattern.findExactTime(sessionId);
            	
            	if (tp == null) {
                    form.reset();
                    getTimePatterns(sessionId);
                    
                    addFieldError("form.uniqueId", MSG.errorNoExactTimePatternDefined());
                    return "list";
            	}
            	
            	List timePrefs = 
            			hibSession.
                		createQuery("select distinct p from TimePref as p inner join p.timePattern as tp where tp.uniqueId=:uniqueid").
        				setParameter("uniqueid", tp.getUniqueId(), org.hibernate.type.LongType.INSTANCE).
                		list();
            	
            	CSVFile csv = new CSVFile();
            	csv.setHeader(
            			new CSVFile.CSVField[] {
            					new CSVFile.CSVField(MSG.columnClass()),
            					new CSVFile.CSVField(MSG.columnTimePattern()),
            					new CSVFile.CSVField(MSG.columnAssignedTime())
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
                    
                    if (tp.isExtended()) continue;
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
                    
                    if (tp.isExtended()) continue;
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
                    
                    if (tp.isExtended()) continue;
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
        
        
        if (MSG.actionAssingDepartmentsToTimePatterns().equals(op)) {
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
            		
            		if (!tp.isExtended()) continue;
            		
            		out.println("Checking "+tp.getName()+" ...");
            		
                	List timePrefs = 
            			hibSession.
                		createQuery("select distinct p from TimePref as p inner join p.timePattern as tp where tp.uniqueId=:uniqueid").
        				setParameter("uniqueid", tp.getUniqueId(), org.hibernate.type.IntegerType.INSTANCE).
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
    	    
    	    form.reset();
        }

        if (MSG.actionExportCsv().equals(op)) {
    		Transaction tx = null;
    		
    		try {
            	org.hibernate.Session hibSession = (new TimePatternDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	CSVFile csv = new CSVFile();
            	csv.setHeader(
            			new CSVFile.CSVField[] {
            					new CSVFile.CSVField(MSG.columnTimePatternName()),
            					new CSVFile.CSVField(MSG.columnTimePatternType()),
            					new CSVFile.CSVField(MSG.columnTimePatternVisible()),
                                new CSVFile.CSVField(MSG.columnTimePatternUsed()),
            					new CSVFile.CSVField(MSG.columnTimePatternNbrMtgs()),
            					new CSVFile.CSVField(MSG.columnTimePatternMinPerMtg()),
            					new CSVFile.CSVField(MSG.columnTimePatternSlotsPerMtg()),
                                new CSVFile.CSVField(MSG.columnTimePatternBreakTime()),
            					new CSVFile.CSVField(MSG.columnTimePatternDays()),
            					new CSVFile.CSVField(MSG.columnTimePatternTimes()),
            					new CSVFile.CSVField(MSG.columnTimePatternDepartments()),
            					new CSVFile.CSVField(MSG.columnTimePatternClasses())
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
                	if (tp.getTimePatternType() != TimePatternType.Standard) {
                    	List timePrefs = 
                			hibSession.
                    		createQuery("select distinct p.owner from TimePref as p inner join p.timePattern as tp where tp.uniqueId=:uniqueid").
            				setParameter("uniqueid", tp.getUniqueId(), org.hibernate.type.IntegerType.INSTANCE).
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
                            classStr += MSG.notUsed();
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
	            					new CSVFile.CSVField(tp.getTimePatternType().getLabel()),
	            					new CSVFile.CSVField(tp.isVisible()?MSG.csvTrue():MSG.csvFalse()),
                                    new CSVFile.CSVField(tp.isEditable()?MSG.csvFalse():MSG.csvTrue()),
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

        if (MSG.actionAddTimePattern().equals(op)) {
            form.load(null, null);
        }

        if ("List".equals(form.getOp())) {
            // Read all existing settings and store in request
            getTimePatterns(sessionId);
            return "list";
        }

        String example = form.getExample();
        if (example!=null) {
            request.setAttribute("example", example);
        }
        return (form.getUniqueId().longValue()<0?"add":"edit");
	}
	
    private void getTimePatterns(Long sessionId) throws Exception {
		WebTable.setOrder(sessionContext,"timePatterns.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = new WebTable( 10,
			    null, "timePatternEdit.action?ord=%%",
			    new String[] {
			    		MSG.columnTimePatternName(),
			    		MSG.columnTimePatternType(),
			    		MSG.columnTimePatternUsed(),
			    		MSG.columnTimePatternNbrMtgs(),
			    		MSG.columnTimePatternMinPerMtg(),
			    		MSG.columnTimePatternSlotsPerMtg(),
			    		MSG.columnTimePatternBreakTime(),
			    		MSG.columnTimePatternDays(),
			    		MSG.columnTimePatternTimes(),
			    		MSG.columnTimePatternDepartments()},
			    new String[] {"left", "left", "left", "left", "left","left", "left", "left", "left", "left"},
			    null );
        
        List<TimePattern> patterns = TimePattern.findAll(sessionId,null);
		if(patterns.isEmpty()) {
		    webTable.addLine(null, new String[] {MSG.errorNoTimePatternsDefined()}, null, null );			    
		}
		
		Set used = TimePattern.findAllUsed(sessionId);

        for (TimePattern pattern: patterns) {
        	String onClick = "onClick=\"document.location='timePatternEdit.action?op=Edit&id=" + pattern.getUniqueId() + "';\"";
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
        	        (pattern.isVisible()?"":"<font color='gray'>")+pattern.getTimePatternType().getLabel().replaceAll(" ","&nbsp;")+
        	        (pattern.isVisible()?"":"</font>"),
        			(isUsed?"<IMG border='0' title='" + MSG.hintTimePatternUsed() + "' alt='Default' align='absmiddle' src='images/accept.png'>":""),
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
        
	    request.setAttribute("table", webTable.printTable(WebTable.getOrder(sessionContext,"timePatterns.ord")));
	    
	    List<Long> ids = new ArrayList<Long>();
	    for (Enumeration<WebTableLine> e = webTable.getLines().elements(); e.hasMoreElements(); ) {
	    	WebTableLine line = e.nextElement();
	    	if (line.getUniqueId() != null)
	    		ids.add(Long.parseLong(line.getUniqueId()));
	    }
	    Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, ids);
    }	
}

