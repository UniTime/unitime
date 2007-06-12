/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.webutil.pdf;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.unitime.commons.User;
import org.unitime.timetable.ApplicationProperties;
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
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.webutil.PdfWebTable;
import org.unitime.timetable.webutil.ReservationsTableBuilder;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Graphic;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;


/**
 * Exports reservations to PDF 
 * 
 * @author Heston Fernandes
 */
public class PdfReservationsTableBuilder extends ReservationsTableBuilder {
	
	/** Pdf document objects **/
    private Document pdfDoc = null;
    private PdfWriter pdfWriter = null;

    /** Colors to indicate and increase/decrease in actual reserved from requested **/
    private final String colorIncrease = "339933";
    private final String colorDecrease = "CC3333";

    /** Header Labels **/
    final String headerF = "@@ITALIC @@COLOR ABABAB ";
    final String headerE = " @@END_ITALIC ";
    
    final String LBL_RESERVED = headerF + "Resv" + headerE;
    final String LBL_PROJECTED = headerF + "Proj" + headerE;
    final String LBL_REQUESTED = headerF + "Req" + headerE;
    final String LBL_LAST_TERM = headerF + "L Term" + headerE;
    final String LBL_PRIORITY = headerF + "Priority" + headerE;
    final String LBL_TYPE = headerF + "Type" + headerE;
    final String LBL_EXPDATE = headerF + "Exp Date" + headerE;
    final String LBL_OVERLIMIT = headerF + "Over Limit" + headerE;
    final String LBL_CLASSIF = "Acad Class";
    
    
    /**
     * Generate pdf for reservations given a subject area and course number (optional)
     * @request request object
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
	public boolean pdfTableForSubjectArea(
			HttpServletRequest request, 
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
            boolean includeCourse ) throws Exception {
        
        FileOutputStream out = null;
		boolean result = false;

        try {
        	// Open pdf file
	        File file = ApplicationProperties.getTempFile("reservation", "pdf");
	        out = new FileOutputStream(file);
	        
	        // Create document with letter size pages 
	        pdfDoc = new Document();
	        pdfDoc.setPageSize(PageSize.LETTER);
	        
	        // Create writer instance
	        pdfWriter = PdfEventHandler.initFooter(pdfDoc, out);
	        
	        // Set metadata
	        pdfDoc.addTitle("Reservations");
	        pdfDoc.addSubject("Timetabling");
	        pdfDoc.addCreator("Timeatbling Web Application");
	        
	        // Open document
	        pdfDoc.open();
	
	        
			this.pdfDoc.add(new Paragraph("Reservations\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
	
	    	Set instrOfferings = super.getInstructionalOfferings(subjectAreaId, courseNbr);
			if (instrOfferings==null || instrOfferings.size()==0) {
				this.pdfDoc.add(new Paragraph("There are no reservations for this subject area / course number.", FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, Color.RED)));
			    result = true;
			}
			else {
				
		    	// Iterate through instr offerings, configs and classes
				for (Iterator iterIo=instrOfferings.iterator(); iterIo.hasNext(); ) {
				    InstructionalOffering io = (InstructionalOffering) iterIo.next();
				    if (pdfTableForInstructionalOffering(
				            user, io, displayIo, displayConfig, displayClass, displayCourse, true,
				            includeIndividual, includeStuGroup, includeAcadArea, includePos, includeCourse )) {
				    	result = true;				    	
				    }
				}	
				
				reset();
				result = true;
			}
			
			// Close pdf document
			pdfDoc.close();
			
			// Set pdf name file as attribute 
	      	request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
	      	
	      	return result;			
        }
        finally {
        	try {
            	if (out!=null) out.close();
            } 
            catch (IOException e) {}
        }
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
     * @param displayIoHeader flag indicating whether Instructional Offering label is displayed
     * @param includeIndividual include individual reservations
     * @param includeStuGroup include student group reservations
     * @param includeAcadArea include academic area reservations
     * @param includePos include pos reservations
     * @param includeCourse include course reservations
     * @return
     */
    private boolean pdfTableForInstructionalOffering(
            User user,
            InstructionalOffering io, 
            boolean displayIo,
            boolean displayConfig,
            boolean displayClass, 
            boolean displayCourse,
            boolean displayIoHeader,
            boolean includeIndividual,
            boolean includeStuGroup,
            boolean includeAcadArea,
            boolean includePos,
            boolean includeCourse) throws Exception {
        
    	int ioIndent = 0;
    	int crsIndent = 15;
    	int cfgIndent = 30;
    	int clsIndent = 45;
    	
		reset();
	    boolean ioHeader = false;
	    boolean found = false;
	    Collection ioResvs = io.getReservations(includeIndividual, includeStuGroup, includeAcadArea, includePos, includeCourse);
	    
	    if (ioResvs!=null && ioResvs.size()>0) {
	        if (displayIoHeader) {
	    		pdfDoc.add(getHeader(io.getCourseNameWithTitle(), io.getLimit(), ioIndent));
	        }

	        // Generate pdf table
	        pdfTableForReservations(ioResvs, crsIndent);
	        
	        // Create Total row
			//pdfBuildTotals();
			
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
	            Collection coResvs = co.effectiveReservations(includeAcadArea);
	            
		        if (coResvs!=null && coResvs.size()>0) {
				    if (!ioHeader) {
				        if (displayIoHeader)
				    		pdfDoc.add(getHeader(io.getCourseNameWithTitle(), io.getLimit(), ioIndent));
				        ioHeader = true;
				    }
				    
				    if (io.getCourseOfferings().size()>1) {
				    	Integer courseResvLimit = null;
					    Collection ioResvs2 = io.getReservations(false, false, false, false, true);
				    	if (ioResvs2!=null && ioResvs2.size()>0) {
				    		for (Iterator it1=ioResvs2.iterator(); it1.hasNext(); ) {
				    			Object o = it1.next();
				    			if (o instanceof CourseOfferingReservation) {
				    				CourseOfferingReservation cor = (CourseOfferingReservation) o;
				    				if (cor.getCourseOffering().equals(co)) {
				    					courseResvLimit = cor.getReserved();
				    					break;
				    				}
				    			}
				    		}
				    	}
				    	
			    		pdfDoc.add(getHeader(co.getCourseName(), courseResvLimit, crsIndent));
				    }
	
			        pdfTableForReservations(coResvs, crsIndent);

			        // Create Total row
					//pdfBuildTotals();
					
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
			    cfgHeader = false;
			    Collection cfgResvs = config.getReservations(includeIndividual, includeStuGroup, includeAcadArea, includePos, includeCourse);
			    
			    if (displayConfig && cfgResvs!=null && cfgResvs.size()>0) {
				    if (!ioHeader) {
				        if (displayIoHeader)
				    		pdfDoc.add(getHeader(io.getCourseNameWithTitle(), io.getLimit(), ioIndent));
				        ioHeader = true;
				    }
				    
		    		pdfDoc.add(getHeader("Configuration " + config.getName(), null, cfgIndent));
			        pdfTableForReservations( cfgResvs, cfgIndent);

			        // Create Total row
					//pdfBuildTotals();
					
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
			            Collection clsResvs = cls.getReservations(includeIndividual, includeStuGroup, includeAcadArea, includePos, includeCourse);
			            
					    if (clsResvs!=null && clsResvs.size()>0) {
						    if (!ioHeader) { 
						        if (displayIoHeader)
						    		pdfDoc.add(getHeader(io.getCourseNameWithTitle(), io.getLimit(), ioIndent));
						        ioHeader = true;
						    }
						    
						    if (!cfgHeader && configs.size()>1) { 
					    		pdfDoc.add(getHeader("Configuration " + config.getName(), new Integer(-1), cfgIndent));
						        cfgHeader = true;
						    }
						    
				    		pdfDoc.add(getHeader(cls.getItypeDesc().trim() + " " + cls.getSectionNumberString(), new Integer(-1), clsIndent+20));
					        pdfTableForReservations(clsResvs, clsIndent);

					        // Create Total row
							//pdfBuildTotals();
							
					        found = true;
					    }
			        }	        
			    }		        
		    }
		    
	    }
	    
