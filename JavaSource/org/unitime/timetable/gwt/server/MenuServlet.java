/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.server;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.ServletException;

import net.sf.cpsolver.ifs.util.DataProperties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.action.PersonalizedExamReportAction;
import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.shared.MenuException;
import org.unitime.timetable.gwt.shared.MenuInterface;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.RoomAvailability;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class MenuServlet extends RemoteServiceServlet implements MenuService {
	private static Logger sLog = Logger.getLogger(MenuServlet.class);
	private static DecimalFormat sDF = new DecimalFormat("0.0");
    private static Element iRoot = null;

	public void init() throws ServletException {
		try {
			String menu = ApplicationProperties.getProperty("unitime.menu","menu.xml");
			Document document = null;
	        URL dbUpdateFileUrl = ApplicationProperties.class.getClassLoader().getResource(menu);
	        if (dbUpdateFileUrl!=null) {
	            Debug.info("Reading menu from " + URLDecoder.decode(dbUpdateFileUrl.getPath(), "UTF-8") + " ...");
	            document = (new SAXReader()).read(dbUpdateFileUrl.openStream());
	        } else if (new File(menu).exists()) {
	            Debug.info("Reading menu from " + menu + " ...");
	            document = (new SAXReader()).read(new File(menu));
	        }
	        if (document==null)
	            throw new ServletException("Unable to create menu, reason: resource " + menu + " not found.");

	        if (!"unitime-menu".equals(document.getRootElement().getName())) throw new ServletException("Menu has an unknown format.");
	        iRoot = document.getRootElement();
		} catch (Exception e) {
			if (e instanceof ServletException) throw (ServletException)e;
			throw new ServletException("Unable to initialize, reason: "+e.getMessage(), e);
		}
	}
	
	public List<MenuInterface> getMenu() throws MenuException {
		try {
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				// init();
				
				List<MenuInterface> menu = new ArrayList<MenuInterface>();
				if (iRoot == null) throw new MenuException("menu is not configured properly");
				
				UserInfo user = new UserInfo();
				
				for (Iterator<Element> i = iRoot.elementIterator(); i.hasNext(); ) {
					Element element = i.next();
					MenuInterface m = getMenu(user, element);
					if (m != null) menu.add(m);
				}

				if (menu.isEmpty())
					throw new MenuException("no menu");
				
				return menu;
			} finally {
				hibSession.close();
			}
		} catch (Exception e) {
			if (e instanceof MenuException) throw (MenuException)e;
			throw new MenuException(e.getMessage());
		}
	}
	
	private MenuInterface getMenu(UserInfo user, Element menuElement) {
		MenuInterface menu = new MenuInterface();
		menu.setName(menuElement.attributeValue("name"));
		menu.setTitle(menuElement.attributeValue("title"));
		menu.setTarget(menuElement.attributeValue("target"));
		menu.setPage(menuElement.attributeValue("page"));
		String type = menuElement.attributeValue("type");
		if ("gwt".equals(type)) menu.setGWT(true);
		if ("property".equals(type) && menu.getPage() != null) {
			menu.setPage(ApplicationProperties.getProperty(menu.getPage()));
			if (menu.getPage() == null) return null;
		}
		
		for (Iterator<Element> i = menuElement.elementIterator(); i.hasNext(); ) {
			Element element = i.next();
			if ("condition".equals(element.getName())) {
				if (!check(user, element)) return null;
			} else {
				MenuInterface m = getMenu(user, element);
				if (m != null) menu.addSubMenu(m);
			}
		}
		return menu;
	}
	
	private boolean check(UserInfo userInfo, Element conditionElement) {
		String cond = conditionElement.getName();
		if ("and".equals(cond) || "condition".equals(cond)) {
			for (Iterator<Element> i = conditionElement.elementIterator(); i.hasNext(); ) {
				Element element = i.next();
				if (!check(userInfo, element)) return false;
			}
			return true;
		} else  if ("or".equals(cond)) {
			for (Iterator<Element> i = conditionElement.elementIterator(); i.hasNext(); ) {
				Element element = i.next();
				if (check(userInfo, element)) return true;
			}
			return false;
		} else if ("not".equals(cond)) {
			for (Iterator<Element> i = conditionElement.elementIterator(); i.hasNext(); ) {
				Element element = i.next();
				if (check(userInfo, element)) return false;
			}
			return true;
		} else if ("isAuthenticated".equals(cond)) {
			return userInfo.getUser() != null;
		} else if ("hasRole".equals(cond)) {
			User user = userInfo.getUser();
			if (user == null) return false;
			String role = conditionElement.attributeValue("name");
			if (role == null) return user.getRole() != null; // has any role
			return role.equalsIgnoreCase(user.getRole());
		} else if ("propertyEquals".equals(cond)) {
			return conditionElement.attributeValue("value", "true").equalsIgnoreCase(ApplicationProperties.getProperty(
					conditionElement.attributeValue("name", "dummy"),
					conditionElement.attributeValue("defaultValue", "false")));
		} else if ("hasProperty".equals(cond)) {
			return ApplicationProperties.getProperty(conditionElement.attributeValue("name", "dummy")) != null;
		} else if ("canSeeEvents".equals(cond)) {
			return userInfo.getUser() != null && TimetableManager.canSeeEvents(userInfo.getUser());
		} else if ("hasRoomAvailability".equals(cond)) {
			return RoomAvailability.getInstance() != null;
		} else if ("hasPersonalReport".equals(cond)) {
			return userInfo.getUser() != null && PersonalizedExamReportAction.hasPersonalReport(userInfo.getUser());
		} else if ("isChameleon".equals(cond)) {
			return getThreadLocalRequest().getSession().getAttribute("hdnAdminAlias")!=null && getThreadLocalRequest().getSession().getAttribute("hdnAdminAlias").toString().equals("1");
		} else {
			User user = userInfo.getUser();
			if (user == null) return false;
			TimetableManager manager = userInfo.getManager();
			if (manager == null) return false;
			Session session = userInfo.getSession();
			if (session == null) return false;
			if ("canSeeCourses".equals(cond)) {
				return manager.canSeeCourses(session, user);
			} else if ("canSeeTimetable".equals(cond)) {
				return manager.canSeeTimetable(session, user);
			} else if ("canDoTimetable".equals(cond)) {
				return manager.canDoTimetable(session, user);
			} else if ("hasASolverGroup".equals(cond)) {
				return manager.hasASolverGroup(session, user);
			} else if ("canSectionStudents".equals(cond)) {
				return manager.canSectionStudents(session, user);
			} else if ("canSeeExams".equals(cond)) {
				return manager.canSeeExams(session, user);
			} else if ("canTimetableExams".equals(cond)) {
				return manager.canTimetableExams(session, user);
			}
		}
		sLog.warn("Unknown condition " + cond + ".");
		return true;
	}
	
	private class UserInfo {
		User iUser = null;
		Session iSession = null;
		TimetableManager iManager = null;

		public UserInfo() {
			iUser = Web.getUser(getThreadLocalRequest().getSession());
			if (iUser != null) {
				Long sessionId = (Long) iUser.getAttribute(Constants.SESSION_ID_ATTR_NAME);
				if (sessionId != null) {
					iSession = SessionDAO.getInstance().get(sessionId);
				}
				iManager = TimetableManager.getManager(iUser);
			}
		}
		
		public User getUser() { return iUser; }
		public Session getSession() { return iSession; }
		public TimetableManager getManager() { return iManager; }
		
	}
	
	public HashMap<String, String> getUserInfo() throws MenuException {
		try {
			HashMap<String, String> ret = new HashMap<String, String>();
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				
				UserInfo user = new UserInfo();
				if (user.getUser() == null) 
					return null;
				
		 		String loginName = user.getUser().getLogin();
		 		String userName = Constants.toInitialCase(user.getUser().getName(), "-".toCharArray());

				ret.put("0Name", userName);
				
				String dept = "";
		 		TimetableManager manager = user.getManager();
		 		if (manager!=null) {
		 			for (Iterator i=manager.getDepartments().iterator();i.hasNext();) {
		 				Department d = (Department)i.next();
		 				if (d.getSessionId().equals(user.getSession().getUniqueId())) {
		 					if (dept.length()>0) dept += ",";
		 					dept += "<span title='"+d.getHtmlTitle()+"'>"+d.getShortLabel()+"</span>";
		 				}
		 			}
		 		} else {
		 			TreeSet depts = new TreeSet(user.getUser().getDepartments());
		 			for (Iterator i=depts.iterator();i.hasNext();) {
		 				dept += i.next().toString();
		 				if (i.hasNext()) dept += ",";
		 			}
		 		}
		 		ret.put("1Dept", dept);
		 		
		 		String role = user.getUser().getRole();
		 		if (role==null) role = "No Role";
		 		ret.put("2Role", role);
		 		
		 		if (user.getUser() != null && Roles.ADMIN_ROLE.equals(user.getUser().getRole()) || 
		 			(getThreadLocalRequest().getSession().getAttribute("hdnAdminAlias")!=null && getThreadLocalRequest().getSession().getAttribute("hdnAdminAlias").toString().equals("1")))
		 			ret.put("Chameleon", "");
		 		
			} finally {
				hibSession.close();
			}
			return ret;
		} catch (Exception e) {
			if (e instanceof MenuException) throw (MenuException)e;
			throw new MenuException(e.getMessage());
		}
	}
	
	public HashMap<String, String> getSessionInfo() throws MenuException {
		try {
			HashMap<String, String> ret = new HashMap<String, String>();
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				
				UserInfo user = new UserInfo();
		 		if (user.getSession() == null)
		 			return null;
		 		
	 			ret.put("0Session", user.getSession().getLabel());
	 			ret.put("1Status", user.getSession().getStatusType().getLabel());
		 		
		 		ret.put("2Database", HibernateUtil.getDatabaseName());
		 		
			} finally {
				hibSession.close();
			}
			return ret;
		} catch (Exception e) {
			if (e instanceof MenuException) throw (MenuException)e;
			throw new MenuException(e.getMessage());
		}
	}
	
	public String getVersion() throws MenuException {
 		return "Version " + Constants.VERSION + "." + Constants.BLD_NUMBER.replaceAll("@build.number@","?") + " built on " + Constants.REL_DATE.replaceAll("@build.date@", "?");
	}
	
	private String getName(String puid) {
		return getName(TimetableManager.findByExternalId(puid));
	}

	private String getName(TimetableManager mgr) {
		if (mgr==null) return null;
		return mgr.getShortName();
	}

	private String getName(SolverGroup gr) {
		if (gr==null) return null;
		return gr.getAbbv();
	}
	
	public HashMap<String, String> getSolverInfo() throws MenuException {
		try {
			HashMap<String, String> ret = new HashMap<String, String>();
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				
				SolverProxy solver = WebSolver.getSolver(getThreadLocalRequest().getSession());
				ExamSolverProxy examSolver = (solver==null?WebSolver.getExamSolverNoSessionCheck(getThreadLocalRequest().getSession()):null);
				StudentSolverProxy studentSolver = (solver==null && examSolver==null?WebSolver.getStudentSolverNoSessionCheck(getThreadLocalRequest().getSession()):null); 
				
				
				Map progress = (studentSolver!=null?studentSolver.getProgress():examSolver!=null?examSolver.getProgress():solver.getProgress());
				if (progress == null) return null;
				
				DataProperties properties = (studentSolver!=null?studentSolver.getProperties():examSolver!=null?examSolver.getProperties():solver.getProperties());
				String progressStatus = (String)progress.get("STATUS");
				String progressPhase = (String)progress.get("PHASE");
				long progressCur = ((Long)progress.get("PROGRESS")).longValue();
				long progressMax = ((Long)progress.get("MAX_PROGRESS")).longValue();
				String version = (String)progress.get("VERSION");
				if (version==null || "-1".equals(version)) version = "N/A";
				if (version.indexOf("@build.number@")>=0)
					version = version.replaceAll("@build.number@","?");
				double progressPercent = 100.0*((double)(progressCur<progressMax?progressCur:progressMax))/((double)progressMax);
				String runnerName = getName(properties.getProperty("General.OwnerPuid","N/A"));
				Long[] solverGroupId = properties.getPropertyLongArry("General.SolverGroupId",null);
				String ownerName = "";
				if (solverGroupId!=null) {
					for (int i=0;i<solverGroupId.length;i++) {
						if (i>0) ownerName += " & ";
						ownerName += getName((new SolverGroupDAO()).get(solverGroupId[i]));
					}
				}
				if (examSolver!=null) ownerName = Exam.sExamTypes[examSolver.getExamType()];
				if (ownerName==null || ownerName.length()==0)
					ownerName = "N/A";
				if (ownerName.equals("N/A"))
					ownerName = runnerName;
				if (runnerName.equals("N/A"))
					runnerName = ownerName;
				if (!ownerName.equals(runnerName))
					ownerName = runnerName+" as "+ownerName;

				ret.put("0Type",  (studentSolver!=null?"Student Sectioning Solver":examSolver!=null?"Examinations Solver":"Course Timetabling Solver"));
				ret.put("4Owner", ownerName);
				ret.put("5Host", (studentSolver!=null?studentSolver.getHostLabel():examSolver!=null?examSolver.getHostLabel():solver.getHostLabel()));
				ret.put("1Solver", progressStatus);
				ret.put("2Phase", progressPhase);
				if (progressMax>0)
					ret.put("3Progress", (progressCur<progressMax?progressCur:progressMax) + " of " + progressMax + " (" + Web.format(progressPercent) + "%)");
				ret.put("7Version", version);
				ret.put("6Session", SessionDAO.getInstance().get(properties.getPropertyLong("General.SessionId",null)).getLabel());
		 		
			} finally {
				hibSession.close();
			}
			return ret;
		} catch (Exception e) {
			if (e instanceof MenuException) throw (MenuException)e;
			throw new MenuException(e.getMessage());
		}
	}
	
	public String getHelpPage(String title) throws MenuException {
		if ("true".equals(ApplicationProperties.getProperty("tmtbl.wiki.help", "true")) && ApplicationProperties.getProperty("tmtbl.wiki.url") != null) {
			return ApplicationProperties.getProperty("tmtbl.wiki.url") + title.trim().replace(' ', '_');
		} else {
			throw new MenuException("help pages are disabled");
		}
	}
	
	public String getUserData(String property) throws MenuException {
		try {
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				return UserData.getProperty(getThreadLocalRequest().getSession(), property);
			} finally {
				hibSession.close();
			}
		} catch (Exception e) {
			if (e instanceof MenuException) throw (MenuException)e;
			throw new MenuException(e.getMessage());
		}
	}
	
	public Boolean setUserData(String property, String value) throws MenuException {
		try {
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				UserData.setProperty(getThreadLocalRequest().getSession(), property, value);
				return null;
			} finally {
				hibSession.close();
			}
		} catch (Exception e) {
			if (e instanceof MenuException) throw (MenuException)e;
			throw new MenuException(e.getMessage());
		}
	}
	
	public HashMap<String, String> getUserData(Collection<String> property) throws MenuException {
		try {
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				return UserData.getProperties(getThreadLocalRequest().getSession(), property);
			} finally {
				hibSession.close();
			}
		} catch (Exception e) {
			if (e instanceof MenuException) throw (MenuException)e;
			throw new MenuException(e.getMessage());
		}
	}
	
	public Boolean setUserData(List<String[]> property2value) throws MenuException {
		try {
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				for (String[] p: property2value)
					UserData.setProperty(getThreadLocalRequest().getSession(), p[0], p[1]);
				return null;
			} finally {
				hibSession.close();
			}
		} catch (Exception e) {
			if (e instanceof MenuException) throw (MenuException)e;
			throw new MenuException(e.getMessage());
		}
	}


}
