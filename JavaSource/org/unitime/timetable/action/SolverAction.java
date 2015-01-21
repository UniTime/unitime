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
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.SolverForm;
import org.unitime.timetable.form.SolverForm.LongIdValue;
import org.unitime.timetable.gwt.resources.GwtConstants;
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
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
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
			if (ApplicationProperty.SolverLocalEnabled.isTrue())
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
        	
        	ExportUtils.exportCSV(solver.export(CONSTANTS.useAmPm()), response, "solution");
        	return null;
        }

		return mapping.findForward("showSolver");
	}

}

