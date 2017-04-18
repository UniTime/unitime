package org.unitime.timetable.reports.pointintimedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.PointInTimeDataReports;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.CourseCreditFormat;
import org.unitime.timetable.model.CourseCreditType;
import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.DemandOfferingType;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserContext;

public abstract class BasePointInTimeDataReports {
	
	private static interface ParameterImplementation {
		public Map<Long, String> getValues(UserContext user);
		public String getDefaultValue(UserContext user);
		public ArrayList<Object> parseSetValue(String valueString);
	}

	//TODO: extend for unknown teaching responsibility
	
	private static class RefTableParameters implements ParameterImplementation {
		protected Class<? extends RefTableEntry> iReference;
		RefTableParameters(Class<? extends RefTableEntry> reference) { iReference = reference; }
		@SuppressWarnings("unchecked")
		public Map<Long, String> getValues(UserContext user) {
			Map<Long, String> ret = new Hashtable<Long, String>();
			for (RefTableEntry ref: (List<RefTableEntry>)SessionDAO.getInstance().getSession().createCriteria(iReference).list())
				ret.put(ref.getUniqueId(), ref.getLabel());
			return ret;
		}
		@Override
		public String getDefaultValue(UserContext user) {
			return null;
		}
		@Override
		public ArrayList<Object> parseSetValue(String valueString) {
				ArrayList<Object> parameterValues = new ArrayList<Object>();
			for (String value : valueString.split(",")){
				parameterValues.add(Long.parseLong(value));
			}
			return(parameterValues);
		}
	}

	private static class RefTableParametersPlusUnknown extends RefTableParameters {
		RefTableParametersPlusUnknown(Class<? extends RefTableEntry> reference) { super(reference); }

		@Override
		public Map<Long, String> getValues(UserContext user) {
			Map<Long, String> map =  super.getValues(user);
			map.put(new Long(-1), MSG.labelUnknown());
			
			return(map);
		}
		
	}
	
