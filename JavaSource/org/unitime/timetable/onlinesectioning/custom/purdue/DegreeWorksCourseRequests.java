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
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CourseRequestsProvider;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.purdue.XEInterface.PlaceHolder;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XStudent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Tomas Muller
 */
public class DegreeWorksCourseRequests implements CourseRequestsProvider {
	private static Logger sLog = Logger.getLogger(DegreeWorksCourseRequests.class);
	
	private String iDegreeWorksApiUrl = ApplicationProperties.getProperty("banner.dgw.site");
	private String iDegreeWorksApiUser = ApplicationProperties.getProperty("banner.dgw.user");
	private String iDegreeWorksApiPassword = ApplicationProperties.getProperty("banner.dgw.password");
	private String iDegreeWorksApiEffectiveOnly = ApplicationProperties.getProperty("banner.dgw.effectiveOnly");

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
			
			resource = new ClientResource(iDegreeWorksApiUrl);
			resource.setNext(iClient);
			resource.addQueryParameter("terms", term);
			resource.addQueryParameter("studentId", getBannerId(student));
			helper.getAction().addOptionBuilder().setKey("terms").setValue(term);
			helper.getAction().addOptionBuilder().setKey("studentId").setValue(getBannerId(student));
			if (iDegreeWorksApiEffectiveOnly != null) {
				resource.addQueryParameter("effectiveOnly", iDegreeWorksApiEffectiveOnly);
				helper.getAction().addOptionBuilder().setKey("effectiveOnly").setValue(iDegreeWorksApiEffectiveOnly);
			}
			resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, iDegreeWorksApiUser, iDegreeWorksApiPassword);
			Gson gson = getGson(helper);
			
			try {
				resource.get(MediaType.APPLICATION_JSON);
			} catch (ResourceException exception) {
				try {
					XEInterface.ErrorResponse response = new GsonRepresentation<XEInterface.ErrorResponse>(resource.getResponseEntity(), XEInterface.ErrorResponse.class).getObject(); 
					helper.getAction().addOptionBuilder().setKey("exception").setValue(gson.toJson(response));
					XEInterface.Error error = response.getError();
					if (error != null && error.message != null) {
						throw new SectioningException(error.message);
					} else if (error != null && error.description != null) {
						throw new SectioningException(error.description);
					} else if (error != null && error.errorMessage != null) {
						throw new SectioningException(error.errorMessage);
					} else {
						throw exception;
					}
				} catch (SectioningException e) {
					throw e;
				} catch (Throwable t) {
					throw exception;
				}
			}
			
			List<XEInterface.DegreePlan> current = new GsonRepresentation<List<XEInterface.DegreePlan>>(resource.getResponseEntity(), XEInterface.DegreePlan.TYPE_LIST).getObject();
			XEInterface.DegreePlan plan = (current.isEmpty() ? null : current.get(0));
			
			if (plan != null) {
				helper.getAction().addOptionBuilder().setKey("response").setValue(gson.toJson(plan));
				if (helper.isDebugEnabled())
					helper.debug("Current degree plan: " + gson.toJson(plan));
			}
			
			if (plan == null || plan.years.isEmpty() || plan.years.get(0).terms.isEmpty() || plan.years.get(0).terms.get(0).group == null) {
				if (helper.isDebugEnabled()) helper.debug("No degree plan has been returned.");
				return null;
			}

			CourseRequestInterface request = new CourseRequestInterface();
			request.setAcademicSessionId(server.getAcademicSession().getUniqueId());
			request.setStudentId(student.getStudentId());
			fillInRequests(server, helper, request, plan.years.get(0).terms.get(0).group);
			if ("true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.dgw.includePlaceHolders", "true")))
				for (PlaceHolder ph: plan.years.get(0).terms.get(0).group.plannedPlaceholders) {
					CourseRequestInterface.Request r = new CourseRequestInterface.Request();
					r.setRequestedCourse(ph.placeholderValue.trim());
					request.getCourses().add(r);
				}
			
			if (helper.isDebugEnabled())
				helper.debug("Course Requests: " + request);
			
			return request;
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
}
