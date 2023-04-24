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

import java.util.Date;
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
import org.hibernate.Transaction;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ExamPeriodEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamStatus;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Action(value = "examPeriodEdit", results = {
		@Result(name = "list", type = "tiles", location = "examPeriodList.tiles"),
		@Result(name = "add", type = "tiles", location = "examPeriodAdd.tiles"),
		@Result(name = "edit", type = "tiles", location = "examPeriodEdit.tiles"),
		@Result(name = "autosetup", type = "tiles", location = "examPeriodSetup.tiles")
	})
@TilesDefinitions({
	@TilesDefinition(name = "examPeriodList.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Examination Periods"),
			@TilesPutAttribute(name = "body", value = "/admin/examPeriods.jsp")
		}),
	@TilesDefinition(name = "examPeriodAdd.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Add Examination Period"),
			@TilesPutAttribute(name = "body", value = "/admin/examPeriods.jsp")
		}),
	@TilesDefinition(name = "examPeriodEdit.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Edit Examination Period"),
			@TilesPutAttribute(name = "body", value = "/admin/examPeriods.jsp")
		}),
	@TilesDefinition(name = "examPeriodSetup.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Setup Examination Periods"),
			@TilesPutAttribute(name = "body", value = "/admin/examPeriods.jsp")
		})
})
public class ExamPeriodEditAction extends UniTimeAction<ExamPeriodEditForm> {
	private static final long serialVersionUID = 3188159298911284079L;
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	
	private String op2 = null;
	private Long id;
	
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String execute() throws Exception {
		if (form == null)
			form = new ExamPeriodEditForm();
			
        // Check Access
		sessionContext.checkPermission(Right.ExaminationPeriods);

		form.setSession(SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()));
		
        // Read operation to be performed
		if (op == null) op = form.getOp();
		if (op2 != null && !op2.isEmpty()) op = op2;

        if (op == null) {
            form.load(null, sessionContext);
            op = "List";
        }
		form.setOp(op);
        
        List<ExamType> types = ExamType.findAll();
        if (!sessionContext.hasPermission(Right.StatusIndependent) && sessionContext.getUser().getCurrentAuthority().hasRight(Right.ExaminationSolver)) {
        	for (Iterator<ExamType> i = types.iterator(); i.hasNext(); ) {
        		ExamType t = i.next();
        		ExamStatus status = ExamStatus.findStatus(sessionContext.getUser().getCurrentAcademicSessionId(), t.getUniqueId());
            	if (status != null && !status.getManagers().isEmpty()) {
            		boolean hasManager = false;
            		for (TimetableManager m: status.getManagers()) {
            			if (sessionContext.getUser().getCurrentAuthority().hasQualifier(m)) {
            				hasManager = true;
            				break;
            			}
            		}
            		if (!hasManager) i.remove();
            	}
            }
        }
        
        request.setAttribute("examTypes", types);

        // Reset Form
        if (MSG.actionBackToExaminationPeriods().equals(op)) {
            if (form.getUniqueId()!=null)
                request.setAttribute("hash", form.getUniqueId());
            form.load(null, sessionContext);
            form.setOp("List");
        }
        
        if (MSG.actionAddExaminationPeriod().equals(op)) {
            form.load(null, sessionContext);
            form.setOp(MSG.actionSaveExaminationPeriod());
        }

        for (ExamType type: ExamTypeDAO.getInstance().findAll()) {
            if (MSG.actionSetupExaminationPeriods(type.getLabel()).equals(op) && form.getCanAutoSetup(type.getUniqueId())) {
            	form.setAutoSetup(true);
            	form.setExamType(type.getUniqueId());
            	form.setOp(MSG.actionSaveExaminationPeriod());
            }
        }

