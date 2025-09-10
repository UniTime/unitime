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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Encoding;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.application.DecodeRepresentation;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.DegreePlansProvider;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.purdue.EduNavInterface.Element;
import org.unitime.timetable.onlinesectioning.custom.purdue.EduNavInterface.Major;
import org.unitime.timetable.onlinesectioning.custom.purdue.EduNavInterface.Plan;
import org.unitime.timetable.onlinesectioning.custom.purdue.EduNavInterface.Program;
import org.unitime.timetable.onlinesectioning.custom.purdue.EduNavInterface.Result;
import org.unitime.timetable.onlinesectioning.custom.purdue.EduNavInterface.Rule;
import org.unitime.timetable.onlinesectioning.custom.purdue.EduNavInterface.Term;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EduNavPlansProvider implements DegreePlansProvider {
	private static Log sLog = LogFactory.getLog(EduNavPlansProvider.class);
	
	private Client iClient;
	private ExternalTermProvider iExternalTermProvider;
	private DegreePlansProvider iFallback;
	
	public EduNavPlansProvider() {
		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.HTTP);
		protocols.add(Protocol.HTTPS);
		iClient = new Client(protocols);
		Context cx = new Context();
		cx.getParameters().add("readTimeout", getEduNavApiReadTimeout());
		iClient.setContext(cx);
		try {
			String clazz = ApplicationProperty.CustomizationExternalTerm.value();
			if (clazz == null || clazz.isEmpty())
				iExternalTermProvider = new BannerTermProvider();
			else
				iExternalTermProvider = (ExternalTermProvider)Class.forName(clazz).getConstructor().newInstance();
		} catch (Exception e) {
			sLog.error("Failed to create external term provider, using the default one instead.", e);
			iExternalTermProvider = new BannerTermProvider();
		}
		try {
			String clazz = getEduNavFallbackProvider();
			if (clazz != null && !clazz.isEmpty()) {
				iFallback = (CriticalCoursesExplorers)Class.forName(clazz).getConstructor().newInstance();
			}
		} catch (Exception e) {
			sLog.error("Failed to create fallback degree plan provider.");
		}
	}
	
	protected String getEduNavApiReadTimeout() {
		return ApplicationProperties.getProperty("edunav.readTimeout", "60000");
	}
	
	protected String getEduNavApiSite() {
		return ApplicationProperties.getProperty("edunav.site");
	}
	
	protected String getEduNavApiAuthenticationKey() {
		return ApplicationProperties.getProperty("edunav.key.name", "edunav-api-key");
	}
	
	protected String getEduNavApiAuthenticationKeyValue() {
		return ApplicationProperties.getProperty("edunav.key.value");
	}
	
	protected String getEduNavFallbackProvider() {
		return ApplicationProperties.getProperty("edunav.fallbackProvider");
	}
	
	protected boolean getEduNavFallbackCombine() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("edunav.fallbackCombine"));
	}
	
	public String getEduNavIncludePlanContent() {
		return ApplicationProperties.getProperty("edunav.includePlanContent", "true");
	}
	
	protected String getDegreeWorksNoPlansMessage() {
		return ApplicationProperties.getProperty("edunav.noPlansMessage", "No EduNav plan is available.");
	}
	
	protected String getBannerId(XStudentId student) {
		String id = student.getExternalId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}
	
	protected String getBannerTerm(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalTerm(session);
	}
	
	protected Gson getGson(OnlineSectioningHelper helper) {
		GsonBuilder builder = new GsonBuilder();
		if (helper == null || helper.isDebugEnabled()) builder.setPrettyPrinting();
		return builder.create();
	}
	
	protected String toString(Reader reader) throws IOException {
		char[] buffer = new char[8192];
		StringBuilder out = new StringBuilder();
		int read = 0;
		while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
			out.append(buffer, 0, read);
		}
		reader.close();
		return out.toString();
	}
	
	protected boolean isCourse(Element element) {
		return element.id != null && element.id.indexOf(' ') >= 0 && element.name != null && !element.name.isEmpty();
	}
	
	protected String getSubject(Element element) {
		String courseName = element.id;
		if (courseName == null || courseName.indexOf(' ') <= 0) return null;
		return courseName.substring(0, courseName.lastIndexOf(' '));
	}
	
	protected String getCourseNbr(Element element) {
		String courseName = element.id;
		if (courseName == null || courseName.indexOf(' ') <= 0) return null;
		return courseName.substring(courseName.lastIndexOf(' ') + 1);
	}
	
	protected String getTitle(Element element) {
		Set<String> rules = new TreeSet<String>();
		if (element.rules != null)
			for (Rule rule: element.rules)
				if (rule.label != null && rule.label.text != null && !rule.label.text.isEmpty())
					rules.add(rule.label.text);
		if (rules.isEmpty()) return element.name;
		for (Iterator<String> it = rules.iterator(); it.hasNext(); ) {
			String rule = it.next();
			boolean contain = false;
			for (String other: rules) {
				if (!other.equals(rule) && other.contains(rule)) contain = true;
			}
			if (contain) it.remove();
		}
		String ret = "";
		for (String rule: rules)
			ret += (ret.isEmpty() ? "" : ", ") + rule;
		return ret;
	}
	
	protected String getDegree(Plan plan) {
		if (plan == null || plan.goal == null || plan.goal.programs == null) return null;
		Set<String> majors = new TreeSet<String>();
		for (Program program: plan.goal.programs) {
			if (program.majors != null)
				for (Major major: program.majors) {
					if (major.name != null)
						majors.add(major.name);
				}
		}
		String ret = "";
		for (String major: majors)
			ret += (ret.isEmpty() ? "" : ", ") + major;
		return ret;
	}
	
	protected String getDegreeShort(Plan plan) {
		if (plan == null || plan.goal == null || plan.goal.programs == null) return null;
		Set<String> majors = new TreeSet<String>();
		for (Program program: plan.goal.programs) {
			if (program.majors != null)
				for (Major major: program.majors) {
					if (major.id != null && major.degree != null)
						majors.add(major.id + "-" + major.degree);
					else if (major.id != null)
						majors.add(major.id);
				}
		}
		String ret = "";
		for (String major: majors)
			ret += (ret.isEmpty() ? "" : ", ") + major;
		return ret;
	}
	
	protected String getSchool(Plan plan) {
		if (plan == null || plan.goal == null || plan.goal.programs == null) return null;
		Set<String> schools = new TreeSet<String>();
		for (Program program: plan.goal.programs) {
			if (program.collegeName != null)
				schools.add(program.collegeName);
		}
		String ret = "";
		for (String school: schools)
			ret += (ret.isEmpty() ? "" : ", ") + school;
		return ret;
	}

	@Override
	public List<DegreePlanInterface> getDegreePlans(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, CourseMatcher matcher) throws SectioningException {
		if (iFallback == null) {
			List<DegreePlanInterface> ret = _getDegreePlans(server, helper, student, matcher);
			if (ret == null || ret.isEmpty())
				throw new SectioningException(getDegreeWorksNoPlansMessage()).withTypeInfo();
			return ret;
		}
		if (getEduNavFallbackCombine()) {
			List<DegreePlanInterface> ret1 = null, ret2 = null;
			SectioningException e1 = null, e2 = null;
			try {
				ret1 = iFallback.getDegreePlans(server, helper, student, matcher);
			} catch (SectioningException e) { e1 = e; }
			try {
				ret2 = _getDegreePlans(server, helper, student, matcher);
			} catch (SectioningException e) { e2 = e; }
			boolean has1 = (ret1 != null && !ret1.isEmpty());
			boolean has2 = (ret2 != null && !ret2.isEmpty());
			if (has2)
				for (DegreePlanInterface p: ret2)
					p.setName("EduNav: " + p.getName());
			if (has1 && !has2) return ret1;
			if (has2 && !has1) return ret2;
			if (!has1 && !has2) {
				if (e2 != null) throw e2;
				if (e1 != null) throw e1;
				return null;
			} else {
				for (DegreePlanInterface p: ret2)
					ret1.add(p);
				return ret1;
			}
		}
		try {
			List<DegreePlanInterface> ret = _getDegreePlans(server, helper, student, matcher);
			if (ret != null && !ret.isEmpty()) return ret;
		} catch (Exception e) {
			helper.warn("EduNav has failed: " + e.getMessage());
		}
		return iFallback.getDegreePlans(server, helper, student, matcher);
	}
	
	protected List<DegreePlanInterface> _getDegreePlans(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, CourseMatcher matcher) throws SectioningException {
		try {
			AcademicSessionInfo session = server.getAcademicSession();
			String bannerTerm = getBannerTerm(session);
			String studentId = getBannerId(student);
			if (helper.isDebugEnabled())
				helper.debug("Retrieving degree plans for " + student.getName() + " (term: " + bannerTerm + ", id:" + studentId + ")");
			helper.getAction().addOptionBuilder().setKey("term").setValue(bannerTerm);
			helper.getAction().addOptionBuilder().setKey("studentId").setValue(studentId);
			
			ClientResource resource = null;
			try {
				resource = new ClientResource(getEduNavApiSite());
				resource.setNext(iClient);
				resource.addQueryParameter("studentId", studentId);
				resource.addQueryParameter("includePlanContent", getEduNavIncludePlanContent());
				Series<Header> headers = (Series<Header>)resource.getRequestAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
				if (headers == null) {
					headers = new Series<>(Header.class);
					resource.getRequestAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headers);
				}
				headers.set(getEduNavApiAuthenticationKey(), getEduNavApiAuthenticationKeyValue());
				resource.accept(Encoding.GZIP, MediaType.APPLICATION_JSON);
				
				try {
					resource.get();
				} catch (ResourceException exception) {
					String message = null;
					try {
						message = toString(new DecodeRepresentation(resource.getResponseEntity()).getReader());
					} catch (Throwable t) {}
					throw new SectioningException(message != null && !message.isEmpty() ? message : exception.getMessage());
				}
				
				List<EduNavInterface.Result> results = new GsonRepresentation<List<EduNavInterface.Result>>(new DecodeRepresentation(resource.getResponseEntity()), EduNavInterface.Result.TYPE_LIST).getObject();
				
				Gson gson = getGson(helper);
				helper.getAction().addOptionBuilder().setKey("edunav-response").setValue(gson.toJson(results));
				if (helper.isDebugEnabled())
					helper.debug("Current degree plans: " + gson.toJson(results));
				
				List<DegreePlanInterface> ret = new ArrayList<DegreePlanInterface>();
				if (results != null)
					for (Result result: results) {
						if (result == null || result.plan == null || result.plan.terms == null) continue;
						DegreePlanInterface p = new DegreePlanInterface();
						p.setId(result.id);
						p.setName(result.name);
						if (p.getName() == null || p.getName().isEmpty())
							p.setName(getDegree(result.plan));
						if (p.getName() == null || p.getName().isEmpty())
							p.setName("Unnamed-" + (ret.size() + 1));
						p.setDegree(getDegreeShort(result.plan));
						p.setSchool(getSchool(result.plan));
						p.setLastModified(result.updateDate == null ? null : new Date(result.updateDate));
						p.setModifiedWho(result.owner);
						ret.add(p);
						for (Term term: result.plan.terms) {
							if (bannerTerm.equals(term.id)) {
								DegreePlanInterface.DegreeGroupInterface root = new DegreePlanInterface.DegreeGroupInterface();
								root.setChoice(false);
								root.setDescription(term.name);
								root.setId(term.id);
								p.setGroup(root);
								if (term.elements != null) {
									for (Element element: term.elements) {
										if (isCourse(element)) {
											Collection<? extends XCourseId> ids = server.findCourses(element.id, -1, matcher);
											DegreePlanInterface.DegreeCourseInterface course = new DegreePlanInterface.DegreeCourseInterface();
											course.setSubject(getSubject(element));
											course.setCourse(getCourseNbr(element));
											course.setTitle(element.name); // getTitle(element);
											course.setId(element.id);
											course.setCritical("mandatory".equals(element.type));
											if (ids != null) {
												for (XCourseId id: ids) {
													XCourse xc = (id instanceof XCourse ? (XCourse) id : server.getCourse(id.getCourseId()));
													if (xc == null) continue;
													if (!id.getCourseName().startsWith(element.id)) continue;
													CourseAssignment ca = new CourseAssignment();
													ca.setCourseId(xc.getCourseId());
													ca.setSubject(xc.getSubjectArea());
													ca.setParentCourseId(xc.getParentCourseId());
													ca.setCourseNbr(xc.getCourseNumber());
													ca.setTitle(xc.getTitle());
													ca.setNote(xc.getNote());
													/*
													if (ids.size() == 1) {
														String title = getTitle(element);
														if (title != null && !title.equals(element.name))
															ca.setNote(getTitle(element) + (ca.hasNote() ? "\n" + ca.getNote() : ""));
													}*/
													ca.setCreditAbbv(xc.getCreditAbbv());
													ca.setCreditText(xc.getCreditText());
													ca.setTitle(xc.getTitle());
													ca.setHasUniqueName(xc.hasUniqueName());
													ca.setLimit(xc.getLimit());
													if (server instanceof DatabaseServer) {
														InstructionalOffering io = InstructionalOfferingDAO.getInstance().get(id.getOfferingId(), helper.getHibSession());
														if (io != null) {
															ca.setEnrollment(io.getEnrollment());
															ca.setProjected(io.getDemand());
															ca.setCanWaitList(io.effectiveWaitList());
														}
													} else {
														int firstChoiceReqs = 0;
														int enrl = 0;
														Collection<XCourseRequest> requests = server.getRequests(id.getOfferingId());
														if (requests != null)
															for (XCourseRequest r: requests) {
																if (r.getEnrollment() != null && r.getEnrollment().getCourseId().equals(id.getCourseId())) enrl ++;
																if (!r.isAlternative() && r.getEnrollment() == null && r.getCourseIds().get(0).equals(id)) firstChoiceReqs ++;
															}
														ca.setEnrollment(enrl);
														ca.setProjected(firstChoiceReqs);
														XOffering io = server.getOffering(id.getOfferingId());
														ca.setCanWaitList(io != null && io.isWaitList());
													}
													course.addCourse(ca);
												}
											}
											if (course.hasCourses()) {
												for (CourseAssignment ca: course.getCourses())
													if (ca.getSubject().equals(course.getSubject()) && ca.getCourseNbr().equals(course.getCourse()))
														course.setCourseId(ca.getCourseId());
											}
											root.addCourse(course);	
										} else {
											DegreePlanInterface.DegreePlaceHolderInterface placeHolder = new DegreePlanInterface.DegreePlaceHolderInterface();
											placeHolder.setType(element.type);
											if (element.name != null && !element.name.isEmpty())
												placeHolder.setName(element.name);
											else
												placeHolder.setName(element.id);
											placeHolder.setId(element.id);
											if ("Additional Credits".equals(element.id) && element.hours != null && element.hours > 0)
												placeHolder.setName("Additional " + new DecimalFormat("0.#").format(element.hours) + " Credit(s)");
											root.addPlaceHolder(placeHolder);
										}
									}
								}
							}
						}
					}
				
				return ret;
			} catch (SectioningException e) {
				throw e;
			} catch (Exception e) {
				throw new SectioningException(e.getMessage(), e);
			} finally {
				if (resource != null) {
					if (resource.getResponse() != null) resource.getResponse().release();
					resource.release();
				}
			}
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			throw new SectioningException(e.getMessage(), e);
		}
	}

	@Override
	public void dispose() {
		try {
			iClient.stop();
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
		}
		if (iFallback != null) {
			iFallback.dispose();
			iFallback = null;
		}
	}
}
