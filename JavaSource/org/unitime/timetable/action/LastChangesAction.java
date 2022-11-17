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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.LastChangesForm;
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
@Action(value="lastChanges", results = {
		@Result(name = "list", type = "tiles", location = "lastChanges.tiles"),
	})
@TilesDefinition(name = "lastChanges.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Last Changes"),
		@TilesPutAttribute(name = "body", value = "/admin/lastChanges.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "false"),
	})


public class LastChangesAction extends UniTimeAction<LastChangesForm> {
	private static final long serialVersionUID = -5679046942523976307L;
	// Messages
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	
	@SuppressWarnings("unchecked")
	public String execute() throws Exception {
        sessionContext.checkPermission(Right.Examinations);
        
        if (getForm() == null) setForm(new LastChangesForm());
		
        // Check Access
        sessionContext.checkPermission(Right.LastChanges);

        // Read operation to be performed
        String op = (getForm().getOp()!=null?getForm().getOp():request.getParameter("op"));

        if ("Apply".equals(op) 
        		|| MSG.buttonApply().equals(op) 
        		|| "Export PDF".equals(op) 
        		|| MSG.buttonExportPDF().equals(op)) {
        	getForm().save(request);
        } else {
            getForm().load(request);
        }
        
        Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
        
        ArrayList<Department> departments = new ArrayList<Department>();
        Department d = new Department();
        d.setUniqueId(Long.valueOf(-1));
        d.setDeptCode(MSG.itemAllDepartments());
        d.setName(MSG.itemAllDepartments());
        d.setExternalManager(Boolean.FALSE);
        departments.add(d);
        departments.addAll(Department.findAll(sessionId));
        request.setAttribute("departments", departments);
        
        ArrayList<SubjectArea> subjects = new ArrayList<SubjectArea>();
        SubjectArea sa = new SubjectArea();
        sa.setUniqueId(Long.valueOf(-1));
        sa.setSubjectAreaAbbreviation(MSG.itemAllSubjects());
        subjects.add(sa);
        subjects.addAll(new TreeSet<SubjectArea>(SubjectArea.getSubjectAreaList(sessionId)));
        request.setAttribute("subjAreas",subjects);
        
        ArrayList<TimetableManager> managers = new ArrayList<TimetableManager>();
        TimetableManager tm = new TimetableManager();
        tm.setUniqueId(Long.valueOf(-1));
        tm.setLastName(MSG.itemAllManagers());
        managers.add(tm);
        managers.addAll(TimetableManager.getManagerList());       
        request.setAttribute("managers", managers);
        boolean html = true;
        String nl = (html?"<br>":"\n");
        
        WebTable.setOrder(sessionContext,"lastChanges.ord2",request.getParameter("ord"),1);
        
        WebTable webTable = new WebTable( 7, MSG.columnLastChanges(),
                "lastChanges.action?ord=%%",
                new String[] {MSG.columnDate().replace("|", nl), 
                		MSG.columnDepartment().replace("|", nl), 
                		MSG.columnSubject().replace("|", nl), 
                   		MSG.columnManager().replace("|", nl), 
                  		MSG.columnPage().replace("|", nl),
                  		MSG.columnObject().replace("|", nl),
                  		MSG.columnOperation().replace("|", nl)},
                new String[] {"left", "left", "left", "left", "left", "left", "left"},
                new boolean[] { false, true, true, true, true, true, true} );
        
        List changes = ChangeLog.findLastNChanges(
                sessionId, 
                (getForm().getManagerId()==null || getForm().getManagerId().longValue()<0?null:getForm().getManagerId()), 
                (getForm().getSubjAreaId()==null || getForm().getSubjAreaId().longValue()<0?null:getForm().getSubjAreaId()),
                (getForm().getDepartmentId()==null || getForm().getDepartmentId().longValue()<0?null:getForm().getDepartmentId()),
                getForm().getN());
        
        if (changes!=null) {
            for (Iterator i=changes.iterator();i.hasNext();)
                printLastChangeTableRow(webTable, (ChangeLog)i.next(), true);
        }
        
        request.setAttribute("table", webTable.printTable(WebTable.getOrder(sessionContext,"lastChanges.ord2")));
        
        if (("Export PDF".equals(op) || MSG.buttonExportPDF().equals(op)) && changes!=null) {
        	html = false;
        	nl = (html?"<br>":"\n");
            PdfWebTable pdfTable = new PdfWebTable( 7, "Last Changes",
                    "lastChanges.do?ord=%%",
                    new String[] {MSG.columnDate().replace("|", nl), 
                    		MSG.columnDepartment().replace("|", nl), 
                    		MSG.columnSubject().replace("|", nl), 
                       		MSG.columnManager().replace("|", nl), 
                      		MSG.columnPage().replace("|", nl),
                      		MSG.columnObject().replace("|", nl),
                      		MSG.columnOperation().replace("|", nl)},
                    new String[] {"left", "left", "left", "left", "left", "left", "left"},
                    new boolean[] { false, true, true, true, true, true, true} );
            for (Iterator i=changes.iterator();i.hasNext();)
                printLastChangeTableRow( pdfTable, (ChangeLog)i.next(), false);
            
            ExportUtils.exportPDF(pdfTable, WebTable.getOrder(sessionContext,"lastChanges.ord2"), response, "lastChanges");
            return null;
        }
        
        return "list";
	}
    
    private int printLastChangeTableRow(WebTable webTable, ChangeLog lastChange, boolean html) {
        if (lastChange==null) return 0;
        webTable.addLine(null,
                new String[] {
                    ChangeLog.sDF.format(lastChange.getTimeStamp()),
                    (lastChange.getDepartment()==null?"":
                        (html?
                                "<span title='"+lastChange.getDepartment().getHtmlTitle()+"'>"+
                                lastChange.getDepartment().getShortLabel()+
                                "</span>":lastChange.getDepartment().getShortLabel())),
                    (lastChange.getSubjectArea()==null?"":lastChange.getSubjectArea().getSubjectAreaAbbreviation()),
                    (html?lastChange.getManager().getShortName():lastChange.getManager().getShortName().replaceAll("&nbsp;"," ")),
                    lastChange.getSourceTitle(),
                    lastChange.getObjectTitle(),
                    lastChange.getOperationTitle()
                    },
                new Comparable[] {
                    Long.valueOf(lastChange.getTimeStamp().getTime()),
                    (lastChange.getDepartment()==null?"":lastChange.getDepartment().getDeptCode()),
                    (lastChange.getSubjectArea()==null?"":lastChange.getSubjectArea().getSubjectAreaAbbreviation()),
                    lastChange.getManager().getName(),
                    lastChange.getSourceTitle(), //Integer.valueOf(lastChange.getSource().ordinal()),
                    lastChange.getObjectTitle(),
                    Integer.valueOf(lastChange.getOperation().ordinal())
                    });
        return 1;
    }
    
}

