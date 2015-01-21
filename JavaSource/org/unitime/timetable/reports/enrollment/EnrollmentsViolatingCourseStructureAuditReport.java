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

package org.unitime.timetable.reports.enrollment;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;

import com.lowagie.text.DocumentException;

/**
 * @author Stephanie Schluttenhofer
 *
 */
public class EnrollmentsViolatingCourseStructureAuditReport extends PdfEnrollmentAuditReport {
	
 
	public EnrollmentsViolatingCourseStructureAuditReport(int mode, File file, Session session, TreeSet<SubjectArea> subjectAreas, String subTitle) throws DocumentException, IOException {
        super(mode, getTitle(), file, session, subjectAreas, subTitle);
    }

    public EnrollmentsViolatingCourseStructureAuditReport(int mode, File file, Session session) throws DocumentException, IOException {
    	super(mode, getTitle(), file, session);
    	setSession(session);
    }

	@Override
	public void printReport() throws DocumentException {
        setHeader(buildHeaderString());
        List results = getAuditResults(getSubjectAreas());
        Vector<String> lines = new Vector<String>();
        Iterator it = results.iterator();
        while(it.hasNext()) {
        	EnrollmentsViolatingCourseStructureAuditResult result = new EnrollmentsViolatingCourseStructureAuditResult((Object[]) it.next());
        	lines.add(buildLineString(result));
        }
        printHeader();
        for (String str : lines) {
                println(str);
        }
        if (!lines.isEmpty()){
        	lastPage();
        }
	}
	
	private String buildLineString(
			EnrollmentsViolatingCourseStructureAuditResult result) {
		StringBuilder sb = new StringBuilder();
		sb.append(buildBaseAuditLine(result));
		sb.append(" ")
		  .append(rpad(result.classString(), ' ', classNameLength))
		  .append(" | ")
		  .append(" ")
		  .append(rpad(result.expectedClassString(), ' ', classNameLength))
		  .append(" ")
		  .append(rpad(result.actualClassString(), ' ', classNameLength));
		return(sb.toString());
	}

	private String[] buildHeaderString(){
		String[] hdr = new String[3];
		StringBuilder sb0 = new StringBuilder();
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		
		String[] baseHdr = getBaseHeader();
		sb0.append(baseHdr[0]);
		sb1.append(baseHdr[1]);
		sb2.append(baseHdr[2]);
				
		sb0.append(" ")
		   .append(rpad("", ' ', classNameLength));
		sb1.append(" ")
		   .append(rpad("Class", ' ', classNameLength));
		sb2.append(" ")
		   .append(rpad("", '-', classNameLength));
		
		sb0.append(" | ");
		sb1.append(" | ");
		sb2.append(" | ");
		
		sb0.append(" ")
		   .append(rpad("Expected", ' ', classNameLength));
		sb1.append(" ")
		   .append(rpad("Parent Class", ' ', classNameLength));
		sb2.append(" ")
		   .append(rpad("", '-', classNameLength));
		
		sb0.append(" ")
		   .append(rpad("", ' ', classNameLength));
		sb1.append(" ")
		   .append(rpad("Error Result", ' ', classNameLength));
		sb2.append(" ")
		   .append(rpad("", '-', classNameLength));

		hdr[0] = sb0.toString();
		hdr[1] = sb1.toString();
		hdr[2] = sb2.toString();
		
		return(hdr);
	}
	
