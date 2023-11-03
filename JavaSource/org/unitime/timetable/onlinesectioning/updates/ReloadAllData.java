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
package org.unitime.timetable.onlinesectioning.updates;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.IgnoreStudentConflictsConstraint;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface.HasClassNamesCache;
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface.HasGradableSubpart;
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface.HasGradableSubpartCache;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.WaitList;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XDistributionType;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XDistribution;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSchedulingRules;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.solver.TimetableDatabaseLoader;

/**
 * @author Tomas Muller
 */
public class ReloadAllData implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	@Override
	public Boolean execute(final OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (ApplicationProperty.OnlineSchedulingGradableIType.isTrue() && Class_.getExternalClassNameHelper() != null) {
			if (Class_.getExternalClassNameHelper() instanceof HasGradableSubpartCache) {
				helper.setGradableSubpartsProvider(((HasGradableSubpartCache)Class_.getExternalClassNameHelper()).getGradableSubparts(server.getAcademicSession().getUniqueId(), helper.getHibSession()));
			} else if (Class_.getExternalClassNameHelper() instanceof HasGradableSubpart) {
				helper.setGradableSubpartsProvider((HasGradableSubpart)Class_.getExternalClassNameHelper());
			}
		}
		if (Class_.getExternalClassNameHelper() != null) {
			if (Class_.getExternalClassNameHelper() instanceof HasClassNamesCache)
				helper.setExternalClassNameHelper(((HasClassNamesCache)Class_.getExternalClassNameHelper()).getClassNamesCache(server.getAcademicSession().getUniqueId(), helper.getHibSession()));
			else
				helper.setExternalClassNameHelper(Class_.getExternalClassNameHelper());
		}

		Lock lock = server.lockAll();
		try {
			helper.beginTransaction();
			try {
				helper.info("Updating course infos and the student sectining model for session " + server.getAcademicSession());
				long t0 = System.currentTimeMillis();
				server.clearAll();
				server.setSchedulingRules(new XSchedulingRules(server.getAcademicSession(), helper.getHibSession()));

				Map<Long, List<XDistribution>> distributions = new Hashtable<Long, List<XDistribution>>();
		    	List<DistributionPref> distPrefs = helper.getHibSession().createQuery(
		        		"select p from DistributionPref p, Department d where p.distributionType.reference in (:ref1, :ref2) and d.session.uniqueId = :sessionId" +
		        		" and p.owner = d and p.prefLevel.prefProlog = :pref", DistributionPref.class)
		        		.setParameter("ref1", GroupConstraint.ConstraintType.LINKED_SECTIONS.reference())
		        		.setParameter("ref2", IgnoreStudentConflictsConstraint.REFERENCE)
		        		.setParameter("pref", PreferenceLevel.sRequired)
		        		.setParameter("sessionId", server.getAcademicSession().getUniqueId())
		        		.list();
		        if (!distPrefs.isEmpty()) {
		        	for (DistributionPref pref: distPrefs) {
		        		int variant = 0;
		        		for (Collection<Class_> sections: getSections(pref)) {
		        			XDistributionType type = XDistributionType.IngoreConflicts;
		        			if (GroupConstraint.ConstraintType.LINKED_SECTIONS.reference().equals(pref.getDistributionType().getReference()))
		        				type = XDistributionType.LinkedSections;
		        			XDistribution distribution = new XDistribution(type, pref.getUniqueId(), variant++, sections);
		        			for (Long offeringId: distribution.getOfferingIds()) {
		        				List<XDistribution> list = distributions.get(offeringId);
		        				if (list == null) {
		        					list = new ArrayList<XDistribution>();
		        					distributions.put(offeringId, list);
		        				}
		        				list.add(distribution);
		        			}
		        				
		        		}
		        	}
		        }
		        
				Map<Long, XOffering> offeringMap = new HashMap<Long, XOffering>();
				Map<Long, XSection> sectionMap = new HashMap<Long, XSection>();
				Map<Long, Map<Long, Double>> spaceMap = new HashMap<Long, Map<Long,Double>>();
				List<InstructionalOffering> offerings = helper.getHibSession().createQuery(
						"select distinct io from InstructionalOffering io " +
						"left join io.courseOfferings co " +
						"left join fetch io.instrOfferingConfigs cf " +
						"left join fetch cf.schedulingSubparts ss " +
						"left join fetch ss.classes as c "+
						"left join fetch io.reservations x " +
						"where io.session.uniqueId = :sessionId and io.notOffered = false and co.subjectArea.department.allowStudentScheduling = true",
						InstructionalOffering.class)
						.setParameter("sessionId", server.getAcademicSession().getUniqueId()).list();
				for (InstructionalOffering io: offerings) {
					XOffering offering = loadOffering(io, distributions.get(io.getUniqueId()), server, helper);
					if (offering != null) {
						offeringMap.put(offering.getOfferingId(), offering);
						for (XConfig config: offering.getConfigs())
							for (XSubpart subpart: config.getSubparts())
								for (XSection section: subpart.getSections())
									sectionMap.put(section.getSectionId(), section);
						spaceMap.put(offering.getOfferingId(), new HashMap<Long, Double>());
						server.update(offering);
					}
				}
				
		        Map<Long, List<XCourseRequest>> requestMap = new HashMap<Long, List<XCourseRequest>>();
				List<org.unitime.timetable.model.Student> students = helper.getHibSession().createQuery(
	                    "select distinct s from Student s " +
	                    "left join fetch s.courseDemands as cd " +
	                    "left join fetch cd.courseRequests as cr " +
	                    "left join fetch cr.classWaitLists as cwl " + 
	                    "left join fetch s.classEnrollments as e " +
	                    "left join fetch s.areaClasfMajors as acm " +
	                    "left join fetch s.waitlists as w " +
	                    "left join fetch s.groups as g " +
	                    "left join fetch s.notes as n " +
	                    "where s.session.uniqueId=:sessionId", org.unitime.timetable.model.Student.class).
	                    setParameter("sessionId", server.getAcademicSession().getUniqueId()).list();
	            for (org.unitime.timetable.model.Student student: students) {
	            	XStudent s = loadStudent(student, requestMap, server, helper, WaitList.WaitListType.RELOAD);
	            	if (s != null)
	            		server.update(s, true);
	            }
				
		    	List<Object[]> infos = helper.getHibSession().createQuery(
		    			"select i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.uniqueId, i.clazz.uniqueId, i.nbrExpectedStudents from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId",
		    			Object[].class)
		    			.setParameter("sessionId", server.getAcademicSession().getUniqueId())
		    			.list();
		    	for (Object[] info : infos) {
		    		Long offeringId = (Long)info[0];
		    		Long sectionId = (Long)info[1];
		    		Double expected = (Double)info[2];
		    		Map<Long, Double> space = spaceMap.get(offeringId);
		    		if (space != null)
		    			space.put(sectionId, expected);
		    	}
		    	for (Map.Entry<Long, Map<Long, Double>> entry: spaceMap.entrySet())
		    		server.update(new XExpectations(entry.getKey(), entry.getValue()));
		        
				long t1 = System.currentTimeMillis();
				helper.info("  Update of session " + server.getAcademicSession() + " done " + new DecimalFormat("0.0").format((t1 - t0) / 1000.0) + " seconds.");
				
				helper.commitTransaction();
				return true;
			} catch (Exception e) {
				helper.rollbackTransaction();
				if (e instanceof SectioningException)
					throw (SectioningException)e;
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			}
		} finally {
			lock.release();
		}		
	}
	
    public static XOffering loadOffering(InstructionalOffering io, List<XDistribution> distributions, OnlineSectioningServer server, OnlineSectioningHelper helper) {
    	if (io.getInstrOfferingConfigs().isEmpty() || io.isNotOffered()) return null;
    	return new XOffering(io, distributions, helper);
    }
    
    public static XStudent loadStudent(org.unitime.timetable.model.Student s, Map<Long, List<XCourseRequest>> requestMap, OnlineSectioningServer server, OnlineSectioningHelper helper) {
    	return loadStudent(s, requestMap, server, helper, null); 
    }
    
    public static XStudent loadStudent(org.unitime.timetable.model.Student s, Map<Long, List<XCourseRequest>> requestMap, OnlineSectioningServer server, OnlineSectioningHelper helper, WaitList.WaitListType resetWaitListType) {
    	XStudent student = new XStudent(s, helper, server.getAcademicSession().getFreeTimePattern(), server.getAcademicSession().getDatePatternFirstDate());
    	
    	for (Iterator<XRequest> i = student.getRequests().iterator(); i.hasNext(); ) {
    		XRequest request = i.next();
    		if (request instanceof XCourseRequest) {
				Map<XSection, XEnrollment> checked = new HashMap<XSection, XEnrollment>();
    			XCourseRequest courseRequest = (XCourseRequest)request;
    			for (Iterator<XCourseId> j = courseRequest.getCourseIds().iterator(); j.hasNext(); ) {
    				XCourseId course = j.next();
    				XOffering offering = server.getOffering(course.getOfferingId());
                    if (offering == null) {
                    	helper.warn("Student " + helper.getStudentNameFormat().format(s) + " (" + s.getExternalUniqueId() + ") requests course " + course.getCourseName() + " that is not loaded.");
                    	j.remove();
                    }
    			}
    			if (courseRequest.getCourseIds().isEmpty()) {
    				i.remove();
    				continue;
    			}
    			XEnrollment enrollment = courseRequest.getEnrollment();
    			if (enrollment != null) {
    				XOffering offering = server.getOffering(enrollment.getOfferingId());
    				
    				if (!offering.getReservations().isEmpty()) {
        				if (requestMap != null) {
        					List<XCourseRequest> assigned = requestMap.get(enrollment.getOfferingId());
        					if (assigned == null) {
        						assigned = new ArrayList<XCourseRequest>();
        						requestMap.put(enrollment.getOfferingId(), assigned);
            				}
            				enrollment.setReservation(offering.guessReservation(assigned, student, enrollment));
        					assigned.add(courseRequest);
        				} else {
        					enrollment.setReservation(offering.guessReservation(server.getRequests(enrollment.getOfferingId()), student, enrollment));
        				}
    				}
    				
    				boolean mixedConfig = false;
    				for (XConfig config: offering.getConfigs()) {
    					for (XSubpart subpart: config.getSubparts()) {
							int nrMatches = 0;
    						for (XSection section: subpart.getSections()) {
    							if (enrollment.getSectionIds().contains(section.getSectionId())) {
    		    					for (XSection other: checked.keySet()) {
    		    						if (section.isOverlapping(offering.getDistributions(), other)) {
    		    							helper.warn("There is a problem assigning " + enrollment.getCourseName() + " to " + helper.getStudentNameFormat().format(s) + " (" + s.getExternalUniqueId() + "): "+
    		    									section.getSubpartName() + " " + section.getName() + " " + section.getTime() +
    		            							" overlaps with " + checked.get(other).getCourseName() + " " + other.getSubpartName() + " " + other.getName() + " " + other.getTime());
    		    						}
    		    					}
    		    					if (!mixedConfig && !config.getConfigId().equals(enrollment.getConfigId())) {
    		    						helper.warn("There is a problem assigning " + enrollment.getCourseName() + " to " + helper.getStudentNameFormat().format(s) + " (" + s.getExternalUniqueId() + "): classes from different configurations.");
    		    						mixedConfig = true;
    		    					}
    		    					checked.put(section, enrollment);
    		    					nrMatches ++;
    							}
    						}
							if (config.getConfigId().equals(enrollment.getConfigId()) && nrMatches != 1) {
								if (nrMatches > 1)
									helper.warn("There is a problem assigning " + enrollment.getCourseName() + " to " + helper.getStudentNameFormat().format(s) + " (" + s.getExternalUniqueId() + "): two or more classes of the same subpart " + subpart.getName() + ".");
								else
									helper.warn("There is a problem assigning " + enrollment.getCourseName() + " to " + helper.getStudentNameFormat().format(s) + " (" + s.getExternalUniqueId() + "): no class of the subpart " + subpart.getName() + ".");
							}
    					}
    				}
    			}
    		}
    	}
    	
    	if (resetWaitListType != null && s.getWaitListMode() == WaitListMode.WaitList)
    		s.resetWaitLists(resetWaitListType, helper.getUser().getExternalId(), null, helper.getHibSession());
    	
        return student;
    }
    
    public static XStudent loadStudentNoCheck(org.unitime.timetable.model.Student s, OnlineSectioningServer server, OnlineSectioningHelper helper) {
    	XStudent student = new XStudent(s, helper, server.getAcademicSession().getFreeTimePattern(), server.getAcademicSession().getDatePatternFirstDate());
    	
    	for (Iterator<XRequest> i = student.getRequests().iterator(); i.hasNext(); ) {
    		XRequest request = i.next();
    		if (request instanceof XCourseRequest) {
    			XCourseRequest courseRequest = (XCourseRequest)request;
    			for (Iterator<XCourseId> j = courseRequest.getCourseIds().iterator(); j.hasNext(); ) {
    				XCourseId course = j.next();
    				XOffering offering = server.getOffering(course.getOfferingId());
                    if (offering == null) {
                    	helper.warn("Student " + helper.getStudentNameFormat().format(s) + " (" + s.getExternalUniqueId() + ") requests course " + course.getCourseName() + " that is not loaded.");
                    	j.remove();
                    }
    			}
    			if (courseRequest.getCourseIds().isEmpty()) {
    				i.remove();
    				continue;
    			}
    			XEnrollment enrollment = courseRequest.getEnrollment();
    			if (enrollment != null) {
    				XOffering offering = server.getOffering(enrollment.getOfferingId());
    				if (offering != null && !offering.getReservations().isEmpty())
    					enrollment.setReservation(offering.guessReservation(server.getRequests(enrollment.getOfferingId()), student, enrollment));
    			}
    		}
    	}
    	
        return student;
    }

	public static List<Collection<Class_>> getSections(DistributionPref pref) {
		List<Collection<Class_>> ret = new ArrayList<Collection<Class_>>();
		DistributionPref.Structure structure = pref.getStructure();
		if (structure == null) structure = DistributionPref.Structure.AllClasses;
    	if (structure == DistributionPref.Structure.Progressive) {
    		int maxSize = 0;
    		for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
        		DistributionObject distributionObject = (DistributionObject)i.next();
        		if (distributionObject.getPrefGroup() instanceof Class_)
        			maxSize = Math.max(maxSize, 1);
        		else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart)
        			maxSize = Math.max(maxSize, ((SchedulingSubpart)distributionObject.getPrefGroup()).getClasses().size());
    		}
    		Set<Class_> sections[] = new Set[maxSize];
    		for (int i=0;i<sections.length;i++)
    			sections[i] = new HashSet<Class_>();

    		List<DistributionObject> distributionObjects = new ArrayList<DistributionObject>(pref.getDistributionObjects());
    		Collections.sort(distributionObjects, new TimetableDatabaseLoader.ChildrenFirstDistributionObjectComparator());
    		for (DistributionObject distributionObject: distributionObjects) {
        		if (distributionObject.getPrefGroup() instanceof Class_) {
        			Class_ section = (Class_)distributionObject.getPrefGroup();
        			if (section!=null)
        				for (int j = 0; j < sections.length; j++)
        					sections[j].add(section);
        		} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        	    	List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	for (int j = 0; j < sections.length; j++) {
        	    		Class_ section = null;
        	    		sections: for (Class_ s: sections[j]) {
        	    			Class_ p = s.getParentClass();
        	    			while (p != null) {
        	    				if (p.getSchedulingSubpart().getUniqueId().equals(subpart.getUniqueId())) {
        	    					section = p;
        	    					break sections;
        	    				}
        	    				p  = p.getParentClass();
        	    			}
        	    		}
        	    		if (section == null)
        	    			section = classes.get(j%classes.size());
        	    		if (section!=null)
        	    			sections[j].add(section);
        	    	}
        		}
    		}
    		for (Set<Class_> s: sections)
    			ret.add(s);
    	} else if (structure == DistributionPref.Structure.OneOfEach) {
    		List<Class_> sections = new ArrayList<Class_>();
    		List<Integer> counts = new ArrayList<Integer>();
        	for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
        		DistributionObject distributionObject = (DistributionObject)i.next();
    			int count = 0;
    			if (distributionObject.getPrefGroup() instanceof Class_) {
        			Class_ clazz = (Class_)distributionObject.getPrefGroup();
        			sections.add(clazz); count++;
    			} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        			List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        	    	Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        			sections.addAll(classes); count += classes.size();
        		}
    			if (count > 0) counts.add(count);
        	}
        	if (counts.size() > 1) {
        		for (Enumeration<List<Class_>> e = DistributionPref.permutations(sections, counts); e.hasMoreElements(); )
        			ret.add(e.nextElement());
        	}
    	} else {
    		List<Class_> sections = new ArrayList<Class_>();
        	for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
        		DistributionObject distributionObject = (DistributionObject)i.next();
        		if (distributionObject.getPrefGroup() instanceof Class_) {
        			sections.add((Class_)distributionObject.getPrefGroup());
        		} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        	    	List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        	    	Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	sections.addAll(classes);
        		}
        	}
        	if (structure == DistributionPref.Structure.Pairwise) {
	        	if (sections.size() >= 2) {
	        		for (int idx1 = 0; idx1 < sections.size() - 1; idx1++) {
	        			Class_ s1 = sections.get(idx1);
	        			for (int idx2 = idx1 + 1; idx2 < sections.size(); idx2++) {
	        				Class_ s2 = sections.get(idx2);
	        				Set<Class_> s = new HashSet<Class_>();
	        				s.add(s1); s.add(s2);
	        				ret.add(s);
	        			}
	        		}
	        	}
			} else if (structure == DistributionPref.Structure.AllClasses) {
				ret.add(sections);
			} else {
		    	int grouping = 2;
		    	switch (structure) {
		    	case GroupsOfTwo: grouping = 2; break;
		    	case GroupsOfThree: grouping = 3; break;
		    	case GroupsOfFour: grouping = 4; break;
		    	case GroupsOfFive: grouping = 5; break;
		    	}
				List<Class_> s = new ArrayList<Class_>();
				for (Class_ section: sections) {
					s.add(section);
					if (s.size() == grouping) {
						ret.add(s); s = new ArrayList<Class_>();
					}
				}
				if (s.size() >= 2)
					ret.add(new HashSet<Class_>(s));
			}
	    }
		return ret;
	}
	
	public static interface SectionProvider{
		public XSection get(Class_ clazz);
	}
    
	@Override
    public String name() { return "reload-all"; }
	
}
