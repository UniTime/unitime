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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.cpsolver.ifs.util.CSVFile;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.DatePatternEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.IdValue;
import org.unitime.timetable.webutil.Navigation;


/** 
 * @author Tomas Muller
 */
@Action(value = "datePatternEdit", results = {
		@Result(name = "list", type = "tiles", location = "datePatternList.tiles"),
		@Result(name = "add", type = "tiles", location = "datePatternAdd.tiles"),
		@Result(name = "edit", type = "tiles", location = "datePatternEdit.tiles")
	})
@TilesDefinitions({
	@TilesDefinition(name = "datePatternList.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Date Patterns"),
			@TilesPutAttribute(name = "body", value = "/admin/datePatterns.jsp")
		}),
	@TilesDefinition(name = "datePatternAdd.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Add Date Pattern"),
			@TilesPutAttribute(name = "body", value = "/admin/datePatterns.jsp")
		}),
	@TilesDefinition(name = "datePatternEdit.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Edit Date Pattern"),
			@TilesPutAttribute(name = "body", value = "/admin/datePatterns.jsp")
		})
})
public class DatePatternEditAction extends UniTimeAction<DatePatternEditForm> {
	private static final long serialVersionUID = -9049668741172580381L;
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private Long id;
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	private int diff(Set x, Set y) {
		int diff = 0;
		for (Iterator i=x.iterator();i.hasNext();) {
			Object o = i.next();
			if (!y.contains(o)) diff++;
		}
		for (Iterator i=y.iterator();i.hasNext();) {
			Object o = i.next();
			if (!x.contains(o)) diff++;
		}
		return diff;
	}

