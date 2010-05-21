/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.webutil;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.Query;
import org.unitime.commons.User;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.model.AcadAreaReservation;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PosReservation;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.comparators.InstructionalOfferingComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.util.Constants;


/**
 * Builds HTML tables for Reservations
 * 
 * @author Heston Fernandes
 */
public class ReservationsTableBuilder {

    private final String colorIncrease = "#339933";
    private final String colorDecrease = "#CC3333";

    protected WebTable individualResvTbl = null;
    protected WebTable stuGroupResvTbl = null;
    protected WebTable acadAreaResvTbl = null;
    protected WebTable posResvTbl = null;
    protected WebTable courseOffrResvTbl = null;

    protected int irTotal = 0;
    protected int[] sgrTotals = null;
    protected int[] aarTotals = null;
    protected int[] prTotals = null;
    protected int[] corTotals = null;

    /**
     * Set all tables to null
     */
    protected void reset() {
        individualResvTbl = null;
        stuGroupResvTbl = null;
        acadAreaResvTbl = null;
        posResvTbl = null;
        courseOffrResvTbl = null;
        irTotal = 0;
        sgrTotals = null;
        aarTotals = null;
        prTotals = null;
        corTotals = null;
    }


    /**
     * Add to Individual Reservation Table
     * @param resv
     * @param onClick
     */
    private void buildIndividualResvRow(IndividualReservation resv, String onClick) {
        if (individualResvTbl==null) {
            irTotal = 0;
            individualResvTbl = new WebTable (
                    5, "",
                    new String[] {"<u>Individual</u>", "Type", "Priority", "Expiration Date", "Add Student Over The Limit"},
                    new String[] {"left", "left", "center", "center", "center"},
                    new boolean[] {true, true, true, true, true} );
            individualResvTbl.enableHR("#EFEFEF");
        }

        String overLimit =  resv.isOverLimit().booleanValue()
								? "<IMG src='images/tick.gif' border='0' alt='Student can be added over the limit', title='Student can be added over the limit'"
								: "&nbsp;";

        individualResvTbl.addLine(
                onClick,
                new String[] { 	resv.getExternalUniqueId(),
                        		resv.getReservationType().getLabel(),
                        		resv.getPriority().toString(),
                        		new SimpleDateFormat("MM/dd/yyyy").format(resv.getExpirationDate()),
                        		overLimit },
                new Comparable[] {resv.getExternalUniqueId(), "", "", "", "", "" } );
        ++irTotal;
    }


    /**
     * Add to Student Group Reservation Table
     * @param resv
     * @param onClick
     */
    private void buildStuGroupResvRow(StudentGroupReservation resv, String onClick) {
        if (stuGroupResvTbl==null) {
           sgrTotals = new int[] {0, 0, 0};
           stuGroupResvTbl = new WebTable (
                    6, "",
                    new String[] {"<u>Student Group</u>", "Type", "Priority", "Reserved", "Projected", "Last Term"},
                    new String[] {"left", "left", "center", "right", "right", "right"},
                    new boolean[] {true, true, true, true, true, true} );
            stuGroupResvTbl.enableHR("#EFEFEF");
        }

        stuGroupResvTbl.addLine(
                onClick,
                new String[] { 	resv.getStudentGroup().getGroupName(),
                        		resv.getReservationType().getLabel(),
                        		resv.getPriority().toString(),
                        		resv.getReserved().toString(),
                        		resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().toString() : "&nbsp;",
                        		resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().toString() : "&nbsp;" },
                new Comparable[] {resv.getStudentGroup().getGroupName(), "", "", "", "", "" } );

        sgrTotals[0] += resv.getReserved().intValue();
        sgrTotals[1] += resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().intValue() : 0;
        sgrTotals[2] += resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().intValue() : 0;
    }


