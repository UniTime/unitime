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

import java.util.Iterator;
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
import org.unitime.timetable.form.RoomTypeEditForm;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.dao.RoomTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;


/** 
 * @author Tomas Muller
 */
@Service("/roomTypeEdit")
public class RoomTypeEditAction extends Action {
	
	@Autowired SessionContext sessionContext;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
		RoomTypeEditForm myForm = (RoomTypeEditForm) form;
		
        // Check Access
		sessionContext.checkPermission(Right.RoomTypes);
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
            op = request.getParameter("op2");

        if (op==null) {
            myForm.reset(mapping, request);
        }
        
        // Reset Form
        if ("Back".equals(op)) {
            myForm.reset(mapping, request);
        }
        
        if ("Add Room Type".equals(op)) {
            myForm.load(null);
        }

        // Add / Update
        if ("Update".equals(op) || "Save".equals(op)) {
            // Validate input
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
            } else {
        		Transaction tx = null;
        		
                try {
                	org.hibernate.Session hibSession = RoomTypeDAO.getInstance().getSession();
                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                		tx = hibSession.beginTransaction();
                	
                	myForm.saveOrUpdate(hibSession);
                	
        			if (tx!=null) tx.commit();
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        	    	throw e;
        	    }

                myForm.reset(mapping, request);
            }
        }

        // Edit
        if("Edit".equals(op)) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("reference", new ActionMessage("errors.invalid", "Unique Id : " + id));
                saveErrors(request, errors);
            } else {
                RoomType t = RoomTypeDAO.getInstance().get(new Long(id));
            	
                if(t==null) {
                    errors.add("reference", new ActionMessage("errors.invalid", "Unique Id : " + id));
                    saveErrors(request, errors);
                } else {
                	myForm.load(t);
                }
            }
        }

        // Delete 
        if("Delete".equals(op)) {
    		Transaction tx = null;
    		
            try {
            	org.hibernate.Session hibSession = RoomTypeDAO.getInstance().getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	myForm.delete(hibSession);
            	
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

    	    myForm.reset(mapping, request);
        }
        
        // Move Up or Down
        if("Move Up".equals(op) || "Move Down".equals(op)) {
            Transaction tx = null;
            
            try {
                org.hibernate.Session hibSession = RoomTypeDAO.getInstance().getSession();
                if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                    tx = hibSession.beginTransaction();
                
                RoomType curType = RoomTypeDAO.getInstance().get(myForm.getUniqueId());
                
                if ("Move Up".equals(op)) {
                    boolean found = false;
                    for (Iterator i=RoomType.findAll().iterator();i.hasNext();) {
                        RoomType s = (RoomType)i.next();
                        if (s.getOrd()+1==curType.getOrd()) {
                            s.setOrd(s.getOrd()+1); 
                            hibSession.saveOrUpdate(s);
                            found = true;
                        }
                    }
                    if (found) {
                        curType.setOrd(curType.getOrd()-1);
                        myForm.setOrder(curType.getOrd());
                        hibSession.saveOrUpdate(curType);
                    }
                } else {
                    boolean found = false;
                    for (Iterator i=RoomType.findAll().iterator();i.hasNext();) {
                        RoomType s = (RoomType)i.next();
                        if (s.getOrd()-1==curType.getOrd()) {
                            s.setOrd(s.getOrd()-1); 
                            hibSession.saveOrUpdate(s);
                            found = true;
                        }
                    }
                    if (found) {
                        curType.setOrd(curType.getOrd()+1);
                        myForm.setOrder(curType.getOrd());
                        hibSession.saveOrUpdate(curType);
                    }
                }
                
                if (tx!=null) tx.commit();
            } catch (Exception e) {
                if (tx!=null) tx.rollback();
                Debug.error(e);
            }
            myForm.reset(mapping, request);
        }

        if ("List".equals(myForm.getOp())) {
            // Read all existing settings and store in request
            getRoomTypeList(request);
            return mapping.findForward("list");
        }
        
        return mapping.findForward("Save".equals(myForm.getOp())?"add":"edit");
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}
	
    private void getRoomTypeList(HttpServletRequest request) throws Exception {
		WebTable.setOrder(sessionContext,"roomTypes.ord",request.getParameter("ord"),2);
		// Create web table instance 
        WebTable webTable = new WebTable( 5,
			    null, "roomTypeEdit.do?ord=%%",
			    new String[] {
                "","Reference", "Label", "Type", "Rooms"},
			    new String[] {"left","left", "left","left", "left"},
			    null );
        
        TreeSet types = RoomType.findAll();
		if(types.isEmpty()) {
		    webTable.addLine(null, new String[] {"No status defined."}, null, null );			    
		}
		
        for (Iterator i=types.iterator();i.hasNext();) {
            RoomType t = (RoomType)i.next();
        	String onClick = "onClick=\"document.location='roomTypeEdit.do?op=Edit&id=" + t.getUniqueId() + "';\"";
            String ops = "";
            if (t.getOrd().intValue()>0) {
                ops += "<img src='images/arrow_up.png' border='0' align='absmiddle' title='Move Up' " +
                		"onclick=\"roomTypeEditForm.op2.value='Move Up';roomTypeEditForm.uniqueId.value='"+t.getUniqueId()+"';roomTypeEditForm.submit(); event.cancelBubble=true;\">";
            } else
                ops += "<img src='images/blank.png' border='0' align='absmiddle'>";
            if (i.hasNext()) {
                ops += "<img src='images/arrow_down.png' border='0' align='absmiddle' title='Move Down' " +
                		"onclick=\"roomTypeEditForm.op2.value='Move Down';roomTypeEditForm.uniqueId.value='"+t.getUniqueId()+"';roomTypeEditForm.submit(); event.cancelBubble=true;\">";
            } else
                ops += "<img src='images/blank.png' border='0' align='absmiddle'>";
            int nrRooms = t.countRooms();
            webTable.addLine(onClick, new String[] {
                    ops,
                    t.getReference(),
                    t.getLabel(),
                    (t.isRoom()?"Room":"Other"),
                    String.valueOf(nrRooms),
        		},new Comparable[] {
                    t.getOrd(),
        			t.getOrd(),
                    t.getLabel(),
                    (t.isRoom()?0:1),
                    nrRooms,
        		});
        }
        
        request.setAttribute("RoomType.last", new Integer(types.size()-1));
	    request.setAttribute("RoomType.table", webTable.printTable(WebTable.getOrder(sessionContext,"roomTypes.ord")));
    }	
}

