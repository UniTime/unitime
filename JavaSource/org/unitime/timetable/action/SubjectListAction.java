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
package org.unitime.timetable.action;

import java.util.Iterator;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;


/**
 * @author Tomas Muller
 */
@Service("/subjectList")
@Action(value = "subjectList", results = {
		@Result(name = "showSubjectList", type = "tiles", location = "subjectList.tiles"),
		@Result(name = "add", type = "redirect", location="/subjectAreaEdit.action", params = {
				"op", "${op}"})
	})
@TilesDefinition(name = "subjectList.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Subject Areas"),
		@TilesPutAttribute(name = "body", value = "/admin/subjectList.jsp")
	})
public class SubjectListAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = -7091704122522332475L;
	protected static final GwtMessages MSG = Localization.create(GwtMessages.class);
	protected static final CourseMessages CMSG = Localization.create(CourseMessages.class);

	@Override
	public String execute() throws Exception {

		sessionContext.checkPermission(Right.SubjectAreas);
		
		if (stripAccessKey(MSG.buttonAddSubjectArea()).equals(op)) {
			return "add";
		}

        if (stripAccessKey(MSG.buttonExportPDF()).equals(op)) {
        	boolean dispLastChanges = isDisplayLastChanges();
        	List<SubjectArea> subjects = SubjectArea.getSubjectAreaList(sessionContext.getUser().getCurrentAcademicSessionId());
        	PdfWebTable webTable = new PdfWebTable((dispLastChanges?5:4),
        			MSG.sectSujectAreas(sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel()),
                    "subjectList.action?ord=%%",
                    (dispLastChanges?
                        new String[] {MSG.fieldAbbv(), MSG.fieldTitle(), MSG.fieldDepartment(), MSG.fieldManagers(), MSG.fieldLastChange()}:
                        new String[] {MSG.fieldAbbv(), MSG.fieldTitle(), MSG.fieldDepartment(), MSG.fieldManagers()}),
                    new String[] {"left", "left","left","left", "right"},
                    new boolean[] {true, true, true, true, false} );
            for (SubjectArea s: subjects) {
                Department d = s.getDepartment();
                String sdName = "";
                for (Iterator it = s.getManagers().iterator(); it.hasNext();) {
                    TimetableManager mgr = (TimetableManager) it.next();
                    if (sdName.length() > 0)
                        sdName = sdName + "\n";
                    sdName = sdName + mgr.getFirstName() + " " + mgr.getLastName();
                }

                String lastChangeStr = null;
                Long lastChangeCmp = null;
                if (dispLastChanges) {
                    List changes = ChangeLog.findLastNChanges(d.getSession().getUniqueId(), null, null, d.getUniqueId(), 1);
                    ChangeLog lastChange =  (changes == null || changes.isEmpty() ? null : (ChangeLog) changes.get(0));
                    lastChangeStr =  (lastChange == null ? "" : MSG.lastChange(ChangeLog.sDFdate.format(lastChange.getTimeStamp()), lastChange.getManager().getShortName()));
                    lastChangeCmp = Long.valueOf( lastChange == null ? 0 : lastChange.getTimeStamp().getTime());
                }

                webTable.addLine(
                    null,
                    new String[] { 
                        s.getSubjectAreaAbbreviation(),
                        s.getTitle(),
                        (d == null) ? "" : d.getDeptCode()+(d.getAbbreviation()==null?"":": "+d.getAbbreviation().trim()),
                        (sdName == null || sdName.trim().length()==0) ? "" : sdName,
                        lastChangeStr },
                    new Comparable[] { 
                        s.getSubjectAreaAbbreviation(),
                        s.getTitle(),
                        (d == null) ? "" : d.getDeptCode(),
                        sdName,
                        lastChangeCmp });
            }

            ExportUtils.exportPDF(webTable, WebTable.getOrder(sessionContext, "SubjectList.ord"), response, "subjects");
            return null;
        }
        
        return "showSubjectList";
		
	}
	
	public String getTable() {
		boolean dispLastChanges = isDisplayLastChanges();
		List<SubjectArea> subjects = SubjectArea.getSubjectAreaList(sessionContext.getUser().getCurrentAcademicSessionId());
        WebTable webTable = new WebTable( 
        	    (dispLastChanges?5:4),
        	    "",
        	    "subjectList.action?ord=%%",
        	    (dispLastChanges?
        		    new String[] {MSG.fieldAbbv(), MSG.fieldTitle(), MSG.fieldDepartment(), MSG.fieldManagers(), MSG.fieldLastChange()}:
        		    new String[] {MSG.fieldAbbv(), MSG.fieldTitle(), MSG.fieldDepartment(), MSG.fieldManagers()}),
        	    new String[] {"left", "left","left","left","right"},
        	    new boolean[] {true, true, true, true, false} );
        webTable.enableHR("#9CB0CE");
        webTable.setRowStyle("white-space: nowrap");
        WebTable.setOrder(sessionContext,"SubjectList.ord",request.getParameter("ord"),1);
        
    	for (SubjectArea s: subjects) {
        	Department d = s.getDepartment();
        	String sdName = "";
        	for (Iterator it = s.getManagers().iterator(); it.hasNext();) {
        		TimetableManager mgr = (TimetableManager) it.next();
        		if (sdName.length() > 0)
        			sdName = sdName + "<BR>";
        		sdName = sdName + mgr.getFirstName() + " " + mgr.getLastName();
        	}

        	String lastChangeStr = null;
        	Long lastChangeCmp = null;
        	if (dispLastChanges) {
        		List changes = ChangeLog.findLastNChanges(
        			d.getSession().getUniqueId(), null, null, d.getUniqueId(), 1);
        		ChangeLog lastChange = 
        			(changes == null || changes.isEmpty() ? null : (ChangeLog) changes.get(0));
        		lastChangeStr = 
        		(lastChange == null 
        			? "&nbsp;"
        			: "<span title='" + HtmlUtils.htmlEscape(lastChange.getLabel()) + "'>"
        				+ MSG.lastChange(ChangeLog.sDFdate.format(lastChange.getTimeStamp()), lastChange.getManager().getShortName())
        				+ "</span>");
        		lastChangeCmp = Long.valueOf(
        			lastChange == null ? 0 : lastChange.getTimeStamp().getTime());
        	}

        	WebTableLine line = webTable.addLine(
        		"onClick=\"document.location.href='subjectAreaEdit.action?op=edit&id=" + s.getUniqueId() + "'\"",
        		new String[] { 
        			s.getSubjectAreaAbbreviation(),
        			s.getTitle(),
        			(d == null) ? "&nbsp;" : "<span title='"+d.getHtmlTitle()+"'>"+
                                    d.getDeptCode()+(d.getAbbreviation()==null?"":": "+d.getAbbreviation().trim())+
                                    "</span>",
        			(sdName == null || sdName.trim().length()==0) ? "&nbsp;" : sdName,
        			lastChangeStr },
        		new Comparable[] { 
        			s.getSubjectAreaAbbreviation(),
        			s.getTitle(),
        			(d == null) ? "" : d.getDeptCode(),
        			sdName,
        			lastChangeCmp });
        	line.setUniqueId(s.getUniqueId().toString());
        }
    	
    	return webTable.printTable(WebTable.getOrder(sessionContext, "SubjectList.ord"));		
	}
	
	public boolean isDisplayLastChanges() {
		return CommonValues.Yes.eq(UserProperty.DisplayLastChanges.get(sessionContext.getUser()));
	}
	
	public String getTitle() {
		return MSG.sectSujectAreas(sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
	}

}