    /**
     * Add to Academic Area Reservation Table
     * @param resv
     * @param onClick
     */
    private void buildAcadAreaResvRow(AcadAreaReservation resv, String onClick) {
        if (acadAreaResvTbl==null) {
            aarTotals = new int[] {0, 0, 0, 0};
            acadAreaResvTbl = new WebTable (
                    8, "",
                    //new String[] {"<u>Academic Area</u>", "<u>Class</u>", "Type", "Priority", "Reserved", "Projected", "Last Term"},
                    //TODO Reservations Bypass to be removed later
                    new String[] {"<u>Academic Area</u>", "&nbsp;", "Type", "&nbsp;", "Reserved", "Requested", "Projected", "Last Term"},
                    new String[] {"left", "left", "left", "center", "right", "right", "right", "right"},
                    new boolean[] {true, true, true, true, true, true, true, true} );
            acadAreaResvTbl.enableHR("#EFEFEF");
        }

        String acadArea = ((AcadAreaReservation)resv).getAcademicArea().getShortTitle();
        String acadClassification = resv.getAcademicClassification()!=null
        								? resv.getAcademicClassification().getName()
        							    : "&nbsp;";
        acadAreaResvTbl.addLine(
                onClick,
                new String[] { 	acadArea,
                        		//TODO Reservations Bypass to be removed later
                        		//acadClassification,
                        		"&nbsp;",
                        		resv.getReservationType().getLabel(),
                        		//resv.getPriority().toString(),
                        		"&nbsp;",
                        		getResvString(resv.getRequested(), resv.getReserved()),
                        		resv.getRequested()!=null ? resv.getRequested().toString() : "-" ,
                        		resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().toString() : "-",
                        		resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().toString() : "-" },
                 new Comparable[] {acadArea, acadClassification, "", "", "", "", "", ""} );

        aarTotals[0] += resv.getReserved().intValue();
        aarTotals[1] += resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().intValue() : 0;
        aarTotals[2] += resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().intValue() : 0;
        aarTotals[3] += resv.getRequested()!=null ? resv.getRequested().intValue() : 0;
    }


    /**
     * Add to POS Reservation Table
     * @param resv
     * @param onClick
     */
    private void buildPosResvRow(PosReservation resv, String onClick) {
        if (posResvTbl==null) {
            prTotals = new int[] {0, 0, 0};
            posResvTbl = new WebTable (
                    7, "",
                    new String[] {"<u>POS Major</u>", "<u>Class</u>", "Type", "Priority", "Reserved", "Projected", "Last Term"},
                    new String[] {"left", "left", "left", "center", "right", "right", "right"},
                    new boolean[] {true, true, true, true, true, true, true} );
            posResvTbl.enableHR("#EFEFEF");
        }

        String posMajor = ((PosReservation)resv).getPosMajor().getName();
        String acadClassification = resv.getAcademicClassification()!=null
        								? resv.getAcademicClassification().getName()
        							    : "&nbsp;";

        posResvTbl.addLine(
                onClick,
                new String[] { 	posMajor,
                        		acadClassification,
                        		resv.getReservationType().getLabel(),
                        		resv.getPriority().toString(),
                        		resv.getReserved().toString(),
                        		resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().toString() : "&nbsp;",
                        		resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().toString() : "&nbsp;" },
               new Comparable[] {posMajor, acadClassification, "", "", "", "", "" } );

        prTotals[0] += resv.getReserved().intValue();
        prTotals[1] += resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().intValue() : 0;
        prTotals[2] += resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().intValue() : 0;
    }


