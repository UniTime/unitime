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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ListSolutionsForm;
import org.unitime.timetable.form.SolverForm;
import org.unitime.timetable.form.ListSolutionsForm.SolutionBean;
import org.unitime.timetable.form.SolverForm.LongIdValue;
import org.unitime.timetable.interfaces.ExternalSolutionCommitAction;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.PropertiesInfo;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.Formats;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Service("/listSolutions")
public class ListSolutionsAction extends Action {
	private static Formats.Format<Date> sDF = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverServerService solverServerService;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ListSolutionsForm myForm = (ListSolutionsForm) form;
		
		sessionContext.checkPermission(Right.Timetables);
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        if ("n".equals(request.getParameter("confirm")))
        	op = null;
        
        /*
        if (op==null) {
        	if (request.getSession().getAttribute("Solver.selectedSolutionId")!=null)
        		op = "Select";
        }
        */
        if (request.getSession().getAttribute("Solver.selectedSolutionId")!=null) {
        	for (StringTokenizer s = new StringTokenizer((String)request.getSession().getAttribute("Solver.selectedSolutionId"),",");s.hasMoreTokens();) {
        		Long solutionId = Long.valueOf(s.nextToken());
        		Solution solution = (new SolutionDAO()).get(solutionId);
        		if (solution!=null) myForm.addSolution(solution);
        	}
        }
        
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
			myForm.setOwnerId(owners.get(0).getId());
		else if (!owners.isEmpty())
			request.setAttribute("owners", owners);
		
