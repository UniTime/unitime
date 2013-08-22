/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.webutil;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Query;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.util.PdfFont;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


/**
 * Builds HTML tables for distribution preferences
 * 
 * @author Tomas Muller
 */
public class ExamDistributionPrefsTableBuilder {
	
	public String getDistPrefsTable(HttpServletRequest request, SessionContext context, Long subjectAreaId, String courseNbr, Long examTypeId) throws Exception {
	    Query q = new DistributionPrefDAO().getSession().createQuery(
	            "select distinct dp from DistributionPref dp " +
	            "inner join dp.distributionObjects do, Exam x inner join x.owners o " +
	            "where "+
	            (courseNbr==null || courseNbr.trim().length()==0?"":courseNbr.indexOf('*')>=0?"o.course.courseNbr like :courseNbr and ":"o.course.courseNbr=:courseNbr and")+
	            (subjectAreaId==null?"":" o.course.subjectArea.uniqueId=:subjectAreaId and ")+
	            "dp.distributionType.examPref = true and "+
	            "do.prefGroup = x and x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId")
	            .setLong("sessionId", context.getUser().getCurrentAcademicSessionId())
	    		.setLong("examTypeId", examTypeId);
	    if (subjectAreaId!=null)
	        q.setLong("subjectAreaId", subjectAreaId);
	    if (courseNbr!=null && courseNbr.trim().length()!=0)
	        q.setString("courseNbr", courseNbr.trim().replaceAll("\\*", "%"));
	    List distPrefs = q.setCacheable(true).list();
		return toHtmlTable(request, context, distPrefs, null); 
	}

    public void getDistPrefsTableAsPdf(OutputStream out, HttpServletRequest request, SessionContext context, Long subjectAreaId, String courseNbr, Long examTypeId) throws Exception {
        
        Query q = new DistributionPrefDAO().getSession().createQuery(
                "select distinct dp from DistributionPref dp " +
                "inner join dp.distributionObjects do, Exam x inner join x.owners o " +
                "where "+
                (courseNbr==null || courseNbr.trim().length()==0?"":courseNbr.indexOf('*')>=0?"o.course.courseNbr like :courseNbr and ":"o.course.courseNbr=:courseNbr and")+
                (subjectAreaId==null?"":" o.course.subjectArea.uniqueId=:subjectAreaId and ")+
                "dp.distributionType.examPref = true and "+
                "do.prefGroup = x and x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId")
                .setLong("sessionId", context.getUser().getCurrentAcademicSessionId())
                .setLong("examTypeId", examTypeId);
        if (subjectAreaId!=null)
            q.setLong("subjectAreaId", subjectAreaId);
        if (courseNbr!=null && courseNbr.trim().length()!=0)
            q.setString("courseNbr", courseNbr.trim().replaceAll("\\*", "%"));
        List distPrefs = q.setCacheable(true).list();

        toPdfTable(out, request, context, distPrefs, examTypeId); 
    }

    public String getDistPrefsTable(HttpServletRequest request, SessionContext context, Exam exam) throws Exception {
        return toHtmlTable(request, context, exam.effectivePreferences(DistributionPref.class), "Distribution Preferences");
    }

