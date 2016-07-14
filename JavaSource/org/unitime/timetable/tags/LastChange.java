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
package org.unitime.timetable.tags;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;


/**
 * @author Tomas Muller
 */
public class LastChange extends BodyTagSupport {
	private static final long serialVersionUID = -983949265164022751L;
	private String iPackage = "org.unitime.timetable.model";
    private String iType = null;
    private String iId = null;
    private String iSource = null;
    
    public LastChange() {
        super();
    }

    public void setType(String type) {
        iType = type;
    }
    public String getType() {
        return iType;
    }

    public void setId(String id) {
        iId = id;
    }
    public String getId() {
        return iId;
    }

    public void setPackage(String packageStr) {
        iPackage = packageStr;
    }
    public String getPackage() {
        return iPackage;
    }

    public void setSource(String source) {
        iSource = source;
    }
    public String getSource() {
        return iSource;
    }
    
    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }
    
    public SessionContext getSessionContext() {
    	return HttpSessionContext.getSessionContext(pageContext.getServletContext());
    }

    private int printLastChangeTableRow(WebTable webTable, ChangeLog lastChange) {
        if (lastChange==null) return 0;
        webTable.addLine(null,
                new String[] {
                        lastChange.getSourceTitle(),
                        lastChange.getObjectTitle(),
                        lastChange.getOperationTitle(),
                        lastChange.getManager().getShortName(),
                        ChangeLog.sDF.format(lastChange.getTimeStamp())},
                new Comparable[] {
                        lastChange.getSourceTitle(), //new Integer(lastChange.getSource().ordinal()),
                        lastChange.getObjectTitle(),
                        new Integer(lastChange.getOperation().ordinal()),
                        lastChange.getManager().getName(),
                        lastChange.getTimeStamp().getTime()});
        return 1;
    }
    
    private static ChangeLog combine(ChangeLog c1, ChangeLog c2) {
        if (c1==null) return c2;
        if (c2==null) return c1;
        return (c1.compareTo(c2)<0?c2:c1);
    }

    public boolean printLastChange(InstructionalOffering io) throws IOException {
        if (io==null) return false;
        int nrChanges = 0;
        
        WebTable.setOrder(getSessionContext(),"lastChanges.ord",pageContext.getRequest().getParameter("lcord"),5);
        
        WebTable webTable = new WebTable( 5, "Last Changes",
                "instructionalOfferingDetail.do?io="+io.getUniqueId()+"&lcord=%%",
                new String[] {"Page", "Object", "Operation", "Manager", "Date"},
                new String[] {"left", "left", "left", "left", "left"},
                new boolean[] { true, true, true, true, false} );
        //webTable.setRowStyle("white-space:nowrap");
        
        
        HashSet configIds = new HashSet();
        HashSet subpartIds = new HashSet();
        HashSet classIds = new HashSet();
        HashSet offeringIds = new HashSet();
        HashSet curriculumIds = new HashSet();
        
        for (Iterator i1=io.getInstrOfferingConfigs().iterator();i1.hasNext();) {
            InstrOfferingConfig ioc = (InstrOfferingConfig)i1.next();
            configIds.add(ioc.getUniqueId());
            for (Iterator i2=ioc.getSchedulingSubparts().iterator();i2.hasNext();) {
                SchedulingSubpart ss = (SchedulingSubpart)i2.next();
                subpartIds.add(ss.getUniqueId());
                for (Iterator i3=ss.getClasses().iterator();i3.hasNext();) {
                    Class_ c = (Class_)i3.next();
                    classIds.add(c.getUniqueId());
                }
            }
        }
        for (Iterator i1=io.getCourseOfferings().iterator();i1.hasNext();) {
            CourseOffering o = (CourseOffering)i1.next();
            offeringIds.add(o.getUniqueId());
        }
        
        curriculumIds.addAll((List<Long>)InstructionalOfferingDAO.getInstance().getSession().createQuery(
				"select c.classification.curriculum.uniqueId from CurriculumCourse c where c.course.instructionalOffering.uniqueId = :offeringId")
				.setLong("offeringId", io.getUniqueId()).setCacheable(true).list());

        nrChanges += printLastChangeTableRow(webTable, 
                ChangeLog.findLastChange(io, ChangeLog.Source.CROSS_LIST));
        
        nrChanges += printLastChangeTableRow(webTable, 
                combine(
                        ChangeLog.findLastChange(io, ChangeLog.Source.MAKE_OFFERED),
                        ChangeLog.findLastChange(io, ChangeLog.Source.MAKE_NOT_OFFERED)));

        nrChanges += printLastChangeTableRow(webTable, 
                ChangeLog.findLastChange(CourseOffering.class.getName(), offeringIds, ChangeLog.Source.COURSE_OFFERING_EDIT));

        nrChanges += printLastChangeTableRow(webTable, 
                combine(
                        ChangeLog.findLastChange(InstructionalOffering.class.getName(), io.getUniqueId(), ChangeLog.Source.RESERVATION),
                        ChangeLog.findLastChange(Class_.class.getName(), classIds, ChangeLog.Source.RESERVATION)));
        
        nrChanges += printLastChangeTableRow(webTable,
                combine(
                        ChangeLog.findLastChange(io, ChangeLog.Source.INSTR_CFG_EDIT),
                        ChangeLog.findLastChange(InstrOfferingConfig.class.getName(), configIds, ChangeLog.Source.INSTR_CFG_EDIT)));

        nrChanges += printLastChangeTableRow(webTable, 
                ChangeLog.findLastChange(InstrOfferingConfig.class.getName(), configIds, ChangeLog.Source.CLASS_SETUP));
        
        nrChanges += printLastChangeTableRow(webTable, 
                ChangeLog.findLastChange(InstrOfferingConfig.class.getName(), configIds, ChangeLog.Source.CLASS_INSTR_ASSIGN));
        
        nrChanges += printLastChangeTableRow(webTable, 
                ChangeLog.findLastChange(SchedulingSubpart.class.getName(), subpartIds, ChangeLog.Source.SCHEDULING_SUBPART_EDIT));
        
        nrChanges += printLastChangeTableRow(webTable, 
                ChangeLog.findLastChange(Class_.class.getName(), classIds, ChangeLog.Source.CLASS_EDIT));
        
        nrChanges += printLastChangeTableRow(webTable, 
                ChangeLog.findLastChange(io, ChangeLog.Source.DIST_PREF_EDIT));
        
        nrChanges += printLastChangeTableRow(webTable, 
        		ChangeLog.findLastChange(CourseOffering.class.getName(), offeringIds, ChangeLog.Source.CURRICULA));
        		
        nrChanges += printLastChangeTableRow(webTable, 
                combine(ChangeLog.findLastChange(Curriculum.class.getName(), curriculumIds, ChangeLog.Source.CURRICULA),
                		ChangeLog.findLastChange(Curriculum.class.getName(), curriculumIds, ChangeLog.Source.CURRICULUM_EDIT)
                		));

        nrChanges += printLastChangeTableRow(webTable, 
        		ChangeLog.findLastChange(Class_.class.getName(), classIds, ChangeLog.Source.INSTRUCTOR_ASSIGNMENT));

        if (nrChanges>0) {
            pageContext.getOut().println(
                    "<TR><TD coslpan='2'>&nbsp;</TD></TR>"+
                    "<TR><TD colspan='2'><table border='0' width='100%' cellspacing='0' cellpadding='3'>"+
                    webTable.printTable(WebTable.getOrder(getSessionContext(),"lastChanges.ord"))+
                    "</table></TD></TR>"
                    );
        }
        
        return true;
    }
    
    public boolean printLastChange(DepartmentalInstructor inst) throws IOException {
        if (inst==null) return false;
        int nrChanges = 0;
        
        WebTable.setOrder(getSessionContext(),"lastChanges.ord",pageContext.getRequest().getParameter("lcord"),5);
        
        WebTable webTable = new WebTable( 5, "Last Changes",
                "instructorDetail.do?instructorId="+inst.getUniqueId()+"&lcord=%%",
                new String[] {"Page", "Object", "Operation", "Manager", "Date"},
                new String[] {"left", "left", "left", "left", "left"},
                new boolean[] { true, true, true, true, false} );
        //webTable.setRowStyle("white-space:nowrap");
        
        nrChanges += printLastChangeTableRow(webTable,
                combine(
                        ChangeLog.findLastChange(inst, ChangeLog.Source.INSTRUCTOR_EDIT),
                        ChangeLog.findLastChange(inst, ChangeLog.Source.INSTRUCTOR_MANAGE)));
        
        nrChanges += printLastChangeTableRow(webTable, 
                ChangeLog.findLastChange(inst, ChangeLog.Source.INSTRUCTOR_PREF_EDIT));
        
        nrChanges += printLastChangeTableRow(webTable, 
                ChangeLog.findLastChange(inst, ChangeLog.Source.INSTRUCTOR_ASSIGNMENT_PREF_EDIT));

        if (nrChanges>0) {
            pageContext.getOut().println(
                    "<TR><TD coslpan='2'>&nbsp;</TD></TR>"+
                    "<TR><TD colspan='2'><table border='0' width='100%' cellspacing='0' cellpadding='3'>"+
                    webTable.printTable(WebTable.getOrder(getSessionContext(),"lastChanges.ord"))+
                    "</table></TD></TR>"
                    );
        }
        
        return true;
    }

    public boolean printLastChange(Location location) throws IOException {
        if (location==null) return false;
        int nrChanges = 0;
        
        WebTable.setOrder(getSessionContext(),"lastChanges.ord",pageContext.getRequest().getParameter("lcord"),5);
        
        WebTable webTable = new WebTable( 5, "Last Changes",
                "roomDetail.do?id="+location.getUniqueId()+"&lcord=%%",
                new String[] {"Page", "Object", "Operation", "Manager", "Date"},
                new String[] {"left", "left", "left", "left", "left"},
                new boolean[] { true, true, true, true, false} );
        //webTable.setRowStyle("white-space:nowrap");
        
        nrChanges += printLastChangeTableRow(webTable, ChangeLog.findLastChange(location, ChangeLog.Source.ROOM_EDIT));
        
        nrChanges += printLastChangeTableRow(webTable, ChangeLog.findLastChange(location, ChangeLog.Source.ROOM_DEPT_EDIT));
        
        nrChanges += printLastChangeTableRow(webTable, ChangeLog.findLastChange(location, ChangeLog.Source.ROOM_PREF_EDIT));
        
        HashSet roomFeatureIds = new HashSet();
        for (Iterator i=location.getGlobalRoomFeatures().iterator();i.hasNext();) {
            roomFeatureIds.add(((RoomFeature)i.next()).getUniqueId());
        }
        for (Iterator i=location.getDepartmentRoomFeatures().iterator();i.hasNext();) {
            roomFeatureIds.add(((RoomFeature)i.next()).getUniqueId());
        }
        HashSet roomGroupIds = new HashSet();
        for (Iterator i=location.getRoomGroups().iterator();i.hasNext();) {
            roomGroupIds.add(((RoomGroup)i.next()).getUniqueId());
        }
        
        nrChanges += printLastChangeTableRow(webTable, 
                combine(
                        ChangeLog.findLastChange(location, ChangeLog.Source.ROOM_FEATURE_EDIT),
                combine(ChangeLog.findLastChange(GlobalRoomFeature.class.getName(), roomFeatureIds, ChangeLog.Source.ROOM_FEATURE_EDIT),
                        ChangeLog.findLastChange(DepartmentRoomFeature.class.getName(), roomFeatureIds, ChangeLog.Source.ROOM_FEATURE_EDIT))));
        
        nrChanges += printLastChangeTableRow(webTable,
                combine(
                        ChangeLog.findLastChange(location, ChangeLog.Source.ROOM_GROUP_EDIT),
                        ChangeLog.findLastChange(RoomGroup.class.getName(), roomGroupIds, ChangeLog.Source.ROOM_GROUP_EDIT)));

        if (nrChanges>0) {
            pageContext.getOut().println(
                    "<TR><TD coslpan='2'>&nbsp;</TD></TR>"+
                    "<TR><TD colspan='2'><table border='0' width='100%' cellspacing='0' cellpadding='3'>"+
                    webTable.printTable(WebTable.getOrder(getSessionContext(),"lastChanges.ord"))+
                    "</table></TD></TR>"
                    );
        }
        
        return true;
    }

    public int doEndTag() throws JspException {
        try {
        	if (!getSessionContext().isAuthenticated() || CommonValues.No.eq(getSessionContext().getUser().getProperty(UserProperty.DisplayLastChanges)))
        		return EVAL_PAGE;
            
            String objectIdStr = (getBodyContent()==null?null:getBodyContent().getString().trim());
            if (objectIdStr==null || objectIdStr.length()==0) objectIdStr = (getId()==null?null:getId().trim());
            if (objectIdStr==null || objectIdStr.length()==0) return EVAL_PAGE;
            Long objectId = Long.parseLong(objectIdStr);
            
            String objectType = getPackage().trim() + "." + getType().trim();
            
            ChangeLog.Source source = null;
            if (getSource()!=null && getSource().trim().length()>0)
                ChangeLog.Source.valueOf(getSource().trim());
            
            if (source==null) {
                if (InstructionalOffering.class.getName().equals(objectType) && printLastChange(new InstructionalOfferingDAO().get(objectId))) 
                    return EVAL_PAGE;
                
                if (DepartmentalInstructor.class.getName().equals(objectType) && printLastChange(new DepartmentalInstructorDAO().get(objectId)))
                    return EVAL_PAGE;

                if (Location.class.getName().equals(objectType) && printLastChange(new LocationDAO().get(objectId)))
                    return EVAL_PAGE;
            }

            ChangeLog lch = ChangeLog.findLastChange(objectType,objectId,source);
            pageContext.getOut().println("<TR><TD>Last Change:</TD><TD>");
            if (lch==null)
                pageContext.getOut().print("<i>N/A</i>");
            else
                pageContext.getOut().print(lch.getShortLabel());
            pageContext.getOut().println("</TD></TR>");
        } catch (Exception e) {
            Debug.error(e);
            try {
                pageContext.getOut().print("<font color='red'>ERROR: "+e.getMessage()+"</font>");
            } catch (IOException io) {}
        }
        
        return EVAL_PAGE;
    }
    
}
