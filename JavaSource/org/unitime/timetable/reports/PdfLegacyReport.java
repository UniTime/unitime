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
package org.unitime.timetable.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.reports.AbstractReport.Line;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.PdfFont;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author Tomas Muller
 */
public class PdfLegacyReport implements ReportWriter {
	protected static ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
    protected int iNrChars = 133;
    protected int iNrLines = 50;
    private OutputStream iOut = null;
    private Document iDoc = null;
    private StringBuffer iBuffer = new StringBuffer();
    private PrintWriter iPrint = null;
    private String iTitle, iTitle2, iSubject;
    private String iSession;
    private int iPageNo = 0;
    private int iLineNo = 0;
    private String iPageId = null;
    private String iCont = null;
    private String iHeader[] = null;
    private String iFooter = null;
    private Line iHeaderLine[] = null;
    private int iMode = sModeNormal;
    private Listener iListner;
    
    private boolean iEmpty = true;
    
    public static final int sModeNormal = 0;
    public static final int sModeLedger = 1;
    public static final int sModeText   = 2;
    
    public PdfLegacyReport(int mode, File file, String title, String title2, String subject, String session) throws IOException, DocumentException{
        iTitle = title;
        iTitle2 = title2;
        iSubject = subject;
        iSession = session;
        iMode = mode;
        
        if (file!=null) open(new FileOutputStream(file));
    }
    
    public PdfLegacyReport(int mode, OutputStream out, String title, String title2, String subject, String session) throws IOException, DocumentException{
        iTitle = title;
        iTitle2 = title2;
        iSubject = subject;
        iSession = session;
        iMode = mode;
        
        if (out!=null) open(out);
    }
    
    public void open(File file, int mode) throws DocumentException, IOException {
    	iMode = mode;
    	open(new FileOutputStream(file));
    }
    
    public void open(OutputStream out, int mode) throws DocumentException, IOException {
    	iMode = mode;
    	open(out);
    }
    
    public void open(OutputStream out) throws DocumentException, IOException {
        iOut = out;
        if (iMode==sModeText) {
            iPrint = new PrintWriter(iOut);
        } else {
            iNrLines = (iMode==sModeLedger?116:50);
            iDoc = new Document(iMode==sModeLedger?PageSize.LEDGER.rotate():PageSize.LETTER.rotate());

            PdfWriter.getInstance(iDoc, iOut);

            iDoc.addTitle(iTitle);
            iDoc.addAuthor("UniTime "+Constants.getVersion()+", www.unitime.org");
            iDoc.addSubject(iSubject);
            iDoc.addCreator("UniTime "+Constants.getVersion()+", www.unitime.org");

            iDoc.open();
        }
        iEmpty = true;
        iPageNo = 0; iLineNo = 0;
    }
    
    @Override
    public void setPageName(String pageName) {
        iPageId = pageName;
    }
    
    @Override
    public void setCont(String cont) {
        iCont = cont;
    }
    
    public void setHeader(String[] header) {
        iHeader = header;
    }
    
    @Override
    public void setHeader(Line... line) {
    	iHeaderLine = line;
    }
    
    @Override
    public Line[] getHeader() {
        return iHeaderLine;
    }
    
    @Override
    public void setFooter(String footer) {
        iFooter = footer;
    }
    
    protected void out(String text) throws DocumentException {
        if (iBuffer.length()>0) iBuffer.append("\n");
        iBuffer.append(text);
    }
    
    protected static String rep(char ch, int cnt) {
        String ret = "";
        for (int i=0;i<cnt;i++) ret+=ch;
        return ret;
    }
    
    protected void outln(char ch) throws DocumentException {
        out(rep(ch,iNrChars));
    }
    
    public String lpad(String s, char ch, int len) {
        if (s==null) s="";
        if (s.length()>len) return s.substring(0,len);
        while (s.length()<len) s = ch + s;
        return s;
    }
    
    public String lpad(String s, int len) {
        if (s==null) s="";
        if (s.length()>len) return s.substring(0,len);
        return lpad(s,' ',len);
    }

    protected String rpad(String s, char ch, int len) {
        if (s==null) s="";
        if (s.length()>len) return s.substring(0,len);
        while (s.length()<len) s = s + ch;
        return s;
    }
    
    public String rpad(String s, int len) {
        if (s==null) s="";
        if (s.length()>len) return s.substring(0,len);
        return rpad(s,' ',len);
    }
    
