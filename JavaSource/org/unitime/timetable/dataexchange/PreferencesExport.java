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
package org.unitime.timetable.dataexchange;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.InstructorPref;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.solver.CommitedClassAssignmentProxy;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;


/**
 * @author Tomas Muller
 */
public class PreferencesExport extends BaseExport{
	public CommitedClassAssignmentProxy proxy = new CommitedClassAssignmentProxy();
	protected static Formats.Format<Date> sDateFormat = Formats.getDateFormat("yyyy/M/d");
    protected static Formats.Format<Date> sTimeFormat = Formats.getDateFormat("HHmm");
    protected static Formats.Format<Number> sNumberFormat = Formats.getNumberFormat("0000");
	
	public Comparator ioCmp = null;
	public Comparator subpartCmp = null;
	public Comparator classCmp = null;
	

	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("preferences");
			root.addAttribute("term", session.getAcademicTerm());
			root.addAttribute("year", session.getAcademicYear());
			root.addAttribute("campus", session.getAcademicInitiative());
			root.addAttribute("dateFormat", sDateFormat.toPattern());
	        root.addAttribute("timeFormat", sTimeFormat.toPattern());
	        root.addAttribute("created", new Date().toString());
	        
	        for (Department department: (List<Department>)getHibSession().createQuery(
	        		"select distinct d from Department d left join fetch d.preferences p where d.session.uniqueId = :sessionId")
	        		.setLong("sessionId", session.getUniqueId()).list()) {
				exportPrefGroup(root, department);
			}
	        for (DepartmentalInstructor instructor: (List<DepartmentalInstructor>)getHibSession().createQuery(
	        		"select distinct i from DepartmentalInstructor i left join fetch i.preferences p where i.department.session.uniqueId = :sessionId " +
	        		"order by i.department.deptCode, i.lastName, i.firstName").setLong("sessionId", session.getUniqueId()).list()) {
				exportPrefGroup(root, instructor);
			}
	        for (SchedulingSubpart subpart: (List<SchedulingSubpart>)getHibSession().createQuery(
	        		"select distinct ss from SchedulingSubpart ss " +
	        		"left join fetch ss.instrOfferingConfig as ioc " +
	        		"left join fetch ioc.instructionalOffering as io " +
	        		"left join fetch io.courseOfferings as co " +
	        		"left join fetch ss.classes c " +
	        		"left join fetch ss.preferences sp " +
	        		"left join fetch c.preferences cp " +
	        		"where ss.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId and co.isControl = true " +
	        		"order by co.subjectAreaAbbv, co.courseNbr, ioc.uniqueId, ss.uniqueId"
	        		).setLong("sessionId", session.getUniqueId()).list()) {
				exportPrefGroup(root, subpart);
				for (Class_ clazz: subpart.getClasses())
					exportPrefGroup(root, clazz);
	        }
			
			commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
	}
	
	protected void exportPrefGroup(Element parent, PreferenceGroup group) {
		Element el = null;
		if (group instanceof Class_) {
			Class_ clazz = (Class_)group;
			CourseOffering course = clazz.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering();
			el = parent.addElement("class");
			el.addAttribute("subject", course.getSubjectAreaAbbv());
			el.addAttribute("course", course.getCourseNbr());
			el.addAttribute("type", clazz.getSchedulingSubpart().getItypeDesc().trim());
			el.addAttribute("suffix", getClassSuffix(clazz));
			String extId = clazz.getExternalId(course);
			if (extId != null && !extId.isEmpty())
				el.addAttribute("externalId", extId);
			if (clazz.getDatePattern() != null)
				el.addAttribute("pattern", clazz.getDatePattern().getName());
		} else if (group instanceof SchedulingSubpart) {
			SchedulingSubpart subpart = (SchedulingSubpart)group;
			CourseOffering course = subpart.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
			el = parent.addElement("subpart");
			el.addAttribute("subject", course.getSubjectAreaAbbv());
			el.addAttribute("course", course.getCourseNbr());
			if (subpart.getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs().size() > 1)
				el.addAttribute("config", subpart.getInstrOfferingConfig().getName());
			el.addAttribute("type", subpart.getItypeDesc().trim());
			if (!subpart.getSchedulingSubpartSuffix().isEmpty())
				el.addAttribute("suffix", subpart.getSchedulingSubpartSuffix());
			if (subpart.getDatePattern() != null)
				el.addAttribute("pattern", subpart.getDatePattern().getName());
		} else if (group instanceof Department) {
			Department department = (Department)group;
			el = parent.addElement("department");
			el.addAttribute("code", department.getDeptCode());
		} else if (group instanceof DepartmentalInstructor) {
			DepartmentalInstructor instructor = (DepartmentalInstructor)group;
			el = parent.addElement("instructor");
			if (instructor.getExternalUniqueId() != null && !instructor.getExternalUniqueId().isEmpty())
				el.addAttribute("externalId", instructor.getExternalUniqueId());
			if (instructor.getFirstName() != null)
				el.addAttribute("firstName", instructor.getFirstName());
			if (instructor.getMiddleName() != null)
				el.addAttribute("middleName", instructor.getMiddleName());
			if (instructor.getLastName() != null)
				el.addAttribute("lastName", instructor.getLastName());
			if (instructor.getEmail() != null)
				el.addAttribute("email", instructor.getEmail());
			if (instructor.getAcademicTitle() != null)
				el.addAttribute("title", instructor.getAcademicTitle());
			el.addAttribute("department", instructor.getDepartment().getDeptCode());
			if (instructor.getMaxLoad() != null && instructor.getTeachingPreference() != null)
				el.addElement("teachingPref").addAttribute("maxLoad", instructor.getMaxLoad().toString())
				.addAttribute("level", instructor.getTeachingPreference().getPrefProlog());
		}
		if (el != null) {
			for (Preference preference: group.getPreferences())
				exportPreference(el, preference);
		}
	}
	
	protected String slot2time(int slot) {
		int min = slot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
		return sNumberFormat.format((min / 60) * 100 + (min % 60));
	}
	
	protected Element exportPreference(Element parent, Preference preference) {
		Element el = null;
		if (preference instanceof RoomPref) {
			Location location = ((RoomPref)preference).getRoom();
			el = parent.addElement("roomPref");
			if (location instanceof Room) {
				el.addAttribute("building", ((Room)location).getBuildingAbbv());
				el.addAttribute("room", ((Room)location).getRoomNumber());
			} else {
				el.addAttribute("location", location.getLabel());
			}
		} else if (preference instanceof RoomGroupPref) {
			RoomGroup group = ((RoomGroupPref)preference).getRoomGroup();
			el = parent.addElement("groupPref");
			el.addAttribute("group", group.getAbbv());
			if (group.getDepartment() != null)
				el.addAttribute("department", group.getDepartment().getDeptCode());
		} else if (preference instanceof RoomFeaturePref) {
			RoomFeature feature = ((RoomFeaturePref)preference).getRoomFeature();
			el = parent.addElement("featurePref");
			el.addAttribute("feature", feature.getAbbv());
			if (feature instanceof DepartmentRoomFeature)
				el.addAttribute("department", ((DepartmentRoomFeature)feature).getDepartment().getDeptCode());
		} else if (preference instanceof BuildingPref) {
			Building building = ((BuildingPref)preference).getBuilding();
			el = parent.addElement("buildingPref");
			el.addAttribute("building", building.getAbbreviation());
		} else if (preference instanceof TimePref) {
			el = parent.addElement("timePref");
			TimePattern pattern = ((TimePref)preference).getTimePattern();
			if (pattern != null)
				el.addAttribute("pattern", pattern.getName());
			TimePatternModel model = ((TimePref)preference).getTimePatternModel();
			if (model.isExactTime()) 
				el.addElement("pref").addAttribute("days", DayCode.toString(model.getExactDays())).addAttribute("time", slot2time(model.getExactStartSlot())).addAttribute("level", PreferenceLevel.sRequired);
			else if (model.getTimePattern() == null) {
				for (int d = 0; d < model.getNrDays(); d++) {
					int start = -1; String pref = null;
					for (int t = 0; t < model.getNrTimes(); t++) {
						String p = model.getPreference(d, t);
						if (pref == null || !pref.equals(p)) {
							if (pref != null && !pref.equals(PreferenceLevel.sNeutral)) {
								int end = model.getStartSlot(t - 1);
								Element pe = el.addElement("pref").addAttribute("level", pref).addAttribute("day", DayCode.toString(model.getDayCode(d))).addAttribute("start", slot2time(start));
								if (start < end)
									pe.addAttribute("stop", slot2time(1 + end));
							}
							start = model.getStartSlot(t); pref = p;
						}
					}
					if (pref != null && !pref.equals(PreferenceLevel.sNeutral)) {
						int end = model.getStartSlot(model.getNrTimes() - 1);
						Element pe = el.addElement("pref").addAttribute("level", pref).addAttribute("day", DayCode.toString(model.getDayCode(d))).addAttribute("start", slot2time(start));
						if (start < end)
							pe.addAttribute("stop", slot2time(1 + end));
					}
				}
			} else {
				for (int d = 0; d < model.getNrDays(); d++) {
					for (int t = 0; t < model.getNrTimes(); t++) {
						String pref = model.getPreference(d, t);
						if (pref != null &&!pref.equals(PreferenceLevel.sNeutral)) {
							el.addElement("pref").addAttribute("level", pref).addAttribute("days", DayCode.toString(model.getDayCode(d))).addAttribute("time", slot2time(model.getStartSlot(t)));
						}
					}
				}
			}
		} else if (preference instanceof DatePatternPref) {
			el = parent.addElement("datePref");
			el.addAttribute("pattern", ((DatePatternPref)preference).getDatePattern().getName());
		} else if (preference instanceof InstructorPref) {
			el = parent.addElement("instructorPref");
			DepartmentalInstructor instructor = ((InstructorPref)preference).getInstructor();
			el.addAttribute("instructor", instructor.getExternalUniqueId() != null && !instructor.getExternalUniqueId().isEmpty() ? instructor.getExternalUniqueId() : instructor.getName("first-middle-last"));
			el.addAttribute("department", instructor.getDepartment().getDeptCode());
		} else if (preference instanceof InstructorCoursePref) {
			el = parent.addElement("coursePref");
			CourseOffering course = ((InstructorCoursePref)preference).getCourse();
			el.addAttribute("subject", course.getSubjectAreaAbbv()).addAttribute("course", course.getCourseNbr());
		} else if (preference instanceof InstructorAttributePref) {
			el = parent.addElement("attributePref");
			el.addAttribute("attribute", ((InstructorAttributePref)preference).getAttribute().getCode());
		} else if (preference instanceof ExamPeriodPref) {
			el = parent.addElement("periodPref");
			ExamPeriod period = ((ExamPeriodPref)preference).getExamPeriod();
			el.addAttribute("date", sDateFormat.format(period.getStartDate()));
			el.addAttribute("start", sTimeFormat.format(period.getStartTime()));
			el.addAttribute("type", period.getExamType().getReference());
		} else if (preference instanceof DistributionPref) {
			el = parent.addElement("distributionPref");
			DistributionPref dp = (DistributionPref)preference;
			el.addAttribute("type", dp.getDistributionType().getReference());
			if (dp.getStructure() != null)
				el.addAttribute("structure", dp.getStructure().name());
			for (DistributionObject distObj: new TreeSet<DistributionObject>(dp.getDistributionObjects())) {
				if (distObj.getPrefGroup() instanceof Class_) {
					Class_ clazz = (Class_)distObj.getPrefGroup();
					CourseOffering course = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
					Element clEl = el.addElement("class");
					clEl.addAttribute("subject", course.getSubjectAreaAbbv());
					clEl.addAttribute("course", course.getCourseNbr());
					clEl.addAttribute("type", clazz.getSchedulingSubpart().getItypeDesc().trim());
					clEl.addAttribute("suffix", getClassSuffix(clazz));
					String extId = clazz.getExternalId(course);
					if (extId != null && !extId.isEmpty())
						clEl.addAttribute("externalId", extId);
				} else if (distObj.getPrefGroup() instanceof SchedulingSubpart) {
					SchedulingSubpart subpart = (SchedulingSubpart)distObj.getPrefGroup();
					CourseOffering course = subpart.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
					Element clEl = el.addElement("subpart");
					clEl.addAttribute("subject", course.getSubjectAreaAbbv());
					clEl.addAttribute("course", course.getCourseNbr());
					if (subpart.getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs().size() > 1)
						clEl.addAttribute("config", subpart.getInstrOfferingConfig().getName());
					clEl.addAttribute("type", subpart.getItypeDesc().trim());
					if (!subpart.getSchedulingSubpartSuffix().isEmpty())
						clEl.addAttribute("suffix", subpart.getSchedulingSubpartSuffix());
				}
			}
		}
		if (el == null) return null;
		el.addAttribute("level", preference.getPrefLevel().getPrefProlog());
		return el;
	}
}
