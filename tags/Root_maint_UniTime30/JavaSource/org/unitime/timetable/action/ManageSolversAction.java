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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ManageSolversForm;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.remote.RemoteSolverServerProxy;
import org.unitime.timetable.solver.remote.SolverRegisterService;
import org.unitime.timetable.solver.ui.PropertiesInfo;
import org.unitime.timetable.util.Constants;


/** 
 * @author Tomas Muller
 */
public class ManageSolversAction extends Action {
	private static SimpleDateFormat sDF = new SimpleDateFormat("MM/dd/yy hh:mmaa");

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ManageSolversForm myForm = (ManageSolversForm) form;

        // Check Access
        if (!Web.isLoggedIn( request.getSession() )
                || !Web.hasRole(request.getSession(), Roles.getAdminRoles()) ) {
             throw new Exception ("Access Denied.");
         }

        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        if ("Select".equals(op) && request.getParameter("puid")!=null) {
        	String puid = request.getParameter("puid");
        	request.getSession().setAttribute("ManageSolver.puid", puid);
        	request.getSession().removeAttribute("SolverProxy");
        	return mapping.findForward("showSolver");
        }
        
        if ("Deselect".equals(op)) {
        	request.getSession().removeAttribute("ManageSolver.puid");
        	request.getSession().removeAttribute("SolverProxy");
        }
        
        if ("Shutdown".equals(op)) {
        	String solverName = request.getParameter("solver");
        	if (solverName!=null) {
                Set servers = SolverRegisterService.getInstance().getServers();
                synchronized (servers) {
                    for (Iterator i=servers.iterator();i.hasNext();) {
                        RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                        if (solverName.equals(server.toString())) {
                            server.shutdown();
                            break;
                        }
                    }
    			}
        	}
        }
        
        if ("Kill".equals(op)) {
            String solverName = request.getParameter("solver");
            if (solverName!=null) {
                Set servers = SolverRegisterService.getInstance().getServers();
                synchronized (servers) {
                    for (Iterator i=servers.iterator();i.hasNext();) {
                        RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                        if (solverName.equals(server.toString())) {
                            server.kill();
                            break;
                        }
                    }
                }
            }
        }

        if ("Start Using".equals(op)) {
            String solverName = request.getParameter("solver");
            if (solverName!=null) {
                Set servers = SolverRegisterService.getInstance().getServers();
                synchronized (servers) {
                    for (Iterator i=servers.iterator();i.hasNext();) {
                        RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                        if (solverName.equals(server.toString())) {
                            server.startUsing();
                            break;
                        }
                    }
                }
            }
        }

        if ("Stop Using".equals(op)) {
            String solverName = request.getParameter("solver");
            if (solverName!=null) {
                Set servers = SolverRegisterService.getInstance().getServers();
                synchronized (servers) {
                    for (Iterator i=servers.iterator();i.hasNext();) {
                        RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                        if (solverName.equals(server.toString())) {
                            server.stopUsing();
                            break;
                        }
                    }
                }
            }
        }

        if ("Disconnect".equals(op)) {
            String solverName = request.getParameter("solver");
            if (solverName!=null) {
                Set servers = SolverRegisterService.getInstance().getServers();
                synchronized (servers) {
                    for (Iterator i=servers.iterator();i.hasNext();) {
                        RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                        if (solverName.equals(server.toString())) {
                            server.disconnectProxy();
                            i.remove();
                            break;
                        }
                    }
                }
            }
        }

