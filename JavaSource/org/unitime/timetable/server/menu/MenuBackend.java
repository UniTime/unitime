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
package org.unitime.timetable.server.menu;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.PageNames;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.action.PersonalizedExamReportAction;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.MenuInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.MenuRpcRequest;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.RoomAvailability;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(MenuRpcRequest.class)
@DependsOn({"startupService"})
public class MenuBackend implements GwtRpcImplementation<MenuRpcRequest, GwtRpcResponseList<MenuInterface>>, InitializingBean {
	private static Logger sLog = Logger.getLogger(MenuBackend.class);
    protected Element iRoot = null;
    private static PageNames sPageNames = Localization.create(PageNames.class);
    
    @Autowired
    private SolverServerService solverServerService;

    @Autowired 
	private SessionContext sessionContext;

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			String menu = ApplicationProperty.MenuFile.value();
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
	            sLog.info("Reading menu from " + URLDecoder.decode(menuUrl.getPath(), "UTF-8") + " ...");
	            document = sax.read(menuUrl.openStream());
	        } else if (new File(menu).exists()) {
	            sLog.info("Reading menu from " + menu + " ...");
	            document = sax.read(new File(menu));
	        }
	        if (document==null)
	            throw new ServletException("Unable to create menu, reason: resource " + menu + " not found.");

	        if (!"unitime-menu".equals(document.getRootElement().getName())) throw new ServletException("Menu has an unknown format.");
	        iRoot = document.getRootElement();
	        
	        String customMenu = ApplicationProperty.CustomMenuFile.value();
			Document customDocument = null;
	        URL customMenuUrl = ApplicationProperties.class.getClassLoader().getResource(customMenu);
	        if (customMenuUrl!=null) {
	        	sLog.info("Reading custom menu from " + URLDecoder.decode(customMenuUrl.getPath(), "UTF-8") + " ...");
	            customDocument = sax.read(customMenuUrl.openStream());
	        } else if (new File(customMenu).exists()) {
	        	sLog.info("Reading custom menu from " + customMenu + " ...");
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

	@Override
	public GwtRpcResponseList<MenuInterface> execute(MenuRpcRequest request, SessionContext context) {
		try {
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				GwtRpcResponseList<MenuInterface> menu = new GwtRpcResponseList<MenuInterface>();
				if (iRoot == null) throw new GwtRpcException("menu is not configured properly");
				
				for (Iterator<Element> i = iRoot.elementIterator(); i.hasNext(); ) {
					Element element = i.next();
					MenuInterface m = getMenu(element);
					if (m != null) menu.add(m);
				}

				if (menu.isEmpty())
					throw new GwtRpcException("no menu");
				
				return menu;
			} finally {
				hibSession.close();
			}
		} catch (GwtRpcException e) {
			throw e;
		} catch (Exception e) {
			throw new GwtRpcException(e.getMessage());
		}
	}

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
			return sessionContext.isAuthenticated();
		} else if ("hasRole".equals(cond)) {
			UserContext user = sessionContext.getUser();
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
			} else if ("role".equals(authority)) {
				UserAuthority ua = (sessionContext.isAuthenticated() ? sessionContext.getUser().getCurrentAuthority() : null);
				String role = (ua != null ? ua.getRole() : null);
				return (role == null ? false : sessionContext.hasPermissionAnyAuthority(right, new SimpleQualifier("Role", role)));
			} else if ("any".equals(authority)) {
				return sessionContext.hasPermissionAnyAuthority(right);
			} else {
				return sessionContext.hasPermission(right);
			}
		} else if ("hasRight".equals(cond)) {
			String right = conditionElement.attributeValue("name", "unknown");
			if ("canSeeEvents".equals(right)) {
				return sessionContext.hasPermissionAnyAuthority(Right.Events);
			} else if ("hasRoomAvailability".equals(right)) {
				return RoomAvailability.getInstance() != null;
			} else if ("hasPersonalReport".equals(right)) {
				return sessionContext.isAuthenticated() && PersonalizedExamReportAction.hasPersonalReport(sessionContext.getUser().getExternalUserId());
			} else if ("isChameleon".equals(right)) {
				return sessionContext.isAuthenticated() && (sessionContext.hasPermission(Right.Chameleon) || sessionContext.getUser() instanceof UserContext.Chameleon);
			} else if ("isSectioningEnabled".equals(right)) {
				return solverServerService.isOnlineStudentSchedulingEnabled();
			} else if ("isStudent".equals(right)) {
				return sessionContext.isAuthenticated() && sessionContext.getUser().hasRole(Roles.ROLE_STUDENT);
			} else if ("isInstructor".equals(right)) {
				return sessionContext.isAuthenticated() && sessionContext.getUser().hasRole(Roles.ROLE_INSTRUCTOR);
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
				} else if ("hasPointInTimeDataReports".equals(right)  && !PointInTimeData.findAllForSession(sessionContext.getUser().getCurrentAcademicSessionId()).isEmpty()) {
					return sessionContext.hasPermission(Right.PointInTimeDataReports);					
				}
			}
			sLog.warn("Unknown right " + right + ".");
			return true;
		}
		sLog.warn("Unknown condition " + cond + ".");
		return true;
	}
}
