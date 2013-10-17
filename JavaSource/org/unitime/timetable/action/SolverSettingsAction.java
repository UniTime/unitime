/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.action;

import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cpsolver.ifs.util.DataProperties;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.SolverSettingsForm;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.dao.SolverParameterDAO;
import org.unitime.timetable.model.dao.SolverParameterDefDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.ExportUtils;


/** 
 * @author Tomas Muller
 */
@Service("/solverSettings")
public class SolverSettingsAction extends Action {
	
	@Autowired SessionContext sessionContext;

	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	@Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		SolverSettingsForm myForm = (SolverSettingsForm) form;
		
        // Check Access
		sessionContext.checkPermission(Right.SolverConfigurations);
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        if (op==null) op = request.getParameter("op2");
        boolean list = true;
        
        
        if (op==null) {
            myForm.setOp("Add Solver Configuration");
	        op = "list";
	        myForm.loadDefaults();
        }
        
        // Reset Form
        if ("Back".equals(op)) {
            myForm.reset(mapping, request);
            myForm.setOp("Add Solver Configuration");
            myForm.loadDefaults();
        }
        
        if ("Add Solver Configuration".equals(op)) {
        	myForm.setOp("Save");
        	myForm.loadDefaults();
        	list = false;
        }
        
        if ("Refresh".equals(op)) {
            myForm.setOp(myForm.getUniqueId()==null || myForm.getUniqueId()<=0?"Save":"Update");
            myForm.loadDefaults(request);
            list = false;
        }
        