    /**
     * Add to Course Offering Reservation Table
     * @param resv
     * @param onClick
     */
    private void buildCourseOffrResvRow(CourseOfferingReservation resv, String onClick) {
        boolean clsOwner = false;
        if (resv.getOwnerClassId().equals(Constants.RESV_OWNER_CLASS))
        	clsOwner = true;

        if (courseOffrResvTbl==null) {
            corTotals = clsOwner ? null : new int[] {0, 0, 0, 0};
            courseOffrResvTbl = new WebTable (
                   7, "",
                   //new String[] {"<u>Course Offering</u>", "Type", "Priority", "Reserved", "Projected", "Last Term"},
                   //TODO Reservations Bypass to be removed later
                   //new String[] {"<u>Course</u>", clsOwner ? "&nbsp;": "Type", "&nbsp;", clsOwner ? "&nbsp;": "Reserved", clsOwner ? "&nbsp;": "Projected", clsOwner ? "&nbsp;": "Last Term"},
                   clsOwner
                   	? null
                   	: new String[] {"<u>Course</u>", "&nbsp;", "&nbsp;", "Reserved", "&nbsp;", "Projected", "Last Term"},
                   new String[] {"left", "left", "center", "right", "right", "right", "right"},
                   new boolean[] {true, true, true, true, true, true, true} );
            courseOffrResvTbl.enableHR("#EFEFEF");
       }

       courseOffrResvTbl.addLine(
               onClick,
               new String[] { resv.getCourseName(),
                       		  "&nbsp;",
                       		  //TODO Reservations Bypass to be removed later
                       		  //resv.getPriority().toString(),
                       		  "&nbsp;",
                       		  clsOwner ? "&nbsp;": resv.getReserved().toString(),
                       		  "&nbsp;",
                       		  //clsOwner ? "&nbsp;": ( resv.getRequested()!=null ? resv.getRequested().toString() : "-" ),
                       		  clsOwner ? "&nbsp;": ( resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().toString() : "-" ),
                       		  clsOwner ? "&nbsp;": ( resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().toString() : "-" ) },
               new Comparable[] {resv.getCourseName(), "", "", "", "", "" } );

       if (corTotals!=null) {
	       corTotals[0] += resv.getReserved().intValue();
	       corTotals[1] += resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().intValue() : 0;
	       corTotals[2] += resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().intValue() : 0;
	       corTotals[3] += resv.getRequested()!=null ? resv.getRequested().intValue() : 0;
       }
    }

    /**
     * Color format reservation total
     * @param oRequest
     * @param oReserved
     * @return
     */
    private String getResvString(Integer oRequest, Integer oReserved ) {
        String ret = "";
        if (oRequest!=null) {
            int res = oReserved.intValue();
            int req = oRequest.intValue();
            if (res<req) ret = "<font color='" + colorDecrease + "'>" + res + "</font>";
            else if (res>req) ret = "<font color='" + colorIncrease + "'>" + res + "</font>";
            else ret = "" + res;
        }
        else {
            ret = "<font color='" + colorIncrease + "'>" + oReserved.toString() + "</font>";
        }
        return ret;
    }

    /**
     * Add total row to the individual reservation tables
     */
    private void buildTotals() {
        if (individualResvTbl!=null && irTotal>0) {
            individualResvTbl.addLine(
                    null, new String[] { "<b><u>Total: </u></b>" + irTotal, "&nbsp;", "&nbsp;", "&nbsp;", "&nbsp;" }, null );
        }

        if (acadAreaResvTbl!=null && aarTotals!=null) {
            acadAreaResvTbl.addLine(
                    null, new String[] { "&nbsp;", "&nbsp;", "&nbsp;", "&nbsp;", "<DIV class='rowTotal'>"+getResvString(new Integer(aarTotals[3]), new Integer(aarTotals[0]))+"</DIV>", "<DIV class='rowTotal'>"+aarTotals[3]+"</DIV>", "<DIV class='rowTotal'>"+aarTotals[1]+"</DIV>", "<DIV class='rowTotal'>"+aarTotals[2]+"</DIV>" }, null );
        }

        if (courseOffrResvTbl!=null && corTotals!=null) {
            courseOffrResvTbl.addLine(
//                    null, new String[] { "&nbsp;", "&nbsp;", "&nbsp;", "<DIV class='rowTotal'>"+getResvString(new Integer(corTotals[3]), new Integer(corTotals[0]))+"</DIV>", "<DIV class='rowTotal'>"+corTotals[3]+"</DIV>", "<DIV class='rowTotal'>"+corTotals[1]+"</DIV>", "<DIV class='rowTotal'>"+corTotals[2]+"</DIV>" }, null );
            		null, new String[] { "&nbsp;", "&nbsp;", "&nbsp;", "<DIV class='rowTotal'>"+new Integer(corTotals[0])+"</DIV>", "&nbsp;", "<DIV class='rowTotal'>"+corTotals[1]+"</DIV>", "<DIV class='rowTotal'>"+corTotals[2]+"</DIV>" }, null );
        }

        if (posResvTbl!=null && prTotals!=null) {
            posResvTbl.addLine(
                    null, new String[] { "&nbsp;", "&nbsp;", "&nbsp;", "&nbsp;", "<DIV class='rowTotal'>"+prTotals[0]+"</DIV>", "<DIV class='rowTotal'>"+prTotals[1]+"</DIV>", "<DIV class='rowTotal'>"+prTotals[2]+"</DIV>" }, null );
        }

        if (stuGroupResvTbl!=null && sgrTotals!=null) {
            stuGroupResvTbl.addLine(
                    null, new String[] { "&nbsp;", "&nbsp;", "&nbsp;", "&nbsp;", "<DIV class='rowTotal'>"+sgrTotals[0]+"</DIV>", "<DIV class='rowTotal'>"+sgrTotals[1]+"</DIV>", "<DIV class='rowTotal'>"+sgrTotals[2]+"</DIV>" }, null );
        }
    }

