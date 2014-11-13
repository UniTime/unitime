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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.TimetableManagerForm;
import org.unitime.timetable.interfaces.ExternalUidLookup;
import org.unitime.timetable.interfaces.ExternalUidLookup.UserInfo;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.RolesComparator;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.RolesDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/**
 * MyEclipse Struts
 * Creation date: 04-06-2005
 *
 * XDoclet definition:
 * @struts:action path="/timetableManagerEdit" name="timetableManagerForm" input="/admin/timetableManagerList.jsp" scope="request" validate="true"
 * @struts:action-forward name="success" path="timetableManagerList.do" redirect="true"
 * @struts:action-forward name="fail" path="timetableManagerList.do" redirect="true"
 *
 * @author Tomas Muller, Stephanie Schluttenhofer, Heston Fernandes
 */
@Service("/timetableManagerEdit")
public class TimetableManagerEditAction extends Action {
	
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
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        // Check access
    	sessionContext.checkPermission(Right.TimetableManagers);

        MessageResources rsc = getResources(request);
        TimetableManagerForm frm = (TimetableManagerForm) form;

        // Read Operation
        String op = (request.getParameter("op")==null) 
						? (frm.getOp()==null || frm.getOp().length()==0)
						        ? (request.getAttribute("op")==null)
						                ? null
						                : request.getAttribute("op").toString()
						        : frm.getOp()
						: request.getParameter("op");		        
	        
		if(op==null || op.trim().length()==0)
		    throw new Exception ("Operation could not be interpreted: " + op);

		// Set up Departments
		LookupTables.setupDepts(request, sessionContext.getUser().getCurrentAcademicSessionId());
		request.setAttribute("solverGroupList", SolverGroup.findBySessionId(sessionContext.getUser().getCurrentAcademicSessionId()));
        frm.setOp(op);

        // Back
        if (op.equalsIgnoreCase(rsc.getMessage("button.backToManagerList"))) {
        	if (frm.getUniqueId()!=null && frm.getUniqueId().trim().length()>0)
           		request.setAttribute(Constants.JUMP_TO_ATTR_NAME, frm.getUniqueId());
               	
            return mapping.findForward("displayManagerList");
        }
        
        // Redirect from Manager List - Edit Manager
        if (op.equalsIgnoreCase(rsc.getMessage("op.edit"))) {
            frm.setOp1("2");
            loadForm(request, frm);
        }
        
        // Redirect from Manager List - Add Manager
        if (op.equalsIgnoreCase(rsc.getMessage("button.addTimetableManager"))) {
        	sessionContext.checkPermission(Right.TimetableManagerAdd);
            frm.setOp1("1");
            frm.setLookupEnabled(ApplicationProperty.ManagerExternalIdLookup.isTrue() && ApplicationProperty.ManagerExternalIdLookupClass.value() != null);
        }
        
        // Lookup puid / career account
        if (op.equalsIgnoreCase(rsc.getMessage("button.lookupManager"))) {
            // Do nothing - taken care below
        }

        String mapPath = frm.getOp1().equals("1") ? "addManagerInfo" : "editManagerInfo";
        
        // Add Role
        if (op.equalsIgnoreCase(rsc.getMessage("button.addRole"))) {
            ActionMessages errors = frm.validate(mapping, request);
            if(!errors.isEmpty()) {
                saveErrors(request, errors);
                setupRoles(request, frm);
                lookupManager(frm);
                return mapping.findForward(mapPath);
            }

            Roles role = new RolesDAO().get(new Long(frm.getRole()));
            frm.addToRoles(role); 
            if (frm.getRoles().size()==1)
                frm.setPrimaryRole(role.getRoleId().toString());
            frm.getRoleReceiveEmailFlags().add(new Boolean(true));
      }
        
        // Add Department
        if (op.equalsIgnoreCase(rsc.getMessage("button.addDepartment"))) {
            ActionMessages errors = frm.validate(mapping, request);
            if(!errors.isEmpty()) {
                saveErrors(request, errors);
                setupRoles(request, frm);
                lookupManager(frm);
                return mapping.findForward(mapPath);
            }

            Department dept = new DepartmentDAO().get(new Long(frm.getDept()));            
            frm.addToDepts(dept);    
        }
        
