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
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.SolverSettingsForm;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.dao.SolverParameterDAO;
import org.unitime.timetable.model.dao.SolverParameterDefDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;


/** 
 * @author Tomas Muller
 */
public class SolverSettingsAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		SolverSettingsForm myForm = (SolverSettingsForm) form;
        // Check Access
        if (!Web.isLoggedIn( request.getSession() )
               || !Web.hasRole(request.getSession(), Roles.getAdminRoles()) ) {
            throw new Exception ("Access Denied.");
        }
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        boolean list = true;
        
        if (op==null) {
            myForm.setOp("Add New");
	        op = "list";
	        myForm.loadDefaults();
        }
        
        // Reset Form
        if ("Cancel".equals(op)) {
            myForm.reset(mapping, request);
            myForm.setOp("Add New");
            myForm.loadDefaults();
        }
        
        if ("Add New".equals(op)) {
        	myForm.setOp("Create");
        	myForm.loadDefaults();
        	list = false;
        }
        
        // Add / Update
        if ("Update".equals(op) || "Create".equals(op) || "Export".equals(op)) {
            // Validate input
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
            	list=false;
                saveErrors(request, errors);
                mapping.findForward("showSolverSettings");
            } else {
            	Transaction tx = null;
            	try {
            		SolverPredefinedSettingDAO dao = new SolverPredefinedSettingDAO();
            		SolverParameterDAO pDao = new SolverParameterDAO();
            		org.hibernate.Session hibSession = dao.getSession();
                    if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                    	tx = hibSession.beginTransaction();
            		SolverPredefinedSetting setting = null;

            		if(op.equals("Create"))
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
                myForm.setOp("Add New");
            }
        }

        // Edit
        if(op.equals("Edit")) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("key", new ActionMessage("errors.invalid", "Unique Id : " + id));
                saveErrors(request, errors);
                mapping.findForward("showSolverSettings");
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
            			mapping.findForward("showSolverSettings");
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
                mapping.findForward("showSolverSettings");
            } else {
                try {
                    SolverPredefinedSettingDAO dao = new SolverPredefinedSettingDAO();
                    org.hibernate.Session hibSession = dao.getSession();
                
                    SolverPredefinedSetting setting = dao.get(new Long(id), hibSession);
                    
                    if(setting==null) {
                        errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
                        saveErrors(request, errors);
                        mapping.findForward("showSolverSettings");
                    } else {
                        File file = ApplicationProperties.getTempFile(setting.getName(), "txt");
                        PrintWriter pw = new PrintWriter(new FileWriter(file));
                        DataProperties properties = WebSolver.createProperties(setting.getUniqueId(), null);
                        pw.println("## Solver Configuration File");
                        pw.println("## Name: "+setting.getDescription());
                        pw.println("## Date: "+new Date());
                        pw.println("######################################");
                        for (Iterator i=hibSession.createQuery("select g from SolverParameterGroup g order by g.order").iterate();i.hasNext();) {
                            SolverParameterGroup g = (SolverParameterGroup)i.next();
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
                        request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
                    }
                } catch (Exception e) {
                    Debug.error(e);
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
            myForm.setOp("Add New");
        }

        // Read all existing settings and store in request
        if (list) getSolverSettingsTable(request);        
        return mapping.findForward("showSolverSettings");
	}
	
    private void getSolverSettingsTable(HttpServletRequest request) throws Exception {
    	Transaction tx = null;
		
		WebTable.setOrder(request.getSession(),"solverSettings.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = new WebTable( 4,
			    "Solver Settings", "solverSettings.do?ord=%%",
			    new String[] {"Name", "Description", "Appearance"},
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

	    request.setAttribute("SolverSettings.table", webTable.printTable(WebTable.getOrder(request.getSession(),"solverSettings.ord")));
    }	
	

}

