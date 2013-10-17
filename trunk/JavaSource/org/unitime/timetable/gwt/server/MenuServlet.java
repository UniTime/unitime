/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.PageNames;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.action.PersonalizedExamReportAction;
import org.unitime.timetable.form.ListSolutionsForm;
import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.shared.MenuException;
import org.unitime.timetable.gwt.shared.MenuInterface;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.context.AnonymousUserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.RoomAvailability;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author Tomas Muller
 */
@Service("menu.gwt")
public class MenuServlet implements MenuService {
	private static Logger sLog = Logger.getLogger(MenuServlet.class);
    private static Element iRoot = null;
    private static PageNames sPageNames = Localization.create(PageNames.class);
    
    @Autowired
    private SolverServerService solverServerService;

	public MenuServlet() {
		try {
			String menu = ApplicationProperties.getProperty("unitime.menu","menu.xml");
			Document document = null;
	        URL menuUrl = ApplicationProperties.class.getClassLoader().getResource(menu);
	        SAXReader sax = new SAXReader();
	        sax.setEntityResolver(new EntityResolver() {
	        	public InputSource resolveEntity(String publicId, String systemId) {
	        		if (publicId.equals("-//UniTime//UniTime Menu DTD/EN")) {
	        			return new InputSource(ApplicationProperties.class.getClassLoader().getResourceAsStream("menu.dtd"));
	        		}
	        		return null;
	        	}
	        });
	        if (menuUrl!=null) {
	            Debug.info("Reading menu from " + URLDecoder.decode(menuUrl.getPath(), "UTF-8") + " ...");
	            document = sax.read(menuUrl.openStream());
	        } else if (new File(menu).exists()) {
	            Debug.info("Reading menu from " + menu + " ...");
	            document = sax.read(new File(menu));
	        }
	        if (document==null)
	            throw new ServletException("Unable to create menu, reason: resource " + menu + " not found.");

	        if (!"unitime-menu".equals(document.getRootElement().getName())) throw new ServletException("Menu has an unknown format.");
	        iRoot = document.getRootElement();
	        
	        String customMenu = ApplicationProperties.getProperty("unitime.menu.custom","menu-custom.xml");
			Document customDocument = null;
	        URL customMenuUrl = ApplicationProperties.class.getClassLoader().getResource(customMenu);
	        if (customMenuUrl!=null) {
	            Debug.info("Reading custom menu from " + URLDecoder.decode(customMenuUrl.getPath(), "UTF-8") + " ...");
	            customDocument = sax.read(customMenuUrl.openStream());
	        } else if (new File(customMenu).exists()) {
	            Debug.info("Reading custom menu from " + customMenu + " ...");
	            customDocument = sax.read(new File(customMenu));
	        }
	        if (customDocument != null) {
	        	merge(iRoot, customDocument.getRootElement());
	        }
	        
		} catch (Exception e) {
			if (e instanceof RuntimeException) throw (RuntimeException)e;
			throw new RuntimeException("Unable to initialize, reason: "+e.getMessage(), e);
		}
	}
	
	private @Autowired SessionContext sessionContext;
	private SessionContext getSessionContext() { return sessionContext; }