        // Add / Update
        if (MSG.actionUpdateExaminationPeriod().equals(op) || MSG.actionSaveExaminationPeriod().equals(op)) {
            // Validate input
        	form.validate(this);
            if (hasFieldErrors()) {
                if (form.getAutoSetup()) form.setDays(request);
                form.setOp(form.getUniqueId() < 0 ? MSG.actionSaveExaminationPeriod() : MSG.actionUpdateExaminationPeriod());
            } else {
        		Transaction tx = null;
                try {
                	org.hibernate.Session hibSession = (ExamPeriodDAO.getInstance()).getSession();
                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                		tx = hibSession.beginTransaction();
                	
                	ExamPeriod ep = form.saveOrUpdate(request, sessionContext, hibSession);
                	
                	if (ep!=null) {
                		ChangeLog.addChange(
                            hibSession, 
                            sessionContext, 
                            ep, 
                            ChangeLog.Source.EXAM_PERIOD_EDIT, 
                            ("Save".equals(op)?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                            null, 
                            null);
                	}

                    if (tx!=null) tx.commit();
        	    } catch (Exception e) {
        	        e.printStackTrace();
        	    	if (tx!=null) tx.rollback();
        	    	throw e;
        	    }

                form.setOp("List");
                if (form.getUniqueId()!=null)
                    request.setAttribute("hash", form.getUniqueId());
            }
        }

        // Edit
        if ("Edit".equals(op)) {
            if (id==null) {
            	addFieldError("form.uniqueId", MSG.errorExaminationIdNotProvided());
                return "list";
            } else {
            	ExamPeriod ep = (ExamPeriodDAO.getInstance()).get(Long.valueOf(id));
                if(ep==null) {
                	addFieldError("form.uniqueId", MSG.errorExaminationIdNotProvided());
                    return "list";
                } else {
                	form.load(ep, sessionContext);
                }
            }
        }

        // Delete 
        if (MSG.actionDeleteExaminationPeriod().equals(op)) {
    		Transaction tx = null;
    		
            try {
            	org.hibernate.Session hibSession = (ExamPeriodDAO.getInstance()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
                ExamPeriod ep = (ExamPeriodDAO.getInstance()).get(form.getUniqueId(), hibSession);
                ChangeLog.addChange(
                        hibSession, 
                        sessionContext, 
                        ep, 
                        ChangeLog.Source.EXAM_PERIOD_EDIT, 
                        ChangeLog.Operation.DELETE, 
                        null, 
                        null);

                form.delete(sessionContext, hibSession);
            	
    			tx.commit();
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

    	    form.load(null, sessionContext);
            form.setOp("List");
        }
        
        if ("List".equals(form.getOp())) {
            return "list";
        } 
        
        if ("Reload".equals(form.getOp())) {
        	if (form.getExamType() != null && form.getExamType() >= 0) {
        		sessionContext.setAttribute(SessionAttribute.ExamType, form.getExamType());
        		form.load(null, sessionContext);
        	} else {
        		form.reset();
        		form.setEditable(true);
        	}
        	form.setOp(MSG.actionSaveExaminationPeriod());
        }
        
        return (form.getAutoSetup()?"autosetup" : form.getUniqueId() < 0 ? "add" : "edit");
	}

    public String getExamPeriods() {
		WebTable.setOrder(sessionContext,"examPeriods.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = new WebTable( 8,
			    null, "examPeriodEdit.action?ord=%%",
			    new String[] {
			    		MSG.colType(),
			    		MSG.colDate(),
			    		MSG.colStartTime(),
			    		MSG.colEndTime(),
			    		MSG.colExamLength(),
			    		MSG.colEventStartOffset(),
			    		MSG.colEventStopOffset(),
			    		MSG.colPreference()},
			    new String[] {"left","left", "left", "left", "right", "right", "right", "left"},
			    null );
        
        TreeSet periods = ExamPeriod.findAll(sessionContext.getUser().getCurrentAcademicSessionId(), (Long)null);
		if(periods.isEmpty()) {
		    webTable.addLine(null, new String[] {MSG.infoNoExaminationPeriodsDefined()}, null, null );			    
		}
		
        Formats.Format<Date> sdf = Formats.getDateFormat(Formats.Pattern.DATE_MEETING);
        Formats.Format<Date> stf = Formats.getDateFormat(Formats.Pattern.TIME_SHORT);

        Set<ExamType> types = null;
        if (!sessionContext.hasPermission(Right.StatusIndependent) && sessionContext.getUser().getCurrentAuthority().hasRight(Right.ExaminationSolver)) {
            types = new HashSet<ExamType>();
            for (ExamType t: ExamType.findAll()) {
            	ExamStatus status = ExamStatus.findStatus(sessionContext.getUser().getCurrentAcademicSessionId(), t.getUniqueId());
            	if (status != null && !status.getManagers().isEmpty()) {
            		for (TimetableManager m: status.getManagers()) {
            			if (sessionContext.getUser().getCurrentAuthority().hasQualifier(m)) {
            				types.add(t);
            				break;
            			}
            		}
            	} else {
            		types.add(t);
            	}
            }
        }
        
        for (Iterator i=periods.iterator();i.hasNext();) {
        	ExamPeriod ep = (ExamPeriod)i.next();
        	if (types != null && !types.contains(ep.getExamType())) continue;
        	String onClick = "onClick=\"document.location='examPeriodEdit.action?op=Edit&id=" + ep.getUniqueId() + "';\"";
        	webTable.addLine(onClick, new String[] {
        			ep.getExamType().getLabel(),
        	        "<a name='"+ep.getUniqueId()+"'>"+sdf.format(ep.getStartDate())+"</a>",
        	        stf.format(ep.getStartTime()),
        	        stf.format(ep.getEndTime()),
        	        String.valueOf(Constants.SLOT_LENGTH_MIN*ep.getLength()),
        	        String.valueOf(Constants.SLOT_LENGTH_MIN*ep.getEventStartOffset()),
        	        String.valueOf(Constants.SLOT_LENGTH_MIN*ep.getEventStopOffset()),
        	        (PreferenceLevel.sNeutral.equals(ep.getPrefLevel().getPrefProlog())?"":
        	        "<font color='"+PreferenceLevel.prolog2color(ep.getPrefLevel().getPrefProlog())+"'>"+ep.getPrefLevel().getPrefName()+"</font>")},
        	        new Comparable[] {
        			ep.getExamType(),ep.getStartDate(), ep.getStartSlot(), ep.getStartSlot()+ep.getLength(), ep.getLength(), ep.getEventStartOffset(), ep.getEventStopOffset(), ep.getPrefLevel().getPrefId()});
        }
        
	    return webTable.printTable(WebTable.getOrder(sessionContext,"examPeriods.ord"));
    }	
}

