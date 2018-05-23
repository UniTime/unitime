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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.restlet.Client;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CourseRequestsProvider;
import org.unitime.timetable.onlinesectioning.custom.DegreePlansProvider;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.purdue.XEInterface.PlaceHolder;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XStudent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Tomas Muller
 */
public class DegreeWorksCourseRequests implements CourseRequestsProvider, DegreePlansProvider {
	private static Logger sLog = Logger.getLogger(DegreeWorksCourseRequests.class);

	private Client iClient;
	private ExternalTermProvider iExternalTermProvider;
	
	public DegreeWorksCourseRequests() {
		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.HTTP);
		protocols.add(Protocol.HTTPS);
		iClient = new Client(protocols);
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
	}
	
	protected String getDegreeWorksApiSite() {
		return ApplicationProperties.getProperty("banner.dgw.site");
	}
	
	protected String getDegreeWorksApiUser() {
		return ApplicationProperties.getProperty("banner.dgw.user");
	}
	
	protected String getDegreeWorksApiPassword() {
		return ApplicationProperties.getProperty("banner.dgw.password");
	}
	
	protected String getDegreeWorksApiEffectiveOnly() {
		return ApplicationProperties.getProperty("banner.dgw.effectiveOnly", "false");
	}
	
	protected String getDegreeWorksErrorPattern() {
		return ApplicationProperties.getProperty("banner.dgw.errorPattern", "<div class=\"exceptionMessage\">\n(.*)\n\n</div>");
	}
	
	protected boolean getDegreeWorksActiveOnly() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.dgw.activeOnly", "true"));
	}
	
	protected String getDegreeWorksNoPlansMessage() {
		return ApplicationProperties.getProperty("banner.dgw.noPlansMessage", "No active degree plan is available.");
	}
	
	protected int getDegreeWorksNrAttempts() {
		return Integer.parseInt(ApplicationProperties.getProperty("banner.dgw.nrAttempts", "3"));
	}

	protected String getBannerId(XStudent student) {
		String id = student.getExternalId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}
	
	protected String getBannerTerm(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalTerm(session);
	}
	
	protected Gson getGson(OnlineSectioningHelper helper) {
		GsonBuilder builder = new GsonBuilder();
		if (helper.isDebugEnabled()) builder.setPrettyPrinting();
		return builder.create();
	}
	
	protected XCourseId getCourse(OnlineSectioningServer server, XEInterface.Course course) {
		Collection<? extends XCourseId> courses = server.findCourses(course.courseDiscipline + " " + course.courseNumber, -1, null);
		for (XCourseId c: courses)
			if (c.matchTitle(course.title)) return c;
		for (XCourseId c: courses)
			return c;
		return new XCourseId(null, null, course.courseDiscipline + " " + course.courseNumber);
	}
	
	protected OnlineSectioningLog.Entity toEntity(XEInterface.Course course, XCourseId courseId) {
		OnlineSectioningLog.Entity.Builder builder = OnlineSectioningLog.Entity.newBuilder();
		if (courseId.getCourseId() != null)
			builder.setUniqueId(courseId.getCourseId());
		builder.setName(courseId.getCourseName());
		builder.setExternalId(course.courseDiscipline + " " + course.courseNumber + (course.title != null && !course.title.isEmpty() ? " - " + course.title : ""));
		return builder.build();
	}
	
	protected boolean hasSelection(XEInterface.Group group) {
		if ("CH".equals(group.groupType.code)) {
			// choice group -- there is at least one course or group selected
			for (XEInterface.Course course: group.plannedClasses)
				if (course.isGroupSelection) return true;
			for (XEInterface.Group g: group.groups)
				if (hasSelection(g)) return true;
			return false;
		} else {
			return group.isGroupSelection;
		}
	}
	
	protected void fillInRequests(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request, XEInterface.Group group) {
		if ("CH".equals(group.groupType.code)) {
			// choice group -- pick (at least) one
			
			// try selected courses and groups first
			boolean hasSelection = false;
			for (XEInterface.Course course: group.plannedClasses) {
				if (course.isGroupSelection) {
					XCourseId cid = getCourse(server, course);
					if (cid == null) continue;
					
					helper.getAction().addRequestBuilder()
						.setPriority(request.getCourses().size())
						.setAlternative(false)
						.addCourse(toEntity(course, cid));
					
					CourseRequestInterface.Request r = new CourseRequestInterface.Request();
					RequestedCourse rc = new RequestedCourse();
					rc.setCourseId(cid.getCourseId());
					rc.setCourseName(cid.getCourseName());
					rc.setCourseTitle(cid.getTitle());
					if (cid instanceof XCourse) {
						rc.setCredit(((XCourse)cid).getMinCredit(), ((XCourse)cid).getMaxCredit());
					} else if (cid.getCourseId() != null) {
						XCourse c = server.getCourse(cid.getCourseId());
						if (c != null) rc.setCredit(c.getMinCredit(), c.getMaxCredit());
					}
					r.addRequestedCourse(rc);
					request.getCourses().add(r);
					hasSelection = true;
				}
			}
			for (XEInterface.Group g: group.groups) {
				if (hasSelection(g)) {
					fillInRequests(server, helper, request, g);
					hasSelection = true;
				}
			}
			
			if (!hasSelection) {
				// no selection -> use the first three courses as alternatives
				CourseRequestInterface.Request r = new CourseRequestInterface.Request();
				OnlineSectioningLog.Request.Builder b = OnlineSectioningLog.Request.newBuilder().setPriority(request.getCourses().size()).setAlternative(false);
				for (XEInterface.Course course: group.plannedClasses) {
					XCourseId cid = getCourse(server, course);
					if (cid == null) continue;
					RequestedCourse rc = new RequestedCourse();
					rc.setCourseId(cid.getCourseId());
					rc.setCourseName(cid.getCourseName());
					rc.setCourseTitle(cid.getTitle());
					if (cid instanceof XCourse) {
						rc.setCredit(((XCourse)cid).getMinCredit(), ((XCourse)cid).getMaxCredit());
					} else if (cid.getCourseId() != null) {
						XCourse c = server.getCourse(cid.getCourseId());
						if (c != null) rc.setCredit(c.getMinCredit(), c.getMaxCredit());
					}
					r.addRequestedCourse(rc);
					b.addCourse(toEntity(course, cid));
				}
				
				if (r.hasRequestedCourse()) {
					helper.getAction().addRequest(b);
					request.getCourses().add(r);
				}
			}
		} else {
			// union group -- take all courses (and sub-groups)
			for (XEInterface.Course course: group.plannedClasses) {
				XCourseId cid = getCourse(server, course);
				if (cid == null) continue;
				
				helper.getAction().addRequestBuilder()
					.setPriority(request.getCourses().size())
					.setAlternative(false)
					.addCourse(toEntity(course, cid));
				
				CourseRequestInterface.Request r = new CourseRequestInterface.Request();
				RequestedCourse rc = new RequestedCourse();
				rc.setCourseId(cid.getCourseId());
				rc.setCourseName(cid.getCourseName());
				rc.setCourseTitle(cid.getTitle());
				if (cid instanceof XCourse) {
					rc.setCredit(((XCourse)cid).getMinCredit(), ((XCourse)cid).getMaxCredit());
				} else if (cid.getCourseId() != null) {
					XCourse c = server.getCourse(cid.getCourseId());
					if (c != null) rc.setCredit(c.getMinCredit(), c.getMaxCredit());
				}
				r.addRequestedCourse(rc);
				request.getCourses().add(r);
			}
			for (XEInterface.Group g: group.groups)
				fillInRequests(server, helper, request, g);
		}
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
	
	@Override
	public CourseRequestInterface getCourseRequests(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) throws SectioningException {
		try {
			AcademicSessionInfo session = server.getAcademicSession();
			String term = getBannerTerm(session);
			String studentId = getBannerId(student);
			if (helper.isDebugEnabled())
				helper.debug("Retrieving student plan for " + student.getName() + " (term: " + term + ", id:" + studentId + ")");

			String effectiveOnly = getDegreeWorksApiEffectiveOnly();
			if (effectiveOnly != null)
				helper.getAction().addOptionBuilder().setKey("effectiveOnly").setValue(effectiveOnly);
			
			List<XEInterface.DegreePlan> current = null;
			long t0 = System.currentTimeMillis();
			try {
				current = getDegreePlans(term, studentId, effectiveOnly, getDegreeWorksNrAttempts());
			} catch (SectioningException e) {
				helper.getAction().setApiException(e.getMessage());
				throw e;
			} finally {
				helper.getAction().setApiGetTime(System.currentTimeMillis() - t0);
			}

			if (current != null && !current.isEmpty()) {
				Gson gson = getGson(helper);
				helper.getAction().addOptionBuilder().setKey("response").setValue(gson.toJson(current));
				for (XEInterface.DegreePlan plan: current) {
					// skip in-active plans
					if (plan.isActive == null || !plan.isActive.value) continue;
					if (plan.years != null) {
						for (XEInterface.Year y: plan.years) {
							if (y.terms != null) {
								for (XEInterface.Term t: y.terms) {
									if (t.term != null && term.equals(t.term.code) && t.group != null) {
										if (helper.isDebugEnabled())
											helper.debug("Current degree plan: " + gson.toJson(t.group));
										CourseRequestInterface request = new CourseRequestInterface();
										request.setAcademicSessionId(server.getAcademicSession().getUniqueId());
										request.setStudentId(student.getStudentId());
										fillInRequests(server, helper, request, t.group);
										if ("true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.dgw.includePlaceHolders", "true")))
											for (PlaceHolder ph: t.group.plannedPlaceholders) {
												CourseRequestInterface.Request r = new CourseRequestInterface.Request();
												RequestedCourse rc = new RequestedCourse();
												rc.setCourseName(ph.placeholderValue.trim());
												r.addRequestedCourse(rc);
												request.getCourses().add(r);
											}
										
										if (helper.isDebugEnabled())
											helper.debug("Course Requests: " + request);
										
										return request;
									}
								}
							}
						}
					}
				}
			}
			
			if (helper.isDebugEnabled()) helper.debug("No degree plan has been returned.");
			return null;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			throw new SectioningException(e.getMessage());
		}
	}
	

	@Override
	public void dispose() {
		try {
			iClient.stop();
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
		}
	}
	
	protected DegreePlanInterface.DegreeGroupInterface toGroup(OnlineSectioningServer server, XEInterface.Group g) {
		DegreePlanInterface.DegreeGroupInterface group = new DegreePlanInterface.DegreeGroupInterface();
		group.setChoice(g.groupType != null && "CH".equals(g.groupType.code));
		group.setDescription(g.summaryDescription);
		group.setId(g.id);
		if (g.plannedClasses != null)
			for (XEInterface.Course c: g.plannedClasses) {
				if (c.courseDiscipline == null || c.courseNumber == null) continue;
				DegreePlanInterface.DegreeCourseInterface course = new DegreePlanInterface.DegreeCourseInterface();
				if (group.isChoice())
					course.setSelected(c.isGroupSelection);
				course.setSubject(c.courseDiscipline);
				course.setCourse(c.courseNumber);
				course.setTitle(c.title);
				course.setId(c.id);
				Collection<? extends XCourseId> ids = server.findCourses(c.courseDiscipline + " " + c.courseNumber, -1, null);
				if (ids != null) {
					for (XCourseId id: ids) {
						XCourse xc = (id instanceof XCourse ? (XCourse) id : server.getCourse(id.getCourseId()));
						if (xc == null) continue;
						CourseAssignment ca = new CourseAssignment();
						ca.setCourseId(xc.getCourseId());
						ca.setSubject(xc.getSubjectArea());
						ca.setCourseNbr(xc.getCourseNumber());
						ca.setTitle(xc.getTitle());
						ca.setNote(xc.getNote());
						ca.setCreditAbbv(xc.getCreditAbbv());
						ca.setCreditText(xc.getCreditText());
						ca.setTitle(xc.getTitle());
						ca.setHasUniqueName(xc.hasUniqueName());
						ca.setLimit(xc.getLimit());
						XOffering offering = server.getOffering(id.getCourseId());
						if (offering != null)
							ca.setAvailability(offering.getCourseAvailability(server.getRequests(id.getOfferingId()), xc));
						course.addCourse(ca);
					}
				}
				if (course.hasCourses()) {
					for (CourseAssignment ca: course.getCourses())
						if (ca.getSubject().equals(course.getSubject()) && ca.getCourseNbr().equals(course.getCourse()))
							course.setCourseId(ca.getCourseId());
				}
				group.addCourse(course);
			}
		if (g.plannedPlaceholders != null)
			for (XEInterface.PlaceHolder ph: g.plannedPlaceholders) {
				DegreePlanInterface.DegreePlaceHolderInterface placeHolder = new DegreePlanInterface.DegreePlaceHolderInterface();
				placeHolder.setType(ph.placeholderType == null ? null : ph.placeholderType.description);
				placeHolder.setName(ph.placeholderValue);
				placeHolder.setId(ph.id);
				group.addPlaceHolder(placeHolder);
			}
		if (g.groups != null)
			for (XEInterface.Group ch: g.groups) {
				DegreePlanInterface.DegreeGroupInterface childGroup = toGroup(server, ch);
				if (childGroup.countItems() <= 1 || childGroup.isChoice() == group.isChoice()) {
					group.merge(childGroup);
				} else {
					if (group.isChoice())
						childGroup.setSelected(hasSelection(ch));
					group.addGroup(childGroup);
				}
			}
		return group;
	}
	
	protected List<XEInterface.DegreePlan> getDegreePlans(String term, String studentId, String effectiveOnly) throws SectioningException {
		ClientResource resource = null;
		try {
			resource = new ClientResource(getDegreeWorksApiSite());
			resource.setNext(iClient);
			resource.addQueryParameter("terms", term);
			resource.addQueryParameter("studentId", studentId);
			if (effectiveOnly != null)
				resource.addQueryParameter("effectiveOnly", effectiveOnly);
			resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, getDegreeWorksApiUser(), getDegreeWorksApiPassword());

			try {
				resource.get(MediaType.APPLICATION_JSON);
			} catch (ResourceException exception) {
				try {
					String response = toString(resource.getResponseEntity().getReader());
					Pattern pattern = Pattern.compile(getDegreeWorksErrorPattern(), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNIX_LINES);
					Matcher match = pattern.matcher(response);
					if (match.find())
						throw new SectioningException(match.group(1));
				} catch (SectioningException e) {
					throw e;
				} catch (Throwable t) {
					throw exception;
				}
				throw exception;
			}
			
			return new GsonRepresentation<List<XEInterface.DegreePlan>>(resource.getResponseEntity(), XEInterface.DegreePlan.TYPE_LIST).getObject();
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}
	
	protected List<XEInterface.DegreePlan> getDegreePlans(String term, String studentId, String effectiveOnly, int nrAttempts) throws SectioningException {
		SectioningException exception = null;
		if (nrAttempts > 1) {
			for (int i = 0; i < nrAttempts; i++) {
				try {
					return getDegreePlans(term, studentId, effectiveOnly);
				} catch (SectioningException e) {
					sLog.warn("Failed to retrieve degree plans for " + studentId + " [" + (1 + i) + ". attempt]: " + e.getMessage());
					exception = e;
				}
			}
			if (exception != null) throw exception;
			return null;
		} else {
			return getDegreePlans(term, studentId, effectiveOnly);
		}
	}

	@Override
	public List<DegreePlanInterface> getDegreePlans(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) throws SectioningException {
		try {
			AcademicSessionInfo session = server.getAcademicSession();
			String term = getBannerTerm(session);
			String studentId = getBannerId(student);
			if (helper.isDebugEnabled())
				helper.debug("Retrieving degree plans for " + student.getName() + " (term: " + term + ", id:" + studentId + ")");
			String effectiveOnly = getDegreeWorksApiEffectiveOnly();
			
			helper.getAction().addOptionBuilder().setKey("terms").setValue(term);
			helper.getAction().addOptionBuilder().setKey("studentId").setValue(studentId);
			if (effectiveOnly != null)
				helper.getAction().addOptionBuilder().setKey("effectiveOnly").setValue(effectiveOnly);

			List<XEInterface.DegreePlan> current = null;
			long t0 = System.currentTimeMillis();
			try {
				current = getDegreePlans(term, studentId, effectiveOnly, getDegreeWorksNrAttempts());
			} catch (SectioningException e) {
				helper.getAction().setApiException(e.getMessage());
				throw e;
			} finally {
				helper.getAction().setApiGetTime(System.currentTimeMillis() - t0);
			}
			if (current == null)
				throw new SectioningException(getDegreeWorksNoPlansMessage()).withTypeInfo();
			
			Gson gson = getGson(helper);
			helper.getAction().addOptionBuilder().setKey("response").setValue(gson.toJson(current));
			if (helper.isDebugEnabled())
				helper.debug("Current degree plans: " + gson.toJson(current));

			List<DegreePlanInterface> plans = new ArrayList<DegreePlanInterface>();
			for (XEInterface.DegreePlan p: current) {
				if (getDegreeWorksActiveOnly() && (p.isActive == null || !p.isActive.value)) continue;
				DegreePlanInterface plan = new DegreePlanInterface();
				plan.setSessionId(server.getAcademicSession().getUniqueId());
				plan.setStudentId(student.getStudentId());
				plan.setId(p.id);
				plan.setDegree(p.degree == null ? null : p.degree.description);
				plan.setName(p.description);
				plan.setSchool(p.school == null ? null : p.school.description);
				plan.setLastModified(p.modifyDate);
				plan.setModifiedWho(p.modifyWho == null ? null : p.modifyWho.name);
				plan.setTrackingStatus(p.officialTrackingStatus == null ? null : p.officialTrackingStatus.description);
				plan.setActive(p.isActive != null && p.isActive.value);
				plan.setLocked(p.isLocked != null && p.isLocked.value);
				if (p.years != null)
					for (XEInterface.Year y: p.years) {
						if (y.terms != null)
							for (XEInterface.Term t: y.terms) {
								if (t.term != null && term.equals(t.term.code) && t.group != null) {
									plan.setGroup(toGroup(server, t.group));
								}
							}
					}
								
				if (plan.getGroup() != null)
					plans.add(plan);
			}
			
			if (plans.isEmpty())
				throw new SectioningException(getDegreeWorksNoPlansMessage()).withTypeInfo();

			return plans;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			throw new SectioningException(e.getMessage());
		}
	}
}