        // Update Note
        if ("Update Note".equals(op)) {
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                mapping.findForward("showSolutions");
            } else {
            	SolutionBean solutionBean = myForm.getSolutionBean();
            	if (solutionBean!=null) {
            		Transaction tx = null;
            		try {
            			SolutionDAO dao = new SolutionDAO();
            			org.hibernate.Session hibSession = dao.getSession();
            			if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            				tx = hibSession.beginTransaction();
            		
            			Solution solution = dao.get(solutionBean.getUniqueId(), hibSession);
            			
            			sessionContext.checkPermission(solution, Right.TimetablesSolutionChangeNote);
            		
           				String note = myForm.getNote();
           				if (note!=null && note.length()>1000)
           					note = note.substring(0,1000);
           				solutionBean.setNote(note);
           				solution.setNote(note);

           				dao.saveOrUpdate(solution,hibSession);
                	
           				if (tx!=null) tx.commit();
            		} catch (Exception e) {
            			if (tx!=null) tx.rollback();
            			Debug.error(e);
            		}
            	}
            }        	
        }
        
        // Update Note
        if ("Commit".equals(op) || "Uncommit".equals(op)) {
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                mapping.findForward("showSolutions");
            } else {
            	SolutionBean solutionBean = myForm.getSolutionBean();
            	List<Long> ids = new ArrayList<Long>();
            	if (solutionBean!=null) {
                	Transaction tx = null;
                	try {
                		SolutionDAO dao = new SolutionDAO();
                		org.hibernate.Session hibSession = dao.getSession();
                		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                			tx = hibSession.beginTransaction();
                		
                		Solution solution = dao.get(solutionBean.getUniqueId(), hibSession);
                		
                		sessionContext.checkPermission(solution.getOwner(), Right.TimetablesSolutionCommit);
                		ids.add(solution.getUniqueId());
                		if ("Commit".equals(op)) {
                			List solutions = hibSession.createCriteria(Solution.class).add(Restrictions.eq("owner",solution.getOwner())).list();
                   			HashSet<Solution> touchedSolutionSet = new HashSet<Solution>();
                			for (Iterator i=solutions.iterator();i.hasNext();) {
                				Solution s = (Solution)i.next();
                				if (s.equals(solution)) continue;
                				if (s.isCommited().booleanValue()) {
                 					touchedSolutionSet.add(s); ids.add(s.getUniqueId());
                				}
                			}
                			touchedSolutionSet.add(solution);
                			boolean committed = solution.commitSolution(myForm.getMessages(),hibSession,sessionContext.getUser().getExternalUserId());
                			hibSession.update(solution);
                	    	String className = ApplicationProperty.ExternalActionSolutionCommit.value();
                	    	if (className != null && className.trim().length() > 0){
                	    		ExternalSolutionCommitAction commitAction = (ExternalSolutionCommitAction) (Class.forName(className).newInstance());              	    		
                	    		commitAction.performExternalSolutionCommitAction(touchedSolutionSet, hibSession);
                	    	}
                			solutionBean.setCommited(committed?sDF.format(solution.getCommitDate()):null);
                			
                		} else {
                			solution.uncommitSolution(hibSession, sessionContext.getUser().getExternalUserId());
                			String className = ApplicationProperty.ExternalActionSolutionCommit.value();
                	    	if (className != null && className.trim().length() > 0){
                	    		ExternalSolutionCommitAction commitAction = (ExternalSolutionCommitAction) (Class.forName(className).newInstance());
                	    		HashSet<Solution> solutions = new HashSet<Solution>();
                	    		solutions.add(solution);
                	    		commitAction.performExternalSolutionCommitAction(solutions, hibSession);
                	    	}
                	    	
                			solutionBean.setCommited(null);
                		}
                		
                		String note = solutionBean.getNote();
                		if (note!=null && note.length()>1000)
                			note = note.substring(0,1000);
                		solution.setNote(note);

                    	dao.saveOrUpdate(solution,hibSession);
                    	
                    	if (tx!=null) tx.commit();
            	    } catch (Exception e) {
            	    	if (tx!=null) tx.rollback();
            			Debug.error(e);
            	    }
                	solverServerService.getLocalServer().refreshCourseSolution(ids.toArray(new Long[ids.size()]));
            	}
            }        	
        }

        // Delete
        if ("Delete".equals(op)) {
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                mapping.findForward("showSolutions");
            } else {
            	SolutionBean solutionBean = myForm.getSolutionBean();
            	if (solutionBean!=null) {
                	Transaction tx = null;
                	try {
                		SolutionDAO dao = new SolutionDAO();
                		org.hibernate.Session hibSession = dao.getSession();
                		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                			tx = hibSession.beginTransaction();
                		Solution solution = dao.get(solutionBean.getUniqueId());
                		if (solution!=null) {
                			if (solution.isCommited().booleanValue()) {
                				sessionContext.checkPermission(solution.getOwner(), Right.TimetablesSolutionCommit);
                				solution.uncommitSolution(hibSession, sessionContext.getUser().getExternalUserId());
                    	    	String className = ApplicationProperty.ExternalActionSolutionCommit.value();
                    	    	if (className != null && className.trim().length() > 0){
                    	    		ExternalSolutionCommitAction commitAction = (ExternalSolutionCommitAction) (Class.forName(className).newInstance());
                    	    		HashSet<Solution> solutions = new HashSet<Solution>();
                    	    		solutions.add(solution);
                    	    		commitAction.performExternalSolutionCommitAction(solutions, hibSession);
                    	    	}
                			}
                			sessionContext.checkPermission(solution, Right.TimetablesSolutionDelete);
                			solution.delete(hibSession);
                		}
                    	if (tx!=null) tx.commit();
            	    } catch (Exception e) {
            	    	if (tx!=null) tx.rollback();
            			Debug.error(e);
            	    }
                	//myForm.reset(mapping,request);
                	myForm.removeSolution(solutionBean.getUniqueId());
                	request.getSession().setAttribute("Solver.selectedSolutionId", myForm.getSolutionId());
            	}
            }
        }
        
        // Load
        if ("Load".equals(op) || "Load Empty Solution".equals(op)) {
        	SolverProxy solver = courseTimetablingSolverService.getSolver();
        	if (solver!=null && solver.isWorking()) throw new Exception("Solver is working, stop it first.");

        	Long[] ownerId = null;
        	if ("Load".equals(op)) {
        		sessionContext.checkPermission(myForm.getSolutionId().split(","), "Solution", Right.TimetablesSolutionLoad);
    			ownerId = myForm.getOwnerIds();
    		} else {
    			sessionContext.checkPermission(myForm.getOwnerId(), "SolverGroup", Right.TimetablesSolutionLoadEmpty);
    			ownerId = new Long[] {myForm.getOwnerId()};
    		}

        	String host = myForm.getHost();
        	Long settingsId = myForm.getSetting();
        	
    	    if ("Load Empty Solution".equals(op)) {
    	    	host = myForm.getHostEmpty();
    	    	settingsId = myForm.getEmptySetting();
    	    }
    	    
    	    DataProperties config = courseTimetablingSolverService.createConfig(settingsId, null);
    	    if ("Load".equals(op))
    	    	config.setProperty("General.SolutionId", myForm.getSolutionId());
    	    if (host != null)
    	    	config.setProperty("General.Host", host);
    	    config.setProperty("General.SolverGroupId", ownerId);
    	    courseTimetablingSolverService.createSolver(config);
        }
        
        // Edit
        if("Select".equals(op)) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("uniqueId", new ActionMessage("errors.invalid", "Unique Id : " + id));
                saveErrors(request, errors);
                mapping.findForward("showSolutions");
            } else {
            	Transaction tx = null;
            	try {
            		SolutionDAO dao = new SolutionDAO();
            		org.hibernate.Session hibSession = dao.getSession();
            		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            			tx = hibSession.beginTransaction();
            		Solution solution = (new SolutionDAO()).get(new Long(id));
            		if(solution==null) {
            			errors.add("uniqueId", new ActionMessage("errors.invalid", "Unique Id : " + id));
            			saveErrors(request, errors);
            			mapping.findForward("showSolutions");
            		} else {
            			myForm.addSolution(solution);
            		}
                	if (tx!=null) tx.commit();
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        			Debug.error(e);
        	    }
        	    request.getSession().setAttribute("Solver.selectedSolutionId", myForm.getSolutionId());
            }
        }
        
        if("Deselect".equals(op)) {
        	SolutionBean solutionBean = myForm.getSolutionBean();
        	if (solutionBean!=null) {
        		myForm.removeSolution(solutionBean.getUniqueId());
        	}
        	request.getSession().setAttribute("Solver.selectedSolutionId", myForm.getSolutionId());
        }
        
        // Unload
        if ("Unload".equals(op)) {
        	SolverProxy solver = courseTimetablingSolverService.getSolver();
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	courseTimetablingSolverService.removeSolver();
        }
        
        // Reload
        if ("Reload Input Data".equals(op)) {
        	SolverProxy solver = courseTimetablingSolverService.getSolver();
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	courseTimetablingSolverService.reload(
        			courseTimetablingSolverService.createConfig(solver.getProperties().getPropertyLong("General.SettingsId", null), null));
        	// WebSolver.reload(request.getSession(), null, null);
        }

        // Save, Save As New, Save & Commit, Save As New & Commit
        if ("Save".equals(op) || "Save As New".equals(op) || "Save & Commit".equals(op) || "Save As New & Commit".equals(op)) {
        	SolverProxy solver = courseTimetablingSolverService.getSolver();
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	solver.setNote(myForm.getSolverNote());
        	courseTimetablingSolverService.getSolver().save(op.indexOf("As New")>=0, op.indexOf("Commit")>=0);
        }
        
        if ("Export Solution".equals(op)) {
        	String solutionIds = "";
        	CSVFile csvFile = new CSVFile();
        	for (Enumeration e=myForm.getSolutionBeans().elements();e.hasMoreElements();) {
        		SolutionBean sb = (SolutionBean)e.nextElement();
        		Solution solution = (new SolutionDAO()).get(sb.getUniqueId());
        		if (solution!=null) {
        			sessionContext.checkPermission(solution, Right.TimetablesSolutionExportCsv);
        			
        			solution.export(csvFile, UserProperty.NameFormat.get(sessionContext.getUser()));
        			if (solutionIds.length()>0) solutionIds+="-";
        			solutionIds+=solution.getUniqueId().toString();
        		}
        	}
        	ExportUtils.exportCSV(csvFile, response, "solution");
        	return null;
        }

        getSolutions(request, myForm);
        myForm.setSolver(courseTimetablingSolverService.getSolver());
        return mapping.findForward("showSolutions");
	}
	
    private void getSolutions(HttpServletRequest request, ListSolutionsForm myForm) throws Exception {
    	try {
			WebTable.setOrder(sessionContext,"listSolutions.ord",request.getParameter("ord"),1);
			
			boolean committedOnly = !sessionContext.hasPermission(Right.Solver);
			boolean listAll = sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
			
			WebTable webTable = new WebTable( 15,
					(committedOnly?"Committed Timetables":"Saved Timetables"), "listSolutions.do?ord=%%",
					new String[] {"Created", "Settings", "Commited", "Owner", "Assign", "Total", "Time", "Stud", "Room", "Distr", "Instr", "TooBig", "Useless", "Pert", "Note"},
					new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left"},
					null );
			webTable.setRowStyle("white-space:nowrap");
			
			SolutionDAO dao = new SolutionDAO();
			org.hibernate.Session hibSession = dao.getSession();
			
			Collection solutions = null;
			if (listAll)
				solutions = Solution.findBySessionId(sessionContext.getUser().getCurrentAcademicSessionId());
			else {
				List<Serializable> solverGroupIds = new ArrayList<Serializable>();
				for (Qualifiable owner: sessionContext.getUser().getCurrentAuthority().getQualifiers("SolverGroup"))
					solverGroupIds.add(owner.getQualifierId());
				solutions = SolutionDAO.getInstance().getSession().createQuery(
						"from Solution where owner.uniqueId in :solverGroupIds"
						).setParameterList("solverGroupIds", solverGroupIds).list();
			}
			int nrLines = 0;
			
			if (solutions==null || solutions.isEmpty()) {
				webTable.addLine(null, new String[] {(committedOnly?"<i>No solution committed so far.</i>":"<i>No solution saved so far.</i>")}, null, null );
			} else {
				for (Iterator i=solutions.iterator();i.hasNext();) {
					Solution solution = (Solution)i.next();
					if (committedOnly && !solution.isCommited().booleanValue()) continue;
					String ownerName = solution.getOwner().getAbbv();
					String onClick = "onClick=\"document.location='listSolutions.do?op=Select&id=" + solution.getUniqueId() + "';\"";
					String note = solution.getNote();
					if (note!=null) note = note.replaceAll("\n","<br>");
					PropertiesInfo globalInfo = (PropertiesInfo)solution.getInfo("GlobalInfo");
					String assigned = (globalInfo==null?"?":globalInfo.getProperty("Assigned variables","N/A"));
					String totVal = (globalInfo==null?"?":globalInfo.getProperty("Overall solution value","N/A"));
					String timePr = (globalInfo==null?"?":globalInfo.getProperty("Time preferences","N/A"));
					String studConf = (globalInfo==null?"?":globalInfo.getProperty("Student conflicts","N/A"));
					String roomPr = (globalInfo==null?"?":globalInfo.getProperty("Room preferences","N/A"));
					String distPr = (globalInfo==null?"?":globalInfo.getProperty("Distribution preferences","N/A"));
					String instrPr = (globalInfo==null?"?":globalInfo.getProperty("Back-to-back instructor preferences","N/A"));
					String tooBig = (globalInfo==null?"?":globalInfo.getProperty("Too big rooms","N/A"));
					String useless = (globalInfo==null?"?":globalInfo.getProperty("Useless half-hours","N/A"));
					String pertPen = (globalInfo==null?"?":globalInfo.getProperty("Perturbations: Total penalty","N/A"));
					assigned = assigned.replaceAll(" of ","/");
					if (!"N/A".equals(timePr) && timePr.indexOf('/')>=0) timePr=timePr.substring(0,timePr.indexOf('/')).trim();
					if (!"N/A".equals(roomPr) && roomPr.indexOf('/')>=0) roomPr=roomPr.substring(0,roomPr.indexOf('/')).trim();
					if (!"N/A".equals(instrPr) && instrPr.indexOf('/')>=0) instrPr=instrPr.substring(0,instrPr.indexOf('/')).trim();
					if (!"N/A".equals(assigned) && assigned.indexOf(' ')>=0) assigned=assigned.substring(0,assigned.indexOf(' ')).trim();
					if (!"N/A".equals(timePr) && timePr.indexOf(' ')>=0) timePr=timePr.substring(0,timePr.indexOf(' ')).trim();
					if (!"N/A".equals(roomPr) && roomPr.indexOf(' ')>=0) roomPr=roomPr.substring(0,roomPr.indexOf(' ')).trim();
					if (!"N/A".equals(instrPr) && instrPr.indexOf(' ')>=0) instrPr=instrPr.substring(0,instrPr.indexOf(' ')).trim();
					if (!"N/A".equals(distPr) && distPr.indexOf(' ')>=0) distPr=distPr.substring(0,distPr.indexOf(' ')).trim();
					if (!"N/A".equals(tooBig) && tooBig.indexOf(' ')>=0) tooBig=tooBig.substring(0,tooBig.indexOf(' ')).trim();
					if (!"N/A".equals(useless) && useless.indexOf(' ')>=0) useless=useless.substring(0,useless.indexOf(' ')).trim();
					studConf = studConf.replaceAll(" \\[","(").replaceAll("\\]",")").replaceAll(", ",",").replaceAll("hard:","h").replaceAll("distance:","d").replaceAll("commited:","c").replaceAll("committed:","c");
					String settings = null;
					String type = null;
					for (Iterator j=solution.getParameters().iterator();j.hasNext();) {
						SolverParameter p = (SolverParameter)j.next();
						if ("General.SettingsId".equals(p.getDefinition().getName())) {
							SolverPredefinedSetting set = (new SolverPredefinedSettingDAO()).get(Long.valueOf(p.getValue()),hibSession);
							if (set!=null) settings = set.getDescription();
						}
						if ("Basic.Mode".equals(p.getDefinition().getName())) {
							type = p.getValue();
						}
					}
					type = (settings==null?type==null?"?":type:settings);
                    
                    String bgColor = null;
                    if (myForm.getSolutionBean(solution.getUniqueId())!=null)
                        bgColor = "rgb(168,187,225)";
					
					webTable.addLine(onClick, new String[] {
							sDF.format(new Date(solution.getCreated().getTime())),
							type,
							(solution.isCommited().booleanValue()?sDF.format(new Date(solution.getCommitDate().getTime())):""),
							ownerName, 
							assigned,
							totVal,
							timePr,
							studConf,
							roomPr,
							distPr,
							instrPr,
							tooBig,
							useless,
							pertPen,
							note},
						new Comparable[] {
							solution.getCreated(),
							type,
							(solution.isCommited().booleanValue()?new Long(solution.getCommitDate().getTime()):new Long(0)),
							ownerName,
							assigned,
							totVal,
							timePr,
							studConf,
							roomPr,
							distPr,
							instrPr,
							tooBig,
							useless,
							pertPen,
							(solution.getNote()==null?"":solution.getNote())}).setBgColor(bgColor);
					nrLines++;
		        }
				if (nrLines==0)
					webTable.addLine(null, new String[] {"<i>No solution saved by "+sessionContext.getUser().getName()+" so far.</i>"}, null, null );
			}
			request.setAttribute("ListSolutions.table",webTable.printTable(WebTable.getOrder(sessionContext,"listSolutions.ord")));
			
		} catch (Exception e) {
			e.printStackTrace();
	    }
	}

}

