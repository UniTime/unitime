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

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.HtmlUtils;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.SubpartDetailReponse;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.SubpartDetailRequest;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.TimePatternModel;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.DatePattern.DatePatternType;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.duration.DurationModel;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.JavascriptFunctions;
import org.unitime.timetable.webutil.BackTracker.BackItem;

@GwtRpcImplements(SubpartDetailRequest.class)
public class SubpartDetailBackend implements GwtRpcImplementation<SubpartDetailRequest, SubpartDetailReponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static final GwtMessages GWT = Localization.create(GwtMessages.class);
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	
	@Override
	public SubpartDetailReponse execute(SubpartDetailRequest request, SessionContext context) {
		org.hibernate.Session hibSession = SchedulingSubpartDAO.getInstance().getSession();
		SchedulingSubpart ss = SchedulingSubpartDAO.getInstance().get(request.getSubpartgId(), hibSession);
		context.checkPermission(ss, Right.SchedulingSubpartDetail);
		
		if (request.getAction() == null) {
	        BackTracker.markForBack(context,
	        		"subpart?id=" + request.getSubpartgId(),
	        		MSG.backSubpart(ss.getSchedulingSubpartLabel()),
	        		true, false);
		} else {
			switch(request.getAction()) {
			case ClearPrefs:
	        	for (Class_ c: ss.getClasses()) {
	        		c.getPreferences().clear();
	        		hibSession.merge(c);
	        	}

	            ChangeLog.addChange(
	                    null,
	                    context,
	                    ss,
	                    ChangeLog.Source.SCHEDULING_SUBPART_EDIT,
	                    ChangeLog.Operation.CLEAR_ALL_PREF,
	                    ss.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
	                    ss.getManagingDept());
	            
	        	hibSession.flush();
	            break;
			}
		}

		SubpartDetailReponse response = new SubpartDetailReponse();
		response.setConfirms(JavascriptFunctions.isJsConfirm(context));
		
		InstrOfferingConfig ioc = ss.getInstrOfferingConfig();
		InstructionalOffering io = ioc.getInstructionalOffering();
		
		response.setSubpartId(ss.getUniqueId());
		String label = ss.getItype().getAbbv();
		SchedulingSubpart parent = ss.getParentSubpart();
		while (parent != null) {
			label = parent.getItype().getAbbv() + " - " + label;
			parent = parent.getParentSubpart();
		}
        if (io.hasMultipleConfigurations())
        	label += " [" + ioc.getName() + "]";
		response.setSubparName(label);
		response.setOfferingId(ss.getInstrOfferingConfig().getInstructionalOffering().getUniqueId());
		response.setCourseName(ss.getCourseNameWithTitle());
		
        SchedulingSubpart next = ss.getNextSchedulingSubpart(context, Right.SchedulingSubpartDetail); 
        response.setNextId(next==null ? null : next.getUniqueId());
        SchedulingSubpart previous = ss.getPreviousSchedulingSubpart(context, Right.SchedulingSubpartDetail); 
        response.setPreviousId(previous == null ? null : previous.getUniqueId());
		
		response.addProperty(MSG.filterManager()).add(ss.getManagingDept().getManagingDeptLabel());
        if (ss.getParentSubpart() != null) {
        	CellInterface c = response.addProperty(MSG.propertyParentSchedulingSubpart()).setText(ss.getParentSubpart().getSchedulingSubpartLabel());
        	if (context.hasPermission(ss.getParentSubpart(), Right.SchedulingSubpartDetail)) {
        		c.setUrl("subpart?id=" + ss.getParentSubpart().getUniqueId());
        		c.setClassName("link");
        	}
        }
        response.addProperty(MSG.filterInstructionalType()).add(ss.getItype().getDesc());
		
        DatePattern dp = ss.getDatePattern();
        if (dp != null) {
        	CellInterface c = response.addProperty(MSG.propertyDatePattern()).add(dp.getName()).add("");
        	if (dp.getDatePatternType() != DatePatternType.PatternSet) {
        		c.addClick().setTitle(MSG.sectPreviewOfDatePattern(dp.getName()))
        			.addWidget().setId("UniTimeGWT:DatePattern").setContent(dp.getPatternText());
        		c.setImage().setSource("images/calendar.png").addStyle("cursor: pointer; padding-left: 5px; vertical-align: bottom;");
        	}
        } else {
        	dp = ss.effectiveDatePattern();
        	if (dp != null) {
            	CellInterface c = response.addProperty(MSG.propertyDatePattern()).add(MSG.dropDefaultDatePattern() + " (" + dp.getName() + ")").add("");
            	if (dp.getDatePatternType() != DatePatternType.PatternSet) {
            		c.addClick().setTitle(MSG.sectPreviewOfDatePattern(dp.getName()))
            			.addWidget().setId("UniTimeGWT:DatePattern").setContent(dp.getPatternText());
            		c.setImage().setSource("images/calendar.png").addStyle("cursor: pointer; padding-left: 5px; vertical-align: bottom;");
            	}
        	}
        }
        if (Boolean.FALSE.equals(ss.isAutoSpreadInTime()))
        	response.addProperty(MSG.propertyAutomaticSpreadInTime()).add(MSG.classDetailNoSpread(), true);
        if (Boolean.TRUE.equals(ss.isStudentAllowOverlap()))
        	response.addProperty(MSG.propertyStudentOverlaps()).add(MSG.classDetailAllowOverlap(), true);
        if (ss.getParentSubpart() == null || !ss.getItype().equals(ss.getParentSubpart().getItype())) {
        	CourseCreditUnitConfig credit = ss.getCredit();
        	if (credit != null)
        		response.addProperty(MSG.propertySubpartCredit()).add(credit.creditText());
        }
        
        if (CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.DisplayLastChanges))) {
        	ChangeLog cl = ChangeLog.findLastChange(ss);
        	if (cl != null)
        		response.addProperty(GWT.propLastChange()).add(cl.getShortLabel());
        	else
        		response.addProperty(GWT.propLastChange()).add(GWT.notApplicable()).addStyle("font-style: italic;");
        }
        
        response.setPreferences(getPreferenceTable(context, ss, Preference.Type.DATE, Preference.Type.TIME,
        		Preference.Type.ROOM_GROUP, Preference.Type.ROOM, Preference.Type.BUILDING, Preference.Type.ROOM_FEATURE));
		
        ClassesTableBuilder builder = new ClassesTableBuilder(context, null, null);
        response.setClasses(builder.generateTableForSubpart(classAssignmentService.getAssignment(), ss));
    	
    	ExaminationsTableBuilder examBuilder = new ExaminationsTableBuilder(context, null, null);
    	response.setExaminations(examBuilder.createExamsTable("SchedulingSubpart", ss.getUniqueId(), examinationSolverService.getSolver()));
	    
    	DistributionsTableBuilder distBuilder = new DistributionsTableBuilder(context, null, null);
    	response.setDistributions(distBuilder.getDistPrefsTableForSchedulingSupart(ss));
    	
    	BackItem back = BackTracker.getBackItem(context, 2);
    	if (back != null) {
    		response.addOperation("back");
    		response.setBackTitle(back.getTitle());
    		response.setBackUrl(back.getUrl() +
    				(back.getUrl().indexOf('?') >= 0 ? "&" : "?") +
    				"backId=" + ss.getUniqueId() + "&backType=PreferenceGroup");
    	}
    	if (response.getPreviousId() != null && context.hasPermission(response.getPreviousId(), "SchedulingSubpart", Right.SchedulingSubpartDetail))
    		response.addOperation("previous");
    	if (response.getNextId() != null && context.hasPermission(response.getNextId(), "SchedulingSubpart", Right.SchedulingSubpartDetail))
    		response.addOperation("next");
    	if (context.hasPermission(Right.ExaminationAdd))
    		response.addOperation("add-exam");
    	if (context.hasPermission(ss.getManagingDept(), Right.DistributionPreferenceAdd) && context.hasPermission(ss, Right.DistributionPreferenceSubpart)) {
    		if (ApplicationProperty.LegacyDistributions.isTrue())
    			response.addOperation("add-distribution-legacy");
    		else
    			response.addOperation("add-distribution");
    	}
    	if (context.hasPermission(ss, Right.SchedulingSubpartEdit))
    		response.addOperation("edit");
    	if (context.hasPermission(ss, Right.SchedulingSubpartDetailClearClassPreferences))
    		response.addOperation("clear-prefs");
		
		return response;
	}
	
	public static TableInterface getPreferenceTable(SessionContext context, PreferenceGroup pg, Preference.Type... types) {
		TableInterface table = new TableInterface();
		boolean hasNotAvailable = false;
		boolean multipleRooms = false;
		boolean excap = (pg instanceof Exam && ((Exam)pg).getSeatingType() == Exam.sSeatingTypeExam);
		DurationModel dm = null;
		int minutes = 0;
		if (pg instanceof Class_) {
    		Class_ clazz = (Class_)pg;
    		if (clazz.getNbrRooms() > 1)
    			multipleRooms = true;
    		dm = clazz.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
    		minutes = clazz.getSchedulingSubpart().getMinutesPerWk();
    	} else if (pg instanceof SchedulingSubpart) {
    		SchedulingSubpart subpart = (SchedulingSubpart)pg;
    		int maxRooms = subpart.getMaxRooms();
    		if (maxRooms > 1) {
    			multipleRooms = true;
    		}
    		dm = subpart.getInstrOfferingConfig().getDurationModel();
    		minutes = subpart.getMinutesPerWk();
    	}
		DatePattern dp = pg.effectiveDatePattern();
		for (Preference.Type type: types) {
			switch (type) {
			case TIME:
				Set<TimePref> timePrefs = pg.effectivePreferences(TimePref.class, null, false);
				if (!timePrefs.isEmpty()) {
					CellInterface tpCell = table.addProperty(MSG.propertyTime());
					for (TimePref tp: timePrefs) {
						if (tp.getTimePattern() != null && tp.getTimePattern().isExactTime()) {
							tpCell.add(tp.getTimePatternModel().toString()).setInline(false);
						} else {
							TimePatternModel tpm = ClassEditBackend.createTimePatternModel(tp, context);
							tpCell.add(null).setTimePreference(tpm);
							if (dm != null && (dp == null || !dm.isValidCombination(minutes, dp, tp.getTimePattern())))
								tpm.setValid(false);
							/*
							RequiredTimeTable rtt = tp.getRequiredTimeTable();
				        	if (tp.getTimePatternModel().hasNotAvailablePreference()) hasNotAvailable = true;
							if (dm != null && (dp == null || !dm.isValidCombination(minutes, dp, tp.getTimePattern()))) {
								tpCell.add(tp.getTimePattern().getName());
								tpCell.add(MSG.warnNoMatchingDatePattern()).setColor("red").addStyle("font-style: italic; padding-left: 20px;");
								String hint = rtt.print(false, timeVertical, false, false, "").replace(");\n</script>", "").replace("<script language=\"javascript\">\ndocument.write(", "").replace("\n", " ");
								tpCell.add("").setInline(false).setScript("$wnd." + hint);
							} else {
								String hint = rtt.print(false, timeVertical, false, false, rtt.getModel().getName()).replace(");\n</script>", "").replace("<script language=\"javascript\">\ndocument.write(", "").replace("\n", " ");
								tpCell.add("").setInline(false).setScript("$wnd." + hint);
							}*/
						}
					}
				}
				break;
			case DATE:
				if (dp.getDatePatternType() == DatePatternType.PatternSet && !dp.getChildren().isEmpty()) {
					Set<DatePatternPref> datePrefs = pg.effectivePreferences(DatePatternPref.class);
					boolean req = false;
					CellInterface rpCell = new CellInterface();
					for (DatePattern child: new TreeSet<DatePattern>(dp.getChildren())) {
						PreferenceLevel pref = PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral);
						for (DatePatternPref p: datePrefs)
							if (p.getDatePattern().equals(child)) { pref = p.getPrefLevel(); break; }
						if (pref.getPrefProlog().equals(PreferenceLevel.sRequired)) {
							if (!req && rpCell.hasItems()) rpCell.getItems().clear();
							req = true;
						} else {
							if (req) continue;
						}
						CellInterface cell = rpCell.add(null).setInline(false);
						if (!pref.getPrefProlog().equals(PreferenceLevel.sNeutral))
							cell.setColor(PreferenceLevel.prolog2color(pref.getPrefProlog()));
						cell.setAria(pref.getPrefAbbv() + " " + child.getName());
						if (child.getDatePatternType() != DatePatternType.Alternate) {
							Formats.Format<Date> dpf = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
							Date first = child.getStartDate();
							Date last = child.getEndDate();
							cell.setText(child.getName() + " (" + dpf.format(first) + (first.equals(last) ? "" : " - " + dpf.format(last)) + ")");
						} else {
							cell.setText(child.getName());
						}
						cell.addStyle("cursor: pointer;");
			        	cell.addClick().setTitle(MSG.sectPreviewOfDatePattern(child.getName()))
			        		.addWidget().setId("UniTimeGWT:DatePattern").setContent(child.getPatternText());
					}
					if (rpCell.hasItems())
						table.addProperty(new PropertyInterface()
								.setName(MSG.propertyDatePatterns())
								.setCell(rpCell));
				}
				break;
			case ROOM_GROUP:
				Set<RoomGroupPref> roomGrouPrefs = pg.effectivePreferences(RoomGroupPref.class);
				if (!roomGrouPrefs.isEmpty()) {
					CellInterface rpCell = table.addProperty(MSG.propertyRoomGroups());
					for (RoomGroupPref rp: roomGrouPrefs) {
						CellInterface cell = rpCell.add(null).setInline(false);
						if (rp.getPrefLevel().getPrefId().intValue() != 4)
							cell.setColor(PreferenceLevel.prolog2color(rp.getPrefLevel().getPrefProlog()));
						cell.setText(rp.getRoomGroup().getNameWithTitle() + (multipleRooms ? rp.getRoomIndex() == null ? " (" + MSG.itemAllRooms() + ")" : " (" + MSG.itemOnlyRoom(rp.getRoomIndex() + 1) + ")" : ""));
						String hint = HtmlUtils.htmlEscape(MSG.prefTitleRoomGroup(rp.getPrefLevel().getPrefName(), cell.getText()));
						cell.setAria(MSG.prefTitleRoomGroup(rp.getPrefLevel().getPrefName(), cell.getText()));
						cell.setMouseOver("$wnd.showGwtHint($wnd.lastMouseOverElement, '" + hint + "');");
						cell.setMouseOut("$wnd.hideGwtHint();");
					}
				}
				break;
			case ROOM:
				Set<RoomPref> roomPrefs = pg.effectivePreferences(RoomPref.class);
				if (!roomPrefs.isEmpty()) {
					CellInterface rpCell = table.addProperty(MSG.propertyRooms());
					for (RoomPref rp: roomPrefs) {
						CellInterface cell = rpCell.add(null).setInline(false);
						if (rp.getPrefLevel().getPrefId().intValue() != 4)
							cell.setColor(PreferenceLevel.prolog2color(rp.getPrefLevel().getPrefProlog()));
						cell.setText((excap ? rp.getRoom().getLabelWithExamCapacity() : rp.getRoom().getLabelWithCapacity()) +
						(multipleRooms ? rp.getRoomIndex() == null ? " (" + MSG.itemAllRooms() + ")" : " (" + MSG.itemOnlyRoom(rp.getRoomIndex() + 1) + ")" : ""));
						cell.setAria(MSG.prefTitleRoom(rp.getPrefLevel().getPrefName(), cell.getText()));
						cell.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + rp.getRoom().getUniqueId() + "', '" + rp.getPrefLevel().getPrefName() + " " + MSG.prefRoom() + " {0} ({1})');");
				    	cell.setMouseOut("$wnd.hideGwtRoomHint();");
					}
				}
				break;
			case BUILDING:
				Set<BuildingPref> buildingPrefs = pg.effectivePreferences(BuildingPref.class);
				if (!buildingPrefs.isEmpty()) {
					CellInterface rpCell = table.addProperty(MSG.propertyBuildings());
					for (BuildingPref rp: buildingPrefs) {
						CellInterface cell = rpCell.add(null).setInline(false);
						if (rp.getPrefLevel().getPrefId().intValue() != 4)
							cell.setColor(PreferenceLevel.prolog2color(rp.getPrefLevel().getPrefProlog()));
						cell.setText(rp.getBuilding().getAbbrName() + (multipleRooms ? rp.getRoomIndex() == null ? " (" + MSG.itemAllRooms() + ")" : " (" + MSG.itemOnlyRoom(rp.getRoomIndex() + 1) + ")" : ""));
						cell.setAria(MSG.prefTitleBuilding(rp.getPrefLevel().getPrefName(), cell.getText()));
						cell.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '-" + rp.getBuilding().getUniqueId() + "', '" + rp.getPrefLevel().getPrefName() + " " + MSG.prefBuilding() + " {0}');");
						cell.setMouseOut("$wnd.hideGwtRoomHint();");
					}
				}
				break;
			case ROOM_FEATURE:
				Set<RoomFeaturePref> roomFeaturePrefs = pg.effectivePreferences(RoomFeaturePref.class);
				if (!roomFeaturePrefs.isEmpty()) {
					CellInterface rpCell = table.addProperty(MSG.propertyRoomFeatures());
					for (RoomFeaturePref rp: roomFeaturePrefs) {
						CellInterface cell = rpCell.add(null).setInline(false);
						if (rp.getPrefLevel().getPrefId().intValue() != 4)
							cell.setColor(PreferenceLevel.prolog2color(rp.getPrefLevel().getPrefProlog()));
						cell.setText(rp.getRoomFeature().getLabelWithType() + (multipleRooms ? rp.getRoomIndex() == null ? " (" + MSG.itemAllRooms() + ")" : " (" + MSG.itemOnlyRoom(rp.getRoomIndex() + 1) + ")" : ""));
						cell.setAria(MSG.prefTitleRoomFeature(rp.getPrefLevel().getPrefName(), cell.getText()));
						String hint = HtmlUtils.htmlEscape(MSG.prefTitleRoomFeature(rp.getPrefLevel().getPrefName(), cell.getText()));
						cell.setMouseOver("$wnd.showGwtHint($wnd.lastMouseOverElement, '" + hint + "');");
						cell.setMouseOut("$wnd.hideGwtHint();");
					}
				}
				break;
			case COURSE:
				Set<InstructorCoursePref> coursePrefs = pg.effectivePreferences(InstructorCoursePref.class);
				if (!coursePrefs.isEmpty()) {
					CellInterface rpCell = table.addProperty(MSG.propertyCoursePrefs());
					for (InstructorCoursePref rp: coursePrefs) {
						CellInterface cell = rpCell.add(null).setInline(false);
						if (rp.getPrefLevel().getPrefId().intValue() != 4)
							cell.setColor(PreferenceLevel.prolog2color(rp.getPrefLevel().getPrefProlog()));
						cell.setText(rp.getCourse().getCourseNameWithTitle());
						cell.setAria(MSG.prefTitleRoomFeature(rp.getPrefLevel().getPrefName(), cell.getText()));
						String hint = HtmlUtils.htmlEscape(MSG.prefTitleCourse(rp.getPrefLevel().getPrefName(), cell.getText()));
						cell.setMouseOver("$wnd.showGwtHint($wnd.lastMouseOverElement, '" + hint + "');");
						cell.setMouseOut("$wnd.hideGwtHint();");
						cell.setUrl("offering?id=" + rp.getCourse().getInstructionalOffering().getUniqueId());
					}
				}
				break;
			case DISTRIBUTION:
				Set<DistributionPref> distPrefs = pg.effectivePreferences(DistributionPref.class);
				if (!distPrefs.isEmpty()) {
					CellInterface rpCell = table.addProperty(MSG.propertyDistribution());
					for (DistributionPref rp: distPrefs) {
						CellInterface cell = rpCell.add(null).setInline(false);
						if (rp.getPrefLevel().getPrefId().intValue() != 4)
							cell.setColor(PreferenceLevel.prolog2color(rp.getPrefLevel().getPrefProlog()));
						cell.setText(rp.getDistributionType().getLabel());
						cell.setAria(MSG.prefTitleDistribution(rp.getPrefLevel().getPrefName(), cell.getText()));
						String hint = HtmlUtils.htmlEscape(rp.getDistributionType().getDescr());
						cell.setMouseOver("$wnd.showGwtHint($wnd.lastMouseOverElement, '" + hint + "');");
						cell.setMouseOut("$wnd.hideGwtHint();");
					}
				}
				break;
			}
		}
		
		if (!table.hasProperties()) return null;
		
		
		table.addProperty(getLegend(hasNotAvailable));
		
		return table;
	}
	
	public static PropertyInterface getLegend(boolean hasNotAvailable) {
		PropertyInterface legend = new PropertyInterface();
		CellInterface cell = new CellInterface(); legend.setCell(cell);
		for (PreferenceLevel pref: PreferenceLevelDAO.getInstance().getSession().createQuery(
        			"from PreferenceLevel order by prefId", PreferenceLevel.class).setCacheable(true).list()) {
			if (!hasNotAvailable && pref.getPrefProlog().equals(PreferenceLevel.sNotAvailable)) continue;
			CellInterface c = cell.add(null).setNoWrap(false);
			c.addImage().setSource("images/pref" + pref.getPrefId() + ".png").addStyle("padding-top: 1px;");
			c.add(pref.getPrefName()).addStyle("padding-left: 10px;");
			c.addStyle("padding-right: 20px;");
		}
		legend.addStyle("text-align: center;");
		legend.addStyle("border-top: 1px dashed #9CB0CE;");
		return legend;
	}
	
}
