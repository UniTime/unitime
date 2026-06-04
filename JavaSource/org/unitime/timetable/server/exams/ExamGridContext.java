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
package org.unitime.timetable.server.exams;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;

public class ExamGridContext {
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	private FilterInterface iFilter;
	private String iTextFilter;
	private boolean iShowSections;
	private Long iExamTypeId;
	private Long iSessionId;
	private Date[] iBounds;
	private Resource iResource;
	private Background iBackground;
	private OrderBy iOrderBy;
	private String iExamTypeRef;
	private transient TreeSet<ExamPeriod> iPeriods;
	private boolean iBgPrefs, iPrefStyles, iStudentConf;
	private String iNameFormat;
	private Query iRoomFilter = null;
	
	public ExamGridContext(SessionContext context, FilterInterface filter) {
		iSessionId = context.getUser().getCurrentAcademicSessionId();
		iFilter = filter;
		iTextFilter = filter.getParameterValue("filter");
		iShowSections = ("1".equals(filter.getParameterValue("showSections", "1")));
		
		String examType = filter.getParameterValue("examType");
		ExamType type = null;
		try {
			type = ExamTypeDAO.getInstance().get(Long.valueOf(examType));
		} catch (Exception e) {}
		if (type == null)
			type = ExamType.findByReference(examType);
		if (type == null)
			throw new GwtRpcException(MSG.messageNoExamType());
		iExamTypeId = type.getUniqueId();
		iExamTypeRef = type.getReference();
		
		Session session = SessionDAO.getInstance().get(iSessionId);
		iBounds = ExamPeriod.getBounds(session.getUniqueId(), session.getExamBeginDate(), type.getUniqueId());
		
		iResource = Resource.values()[Integer.valueOf(filter.getParameterValue("resource", "1"))];
		iBackground = Background.values()[Integer.valueOf(filter.getParameterValue("background", "0"))];
		iBgPrefs = ("1".equals(filter.getParameterValue("bgPreferences", "0")));
		
		iPeriods = ExamPeriod.findAll(iSessionId, iExamTypeId);
		iPrefStyles = CommonValues.Yes.eq(UserProperty.HighContrastPreferences.get(context.getUser()));
		iOrderBy = OrderBy.values()[Integer.valueOf(filter.getParameterValue("order", "0"))];
		iStudentConf = "1".equals(filter.getParameterValue("studentConf", "1"));
        iNameFormat = UserProperty.NameFormat.get(context.getUser());
        
        String rf = filter.getParameterValue("roomFilter", "");
		if (rf != null && !rf.isEmpty())
			iRoomFilter = new Query(rf);
	}
	
	public boolean isShowSections() { return iShowSections; }
	public String getTextFilter() { return iTextFilter; }
	public FilterInterface getFilter() { return iFilter; }
	public Long getExamTypeId() { return iExamTypeId; }
	public String getExamTypeRef() { return iExamTypeRef; }
	public Long getSessionId() { return iSessionId; }
	public Date[] getBounds() { return iBounds; }
	public Resource getResource() { return iResource; } 
	public Background getBackground() { return iBackground; }
	public OrderBy getOrderBy() { return iOrderBy; }
	public boolean isBgPreferences() { return iBgPrefs; }
	public boolean isUsePrefStyles() { return iPrefStyles; }
	public boolean isShowStudentConflicts() { return iStudentConf; }
	public String getNameFormat() { return iNameFormat; }
	public Query getRoomFilter() { return iRoomFilter; }
	
	public TreeSet<ExamPeriod> getPeriods() {
		if (iPeriods == null)
			iPeriods = ExamPeriod.findAll(iSessionId, iExamTypeId);
		return iPeriods;
	}
	