	    if (found) {
	    	Graphic g = new Graphic();
	    	g.setHorizontalLine(1, 100, new Color(200, 200, 200));
	    	Paragraph p1 = new Paragraph();
	    	p1.setIndentationLeft(-20);
	    	p1.add("\n");
	    	p1.add(g);
	    	p1.add("\n");
	    	pdfDoc.add(p1);
	    }    	
        return found; 
    }


    /**
     * Generate pdf table for reservations
     * @param reservations
     * @param margin margin to indent tables
     */
    public void pdfTableForReservations(
    		Collection reservations, 
    		int margin ) throws Exception {
        
        if (reservations==null || reservations.size()==0)
            return;
        
       // Build sub tables
		for (Iterator i=reservations.iterator(); i.hasNext();) {
		    Reservation resv = (Reservation) i.next();

		    if (resv instanceof IndividualReservation) {
		        pdfBuildIndividualResvRow((IndividualReservation) resv);
		    }
		    
		    if (resv instanceof StudentGroupReservation) {
		        pdfBuildStuGroupResvRow((StudentGroupReservation) resv);
		    }

		    if (resv instanceof AcadAreaReservation) {
		        pdfBuildAcadAreaResvRow((AcadAreaReservation) resv);
		    }

		    if (resv instanceof PosReservation) {
		        pdfBuildPosResvRow((PosReservation) resv);
		    }

		    if (resv instanceof CourseOfferingReservation) {
		        pdfBuildCourseOffrResvRow((CourseOfferingReservation) resv);
		    }
		}

		// Create Total row
		pdfBuildTotals();

		Paragraph p = null;
		
		// Add each reservation table 
	    if (individualResvTbl != null) {
			p = new Paragraph();
			p.setIndentationLeft(margin);
			p.add(((PdfWebTable)individualResvTbl).printPdfTable(0,true));
	    	pdfDoc.add( p );
	    }
	    
	    if (courseOffrResvTbl != null) {
			p = new Paragraph();
			p.setIndentationLeft(margin);
			p.add(((PdfWebTable)courseOffrResvTbl).printPdfTable(0,true));
	    	pdfDoc.add( p );
	    }
	    
	    if (acadAreaResvTbl != null) {
			p = new Paragraph();
			p.setIndentationLeft(margin);
			p.add(((PdfWebTable)acadAreaResvTbl).printPdfTable(0,true));
	    	pdfDoc.add( p );
	    }
	    
	    if (posResvTbl != null) {
			p = new Paragraph();
			p.setIndentationLeft(margin);
			p.add(((PdfWebTable)posResvTbl).printPdfTable(0,true));
	    	pdfDoc.add( p );
	    }
	    
	    if (stuGroupResvTbl != null) {
			p = new Paragraph();
			p.setIndentationLeft(margin);
			p.add(((PdfWebTable)stuGroupResvTbl).printPdfTable(0,true));
	    	pdfDoc.add( p );
	    }
	    
	    reset();
	    return;
    }
    

    /**
     * Add to Individual Reservation Table
     * @param resv
     */
    private void pdfBuildIndividualResvRow(IndividualReservation resv) {
        if (individualResvTbl==null) {
            irTotal = 0;
            individualResvTbl = new PdfWebTable (
                    5, "", 
                    new String[] {"Individual", nowrap(LBL_TYPE), nowrap(LBL_PRIORITY), nowrap(LBL_EXPDATE), nowrap(LBL_OVERLIMIT) },
                    new String[] {"left", "left", "center", "center", "center"},
                    new boolean[] {true, true, true, true, true} );
        }

        String overLimit =  resv.isOverLimit().booleanValue() 
								? "Yes"
								: " "; 

        individualResvTbl.addLine(
                null, 
                new String[] { 	resv.getExternalUniqueId(),
                				Constants.toInitialCase(resv.getReservationType().getReference()), 
                        		resv.getPriority().toString(), 
                        		new SimpleDateFormat("MM/dd/yyyy").format(resv.getExpirationDate()),
                        		overLimit },
                new Comparable[] {resv.getExternalUniqueId(), "", "", "", "", "" } );
        ++irTotal;
    }
    

    /**
     * Add to Student Group Reservation Table
     * @param resv
     */
    private void pdfBuildStuGroupResvRow(StudentGroupReservation resv) {
        if (stuGroupResvTbl==null) {
           sgrTotals = new int[] {0, 0, 0};
           stuGroupResvTbl = new PdfWebTable (
                    6, "", 
                    new String[] {"Student Group", nowrap(LBL_TYPE), nowrap(LBL_PRIORITY), nowrap(LBL_RESERVED), nowrap(LBL_PROJECTED), nowrap(LBL_LAST_TERM)},
                    new String[] {"left", "left", "center", "right", "right", "right"},
                    new boolean[] {true, true, true, true, true, true} );
        }
        
        stuGroupResvTbl.addLine(
                null, 
                new String[] { 	resv.getStudentGroup().getGroupName(),
                				Constants.toInitialCase(resv.getReservationType().getReference()), 
                        		resv.getPriority().toString(), 
                        		resv.getReserved().toString(),
                        		resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().toString() : " ", 
                        		resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().toString() : " " }, 
                new Comparable[] {resv.getStudentGroup().getGroupName(), "", "", "", "", "" } );
        
        sgrTotals[0] += resv.getReserved().intValue();
        sgrTotals[1] += resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().intValue() : 0;
        sgrTotals[2] += resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().intValue() : 0;
    }

    
    /**
     * Add to Academic Area Reservation Table
     * @param resv
     */
    private void pdfBuildAcadAreaResvRow(AcadAreaReservation resv) {
        if (acadAreaResvTbl==null) {
            aarTotals = new int[] {0, 0, 0, 0};
            acadAreaResvTbl = new PdfWebTable (
                    8, "", 
                    new String[] {"Academic Area", " ", nowrap(LBL_TYPE), " ", nowrap(LBL_RESERVED), nowrap(LBL_REQUESTED), nowrap(LBL_PROJECTED), nowrap(LBL_LAST_TERM)},
                    new String[] {"left", "left", "left", "center", "right", "right", "right", "right"},
                    new boolean[] {true, true, true, true, true, true, true, true} );
        }
        
        String acadArea = ((AcadAreaReservation)resv).getAcademicArea().getShortTitle();
        String acadClassification = resv.getAcademicClassification()!=null 
        								? resv.getAcademicClassification().getName()
        							    : " ";
        acadAreaResvTbl.addLine(
                null, 
                new String[] { 	acadArea,
                        		" ",
                        		Constants.toInitialCase(resv.getReservationType().getReference()), 
                        		" ",
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
     */
    private void pdfBuildPosResvRow(PosReservation resv) {
        if (posResvTbl==null) {
            prTotals = new int[] {0, 0, 0};
            posResvTbl = new PdfWebTable (
                    7, "", 
                    new String[] {"POS Major", LBL_CLASSIF, nowrap(LBL_TYPE), nowrap(LBL_PRIORITY), nowrap(LBL_RESERVED), nowrap(LBL_PROJECTED), nowrap(LBL_LAST_TERM)},
                    new String[] {"left", "left", "left", "center", "right", "right", "right"},
                    new boolean[] {true, true, true, true, true, true, true} );
        }
        
        String posMajor = ((PosReservation)resv).getPosMajor().getName();
        String acadClassification = resv.getAcademicClassification()!=null 
        								? resv.getAcademicClassification().getName() 
        							    : " ";
        								
        posResvTbl.addLine(
                null, 
                new String[] { 	posMajor, 
                        		acadClassification,
                        		Constants.toInitialCase(resv.getReservationType().getReference()), 
                        		resv.getPriority().toString(), 
                        		resv.getReserved().toString(),
                        		resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().toString() : " ", 
                        		resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().toString() : " " }, 
               new Comparable[] {posMajor, acadClassification, "", "", "", "", "" } );
        
        prTotals[0] += resv.getReserved().intValue();
        prTotals[1] += resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().intValue() : 0;
        prTotals[2] += resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().intValue() : 0;
    }

    
    /**
     * Add to Course Offering Reservation Table
     * @param resv
     */
    private void pdfBuildCourseOffrResvRow(CourseOfferingReservation resv) {
        boolean clsOwner = false;
        if (resv.getOwnerClassId().equals(Constants.RESV_OWNER_CLASS))
        	clsOwner = true;

        if (courseOffrResvTbl==null) {
            corTotals = clsOwner ? null : new int[] {0, 0, 0, 0};
            courseOffrResvTbl = new PdfWebTable (
                   7, "", 
                   clsOwner 
                   	? null
                   	: new String[] {"Course", " ", " ", nowrap(LBL_RESERVED), nowrap(LBL_REQUESTED), nowrap(LBL_PROJECTED), nowrap(LBL_LAST_TERM)},
                   new String[] {"left", "left", "center", "right", "right", "right", "right"},
                   new boolean[] {true, true, true, true, true, true, true} );
       }
       
       courseOffrResvTbl.addLine(
               null, 
               new String[] { resv.getCourseName(), 
                       		  " ", 
                       		  " ",
                       		  clsOwner ? " ": getResvString(resv.getRequested(), resv.getReserved()),
                       		  clsOwner ? " ": ( resv.getRequested()!=null ? resv.getRequested().toString() : "-" ), 
                       		  clsOwner ? " ": ( resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().toString() : "-" ), 
                       		  clsOwner ? " ": ( resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().toString() : "-" ) }, 
               new Comparable[] {resv.getCourseName(), "", "", "", "", "" } );
       
       if (corTotals!=null) {
	       corTotals[0] += resv.getReserved().intValue();
	       corTotals[1] += resv.getProjectedEnrollment()!=null ? resv.getProjectedEnrollment().intValue() : 0;
	       corTotals[2] += resv.getPriorEnrollment()!=null ? resv.getPriorEnrollment().intValue() : 0;
	       corTotals[3] += resv.getRequested()!=null ? resv.getRequested().intValue() : 0;
       }
    }

    
    /**
     * Add total row to the individual reservation tables
     */
    private void pdfBuildTotals() {
        if (individualResvTbl!=null && irTotal>0) {
            individualResvTbl.addLine(
                    null, new String[] { bold(underline("Total:")) + irTotal, " ", " ", " ", " " }, null );
        }
        
        if (acadAreaResvTbl!=null && aarTotals!=null) {
            acadAreaResvTbl.addLine(
                    null, new String[] { " ", " ", " ", " ", "@@BORDER_TOP 666666 "+ bold(getResvString(new Integer(aarTotals[3]), new Integer(aarTotals[0]))), "@@BORDER_TOP 666666 "+ bold(""+aarTotals[3]), "@@BORDER_TOP 666666 "+ bold(""+aarTotals[1]), "@@BORDER_TOP 666666 "+ bold(""+aarTotals[2]) }, null );
        }
        
        if (courseOffrResvTbl!=null && corTotals!=null) {
            courseOffrResvTbl.addLine(
                    null, new String[] { " ", " ", " ", "@@BORDER_TOP 666666 "+ bold(getResvString(new Integer(corTotals[3]), new Integer(corTotals[0]))), "@@BORDER_TOP 666666 "+ bold(""+corTotals[3]), "@@BORDER_TOP 666666 "+ bold(""+corTotals[1]), "@@BORDER_TOP 666666 "+ bold(""+corTotals[2]) }, null );
        }
        
        if (posResvTbl!=null && prTotals!=null) {
            posResvTbl.addLine(
                    null, new String[] { " ", " ", " ", " ", "@@BORDER_TOP 666666 "+ bold(""+prTotals[0]), "@@BORDER_TOP 666666 "+ bold(""+prTotals[1]), "@@BORDER_TOP 666666 "+ bold(""+prTotals[2]) }, null );
        }
        
        if (stuGroupResvTbl!=null && sgrTotals!=null) {
            stuGroupResvTbl.addLine(
                    null, new String[] { " ", " ", " ", " ", "@@BORDER_TOP 666666 "+ bold(""+sgrTotals[0]), "@@BORDER_TOP 666666 "+ bold(""+sgrTotals[1]), "@@BORDER_TOP 666666 "+ bold(""+sgrTotals[2]) }, null );
        }
    }
    
    /**
     * Color format reservation total
     * @param oRequest
     * @param oReserved
     * @return
     */
    private String getResvString(Integer oRequest, Integer oReserved ) {
        String ret = null;								
        if (oRequest!=null) {
            int res = oReserved.intValue();
            int req = oRequest.intValue();
            
            if (res<req) 
            	ret = color(""+res, colorDecrease);
            
            else 
            	if (res>req) ret = color(""+res, colorIncrease);
            
            else 
            	ret = "" + res;
        }
        else {
            ret = color(oReserved.toString(), colorIncrease);
        }
        return ret;
    }

    /**
     * Format header with offering/course limit 
     * @param name header string
     * @param limit null if no offering/course limit
     * @param indent margin to indent header
     * @return
     */
    private Paragraph getHeader(String name, Integer limit, int indent) {

    	String limitStr = " ";
    	if (limit==null) {
    		limitStr = " ( limit not available )";
    	}
    	else {
    		if (limit.intValue()>=0)
        		limitStr = " ( " + limit.toString() + " )";
    	}
    	
    	Font black = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.BOLD, new Color(0x00, 0x00, 0x00));
    	Chunk c1 = new Chunk(name, black);
		
    	Font navyBlue = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.BOLD, new Color(0x00, 0x00, 0x80));
    	Chunk c2 = new Chunk( limitStr, navyBlue );
    	
    	Paragraph p = new Paragraph(c1);
    	p.add(c2);
    	p.setIndentationLeft(indent);
    	
    	return p;
    }
    
    /**
     * Add bold tag for PdfWebTable to parse
     * @param str
     * @return
     */
    private String bold(String str) {
    	return "@@BOLD " + str + " @@END_BOLD ";
    }
    
    /**
     * Add underline tag for PdfWebTable to parse
     * @param str
     * @return
     */
    private String underline(String str) {
    	return "@@UNDERLINE " + str + " @@END_UNDERLINE ";
    }
    
    /**
     * Add color tag for PdfWebTable to parse
     * @param str
     * @return
     */
    private String color(String str, String color) {
    	return "@@COLOR " + color + " " + str + " @@END_COLOR ";
    }
    
    /**
     * Add nowrap tag for PdfWebTable to parse
     * @param str
     * @return
     */
    private String nowrap(String str) {
    	return "@@NO_WRAP " + str;
    }
    
    /**
     * reset variables
     */
    protected void reset() {
		super.reset();
	}
}
