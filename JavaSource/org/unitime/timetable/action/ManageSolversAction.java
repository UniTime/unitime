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

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ManageSolversForm;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.basic.GetInfo;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.solver.ui.PropertiesInfo;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;


/** 
 * @author Tomas Muller
 */
@Service("/manageSolvers")
public class ManageSolversAction extends Action {
	private static Formats.Format<Date> sDF = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	@Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;
	@Autowired SolverServerService solverServerService;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ManageSolversForm myForm = (ManageSolversForm) form;

        // Check Access.
		sessionContext.checkPermission(Right.ManageSolvers);

        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        if ("Select".equals(op) && request.getParameter("puid")!=null) {
        	String puid = request.getParameter("puid");
        	sessionContext.setAttribute(SessionAttribute.CourseTimetablingUser, puid);
        	sessionContext.removeAttribute(SessionAttribute.CourseTimetablingSolver);
        	return mapping.findForward("showSolver");
        }
        
        if ("Unload".equals(op) && request.getParameter("puid")!=null) {
        	String puid = request.getParameter("puid");
        	sessionContext.setAttribute(SessionAttribute.CourseTimetablingUser, puid);
        	sessionContext.removeAttribute(SessionAttribute.CourseTimetablingSolver);
        	courseTimetablingSolverService.removeSolver();
        }