        // Add / Update
        if ("Update".equals(op) || "Save".equals(op)) {
            // Validate input
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
            	list=false;
                saveErrors(request, errors);
            } else {
            	Transaction tx = null;
            	try {
            		SolverPredefinedSettingDAO dao = new SolverPredefinedSettingDAO();
            		SolverParameterDAO pDao = new SolverParameterDAO();
            		org.hibernate.Session hibSession = dao.getSession();
                    if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                    	tx = hibSession.beginTransaction();
            		SolverPredefinedSetting setting = null;

            		if(op.equals("Save"))
            			setting = new SolverPredefinedSetting();
            		else 
            			setting = dao.get(myForm.getUniqueId(), hibSession);
                
            		setting.setName(myForm.getName());
            		setting.setDescription(myForm.getDescription());                
            		setting.setAppearance(new Integer(myForm.getAppearanceIdx()));
            		Set params = setting.getParameters();
            		if (params==null) {
            			params = new HashSet();
            			setting.setParameters(params);
            		}
            		for (Iterator i=(new SolverParameterDefDAO()).findAll(hibSession).iterator();i.hasNext();) {
            			SolverParameterDef def = (SolverParameterDef)i.next();
            			SolverParameter param = null;
            			for (Iterator j=params.iterator();j.hasNext();) {
            				SolverParameter p = (SolverParameter)j.next();
            				if (p.getDefinition().equals(def)) {
            					param = p; break;
            				}
            			}
            			if (!def.isVisible().booleanValue()) {
            				if (param!=null) {
            					params.remove(param);
            					pDao.delete(param, hibSession);
            				}
            			} else {
                			String value = myForm.getParameter(def.getUniqueId());
                			boolean useDefault = myForm.getUseDefault(def.getUniqueId()).booleanValue();
                			if (useDefault) {
                				if (param!=null) {
                					params.remove(param);
                					pDao.delete(param, hibSession);
                				}
                			} else {
                				if (param==null) {
                					param = new SolverParameter();
                					param.setDefinition(def);
                				}
                				param.setValue(value==null?def.getDefault():value);
                    			pDao.saveOrUpdate(param, hibSession);
                    			params.add(param);
                			}
            			}
            		}
            		dao.saveOrUpdate(setting, hibSession);
            		if (tx!=null) tx.commit();
            	} catch (Exception e) {
            		if (tx!=null) tx.rollback();
            		Debug.error(e);
            	}
                
                myForm.reset(mapping, request);
                myForm.loadDefaults();
                myForm.setOp("Add Solver Configuration");
            }
        }

        // Edit
        if(op.equals("Edit")) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("key", new ActionMessage("errors.invalid", "Unique Id : " + id));
                saveErrors(request, errors);
            } else {
            	Transaction tx = null;
            	try {
            		SolverPredefinedSettingDAO dao = new SolverPredefinedSettingDAO();
            		org.hibernate.Session hibSession = dao.getSession();
                    if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                    	tx = hibSession.beginTransaction();
            	
            		SolverPredefinedSetting setting = dao.get(new Long(id), hibSession);
            		if(setting==null) {
            			errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
            			saveErrors(request, errors);
            		} else {
            			myForm.reset(mapping, request);
            			myForm.loadDefaults();
                        myForm.setUniqueId(setting.getUniqueId());
                        myForm.setName(setting.getName());
                        myForm.setDescription(setting.getDescription());
            			myForm.setAppearanceIdx(setting.getAppearance().intValue());
            			myForm.setOp("Update");
            			for (Iterator i=setting.getParameters().iterator();i.hasNext();) {
            				SolverParameter param = (SolverParameter)i.next();
            				if (!param.getDefinition().isVisible().booleanValue()) continue;
            				myForm.setParameter(param.getDefinition().getUniqueId(),param.getValue());
            				myForm.setUseDefault(param.getDefinition().getUniqueId(),Boolean.FALSE);
            			}
                	}
            		if (tx!=null) tx.commit();
            	} catch (Exception e) {
            		if (tx!=null) tx.rollback();
            		Debug.error(e);
            	}
            	list=false;
            }
        }
        
        // Export
        if ("Export".equals(op)) {
            String id = request.getParameter("id");
            if ((id==null || id.trim().length()==0) && myForm.getUniqueId()!=null)
                id = myForm.getUniqueId().toString();
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("key", new ActionMessage("errors.invalid", "Unique Id : " + id));
                saveErrors(request, errors);
            } else {
                SolverPredefinedSettingDAO dao = new SolverPredefinedSettingDAO();
                org.hibernate.Session hibSession = dao.getSession();
            
                SolverPredefinedSetting setting = dao.get(new Long(id), hibSession);
                
                if(setting==null) {
                    errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
                    saveErrors(request, errors);
                } else {
                    PrintWriter pw = ExportUtils.getPlainTextWriter(response, setting.getName() + ".txt");
                    DataProperties properties = null;
                    switch (myForm.getAppearanceIdx()) {
                    case SolverPredefinedSetting.APPEARANCE_STUDENT_SOLVER:
                    	properties = studentSectioningSolverService.createConfig(setting.getUniqueId(), null);
                    	break;
                    case SolverPredefinedSetting.APPEARANCE_EXAM_SOLVER:
                    	properties = examinationSolverService.createConfig(setting.getUniqueId(), null);
                    	break;
                    default:
                    	properties = courseTimetablingSolverService.createConfig(setting.getUniqueId(), null);
                    }
                    pw.println("## Solver Configuration File");
                    pw.println("## Name: "+setting.getDescription());
                    pw.println("## Date: "+new Date());
                    pw.println("######################################");
                    for (Iterator i=hibSession.createQuery("select g from SolverParameterGroup g order by g.order").iterate();i.hasNext();) {
                        SolverParameterGroup g = (SolverParameterGroup)i.next();
                        if (myForm.getAppearanceIdx()==SolverPredefinedSetting.APPEARANCE_STUDENT_SOLVER) {
                            if (g.getType()!=SolverParameterGroup.sTypeStudent) continue;
                        } else if (myForm.getAppearanceIdx()==SolverPredefinedSetting.APPEARANCE_EXAM_SOLVER) {
                            if (g.getType()!=SolverParameterGroup.sTypeExam) continue;
                        } else {
                            if (g.getType()!=SolverParameterGroup.sTypeCourse) continue;
                        }
                        pw.println();
                        pw.println("## "+g.getDescription().replaceAll("<br>", "\n#"));
                        pw.println("######################################");
                        TreeSet parameters = new TreeSet(g.getParameters());
                        for (Iterator j=parameters.iterator();j.hasNext();) {
                            SolverParameterDef p = (SolverParameterDef)j.next();
                            String value = properties.getProperty(p.getName(),p.getDefault());
                            if (value==null) continue;
                            pw.println("## "+p.getDescription().replaceAll("<br>", "\n#"));
                            pw.println("## Type: "+p.getType());
                            if (value!=null && !value.equals(p.getDefault()))
                                pw.println("## Default: "+p.getDefault());
                            pw.println(p.getName()+"="+properties.getProperty(p.getName(),p.getDefault()));
                            properties.remove(p.getName());
                        }
                    }
                    pw.println();
                    pw.println("## Other Properties");
                    pw.println("######################################");
                    for (Enumeration e=properties.propertyNames();e.hasMoreElements();) {
                        String name = (String)e.nextElement();
                        pw.println(name+"="+properties.getProperty(name));
                    }
                    pw.flush(); pw.close();
                    return null;
                }
                list=false;
            }
        }
        

        // Delete 
        if("Delete".equals(op)) {
        	Transaction tx = null;
    		
            try {
            	SolverPredefinedSettingDAO dao = new SolverPredefinedSettingDAO();
            	org.hibernate.Session hibSession = dao.getSession();
                if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                	tx = hibSession.beginTransaction();
    			
    			SolverPredefinedSetting setting = dao.get(myForm.getUniqueId(), hibSession);

    			dao.delete(setting, hibSession);
    			
    			if (tx!=null) tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    			Debug.error(e);
    	    }
            myForm.reset(mapping, request);
            myForm.setOp("Add Solver Configuration");
        }

        if ("Add Solver Configuration".equals(myForm.getOp())) {
            // Read all existing settings and store in request
            if (list) getSolverSettingsTable(request);        
            return mapping.findForward("list");
        } 
        
        return mapping.findForward("Save".equals(myForm.getOp())?"add":"edit");
	}
	
    private void getSolverSettingsTable(HttpServletRequest request) throws Exception {
    	Transaction tx = null;
		
		WebTable.setOrder(sessionContext,"solverSettings.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = new WebTable( 4,
			    null, "solverSettings.do?ord=%%",
			    new String[] {"Reference", "Name", "Appearance"},
			    new String[] {"left", "left", "left"},
			    null );
        
        try {
        	SolverPredefinedSettingDAO dao = new SolverPredefinedSettingDAO();
        	org.hibernate.Session hibSession = dao.getSession();
            if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            	tx = hibSession.beginTransaction();
            
			List list = hibSession.createCriteria(SolverPredefinedSetting.class).list();
			
			if(list.isEmpty()) {
			    webTable.addLine(null, new String[] {"No solver settings defined."}, null, null );			    
			} else {
				for (Iterator i=list.iterator();i.hasNext();) {
					SolverPredefinedSetting setting = (SolverPredefinedSetting)i.next();
					String onClick = "onClick=\"document.location='solverSettings.do?op=Edit&id=" + setting.getUniqueId() + "';\"";
					
					webTable.addLine(onClick, new String[] {
							setting.getName(), 
							setting.getDescription(), 
							SolverPredefinedSetting.sAppearances[setting.getAppearance().intValue()]},
						new Comparable[] {
							setting.getName(), 
							setting.getDescription(),
							setting.getAppearance()
						});
				}
			}
			
			if (tx!=null) tx.commit();
	    } catch (Exception e) {
	    	if (tx!=null) tx.rollback();
	    	throw e;
	    }

	    request.setAttribute("SolverSettings.table", webTable.printTable(WebTable.getOrder(sessionContext,"solverSettings.ord")));
    }	
	

}