	protected String createQueryString(TreeSet<SubjectArea> subjectAreas){
		StringBuilder sb = new StringBuilder();

		sb.append("select s.externalUniqueId, s.lastName, s.firstName, s.middleName,")
		  .append(" sce.courseOffering.subjectArea.subjectAreaAbbreviation, sce.courseOffering.courseNbr,")
		  .append(" sce.courseOffering.title, sce.clazz.schedulingSubpart.itype.abbv, sce.clazz.sectionNumberCache,")
		  .append(" sce.clazz.schedulingSubpart.schedulingSubpartSuffixCache, c.schedulingSubpart.itype.abbv,")
		  .append(" c.sectionNumberCache, c.schedulingSubpart.schedulingSubpartSuffixCache,")
		  .append(" ( select count(sce1) from StudentClassEnrollment sce1 where sce1.clazz.uniqueId = c.uniqueId and sce1.student.uniqueId = s.uniqueId ),")
		  .append(" ( select distinct sce2.clazz.schedulingSubpart.itype.abbv || ' ' || sce2.clazz.sectionNumberCache || ' ' ||  sce2.clazz.schedulingSubpart.schedulingSubpartSuffixCache")
		  .append(" from StudentClassEnrollment sce2 where sce2.clazz.schedulingSubpart.uniqueId = c.schedulingSubpart.uniqueId and sce2.student.uniqueId = s.uniqueId and sce2.courseOffering.uniqueId = sce.courseOffering.uniqueId )")
		  .append(" from Student s inner join s.classEnrollments as sce, Class_ c")
		  .append(" where s.session.uniqueId = :sessId")
		  .append(" and c.uniqueId = sce.clazz.parentClass.uniqueId");

		if(subjectAreas != null && !subjectAreas.isEmpty()){
			sb.append(" and sce.courseOffering.subjectArea.uniqueId in (");
			boolean first = true;
			for (SubjectArea sa : subjectAreas){
				if (first){
					first = false;
				} else {
					sb.append(",");
				}
				sb.append(" ");
				sb.append(sa.getUniqueId().toString());
			}
			sb.append(" ) ");
		}

		sb.append(" and 1 != ( select count(sce1) from StudentClassEnrollment sce1 where sce1.clazz.uniqueId = c.uniqueId and sce1.student.uniqueId = s.uniqueId )")
		  .append(" order by sce.courseOffering.subjectArea.subjectAreaAbbreviation, sce.courseOffering.courseNbr,")
		  .append(" sce.courseOffering.title, sce.clazz.schedulingSubpart.itype.abbv, sce.clazz.sectionNumberCache,")
		  .append(" sce.clazz.schedulingSubpart.schedulingSubpartSuffixCache");
		if (isShowId()){
			sb.append(", s.externalUniqueId");
		} else if (isShowName()) {
			sb.append(", s.lastName, s.firstName, s.middleName");
		}

		
		return(sb.toString());
	}
		
	private class EnrollmentsViolatingCourseStructureAuditResult extends EnrollmentAuditResult {
		private String itype;
		private String classNbr;
		private String classNbrSuffix;
		private String parentItype;
		private String parentClassNbr;
		private String parentClassNbrSuffix;
		private boolean hasEnrollmentInParentSubpart;
		private String actualParentSupartItype;
		private String actualParentSubpartClassSectionNumber;
		private String actualParentSubpartClassSectionNumberSuffix;


		public EnrollmentsViolatingCourseStructureAuditResult(Object[] result) {
			super(result);
			if (result[7] != null) this.itype = result[7].toString();
			if (result[8] != null) this.classNbr = result[8].toString();
			if (result[9] != null) this.classNbrSuffix = result[9].toString();
			if (result[10] != null) this.parentItype = result[10].toString();
			if (result[11] != null) this.parentClassNbr = result[11].toString();
			if (result[12] != null) this.parentClassNbrSuffix = result[12].toString();
			this.hasEnrollmentInParentSubpart = result[14] != null;
			if (this.hasEnrollmentInParentSubpart){
				this.actualParentSupartItype = result[14].toString().substring(0, result[14].toString().indexOf(' '));
				this.actualParentSubpartClassSectionNumber = result[14].toString().substring((result[14].toString().indexOf(' ')+1), (result[14].toString().indexOf(' ',result[14].toString().indexOf(' ')+1)));
				this.actualParentSubpartClassSectionNumberSuffix = result[14].toString().substring((result[14].toString().indexOf(' ',result[14].toString().indexOf(' ')+1)+1)).trim();
			} else {
				this.actualParentSupartItype = "No Class";
				this.actualParentSubpartClassSectionNumber = "";
				this.actualParentSubpartClassSectionNumberSuffix = "";		
			}
		}
				
		public String classString(){
			return(createClassString(itype, classNbr, classNbrSuffix));
		}
		
		public String expectedClassString(){
			return(createClassString(parentItype, parentClassNbr, parentClassNbrSuffix));
		}
		
		public String actualClassString(){
			return(createClassString(actualParentSupartItype, actualParentSubpartClassSectionNumber, actualParentSubpartClassSectionNumberSuffix));
		}
	}

	public static String getTitle() {
		return ("Enrollments Violating Course Structure");
	}

}