    protected String mpad(String s, char ch, int len) {
        if (s==null) s="";
        if (s.length()>len) return s.substring(0,len);
        while (s.length()<len) 
            if (s.length()%2==0) s = s + ch; else s = ch + s;
        return s;
    }

    public String mpad(String s, int len) {
        return mpad(s,' ',len);
    }
    
    protected String mpad(String s1, String s2, char ch, int len) {
        String m = "";
        while ((s1+m+s2).length()<len) m += ch;
        return s1+m+s2;
    }
    
    protected String render(String line, String s, int idx) {
        String a = (line.length()<=idx?rpad(line,' ',idx):line.substring(0,idx));
        String b = (line.length()<=idx+s.length()?"":line.substring(idx+s.length()));
        return a + s + b;
    }

    protected String renderMiddle(String line, String s) {
        return render(line, s, (iNrChars - s.length())/2);
    }

    protected String renderEnd(String line, String s) {
        return render(line, s, iNrChars-s.length());
    }
    
    public void printHeader() throws DocumentException {
    	printHeader(true);
    }
    
    public void printHeader(boolean newPage) throws DocumentException {
    	if (newPage) {
            out(renderEnd(
                    renderMiddle("UniTime "+Constants.getVersion(),iTitle),
                    iTitle2));
            out(mpad(
            		Formats.getDateFormat(Formats.Pattern.DATE_MEETING).format(new Date()),
                    iSession,' ',iNrChars));
            outln('=');
            iLineNo=0;
            if (iCont!=null && iCont.length()>0)
                println("("+iCont+" Continued)");
    	}
        if (iHeader!=null) for (int i=0;i<iHeader.length;i++) println(iHeader[i]);
        if (iHeaderLine != null) for (int i=0;i<iHeaderLine.length;i++) printLine(iHeaderLine[i]);
        if (iListner != null) iListner.headerPrinted();
    }
    
    protected void printFooter() throws DocumentException {
        iEmpty=false;
        out("");
        out(renderEnd(renderMiddle((iFooter==null?"":iFooter),MSG.lrPage(iPageNo+1)),(iPageId==null||iPageId.length()==0?"":iPageId)+"  "));
        if (iPrint!=null) {
            iPrint.print(iBuffer);
        } else {
        	//FIXME: For some reason when a line starts with space, the line is shifted by one space in the resulting PDF (when using iText 5.0.2)
            Paragraph p = new Paragraph(iBuffer.toString().replace("\n ", "\n  "), PdfFont.getFixedFont());
            p.setLeading(9.5f); //was 13.5f
            iDoc.add(p);
        }
        iBuffer = new StringBuffer();
        iPageNo++;
    }
    public void lastPage() throws DocumentException {
        while (iLineNo<iNrLines) {
            out(""); iLineNo++;
        }
        printFooter();
    }
    
    @Override
    public void newPage() throws DocumentException {
        while (iLineNo<iNrLines) {
            out(""); iLineNo++;
        }
        printFooter();
        if (iPrint!=null) {
            iPrint.print("\f\n");
        } else {
            iDoc.newPage();
        }
        printHeader();
    }
    
    public int getLineNumber() {
        return iLineNo;
    }
    
    public int getNrLinesPerPage() {
    	return iNrLines;
    }
    
    public void println(String text) throws DocumentException {
        out(text);
        iLineNo++;
        if (iLineNo>=iNrLines) newPage();
    }
    
    public boolean isEmpty() {
        return iEmpty;
    }
    
    public void close() throws IOException, DocumentException {
        if (isEmpty()) { println(MSG.lrNothingToReport()); lastPage(); }
        if (iPrint!=null) {
            iPrint.flush(); iPrint.close();
        } else {
            iDoc.close();
            iOut.close();
        }
    }
    
    public int getNrCharsPerLine() {
    	return iNrChars;
    }
    
	@Override
	public void printLine(Line line) throws DocumentException {
		println(line.render());
	}

	@Override
	public void printSeparator(Line line) throws DocumentException {
		if (line != null) {
			printLine(line);
		} else {
			if (iHeader != null && iHeader.length > 0)
				println(iHeader[iHeader.length - 1]);
			if (iHeaderLine != null && iHeaderLine.length > 0)
				printLine(iHeaderLine[iHeaderLine.length - 1]);
		}
	}

	@Override
	public int getSeparatorNrLines() {
		return 1;
	}

	@Override
	public void setListener(Listener listener) {
		iListner = listener;
	}
	
	@Override
	public boolean isSkipRepeating() { return true; }
}
