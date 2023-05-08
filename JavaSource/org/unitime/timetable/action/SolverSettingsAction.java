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

import java.io.PrintWriter;
import java.util.Date;
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
import org.cpsolver.ifs.util.DataProperties;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.SolverSettingsForm;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.dao.SolverParameterDAO;
import org.unitime.timetable.model.dao.SolverParameterDefDAO;
import org.unitime.timetable.model.dao.SolverParameterGroupDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;


/** 
 * @author Tomas Muller
 */
@Action(value = "solverSettings", results = {
		@Result(name = "list", type = "tiles", location = "solverSettingsList.tiles"),
		@Result(name = "add", type = "tiles", location = "solverSettingAdd.tiles"),
		@Result(name = "edit", type = "tiles", location = "solverSettingEdit.tiles")
	})
@TilesDefinitions({
	@TilesDefinition(name = "solverSettingsList.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Solver Configurations"),
			@TilesPutAttribute(name = "body", value = "/admin/solverSettings.jsp")
		}),
	@TilesDefinition(name = "solverSettingAdd.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Add Solver Configuration"),
			@TilesPutAttribute(name = "body", value = "/admin/solverSettings.jsp")
		}),
	@TilesDefinition(name = "solverSettingEdit.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Edit Solver Configuration"),
			@TilesPutAttribute(name = "body", value = "/admin/solverSettings.jsp")
		})
})
public class SolverSettingsAction extends UniTimeAction<SolverSettingsForm> {
	private static final long serialVersionUID = 2993473779756335885L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	protected String id = null;
	protected String op2 = null;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }	
	
	public String execute() throws Exception {
		if (form == null) form = new SolverSettingsForm();
		
        // Check Access
		sessionContext.checkPermission(Right.SolverConfigurations);
        
        // Read operation to be performed
		if (op == null) op = form.getOp();
        if (op2 != null && !op2.isEmpty()) op = op2;
        
        
        if (op==null) {
        	form.reset();
            form.setOp(MSG.actionAddNewSolverConfig());
            op = "list";
        }
        
        // Reset Form
        if (MSG.actionBackToSolverConfigs().equals(op)) {
            form.reset();
            form.setOp(MSG.actionAddNewSolverConfig());
        }
        
        if (MSG.actionAddNewSolverConfig().equals(op)) {
        	form.reset();
        	form.setOp(MSG.actionSaveSolverConfig());
        	form.loadDefaults();
        }
        
        if ("Refresh".equals(op)) {
            form.setOp(form.getUniqueId()==null || form.getUniqueId()<=0 ? MSG.actionSaveSolverConfig(): MSG.actionUpdateSolverConfig());
    		for (SolverParameterDef def: SolverParameterDefDAO.getInstance().findAll()) {
    			if (!def.isVisible().booleanValue()) continue;
    			if (Boolean.TRUE.equals(form.getUseDefault(def.getUniqueId())) && form.getParameter(def.getUniqueId()) == null) {
    				form.setParameter(def.getUniqueId(), def.getDefault());
    			}
    		}
            // form.loadDefaults();
        }
        
        // Add / Update
        if (MSG.actionSaveSolverConfig().equals(op) || MSG.actionUpdateSolverConfig().equals(op)) {
            // Validate input
            form.validate(this);
            if (!hasFieldErrors()) {
            	Transaction tx = null;
            	try {
            		SolverPredefinedSettingDAO dao = new SolverPredefinedSettingDAO();
            		SolverParameterDAO pDao = new SolverParameterDAO();
            		org.hibernate.Session hibSession = dao.getSession();
                    if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                    	tx = hibSession.beginTransaction();
            		SolverPredefinedSetting setting = null;

            		if (MSG.actionSaveSolverConfig().equals(op))
            			setting = new SolverPredefinedSetting();
            		else 
            			setting = dao.get(form.getUniqueId(), hibSession);
                
            		setting.setName(form.getName());
            		setting.setDescription(form.getDescription());                
            		setting.setAppearanceType(form.getAppearanceType());
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
                			String value = form.getParameter(def.getUniqueId());
                			Boolean useDefault = form.getUseDefault(def.getUniqueId());
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
                
                form.reset();
                form.setOp(MSG.actionAddNewSolverConfig());
            } else {
            	form.setOp(op);
            	for (SolverParameterDef def: SolverParameterDefDAO.getInstance().findAll()) {
        			if (!def.isVisible().booleanValue()) continue;
        			if (Boolean.TRUE.equals(form.getUseDefault(def.getUniqueId())) && form.getParameter(def.getUniqueId()) == null) {
        				form.setParameter(def.getUniqueId(), def.getDefault());
        			}
        		}
            }
        }

        // Edit
        if (op.equals("Edit")) {
            if (id == null) {
            	addFieldError("form.uniqueId", MSG.errorRequiredField("Id"));
            } else {
            	Transaction tx = null;
            	try {
            		SolverPredefinedSettingDAO dao = new SolverPredefinedSettingDAO();
            		org.hibernate.Session hibSession = dao.getSession();
                    if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                    	tx = hibSession.beginTransaction();
            	
            		SolverPredefinedSetting setting = dao.get(Long.valueOf(id), hibSession);
            		if (setting==null) {
            			addFieldError("form.name", MSG.errorDoesNotExists(id));
            		} else {
            			form.reset();
            			form.loadDefaults();
                        form.setUniqueId(setting.getUniqueId());
                        form.setName(setting.getName());
                        form.setDescription(setting.getDescription());
            			form.setAppearanceType(setting.getAppearanceType());
            			form.setOp(MSG.actionUpdateSolverConfig());
            			for (SolverParameter param: setting.getParameters()) {
            				if (!param.getDefinition().isVisible().booleanValue()) continue;
            				String value = param.getValue();
            				if ("boolean".equals(param.getDefinition().getType()) && "on".equals(param.getValue()))
            					value = "true";
            				form.setParameter(param.getDefinition().getUniqueId(), value);
            				form.setUseDefault(param.getDefinition().getUniqueId(), Boolean.FALSE);
            			}
                	}
            		if (tx!=null) tx.commit();
            	} catch (Exception e) {
            		if (tx!=null) tx.rollback();
            		Debug.error(e);
            	}
            }
        }
        
        // Export
        if (MSG.actionExportSolverConfig().equals(op)) {
        	if (form.getUniqueId() == null) {
            	addFieldError("form.uniqueId", MSG.errorRequiredField("Id"));
            } else {
                SolverPredefinedSettingDAO dao = new SolverPredefinedSettingDAO();
                org.hibernate.Session hibSession = dao.getSession();
            
                SolverPredefinedSetting setting = dao.get(form.getUniqueId(), hibSession);
                
                if (setting==null) {
        			addFieldError("form.name", MSG.errorDoesNotExists(String.valueOf(form.getUniqueId())));
                } else {
                    PrintWriter pw = ExportUtils.getPlainTextWriter(response, setting.getName() + ".txt");
                    DataProperties properties = null;
                    switch (setting.getAppearanceType().getSolverType()) {
                    case STUDENT:
                    	properties = getStudentSectioningSolverService().createConfig(setting.getUniqueId(), null);
                    	break;
                    case EXAM:
                    	properties = getExaminationSolverService().createConfig(setting.getUniqueId(), null);
                    	break;
                    case INSTRUCTOR:
                    	properties = getInstructorSchedulingSolverService().createConfig(setting.getUniqueId(), null);
                    	break;
                    default:
                    	properties = getCourseTimetablingSolverService().createConfig(setting.getUniqueId(), null);
                    }
                    pw.println("## Solver Configuration File");
                    pw.println("## Reference: " + setting.getName());
                    pw.println("## Name: " + setting.getDescription());
                    pw.println("## Appearance: " + SolverPredefinedSetting.Appearance.values()[setting.getAppearance()].getLabel());
                    pw.println("## Date: " + new Date());
                    pw.println("######################################");
                    for (Iterator i=hibSession.createQuery("select g from SolverParameterGroup g order by g.order").iterate();i.hasNext();) {
                        SolverParameterGroup g = (SolverParameterGroup)i.next();
                        if (setting.getAppearanceType() == SolverPredefinedSetting.Appearance.STUDENT_SOLVER) {
                            if (g.getSolverType() != SolverParameterGroup.SolverType.STUDENT) continue;
                        } else if (setting.getAppearanceType() == SolverPredefinedSetting.Appearance.EXAM_SOLVER) {
                        	if (g.getSolverType() != SolverParameterGroup.SolverType.EXAM) continue;
                        } else if (setting.getAppearanceType() == SolverPredefinedSetting.Appearance.INSTRUCTOR_SOLVER) {
                        	if (g.getSolverType() != SolverParameterGroup.SolverType.INSTRUCTOR) continue;
                        } else {
                        	if (g.getSolverType() != SolverParameterGroup.SolverType.COURSE) continue;
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
            }
        }
        

        // Delete 
        if(MSG.actionDeleteSolverConfig().equals(op)) {
        	Transaction tx = null;
    		
            try {
            	SolverPredefinedSettingDAO dao = new SolverPredefinedSettingDAO();
            	org.hibernate.Session hibSession = dao.getSession();
                if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                	tx = hibSession.beginTransaction();
    			
    			SolverPredefinedSetting setting = dao.get(form.getUniqueId(), hibSession);

    			dao.delete(setting, hibSession);
    			
    			if (tx!=null) tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    			Debug.error(e);
    	    }
            form.reset();
            form.setOp(MSG.actionAddNewSolverConfig());
        }

        if (MSG.actionAddNewSolverConfig().equals(form.getOp())) {
            // Read all existing settings and store in request
            return "list";
        } 
        
        return (MSG.actionSaveSolverConfig().equals(form.getOp()) ? "add" : "edit");
	}
	
    public String getSolverSettingsTable() {
    	Transaction tx = null;
		
		WebTable.setOrder(sessionContext,"solverSettings.ord",request.getParameter("ord"), 1);
		// Create web table instance 
        WebTable webTable = new WebTable( 4,
			    null, "solverSettings.action?ord=%%",
			    new String[] {MSG.fieldReference(), MSG.fieldName(), MSG.fieldAppearance()},
			    new String[] {"left", "left", "left"},
			    null );
        
        try {
        	SolverPredefinedSettingDAO dao = new SolverPredefinedSettingDAO();
        	org.hibernate.Session hibSession = dao.getSession();
            if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            	tx = hibSession.beginTransaction();
            
			List list = hibSession.createCriteria(SolverPredefinedSetting.class).list();
			
			if(list.isEmpty()) {
			    webTable.addLine(null, new String[] {MSG.infoNoSolverConfigs()}, null, null );			    
			} else {
				for (Iterator i=list.iterator();i.hasNext();) {
					SolverPredefinedSetting setting = (SolverPredefinedSetting)i.next();
					String onClick = "onClick=\"document.location='solverSettings.action?op=Edit&id=" + setting.getUniqueId() + "';\"";
					
					webTable.addLine(onClick, new String[] {
							setting.getName(), 
							setting.getDescription(), 
							setting.getAppearanceType().getLabel()},
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

	    return webTable.printTable(WebTable.getOrder(sessionContext,"solverSettings.ord"));
    }	
	
    
    public List<SolverParameterGroup> getSolverParameterGroups() {
    	return (List<SolverParameterGroup>)SolverParameterGroupDAO.getInstance().getSession().createQuery(
    			"from SolverParameterGroup order by order").list();
    }

}

