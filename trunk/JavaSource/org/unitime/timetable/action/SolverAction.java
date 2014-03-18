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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.cpsolver.ifs.util.DataProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.SolverForm;
import org.unitime.timetable.form.SolverForm.LongIdValue;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.ExportUtils;


/** 
 * @author Tomas Muller
 */
@Service("/solver")
public class SolverAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
	@Autowired SolverServerService solverServerService;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		SolverForm myForm = (SolverForm) form;
		
		sessionContext.checkPermission(Right.Solver);
        
		if (sessionContext.getUser().getCurrentAuthority().hasRight(Right.CanSelectSolverServer)) {
			List<String> hosts = new ArrayList<String>();
			for (SolverServer server: solverServerService.getServers(true))
				hosts.add(server.getHost());
			Collections.sort(hosts);
			if (ApplicationProperties.isLocalSolverEnabled())
				hosts.add(0, "local");
			hosts.add(0, "auto");
			request.setAttribute("hosts", hosts);
		}
		
		List<SolverForm.LongIdValue> owners = new ArrayList<SolverForm.LongIdValue>();
		for (SolverGroup owner: SolverGroup.getUserSolverGroups(sessionContext.getUser())) {
			if (sessionContext.hasPermission(owner, Right.TimetablesSolutionLoadEmpty))
				owners.add(new LongIdValue(owner.getUniqueId(),owner.getName()));
		}
		if (owners.size() == 1)
			myForm.setOwner(new Long[] {owners.get(0).getId()});
		else if (!owners.isEmpty())
			request.setAttribute("owners", owners);

        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if ("n".equals(request.getParameter("confirm")))
        	op = null;
        
        if (op==null) {
        	myForm.init("y".equals(request.getParameter("confirm")));
        	return mapping.findForward("showSolver");
        }
        
        SolverProxy solver = courseTimetablingSolverService.getSolver();
        if (solver != null)
        	myForm.setOwner(solver.getProperties().getPropertyLongArry("General.SolverGroupId", null));
        
        if ("Export XML".equals(op)) {
            if (solver==null) throw new Exception("Solver is not started.");
            if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
            sessionContext.checkPermission(myForm.getOwner(), "SolverGroup", Right.SolverSolutionExportXml);
            solver.restoreBest();
            byte[] buf = solver.exportXml();
            OutputStream out = ExportUtils.getXmlOutputStream(response, "solution");
            out.write(buf);
            out.flush(); out.close();
            return null;
        }
        
        if ("Restore From Best".equals(op)) {
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	solver.restoreBest();
        }
        
        if ("Save To Best".equals(op)) {
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	solver.saveBest();
        }
        
        if (op.startsWith("Save") && !op.equals("Save To Best")) {
        	sessionContext.checkPermission(myForm.getOwner(), "SolverGroup", Right.SolverSolutionSave);
        	
        	if (op.indexOf("Commit")>=0)
        		sessionContext.checkPermission(myForm.getOwner(), "SolverGroup", Right.TimetablesSolutionCommit);
        	
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	solver.restoreBest();
        	solver.save(op.indexOf("As New")>=0, op.indexOf("Commit")>=0);
        }
        
        if ("Unload".equals(op)) {
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	courseTimetablingSolverService.removeSolver();
        	myForm.reset(mapping, request);
        	myForm.init(false);
        }
        
        // Reload
        if ("Reload Input Data".equals(op)) {
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                return mapping.findForward("showSolver");
            }
            courseTimetablingSolverService.reload(
            		courseTimetablingSolverService.createConfig(myForm.getSetting(), myForm.getParameterValues()));
        }
        
        if ("Start".equals(op) || "Load".equals(op)) {
        	boolean start = "Start".equals(op); 
        	if (solver!=null && solver.isWorking()) throw new Exception("Solver is working, stop it first.");
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                return mapping.findForward("showSolver");
            }
            Long settingsId = myForm.getSetting();
        	Long[] ownerId = myForm.getOwner();
        	String solutionId = (String)request.getSession().getAttribute("Solver.selectedSolutionId");
    	    DataProperties config = courseTimetablingSolverService.createConfig(settingsId, myForm.getParameterValues());
    	    if (solutionId != null)
    	    	config.setProperty("General.SolutionId", solutionId);
    	    if (myForm.getHost() != null)
    	    	config.setProperty("General.Host", myForm.getHost());
    	    config.setProperty("General.SolverGroupId", ownerId);
    	    config.setProperty("General.StartSolver", new Boolean(start).toString());
    	    if (solver == null) {
        	    solver = courseTimetablingSolverService.createSolver(config);
        	} else if (start) {
        		solver.setProperties(config);
        		solver.start();
        	}
        }
        
        if ("Stop".equals(op)) {
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isRunning()) solver.stopSolver();
        	myForm.reset(mapping, request);
        	myForm.init(false);
        }
        
        if ("Refresh".equals(op)) {
        	myForm.reset(mapping, request);
        	myForm.init(false);
        }
        
        if ("Student Sectioning".equals(op)) {
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	solver.finalSectioning();
        }
        
        if ("Export Solution".equals(op)) {
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	sessionContext.checkPermission(myForm.getOwner(), "SolverGroup", Right.SolverSolutionExportCsv);
        	
        	ExportUtils.exportCSV(solver.export(), response, "solution");
        	return null;
        }

		return mapping.findForward("showSolver");
	}

}