        if ("Select".equals(op) && request.getParameter("examPuid")!=null) {
            String puid = request.getParameter("examPuid");
            sessionContext.setAttribute(SessionAttribute.ExaminationUser, puid);
            sessionContext.removeAttribute(SessionAttribute.ExaminationSolver);
            LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());
            return mapping.findForward("showExamSolver");
        }

        if ("Unload".equals(op) && request.getParameter("examPuid")!=null) {
            String puid = request.getParameter("examPuid");
            sessionContext.setAttribute(SessionAttribute.ExaminationUser, puid);
            sessionContext.removeAttribute(SessionAttribute.ExaminationSolver);
            examinationSolverService.removeSolver();
        }

        if ("Select".equals(op) && request.getParameter("sectionPuid")!=null) {
            String puid = request.getParameter("sectionPuid");
            sessionContext.setAttribute(SessionAttribute.StudentSectioningUser, puid);
            sessionContext.removeAttribute(SessionAttribute.StudentSectioningSolver);
            return mapping.findForward("showStudentSolver");
        }

        if ("Unload".equals(op) && request.getParameter("sectionPuid")!=null) {
            String puid = request.getParameter("sectionPuid");
            sessionContext.setAttribute(SessionAttribute.StudentSectioningUser, puid);
            sessionContext.removeAttribute(SessionAttribute.StudentSectioningSolver);
            studentSectioningSolverService.removeSolver();
        }
        
        if ("Unload".equals(op) && request.getParameter("onlineId")!=null) {
        	String id = request.getParameter("onlineId");
        	if (request.getParameter("host") != null) {
        		SolverServer server = solverServerService.getServer(request.getParameter("host"));
        		if (server != null) {
        			server.getOnlineStudentSchedulingContainer().unloadSolver(id);
        		} else {
        			solverServerService.getOnlineStudentSchedulingContainer().unloadSolver(id);
        		}
        	} else {
        		solverServerService.getOnlineStudentSchedulingContainer().unloadSolver(id);
        	}
        }
        
        if ("Unmaster".equals(op) && request.getParameter("onlineId")!=null) {
        	String id = request.getParameter("onlineId");
        	if (request.getParameter("host") != null) {
        		SolverServer server = solverServerService.getServer(request.getParameter("host"));
        		if (server != null) {
        			OnlineSectioningServer solver = server.getOnlineStudentSchedulingContainer().getSolver(id);
        			if (solver != null) solver.releaseMasterLockIfHeld();
        		}
        	} else {
        		OnlineSectioningServer solver = solverServerService.getOnlineStudentSchedulingContainer().getSolver(id);
        		if (solver != null) solver.releaseMasterLockIfHeld();
        	}
        }
        
        if ("Deselect".equals(op)) {
        	sessionContext.removeAttribute(SessionAttribute.CourseTimetablingUser);
        	sessionContext.removeAttribute(SessionAttribute.CourseTimetablingSolver);
            sessionContext.removeAttribute(SessionAttribute.ExaminationUser);
            sessionContext.removeAttribute(SessionAttribute.ExaminationSolver);
            sessionContext.removeAttribute(SessionAttribute.StudentSectioningUser);
            sessionContext.removeAttribute(SessionAttribute.StudentSectioningSolver);
        }
        
        if ("Shutdown".equals(op)) {
        	SolverServer server = solverServerService.getServer(request.getParameter("solver"));
        	if (server != null)
        		server.shutdown();
        }

        if ("Start Using".equals(op)) {
        	SolverServer server = solverServerService.getServer(request.getParameter("solver"));
        	if (server != null)
        		server.setUsageBase(0);
        }

        if ("Stop Using".equals(op)) {
        	SolverServer server = solverServerService.getServer(request.getParameter("solver"));
        	if (server != null)
        		server.setUsageBase(1000);
        }

        getSolvers(request);
        if (sessionContext.hasPermission(Right.SessionIndependent))
        	getServers(request);
        getExamSolvers(request);
        getStudentSolvers(request);
        getOnlineSolvers(request);
        return mapping.findForward("showSolvers");
	}
	
	public static String getName(String puid) {
		TimetableManager mgr = TimetableManager.findByExternalId(puid);
	    return (mgr == null ? puid : mgr.getShortName());
	}

	public static String getName(SolverGroup sg) {
		if (sg==null) return null;
	    return sg.getAbbv();
	}

	private void getSolvers(HttpServletRequest request) throws Exception {
		try {
			WebTable.setOrder(sessionContext,"manageSolvers.ord",request.getParameter("ord"),1);
			
			WebTable webTable = new WebTable( 19,
					"Manage Course Solvers", "manageSolvers.do?ord=%%",
					new String[] {"Created", "Last Used", "Session", "Host", "Config", "Status", "Owner", "Assign", "Total", "Time", "Stud", "Room", "Distr", "Instr", "TooBig", "Useless", "Pert", "Note", "Operation(s)"},
					new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left"},
					null );
			webTable.setRowStyle("white-space:nowrap");
			
			int nrLines = 0;
			Long currentSessionId = sessionContext.getUser().getCurrentAcademicSessionId();
			
            SolverProxy x = courseTimetablingSolverService.getSolverNoSessionCheck();
            String xId = (x==null?null:x.getProperties().getProperty("General.OwnerPuid"));
            
			HashSet solvers = new HashSet(courseTimetablingSolverService.getSolvers().values());
			for (Iterator i=solvers.iterator();i.hasNext();) {
				SolverProxy solver = (SolverProxy)i.next();
				if (solver==null) continue;
				DataProperties properties = solver.getProperties();
				if (properties==null) continue;
				String runnerName = getName(properties.getProperty("General.OwnerPuid","N/A"));
			   Long[] solverGroupId = properties.getPropertyLongArry("General.SolverGroupId",null);
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
				Session session = (new SessionDAO()).get(properties.getPropertyLong("General.SessionId",new Long(-1)));
				if (session == null || sessionContext.getUser().getAuthorities(sessionContext.getUser().getCurrentAuthority().getRole(), session).isEmpty()) continue;
				String sessionLabel = "N/A";
				if (session!=null)
					sessionLabel = session.getLabel();
				SolverPredefinedSetting setting = (new SolverPredefinedSettingDAO()).get(properties.getPropertyLong("General.SettingsId",new Long(-1)));
				String settingLabel = properties.getProperty("Basic.Mode","N/A");
				if (setting!=null)
					settingLabel = setting.getDescription();
				String onClick = null;
				if (session!=null && session.getUniqueId().equals(currentSessionId) && properties.getProperty("General.OwnerPuid")!=null)
					onClick = "onClick=\"document.location='manageSolvers.do?op=Select&puid=" + properties.getProperty("General.OwnerPuid") + "';\"";
				String status = "N/A";
				try {
					status = (String)solver.getProgress().get("STATUS");
				} catch (Exception e) {}
				
				String note = null;
				try {
					note = solver.getNote();
				} catch (Exception e) {}
				if (note!=null) note = note.replaceAll("\n","<br>");
				PropertiesInfo globalInfo = null;
				try {
					globalInfo = solver.getGlobalInfo();
				} catch (Exception e) {}
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
				
                String bgColor = null;
            	if (x!=null && ToolBox.equals(properties.getProperty("General.OwnerPuid"), xId))
            		bgColor = "rgb(168,187,225)";
                
                String op = "";
                op += 
                	"<input type=\"button\" value=\"Unload\" onClick=\"" +
                	"if (confirm('Do you really want to unload this solver?')) " +
                	"document.location='manageSolvers.do?op=Unload&puid=" + properties.getProperty("General.OwnerPuid")+ "';" +
                	" event.cancelBubble=true;\">";

				webTable.addLine(onClick, new String[] {
							(loaded==null?"N/A":sDF.format(loaded)),
							(lastUsed==null?"N/A":sDF.format(lastUsed)),
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
							note,
							op},
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
							(solver.getNote()==null?"":solver.getNote()),
							null}).setBgColor(bgColor);
					nrLines++;
			}
			if (nrLines==0)
				webTable.addLine(null, new String[] {"<i>No solver is running.</i>"}, null, null );
			request.setAttribute("ManageSolvers.table",webTable.printTable(WebTable.getOrder(sessionContext,"manageSolvers.ord")));
			
	    } catch (Exception e) {
	        throw new Exception(e);
	    }
	}

	private void getServers(HttpServletRequest request) throws Exception {
		try {
			WebTable.setOrder(sessionContext,"manageSolvers.ord2",request.getParameter("ord2"),1);
			
			WebTable webTable = new WebTable( 11,
					"Available Servers", "manageSolvers.do?ord2=%%",
					new String[] {"Host", "Version", "Started", "Available Memory", "Ping", "Usage", "NrInstances", "Active", "Working", "Passivated", "Operation(s)"},
					new String[] {"left", "left", "left", "left", "left", "left", "left", "left","left","left","left"},
					null );
			webTable.setRowStyle("white-space:nowrap");
			
			DecimalFormat df = new DecimalFormat("0.00");
			
			int nrLines = 0;
			
			for (SolverServer server: solverServerService.getServers(false)) {
                    if (!server.isActive()) {
                        // String op="<input type=\"button\" value=\"Disconnect\" onClick=\"if (confirm('Do you really want to disconnect server "+server.getHost()+"?')) document.location='manageSolvers.do?op=Disconnect&solver="+server.getHost()+"';\">";
                        webTable.addLine(null, new String[] {
                                server.getHost(),
                                "<i>inactive</i>",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                ""
                                },
                            new Comparable[] {
                        		server.getHost(),
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
                    for (String user: server.getCourseSolverContainer().getSolvers()) {
                        SolverProxy solver = server.getCourseSolverContainer().getSolver(user);
                        if (solver == null) continue;
                        if (solver.isPassivated()) {
                            nrPassivated++;
                        } else {
                            nrActive++;
                            if (solver.isWorking())
                                nrWorking++;
                        }
                    }
                    for (String user: server.getExamSolverContainer().getSolvers()) {
                        ExamSolverProxy solver = server.getExamSolverContainer().getSolver(user);
                        if (solver == null) continue;
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
                    boolean local = server.isLocal();
                    String op = "";
                    if (usage >= 1000) {
                        op+="<input type=\"button\" value=\"Enable\" onClick=\"if (confirm('Do you really want to enable server "+server.getHost()+" for the new solver instances?')) document.location='manageSolvers.do?op=Start%20Using&solver="+server.getHost()+"';\">&nbsp;&nbsp;";
                    } else {
                        op+="<input type=\"button\" value=\"Disable\" onClick=\"if (confirm('Do you really want to disable server "+server.getHost()+" for the new solver instances?')) document.location='manageSolvers.do?op=Stop%20Using&solver="+server.getHost()+"';\">&nbsp;&nbsp;";
                    }
                    if (!local) {
                    	op+="<input type=\"button\" value=\"Shutdown\" onClick=\"if (confirm('Do you really want to shutdown server "+server.getHost()+"?')) document.location='manageSolvers.do?op=Shutdown&solver="+server.getHost()+"';\">&nbsp;&nbsp;";
                    } else {
                    	usage += 500;
                    }
                    webTable.addLine(null, new String[] {
                            server.getHost() + (local ? " (local)" : ""),
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
                            server.getHost(),
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
			

			if (nrLines==0)
				webTable.addLine(null, new String[] {"<i>No solver server is running.</i>"}, null, null );

			request.setAttribute("ManageSolvers.table2",webTable.printTable(WebTable.getOrder(sessionContext,"manageSolvers.ord2")));
			
	    } catch (Exception e) {
	        throw new Exception(e);
	    }
	}
	
	   private void getExamSolvers(HttpServletRequest request) throws Exception {
	        try {
	            WebTable.setOrder(sessionContext,"manageSolvers.ord3",request.getParameter("ord3"),1);
	            
	            WebTable webTable = new WebTable( 20,
	                    "Manage Examination Solvers", "manageSolvers.do?ord3=%%",
	                    new String[] {"Created", "Last Used", "Session", "Host", "Config", "Status", "Owner", "Type", "Assign", "Total", "StudConf", "InstConf", "Period", "Room", "RoomSplit", "RoomSize", "Distr", "Rot", "Pert", "Operation(s)"},
	                    new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left"},
	                    null );
	            webTable.setRowStyle("white-space:nowrap");
	            
	            int nrLines = 0;
	            Long currentSessionId = sessionContext.getUser().getCurrentAcademicSessionId();
	            
	            ExamSolverProxy x = examinationSolverService.getSolver();
	            String xId = (x==null?null:x.getProperties().getProperty("General.OwnerPuid"));
	            
	            for (ExamSolverProxy solver : examinationSolverService.getSolvers().values()) {
	            	if (solver==null) continue;
					DataProperties properties = solver.getProperties();
					if (properties==null) continue;
	                String runnerName = getName(properties.getProperty("General.OwnerPuid","N/A"));
	                Long examTypeId = solver.getExamTypeId();
	                if (runnerName==null)
	                    runnerName = "N/A";
	                Session session = (new SessionDAO()).get(properties.getPropertyLong("General.SessionId",new Long(-1)));
	                if (session == null || sessionContext.getUser().getAuthorities(sessionContext.getUser().getCurrentAuthority().getRole(), session).isEmpty()) continue;
	                String sessionLabel = "N/A";
	                if (session!=null)
	                    sessionLabel = session.getLabel();
	                SolverPredefinedSetting setting = (new SolverPredefinedSettingDAO()).get(properties.getPropertyLong("General.SettingsId",new Long(-1)));
	                String settingLabel = properties.getProperty("Basic.Mode","N/A");
	                if (setting!=null)
	                    settingLabel = setting.getDescription();
	                String onClick = null;
	                if (session.getUniqueId().equals(currentSessionId) && properties.getProperty("General.OwnerPuid")!=null)
	                    onClick = "onClick=\"document.location='manageSolvers.do?op=Select&examPuid=" + properties.getProperty("General.OwnerPuid") + "';\"";
	                String status = (String)solver.getProgress().get("STATUS");
	                
	                Map<String,String> info = null;
	                try {
	                	info = solver.currentSolutionInfo();
	                } catch (Exception e) {}
	                String assigned = (String)info.get("Assigned variables");
	                String totVal = (String)info.get("Overall solution value");
	                String dc = (String)info.get("Direct Conflicts");
	                String m2d = (String)info.get("More Than 2 A Day Conflicts");
	                String btb = (String)info.get("Back-To-Back Conflicts");
	                String conf = (dc==null?"0":dc)+", "+(m2d==null?"0":m2d)+", "+(btb==null?"0":btb);
                    String idc = (String)info.get("Instructor Direct Conflicts");
                    String im2d = (String)info.get("Instructor More Than 2 A Day Conflicts");
                    String ibtb = (String)info.get("Instructor Back-To-Back Conflicts");
                    String iconf = (idc==null?"0":idc)+", "+(im2d==null?"0":im2d)+", "+(ibtb==null?"0":ibtb);
	                String pp = (String)info.get("Period Penalty");
	                String rp = (String)info.get("Room Penalty");
	                String rsp = (String)info.get("Room Split Penalty");
	                String rsz = (String)info.get("Room Size Penalty");
	                String dp = (String)info.get("Distribution Penalty");
	                String erp = (String)info.get("Exam Rotation Penalty");
	                String pert = (String)info.get("Perturbation Penalty");
	                Date loaded = solver.getLoadedDate();
	                Date lastUsed = solver.getLastUsed(); 
	                
                    String bgColor = null;
                    if (x!=null && ToolBox.equals(properties.getProperty("General.OwnerPuid"), xId))
                        bgColor = "rgb(168,187,225)";
                    
                    String op = "";
                    op += 
                    	"<input type=\"button\" value=\"Unload\" onClick=\"" +
                    	"if (confirm('Do you really want to unload this solver?')) " +
                    	"document.location='manageSolvers.do?op=Unload&examPuid=" + properties.getProperty("General.OwnerPuid")+ "';" +
                    	" event.cancelBubble=true;\">";
                    
                    ExamType examType = (examTypeId == null ? null : ExamTypeDAO.getInstance().get(examTypeId));
                    
	                webTable.addLine(onClick, new String[] {
	                            (loaded==null?"N/A":sDF.format(loaded)),
	                            (lastUsed==null?"N/A":sDF.format(lastUsed)),
	                            sessionLabel,
	                            solver.getHost(),
	                            settingLabel,
	                            status,
	                            runnerName, 
	                            (examType==null?"N/A?":examType.getLabel()),
	                            (assigned==null?"N/A":assigned.indexOf(' ')>=0?assigned.substring(0,assigned.indexOf(' ')):assigned),
	                            (totVal==null?"N/A":totVal),
	                            (conf==null?"N/A":conf), 
	                            (iconf==null?"N/A":iconf),
	                            (pp==null?"N/A":pp.indexOf(' ')>=0?pp.substring(0,pp.indexOf(' ')):pp),
	                            (rp==null?"N/A":rp.indexOf(' ')>=0?rp.substring(0,rp.indexOf(' ')):rp),
	                            (rsp==null?"N/A":rsp.indexOf(' ')>=0?rsp.substring(0,rsp.indexOf(' ')):rsp), 
	                            (rsz==null?"N/A":rsz.indexOf(' ')>=0?rsz.substring(0,rsz.indexOf(' ')):rsz),
	                            (dp==null?"N/A":dp.indexOf(' ')>=0?dp.substring(0,dp.indexOf(' ')):dp),
	                            (erp==null?"N/A":erp.indexOf(' ')>=0?erp.substring(0,erp.indexOf(' ')):erp),
	                            (pert==null?"N/A":pert.indexOf(' ')>=0?pert.substring(0,pert.indexOf(' ')):pert),
	                            op},
	                        new Comparable[] {
	                            (loaded==null?new Date():loaded),
	                            (lastUsed==null?new Date():lastUsed),
	                            sessionLabel,
	                            solver.getHost(),
	                            settingLabel, 
	                            status,
	                            runnerName,
	                            examTypeId,
	                            (assigned==null?"":assigned),
                                (totVal==null?"":totVal),
                                (conf==null?"":conf), 
                                (iconf==null?"":iconf),
                                (pp==null?"":pp), 
                                (rp==null?"":rp),
                                (rsp==null?"":rsp), 
                                (rsz==null?"":rsz),
                                (dp==null?"":dp), 
                                (erp==null?"":erp), 
                                (pert==null?"":pert),
                                null}).setBgColor(bgColor);
	                    nrLines++;
	            }
	            if (nrLines==0)
	                webTable.addLine(null, new String[] {"<i>No solver is running.</i>"}, null, null );
	            request.setAttribute("ManageSolvers.xtable",webTable.printTable(WebTable.getOrder(sessionContext,"manageSolvers.ord3")));
	            
	        } catch (Exception e) {
	            throw new Exception(e);
	        }
	    }

       private void getStudentSolvers(HttpServletRequest request) throws Exception {
           try {
               WebTable.setOrder(sessionContext,"manageSolvers.ord4",request.getParameter("ord4"),1);
               
               WebTable webTable = new WebTable( 13,
                       "Manage Student Sectioning Solvers", "manageSolvers.do?ord4=%%",
                       new String[] {"Created", "Last Used", "Session", "Host", "Config", "Status", "Owner", "Assign", "Total", "CompSched", "DistConf", "Pert", "Operation(s)"},
                       new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left"},
                       null );
               webTable.setRowStyle("white-space:nowrap");
               
               int nrLines = 0;
               Long currentSessionId = sessionContext.getUser().getCurrentAcademicSessionId();
               
               StudentSolverProxy x = studentSectioningSolverService.getSolver();
               String xId = (x==null?null:x.getProperties().getProperty("General.OwnerPuid"));
               
               for (StudentSolverProxy solver : studentSectioningSolverService.getSolvers().values()) {
      				if (solver==null) continue;
    				DataProperties properties = solver.getProperties();
    				if (properties==null) continue;
                   String runnerName = getName(properties.getProperty("General.OwnerPuid","N/A"));
                   if (runnerName==null)
                       runnerName = "N/A";
                   Session session = (new SessionDAO()).get(properties.getPropertyLong("General.SessionId",new Long(-1)));
                   if (session == null || sessionContext.getUser().getAuthorities(sessionContext.getUser().getCurrentAuthority().getRole(), session).isEmpty()) continue;
                   String sessionLabel = session.getLabel();
                   SolverPredefinedSetting setting = (new SolverPredefinedSettingDAO()).get(properties.getPropertyLong("General.SettingsId",new Long(-1)));
                   String settingLabel = properties.getProperty("Basic.Mode","N/A");
                   if (setting!=null)
                       settingLabel = setting.getDescription();
                   String onClick = null;
                   if (session.getUniqueId().equals(currentSessionId) && properties.getProperty("General.OwnerPuid")!=null)
                       onClick = "onClick=\"document.location='manageSolvers.do?op=Select&sectionPuid=" + properties.getProperty("General.OwnerPuid") + "';\"";
                   String status = (String)solver.getProgress().get("STATUS");
                   
                   Map<String,String> info = null;
                   try {
                	   info = solver.currentSolutionInfo();
                   } catch (Exception e) {}
                   String assigned = (String)info.get("Assigned variables");
                   String totVal = (String)info.get("Overall solution value");
                   String compSch = (String)info.get("Students with complete schedule");
                   String distConf = (String)info.get("Student distance conflicts");
                   String pert = (String)info.get("Perturbation Penalty");
                   Date loaded = solver.getLoadedDate();
                   Date lastUsed = solver.getLastUsed(); 
                   
                   String bgColor = null;
                   if (x!=null && ToolBox.equals(properties.getProperty("General.OwnerPuid"), xId))
                       bgColor = "rgb(168,187,225)";
                   
                   String op = "";
                   op += 
                   	"<input type=\"button\" value=\"Unload\" onClick=\"" +
                   	"if (confirm('Do you really want to unload this solver?')) " +
                   	"document.location='manageSolvers.do?op=Unload&sectionPuid=" + properties.getProperty("General.OwnerPuid")+ "';" +
                   	" event.cancelBubble=true;\">";
                   
                   webTable.addLine(onClick, new String[] {
                               (loaded==null?"N/A":sDF.format(loaded)),
                               (lastUsed==null?"N/A":sDF.format(lastUsed)),
                               sessionLabel,
                               solver.getHost(),
                               settingLabel,
                               status,
                               runnerName, 
                               (assigned==null?"N/A":assigned.indexOf(' ')>=0?assigned.substring(0,assigned.indexOf(' ')):assigned),
                               (totVal==null?"N/A":totVal),
                               (compSch==null?"N/A":compSch), 
                               (distConf==null?"N/A":distConf),
                               (pert==null?"N/A":pert.indexOf(' ')>=0?pert.substring(0,pert.indexOf(' ')):pert),
                               op},
                           new Comparable[] {
                               (loaded==null?new Date():loaded),
                               (lastUsed==null?new Date():lastUsed),
                               sessionLabel,
                               solver.getHost(),
                               settingLabel, 
                               status,
                               runnerName,
                               (assigned==null?"":assigned),
                               (totVal==null?"":totVal),
                               (compSch==null?"":compSch), 
                               (distConf==null?"":distConf),
                               (pert==null?"":pert),
                               null}).setBgColor(bgColor);
                       nrLines++;
               }
               if (nrLines==0)
                   webTable.addLine(null, new String[] {"<i>No solver is running.</i>"}, null, null );
               request.setAttribute("ManageSolvers.stable",webTable.printTable(WebTable.getOrder(sessionContext,"manageSolvers.ord4")));
               
           } catch (Exception e) {
               throw new Exception(e);
           }
       }
       
       private void getOnlineSolvers(HttpServletRequest request) throws Exception {
           try {
               WebTable.setOrder(sessionContext,"manageSolvers.ord5",request.getParameter("ord5"),1);
               
               WebTable webTable = new WebTable( 13,
                       "Manage Online Scheduling Servers", "manageSolvers.do?ord5=%%",
                       new String[] {"Created", "Session", "Host", "Mode", "Assign", "Total", "CompSched", "DistConf", "TimeConf", "FreeConf", "AvgDisb", "Disb[>=10%]", "Operation(s)"},
                       new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left"},
                       null );
               webTable.setRowStyle("white-space:nowrap");
               
               int nrLines = 0;
               
               List<SolverServer> servers = solverServerService.getServers(true);
               for (SolverServer server: servers) {
                   for (String sessionId : server.getOnlineStudentSchedulingContainer().getSolvers()) {
                	   OnlineSectioningServer solver = server.getOnlineStudentSchedulingContainer().getSolver(sessionId);
                	   if (solver==null) continue;
                	   DataProperties properties = solver.getConfig();
                	   if (properties==null) continue;
                       if (sessionContext.getUser().getAuthorities(sessionContext.getUser().getCurrentAuthority().getRole(), new SimpleQualifier("Session", Long.valueOf(sessionId))).isEmpty()) continue;
                       String sessionLabel = solver.getAcademicSession().toString();
                       String mode = solver.getAcademicSession().isSectioningEnabled() ? "Online" : "Assistant";
                       Map<String,String> info = solver.execute(new GetInfo(), null);
                       String assigned = (info == null ? null : info.get("Assigned variables"));
                       String totVal = (info == null ? null : info.get("Overall solution value"));
                       String compSch = (info == null ? null : info.get("Students with complete schedule"));
                       String distConf = (info == null ? null : info.get("Student distance conflicts"));
                       String time = (info == null ? null : info.get("Time overlapping conflicts"));
                       String free = (info == null ? null : info.get("Free time overlapping conflicts"));
                       String disb = (info == null ? null : info.get("Average disbalance"));
                       String disb10 = (info == null ? null : info.get("Sections disbalanced by 10% or more"));
                       Date loaded = new Date(solver.getConfig().getPropertyLong("General.StartUpDate", 0));

                       String op = "";
                       if (solver.isMaster() && servers.size() > 1) {
                           op += "<input type=\"button\" value=\"Shutdown All\" onClick=\"" +
                        			"if (confirm('Do you really want to shutdown this server?')) " +
                        			"document.location='manageSolvers.do?op=Unload&onlineId=" + sessionId + "';" + 
                        			" event.cancelBubble=true;\">&nbsp;&nbsp;";
                           op += "<input type=\"button\" value=\"Un-Master\" onClick=\"" +
                         			"if (confirm('Do you really want to un-master this server?')) " +
                         			"document.location='manageSolvers.do?op=Unmaster&onlineId=" + sessionId + "&host=" + server.getHost() + "';" + 
                         			" event.cancelBubble=true;\">";
                       } else {
                           op += "<input type=\"button\" value=\"Shutdown\" onClick=\"" +
                        			"if (confirm('Do you really want to shutdown this server?')) " +
                        			"document.location='manageSolvers.do?op=Unload&onlineId=" + sessionId + "&host=" + server.getHost() + "';" + 
                        			" event.cancelBubble=true;\">";
                       }
                       
                       webTable.addLine(null, new String[] {
                                   (loaded.getTime() <= 0 ? "N/A" : sDF.format(loaded)),
                                   sessionLabel,
                                   solver.getHost() + (solver.isMaster() ? " (master)" : ""),
                                   mode,
                                   (assigned==null?"N/A":assigned),
                                   (totVal==null?"N/A":totVal),
                                   (compSch==null?"N/A":compSch), 
                                   (distConf==null?"N/A":distConf),
                                   (time==null?"N/A":time),
                                   (free==null?"N/A":free),
                                   (disb==null?"N/A":disb),
                                   (disb10==null?"N/A":disb10),
                                   op},
                               new Comparable[] {
                                   loaded,
                                   sessionLabel,
                                   solver.getHost(),
                                   mode, 
                                   (assigned==null?"":assigned),
                                   (totVal==null?"":totVal),
                                   (compSch==null?"":compSch), 
                                   (distConf==null?"":distConf),
                                   (time==null?"":time),
                                   (free==null?"":free),
                                   (disb==null?"":disb),
                                   (disb10==null?"":disb10),
                                   null});
                           nrLines++;
                   }
               }
               if (nrLines==0)
                   webTable.addLine(null, new String[] {"<i>There is no online student scheduling server running at the moment.</i>"}, null, null );
               request.setAttribute("ManageSolvers.otable",webTable.printTable(WebTable.getOrder(sessionContext,"manageSolvers.ord5")));
               
           } catch (Exception e) {
               throw new Exception(e);
           }
       }

}

