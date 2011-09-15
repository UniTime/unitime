/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.reports.enrollment;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.StudentClassEnrollmentDAO;
import org.unitime.timetable.reports.PdfLegacyReport;

import com.lowagie.text.DocumentException;

/**
 * @author says
 *
 */
public abstract class PdfEnrollmentAuditReport extends PdfLegacyReport {
    public static Hashtable<String,Class> sRegisteredReports = new Hashtable<String, Class>();
    public static String sAllRegisteredReports = "";
	protected static int studentIdLength = 10;
	protected static int studentNameLength = 23;
	protected static int offeringNameLength = 45;
	protected static int classNameLength = 14;
	protected static int itypeLength = 7;
	protected static int multipleClassesLength = 30;


    static {
        sRegisteredReports.put("struct", EnrollmentsViolatingCourseStructureAuditReport.class);
        sRegisteredReports.put("missing", MissingCourseEnrollmentsAuditReport.class);
        sRegisteredReports.put("many", MultipleCourseEnrollmentsAuditReport.class);
        for (String report : sRegisteredReports.keySet())
            sAllRegisteredReports += (sAllRegisteredReports.length()>0?",":"") + report;
    }

    private Session iSession = null;
    private boolean iShowId;
    private boolean iShowName;
    private TreeSet<SubjectArea> iSubjectAreas;

	/**
	 * @param mode
	 * @param file
	 * @param title
	 * @param title2
	 * @param subject
	 * @param session
	 * @throws IOException
	 * @throws DocumentException
	 */
	public PdfEnrollmentAuditReport(int mode, File file, String title,
			String title2, String subject, String session) throws IOException,
			DocumentException {
		super(mode, file, title, title2, subject, session);
	}

    public PdfEnrollmentAuditReport(int mode, String title, File file, Session session, TreeSet<SubjectArea> subjectAreas, String subTitle) throws DocumentException, IOException {
        super(mode, file, title, subTitle, title + " -- " + session.getLabel(), session.getLabel());
        this.iSession = session;
        this.iSubjectAreas = subjectAreas;
    }

    public PdfEnrollmentAuditReport(int mode, String title, File file, Session session) throws DocumentException, IOException {
    	super(mode, file, title, "", title + " -- " + session.getLabel(), session.getLabel());
        iSession = session;
        this.iSubjectAreas = null;
    }

   public abstract void printReport() throws DocumentException;

/**
 * @return the iSession
 */
public Session getSession() {
	return iSession;
}

/**
 * @param iSession the iSession to set
 */
public void setSession(Session session) {
	this.iSession = session;
}

/**
 * @return the showId
 */
public boolean isShowId() {
	return iShowId;
}

/**
 * @param showId the showId to set
 */
public void setShowId(boolean showId) {
	this.iShowId = showId;
}

/**
 * @return the showName
 */
public boolean isShowName() {
	return iShowName;
}

/**
 * @param showName the showName to set
 */
public void setShowName(boolean showName) {
	this.iShowName = showName;
}

/**
 * @return the subjectAreas
 */
public TreeSet<SubjectArea> getSubjectAreas() {
	return iSubjectAreas;
}

/**
 * @param subjectAreas the subjectAreas to set
 */
public void setSubjectAreas(TreeSet<SubjectArea> subjectAreas) {
	this.iSubjectAreas = subjectAreas;
} 

protected abstract String createQueryString(TreeSet<SubjectArea> subjectAreas);

protected List getAuditResults(TreeSet<SubjectArea> subjectAreas){

	String query = createQueryString(subjectAreas);
	return(StudentClassEnrollmentDAO.getInstance().getQuery(query).setLong("sessId", getSession().getUniqueId().longValue()).list());

}

protected String buildBaseAuditLine(EnrollmentAuditResult result) {
	StringBuilder sb = new StringBuilder();
	if (isShowId()){
		sb.append(" ")
		  .append(lpad(result.getStudentId(), ' ', studentIdLength));
	}
	if (isShowName()){
		sb.append(" ")
		  .append(rpad(result.getStudentName(), ' ', studentNameLength));
	}
	sb.append(" ")
	  .append(rpad(result.getOffering(), ' ', offeringNameLength));
	return(sb.toString());
}

protected String[] getBaseHeader(){
	String[] hdr = new String[3];
	StringBuilder sb0 = new StringBuilder();
	StringBuilder sb1 = new StringBuilder();
	StringBuilder sb2 = new StringBuilder();
	if (isShowId()){
		sb0.append(" ")
		   .append(rpad("", ' ', studentIdLength));
		sb1.append(" ")
		   .append(rpad("Student ID", ' ', studentIdLength));
		sb2.append(" ")
		   .append(rpad("", '-', studentIdLength));
	}
	if (isShowName()){
		sb0.append(" ")
		   .append(rpad("", ' ', studentNameLength));
		sb1.append(" ")
		   .append(rpad("Name", ' ', studentNameLength));
		sb2.append(" ")
		   .append(rpad("", '-', studentNameLength));
	}
	sb0.append(" ")
	   .append(rpad("", ' ', offeringNameLength));
	sb1.append(" ")
	   .append(rpad("Offering", ' ', offeringNameLength));
	sb2.append(" ")
	    .append(rpad("", '-', offeringNameLength));
	
	hdr[0] = sb0.toString();
	hdr[1] = sb1.toString();
	hdr[2] = sb2.toString();

    return(hdr);
}

protected abstract class EnrollmentAuditResult  {
	private String studentId;
	private String studentLastName;
	private String studentFirstName;
	private String studentMiddleName;
	private String subjectArea;
	private String courseNbr;
	private String title;


	public EnrollmentAuditResult(Object[] result) {
		super();
		if (result[0] != null) this.studentId = result[0].toString().trim();
		if (result[1] != null) this.studentLastName = result[1].toString();
		if (result[2] != null) this.studentFirstName = result[2].toString();
		if (result[3] != null) this.studentMiddleName = result[3].toString();
		if (result[4] != null) this.subjectArea = result[4].toString();
		if (result[5] != null) this.courseNbr = result[5].toString();
		if (result[6] != null) this.title = result[6].toString();
	}
	
	public String getStudentName(){
		StringBuilder sb = new StringBuilder();
		if (studentLastName != null && studentLastName.length() > 0) {
			sb.append(studentLastName);
			if (studentFirstName != null && studentFirstName.length() > 0) {
				sb.append(", ")
				  .append(studentFirstName.charAt(0));
				if (studentMiddleName != null && studentMiddleName.length() > 0){
					sb.append(" ")
					  .append(studentMiddleName.charAt(0));
				}
			}
		}
		String name = sb.toString();
		if (name.length() > studentNameLength){
			name = name.substring(0, studentNameLength);
		}
		return(name);
	}

	/**
	 * @return the studentId
	 */
	public String getStudentId() {
		return studentId;
	}
	
	public String getOffering(){
		StringBuilder sb = new StringBuilder();
		sb.append(subjectArea)
		  .append(" ")
		  .append(courseNbr)
		  .append(" - ")
		  .append(title);
		String title = sb.toString();
		if (title.length() > offeringNameLength){
			title = title.substring(0, offeringNameLength);
		}
		return(title);
	}
	
	protected String createClassString(String itypeStr, String nbrStr, String suffixStr){
		StringBuilder sb = new StringBuilder();
		sb.append(itypeStr);
		if (nbrStr.length() != 0){
			sb.append(" ")
			  .append(nbrStr);
			if (!suffixStr.trim().equals("-")){
				sb.append("(")
				  .append(suffixStr)
				  .append(")");
			}
		}
		return(sb.toString());
	}
	
}


}
