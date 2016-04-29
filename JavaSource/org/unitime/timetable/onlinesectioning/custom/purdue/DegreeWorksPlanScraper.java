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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.Client;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLogger;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningTestFwk;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.model.XAcademicAreaCode;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author Tomas Muller
 */
public class DegreeWorksPlanScraper extends OnlineSectioningTestFwk {
	
	private Client iClient;
	private ExternalTermProvider iExternalTermProvider;
	private Set<String> iFilter;
	
	public DegreeWorksPlanScraper(Set<String> filter) {
		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.HTTP);
		protocols.add(Protocol.HTTPS);
		iClient = new Client(protocols);
		iFilter = filter;
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
	
	@Override
	protected void startServer() {
		final Session session = Session.getSessionUsingInitiativeYearTerm(
                ApplicationProperties.getProperty("initiative", "PWL"),
                ApplicationProperties.getProperty("year","2015"),
                ApplicationProperties.getProperty("term","Fall")
                );

        if (session==null) {
            sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
            System.exit(0);
        } else {
            sLog.info("Session: "+session);
        }
        
        iSessionId = session.getUniqueId();
        
        OnlineSectioningLogger.getInstance().setEnabled(false);

        iServer = new DatabaseServer(new AcademicSessionInfo(session), false);
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
	
	protected String getBannerId(XStudent student) {
		String id = student.getExternalId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}
	
	public String getBannerTerm(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalTerm(session);
	}
	
	protected Gson getGson() {
		return new GsonBuilder()
				.registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
	    			@Override
	    			public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
	    				return new JsonPrimitive(src.toString("yyyy-MM-dd'T'HH:mm:ss'Z'"));
	    			}
	    		})
	    		.registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
	    			@Override
	    			public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
	    				return new DateTime(json.getAsJsonPrimitive().getAsString(), DateTimeZone.UTC);
	    			}
	    		})
	        	.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
	    			@Override
	    			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
	    				return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(src));
	    			}
	    		})
	    		.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
	    			@Override
	    			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
	    				try {
	    					return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(json.getAsJsonPrimitive().getAsString());
	    				} catch (ParseException e) {
	    					throw new JsonParseException(e.getMessage(), e);
	    				}
	    			}
	    		})
	    		.setPrettyPrinting().create();
	}
	
	public String getDegreePlans(OnlineSectioningServer server, XStudent student) throws SectioningException {
		ClientResource resource = null;
		try {
			resource = new ClientResource(getDegreeWorksApiSite());
			resource.setNext(iClient);
			if ("true".equalsIgnoreCase(ApplicationProperties.getProperty("setTerms", "false")))
				resource.addQueryParameter("terms", getBannerTerm(server.getAcademicSession()));
			resource.addQueryParameter("studentId", getBannerId(student));
			String effectiveOnly = getDegreeWorksApiEffectiveOnly();
			if (effectiveOnly != null)
				resource.addQueryParameter("effectiveOnly", effectiveOnly);

			resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, getDegreeWorksApiUser(), getDegreeWorksApiPassword());
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
			
			return IOUtils.toString(resource.getResponseEntity().getReader());
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
	
	public List<Operation> operations() {

		org.hibernate.Session hibSession = new _RootDAO().getSession();
		
		List<Operation> operations = new ArrayList<Operation>();
		
		OnlineSectioningHelper helper = new OnlineSectioningHelper(user(), null);
		
		List<org.unitime.timetable.model.Student> students = hibSession.createQuery(
                "select distinct s from Student s " +
                "left join fetch s.courseDemands as cd " +
                "left join fetch cd.courseRequests as cr " +
                "left join fetch cr.classWaitLists as cwl " + 
                "left join fetch s.classEnrollments as e " +
                "left join fetch s.academicAreaClassifications as a " +
                "left join fetch s.posMajors as mj " +
                "left join fetch s.waitlists as w " +
                "left join fetch s.groups as g " +
                "where s.session.uniqueId=:sessionId").
                setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list();
		
		for (final Student s: students) {
			if (iFilter != null && !iFilter.contains(s.getExternalUniqueId())) continue;
			final XStudent student = new XStudent(s, helper, getServer().getAcademicSession().getFreeTimePattern());
			
			if (student != null)
				operations.add(new Operation() {
					@Override
					public double execute(OnlineSectioningServer s) {
						try {
							String plans = null;
							long t0 = System.currentTimeMillis();
							try {
								plans = getDegreePlans(s, student);
								inc("Request Succeeded [s]", (System.currentTimeMillis() - t0) / 1000.0);
								if (plans == null || plans.isEmpty() || "[]".equals(plans)) {
									inc("Request Succeeded with no plans", (System.currentTimeMillis() - t0) / 1000.0);
									inc("students-noplans", student.getExternalId(), "Time [s]", (System.currentTimeMillis() - t0) / 1000.0);
								} else {
									inc("Request Succeeded with plans", (System.currentTimeMillis() - t0) / 1000.0);
									inc("students-withplans", student.getExternalId(), "Time [s]", (System.currentTimeMillis() - t0) / 1000.0);
								}
							} catch (Exception e) {
								inc("Request Failed [s]", (System.currentTimeMillis() - t0) / 1000.0);
								inc("Request Failed with  " + e.getMessage() + " [s]", (System.currentTimeMillis() - t0) / 1000.0);
								inc("students-failed", student.getExternalId(), e.getMessage(), 1);
							} finally {
								inc("Request Time [s]", (System.currentTimeMillis() - t0) / 1000.0);
							}
							
							int ret = 0;
							if (plans != null && !plans.isEmpty() && !"[]".equals(plans)) {
								try {
									Writer out = new FileWriter(new File(new File("plans"), getBannerId(student) + ".json"));
									out.write(plans);
									out.flush(); out.close();
								} catch (IOException e) {}
								
								inc("Request Succeeded [response length in chars]", plans.length());
								List<XEInterface.DegreePlan> current = getGson().fromJson(plans, XEInterface.DegreePlan.TYPE_LIST);

								int nrActive = 0;
								int nrLocked = 0;
								int nrMatching = 0;
								for (XEInterface.DegreePlan p: current) {
									if (p.isActive.value) nrActive ++;
									if (p.isLocked.value) nrLocked ++;
									inc("plans-by-degree", p.school.code, p.degree.code, 1);
									inc("plans-by-stattus",p.officialTrackingStatus.code, "All", 1);
									if (p.isActive.value)
										inc("plans-by-stattus",p.officialTrackingStatus.code, "Active", 1);
									if (p.isLocked.value)
										inc("plans-by-stattus",p.officialTrackingStatus.code, "Locked", 1);
									String term = getBannerTerm(s.getAcademicSession());
									for (XEInterface.Year y: p.years) {
										for (XEInterface.Term t: y.terms) {
											if (term.equals(t.term.code)) 
												nrMatching ++;
										}
									}
								}
								inc("Request Succeeded [All]", current.size());
								if (nrActive > 0)
									inc("Request Succeeded [Active]", nrActive);
								if (nrLocked > 0)
									inc("Request Succeeded [Locked]", nrLocked);
								if (nrMatching > 0)
									inc("Request Succeeded [Matching Term]", nrMatching);
								ret = nrActive;
								
								for (XAcademicAreaCode aac: student.getAcademicAreaClasiffications())
									for (XAcademicAreaCode m: student.getMajors())
										if (m.getArea().equals(aac.getArea())) {
											inc("students-curricula", aac.getArea() + "/" + m.getCode() + " " + aac.getCode(), "Students", 1);
											inc("students-curricula", aac.getArea() + "/" + m.getCode() + " " + aac.getCode(), "Plans", current.size());
											inc("students-curricula", aac.getArea() + "/" + m.getCode() + " " + aac.getCode(), "Active", nrActive);
											inc("students-curricula", aac.getArea() + "/" + m.getCode() + " " + aac.getCode(), "Locked", nrLocked);
											inc("students-curricula", aac.getArea() + "/" + m.getCode() + " " + aac.getCode(), "Matching", nrMatching);
										}
								
								inc("students-withplans", student.getExternalId(), "Total", current.size());
								inc("students-withplans", student.getExternalId(), "Active", nrActive);
								inc("students-withplans", student.getExternalId(), "Locked", nrLocked);
								inc("students-withplans", student.getExternalId(), "Matching", nrMatching);
							}
							return ret;							
						} catch (Throwable t) {
							inc("Uncaught: " + t.getMessage(), 1);
							return 0;
						}
					}
				});			
		}
		
		hibSession.close();

		return operations;
	}
	
	public static void main(String[] args) throws Exception {
		new File("plans").mkdir();
		String[] nc = System.getProperty("nrConcurrent", "10").split(",");
		int[] nrConcurrent = new int[nc.length];
		for (int i = 0; i < nc.length; i++)
			nrConcurrent[i] = Integer.valueOf(nc[i].trim());
		Set<String> filter = null;
		if (System.getProperty("studentFilter") != null) {
			filter = new HashSet<String>();
			BufferedReader br = new BufferedReader(new FileReader(System.getProperty("studentFilter")));
			String line = null;
			while ((line = br.readLine()) != null) {
				filter.add(line.trim());
			}
			br.close();
		}
		new DegreeWorksPlanScraper(filter).test(Integer.valueOf(System.getProperty("nrTasks", "-1")), nrConcurrent);
	}

}