        getSolvers(request);
        getServers(request);
        return mapping.findForward("showSolvers");
	}
	
	public static String getName(String puid) {
	    return getName(TimetableManager.findByExternalId(puid));
	}

	public static String getName(TimetableManager mgr) {
		if (mgr==null) return null;
	    return mgr.getShortName();
	}

	public static String getName(SolverGroup sg) {
		if (sg==null) return null;
	    return sg.getAbbv();
	}

	private void getSolvers(HttpServletRequest request) throws Exception {
		try {
			WebTable.setOrder(request.getSession(),"manageSolvers.ord",request.getParameter("ord"),1);
			
			WebTable webTable = new WebTable( 18,
					"Manage Solvers", "manageSolvers.do?ord=%%",
					new String[] {"Created", "Last Used", "Session", "Host", "Config", "Status", "Owner", "Assign", "Total", "Time", "Stud", "Room", "Distr", "Instr", "TooBig", "Useless", "Pert", "Note"},
					new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left"},
					null );
			webTable.setRowStyle("white-space:nowrap");
			
			int nrLines = 0;
			Long currentSessionId = Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId();
			
			HashSet solvers = new HashSet(WebSolver.getSolvers().values());
			for (Iterator i=solvers.iterator();i.hasNext();) {
				SolverProxy solver = (SolverProxy)i.next();
				String runnerName = getName(solver.getProperties().getProperty("General.OwnerPuid","N/A"));
			   Long[] solverGroupId = solver.getProperties().getPropertyLongArry("General.SolverGroupId",null);
			   String ownerName = "";
			   if (solverGroupId!=null) {
				   for (int j=0;j<solverGroupId.length;j++) {
					   if (j>0) ownerName += " & ";
					   ownerName += getName((new SolverGroupDAO()).get(solverGroupId[j]));
				   }
			   }
			   if (ownerName==null || ownerName.length()==0)
					ownerName = "N/A";
				if (runnerName==null)
					runnerName = "N/A";
				if (ownerName.equals("N/A"))
					ownerName = runnerName;
				if (runnerName.equals("N/A"))
					runnerName = ownerName;
				if (!ownerName.equals(runnerName))
					ownerName = runnerName+" as "+ownerName;
				Session session = (new SessionDAO()).get(solver.getProperties().getPropertyLong("General.SessionId",new Long(-1)));
				String sessionLabel = "N/A";
				if (session!=null)
					sessionLabel = session.getLabel();
				SolverPredefinedSetting setting = (new SolverPredefinedSettingDAO()).get(solver.getProperties().getPropertyLong("General.SettingsId",new Long(-1)));
				String settingLabel = solver.getProperties().getProperty("Basic.Mode","N/A");
				if (setting!=null)
					settingLabel = setting.getDescription();
				String onClick = null;
				if (session.getUniqueId().equals(currentSessionId) && solver.getProperties().getProperty("General.OwnerPuid")!=null)
					onClick = "onClick=\"document.location='manageSolvers.do?op=Select&puid=" + solver.getProperties().getProperty("General.OwnerPuid") + "';\"";
				String status = (String)solver.getProgress().get("STATUS");
				
				String note = solver.getNote();
					if (note!=null) note = note.replaceAll("\n","<br>");
				PropertiesInfo globalInfo = solver.getGlobalInfo();
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
				Date loaded = solver.getLoadedDate();
				Date lastUsed = solver.getLastUsed(); 
				
				webTable.addLine(onClick, new String[] {
							(loaded==null?"N/A":sDF.format(loaded)),
							(lastUsed==null?"N/A":sDF.format(lastUsed)),
							sessionLabel,
							solver.getHostLabel(),
							settingLabel,
							status,
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
							(loaded==null?new Date():loaded),
							(lastUsed==null?new Date():lastUsed),
							sessionLabel,
							solver.getHost(),
							settingLabel, 
							status,
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
							(solver.getNote()==null?"":solver.getNote())});
					nrLines++;
			}
			if (nrLines==0)
				webTable.addLine(null, new String[] {"<i>No solver is running.</i>"}, null, null );
			request.setAttribute("ManageSolvers.table",webTable.printTable(WebTable.getOrder(request.getSession(),"manageSolvers.ord")));
			
	    } catch (Exception e) {
	        throw new Exception(e);
	    }
	}

	private void getServers(HttpServletRequest request) throws Exception {
		try {
			WebTable.setOrder(request.getSession(),"manageSolvers.ord2",request.getParameter("ord2"),1);
			
			WebTable webTable = new WebTable( 11,
					"Available Servers", "manageSolvers.do?ord2=%%",
					new String[] {"Host", "Version", "Started", "Available Memory", "Ping", "Usage", "NrInstances", "Active", "Working", "Passivated", "Operation(s)"},
					new String[] {"left", "left", "left", "left", "left", "left", "left", "left","left","left","left"},
					null );
			webTable.setRowStyle("white-space:nowrap");
			
			DecimalFormat df = new DecimalFormat("0.00");
			
			int nrLines = 0;

            Set servers = SolverRegisterService.getInstance().getServers();
            synchronized (servers) {
                for (Iterator i=servers.iterator();i.hasNext();) {
                    RemoteSolverServerProxy server = (RemoteSolverServerProxy)i.next();
                    if (!server.isActive()) {
                        String op="<input type=\"button\" value=\"Disconnect\" onClick=\"if (confirm('Do you really want to disconnect server "+server+"?')) document.location='manageSolvers.do?op=Disconnect&solver="+server.toString()+"';\">";
                        webTable.addLine(null, new String[] {
                                server.getAddress().getHostName()+":"+server.getPort(),
                                "<i>inactive</i>",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                op
                                },
                            new Comparable[] {
                                server.getAddress().getHostName()+":"+server.getPort(),
                                "",
                                null,
                                new Long(-1),
                                new Long(-1),
                                new Long(-1),
                                new Integer(-1),
                                new Integer(-1),
                                new Integer(-1),
                                new Integer(-1),
                                null
                        });
                        continue;
                    }
                    int nrActive = 0;
                    int nrPassivated = 0;
                    int nrWorking = 0;
                    long mem = server.getAvailableMemory();
                    long t0 = System.currentTimeMillis();
                    long usage = server.getUsage();
                    long t1 = System.currentTimeMillis();
                    for (Enumeration e=server.getSolvers().elements();e.hasMoreElements();) {
                        SolverProxy solver = (SolverProxy)e.nextElement();
                        if (solver.isPassivated()) {
                            nrPassivated++;
                        } else {
                            nrActive++;
                            if (solver.isWorking())
                                nrWorking++;
                        }
                    }
                    String version = server.getVersion();
                    Date startTime = server.getStartTime();
                    String op="<input type=\"button\" value=\"Shutdown\" onClick=\"if (confirm('Do you really want to shutdown server "+server+"?')) document.location='manageSolvers.do?op=Shutdown&solver="+server.toString()+"';\">";
                    //op+="&nbsp;&nbsp;<input type=\"button\" value=\"Disconnect\" onClick=\"if (confirm('Do you really want to disconnect server "+server+"?')) document.location='manageSolvers.do?op=Disconnect&solver="+server.toString()+"';\">";
                    //op+="&nbsp;&nbsp;<input type=\"button\" value=\"Kill\" onClick=\"if (confirm('Do you really want to kill server "+server+"?') && confirm('DO YOU REALLY REALLY WANT TO KILL SERVER "+server+"? THIS FUNCTION IS ONLY FOR TESTING PURPOSES AND SHOULD NEVER BE USED IN PRODUCTION!!!')) document.location='manageSolvers.do?op=Kill&solver="+server.toString()+"';\">";
                    if (usage>=1000) {
                        op+="&nbsp;&nbsp;<input type=\"button\" value=\"Enable\" onClick=\"if (confirm('Do you really want to enable server "+server+" for the new solver instances?')) document.location='manageSolvers.do?op=Start%20Using&solver="+server.toString()+"';\">";
                    } else {
                        op+="&nbsp;&nbsp;<input type=\"button\" value=\"Disable\" onClick=\"if (confirm('Do you really want to disable server "+server+" for the new solver instances?')) document.location='manageSolvers.do?op=Stop%20Using&solver="+server.toString()+"';\">";
                    }
                    webTable.addLine(null, new String[] {
                            server.getAddress().getHostName()+":"+server.getPort(),
                            (version==null||"-1".equals(version)?"<i>N/A</i>":version),
                            (startTime==null?"<i>N/A</i>":sDF.format(startTime)),
                            df.format( ((double)mem)/1024/1024)+" MB",
                            (t1-t0)+" ms",
                            String.valueOf(usage),
                            String.valueOf(nrActive+nrPassivated),
                            String.valueOf(nrActive),
                            String.valueOf(nrWorking),
                            String.valueOf(nrPassivated),
                            op
                            },
                        new Comparable[] {
                            server.getAddress().getHostName()+":"+server.getPort(),
                            version,
                            startTime,
                            new Long(t1-t0),
                            new Long(mem),
                            new Long(usage),
                            new Integer(nrActive+nrPassivated),
                            new Integer(nrActive),
                            new Integer(nrWorking),
                            new Integer(nrPassivated),
                            null
                    });
                    nrLines++;
                }
            }
			
			if (ApplicationProperties.isLocalSolverEnabled()) {
				int nrActive = 0;
				int nrPassivated = 0;
				int nrWorking = 0;
				long mem = WebSolver.getAvailableMemory();
				long usage = WebSolver.getUsage();
				for (Enumeration e=WebSolver.getLocalSolvers().elements();e.hasMoreElements();) {
					SolverProxy solver = (SolverProxy)e.nextElement();
					if (solver.isPassivated()) {
						nrPassivated++;
					} else {
						nrActive++;
						if (solver.isWorking())
							nrWorking++;
					}
				}
				String version = Constants.VERSION+"."+Constants.BLD_NUMBER;
				webTable.addLine(null, new String[] {
						"local",
						(version==null||"-1".equals(version)?"<i>N/A</i>":version),
						"<i>N/A</i>",
						df.format(((double)mem)/1024/1024)+" MB",
						"N/A",
						String.valueOf(usage),
						String.valueOf(nrActive+nrPassivated),
						String.valueOf(nrActive),
						String.valueOf(nrWorking),
						String.valueOf(nrPassivated),
						""
						},
					new Comparable[] {
						"",
						version,
						new Date(),
						new Long(mem),
						new Long(0),
						new Long(usage),
						new Integer(nrActive+nrPassivated),
						new Integer(nrActive),
						new Integer(nrWorking),
						new Integer(nrPassivated),
						null
				});
				nrLines++;
			}
			if (nrLines==0)
				webTable.addLine(null, new String[] {"<i>No solver server is running.</i>"}, null, null );

			request.setAttribute("ManageSolvers.table2",webTable.printTable(WebTable.getOrder(request.getSession(),"manageSolvers.ord2")));
			
	    } catch (Exception e) {
	        throw new Exception(e);
	    }
	}
}