	public boolean match(String name) {
		if (iTextFilter==null || iTextFilter.trim().isEmpty()) return true;
        String n = name.toUpperCase();
		StringTokenizer stk1 = new StringTokenizer(iTextFilter.toUpperCase(),";");
		while (stk1.hasMoreTokens()) {
		    StringTokenizer stk2 = new StringTokenizer(stk1.nextToken()," ,");
		    boolean match = true;
		    while (match && stk2.hasMoreTokens()) {
		        String token = stk2.nextToken().trim();
		        if (token.length()==0) continue;
		        if (n.indexOf(token)<0) match = false;
		    }
		    if (match) return true;
		}
		return false;
	}

	private Set<String> iFeatureTypes = null;
    public Set<String> getRoomFeatureTypes() {
    	if (iFeatureTypes == null) {
    		iFeatureTypes = new HashSet<String>();
    		for (RoomFeatureType ft: RoomFeatureTypeDAO.getInstance().findAll())
    			iFeatureTypes.add(ft.getReference().toLowerCase().replace(' ', '_'));
    	}
    	return iFeatureTypes;
    }
	
    public static enum Resource {
    	Room,
    	Instructor,
    	Subject,
    	;
    	public String getLabel() {
    		switch (this) {
    		case Room: return MSG.resourceRoom();
    		case Instructor: return MSG.resourceInstructor();
    		case Subject: return MSG.resourceSubjectArea();
			default: return name();
			}
    	}
    };
    
    public static enum Background {
        None,
        StudentConfs,
        DirectStudentConfs,
        MoreThanTwoADayStudentConfs,
        BackToBackStudentConfs,
        InstructorConfs,
        DirectInstructorConfs,
        MoreThanTwoADayInstructorConfs,
        BackToBackInstructorConfs,
        PeriodPref,
        RoomPref,
        DistPref,
        ;
    	public String getLabel() {
    		switch(this) {
    		case None: return MSG.backgroundNone();
    		case StudentConfs: return MSG.backgroundStudentConflicts();
    		case DirectStudentConfs: return MSG.backgroundStudentDirectConflicts();
    		case MoreThanTwoADayStudentConfs: return MSG.backgroundStudentMoreThanTwoExamsADayConflicts();
    		case BackToBackStudentConfs: return MSG.backgroundStudentBackToBackConflicts();
    		case InstructorConfs: return MSG.backgroundInstructorConflicts();
    		case DirectInstructorConfs: return MSG.backgroundInstructorDirectConflicts();
    		case MoreThanTwoADayInstructorConfs: return MSG.backgroundInstructorMoreThanTwoExamsADayConflicts();
    		case BackToBackInstructorConfs: return MSG.backgroundInstructorBackToBackConflicts();
    		case PeriodPref: return MSG.backgroundPeriodPreferences();
    		case RoomPref: return MSG.backgroundRoomPreferences();
    		case DistPref: return MSG.backgroundDistributionPreferences();
    		default: return name();
    		}
    	}
    }
    
    public static enum DispMode {
        InRowHorizontal,
        InRowVertical,
        PerDayHorizontal,
        PerDayVertical,
        PerWeekHorizontal,
        PerWeekVertical,
        ;
    	public String getLabel() {
    		switch(this) {
    		case InRowHorizontal: return MSG.dispModeInRowHorizontal();
    		case InRowVertical: return MSG.dispModeInRowVertical();
    		case PerDayHorizontal: return MSG.dispModePerDayHorizontal();
    		case PerDayVertical: return MSG.dispModePerDayVertical();
    		case PerWeekHorizontal: return MSG.dispModePerWeekHorizontal();
    		case PerWeekVertical: return MSG.dispModePerWeekVertical();
    		default: return name();
    		}
    	} 	
    }
    public static enum OrderBy{
    	NameAsc,
    	NameDesc,
    	SizeAsc,
    	SizeDesc,
    	;
    	public String getLabel() {
    		switch(this) {
    		case NameAsc: return MSG.orderByNameAsc();
    		case NameDesc: return MSG.orderByNameDesc();
    		case SizeAsc: return MSG.orderBySizeAsc();
    		case SizeDesc: return MSG.orderBySizeDesc();
    		default: return name();
    		}
    	}  
    }
}