	private void merge(Element menu, Element custom) {
		if ("remove".equals(custom.getName())) {
			menu.getParent().remove(menu);
			return;
		}
		for (Iterator<Attribute> i = custom.attributeIterator(); i.hasNext();) {
			Attribute a = i.next();
			menu.addAttribute(a.getName(), a.getValue());
		}
		for (Iterator<Element> i = custom.elementIterator(); i.hasNext(); ) {
			Element e = i.next();
			if ("parameter".equals(e.getName())) {
				for (Iterator<Element> j = menu.elementIterator("parameter"); j.hasNext(); ) {
					menu.remove(j.next());
				}
				menu.add(e.createCopy());
				continue;
			}
			if ("condition".equals(e.getName())) {
				menu.add(e.createCopy());
				continue;
			}
			if ("new-condition".equals(e.getName())) {
				for (Iterator<Element> j = menu.elementIterator("condition"); j.hasNext(); ) {
					menu.remove(j.next());
				}
				Element f = e.createCopy();
				f.setName("condition");
				menu.add(f);
				continue;
			}
			String name = e.attributeValue("name");
			Element x = null;
			if (name != null) {
				for (Iterator<Element> j = menu.elementIterator(); j.hasNext(); ) {
					Element f = j.next();
					if (name.equals(f.attributeValue("name"))) { x = f; break; }
				}
			}
			if (x != null) {
				merge(x, e);
			} else {
				int pos = Integer.parseInt(e.attributeValue("position", "-1"));
				if (pos >= 0) {
					List<Element> after = new ArrayList<Element>();
					for (Iterator<Element> j = menu.elementIterator(); j.hasNext(); ) {
						Element f = j.next();
						if ("condition".equals(f.getName())) continue;
						if (pos > 0) {
							pos--;
						} else {
							after.add(f);
							menu.remove(f);
						}
					}
					menu.add(e.createCopy());
					for (Element f: after)
						menu.add(f);
				} else
					menu.add(e.createCopy());
			}
		}
	}
	
