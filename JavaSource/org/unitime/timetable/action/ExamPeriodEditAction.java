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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.unitime.timetable.form.ExamPeriodEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamStatus;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Service("/examPeriodEdit")
public class ExamPeriodEditAction extends Action {
	
	
	@Autowired SessionContext sessionContext;
	
	// --------------------------------------------------------- Instance Variables
	
	// --------------------------------------------------------- Methods
	
	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			ExamPeriodEditForm myForm = (ExamPeriodEditForm) form;
			
	        // Check Access
			sessionContext.checkPermission(Right.ExaminationPeriods);
	        
	        // Read operation to be performed
	        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
	        
	        if (op == null && request.getParameter("op2") != null) {
	        	op = request.getParameter("op2");
	        	myForm.setOp(op);
	        }
	        
	        if (op==null) {
	            myForm.load(null, sessionContext);
	            myForm.setOp("List");
	        }
	        
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
	        if ("Back".equals(op)) {
	            if (myForm.getUniqueId()!=null)
	                request.setAttribute("hash", myForm.getUniqueId());
	            myForm.load(null, sessionContext);
	            myForm.setOp("List");
	        }
	        
            if ("Add Period".equals(op)) {
                myForm.load(null, sessionContext);
                myForm.setOp("Save");
            }

            for (ExamType type: ExamTypeDAO.getInstance().findAll()) {
                if ((type.getLabel() + " Periods").equals(op) && myForm.getCanAutoSetup(type.getUniqueId())) {
                	myForm.setAutoSetup(true);
                	myForm.setExamType(type.getUniqueId());
                    myForm.setOp("Save");
                }
            }

            // Add / Update
	        if ("Update".equals(op) || "Save".equals(op)) {
	            // Validate input
	            ActionMessages errors = myForm.validate(mapping, request);
	            if(errors.size()>0) {
	                saveErrors(request, errors);
	                if (myForm.getAutoSetup()) myForm.setDays(request);
	                myForm.setOp(myForm.getUniqueId().longValue()<0?"Save":"Update");
	            } else {
	        		Transaction tx = null;
	        		
	                try {
	                	org.hibernate.Session hibSession = (new ExamPeriodDAO()).getSession();
	                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
	                		tx = hibSession.beginTransaction();
	                	
	                	ExamPeriod ep = myForm.saveOrUpdate(request, sessionContext, hibSession);
	                	
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

	                myForm.setOp("List");
	                if (myForm.getUniqueId()!=null)
	                    request.setAttribute("hash", myForm.getUniqueId());
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
	            	ExamPeriod ep = (new ExamPeriodDAO()).get(new Long(id));
	            	
	                if(ep==null) {
	                    errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
	                    saveErrors(request, errors);
	                    return mapping.findForward("list");
	                } else {
	                	myForm.load(ep, sessionContext);
	                }
	            }
	        }

	        // Delete 
	        if("Delete".equals(op)) {
	    		Transaction tx = null;
	    		
	            try {
	            	org.hibernate.Session hibSession = (new ExamPeriodDAO()).getSession();
	            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
	            		tx = hibSession.beginTransaction();
	            	
                    ExamPeriod ep = (new ExamPeriodDAO()).get(myForm.getUniqueId(), hibSession);
                    ChangeLog.addChange(
                            hibSession, 
                            sessionContext, 
                            ep, 
                            ChangeLog.Source.EXAM_PERIOD_EDIT, 
                            ChangeLog.Operation.DELETE, 
                            null, 
                            null);

                    myForm.delete(sessionContext, hibSession);
	            	
	    			tx.commit();
	    	    } catch (Exception e) {
	    	        e.printStackTrace();
	    	    	if (tx!=null) tx.rollback();
	    	    	throw e;
	    	    }

	    	    myForm.load(null, sessionContext);
	            myForm.setOp("List");
	        }
	        
	        if ("List".equals(myForm.getOp())) {
	            // Read all existing settings and store in request
	            getExamPeriods(request);
	            return mapping.findForward("list");
	        } 
	        
	        if ("Reload".equals(myForm.getOp())) {
	        	if (myForm.getExamType() != null && myForm.getExamType() >= 0) {
	        		sessionContext.setAttribute("Exam.Type", myForm.getExamType());
	        		myForm.load(null, sessionContext);
	        	} else {
	        		myForm.reset(mapping, request);
	        		myForm.setEditable(true);
	        	}
	        	myForm.setOp("Save");
	        }
	        
	        return mapping.findForward(myForm.getAutoSetup()?"midterm":myForm.getUniqueId().longValue()<0?"add":"edit");
	        
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}

    private void getExamPeriods(HttpServletRequest request) throws Exception {
		WebTable.setOrder(sessionContext,"examPeriods.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = new WebTable( 8,
			    null, "examPeriodEdit.do?ord=%%",
			    new String[] {"Type","Date", "Start Time", "End Time", "Length", "Event Start Offset", "Event Stop Offset", "Preference"},
			    new String[] {"left","left", "left", "left", "right", "right", "right", "left"},
			    null );
        
        TreeSet periods = ExamPeriod.findAll(sessionContext.getUser().getCurrentAcademicSessionId(), (Long)null);
		if(periods.isEmpty()) {
		    webTable.addLine(null, new String[] {"No examination periods defined for this session."}, null, null );			    
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
        	String onClick = "onClick=\"document.location='examPeriodEdit.do?op=Edit&id=" + ep.getUniqueId() + "';\"";
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
        
	    request.setAttribute("ExamPeriods.table", webTable.printTable(WebTable.getOrder(sessionContext,"examPeriods.ord")));
    }	
}