	public String execute() throws Exception {
		if (form == null)
			form = new DatePatternEditForm();
		
		sessionContext.checkPermission(Right.DatePatterns);
        
        // Read operation to be performed
		if (op == null) op = form.getOp();
        
        if (op==null) {
            form.load(null);
            form.setOp("List");
        }
        
        request.setAttribute(DatePattern.DATE_PATTERN_PARENT_LIST_ATTR, DatePattern.findAllParents(sessionContext.getUser().getCurrentAcademicSessionId()));
        request.setAttribute(DatePattern.DATE_PATTERN_CHILDREN_LIST_ATTR, DatePattern.findAllChildren(sessionContext.getUser().getCurrentAcademicSessionId()));

        List<Department> list = (new DepartmentDAO()).getSession()
					.createCriteria(Department.class)
					.add(Restrictions.eq("session.uniqueId", sessionContext.getUser().getCurrentAcademicSessionId()))
					.addOrder(Order.asc("deptCode"))
					.list();
    	List<IdValue> availableDepts = new ArrayList<IdValue>();
    	for (Department d: list) {
    		availableDepts.add(new IdValue(d.getUniqueId(), d.getDeptCode() + "-" + d.getName()));
    	}
    	request.setAttribute(Department.DEPT_ATTR_NAME, availableDepts);        	
        
        // Reset Form
        if (MSG.actionBackToDatePatterns().equals(op)) {
            if (form.getUniqueId()!=null)
                request.setAttribute("hash", form.getUniqueId());
            form.load(null);
            form.setOp("List");
        }
        
        if (MSG.actionAddDatePattern().equals(op) && form.getChildId() == null) {
            form.load(null);
            form.setSessionId(sessionContext.getUser().getCurrentAcademicSessionId());
            form.setOp(MSG.actionSaveDatePattern());
        }

        if (MSG.actionAddDepartment().equals(op)) {
			if (form.getDepartmentId()==null || form.getDepartmentId() < 0)
				addFieldError("form.department", MSG.errorNoDepartmentSelected());
			else {
				boolean contains = form.getDepartmentIds().contains(form.getDepartmentId());
				if (contains)
					addFieldError("form.department", MSG.errorDepartmentAlreadyListed());
			}
			if (!hasFieldErrors()) {
            	form.getDepartmentIds().add(form.getDepartmentId());
            }
            form.setOp(form.getUniqueId() < 0 ? MSG.actionSaveDatePattern() : MSG.actionUpdateDatePattern());
        }

        if (MSG.actionAddAltPatternSet().equals(op)) {
			if (form.getParentId()==null || form.getParentId() < 0)
				addFieldError("form.parent", MSG.errorNoDatePatternSelected());
			else {
				boolean contains = form.getParentIds().contains(form.getParentId());
				if (contains)
					addFieldError("form.parent", MSG.errorDatePatternAlreadyListed());
			}
			if (!hasFieldErrors()) {
            	form.getParentIds().add(form.getParentId());
            }
			form.setOp(form.getUniqueId() < 0 ? MSG.actionSaveDatePattern() : MSG.actionUpdateDatePattern());
        }
        
        if (MSG.actionRemoveDepartment().equals(op)) {
			if (form.getDepartmentId()==null || form.getDepartmentId() < 0)
				addFieldError("form.department", MSG.errorNoDepartmentSelected());
			else {
				boolean contains = form.getDepartmentIds().contains(form.getDepartmentId());
				if (!contains)
					addFieldError("form.department", MSG.errorDepartmentNotListed());
			}
			if (!hasFieldErrors()) {
            	form.getDepartmentIds().remove(form.getDepartmentId());
            }	
			form.setOp(form.getUniqueId() < 0 ? MSG.actionSaveDatePattern() : MSG.actionUpdateDatePattern());
        }
        
        if (MSG.actionRemovePatternSet().equals(op)) {
			if (form.getParentId()==null || form.getParentId().longValue()<0)
				addFieldError("form.parent", MSG.errorNoDatePatternSelected());
			else {
				boolean contains = form.getParentIds().contains(form.getParentId());
				if (!contains)
					addFieldError("form.parent", MSG.errorDatePatternNotListed());
			}
			if (!hasFieldErrors()) {
            	form.getParentIds().remove(form.getParentId());
            }	
			form.setOp(form.getUniqueId() < 0 ? MSG.actionSaveDatePattern() : MSG.actionUpdateDatePattern());
        }
        
        if (MSG.actionAddDatePattern().equals(op) && form.getChildId() != null) {
			if (form.getChildId()==null || form.getChildId() < 0)
				addFieldError("form.child", MSG.errorNoDatePatternSelected());
			else {
				boolean contains = form.getChildrenIds().contains(form.getChildId());
				if (contains)
					addFieldError("form.child", MSG.errorDatePatternAlreadyListed());
			}
			if (!hasFieldErrors()) {
            	form.getChildrenIds().add(form.getChildId());
            }
            form.setOp(form.getUniqueId() < 0 ? MSG.actionSaveDatePattern() : MSG.actionUpdateDatePattern());
        }
        
        if (MSG.actionRemoveDatePattern().equals(op)) {
			if (form.getChildId()==null || form.getChildId() < 0)
				addFieldError("form.child", MSG.errorNoDatePatternSelected());
			else {
				boolean contains = form.getChildrenIds().contains(form.getChildId());
				if (!contains)
					addFieldError("form.child", MSG.errorDatePatternNotListed());
			}
			if (!hasFieldErrors()) {
            	form.getChildrenIds().remove(form.getChildId());
            }
            form.setOp(form.getUniqueId() < 0 ? MSG.actionSaveDatePattern() : MSG.actionUpdateDatePattern());
        }

        // Add / Update
        if (MSG.actionUpdateDatePattern().equals(op) || MSG.actionSaveDatePattern().equals(op) || MSG.actionMakeDatePatternDefaulf().equals(op)
        		|| MSG.actionPreviousDatePattern().equals(op) || MSG.actionNextDatePattern().equals(op)) {
            // Validate input
        	form.validate(this);
            if (hasFieldErrors()) {
            	form.setOp(form.getUniqueId() < 0 ? MSG.actionSaveDatePattern() : MSG.actionUpdateDatePattern());
            } else {
        		Transaction tx = null;
                try {
                	org.hibernate.Session hibSession = DatePatternDAO.getInstance().getSession();
                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                		tx = hibSession.beginTransaction();
                	
                	DatePattern dp = form.saveOrUpdate(request, hibSession);
                	if (MSG.actionMakeDatePatternDefaulf().equals(op)) {
                		dp.getSession().setDefaultDatePattern(dp);
                		hibSession.saveOrUpdate(dp.getSession());
                		form.setIsDefault(true);
                	}
                	
                    ChangeLog.addChange(
                            hibSession, 
                            sessionContext, 
                            dp, 
                            ChangeLog.Source.DATE_PATTERN_EDIT, 
                            (MSG.actionSaveDatePattern().equals(op)?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                            null, 
                            null);

                    if (tx!=null) tx.commit();
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        	    	throw e;
        	    }

                if (MSG.actionNextDatePattern().equals(op) && form.getNextId() != null) {
                	response.sendRedirect(response.encodeURL("datePatternEdit.action?op=Edit&id="+form.getNextId()));
                } else if (MSG.actionPreviousDatePattern().equals(op) && form.getPreviousId() != null) {
                	response.sendRedirect(response.encodeURL("datePatternEdit.action?op=Edit&id="+form.getPreviousId()));
                } else {
                	form.setOp("List");
                	if (form.getUniqueId() != null)
                		request.setAttribute("hash", form.getUniqueId());
                }
            }
        }

        // Edit
        if("Edit".equals(op)) {
            if (id == null && form.getUniqueId() != null) id = form.getUniqueId();
            if (id==null) {
            	addFieldError("form.uniqueId", MSG.errorRequiredField("Id"));
                request.setAttribute("pattern", form.getDatePattern(request).getPatternHtml(true, form.getUniqueId()));
                return "list";
            } else {
            	DatePattern pattern = (new DatePatternDAO()).get(Long.valueOf(id));
            	form.setPreviousId(Navigation.getPrevious(sessionContext, Navigation.sInstructionalOfferingLevel, Long.valueOf(id)));
            	form.setNextId(Navigation.getNext(sessionContext, Navigation.sInstructionalOfferingLevel, Long.valueOf(id)));
            	
                if(pattern==null) {
                	addFieldError("form.uniqueId", MSG.errorDoesNotExists(MSG.columnDatePattern()));
                    request.setAttribute("pattern", form.getDatePattern(request).getPatternHtml(true, form.getUniqueId()));
                    return "list";
                } else {
                	form.load(pattern);
                }
            }
        }

        // Delete 
        if (MSG.actionDeleteDatePattern().equals(op)) {
    		Transaction tx = null;
    		
            try {
            	org.hibernate.Session hibSession = DatePatternDAO.getInstance().getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
                DatePattern dp = (new DatePatternDAO()).get(form.getUniqueId(), hibSession);
                ChangeLog.addChange(
                        hibSession, 
                        sessionContext, 
                        dp, 
                        ChangeLog.Source.DATE_PATTERN_EDIT, 
                        ChangeLog.Operation.DELETE, 
                        null, 
                        null);

                form.delete(hibSession);
            	
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

    	    form.load(null);
            form.setOp("List");
        }
        
        if ("Fix Generated".equals(op)) {
    		Transaction tx = null;
    		
        	PrintWriter out = null;

        	try {
            	org.hibernate.Session hibSession = DatePatternDAO.getInstance().getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	File file = ApplicationProperties.getTempFile("fix", "txt");
            	out = new PrintWriter(new FileWriter(file));
            	
            	List<DatePattern> allDatePatterns = DatePattern.findAll(sessionContext.getUser().getCurrentAcademicSessionId(), true, null, null);
            	for (DatePattern dp: allDatePatterns) {
            		if (!dp.getName().startsWith("generated")) continue;
                    
                    out.println("Checking "+dp.getName()+" ...");
            		
            		List classes =
       					hibSession.
       					createQuery("select distinct c from Class_ as c inner join c.datePattern as dp where dp.uniqueId=:uniqueId").
       					setLong("uniqueId", dp.getUniqueId().longValue()).list();
            		
            		List subparts = 
    					hibSession.
    					createQuery("select distinct s from SchedulingSubpart as s inner join s.datePattern as dp where dp.uniqueId=:uniqueId").
    					setLong("uniqueId", dp.getUniqueId().longValue()).list();
            		
            		Vector allClasses = new Vector(classes);
            		
            		for (Iterator j=subparts.iterator();j.hasNext();) {
            			SchedulingSubpart s = (SchedulingSubpart)j.next();
            			for (Iterator k=s.getClasses().iterator();k.hasNext();) {
            				Class_ c = (Class_)k.next();
            				if (c.getDatePattern()==null)
            					allClasses.add(c);
            			}
            		}

            		if (allClasses.isEmpty()) {
            			out.println("  -- date pattern is not used -> delete");
            			for (Iterator j=dp.getDepartments().iterator();j.hasNext();) {
            				Department d = (Department)j.next();
            				d.getDatePatterns().remove(dp);
            				hibSession.saveOrUpdate(d);
            			}
                        ChangeLog.addChange(
                                hibSession, 
                                sessionContext, 
                                dp, 
                                ChangeLog.Source.DATE_PATTERN_EDIT, 
                                ChangeLog.Operation.DELETE, 
                                null, 
                                null);
            			hibSession.delete(dp);
            			continue;
            		}

            		for (Iterator j=allClasses.iterator();j.hasNext();) {
            			Class_ clazz = (Class_)j.next();
            			out.println("  -- used by "+clazz.getClassLabel());
            		}
            		
            		TreeSet days = dp.getUsage(allClasses);

        			out.println("    -- "+days);
        			if (days.isEmpty()) {
        				int offset = dp.getPatternOffset();
    					for (int x=0;x<dp.getPattern().length();x++) {
                            if (dp.getPattern().charAt(x)!='1') continue;
   							days.add(Integer.valueOf(x+offset));
    					}
        			}

            		DatePattern likeDp = null;
            		int likeDiff = 0;
        			
        			for (Iterator j=allDatePatterns.iterator();j.hasNext();) {
	            		DatePattern xdp = (DatePattern)j.next();
	            		if (xdp.getName().startsWith("generated")) continue;
	            		TreeSet xdays = xdp.getUsage(allClasses);
            			if (xdays.isEmpty()) {
            				int offset = xdp.getPatternOffset();
        					for (int x=0;x<xdp.getPattern().length();x++) {
        						if (xdp.getPattern().charAt(x)!='1') continue;
        						xdays.add(Integer.valueOf(x+offset));
        					}
            			}

            			int diff = diff(days, xdays);
	            		if (likeDp==null || likeDiff>diff || (likeDiff==diff && xdp.isDefault())) {
	            			likeDp = xdp; likeDiff = diff;
	            		}
        			}
            		
        			if (likeDp!=null) {
                        if (likeDiff<=5) {
                            out.println("      -- like "+likeDp.getName()+", diff="+likeDiff);
                            out.println("      -- "+likeDp.getUsage(allClasses));
                            out.println("    -- transfering all classes and subparts from "+dp.getName()+" to "+likeDp.getName());
                            for (Iterator j=classes.iterator();j.hasNext();) {
                                Class_ clazz = (Class_)j.next();
                                clazz.setDatePattern(likeDp.isDefault()?null:likeDp);
                                hibSession.saveOrUpdate(clazz);
                            }
                            for (Iterator j=subparts.iterator();j.hasNext();) {
                                SchedulingSubpart subpart = (SchedulingSubpart)j.next();
                                subpart.setDatePattern(likeDp.isDefault()?null:likeDp);
                                hibSession.saveOrUpdate(subpart);
                            }
                            out.println("    -- deleting date pattern "+dp.getName());
                            for (Iterator j=dp.getDepartments().iterator();j.hasNext();) {
                                Department d = (Department)j.next();
                                d.getDatePatterns().remove(dp);
                                hibSession.saveOrUpdate(d);
                            }
                            ChangeLog.addChange(
                                    hibSession, 
                                    sessionContext, 
                                    dp, 
                                    ChangeLog.Source.DATE_PATTERN_EDIT, 
                                    ChangeLog.Operation.DELETE, 
                                    null, 
                                    null);
                            hibSession.delete(dp);
                        } else {
                            out.println("      -- like "+likeDp.getName()+", diff="+likeDiff);
                            out.println("      -- "+likeDp.getUsage(allClasses));
                            Formats.Format<Date> sdf = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
                            dp.setName("generated "+sdf.format(dp.getStartDate())+" - "+sdf.format(dp.getEndDate()));
                            hibSession.saveOrUpdate(dp);
                        }
        			}
            	}
            	
            	out.flush(); out.close(); out = null;
            	request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        	
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    } finally {
    	    	if (out!=null) out.close();
    	    }
    	    
        	form.load(null);
        	form.setOp("List");
        }

        if ("Generate SQL".equals(op)) {
            PrintWriter out = ExportUtils.getPlainTextWriter(response, "tp.sql");
            try {
                TreeSet patterns = new TreeSet(DatePattern.findAll(sessionContext.getUser(), null, null));
                
                boolean mysql = false;

                int line = 0;
                if (mysql) {
                    out.println("INSERT INTO `timetable`.`date_pattern`(`uniqueid`, `name`, `pattern`, `offset`, `type`, `visible`, `session_id`)");
                } else {
                    out.println("prompt Loading DATE_PATTERN...");
                }
                for (Iterator i=patterns.iterator();i.hasNext();) {
                    DatePattern dp = (DatePattern)i.next();
                    
                    if (dp.isExtended()) continue;
                    if (!dp.isVisible()) continue;
                    
                    if (mysql) {
                        if (line==0) out.print("VALUES"); else out.println(",");
                    
                        out.print(" ("+dp.getUniqueId()+", '"+dp.getName()+"', '"+dp.getPattern()+"', "+
                            dp.getOffset()+", "+dp.getType()+", "+(dp.isVisible()?"1":"0")+", "+
                            sessionContext.getUser().getCurrentAcademicSessionId()+")");
                    } else {
                        out.println("insert into DATE_PATTERN (UNIQUEID, NAME, PATTERN, OFFSET, TYPE, VISIBLE, SESSION_ID)");
                        out.println("values ("+dp.getUniqueId()+", '"+dp.getName()+"', '"+dp.getPattern()+"', "+
                            dp.getOffset()+", "+dp.getType()+", "+(dp.isVisible()?"1":"0")+", "+
                            sessionContext.getUser().getCurrentAcademicSessionId()+");");
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

                out.flush(); out.close(); out = null;
            } catch (Exception e) {
                throw e;
            } finally {
                if (out!=null) out.close();
            }

            return null;
        }
        
        if (MSG.actionPushUpDatePatterns().equals(op)) {
    		Transaction tx = null;
    		
        	PrintWriter out = null;

        	try {
            	org.hibernate.Session hibSession = DatePatternDAO.getInstance().getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	File file = ApplicationProperties.getTempFile("push", "txt");
            	out = new PrintWriter(new FileWriter(file));
            		            	
            	List subparts =
   					hibSession.
   					createQuery("select distinct c.schedulingSubpart from Class_ as c inner join c.datePattern as dp where dp.session.uniqueId=:sessionId").
   					setLong("sessionId", sessionContext.getUser().getCurrentAcademicSessionId()).list();

            	for (Iterator i=subparts.iterator();i.hasNext();) {
            		SchedulingSubpart subpart = (SchedulingSubpart)i.next();
            		
            		out.println("Checking "+subpart.getSchedulingSubpartLabel()+" ...");
            		
            		boolean sameDatePattern = true;
            		DatePattern dp = null;
            		
            		for (Iterator j=subpart.getClasses().iterator();j.hasNext();) {
            			Class_ clazz = (Class_)j.next();
            			if (clazz.getDatePattern()==null) {
            				sameDatePattern=false; break;
            			}
            			if (dp==null)
            				dp = clazz.getDatePattern();
            			else if (!dp.equals(clazz.getDatePattern())) {
            				sameDatePattern=false; break;
            			}
            		}
            		
            		if (!sameDatePattern) continue;
            		
            		out.println("  -- all classes share same date pattern "+dp.getName()+" --> pushing it to subpart");
            		
            		for (Iterator j=subpart.getClasses().iterator();j.hasNext();) {
            			Class_ clazz = (Class_)j.next();
            			clazz.setDatePattern(null);
            			hibSession.saveOrUpdate(clazz);
            		}
            		subpart.setDatePattern(dp.isDefault()?null:dp);
            		hibSession.saveOrUpdate(subpart);
                }

            	out.flush(); out.close(); out = null;
            	request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
	        	
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    } finally {
    	    	if (out!=null) out.close();
    	    }
    	    
        	form.load(null);
        	form.setOp("List");
        }

        if (MSG.actionAssingDepartmentsToDatePatterns().equals(op)) {
    		Transaction tx = null;
    		
        	PrintWriter out = null;
        	HashSet refresh = new HashSet(); 

        	try {
            	org.hibernate.Session hibSession = DatePatternDAO.getInstance().getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	File file = ApplicationProperties.getTempFile("assigndept", "txt");
            	out = new PrintWriter(new FileWriter(file));
            	
            	TreeSet allDatePatterns = new TreeSet(DatePattern.findAll(sessionContext.getUser().getCurrentAcademicSessionId(), true, null, null));
            	for (Iterator i=allDatePatterns.iterator();i.hasNext();) {
            		DatePattern dp = (DatePattern)i.next();
            		
            		if (!dp.isExtended()) continue;
            		
            		out.println("Checking "+dp.getName()+" ...");
            		
            		List classes =
       					hibSession.
       					createQuery("select distinct c from Class_ as c inner join c.datePattern as dp where dp.uniqueId=:uniqueId").
       					setLong("uniqueId", dp.getUniqueId().longValue()).list();
            		
            		List subparts = 
    					hibSession.
    					createQuery("select distinct s from SchedulingSubpart as s inner join s.datePattern as dp where dp.uniqueId=:uniqueId").
    					setLong("uniqueId", dp.getUniqueId().longValue()).list();
            		
            		HashSet depts = new HashSet();
            		
            		for (Iterator j=classes.iterator();j.hasNext();) {
            			Class_ c = (Class_)j.next();
            			depts.add(c.getManagingDept());
            		}
            		
            		for (Iterator j=subparts.iterator();j.hasNext();) {
            			SchedulingSubpart s = (SchedulingSubpart)j.next();
            			depts.add(s.getManagingDept());
            		}
            		
            		out.println("  -- departments: "+depts);

            		boolean added = false;
            		for (Iterator j=depts.iterator();j.hasNext();) {
            			Department d = (Department)j.next();
            			if (d.isExternalManager().booleanValue()) {
            				/*
            				if (dp.getDepartments().contains(d)) {
            					dp.getDepartments().remove(d);
            					d.getDatePatterns().remove(dp);
            					hibSession.saveOrUpdate(d);
                				out.println("    -- department "+d+" removed from "+dp.getName());
                				added=true;
            				}*/
            				continue;
            			}
            			if (!dp.getDepartments().contains(d)) {
            				dp.getDepartments().add(d);
            				d.getDatePatterns().add(dp);
            				hibSession.saveOrUpdate(d);
            				out.println("    -- department "+d+" added to "+dp.getName());
            				added = true;
            			}
            		}
            		if (added) {
            			hibSession.saveOrUpdate(dp);
                        ChangeLog.addChange(
                                hibSession, 
                                sessionContext, 
                                dp, 
                                ChangeLog.Source.DATE_PATTERN_EDIT, 
                                ChangeLog.Operation.UPDATE, 
                                null, 
                                null);
            			refresh.add(dp);
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
    	    
        	form.load(null);
        	form.setOp("List");
        }

        if (MSG.actionExportCsv().equals(op)) {
    		Transaction tx = null;
    		
    		try {
            	org.hibernate.Session hibSession = DatePatternDAO.getInstance().getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	CSVFile csv = new CSVFile();
            	csv.setHeader(
            			new CSVFile.CSVField[] {
            					new CSVFile.CSVField(MSG.columnDatePatternName()),
            					new CSVFile.CSVField(MSG.columnDatePatternType()),
            					new CSVFile.CSVField(MSG.columnDatePatternNbrDays()),
            					new CSVFile.CSVField(MSG.columnDatePatternFrom()),
            					new CSVFile.CSVField(MSG.columnDatePatternTo()),
            					new CSVFile.CSVField(MSG.columnDatePatternDates()),
            					new CSVFile.CSVField(MSG.columnDatePatternParent()),
            					new CSVFile.CSVField(MSG.columnDatePatternDepartments()),
            					new CSVFile.CSVField(MSG.columnDatePatternClasses())
            			});
            	
            	Formats.Format<Date> sdf = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
            	TreeSet allDatePatterns = new TreeSet(DatePattern.findAll(sessionContext.getUser().getCurrentAcademicSessionId(), true, null, null));
            	for (Iterator i=allDatePatterns.iterator();i.hasNext();) {
            		DatePattern dp = (DatePattern)i.next();
            		
            		List classes =
       					hibSession.
       					createQuery("select distinct c from Class_ as c inner join c.datePattern as dp where dp.uniqueId=:uniqueId").
       					setLong("uniqueId", dp.getUniqueId().longValue()).list();
            		
            		List subparts = 
    					hibSession.
    					createQuery("select distinct s from SchedulingSubpart as s inner join s.datePattern as dp where dp.uniqueId=:uniqueId").
    					setLong("uniqueId", dp.getUniqueId().longValue()).list();
            		
            		TreeSet allClasses = new TreeSet(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
            		allClasses.addAll(classes);
            		
            		for (Iterator j=subparts.iterator();j.hasNext();) {
            			SchedulingSubpart s = (SchedulingSubpart)j.next();
            			for (Iterator k=s.getClasses().iterator();k.hasNext();) {
            				Class_ c = (Class_)k.next();
            				if (c.getDatePattern()==null)
            					allClasses.add(c);
            			}
            		}

                	String deptStr = "";
                	TreeSet depts = new TreeSet(dp.getDepartments()); 
                	for (Iterator j=depts.iterator();j.hasNext();) {
                		Department d = (Department)j.next();
                		deptStr += d.getShortLabel().trim();
                		if (j.hasNext()) { deptStr += ", "; }
                	}
                	
                	String classStr = "";
                	for (Iterator j=allClasses.iterator();j.hasNext();) {
                		Class_ clazz = (Class_)j.next();
                		classStr += clazz.getClassLabel();
                		if (j.hasNext()) { classStr += ", "; }
                	}
                	
                	String datePattStr = "";
                	for (Iterator j=dp.getParents().iterator();j.hasNext();) {
                		DatePattern d = (DatePattern)j.next();
                		datePattStr += d.getName();
                		if (j.hasNext()) { datePattStr += ", "; }
                	}
                	
                	csv.addLine(
                			new CSVFile.CSVField[] {
	            					new CSVFile.CSVField(dp.getName()),		            					
	            					new CSVFile.CSVField(dp.getDatePatternType().getLabel()),
	            					new CSVFile.CSVField(String.valueOf(dp.size())),
	            					new CSVFile.CSVField(sdf.format(dp.getStartDate())),
	            					new CSVFile.CSVField(sdf.format(dp.getEndDate())),
	            					new CSVFile.CSVField(dp.getPatternString()),
	            					new CSVFile.CSVField(datePattStr),
	            					new CSVFile.CSVField(deptStr),
	            					new CSVFile.CSVField(classStr)
	            			});
                	
                	/*
                	System.out.println(
                			"insert into date_pattern (uniqueid, name, pattern, offset, type, visible, session_id) values (" +
                			"DATE_PATTERN_SEQ.Nextval, "+
                			"'"+dp.getName()+"', "+
                			"'"+dp.getPattern()+"', "+
                			dp.getOffset()+", "+
                			dp.getType()+", "+
                			(dp.isVisible().booleanValue()?1:0)+", "+
                			dp.getSession().getUniqueId()+");");
					*/
            	}
            	
            	ExportUtils.exportCSV(csv, response, "datePatterns");
    			tx.commit();
            	return null;
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }
        }

        if ("List".equals(form.getOp())) {
            // Read all existing settings and store in request
            getDatePatterns(request);
            return "list";
        } 
        
        request.setAttribute("pattern", form.getDatePattern(request).getPatternHtml(true, form.getUniqueId()));
        return form.getUniqueId() < 0 ? "add" : "edit";
	}
	
	
    private void getDatePatterns(HttpServletRequest request) throws Exception {
		Set used = DatePattern.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId());
		boolean hasSet = !DatePattern.findAllParents(sessionContext.getUser().getCurrentAcademicSessionId()).isEmpty(); 
        Formats.Format<Date> sdf = Formats.getDateFormat(Formats.Pattern.DATE_EXAM_PERIOD);

		WebTable.setOrder(sessionContext,"datePatterns.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = (hasSet ?
        		new WebTable( 7,
        				null, 
        				"datePatternEdit.action?ord=%%",
        				new String[] {
        						MSG.columnDatePatternName(),
        						MSG.columnDatePatternType(),
        						MSG.columnDatePatternUsed(),
        						MSG.columnDatePatternWeeks(),
        						MSG.columnDatePatternDatesOrPatterns(),
        						MSG.columnDatePatternPatternSets(),
        						MSG.columnDatePatternDepartments()
        						},
        				new String[] {"left", "left", "left", "left", "left", "left", "left"},
        				null ) :
			    new WebTable( 6,
			    		null,
			    		"datePatternEdit.action?ord=%%",
						new String[] {
								MSG.columnDatePatternName(),
        						MSG.columnDatePatternType(),
        						MSG.columnDatePatternUsed(),
        						MSG.columnDatePatternWeeks(),
        						MSG.columnDatePatternDates(),
        						MSG.columnDatePatternDepartments()
        						},
						new String[] {"left", "left", "left", "left", "left", "left"},
						null ) );
        
        List<DatePattern> patterns = DatePattern.findAll(sessionContext.getUser(), null, null);
		if(patterns.isEmpty()) {
		    webTable.addLine(null, new String[] {MSG.errorNoDatePatterns()}, null, null);
		}
		
		DecimalFormat df = new DecimalFormat("0.##", new DecimalFormatSymbols(Localization.getJavaLocale()));
        for (DatePattern pattern: patterns) {
        	String onClick = "onClick=\"document.location='datePatternEdit.action?op=Edit&id=" + pattern.getUniqueId() + "';\"";
        	String deptStr = "";
        	String deptCmp = "";
        	TreeSet depts = new TreeSet(pattern.getDepartments()); 
        	for (Iterator i=depts.iterator();i.hasNext();) {
        		Department d = (Department)i.next();
        		deptStr += d.getManagingDeptAbbv().trim();
        		deptCmp += d.getDeptCode();
        		if (i.hasNext()) { deptStr += ", "; deptCmp += ","; }
        	}
        	
        	String datePatternStr = "";        	
        	List dps = new ArrayList(pattern.getParents()); 
        	Collections.sort(dps);
        	for (Iterator i=dps.iterator();i.hasNext();) {
        		DatePattern d = (DatePattern)i.next();
        		datePatternStr += d.getName();
        		if (i.hasNext()) { datePatternStr += ", ";}
        	}
        	
            String pattStr = pattern.getPatternString();
            if (pattern.getName().startsWith("generated")) {
                int first = pattern.getPattern().indexOf('1') - pattern.getOffset().intValue();
                int last = pattern.getPattern().lastIndexOf('1') - pattern.getOffset().intValue();

                DatePattern likeDp = null;
                int likeDiff = 0;
                
                for (Iterator j=patterns.iterator();j.hasNext();) {
                    DatePattern xdp = (DatePattern)j.next();
                    if (xdp.getName().startsWith("generated")) continue;
                    int xfirst = xdp.getPattern().indexOf('1') - xdp.getOffset().intValue();
                    int xlast = xdp.getPattern().lastIndexOf('1') - xdp.getOffset().intValue();
                    int diff = Math.abs(first-xfirst) + Math.abs(last-xlast);
                    if (likeDp==null || likeDiff>diff || (likeDiff==diff && xdp.isDefault())) {
                        likeDp = xdp; likeDiff = diff;
                    }
                }
                
                if (likeDp!=null) {
                    int xfirst = likeDp.getPattern().indexOf('1') - likeDp.getOffset().intValue();
                    int xlast = likeDp.getPattern().lastIndexOf('1') - likeDp.getOffset().intValue();
                    int firstDiff = first - xfirst;
                    int lastDiff = last - xlast;
                    if (Math.abs(lastDiff)>3 || Math.abs(firstDiff)>3) pattStr += "<b>";
                    pattStr += "<br>Similar to "+likeDp.getName()+" (offset "+firstDiff+" and "+lastDiff+" days)";
                    pattStr += "<br>"+sdf.format(pattern.getStartDate())+"-"+sdf.format(pattern.getEndDate())+" versus "+sdf.format(likeDp.getStartDate())+"-"+sdf.format(likeDp.getEndDate());
                    if (Math.abs(lastDiff)>3 || Math.abs(firstDiff)>3) pattStr += "</b>";
                }
            }
            
            if (pattern.isPatternSet()) {
            	for (DatePattern child: new TreeSet<DatePattern>(pattern.findChildren())) {
            		pattStr += (pattStr.isEmpty() ? "" : ", ") + child.getName();
            	}
            }
            
            String nbrWeeks = null;
            if (pattern.getNumberOfWeeks() == null) {
            	nbrWeeks = "<i>" + df.format(pattern.getComputedNumberOfWeeks()) + "</i>";
            } else {
            	nbrWeeks = df.format(pattern.getNumberOfWeeks());
            }
            
        	boolean isUsed = used.contains(pattern) || pattern.isDefault();
        	if (hasSet)
            	webTable.addLine(onClick, new String[] {
            	        (pattern.isDefault()?"<B>":"")+(pattern.isVisible()?"":"<font color='gray'>")+pattern.getName().replaceAll(" ","&nbsp;")+
            	        (pattern.isVisible()?"":"</font>")+(pattern.isDefault()?"</B>":""),
            	        (pattern.isVisible()?"":"<font color='gray'>")+pattern.getDatePatternType().getLabel().replaceAll(" ","&nbsp;")+(pattern.isVisible()?"":"</font>"),
            			(isUsed?"<IMG border='0' title='" + MSG.infoDatePatternUsed() + "' alt='Default' align='absmiddle' src='images/accept.png'>":""),
            			(pattern.isVisible()?"":"<font color='gray'>")+nbrWeeks+(pattern.isVisible()?"":"</font>"),
            			(pattern.isVisible()?"":"<font color='gray'>")+pattStr+(pattern.isVisible()?"":"</font>"),
            			(pattern.isVisible()?"":"<font color='gray'>")+datePatternStr+(pattern.isVisible()?"":"</font>"),
            			(pattern.isVisible()?"":"<font color='gray'>")+deptStr+(pattern.isVisible()?"":"</font>")        			
            		},new Comparable[] {
            			pattern.getName(),
            			pattern.getType(),
            			(isUsed?"0":"1"),
            			pattern.getEffectiveNumberOfWeeks(),
            			pattStr,
            			datePatternStr,
            			deptCmp,
            		},pattern.getUniqueId().toString());
        	else
            	webTable.addLine(onClick, new String[] {
            	        (pattern.isDefault()?"<B>":"")+(pattern.isVisible()?"":"<font color='gray'>")+pattern.getName().replaceAll(" ","&nbsp;")+
            	        (pattern.isVisible()?"":"</font>")+(pattern.isDefault()?"</B>":""),
            	        (pattern.isVisible()?"":"<font color='gray'>")+pattern.getDatePatternType().getLabel().replaceAll(" ","&nbsp;")+(pattern.isVisible()?"":"</font>"),
            			(isUsed?"<IMG border='0' title='" + MSG.infoDatePatternUsed() + "' alt='Default' align='absmiddle' src='images/accept.png'>":""),
            			(pattern.isVisible()?"":"<font color='gray'>")+nbrWeeks+(pattern.isVisible()?"":"</font>"),
            			(pattern.isVisible()?"":"<font color='gray'>")+pattStr+(pattern.isVisible()?"":"</font>"),
            			(pattern.isVisible()?"":"<font color='gray'>")+deptStr+(pattern.isVisible()?"":"</font>")        			
            		},new Comparable[] {
            			pattern.getName(),
            			pattern.getType(),
            			(isUsed?"0":"1"),
            			pattern.getEffectiveNumberOfWeeks(),
            			pattStr,
            			deptCmp,
            		}, pattern.getUniqueId().toString());
        }
        
	    request.setAttribute("table", webTable.printTable(WebTable.getOrder(sessionContext,"datePatterns.ord")));
	    
	    List<Long> ids = new ArrayList<Long>();
	    for (Enumeration<WebTableLine> e = webTable.getLines().elements(); e.hasMoreElements(); ) {
	    	WebTableLine line = e.nextElement();
	    	if (line.getUniqueId() != null)
	    		ids.add(Long.parseLong(line.getUniqueId()));
	    }
	    Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, ids);
		
    }	
}

