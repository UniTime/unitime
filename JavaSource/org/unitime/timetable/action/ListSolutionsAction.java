/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
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
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ListSolutionsForm;
import org.unitime.timetable.form.SolverForm;
import org.unitime.timetable.form.ListSolutionsForm.SolutionBean;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.ui.PropertiesInfo;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.ifs.util.CSVFile;

/** 
 * @author Tomas Muller
 */
public class ListSolutionsAction extends Action {
	private static SimpleDateFormat sDF = new SimpleDateFormat("MM/dd/yy hh:mmaa");

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ListSolutionsForm myForm = (ListSolutionsForm) form;
        // Check Access
        if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        
        User user = Web.getUser(request.getSession());
        
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
        		if (solution!=null) myForm.addSolution(solution, user);
        	}
        }
        
        
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
            	if (solutionBean!=null) {
                	Transaction tx = null;
                	try {
                		SolutionDAO dao = new SolutionDAO();
                		org.hibernate.Session hibSession = dao.getSession();
                		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                			tx = hibSession.beginTransaction();
                		
                		Solution solution = dao.get(solutionBean.getUniqueId(), hibSession);
                		
                		if ("Commit".equals(op)) {
                			boolean committed = solution.commitSolution(myForm.getMessages(),hibSession,user.getId());
                			hibSession.update(solution);
                			solutionBean.setCommited(committed?sDF.format(solution.getCommitDate()):null);
                		} else {
                			solution.uncommitSolution(hibSession, user.getId());
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
                			if (solution.isCommited().booleanValue()) solution.uncommitSolution(hibSession, user.getId());
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
        	SolverProxy solver = WebSolver.getSolver(request.getSession());
        	if (solver!=null && solver.isWorking()) throw new Exception("Solver is working, stop it first.");
            ActionMessages errors = myForm.validate(mapping, request);
        	Long sessionId = null;
        	Long settingsId = null;
        	Long[] ownerId = null;
        	Transaction tx = null;
        	try {
        		SolutionDAO dao = new SolutionDAO();
        		org.hibernate.Session hibSession = dao.getSession();
        		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
        			tx = hibSession.beginTransaction();
        		
            	if ("Load".equals(op)) {
        			ownerId = myForm.getOwnerIds();
        		} else {
        			if (myForm.getSelectOwner())
        				ownerId = new Long[] {myForm.getOwnerId()};
        			else if (myForm.getOwners()!=null && !myForm.getOwners().isEmpty()) {
        				ownerId = new Long[myForm.getOwners().size()];
        				for (int i=0;i<myForm.getOwners().size();i++)
        					ownerId[i] = ((SolverForm.LongIdValue)myForm.getOwners().elementAt(i)).getId();
        			}
        		}
            	sessionId = Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId();
            	
            	List list = null;
            	if ("Load".equals(op)) {
            		list = hibSession.createCriteria(SolverPredefinedSetting.class).add(Restrictions.eq("uniqueId", myForm.getSetting())).list();
            	} else {
            		list = hibSession.createCriteria(SolverPredefinedSetting.class).add(Restrictions.eq("uniqueId", myForm.getEmptySetting())).list();
            	}
            	SolverPredefinedSetting settings = (SolverPredefinedSetting)list.get(0);
            	settingsId = settings.getUniqueId();

            	if (tx!=null) tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    			Debug.error(e);
    	    }
    	    String host = myForm.getHost();
    	    if ("Load Empty Solution".equals(op))
    	    	host = myForm.getHostEmpty();
    	    if ("Load".equals(op))
    	    	WebSolver.createSolver(sessionId,request.getSession(),ownerId,myForm.getSolutionId(),settingsId,null,false,host);
    	    else
    	    	WebSolver.createSolver(sessionId,request.getSession(),ownerId,null,settingsId,null,false,host);
    	    myForm.setChangeTab(true);
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
            			myForm.addSolution(solution, user);
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
        	SolverProxy solver = WebSolver.getSolver(request.getSession());
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	WebSolver.removeSolver(request.getSession());
        }
        
        // Reload
        if ("Reload Input Data".equals(op)) {
        	SolverProxy solver = WebSolver.getSolver(request.getSession());
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	WebSolver.reload(request.getSession(), null, null);
        	myForm.setChangeTab(true);
        }

        // Save, Save As New, Save & Commit, Save As New & Commit
        if ("Save".equals(op) || "Save As New".equals(op) || "Save & Commit".equals(op) || "Save As New & Commit".equals(op)) {
        	SolverProxy solver = WebSolver.getSolver(request.getSession());
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	solver.setNote(myForm.getSolverNote());
        	WebSolver.saveSolution(request.getSession(), op.indexOf("As New")>=0, op.indexOf("Commit")>=0);
        	myForm.setChangeTab(true);
        }
        
        if ("Export Solution".equals(op)) {
        	String solutionIds = "";
        	CSVFile csvFile = new CSVFile();
        	for (Enumeration e=myForm.getSolutionBeans().elements();e.hasMoreElements();) {
        		SolutionBean sb = (SolutionBean)e.nextElement();
        		Solution solution = (new SolutionDAO()).get(sb.getUniqueId());
        		if (solution!=null) {
        			solution.export(csvFile, Web.getUser(request.getSession()));
        			if (solutionIds.length()>0) solutionIds+="-";
        			solutionIds+=solution.getUniqueId().toString();
        		}
        	}
        	File file = ApplicationProperties.getTempFile("solution", "csv");
        	csvFile.save(file);
        	request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        	/*
        	response.sendRedirect("temp/"+file.getName());
       		response.setContentType("text/csv");
       		*/
        }

        getSolutions(request, Web.hasRole(request.getSession(), Roles.getAdminRoles()) || user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE) || user.getCurrentRole().equals(Roles.EXAM_MGR_ROLE), user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE) || user.getCurrentRole().equals(Roles.EXAM_MGR_ROLE), myForm);
        myForm.setSolver(WebSolver.getSolver(request.getSession()));
        return mapping.findForward("showSolutions");
	}
	
	private static Hashtable sNames = new Hashtable();
	
    private void getSolutions(HttpServletRequest request, boolean listAll, boolean committedOnly, ListSolutionsForm myForm) throws Exception {
    	Transaction tx = null;
		try {
			WebTable.setOrder(request.getSession(),"listSolutions.ord",request.getParameter("ord"),1);
			
			WebTable webTable = new WebTable( 16,
					(committedOnly?"Committed Timetables":"Saved Timetables"), "listSolutions.do?ord=%%",
					new String[] {"Created", "Settings", "Valid", "Commited", "Owner", "Assign", "Total", "Time", "Stud", "Room", "Distr", "Instr", "TooBig", "Useless", "Pert", "Note"},
					new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left"},
					null );
			webTable.setRowStyle("white-space:nowrap");
			
			SolutionDAO dao = new SolutionDAO();
			org.hibernate.Session hibSession = dao.getSession();
			
			Collection solutions = null;
			if (listAll)
				solutions = Solution.findBySessionId(Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId());
			else
				solutions = Solution.findBySessionIdAndManagerId(
						Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId(),
						Long.valueOf((String)Web.getUser(request.getSession()).getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME)));
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
							(solution.isValid().booleanValue()?"<IMG border='0' align='absmiddle' src='images/tick.gif'>":""),
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
							(solution.isValid().booleanValue()?new Integer(1):new Integer(0)), 
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
					webTable.addLine(null, new String[] {"<i>No solution saved by "+Web.getUser(request.getSession()).getName()+" so far.</i>"}, null, null );
			}
			request.setAttribute("ListSolutions.table",webTable.printTable(WebTable.getOrder(request.getSession(),"listSolutions.ord")));
			
		} catch (Exception e) {
			e.printStackTrace();
	    }
	}

}