        // Add Solver Group
        if (op.equalsIgnoreCase(rsc.getMessage("button.addSolverGroup"))) {
            ActionMessages errors = frm.validate(mapping, request);
            if(!errors.isEmpty()) {
                saveErrors(request, errors);
                setupRoles(request, frm);
                lookupManager(frm);
                return mapping.findForward(mapPath);
            }

            SolverGroup sg = new SolverGroupDAO().get(new Long(frm.getSolverGr()));            
            frm.addToSolverGrs(sg);    
        }

        // Add new manager
        if (op.equalsIgnoreCase(rsc.getMessage("button.insertTimetableManager"))) {
            frm.setOp1("1");

            ActionMessages errors = frm.validate(mapping, request);
            if(!errors.isEmpty()) {
                saveErrors(request, errors);
                setupRoles(request, frm);
                try {
                    lookupManager(frm);
                }
                catch (Exception e) {}
                return mapping.findForward(mapPath);
            }

            addManager(request, frm);
            return mapping.findForward("displayManagerList");
        }
        
        // Update Manager
        if (op.equalsIgnoreCase(rsc.getMessage("button.updateTimetableManager"))) {
            frm.setOp1("2");

            ActionMessages errors = frm.validate(mapping, request);
            if(!errors.isEmpty()) {
                saveErrors(request, errors);
                setupRoles(request, frm);
                try {
                    lookupManager(frm);
                }
                catch (Exception e) {}
                return mapping.findForward(mapPath);
            }

            updateManager(request, frm);
            return mapping.findForward("displayManagerList");
        }
        
        // Delete Manager
        if (op.equalsIgnoreCase(rsc.getMessage("button.delete"))
                || op.equalsIgnoreCase(rsc.getMessage("button.deleteTimetableManager")) ) {
            
            String deleteType = request.getParameter("deleteType");
            String deleteId = request.getParameter("deleteId");
            
            if (deleteType.equalsIgnoreCase("dept")) {
                frm.removeFromDepts(Integer.parseInt(deleteId));
            }
            
            if (deleteType.equalsIgnoreCase("solverGr")) {
                frm.removeFromSolverGrs(Integer.parseInt(deleteId));
            }

            if (deleteType.equalsIgnoreCase("role")) {
                frm.removeFromRoles(Integer.parseInt(deleteId));
                if (frm.getRoles().size()==1)
                    frm.setPrimaryRole(frm.getRoles(0));
            }

            if (deleteType.equalsIgnoreCase("manager")) {
                frm.setOp1("2");
                deleteManager(request, frm);
                return mapping.findForward("displayManagerList");
            }
        }

        // Get manager details
        try {
            lookupManager(frm);
        }
        catch (Exception e) {
            ActionMessages errors = new ActionMessages();
            errors.add("puid", new ActionMessage("errors.generic", e.getMessage()));
            saveErrors(request, errors);
            setupRoles(request, frm);
            return mapping.findForward(mapPath);
        }
        
        // Get roles not already assigned
        setupRoles(request, frm);
        