	public List<MenuInterface> getMenu() throws MenuException {
		try {
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				List<MenuInterface> menu = new ArrayList<MenuInterface>();
				if (iRoot == null) throw new MenuException("menu is not configured properly");
				
				for (Iterator<Element> i = iRoot.elementIterator(); i.hasNext(); ) {
					Element element = i.next();
					MenuInterface m = getMenu(element);
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
	
	private MenuInterface getMenu(Element menuElement) {
		try {
		MenuInterface menu = new MenuInterface();
		String name = menuElement.attributeValue("name");
		String localizedName = (name == null ? null : sPageNames.translateMessage(
				name.trim().replace(' ', '_').replace("(", "").replace(")", "").replace(':', '_'), null));
		menu.setName(localizedName == null ? name : localizedName);
		menu.setTitle(menuElement.attributeValue("title"));
		menu.setTarget(menuElement.attributeValue("target"));
		menu.setPage(menuElement.attributeValue("page"));
		menu.setHash(menuElement.attributeValue("hash"));
		String type = menuElement.attributeValue("type");
		if ("gwt".equals(type)) menu.setGWT(true);
		if ("property".equals(type) && menu.getPage() != null) {
			menu.setPage(ApplicationProperties.getProperty(menu.getPage()));
			if (menu.getPage() == null) return null;
		}
		
		boolean sep = true;
		for (Iterator<Element> i = menuElement.elementIterator(); i.hasNext(); ) {
			Element element = i.next();
			if ("condition".equals(element.getName())) {
				if (!check(element)) return null;
			} else if ("parameter".equals(element.getName())) {
				menu.addParameter(element.attributeValue("name"), element.attributeValue("value", element.getText()));
			} else {
				MenuInterface m = getMenu(element);
				if (m != null) {
					if (sep && m.isSeparator()) continue;
					menu.addSubMenu(m);
					sep = m.isSeparator();
				}
			}
		}
		while (menu.hasSubMenus() && menu.getSubMenus().get(menu.getSubMenus().size() - 1).isSeparator())
			menu.getSubMenus().remove(menu.getSubMenus().size() - 1);
		return (menu.isSeparator() || menu.hasPage() || menu.hasSubMenus() ? menu : null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private boolean check(Element conditionElement) {
		String cond = conditionElement.getName();
		if ("and".equals(cond) || "condition".equals(cond)) {
			for (Iterator<Element> i = conditionElement.elementIterator(); i.hasNext(); ) {
				Element element = i.next();
				if (!check(element)) return false;
			}
			return true;
		} else  if ("or".equals(cond)) {
			for (Iterator<Element> i = conditionElement.elementIterator(); i.hasNext(); ) {
				Element element = i.next();
				if (check(element)) return true;
			}
			return false;
		} else if ("not".equals(cond)) {
			for (Iterator<Element> i = conditionElement.elementIterator(); i.hasNext(); ) {
				Element element = i.next();
				if (check(element)) return false;
			}
			return true;
		} else if ("isAuthenticated".equals(cond)) {
			return getSessionContext().isAuthenticated();
		} else if ("hasRole".equals(cond)) {
			UserContext user = getSessionContext().getUser();
			if (user == null) return false;
			String role = conditionElement.attributeValue("name");
			if (role == null) return sessionContext.hasPermission(Right.HasRole);; // has any role
			if (user.getCurrentAuthority() == null)
				return Roles.ROLE_ANONYMOUS.equals(role);
			else
				return role.equalsIgnoreCase(user.getCurrentAuthority().getRole());
		} else if ("propertyEquals".equals(cond)) {
			return conditionElement.attributeValue("value", "true").equalsIgnoreCase(ApplicationProperties.getProperty(
					conditionElement.attributeValue("name", "dummy"),
					conditionElement.attributeValue("defaultValue", "false")));
		} else if ("hasProperty".equals(cond)) {
			return ApplicationProperties.getProperty(conditionElement.attributeValue("name", "dummy")) != null;
		} else if ("hasPermission".equals(cond)) {
			Right right = null;
			try {
				right = Right.valueOf(conditionElement.attributeValue("name"));
			} catch (IllegalArgumentException e) {
				sLog.warn("Unknown right: " + conditionElement.attributeValue("name"));
			}
			if (right == null) return false;
			String authority = conditionElement.attributeValue("authority", "current");
			if ("session".equals(authority)) {
				Long sessionId = (sessionContext.isAuthenticated() ? sessionContext.getUser().getCurrentAcademicSessionId() : null);
				return (sessionId == null ? false : sessionContext.hasPermissionAnyAuthority(right, new SimpleQualifier("Session", sessionId)));
			} else if ("any".equals(authority)) {
				return sessionContext.hasPermissionAnyAuthority(right);
			} else {
				return sessionContext.hasPermission(right);
			}
		} else if ("hasRight".equals(cond)) {
			String right = conditionElement.attributeValue("name", "unknown");
			if ("canSeeEvents".equals(right)) {
				return getSessionContext().hasPermissionAnyAuthority(Right.Events);
			} else if ("hasRoomAvailability".equals(right)) {
				return RoomAvailability.getInstance() != null;
			} else if ("hasPersonalReport".equals(right)) {
				return getSessionContext().isAuthenticated() && PersonalizedExamReportAction.hasPersonalReport(getSessionContext().getUser().getExternalUserId());
			} else if ("isChameleon".equals(right)) {
				return getSessionContext().isAuthenticated() && (getSessionContext().hasPermission(Right.Chameleon) || getSessionContext().getUser() instanceof UserContext.Chameleon);
			} else if ("isSectioningEnabled".equals(right)) {
				return solverServerService.isOnlineStudentSchedulingEnabled();
			} else if ("isStudent".equals(right)) {
				return getSessionContext().isAuthenticated() && getSessionContext().getUser().hasRole(Roles.ROLE_STUDENT);
			} else if ("isInstructor".equals(right)) {
				return getSessionContext().isAuthenticated() && getSessionContext().getUser().hasRole(Roles.ROLE_INSTRUCTOR);
			} else if ("isRegistrationEnabled".equals(right)) {
				return solverServerService.isStudentRegistrationEnabled();
			} else {
				if ("canSeeCourses".equals(right)) {
					return sessionContext.hasPermission(Right.InstructionalOfferings) || sessionContext.hasPermission(Right.Classes);
				} else if ("canSeeTimetable".equals(right)) {
					return sessionContext.hasPermission(Right.ClassAssignments);
				} else if ("canDoTimetable".equals(right)) {
					return sessionContext.hasPermission(Right.CourseTimetabling);
				} else if ("hasASolverGroup".equals(right)) {
					return sessionContext.isAuthenticated() && !SolverGroup.getUserSolverGroups(sessionContext.getUser()).isEmpty();
				} else if ("canSectionStudents".equals(right)) {
					return sessionContext.hasPermission(Right.StudentScheduling);
				} else if ("canSeeExams".equals(right)) {
					return sessionContext.hasPermission(Right.Examinations);
				} else if ("canTimetableExams".equals(right)) {
					return sessionContext.hasPermission(Right.ExaminationTimetabling);
				} else if ("canAudit".equals(right)) {
					return sessionContext.hasPermission(Right.CourseTimetablingAudit);
				} else if ("hasCourseReports".equals(right)) {
					return sessionContext.hasPermission(Right.HQLReportsCourses) && SavedHQL.hasQueries(SavedHQL.Flag.APPEARANCE_COURSES, sessionContext.hasPermission(Right.HQLReportsAdminOnly));
				} else if ("hasExamReports".equals(right)) {
					return sessionContext.hasPermission(Right.HQLReportsExaminations) && SavedHQL.hasQueries(SavedHQL.Flag.APPEARANCE_EXAMS, sessionContext.hasPermission(Right.HQLReportsAdminOnly));
				} else if ("hasEventReports".equals(right)) {
					return sessionContext.hasPermission(Right.HQLReportsEvents) && SavedHQL.hasQueries(SavedHQL.Flag.APPEARANCE_EVENTS, sessionContext.hasPermission(Right.HQLReportsAdminOnly));
				} else if ("hasStudentReports".equals(right)) {
					return sessionContext.hasPermission(Right.HQLReportsStudents) && SavedHQL.hasQueries(SavedHQL.Flag.APPEARANCE_SECTIONING, sessionContext.hasPermission(Right.HQLReportsAdminOnly));
				}
			}
			sLog.warn("Unknown right " + right + ".");
			return true;
		}
		sLog.warn("Unknown condition " + cond + ".");
		return true;
	}
	
	public HashMap<String, String> getUserInfo() throws MenuException {
		try {
			HashMap<String, String> ret = new HashMap<String, String>();
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				
				UserContext user = getSessionContext().getUser();
				if (user == null || user instanceof AnonymousUserContext) return null;
				
				ret.put("0Name", user.getName());
				
				String dept = "";
				if (user.getCurrentAuthority() != null)
					for (Qualifiable q: user.getCurrentAuthority().getQualifiers("Department")) {
	 					if (!dept.isEmpty()) dept += ",";
	 					dept += "<span title='"+q.getQualifierLabel()+"'>"+q.getQualifierReference()+"</span>";
					}
		 		ret.put("1Dept", dept);
		 		
		 		String role = (user.getCurrentAuthority() == null ? null : user.getCurrentAuthority().getLabel());
		 		if (role == null) role = "No Role";
		 		ret.put("2Role", role);
		 		
		 		if (sessionContext.hasPermission(Right.Chameleon) || (user instanceof UserContext.Chameleon))
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
				
				UserContext user = getSessionContext().getUser();
				if (user == null) return null;
				
				if (user.getCurrentAcademicSessionId() == null) {
					if (sessionContext.hasPermissionAnyAuthority(Right.HasRole)) {
						ret.put("0Session", "Not Selected");
				 		ret.put("2Database", HibernateUtil.getDatabaseName());
				 		return ret;
					}
					return null;
				}
				
				Session session = SessionDAO.getInstance().get(user.getCurrentAcademicSessionId(), hibSession);
		 		
	 			ret.put("0Session", session.getLabel());
	 			ret.put("1Status", session.getStatusType().getLabel());
		 		
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
 		return "Version " + Constants.getVersion() + " built on " + Constants.getReleaseDate();
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
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	@Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;
	
	public HashMap<String, String> getSolverInfo(boolean includeSolutionInfo) throws MenuException {
		try {
			HashMap<String, String> ret = new HashMap<String, String>();
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				
				SolverProxy solver = courseTimetablingSolverService.getSolver();
				ExamSolverProxy examSolver = examinationSolverService.getSolver();
				StudentSolverProxy studentSolver = studentSectioningSolverService.getSolver();
				
				Map progress = (studentSolver!=null?studentSolver.getProgress():examSolver!=null?examSolver.getProgress():solver!=null?solver.getProgress():null);
				if (progress == null) return null;
				
				DataProperties properties = (studentSolver!=null?studentSolver.getProperties():examSolver!=null?examSolver.getProperties():solver.getProperties());
				String progressStatus = (String)progress.get("STATUS");
				String progressPhase = (String)progress.get("PHASE");
				long progressCur = ((Long)progress.get("PROGRESS")).longValue();
				long progressMax = ((Long)progress.get("MAX_PROGRESS")).longValue();
				String version = (String)progress.get("VERSION");
				if (version==null || "-1".equals(version)) version = "N/A";
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
				if (examSolver!=null) ownerName = ExamTypeDAO.getInstance().get(examSolver.getExamTypeId()).getLabel();
				if (ownerName==null || ownerName.length()==0)
					ownerName = "N/A";
				if (ownerName.equals("N/A"))
					ownerName = runnerName;
				if (runnerName.equals("N/A"))
					runnerName = ownerName;
				if (!ownerName.equals(runnerName))
					ownerName = runnerName+" as "+ownerName;
				if (ownerName.length() > 50)
					ownerName = ownerName.substring(0,47) + "...";

				ret.put("0Type",  (studentSolver!=null?"Student Sectioning Solver":examSolver!=null?"Examinations Solver":"Course Timetabling Solver"));
				ret.put("4Owner", ownerName);
				ret.put("5Host", (studentSolver!=null?studentSolver.getHost():examSolver!=null?examSolver.getHost():solver.getHost()));
				ret.put("1Solver", progressStatus);
				ret.put("2Phase", progressPhase);
				if (progressMax>0)
					ret.put("3Progress", (progressCur<progressMax?progressCur:progressMax) + " of " + progressMax + " (" + new DecimalFormat("0.0").format(progressPercent) + "%)");
				ret.put("7Version", version);
				ret.put("6Session", SessionDAO.getInstance().get(properties.getPropertyLong("General.SessionId",null)).getLabel());
				
				if (includeSolutionInfo) {
					Map<String,String> info = null;
					if (studentSolver != null) {
						info = studentSolver.statusSolutionInfo();
					} else if (examSolver != null) {
						info = examSolver.statusSolutionInfo();
					} else if (solver != null) {
						info = solver.statusSolutionInfo();
					}
					
					if (info != null) {
						TreeSet<String> keys = new TreeSet<String>(new ListSolutionsForm.InfoComparator());
						keys.addAll(info.keySet());
						int idx = 0;
						for (String key: keys) {
							ret.put((char)('A' + idx) + key, (String)info.get(key));
							idx++;
						}
					}
				}
		 		
			} finally {
				hibSession.close();
			}
			return ret;
		} catch (Exception e) {
			sLog.warn("Unable to get solver info: " + e.getMessage());
			if (e instanceof MenuException) throw (MenuException)e;
			throw new MenuException(e.getMessage());
		}
	}
	
	public String[] getHelpPageAndLocalizedTitle(String title) throws MenuException {
		String name = title.trim().replace(' ', '_').replace("(", "").replace(")", "").replace(':', '_');
		String help = null;
		if ("true".equals(ApplicationProperties.getProperty("tmtbl.wiki.help", "true")) && ApplicationProperties.getProperty("tmtbl.wiki.url") != null) {
			help = ApplicationProperties.getProperty("tmtbl.wiki.url") + name;
		}
		return new String[] {
				help,
				sPageNames.translateMessage(name, title)
				};
	}
	
	public String getUserData(String property) throws MenuException {
		try {
			UserContext user = getSessionContext().getUser();
			if (user == null) return null;
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				return UserData.getProperty(user.getExternalUserId(), property);
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
			UserContext user = getSessionContext().getUser();
			if (user == null) return null;
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				UserData.setProperty(user.getExternalUserId(), property, value);
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
			UserContext user = getSessionContext().getUser();
			if (user == null) return null;
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				return UserData.getProperties(user.getExternalUserId(), property);
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
			UserContext user = getSessionContext().getUser();
			if (user == null) return null;
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				for (String[] p: property2value) {
					UserData.setProperty(user.getExternalUserId(), p[0], p[1]);
				}
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
