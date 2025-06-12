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
package org.unitime.timetable.server.courses;

import java.util.Iterator;

import org.springframework.web.util.HtmlUtils;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.webutil.RequiredTimeTable;

public class TableBuilder {
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static ExaminationMessages XMSG = Localization.create(ExaminationMessages.class);
	private SessionContext iContext;
    private String iBackType = null;
    private String iBackId = null;
    protected boolean iSticky = false;
	
	public TableBuilder(SessionContext context, String backType, String backId) {
		setSessionContext(context);
		setInstructorNameFormat(UserProperty.NameFormat.get(context.getUser()));
		setBackType(backType);
		setBackId(backId);
        setUserSettings(context.getUser());
	}
	
	public SessionContext getSessionContext() { return iContext; }
	public void setSessionContext(SessionContext context) { iContext = context; }
	public UserContext getUser() { return getSessionContext().getUser(); }
	public Long getCurrentAcademicSessionId() { return getUser().getCurrentAcademicSessionId(); }
	
    protected boolean iSimple = false;
	public void setSimple(boolean simple) { iSimple = simple; }
	public boolean isSimple() { return iSimple; }
	
    public String iInstructorNameFormat = "last-first";
    public void setInstructorNameFormat(String instructorNameFormat) {
    	iInstructorNameFormat = instructorNameFormat;
    }
    public String getInstructorNameFormat() {
    	return iInstructorNameFormat;
    }
    
    private Boolean iHighlightClassPrefs = null;
    public void setHighlightClassPrefs(boolean highlightClassPrefs) {
    	iHighlightClassPrefs = highlightClassPrefs;
    }
    public boolean isHighlightClassPrefs() {
    	if (iHighlightClassPrefs == null) return ApplicationProperty.PreferencesHighlighClassPreferences.isTrue();
    	return iHighlightClassPrefs;
    }
    
    public String getBackType() {
    	return iBackType;
    }
    public void setBackType(String backType) {
    	iBackType = backType;
    }
    public String getBackId() {
    	return iBackId;
    }
    public void setBackId(String backId) {
    	iBackId = backId;
    }
    
    private boolean iTimeVertical = false;
    public void setTimeVertival(boolean timeVertical) {
    	iTimeVertical = timeVertical;
    }
    public boolean getTimeVertival() {
    	return iTimeVertical;
    }
    private boolean iGridAsText = false;
    public void setGridAsText(boolean gridAsText) {
    	iGridAsText = gridAsText;
    }
    public boolean getGridAsText() {
    	return iGridAsText;
    }
    public String iDefaultTimeGridSize = null;
    public void setDefaultTimeGridSize(String defaultTimeGridSize) {
    	iDefaultTimeGridSize = defaultTimeGridSize;
    }
    public String getDefaultTimeGridSize() {
    	return iDefaultTimeGridSize;
    }
    
    public void setUserSettings(UserContext user) {
		setTimeVertival(RequiredTimeTable.getTimeGridVertical(user));
		setGridAsText(RequiredTimeTable.getTimeGridAsText(user));
		setInstructorNameFormat(UserProperty.NameFormat.get(user));
		setDefaultTimeGridSize(RequiredTimeTable.getTimeGridSize(user));
		iSticky = CommonValues.Yes.eq(UserProperty.StickyTables.get(user));
		String highlighClassPreferences = UserProperty.HighlighClassPreferences.get(user);
		if (CommonValues.Yes.eq(highlighClassPreferences))
			setHighlightClassPrefs(true);
		else if (CommonValues.No.eq(highlighClassPreferences))
			setHighlightClassPrefs(false);
		else
			setHighlightClassPrefs(ApplicationProperty.PreferencesHighlighClassPreferences.isTrue());
    }