	/**
     * Build a html table with the list representing distribution prefs 
     * @param distPrefs
     * @param ordCol
     * @param editable
     * @return
     */
    public String toHtmlTable(HttpServletRequest request, SessionContext context, Collection distPrefs, String title) throws Exception {

    	String backId = ("PreferenceGroup".equals(request.getParameter("backType"))?request.getParameter("backId"):null);
        
        WebTable.setOrder(context,"examDistPrefsTable.ord",request.getParameter("order"),4);
        
        WebTable tbl = new WebTable(3, 
                title,  
    			"examDistributionPrefs.do?order=%%",
    			new String[] {" Type ", " Exam ", " Class/Course " },
    			new String[] { "left", "left", "left"},
    			new boolean[] { true, true, true} );
        
        int nrPrefs = 0;

        for (Iterator i1=distPrefs.iterator();i1.hasNext();) {
        	DistributionPref dp = (DistributionPref)i1.next();
        	
        	if (!context.hasPermission(dp, Right.ExaminationDistributionPreferenceDetail)) continue;
        	
        	boolean prefEditable = context.hasPermission(dp, Right.ExaminationDistributionPreferenceEdit);
        	
        	nrPrefs++;
        	
        	String examStr = "";
        	String objStr = "";
        	
        	
        	for (Iterator i2=dp.getOrderedSetOfDistributionObjects().iterator();i2.hasNext();) {
        		DistributionObject dO = (DistributionObject)i2.next();
        		Exam exam = (Exam)dO.getPrefGroup();
                examStr += dO.preferenceText();
                for (Iterator i3=exam.getOwners().iterator();i3.hasNext();) {
                    ExamOwner owner = (ExamOwner)i3.next();
                    objStr += owner.getLabel();
                    if (i3.hasNext()) {
                        examStr += "<BR>";
                        objStr += "<BR>";
                    }
                }
        		if (i2.hasNext()) {
        		    examStr += "<BR>";
        		    objStr += "<BR>";
        		}
        	}

        	String distType = dp.getDistributionType().getLabel();
            String prefLevel = dp.getPrefLevel().getPrefName();
            String prefColor = dp.getPrefLevel().prefcolor();
        	if (PreferenceLevel.sNeutral.equals(dp.getPrefLevel().getPrefProlog())) prefColor = "gray";
            String onClick = null;
            
            boolean gray = false;
            
            if (prefEditable) {
                onClick = "onClick=\"document.location='examDistributionPrefs.do"
                    + "?dp=" + dp.getUniqueId().toString() 
                    + "&op=view'\"";
            } //else gray = true;
            
            boolean back = dp.getUniqueId().toString().equals(backId);
            
            tbl.addLine(
                    onClick, 
                	new String[] { 
                    		(back?"<A name=\"back\"</A>":"")+
                    		(gray?"<span style='color:gray;'>":"<span style='color:"+prefColor+";font-weight:bold;' title='"+prefLevel+" "+distType+"'>")+distType+"</span>",
                    		(gray?"<span style='color:gray;'>":"")+examStr+(gray?"</span>":""), 
                    		(gray?"<span style='color:gray;'>":"")+objStr+(gray?"</span>":"")
                    	}, 
                   	new Comparable[] { distType, examStr, objStr });
            
        }       
        
        if (nrPrefs==0)
            tbl.addLine(null,  new String[] { "No preferences found", "", "" }, null);
        
        return tbl.printTable(WebTable.getOrder(context,"examDistPrefsTable.ord"));
    }

    public void toPdfTable(OutputStream out, HttpServletRequest request, SessionContext context, Collection distPrefs, Long examTypeId) throws Exception {
        WebTable.setOrder(context,"examDistPrefsTable.ord",request.getParameter("order"),4);
        
        PdfWebTable tbl = new PdfWebTable(4, 
                ExamTypeDAO.getInstance().get(examTypeId).getLabel()+" Examination Distribution Preferences",  
                null,
                new String[] {" Preference ", " Type ", " Exam ", " Class/Course " },
                new String[] { "left", "left", "left", "left"},
                new boolean[] { true, true, true, true} );
        
        int nrPrefs = 0;

        for (Iterator i1=distPrefs.iterator();i1.hasNext();) {
            DistributionPref dp = (DistributionPref)i1.next();
            
            if (!context.hasPermission(dp, Right.ExaminationDistributionPreferenceDetail)) continue;
            
            nrPrefs++;
            
            String examStr = "";
            String objStr = "";
            
            
            for (Iterator i2=dp.getOrderedSetOfDistributionObjects().iterator();i2.hasNext();) {
                DistributionObject dO = (DistributionObject)i2.next();
                Exam exam = (Exam)dO.getPrefGroup();
                examStr += dO.preferenceText();
                for (Iterator i3=exam.getOwners().iterator();i3.hasNext();) {
                    ExamOwner owner = (ExamOwner)i3.next();
                    objStr += owner.getLabel();
                    if (i3.hasNext()) {
                        examStr += "\n";
                        objStr += "\n";
                    }
                }
                if (i2.hasNext()) {
                    examStr += "\n";
                    objStr += "\n";
                }
            }

            String distType = dp.getDistributionType().getLabel();
            String prefLevel = dp.getPrefLevel().getPrefName();
            
            tbl.addLine(
                    null, 
                    new String[] { 
                            prefLevel,
                            distType,
                            examStr, 
                            objStr
                        }, 
                    new Comparable[] { null, distType, examStr, objStr });
            
        }       
        
        if (nrPrefs==0)
            tbl.addLine(null,  new String[] { "No preferences found", "", "", "" }, null);
        
        int ord = WebTable.getOrder(context,"examDistPrefsTable.ord");
        ord = (ord>0?1:-1)*(1+Math.abs(ord));
    	
    	PdfPTable table = tbl.printPdfTable(ord);
    	
    	float width = tbl.getWidth();
    	
    	Document doc = new Document(new Rectangle(60f + width, 60f + 1.30f * width),30,30,30,30); 

		PdfWriter iWriter = PdfWriter.getInstance(doc, out);
		iWriter.setPageEvent(new PdfEventHandler());
		doc.open();
		
		if (tbl.getName()!=null)
			doc.add(new Paragraph(tbl.getName(), PdfFont.getBigFont(true)));
		
		doc.add(table);
		
		doc.close();
    }
}
