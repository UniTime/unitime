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
public class MissingCourseEnrollmentsAuditReport extends PdfEnrollmentAuditReport {

    public MissingCourseEnrollmentsAuditReport(int mode, File file, Session session, TreeSet<SubjectArea> subjectAreas, String subTitle) throws DocumentException, IOException {
        super(mode, getTitle(), file, session, subjectAreas, subTitle);
    }

    public MissingCourseEnrollmentsAuditReport(int mode, File file, Session session) throws DocumentException, IOException {
    	super(mode, getTitle(), file, session);
    }


	@Override
	public void printReport() throws DocumentException {
        setHeaderLine(buildHeaderString());
        List results = getAuditResults(getSubjectAreas());
        Vector<Line> lines = new Vector<Line>();
        Iterator it = results.iterator();
        while(it.hasNext()) {
        	MissingCourseEnrollmentsAuditResult result = new MissingCourseEnrollmentsAuditResult((Object[]) it.next());
        	lines.add(buildLineString(result));
        }
        printHeader();
        for (Line str : lines) {
                printLine(str);
        }
        if (!lines.isEmpty()){
        	lastPage();
        }
	}

	public static String getTitle() {
		return MSG.reportMissingCourseEnrollmentsAudit();
	}
	
	private Line buildLineString(MissingCourseEnrollmentsAuditResult result) {
		return new Line(buildBaseAuditLine(result), new Line(
				rpad(result.itypeString(), ' ', itypeLength)
				));
	}

	private Line[] buildHeaderString(){
		Line[] baseHdr = getBaseHeader();
		return new Line[] {
				new Line(baseHdr[0], new Line(
						rpad(MSG.lrMissing(), ' ', itypeLength)
				)),
				new Line(baseHdr[1], new Line(
						rpad(MSG.lrSubpart(), ' ', itypeLength)
				)),
				new Line(baseHdr[2], new Line(
						rpad("", '-', itypeLength)
				))
		};
	}
	
	protected String createQueryString(TreeSet<SubjectArea> subjectAreas){
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct s.externalUniqueId, s.lastName, s.firstName, s.middleName,")
		  .append(" sce.courseOffering.subjectArea.subjectAreaAbbreviation, sce.courseOffering.courseNbr, sce.courseOffering.title,")
		  .append(" ss.itype.abbv,")
		  .append(" ( select count(sce1) from StudentClassEnrollment sce1")
		  .append(" where sce1.clazz.schedulingSubpart.uniqueId = ss.uniqueId and sce1.student.uniqueId = s.uniqueId ) ")
		  .append(" from Student s inner join s.classEnrollments as sce, SchedulingSubpart ss")
		  .append(" where  ss.instrOfferingConfig.uniqueId = sce.clazz.schedulingSubpart.instrOfferingConfig.uniqueId")
		  .append(" and s.session.uniqueId = :sessId");

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

		sb.append(" and 0 = ( select count(sce1) from StudentClassEnrollment sce1")
		  .append(" where sce1.clazz.schedulingSubpart.uniqueId = ss.uniqueId and sce1.student.uniqueId = s.uniqueId )")
		  .append(" order by sce.courseOffering.subjectArea.subjectAreaAbbreviation, sce.courseOffering.courseNbr,")
		  .append(" sce.courseOffering.title, ss.itype.abbv");

		if (isShowId()){
			sb.append(", s.externalUniqueId");
		} else if (isShowName()) {
			sb.append(", s.lastName, s.firstName, s.middleName");
		}

		
		return(sb.toString());
	}

	private class MissingCourseEnrollmentsAuditResult extends EnrollmentAuditResult {
		private String itype;


		public MissingCourseEnrollmentsAuditResult(Object[] result) {
			super(result);
			if (result[7] != null) this.itype = result[7].toString();
		}
				
		public String itypeString(){
			return(itype);
		}
		
	}

}