    protected CellInterface preferenceCell(Preference p) {
		CellInterface cell = new CellInterface();
		if (!isSimple()) cell.addStyle("font-weight: bold;");
		if (p.getPrefLevel().getPrefId().intValue() != 4)
			cell.setColor(PreferenceLevel.prolog2color(p.getPrefLevel().getPrefProlog()));
		String owner = "";
		if (p.getOwner() != null && p.getOwner() instanceof Class_) {
			owner = " (" + MSG.prefOwnerClass() + ")";
		} else if (p.getOwner() != null && p.getOwner() instanceof SchedulingSubpart) {
			owner = " (" + MSG.prefOwnerSchedulingSubpart() + ")";
		} else if (p.getOwner() != null && p.getOwner() instanceof DepartmentalInstructor) {
			owner = " (" + MSG.prefOwnerInstructor() + ")";
		} else if (p.getOwner() != null && p.getOwner() instanceof Exam) {
			owner = " (" + MSG.prefOwnerExamination() + ")";
		} else if (p.getOwner() != null && p.getOwner() instanceof Department) {
			owner = " (" + MSG.prefOwnerDepartment() + ")";
		} else if (p.getOwner() != null && p.getOwner() instanceof Session) {
			owner = " (" + MSG.prefOwnerSession() + ")";
		}
		String hint = HtmlUtils.htmlEscape(p.preferenceTitle(getInstructorNameFormat()) + owner);
		String description = p.preferenceDescription();
		if (description != null && !description.isEmpty())
			hint += "<br>" + HtmlUtils.htmlEscape(description.replace("\'", "\\\'")).replace("\n", "<br>");
		//cell.setTitle(hint);
		cell.setAria(p.getPrefLevel().getPrefAbbv() + " " + p.preferenceAbbv(getInstructorNameFormat()));
		if (p.getOwner() != null && p.getOwner() instanceof Class_ && isHighlightClassPrefs())
			cell.add(p.preferenceAbbv(getInstructorNameFormat()))
				.addStyle("background: #ffa;")
				.setAria("");
		else
			cell.setText(p.preferenceAbbv(getInstructorNameFormat()));
		cell.setInline(false);
		if (p instanceof RoomPref) {
			RoomPref rp = (RoomPref) p;
			cell.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + rp.getRoom().getUniqueId() + "', '" + p.getPrefLevel().getPrefName() + " " + MSG.prefRoom() + " {0} ({1}" + owner + ")');");
	    	cell.setMouseOut("$wnd.hideGwtRoomHint();");
		} else if (p instanceof BuildingPref) {
			BuildingPref bp = (BuildingPref)p;
			cell.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '-" + bp.getBuilding().getUniqueId() + "', '" + p.getPrefLevel().getPrefName() + " " + MSG.prefBuilding() + " {0}" + owner + "');");
			cell.setMouseOut("$wnd.hideGwtRoomHint();");
		} else if (p instanceof DistributionPref) {
			DistributionPref dp = (DistributionPref) p;
			hint = HtmlUtils.htmlEscape(p.getPrefLevel().getPrefName() + " " + dp.getLabel() + owner);
			if (dp.getDistributionObjects()!=null && !dp.getDistributionObjects().isEmpty()) {
				hint += "<ul><li>";
				String aria = p.getPrefLevel().getPrefAbbv() + " " + p.preferenceAbbv(getInstructorNameFormat()) + " (";
				for (Iterator<DistributionObject> it = dp.getOrderedSetOfDistributionObjects().iterator(); it.hasNext();) {
					DistributionObject distObj = it.next();
					hint += HtmlUtils.htmlEscape(distObj.preferenceText());
					aria += distObj.preferenceText();
					if (it.hasNext()) {
						hint += "<li>";
						aria += ", ";
					}
				}
				aria += ")";
				cell.setAria(aria);
				hint += "</ul>";
			} else if (dp.getOwner() instanceof DepartmentalInstructor) {
				hint += "<ul><li>";
				for (Iterator<ClassInstructor> it = ((DepartmentalInstructor)dp.getOwner()).getClasses().iterator(); it.hasNext();) {
					ClassInstructor ci = (ClassInstructor)it.next();
					hint += HtmlUtils.htmlEscape(ci.getClassInstructing().getClassLabel());
					if (it.hasNext())
						hint += "<li>";
				}
				hint += "</ul>";
			}
			if (description != null && !description.isEmpty())
				hint += "<br>" + HtmlUtils.htmlEscape(description.replace("\'", "\\\'")).replace("\n", "<br>");
			cell.setMouseOver("$wnd.showGwtHint($wnd.lastMouseOverElement, '" + hint + "');");
			cell.setMouseOut("$wnd.hideGwtHint();");
		} else {
			cell.setMouseOver("$wnd.showGwtHint($wnd.lastMouseOverElement, '" + hint + "');");
			cell.setMouseOut("$wnd.hideGwtHint();");
		}
		return cell;
	}
}
