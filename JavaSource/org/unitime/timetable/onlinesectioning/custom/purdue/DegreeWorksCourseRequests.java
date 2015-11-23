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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
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
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
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
					r.setRequestedCourse(cid.getCourseName());
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
					if (!r.hasRequestedCourse()) {
						r.setRequestedCourse(cid.getCourseName());
					} else if (!r.hasFirstAlternative()) {
						r.setFirstAlternative(cid.getCourseName());
					} else if (!r.hasSecondAlternative()) {
						r.setSecondAlternative(cid.getCourseName());
						break;
					}
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
				r.setRequestedCourse(cid.getCourseName());
				request.getCourses().add(r);
			}
			for (XEInterface.Group g: group.groups)
				fillInRequests(server, helper, request, g);
		}
	}
	
	@Override
	public CourseRequestInterface getCourseRequests(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) throws SectioningException {
		ClientResource resource = null;
		try {
			AcademicSessionInfo session = server.getAcademicSession();
			String term = getBannerTerm(session);
			if (helper.isDebugEnabled())
				helper.debug("Retrieving student plan for " + student.getName() + " (term: " + term + ", id:" + getBannerId(student) + ")");
			
			resource = new ClientResource(getDegreeWorksApiSite());
			resource.setNext(iClient);
			resource.addQueryParameter("terms", term);
			resource.addQueryParameter("studentId", getBannerId(student));
			helper.getAction().addOptionBuilder().setKey("terms").setValue(term);
			helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
			String effectiveOnly = getDegreeWorksApiEffectiveOnly();
			if (effectiveOnly != null) {
				resource.addQueryParameter("effectiveOnly", effectiveOnly);
				helper.getAction().addOptionBuilder().setKey("effectiveOnly").setValue(effectiveOnly);
			}
			resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, getDegreeWorksApiUser(), getDegreeWorksApiPassword());
			Gson gson = getGson(helper);
			
			try {
				resource.get(MediaType.APPLICATION_JSON);
			} catch (ResourceException exception) {
				try {
					String response = IOUtils.toString(resource.getResponseEntity().getReader());
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
			
			List<XEInterface.DegreePlan> current = new GsonRepresentation<List<XEInterface.DegreePlan>>(resource.getResponseEntity(), XEInterface.DegreePlan.TYPE_LIST).getObject();
			
			if (current != null && !current.isEmpty()) {
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
												r.setRequestedCourse(ph.placeholderValue.trim());
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
			helper.info("Failed to retrieve degree plan: " + e.getMessage());
			throw e;
		} catch (Exception e) {
			helper.warn("Failed to retrieve degree plan: " + e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
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
						Collection<XCourseRequest> requests = server.getRequests(id.getOfferingId());
						if (requests != null) {
							int enrl = 0;
							for (XCourseRequest r: requests)
								if (r.getEnrollment() != null && r.getEnrollment().getCourseId().equals(id.getCourseId()))
									enrl ++;
							ca.setEnrollment(enrl);
						}
						course.addCourse(ca);
					}
				}
				if (course.hasCourses()) {
					for (CourseAssignment ca: course.getCourses()) {
						if (ca.getSubject().equals(course.getSubject()) && ca.getCourseNbr().equals(course.getCourse())) {
							course.setCourseId(ca.getCourseId());
							course.setName(ca.getCourseName());
						}
					}
					if (course.getCourseId() == null) {
						course.setCourseId(course.getCourses().get(0).getCourseId());
						course.setName(course.getCourses().get(0).getCourseName());
					}
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

	@Override
	public List<DegreePlanInterface> getDegreePlans(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) throws SectioningException {
		ClientResource resource = null;
		try {
			AcademicSessionInfo session = server.getAcademicSession();
			String term = getBannerTerm(session);
			if (helper.isDebugEnabled())
				helper.debug("Retrieving degree plans for " + student.getName() + " (term: " + term + ", id:" + getBannerId(student) + ")");
			
			resource = new ClientResource(getDegreeWorksApiSite());
			resource.setNext(iClient);
			resource.addQueryParameter("terms", term);
			resource.addQueryParameter("studentId", getBannerId(student));
			helper.getAction().addOptionBuilder().setKey("terms").setValue(term);
			helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
			String effectiveOnly = getDegreeWorksApiEffectiveOnly();
			if (effectiveOnly != null) {
				resource.addQueryParameter("effectiveOnly", effectiveOnly);
				helper.getAction().addOptionBuilder().setKey("effectiveOnly").setValue(effectiveOnly);
			}
			resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, getDegreeWorksApiUser(), getDegreeWorksApiPassword());
			Gson gson = getGson(helper);
			
			try {
				resource.get(MediaType.APPLICATION_JSON);
			} catch (ResourceException exception) {
				try {
					String response = IOUtils.toString(resource.getResponseEntity().getReader());
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
			
			List<XEInterface.DegreePlan> current = new GsonRepresentation<List<XEInterface.DegreePlan>>(resource.getResponseEntity(), XEInterface.DegreePlan.TYPE_LIST).getObject();
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
			
			return plans;
		} catch (SectioningException e) {
			helper.info("Failed to retrieve degree plans: " + e.getMessage());
			throw e;
		} catch (Exception e) {
			helper.warn("Failed to retrieve degree plans: " + e.getMessage(), e);
			throw new SectioningException(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}
}
