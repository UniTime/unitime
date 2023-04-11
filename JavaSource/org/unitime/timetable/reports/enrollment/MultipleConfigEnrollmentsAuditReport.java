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

import org.unitime.commons.Debug;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.StudentClassEnrollmentDAO;

import com.lowagie.text.DocumentException;

/**
 * @author Stephanie Schluttenhofer
 *
 */
public class MultipleConfigEnrollmentsAuditReport extends PdfEnrollmentAuditReport {

    public MultipleConfigEnrollmentsAuditReport(int mode, File file, Session session, TreeSet<SubjectArea> subjectAreas, String subTitle) throws DocumentException, IOException {
        super(mode, getTitle(), file, session, subjectAreas, subTitle);
    }

    public MultipleConfigEnrollmentsAuditReport(int mode, File file, Session session) throws DocumentException, IOException {
    	super(mode, getTitle(), file, session);
    }

	@Override
	public void printReport() throws DocumentException {
		setHeaderLine(buildHeaderString());
        List results = getAuditResults(getSubjectAreas());
        Vector<Line> lines = new Vector<Line>();
        Iterator it = results.iterator();
        while(it.hasNext()) {
        	MultipleConfigEnrollmentsAuditResult result = new MultipleConfigEnrollmentsAuditResult((Object[]) it.next());
        	lines.add(buildLineString(result));
        }
        printHeader();
        for (Line str : lines) {
                println(str);
        }
        if (!lines.isEmpty()){
        	lastPage();
        }

	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected List getAuditResults(TreeSet<SubjectArea> subjectAreas){
		TreeSet<SubjectArea> subjects = new TreeSet<SubjectArea>();
		if (subjectAreas != null && !subjectAreas.isEmpty()){
			subjects.addAll(subjectAreas);
		} else {
			subjects.addAll(SubjectArea.getSubjectAreaList(getSession().getUniqueId()));
		}

		String query = createQueryString(subjects);
		Vector results = new Vector();
		for (SubjectArea sa : subjects){
			Debug.info(getTitle() + " - Checking Subject Area:  " + sa.getSubjectAreaAbbreviation());
			results.addAll(StudentClassEnrollmentDAO.getInstance()
				 .getSession().createQuery(query)
				 .setParameter("sessId", getSession().getUniqueId().longValue(), org.hibernate.type.LongType.INSTANCE)
				 .setParameter("subjectId", sa.getUniqueId().longValue(), org.hibernate.type.LongType.INSTANCE)
				 .list());
		}
		return(results);
	}

	public static String getTitle() {
		return MSG.reportMultipleConfigEnrollmentsAudit();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String createQueryString(TreeSet<SubjectArea> subjectAreas) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct s.externalUniqueId, s.lastName, s.firstName, s.middleName,")
		  .append(" sce.courseOffering.subjectArea.subjectAreaAbbreviation, sce.courseOffering.courseNbr, sce.courseOffering.title,")
		  .append(" s.uniqueId, sce.courseOffering.uniqueId")
		  .append(" from Student s inner join s.classEnrollments as sce")
		  .append(" where s.session.uniqueId = :sessId")
		  .append(" and sce.courseOffering.subjectArea.uniqueId = :subjectId")
		  .append(" and 1 < ( select count(distinct cfg1) from StudentClassEnrollment sce1")
		  .append(" inner join sce1.clazz.schedulingSubpart.instrOfferingConfig cfg1")
		  .append(" where sce1.courseOffering = sce.courseOffering and sce1.student = sce.student)")
		  .append(" order by sce.courseOffering.subjectArea.subjectAreaAbbreviation, sce.courseOffering.courseNbr,")
		  .append(" sce.courseOffering.title");

		if (isShowId()){
			sb.append(", s.externalUniqueId");
		} else if (isShowName()) {
			sb.append(", s.lastName, s.firstName, s.middleName");
		}

		return(sb.toString());
	
	}
	
	private Line buildLineString(MultipleConfigEnrollmentsAuditResult result) {
		return new Line(buildBaseAuditLine(result), new Line(
				rpad(result.configsListStr(), ' ', multipleClassesLength)
				));
	}

	private Line[] buildHeaderString(){
		Line[] baseHdr = getBaseHeader();
		return new Line[] {
				new Line(baseHdr[0], new Line(
						rpad(MSG.lrMultipleConfigs(), ' ', multipleClassesLength)
				)),
				new Line(baseHdr[1], new Line(
						rpad(MSG.lrOfSameCourse(), ' ', multipleClassesLength)
				)),
				new Line(baseHdr[2], new Line(
						rpad("", '-', multipleClassesLength)
				))
		};
	}


	private class MultipleConfigEnrollmentsAuditResult extends EnrollmentAuditResult {
		private Long studentUniqueId;
		private Long courseId;
		private TreeSet<String> configs = new TreeSet<String>();


		public MultipleConfigEnrollmentsAuditResult(Object[] result) {
			super(result);
			if (result[7] != null) this.studentUniqueId = Long.valueOf(result[7].toString());
			if (result[8] != null) this.courseId = Long.valueOf(result[8].toString());
			findConfigs();
		}
				
		private void findConfigs(){
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct sce.clazz.schedulingSubpart.instrOfferingConfig")
			  .append(" from StudentClassEnrollment sce where sce.student.uniqueId = :studId and sce.courseOffering.uniqueId = :courseId");
			for (InstrOfferingConfig result: StudentClassEnrollmentDAO.getInstance().getSession()
					.createQuery(sb.toString(), InstrOfferingConfig.class)
					.setParameter("studId", studentUniqueId, org.hibernate.type.LongType.INSTANCE)
					.setParameter("courseId", courseId, org.hibernate.type.LongType.INSTANCE)
					.list()) {
				configs.add(result.getName());
			}
			
		}
		
		public String configsListStr(){
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String config : configs){
				if (first){
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(config);
			}
			return(sb.toString());
		}
		
	}

}
