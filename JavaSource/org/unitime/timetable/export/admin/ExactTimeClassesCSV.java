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
package org.unitime.timetable.export.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.courses.ClassesCSV;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.duration.DurationModel;

@Service("org.unitime.timetable.export.Exporter:exact-time-classes.csv")
public class ExactTimeClassesCSV extends ClassesCSV {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public String reference() {
		return "exact-time-classes.csv";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		helper.getSessionContext().checkPermission(Right.TimePatterns);
		
		TableInterface table = new TableInterface();
		LineInterface header = table.addHeader();
		header.addCell(MSG.columnClass());
        header.addCell(MSG.columnTimePattern());
        header.addCell(MSG.columnAssignedTime());
		
		TimePattern tp = TimePattern.findExactTime(helper.getAcademicSessionId());
        if (tp == null)
        	table.setErrorMessage(MSG.errorNoExactTimePatternDefined());
        else {
        	List<TimePref> timePrefs = TimePatternDAO.getInstance().getSession().
            		createQuery("select distinct p from TimePref as p inner join p.timePattern as tp where tp.uniqueId=:uniqueid", TimePref.class).
    				setParameter("uniqueid", tp.getUniqueId()).
            		list();
        	for (TimePref tpref: timePrefs) {
        		if (tpref.getOwner() instanceof Class_) {
        			Class_ clazz = (Class_)tpref.getOwner();
        			DurationModel dm = clazz.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
        		
            		int dayCode = tpref.getTimePatternModel().getExactDays();
            		String name = "";
            		int nrDays = 0;
            		for (int j=0;j<Constants.DAY_CODES.length;j++) {
            			if ((Constants.DAY_CODES[j]&dayCode)!=0) { 
            				name += CONSTANTS.shortDays()[j];
            				nrDays ++;
            			}
            		}
            		name += " ";
            		int startSlot = tpref.getTimePatternModel().getExactStartSlot();
                    name+= Constants.toTime(Constants.FIRST_SLOT_TIME_MIN + (Constants.SLOT_LENGTH_MIN*startSlot));
            		int minPerMtg = (nrDays==0?0:dm.getExactTimeMinutesPerMeeting(clazz.getSchedulingSubpart().getMinutesPerWk(), clazz.effectiveDatePattern(), dayCode)); 
            		if (nrDays==0)
                        Debug.warning("Class "+clazz.getClassLabel()+" has zero number of days.");
            		
            		LineInterface line = table.addLine();
            		line.addCell(clazz.getClassLabel());
            		line.addCell(nrDays + " x " + minPerMtg);
            		line.addCell(name);
        		}
        	}
            table.sort(MSG.columnClass());
        }
		
		List<TableInterface> tables = new ArrayList<TableInterface>(1); tables.add(table); 
		exportDataCsv(tables, helper);
	}

}