    /**
     * Enclose passed paramter in TABLE tags
     * @param str
     * @param style
     * @param width
     * @param align defaults to left
     * @param onClick
     * @return
     */
    public String createTable(String str, String style, String width, String align) {
        if (style!=null && style.trim().length()>0)
            style = "style='" + style + "'";
        else
            style = "";

        if (width!=null && width.trim().length()>0)
            width = "width='" + width + "'";
        else
            width = "";

        if (align==null || align.trim().length()==0)
            align = "left";

        return "<TABLE border='0' " + width + " align='" + align + "' " + style + " cellpadding='2' cellspacing='4'>" +
        		str + "</TABLE>";
    }


    /**
     * Generate html table for reservations given a collection of reservations
     * @param reservations
     * @param displayHeader Display Table Header
     * @param isEditable
     * @return html for reservations if exist, null otherwise
     */
    public String htmlTableForReservations(
            Collection reservations,
            boolean displayHeader,
            boolean isEditable, boolean isLimitedEditable ) {

        String ownerId = null;
        String ownerClassId = null;
		String onClick = null;
		String endOnClick = null;
		String ocAcadArea = null;
		String ocPos = null;
		String ocStuGrp = null;
		String ocCourse = null;
		String ocIndiv = null;

        if (reservations==null || reservations.size()==0)
            return null;

       // Build sub tables
		for (Iterator i=reservations.iterator(); i.hasNext();) {
		    Reservation resv = (Reservation) i.next();
		    if (ownerId==null) {
		        ownerId = resv.getOwner().toString();
		        ownerClassId = resv.getOwnerClassId();
				if (isEditable || (isLimitedEditable && resv instanceof AcadAreaReservation)) {
				    onClick = "onClick=\"document.location='reservationEdit.do"
				        		+ "?op=Submit&addBlankRow=false&ownerId=" + ownerId + "&ownerType=" + ownerClassId + "&reservationClass=";
				    endOnClick = "';\"";
			        ocIndiv = ( onClick!=null ? onClick + Constants.RESV_INDIVIDUAL + endOnClick : null );
			        ocCourse = ( onClick!=null ? onClick + Constants.RESV_COURSE + endOnClick : null );
			        ocAcadArea = ( onClick!=null ? onClick + Constants.RESV_ACAD_AREA + endOnClick : null );
			        ocPos = ( onClick!=null ? onClick + Constants.RESV_POS + endOnClick : null );
			        ocStuGrp = ( onClick!=null ? onClick + Constants.RESV_STU_GROUP + endOnClick : null );
				}
		    }

		    if (resv instanceof IndividualReservation) {
		        buildIndividualResvRow((IndividualReservation) resv, ocIndiv);
		    }

		    if (resv instanceof StudentGroupReservation) {
		        buildStuGroupResvRow((StudentGroupReservation) resv, ocStuGrp);
		    }

		    if (resv instanceof AcadAreaReservation) {
		        buildAcadAreaResvRow((AcadAreaReservation) resv, ocAcadArea);
		    }

		    if (resv instanceof PosReservation) {
		        buildPosResvRow((PosReservation) resv, ocPos);
		    }

		    if (resv instanceof CourseOfferingReservation) {
		        buildCourseOffrResvRow((CourseOfferingReservation) resv, ocCourse);
		    }
		}

		// Create Total row
		buildTotals();

		// Build main table
		String header = null;
		if (displayHeader)
		    header = "Reservations";

		WebTable mainTbl = new WebTable(1, header, null, null, null);

		// Add each reservation table as a row to the main table

	    if (individualResvTbl != null) {
	        mainTbl.addLine(ocIndiv, new String[] { createTable(individualResvTbl.printTable(0), "margin:0;", null, null)  }, null);
	    }

	    if (courseOffrResvTbl != null) {
	        mainTbl.addLine(ocCourse, new String[] { createTable(courseOffrResvTbl.printTable(0), "margin:0;", null, null) }, null);
	    }

	    if (acadAreaResvTbl != null) {
	        mainTbl.addLine(ocAcadArea, new String[] { createTable(acadAreaResvTbl.printTable(0), "margin:0;", null, null) }, null);
	    }

	    if (posResvTbl != null) {
	        mainTbl.addLine(ocPos, new String[] { createTable(posResvTbl.printTable(0), "margin:0;", null, null) }, null);
	    }

	    if (stuGroupResvTbl != null) {
	        mainTbl.addLine(ocStuGrp, new String[] { createTable(stuGroupResvTbl.printTable(0), "margin:0;", null, null) }, null);
	    }

	    reset();
	    return mainTbl.printTable();
    }