	public static enum Parameter {
		PITD("Point In Time Data", true, false, false, new ParameterImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Long sessionId = user.getCurrentAcademicSessionId();
				Session session = (sessionId == null ? null : SessionDAO.getInstance().get(sessionId));
				if (session == null) return null;
				List<PointInTimeData> pitdList = PointInTimeData.findAllSavedSuccessfullyForSession(sessionId); 
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (PointInTimeData pitd : pitdList){
					ret.put(pitd.getUniqueId(), pitd.getName());
				}
				return(ret);
			}
			@Override
			public String getDefaultValue(UserContext user) {
				return(null);
			}
			@Override
			public ArrayList<Object> parseSetValue(String valueString) {
				ArrayList<Object> parameterValues = new ArrayList<Object>();
				for (String value : valueString.split(",")){
					parameterValues.add(Long.parseLong(value));
				}
				return(parameterValues);
			}
		}),
		PITD2("Point In Time Data Comparison", true, false, false, PITD.iImplementation),
		SESSION("Academic Session", false, false, false, new ParameterImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Long sessionId = user.getCurrentAcademicSessionId();
				Session session = (sessionId == null ? null : SessionDAO.getInstance().get(sessionId));
				if (session == null) return null;
				Map<Long, String> ret = new Hashtable<Long, String>();
				ret.put(session.getUniqueId(), session.getLabel());
				return ret;
			}
			@Override
			public String getDefaultValue(UserContext user) {
				Long sessionId = user.getCurrentAcademicSessionId();
				Session session = (sessionId == null ? null : SessionDAO.getInstance().get(sessionId));
				if (session == null) return null;
				return(sessionId.toString());
			}
			@Override
			public ArrayList<Object> parseSetValue(String valueString) {
				ArrayList<Object> parameterValues = new ArrayList<Object>();
				for (String value : valueString.split(",")){
					parameterValues.add(Long.parseLong(value));
				}
				return(parameterValues);
			}
		}),
		DEPARTMENT("Department", true, false, false, new ParameterImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Long sessionId = user.getCurrentAcademicSessionId();
				Session session = (sessionId == null ? null : SessionDAO.getInstance().get(sessionId));
				if (session == null) return null;
				TimetableManager manager = TimetableManager.findByExternalId(user.getExternalUserId());
				if (manager == null) return null;
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (Department d: Department.getUserDepartments(user))
					ret.put(d.getUniqueId(), d.htmlLabel());
				return ret;
			}
			@Override
			public String getDefaultValue(UserContext user) {
				return null;
			}
			@Override
			public ArrayList<Object> parseSetValue(String valueString) {
				ArrayList<Object> parameterValues = new ArrayList<Object>();
				for (String value : valueString.split(",")){
					parameterValues.add(Long.parseLong(value));
				}
				return(parameterValues);
			}
		}),
		DEPARTMENTS("Departments", true, true, false, DEPARTMENT.iImplementation),
		SUBJECT("Subject Area", true, false, false, new ParameterImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Map<Long, String> ret = new Hashtable<Long, String>();
				try {
					for (SubjectArea s: SubjectArea.getUserSubjectAreas(user)) {
						ret.put(s.getUniqueId(), s.getSubjectAreaAbbreviation());
					}
				} catch (Exception e) { return null; }
				return ret;
			}
			@Override
			public String getDefaultValue(UserContext user) {
				return null;
			}
			@Override
			public ArrayList<Object> parseSetValue(String valueString) {
				ArrayList<Object> parameterValues = new ArrayList<Object>();
				for (String value : valueString.split(",")){
					parameterValues.add(Long.parseLong(value));
				}
				return(parameterValues);
			}
		}),
		SUBJECTS("Subject Areas", true, true, false, SUBJECT.iImplementation),
		BUILDING("Building", true, false, false, new ParameterImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Long sessionId = user.getCurrentAcademicSessionId();
				Session session = (sessionId == null ? null : SessionDAO.getInstance().get(sessionId));
				TimetableManager manager = TimetableManager.findByExternalId(user.getExternalUserId());
				if (manager == null) return null;
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (Building b: (List<Building>)Building.findAll(session.getUniqueId()))
					ret.put(b.getUniqueId(), b.getAbbrName());
				return ret;
			}
			@Override
			public String getDefaultValue(UserContext user) {
				return null;
			}
			@Override
			public ArrayList<Object> parseSetValue(String valueString) {
				ArrayList<Object> parameterValues = new ArrayList<Object>();
				for (String value : valueString.split(",")){
					parameterValues.add(Long.parseLong(value));
				}
				return(parameterValues);
			}
		}),
		BUILDINGS("Buildings", true, true, false, BUILDING.iImplementation),
		ROOM("Room", true, false, false, new ParameterImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				Long sessionId = user.getCurrentAcademicSessionId();
				Session session = (sessionId == null ? null : SessionDAO.getInstance().get(sessionId));
				TimetableManager manager = TimetableManager.findByExternalId(user.getExternalUserId());
				if (manager == null) return null;
				Map<Long, String> ret = new Hashtable<Long, String>();
				for (Room r: (List<Room>)Room.findAllRooms(session.getUniqueId())){
					ret.put(r.getUniqueId(), r.getLabel());
				}
				return ret;
			}
			@Override
			public String getDefaultValue(UserContext user) {
				return null;
			}
			@Override
			public ArrayList<Object> parseSetValue(String valueString) {
				ArrayList<Object> parameterValues = new ArrayList<Object>();
				for (String value : valueString.split(",")){
					parameterValues.add(Long.parseLong(value));
				}
				return(parameterValues);
			}
		}),
		ROOMS("Rooms", true, true, false, ROOM.iImplementation),
		MINUTES_IN_REPORTING_HOUR("Minutes in Reporting Hour", true, false, true, new ParameterImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				return null;
			}
			@Override
			public String getDefaultValue(UserContext user) {
				return(ApplicationProperty.StandardMinutesInReportingHour.value().toString());
			}
			@Override
			public ArrayList<Object> parseSetValue(String valueString) {
				ArrayList<Object> parameterValues = new ArrayList<Object>();
				for (String value : valueString.split(",")){
					parameterValues.add(Float.parseFloat(value));
				}
				return(parameterValues);
			}
		}),
		WEEKS_IN_REPORTING_TERM("Weeks in Reporting Term", true, false, true, new ParameterImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				return null;
			}
			@Override
			public String getDefaultValue(UserContext user) {
				return(ApplicationProperty.StandardWeeksInReportingTerm.value().toString());
			}
			@Override
			public ArrayList<Object> parseSetValue(String valueString) {
				ArrayList<Object> parameterValues = new ArrayList<Object>();
				for (String value : valueString.split(",")){
					parameterValues.add(Float.parseFloat(value));
				}
				return(parameterValues);
			}
		}),
		MINIMUM_LOCATION_CAPACITY("Minimum Location Capacity", true, false, true, new ParameterImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				return null;
			}
			@Override
			public String getDefaultValue(UserContext user) {
				return("0");
			}
			@Override
			public ArrayList<Object> parseSetValue(String valueString) {
				ArrayList<Object> parameterValues = new ArrayList<Object>();
				for (String value : valueString.split(",")){
					parameterValues.add(Integer.parseInt(value));
				}
				return(parameterValues);
			}
		}),
		MAXIMUM_LOCATION_CAPACITY("Maximum Location Capacity", true, false, true, new ParameterImplementation() {
			@Override
			public Map<Long, String> getValues(UserContext user) {
				return null;
			}
			@Override
			public String getDefaultValue(UserContext user) {
				return("999999");
			}
			@Override
			public ArrayList<Object> parseSetValue(String valueString) {
				ArrayList<Object> parameterValues = new ArrayList<Object>();
				for (String value : valueString.split(",")){
					parameterValues.add(Integer.parseInt(value));
				}
				return(parameterValues);
			}
		}),
		
		DistributionType(DistributionType.class, false, false),
		DistributionTypes(DistributionType.class, true, false),
		DemandOfferingType(DemandOfferingType.class, false, false),
		DemandOfferingTypes(DemandOfferingType.class, true, false),
		OfferingConsentType(OfferingConsentType.class, false, false),
		OfferingConsentTypes(OfferingConsentType.class, true, false),
		CourseCreditFormat(CourseCreditFormat.class, false, false),
		CourseCreditFormats(CourseCreditFormat.class, true, false),
		CourseCreditType(CourseCreditType.class, false, false),
		CourseCreditTypes(CourseCreditType.class, true, false),
		CourseCreditUnitType(CourseCreditUnitType.class, false, false),
		CourseCreditUnitTypes(CourseCreditUnitType.class, true, false),
		PositionType(PositionType.class, false, true),
		PositionTypes(PositionType.class, true, true),
		DepartmentStatusType(DepartmentStatusType.class, false, false),
		DepartmentStatusTypes(DepartmentStatusType.class, true, false),
		RoomType(RoomType.class, false, false),
		RoomTypes(RoomType.class, true, false),
		StudentSectioningStatus(StudentSectioningStatus.class, false, false),
		StudentSectioningStatuses(StudentSectioningStatus.class, true, false),
		ExamType(ExamType.class, false, false),
		ExamTypes(ExamType.class, true, false),
		RoomFeatureType(RoomFeatureType.class, false, false),
		RoomFeatureTypes(RoomFeatureType.class, true, false),
		CourseType(CourseType.class, false, false),
		CourseTypes(CourseType.class, true, false),
		;
		
		String iName;
		ParameterImplementation iImplementation;
		boolean iAllowSelection, iMultiSelect, iTextField;
		Parameter(String name, boolean allowSelection, boolean multiSelect, boolean isText, ParameterImplementation impl) {
			iName = name;
			iAllowSelection = allowSelection; iMultiSelect = multiSelect; iTextField = isText;
			iImplementation = impl;
		}
		Parameter(Class<? extends RefTableEntry> reference, boolean multiSelect, boolean addUnknownValue) {
			iName = name().replaceAll("(?<=[^A-Z])([A-Z])"," $1");
			iAllowSelection = true; iMultiSelect = multiSelect; iTextField = false;
			if (addUnknownValue) {
				iImplementation = new RefTableParametersPlusUnknown(reference);				
			} else {
				iImplementation = new RefTableParameters(reference);
			}
		}
		
		public String text() { return iName; }
		public boolean allowSingleSelection() { return iAllowSelection; }
		public boolean allowMultiSelection() { return iAllowSelection && iMultiSelect; }
		public boolean isTextField() { return iTextField; }
		public Map<Long, String> values(UserContext user) { return iImplementation.getValues(user); }
		public String defaultValue(UserContext user) { return iImplementation.getDefaultValue(user); }
		public ArrayList<Object> parseSetValue(String valueString) { return(iImplementation.parseSetValue(valueString)) ; }
	}

	protected static PointInTimeDataReports MSG = Localization.create(PointInTimeDataReports.class);
	private Float standardMinutesInReportingHour = null;
	private Float standardWeeksInReportingTerm = null;
	private Long pointInTimeDataUniqueId = null;
	
    public static Hashtable<String,Class> sPointInTimeDataReportRegister;
    static {
        sPointInTimeDataReportRegister = new Hashtable<String, Class>();
        sPointInTimeDataReportRegister.put("allWSCHbyDept", AllWSCHByDepartment.class);
        sPointInTimeDataReportRegister.put("allWSCHforDeptbyClass", AllWSCHForDepartmentByClass.class);
        sPointInTimeDataReportRegister.put("allWSCHforDeptbyPosition", AllWSCHForDepartmentByInstructorPosition.class);
        sPointInTimeDataReportRegister.put("allWSCHforDeptbyInstructor", AllWSCHForDepartmentByInstructor.class);
        sPointInTimeDataReportRegister.put("roomUtilization", RoomUtilization.class);
        sPointInTimeDataReportRegister.put("roomTypeUtilization", RoomTypeUtilizationByDepartment.class);
        sPointInTimeDataReportRegister.put("wschByDayOfWeekAndPeriod", WSEByDayOfWeekAndPeriod.class);
        sPointInTimeDataReportRegister.put("wschByDayOfWeekAndHourOfDay", WSCHByDayOfWeekAndHourOfDay.class);
        sPointInTimeDataReportRegister.put("wschByItypeDayOfWeekAndHourOfDay", WSCHByItypeDayOfWeekHourOfDay.class);
        sPointInTimeDataReportRegister.put("wschByDeptDayOfWeekAndHourOfDay", WSCHByDepartmentDayOfWeekHourOfDay.class);
        sPointInTimeDataReportRegister.put("wschBySubjectAreaDayOfWeekAndHourOfDay", WSCHBySubjectAreaDayOfWeekHourOfDay.class);
        sPointInTimeDataReportRegister.put("wschByBuildingDayOfWeekAndHourOfDay", WSCHByBuildingDayOfWeekHourOfDay.class);
                 
        }
    
    private ArrayList<Parameter> parameters = new ArrayList<BasePointInTimeDataReports.Parameter>();
    private String[] header;
    private ArrayList<String[]> data = new ArrayList<String[]>();
    private HashMap<Parameter, ArrayList<Object>> parameterValues = new HashMap<Parameter, ArrayList<Object>>();


	public BasePointInTimeDataReports() {
		super();
		getParameters().add(Parameter.PITD);
		getParameters().add(Parameter.MINUTES_IN_REPORTING_HOUR);
		getParameters().add(Parameter.WEEKS_IN_REPORTING_TERM);
		intializeHeader();
	}

	public abstract String reportName();

	public abstract String reportDescription();
	
	protected abstract void intializeHeader();
	protected void setHeader(ArrayList<String> header) { 
		this.header = new String[header.size()];
		for(int i = 0; i < header.size(); i++) {
			this.header[i] = header.get(i);
		}
	}
	public String[] getHeader() { return (header); }
	
	protected void addDataRow(ArrayList<String> row) { 
		String[] r = new String[row.size()];
		for(int i = 0; i < row.size(); i++){
			r[i] = row.get(i);
		}
		this.data.add(r);
	}
	public ArrayList<String[]> getData() { return(data); }
	
	public HashMap<Parameter, ArrayList<Object>> getParameterValues() { return parameterValues; }

	public Long getPointInTimeDataUniqueId() { return pointInTimeDataUniqueId; }
	public void setPointInTimeDataUniqueId(Long pointInTimeDataUniqueId) { this.pointInTimeDataUniqueId = pointInTimeDataUniqueId; }

	public Float getStandardMinutesInReportingHour() { return standardMinutesInReportingHour; }
	public void setStandardMinutesInReportingHour(Float standardMinutesInReportingHour) { this.standardMinutesInReportingHour = standardMinutesInReportingHour; }

	public Float getStandardWeeksInReportingTerm() { return standardWeeksInReportingTerm; }
	public void setStandardWeeksInReportingTerm(Float standardWeeksInReportingTerm) { this.standardWeeksInReportingTerm = standardWeeksInReportingTerm; }

	
	public ArrayList<Parameter> getParameters() { return parameters; }
	public void setParameters(ArrayList<Parameter> parameters) { this.parameters = parameters; }
	
	protected void parseParameters() {
		if (getParameterValues().get(Parameter.PITD).size() != 1){
			//TODO: error
		} else {
			setPointInTimeDataUniqueId((Long)getParameterValues().get(Parameter.PITD).get(0));
		}
		if (getParameterValues().get(Parameter.MINUTES_IN_REPORTING_HOUR).size() != 1){
			//TODO: error
		} else {
			setStandardMinutesInReportingHour((Float)getParameterValues().get(Parameter.MINUTES_IN_REPORTING_HOUR).get(0));
		}
		if (getParameterValues().get(Parameter.WEEKS_IN_REPORTING_TERM).size() != 1){
			//TODO: error
		} else {
			setStandardWeeksInReportingTerm((Float)getParameterValues().get(Parameter.WEEKS_IN_REPORTING_TERM).get(0));
		}
	}
	protected abstract void runReport(org.hibernate.Session hibSession);
	
	private ArrayList<String[]> outputReport(){
		ArrayList<String[]> report = new ArrayList<String[]>();
		report.add(getHeader());
		for (String[] row : getData()){
			report.add(row);
		}
		return(report);
	}
	
	private void setParameterValues(HashMap<Parameter, String> parameterValues){
		for(Parameter p : parameterValues.keySet()){
			getParameterValues().put(p, p.parseSetValue(parameterValues.get(p)));
		}
	}
	
	
	
	public ArrayList<String[]> execute(HashMap<Parameter, String> parameterValues, org.hibernate.Session hibSession) {
		setParameterValues(parameterValues);
		parseParameters();
		runReport(hibSession);
		return(outputReport());
	}


	protected List<PitClass> findAllPitClassesWithContactHoursForDepartment(PointInTimeData pointInTimeData, Department department, org.hibernate.Session hibSession) {
		return(findAllPitClassesWithContactHoursForDepartment(pointInTimeData, department.getUniqueId(), hibSession));
	}

	@SuppressWarnings("unchecked")
	protected List<PitClass> findAllPitClassesWithContactHoursForDepartment(PointInTimeData pointInTimeData, Long departmentId, org.hibernate.Session hibSession) {
		StringBuilder sb = new StringBuilder();
		sb.append("select pc")
		  .append(" from PitClass pc")
		  .append(" inner join pc.pitSchedulingSubpart.pitInstrOfferingConfig.pitInstructionalOffering.pitCourseOfferings as pco")
		  .append(" where pc.pitSchedulingSubpart.pitInstrOfferingConfig.pitInstructionalOffering.pointInTimeData.uniqueId = :pitdUid")
		  .append(" and pc.pitClassEvents is not empty")
		  .append(" and pco.isControl = true")
		  .append(" and pco.subjectArea.department.uniqueId = :deptUid");
	
		return((List<PitClass>)hibSession.createQuery(sb.toString())
		          .setLong("pitdUid", pointInTimeData.getUniqueId().longValue())
		          .setLong("deptUid", departmentId.longValue())
		          .setCacheable(true)
		          .list());
	}

}