        return mapping.findForward(mapPath);
    }

    /**
     * Lookup manager details from I2A2
     * @param frm
     */
    private void lookupManager(TimetableManagerForm frm) throws Exception{
        String id = frm.getExternalId();
        if (id!=null && id.trim().length()>0 && frm.getLookupEnabled().booleanValue()) {
            
        	String className = ApplicationProperty.ManagerExternalIdLookupClass.value();        	
        	ExternalUidLookup lookup = (ExternalUidLookup) (Class.forName(className).newInstance());
       		UserInfo results = lookup.doLookup(id);
       		if (results == null) return;
			frm.setExternalId(results.getExternalId());
			frm.setLookupResult(results.getUserName());
			if (frm.getFirstName() == null || frm.getFirstName().trim().length() == 0){
				frm.setFirstName(results.getFirstName());
			}
			if (frm.getMiddleName() == null || frm.getMiddleName().trim().length() == 0){
				frm.setMiddleName(results.getMiddleName());
			}
			if (frm.getLastName() == null || frm.getLastName().trim().length() == 0){
				frm.setLastName(results.getLastName());
			}
			if (frm.getTitle() == null || frm.getTitle().trim().length() == 0){
				frm.setTitle(results.getAcademicTitle());
			}
			frm.setEmail(results.getEmail());
        }
    }

    /**
     * Display only those roles not already assigned to the manager
     * @param request
     */
    private void setupRoles(HttpServletRequest request, TimetableManagerForm frm ) {
        Set<Roles> roles = Roles.findAll(true);
        
        if (!sessionContext.hasPermission(Right.SessionIndependent))
        	for (Iterator<Roles> i = roles.iterator(); i.hasNext(); )
        		if (i.next().hasRight(Right.SessionIndependent)) i.remove();
        
        if (frm.getRoleRefs() != null && !frm.getRoleRefs().isEmpty())
        	for (Iterator<Roles> i = roles.iterator(); i.hasNext(); )
        		if (frm.getRoleRefs().contains(i.next().getReference()))
        			i.remove();
        
        request.setAttribute(Roles.ROLES_ATTR_NAME, roles);
    }

    /**
     * Load details of manager from database
     * @param request
     * @param frm
     * @throws Exception
     */
    private void loadForm (
            HttpServletRequest request, 
            TimetableManagerForm frm ) throws Exception {
        
        Long mgrId = null;
        String uniqueId = request.getParameter("id");
        if(uniqueId==null || uniqueId.trim().length()==0)
            throw new Exception ("Manager Id could not be read.");
        
        try {
            mgrId = new Long (uniqueId);
        }
        catch (Exception e) {
            throw new Exception ("Invalid Manager Id : " + uniqueId);
        }
        
        sessionContext.checkPermission(mgrId, "TimetableManager", Right.TimetableManagerEdit);

        frm.setUniqueId(uniqueId);
        TimetableManagerDAO mgrDao = new TimetableManagerDAO();
        TimetableManager mgr = mgrDao.get(mgrId);
        
        frm.setEmail(mgr.getEmailAddress());
        frm.setExternalId(mgr.getExternalUniqueId());
        
        Set rolesSet = mgr.getManagerRoles();
        ArrayList roles = new ArrayList(rolesSet);
        Collections.sort(roles, new RolesComparator());
        
        for (Iterator i=roles.iterator(); i.hasNext(); ) {
            ManagerRole mgrRole = (ManagerRole) i.next();
            Roles role = mgrRole.getRole();
            frm.addToRoles(role);
            if (mgrRole.isPrimary().booleanValue())
                frm.setPrimaryRole(role.getRoleId().toString());
            frm.getRoleReceiveEmailFlags().add(mgrRole.isReceiveEmails() == null? new Boolean(false): mgrRole.isReceiveEmails());
        }

        Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
        Set depts = mgr.getDepartments();
        for (Iterator i=depts.iterator(); i.hasNext(); ) {
            Department dept = (Department) i.next();
            if(dept.getSessionId().equals(sessionId))
                frm.addToDepts(dept);
        }        
        for (Iterator i=mgr.getSolverGroups().iterator(); i.hasNext(); ) {
        	SolverGroup sg = (SolverGroup) i.next();
            if(sg.getSession().getUniqueId().equals(sessionId))
                frm.addToSolverGrs(sg);
        }        
        
        if (ApplicationProperty.ManagerExternalIdLookup.isTrue() && ApplicationProperty.ManagerExternalIdLookupClass.value() != null) {
        	frm.setLookupEnabled(Boolean.TRUE);
            frm.setFirstName(mgr.getFirstName());
            frm.setMiddleName(mgr.getMiddleName());
            frm.setLastName(mgr.getLastName());
            frm.setTitle(mgr.getAcademicTitle());
        } else {
        	frm.setLookupEnabled(Boolean.FALSE);
            frm.setFirstName(mgr.getFirstName());
            frm.setMiddleName(mgr.getMiddleName());
            frm.setLastName(mgr.getLastName());
            frm.setTitle(mgr.getAcademicTitle());
        }
    }

    /**
     * Add New Manager
     * @param request
     * @param frm
     * @throws Exception
     */
    private void addManager(
            HttpServletRequest request, TimetableManagerForm frm ) throws Exception {

    	sessionContext.checkPermission(Right.TimetableManagerAdd);
    	
        lookupManager(frm);
        
        TimetableManagerDAO mgrDao = new TimetableManagerDAO();
        RolesDAO rDao = new RolesDAO();
        DepartmentDAO dDao = new DepartmentDAO();
        SolverGroupDAO sgDao = new SolverGroupDAO();
        
        Session hibSession = mgrDao.getSession();
        
        Transaction tx = hibSession.beginTransaction();

        TimetableManager mgr = new TimetableManager();
        mgr.setFirstName(frm.getFirstName());
        mgr.setMiddleName(frm.getMiddleName());
        mgr.setLastName(frm.getLastName());
        mgr.setAcademicTitle(frm.getTitle());
        mgr.setExternalUniqueId(frm.getExternalId());
        mgr.setEmailAddress(frm.getEmail());
        
        // Add Roles
        List roles = frm.getRoles();
        List roleReceiveEmails = frm.getRoleReceiveEmailFlags();
        Iterator receiveEmailIt = roleReceiveEmails.iterator();
       	for (Iterator i=roles.iterator(); i.hasNext(); ) {
       	    Roles role = rDao.get(new Long(i.next().toString()));
       	    ManagerRole mgrRole = new ManagerRole();
       	    mgrRole.setRole(role);
       	    mgrRole.setTimetableManager(mgr);
       	    if (frm.getPrimaryRole().equals(role.getRoleId().toString()))
       	        mgrRole.setPrimary(new Boolean(true));
       	    else
       	        mgrRole.setPrimary(new Boolean(false));
       	    if (receiveEmailIt.hasNext()){
       	    	String receiveEmailsStr = (String) receiveEmailIt.next();
       	    	Boolean receiveEmails = new Boolean("on".equalsIgnoreCase(receiveEmailsStr)); 
       	    	mgrRole.setReceiveEmails(receiveEmails);
       	    } else {
       	    	mgrRole.setReceiveEmails(new Boolean(false));
       	    }
       	    
       	    mgr.addTomanagerRoles(mgrRole);
       	}        
		hibSession.saveOrUpdate(mgr);

       	// Add departments
		mgr.setDepartments(new HashSet<Department>());
       	for (Iterator i=frm.getDepts().iterator(); i.hasNext(); ) {
       	    Department dept = dDao.get(new Long(i.next().toString()));
       	    mgr.getDepartments().add(dept);
       	    dept.getTimetableManagers().add(mgr);
    		hibSession.saveOrUpdate(dept);
       	}
       	
       	mgr.setSolverGroups(new HashSet<SolverGroup>());
       	for (Iterator i=frm.getSolverGrs().iterator(); i.hasNext(); ) {
       	    SolverGroup sg = sgDao.get(new Long(i.next().toString()));
       	    mgr.getSolverGroups().add(sg);
       	    sg.getTimetableManagers().add(mgr);
    		hibSession.saveOrUpdate(sg);
       	}

        ChangeLog.addChange(
                hibSession, 
                sessionContext, 
                mgr, 
                ChangeLog.Source.MANAGER_EDIT, 
                ChangeLog.Operation.CREATE, 
                null, 
                null);
        
       	tx.commit();

       	if (mgr.getUniqueId()!=null)
       		request.setAttribute(Constants.JUMP_TO_ATTR_NAME, mgr.getUniqueId().toString());
       	
    }

    /**
     * Update Manager Details
     * @param request
     * @param frm
     */
    private void updateManager(
            HttpServletRequest request, TimetableManagerForm frm ) throws Exception {
        
    	sessionContext.checkPermission(frm.getUniqueId(), "TimetableManager", Right.TimetableManagerEdit);
    	
        lookupManager(frm);
        
        TimetableManagerDAO mgrDao = new TimetableManagerDAO();
        RolesDAO rDao = new RolesDAO();
        DepartmentDAO dDao = new DepartmentDAO();
        SolverGroupDAO sgDao = new SolverGroupDAO();
        
        Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
        
        Session hibSession = mgrDao.getSession();
        
        Transaction tx = hibSession.beginTransaction();

        TimetableManager mgr = mgrDao.get(new Long(frm.getUniqueId()));
        mgr.setFirstName(frm.getFirstName());
        mgr.setMiddleName(frm.getMiddleName());
        mgr.setLastName(frm.getLastName());
        mgr.setAcademicTitle(frm.getTitle());
        mgr.setExternalUniqueId(frm.getExternalId());
        mgr.setEmailAddress(frm.getEmail());

        // Update Roles
        List roles = frm.getRoles();
        List roleReceiveEmails = frm.getRoleReceiveEmailFlags();
        Set mgrRoles = mgr.getManagerRoles();
        if (mgrRoles==null)
            mgrRoles = new HashSet();
        
        // Check if roles added or updated
        Iterator receiveEmailIt = roleReceiveEmails.iterator();
       	for (Iterator i=roles.iterator(); i.hasNext(); ) {
       	    Roles role = rDao.get(new Long(i.next().toString()));
       	    Boolean receiveEmail = new Boolean(false);
       	    if (receiveEmailIt.hasNext()){
       	    	String str = (String)receiveEmailIt.next();
       	    	str = (str == null?"false":(str.equalsIgnoreCase("on")?"true":str));
       	    	receiveEmail = new Boolean(str);
       	    }
       	    boolean found = false;
       	    
       	    // Check if role already exists
           	for (Iterator j=mgrRoles.iterator(); j.hasNext(); ) {
           	    ManagerRole eMgrRole = (ManagerRole) j.next();
           	    Roles eRole = eMgrRole.getRole();
           	    
           	    // Exists - check if primary
           	    if (eRole.equals(role)) {
               	    if (frm.getPrimaryRole().equals(role.getRoleId().toString()))
               	        eMgrRole.setPrimary(new Boolean(true));
               	    else
               	        eMgrRole.setPrimary(new Boolean(false));
               	    
               	    found = true;
               	    eMgrRole.setReceiveEmails(receiveEmail);
              	    break;
               	    
           	    }
           	    
           	}       	  
           	
           	// Role does not exist - add  
           	if (!found) {
	       	    ManagerRole mgrRole = new ManagerRole();
	       	    mgrRole.setRole(role);
	       	    mgrRole.setTimetableManager(mgr);
	       	    if (frm.getPrimaryRole().equals(role.getRoleId().toString()))
	       	        mgrRole.setPrimary(new Boolean(true));
	       	    else
	       	        mgrRole.setPrimary(new Boolean(false));
	       	    mgrRole.setReceiveEmails(receiveEmail);
	       	    mgr.addTomanagerRoles(mgrRole);
           	}           	
       	}        

       	// Check if roles deleted
       	for (Iterator j=mgrRoles.iterator(); j.hasNext(); ) {
       	    ManagerRole eMgrRole = (ManagerRole) j.next();
       	    Roles eRole = eMgrRole.getRole();
       	    boolean found = false;

           	for (Iterator i=roles.iterator(); i.hasNext(); ) {
           	    Roles role = rDao.get(new Long(i.next().toString()));
           	    if (eRole.equals(role)) {
           	        found = true;
           	        break;
           	    }
           	}
           	
           	if (!found) {
           	    j.remove();
           	}
       	}       	
       	
       	// Update departments
       	List depts = frm.getDepts();
       	Set mgrDepts = mgr.getDepartments();
       	if (mgrDepts==null) {
       	    mgrDepts = new HashSet();
       	    mgr.setDepartments(mgrDepts);
       	}
       	
        // Check if depts added or updated
       	for (Iterator i=depts.iterator(); i.hasNext(); ) {
       	    Department dept = dDao.get(new Long(i.next().toString()));
       	    boolean found = false;
           	for (Iterator j=mgrDepts.iterator(); j.hasNext(); ) {
           	    Department eDept = (Department) j.next();
           	    if (eDept.equals(dept)) {
           	        found = true;
           	        break;
           	    }
           	}
           	
           	if (!found){
           	    mgrDepts.add(dept);
           	    dept.getTimetableManagers().add(mgr);
           	    hibSession.saveOrUpdate(dept);
           	}
       	}

       	// Check if depts deleted
       	for (Iterator j=mgrDepts.iterator(); j.hasNext(); ) {
       	    Department eDept = (Department) j.next();
       	    if (!eDept.getSessionId().equals(sessionId)) continue; //SKIP DEPARTMENTS OF DIFFERENT SESSIONS!!!
       	    boolean found = false;
           	for (Iterator i=depts.iterator(); i.hasNext(); ) {
           	    Department dept = dDao.get(new Long(i.next().toString()));
           	    if (eDept.equals(dept)) {
           	        found = true;
           	        break;
           	    }
           	}
           	
           	if (!found) {
           	    j.remove();
           	    eDept.getTimetableManagers().remove(mgr);
           	    hibSession.saveOrUpdate(eDept);
           	}
       	}
       	
       	// Update solver groups
       	List solverGrs = frm.getSolverGrs();
       	Set mgrSolverGrs = mgr.getSolverGroups();
       	if (mgrSolverGrs==null) {
       		mgrSolverGrs = new HashSet();
       		mgr.setSolverGroups(mgrSolverGrs);
       	}
       	
        // Check if solver group added or updated
       	for (Iterator i=solverGrs.iterator(); i.hasNext(); ) {
       	    SolverGroup sg = sgDao.get(new Long(i.next().toString()));
       	    boolean found = false;
           	for (Iterator j=mgrSolverGrs.iterator(); j.hasNext(); ) {
           		SolverGroup eSg = (SolverGroup) j.next();
           	    if (eSg.equals(sg)) {
           	        found = true;
           	        break;
           	    }
           	}
           	
           	if (!found){
           		mgrSolverGrs.add(sg);
           	    sg.getTimetableManagers().add(mgr);
           	    hibSession.saveOrUpdate(sg);
           	}
       	}

       	// Check if depts deleted
       	for (Iterator j=mgrSolverGrs.iterator(); j.hasNext(); ) {
       		SolverGroup eSg = (SolverGroup) j.next();
       	    if (!eSg.getSession().getUniqueId().equals(sessionId)) continue; //SKIP DEPARTMENTS OF DIFFERENT SESSIONS!!!
       	    boolean found = false;
           	for (Iterator i=solverGrs.iterator(); i.hasNext(); ) {
           		SolverGroup sg = sgDao.get(new Long(i.next().toString()));
           	    if (eSg.equals(sg)) {
           	        found = true;
           	        break;
           	    }
           	}
           	
           	if (!found) {
           	    j.remove();
           	    eSg.getTimetableManagers().remove(mgr);
           	    hibSession.saveOrUpdate(eSg);
           	}
       	}

       	hibSession.saveOrUpdate(mgr);       	

        ChangeLog.addChange(
                hibSession, 
                sessionContext, 
                mgr, 
                ChangeLog.Source.MANAGER_EDIT, 
                ChangeLog.Operation.UPDATE, 
                null, 
                null);

        tx.commit();
       	
       	if (mgr.getUniqueId()!=null)
       		request.setAttribute(Constants.JUMP_TO_ATTR_NAME, mgr.getUniqueId().toString());
    }

    /**
     * Delete Manager
     * @param request
     * @param frm
     */
    private void deleteManager(
            HttpServletRequest request, TimetableManagerForm frm ) {
    	
    	sessionContext.checkPermission(frm.getUniqueId(), "TimetableManager", Right.TimetableManagerEdit);
        
        TimetableManagerDAO mgrDao = new TimetableManagerDAO();
        Session hibSession = mgrDao.getSession();
        TimetableManager mgr = mgrDao.get(new Long(frm.getUniqueId()));

        Transaction tx = hibSession.beginTransaction();
       	
        ChangeLog.addChange(
                hibSession, 
                sessionContext, 
                mgr, 
                ChangeLog.Source.MANAGER_EDIT, 
                ChangeLog.Operation.DELETE, 
                null, 
                null);

        Set mgrRoles = mgr.getManagerRoles();
       	for (Iterator i=mgrRoles.iterator(); i.hasNext(); ) {
       	    ManagerRole mgrRole = (ManagerRole) i.next();
       	    hibSession.delete(mgrRole);
       	}
       	for (Department d: mgr.getDepartments()) {
       		d.getTimetableManagers().remove(mgr);
       		hibSession.saveOrUpdate(d);
       	}
       	for (SolverGroup sg: mgr.getSolverGroups()) {
       		sg.getTimetableManagers().remove(mgr);
       		hibSession.saveOrUpdate(sg);
       	}

        hibSession.delete(mgr);

       	tx.commit();
    }
}