    /**
     * Generate html table for reservations given a subject area and course number (optional)
     * @param user
     * @param subjectAreaId
     * @param courseNbr
     * @param displayIo flag indicating whether IO reservations are to be displayed
     * @param displayConfig flag indicating whether Config reservations are to be displayed
     * @param displayClass flag indicating whether Class reservations are to be displayed
     * @param displayCourse flag indicating whether Course Offering reservations are to be displayed
     * @param includeIndividual include individual reservations
     * @param includeStuGroup include student group reservations
     * @param includeAcadArea include academic area reservations
     * @param includePos include pos reservations
     * @param includeCourse include course reservations
     * @return
     */
    public String htmlTableForSubjectArea(
            User user,
            String subjectAreaId,
            String courseNbr,
            boolean displayIo,
            boolean displayConfig,
            boolean displayClass,
            boolean displayCourse,
            boolean includeIndividual,
            boolean includeStuGroup,
            boolean includeAcadArea,
            boolean includePos,
            boolean includeCourse ) {

		//Build Main Table
		WebTable mainTbl = new WebTable(1, "", new String[] {""}, new String[] {"left"}, null);
		mainTbl.enableHR("#ABABAB");
		mainTbl.setSuppressRowHighlight(true);

        if (subjectAreaId==null || subjectAreaId.length()==0) {
		    mainTbl.addLine(null, new String[] { "Invalid Subject Area" }, null);
		    return mainTbl.printTable();
        }

        Set instrOfferings = getInstructionalOfferings(subjectAreaId, courseNbr);
		if (instrOfferings==null || instrOfferings.size()==0) {
		    return null;
		}

		// Iterate through instr offerings, configs and classes
		boolean found = false;

		for (Iterator iterIo=instrOfferings.iterator(); iterIo.hasNext(); ) {
		    InstructionalOffering io = (InstructionalOffering) iterIo.next();
		    if (htmlTableForInstructionalOffering(
		            user, io, mainTbl, displayIo, displayConfig, displayClass, displayCourse, true,
		            includeIndividual, includeStuGroup, includeAcadArea, includePos, includeCourse ))
		        found = true;
		}

		if (!found) {
		    return null;
		}

		reset();
		return mainTbl.printTable();
    }


