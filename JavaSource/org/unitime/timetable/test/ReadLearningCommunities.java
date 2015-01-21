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
package org.unitime.timetable.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
public class ReadLearningCommunities {
    protected static Logger sLog = Logger.getLogger(ReadLearningCommunities.class);
    
    private static String abbv(String name) {
        StringBuffer sb = new StringBuffer();
        for (StringTokenizer stk = new StringTokenizer(name," .");stk.hasMoreTokens();) {
            String word = stk.nextToken();
            if ("and".equalsIgnoreCase(word)) {
                sb.append("&");
            } else if ("in".equalsIgnoreCase(word)) {
            } else if (word.replaceAll("[a-zA-Z\\.]*", "").length()==0) {
                for (int i=0;i<word.length();i++) {
                    if (i==0)
                        sb.append(word.substring(i,i+1).toUpperCase());
                    else if ((i==1 && word.length()>3) || (word.charAt(i)>='A' && word.charAt(i)<='Z'))
                        sb.append(word.charAt(i));
                }
            } else {
                sb.append(word);
            }
        }
        return sb.toString();
    }

	public static void main(String[] args) {
        try {
            Properties props = new Properties();
            props.setProperty("log4j.rootLogger", "DEBUG, A1");
            props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
            props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
            props.setProperty("log4j.appender.A1.layout.ConversionPattern","%-5p %c{2}: %m%n");
            props.setProperty("log4j.logger.org.hibernate","INFO");
            props.setProperty("log4j.logger.org.hibernate.cfg","WARN");
            props.setProperty("log4j.logger.org.hibernate.cache.EhCacheProvider","ERROR");
            props.setProperty("log4j.logger.org.unitime.commons.hibernate","INFO");
            props.setProperty("log4j.logger.net","INFO");
            PropertyConfigurator.configure(props);
            
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

            org.hibernate.Session hibSession = new _RootDAO().getSession();
            
            Session session = Session.getSessionUsingInitiativeYearTerm(
                    ApplicationProperties.getProperty("initiative", "PWL"),
                    ApplicationProperties.getProperty("year","2011"),
                    ApplicationProperties.getProperty("term","Fall")
                    );
            
            if (session==null) {
                sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                System.exit(0);
            } else {
                sLog.info("Session: "+session);
            }
            
            DistributionType linkedSections = (DistributionType)hibSession.createQuery(
			"select d from DistributionType d where d.reference = :type").setString("type", "LINKED_SECTIONS").uniqueResult();

            BufferedReader r = new BufferedReader(new FileReader(ApplicationProperties.getProperty("file", "/Users/muller/Downloads/Fall 2011 LC Course Matrix.csv")));
            Document document = DocumentHelper.createDocument();
            document.addComment("Learning comunities for " + session);
            Element root = document.addElement("constraints");
            Element group = null;
            String line = null;
            DistributionPref dpLinkedSections = null;
            
            List<DistributionPref> distPrefs = new ArrayList<DistributionPref>();
            StudentGroupReservation reservation = null;
            List<StudentGroupReservation> reservations = new ArrayList<StudentGroupReservation>();
            StudentGroup studentGroup = null;
            List<StudentGroup> groups = new ArrayList<StudentGroup>();
            Map<SchedulingSubpart, Set<Class_>> classes = new HashMap<SchedulingSubpart, Set<Class_>>();
            
            while ((line = r.readLine()) != null) {
            	if (line.trim().isEmpty()) {
            		if (group != null) {
            			Set<Student> students = null;
            			for (Map.Entry<SchedulingSubpart, Set<Class_>> entry: classes.entrySet()) {
            				Set<Student> studentsThisSubpart = new HashSet<Student>();
        					for (Class_ c: entry.getValue())
        						for (StudentClassEnrollment e: c.getStudentEnrollments())
        							studentsThisSubpart.add(e.getStudent());
            				if (students == null) {
            					students = studentsThisSubpart;
            				} else {
            					for (Iterator<Student> i = students.iterator(); i.hasNext(); ) {
            						if (!studentsThisSubpart.contains(i.next())) i.remove();
            					}
            				}
            			} 
                		studentGroup.setStudents(students);
            		}
            		group = null; continue;
            	}
            	String[] cols = line.split(",");
            	if (cols.length <= 9) {
            		sLog.info("Skipping " + line);
            		continue;
            	}
            	if (group == null) {
            		group = root.addElement("linked-sections");
            		if (!cols[0].trim().isEmpty())
            			group.addAttribute("name", cols[0].trim());
                	dpLinkedSections = new DistributionPref();
                	dpLinkedSections.setDistributionType(linkedSections);
                	dpLinkedSections.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
                	dpLinkedSections.setGrouping(DistributionPref.sGroupingNone);
                	dpLinkedSections.setDistributionObjects(new HashSet<DistributionObject>());
                	distPrefs.add(dpLinkedSections);
                	studentGroup = new StudentGroup();
                	studentGroup.setGroupName(cols[0].trim());
                	studentGroup.setGroupAbbreviation(abbv(cols[0].trim()));
                	studentGroup.setSession(session);
                	// studentGroup.setStudents(new HashSet<Student>());
                	groups.add(studentGroup);
                	reservation = new StudentGroupReservation();
                	reservation.setClasses(new HashSet<Class_>());
                	reservation.setGroup(studentGroup);
                	reservations.add(reservation);
                	classes.clear();
            	}
            	String crn = cols[2].trim();
            	Class_ clazz = (Class_)hibSession.createQuery("from Class_ c where c.classSuffix like :crn and " +
            			"c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId")
            		.setString("crn", crn + "%")
            		.setLong("sessionId", session.getUniqueId())
            		.setMaxResults(1).uniqueResult();
            	if (clazz != null) {
            		group.addElement("section")
            			.addAttribute("offering", clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId().toString())
            			.addAttribute("id", clazz.getUniqueId().toString())
            			.addAttribute("name", clazz.getClassLabel());
            		
            		if (dpLinkedSections.getOwner() == null)
            			dpLinkedSections.setOwner(clazz.getManagingDept());
    	        	else if (!dpLinkedSections.getOwner().equals(clazz.getManagingDept()) && 
    	        			((Department)dpLinkedSections.getOwner()).getDistributionPrefPriority() < clazz.getManagingDept().getDistributionPrefPriority())
    	        		dpLinkedSections.setOwner(clazz.getManagingDept());
            		
            		/*
            		if (dpLinkedSections.getDistributionObjects().isEmpty()) {
            			for (StudentClassEnrollment enrl: clazz.getStudentEnrollments()) {
            				studentGroup.getStudents().add(enrl.getStudent());
            			}
            		} else {
            			students: for (Iterator<Student> i = studentGroup.getStudents().iterator(); i.hasNext(); ) {
            				Student student = i.next();
            				for (StudentClassEnrollment enrl: clazz.getStudentEnrollments()) {
            					if (student.equals(enrl.getStudent())) continue students;
            				}
            				i.remove();
            			}
            		}
            		*/
            		reservation.getClasses().add(clazz);
            		Set<Class_> c = classes.get(clazz.getSchedulingSubpart());
            		if (c == null) { c = new HashSet<Class_>(); classes.put(clazz.getSchedulingSubpart(), c); }
            		c.add(clazz);

    				DistributionObject o = new DistributionObject();
    				o.setDistributionPref(dpLinkedSections);
    				o.setPrefGroup(clazz);
    				o.setSequenceNumber(dpLinkedSections.getDistributionObjects().size());
    				dpLinkedSections.getDistributionObjects().add(o);
            	} else {
            		sLog.warn("Unable to find class " + crn + " (" + cols[3] + " " + cols[4] + " " + cols[7] + " " + cols[8] + " " + cols[9] + ")");
            	}
            }
            r.close();

            (new XMLWriter(System.out, OutputFormat.createPrettyPrint())).write(document);
            
            for (StudentGroup g: groups) {
            	sLog.info(g.getGroupAbbreviation() + ": " + g.getGroupName() + " has " + g.getStudents().size() + " students.");
            }
            
        	for (DistributionPref distPref: distPrefs) {
        		for (DistributionObject obj: new ArrayList<DistributionObject>(distPref.getDistributionObjects())) {
        			Class_ p = ((Class_)obj.getPrefGroup()).getParentClass();
    				while (p != null) {
    					for (DistributionObject x: distPref.getDistributionObjects()) {
    						if (x.getPrefGroup().equals(p)) {
    							distPref.getDistributionObjects().remove(x); break;
    						}
    					}
    					p = p.getParentClass();
    				}

        		}
        		if (distPref.getDistributionObjects().size() <= 1) continue;
        		int idx = 0;
        		for (DistributionObject obj: new TreeSet<DistributionObject>(distPref.getDistributionObjects()))
        			obj.setSequenceNumber(idx++);
        		hibSession.saveOrUpdate(distPref);
        	}
        	for (StudentGroup g: groups) {
        		// if (g.getStudents().isEmpty()) continue;
        		hibSession.saveOrUpdate(g);
        	}
        	
        	for (StudentGroupReservation res: reservations) {
        		// if (res.getGroup().getStudents().isEmpty()) continue;
        		Map<InstructionalOffering, StudentGroupReservation> of2res = new HashMap<InstructionalOffering, StudentGroupReservation>();
        		for (Class_ clazz: res.getClasses()) {
        			InstructionalOffering offering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering();
        			StudentGroupReservation x = of2res.get(offering);
        			if (x == null) {
        				x = new StudentGroupReservation();
        				x.setClasses(new HashSet<Class_>());
        				x.setGroup(res.getGroup());
        				x.setConfigurations(new HashSet<InstrOfferingConfig>());
        				x.setInstructionalOffering(offering);
        				x.setLimit(res.getGroup().getStudents().size());
        				of2res.put(offering, x);
        			}
        			x.getClasses().add(clazz);
        			// x.getConfigurations().add(clazz.getSchedulingSubpart().getInstrOfferingConfig());
        		}
        		for (StudentGroupReservation x: of2res.values()) {
        			for (Class_ c: new ArrayList<Class_>(x.getClasses())) {
        				Class_ p = c.getParentClass();
        				while (p != null) {
        					if (x.getClasses().contains(p)) x.getClasses().remove(p);
        					p = p.getParentClass();
        				}
        			}
        			hibSession.saveOrUpdate(x);
        		}
        	}
        	
        	hibSession.flush();
            
            sLog.info("All done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