    /**
     * Build reservations table for an instructional offering
     * Includes all config and class reservations as well
     * @param user
     * @param io
     * @param table WebTable object
     * @param displayIo flag indicating whether IO reservations are to be displayed
     * @param displayConfig flag indicating whether Config reservations are to be displayed
     * @param displayClass flag indicating whether Class reservations are to be displayed
     * @param displayCourse flag indicating whether Course Offering reservations are to be displayed
     * @param displayIoHeader flag indicating whether Instructional Offering label is displayed
     * @param includeIndividual include individual reservations
     * @param includeStuGroup include student group reservations
     * @param includeAcadArea include academic area reservations
     * @param includePos include pos reservations
     * @param includeCourse include course reservations
     * @return
     */
    private boolean htmlTableForInstructionalOffering(
            User user,
            InstructionalOffering io,
            WebTable table,
            boolean displayIo,
            boolean displayConfig,
            boolean displayClass,
            boolean displayCourse,
            boolean displayIoHeader,
            boolean includeIndividual,
            boolean includeStuGroup,
            boolean includeAcadArea,
            boolean includePos,
            boolean includeCourse) {

		WebTable ioTbl = new WebTable(1, null, null, null, null);
	    ioTbl.setSuppressRowHighlight(true);

		reset();
	    boolean ioHeader = false;
	    boolean found = false;
	    String ioResvTbl = null;
	    Collection ioResvs = io.getReservations(includeIndividual, includeStuGroup, includeAcadArea, includePos, includeCourse);

	    if (displayIo)
	        ioResvTbl = htmlTableForReservations(ioResvs, false, io.isEditableBy(user), io.getControllingCourseOffering().isLimitedEditableBy(user));

	    if (ioResvTbl!=null) {
	        if (displayIoHeader)
	            ioTbl.addLine(null, new String[] { "<A name='" + io.getUniqueId().toString() + "'><FONT class='ReservationRowHead'>"+io.getCourseNameWithTitle()+ " <FONT class='resvIoOfferLimit'>(" + (io.getLimit()!=null ? io.getLimit().toString() : "limit not available") + ")</FONT></FONT></A>" }, null);

	        // Create Total row
			buildTotals();

	        ioTbl.addLine(null, new String[] { createTable(ioResvTbl, "margin:0;", "100%", null) }, null);
	        ioHeader = true;
	        found = true;
	    }

        //TODO Reservations functionality to be removed later
	    if (displayCourse) {
	        // Loop through Course Offering
	        Vector courseOfferings = new Vector(io.getCourseOfferings());
	        Collections.sort(courseOfferings, new CourseOfferingComparator(CourseOfferingComparator.COMPARE_BY_SUBJ_CRS));

	        for (Iterator iterCourses=courseOfferings.iterator(); iterCourses.hasNext(); ) {
		        reset();
	            CourseOffering co = (CourseOffering) iterCourses.next();
		        String coResvTbl = null;

		        coResvTbl = htmlTableForReservations(co.effectiveReservations(includeAcadArea), false, co.isEditableBy(user), co.isLimitedEditableBy(user));
			    if (coResvTbl!=null) {
				    if (!ioHeader) {
				        if (displayIoHeader)
				            ioTbl.addLine("", new String[] { "<A name='" + io.getUniqueId().toString() + "'><FONT class='ReservationRowHead'>"+io.getCourseNameWithTitle()+ " <FONT class='resvIoOfferLimit'>(" + (io.getLimit()!=null ? io.getLimit().toString() : "limit not available") + ")</FONT></FONT></A>" }, null);
				        ioHeader = true;
				    }

				    if (io.getCourseOfferings().size()>1) {
				    	String courseResvLimit = "";
					    Collection ioResvs2 = io.getReservations(false, false, false, false, true);
				    	if (ioResvs2!=null && ioResvs2.size()>0) {
				    		for (Iterator it1=ioResvs2.iterator(); it1.hasNext(); ) {
				    			Object o = it1.next();
				    			if (o instanceof CourseOfferingReservation) {
				    				CourseOfferingReservation cor = (CourseOfferingReservation) o;
				    				if (cor.getCourseOffering().equals(co)) {
				    					courseResvLimit = cor.getReserved().toString();
				    					break;
				    				}
				    			}
				    		}
				    	}
				    	ioTbl.addLine(null, new String[] { "<FONT class='ReservationRowHead' style='margin-left:15px;'>" + co.getCourseName() + " <FONT class='resvIoOfferLimit'>(" + courseResvLimit + ")</FONT></FONT>" }, null);
				    }

			        // Create Total row
					buildTotals();

			        ioTbl.addLine(null, new String[] { createTable(coResvTbl, "margin-left:15px;", "100%", null) }, null);
			        found = true;
			    }
	        }
	    }
        // End Bypass

	    // Check if filter set for displaying config and class reservations
	    if(displayConfig || displayClass) {

		    reset();
		    boolean cfgHeader = false;

		    Set configs = io.getInstrOfferingConfigs();
		    for (Iterator iterCfg= configs.iterator(); iterCfg.hasNext(); ) {
		        InstrOfferingConfig config = (InstrOfferingConfig) iterCfg.next();
		        String cfgResvTbl = null;
			    cfgHeader = false;

			    if (displayConfig)
			        cfgResvTbl = htmlTableForReservations(
			                config.getReservations(includeIndividual, includeStuGroup, includeAcadArea, includePos, includeCourse),
			                false, config.isEditableBy(user), config.getControllingCourseOffering().isLimitedEditableBy(user));

			    if (cfgResvTbl!=null) {
				    if (!ioHeader) {
				        if (displayIoHeader)
				            ioTbl.addLine("", new String[] { "<A name='" + io.getUniqueId().toString() + "'><FONT class='ReservationRowHead'>"+io.getCourseNameWithTitle()+ " <FONT class='resvIoOfferLimit'>(" + (io.getLimit()!=null ? io.getLimit().toString() : "limit not available") + ")</FONT></FONT></A>" }, null);
				        ioHeader = true;
				    }

			        ioTbl.addLine(null, new String[] { "<FONT class='ReservationRowHead' style='margin-left:30px;'>Configuration " + config.getName() + "</FONT>" }, null);

			        // Create Total row
					buildTotals();

			        ioTbl.addLine(null, new String[] { createTable(cfgResvTbl, "margin-left:30px;", "100%", null) }, null);
			        cfgHeader = true;
			        found = true;
			    }

			    reset();
			    if(!displayClass)
			        continue;

			    Vector subparts = new Vector (config.getSchedulingSubparts());
			    Collections.sort(subparts, new SchedulingSubpartComparator());

			    for (Iterator iterSubpart= subparts.iterator(); iterSubpart.hasNext(); ) {

			        SchedulingSubpart subpart = (SchedulingSubpart) iterSubpart.next();
			        Vector classes = new Vector(subpart.getClasses());
			        Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_ITYPE));

			        for (Iterator iterClasses=classes.iterator(); iterClasses.hasNext(); ) {
			            Class_ cls = (Class_) iterClasses.next();
				        String clsResvTbl = htmlTableForReservations(
				                	cls.getReservations(includeIndividual, includeStuGroup, includeAcadArea, includePos, includeCourse),
				                	false, cls.isEditableBy(user), cls.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering().isLimitedEditableBy(user));

					    if (clsResvTbl!=null) {
						    if (!ioHeader) {
						        if (displayIoHeader)
						            ioTbl.addLine("", new String[] { "<A name='" + io.getUniqueId().toString() + "'><FONT class='ReservationRowHead'>"+io.getCourseNameWithTitle()+ " <FONT class='resvIoOfferLimit'>(" + (io.getLimit()!=null ? io.getLimit().toString() : "limit not available") + ")</FONT></FONT></A>" }, null);
						        ioHeader = true;
						    }
						    if (!cfgHeader && configs.size()>1) {
						        ioTbl.addLine(null, new String[] { "<FONT class='ReservationRowHead' style='margin-left:30px;'>Configuration " + config.getName() + "</FONT>" }, null);
						        cfgHeader = true;
						    }

							// Create Total row
							buildTotals();

					        ioTbl.addLine(null, new String[] { "<FONT class='ReservationRowHead' style='margin-left:45px;'>" + cls.getItypeDesc().trim() + " " + cls.getSectionNumberString() + "</FONT>" }, null);
					        ioTbl.addLine(null, new String[] { createTable(clsResvTbl, "margin-left:45px;", "100%", null) }, null );
					        found = true;
					    }
			        }
			    }
		    }

	    }

	    if (ioHeader) {
	        table.addLine(null, new String[] { createTable(ioTbl.printTable(), "margin:0;", null, null) }, null);
	    }

        return found;
    }

    /**
     * Build reservations table for an instructional offering
     * Includes all config and class reservations as well
     * @param user
     * @param io
     * @param displayIo flag indicating whether IO reservations are to be displayed
     * @param displayConfig flag indicating whether Config reservations are to be displayed
     * @param displayClass flag indicating whether Class reservations are to be displayed
     * @param displayCourse flag indicating whether Course Offering reservations are to be displayed
     * @param includeIndividual include individual reservations
     * @param includeStuGroup include student group reservations
     * @param includeAcadArea include academic area reservations
     * @param includePos include pos reservations
     * @param includeCourse include course reservations
     * @return null if no reservations found, reservations table html string otherwise
     */
    public String htmlTableForInstructionalOffering(
            User user,
            InstructionalOffering io,
            boolean displayIo,
            boolean displayConfig,
            boolean displayClass,
            boolean displayCourse,
            boolean includeIndividual,
            boolean includeStuGroup,
            boolean includeAcadArea,
            boolean includePos,
            boolean includeCourse ) {

		//Build Main Table
		WebTable mainTbl = new WebTable(1, "Reservations", new String[] {""}, new String[] {"left"}, null);
		mainTbl.enableHR("#ABABAB");
		mainTbl.setSuppressRowHighlight(true);

        boolean found = htmlTableForInstructionalOffering(
                user, io, mainTbl, displayIo, displayConfig, displayClass, displayCourse, false,
                includeIndividual, includeStuGroup, includeAcadArea, includePos, includeCourse );

        if (!found)
            return null;

        reset();
        return mainTbl.printTable();
    }


	/**
	 * Retrieves the instr offering for a given subject area and course number pattern
	 * @param subjectAreaId
	 * @param courseNbr
	 * @return null if none found
	 */
	protected Set getInstructionalOfferings(String subjectAreaId, String courseNbr) {

		Set instrOfferings = null;
		org.hibernate.Session hibSession = (new InstructionalOfferingDAO()).getSession();

	    StringBuffer query = new StringBuffer("");
		query.append("select distinct io ");
		query.append(" from InstructionalOffering as io inner join io.courseOfferings as co  ");
		query.append(" where io.notOffered=0 and co.subjectArea.uniqueId = :subjectAreaId ");

		if (courseNbr!=null && courseNbr.trim().length()!=0) {
		    query.append(" and co.courseNbr ");
		    if (courseNbr.indexOf('*')>=0) {
	            query.append(" like ");
	            courseNbr = courseNbr.replace('*', '%');
		    }
		    else {
	            query.append(" = ");
		    }
            query.append(":courseNbr");
		}

		Query q = hibSession.createQuery(query.toString());
		q.setInteger("subjectAreaId", Integer.parseInt(subjectAreaId));
		if (courseNbr!=null && courseNbr.trim().length()!=0) {
			q.setString("courseNbr", courseNbr.toUpperCase());
		}
		q.setCacheable(true);

		List l = q.list();
		if (l!=null && l.size()>0) {
		    instrOfferings = new TreeSet(new InstructionalOfferingComparator(Long.valueOf(subjectAreaId)));
		    instrOfferings.addAll(l);
		}

		return instrOfferings;
	}

	protected void finalize() throws Throwable {
        reset();
        super.finalize();
    }
}
