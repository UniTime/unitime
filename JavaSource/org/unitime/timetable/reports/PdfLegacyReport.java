package org.unitime.timetable.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.util.Constants;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

public class PdfLegacyReport {
    protected static int sNrChars = 133;
    protected static int sNrLines = 50;
    private File iFile = null;
    private FileOutputStream iOut = null;
    private Document iDoc = null;
    private PdfWriter iWriter = null;
    private StringBuffer iBuffer = new StringBuffer();
    private PrintWriter iPrint = null;
    private String iTitle, iTitle2;
    private String iSession;
    private int iPageNo = 0;
    private int iLineNo = 0;
    private String iPageId = null;
    private String iCont = null;
    private String iHeader[] = null;
    private String iFooter = null;
    
    private boolean iEmpty = true;
    private int iMode = 0;
    
    public static final int sModeNormal = 0;
    public static final int sModeLedger = 1;
    public static final int sModeText   = 2;
    
    public PdfLegacyReport(int mode, File file, String title, String title2, String subject, String session) throws IOException, DocumentException{
        iFile = file;
        iTitle = title;
        iTitle2 = title2;
        iSession = session;

        iOut = new FileOutputStream(file);

        if (mode==sModeText) {
            iPrint = new PrintWriter(iOut);
        } else {
            if (mode==sModeLedger) sNrLines = 116;
            iDoc = new Document(mode==sModeLedger?PageSize.LEDGER.rotate():PageSize.LETTER.rotate());

            iWriter = PdfWriter.getInstance(iDoc, iOut);

            iDoc.addTitle(title);
            iDoc.addAuthor(ApplicationProperties.getProperty("tmtbl.pdf.examreport.author","UniTime 3.0."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")+", www.unitime.org"));
            iDoc.addSubject(subject);
            iDoc.addCreator("UniTime 3.0."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")+", www.unitime.org");

            iDoc.open();
        }
    }
    
    protected void setPageName(String pageName) {
        iPageId = pageName;
    }
    
    protected void setCont(String cont) {
        iCont = cont;
    }
    
    protected void setHeader(String[] header) {
        iHeader = header;
    }
    
    protected void setFooter(String footer) {
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
        out(rep(ch,sNrChars));
    }
    
    protected String lpad(String s, char ch, int len) {
        while (s.length()<len) s = ch + s;
        return s;
    }
    
    protected String lpad(String s, int len) {
        if (s==null) s="";
        if (s.length()>len) return s.substring(0,len);
        return lpad(s,' ',len);
    }

    protected String rpad(String s, char ch, int len) {
        while (s.length()<len) s = s + ch;
        return s;
    }
    
    protected String rpad(String s, int len) {
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

    protected String mpad(String s, int len) {
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
        return render(line, s, (sNrChars - s.length())/2);
    }

    protected String renderEnd(String line, String s) {
        return render(line, s, sNrChars-s.length());
    }    
    
    protected void printHeader() throws DocumentException {
        out(renderEnd(renderMiddle(
                ApplicationProperties.getProperty("tmtbl.pdf.examreport.author","UniTime 3.0."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")),
                ApplicationProperties.getProperty("tmtbl.pdf.examreport.title",iTitle)
                ),
                iTitle2));
        out(mpad(
                new SimpleDateFormat("EEE MMM dd, yyyy").format(new Date()),
                iSession,' ',sNrChars));
        outln('=');
        iLineNo=0;
        if (iCont!=null && iCont.length()>0)
            println("("+iCont+" Continued)");
        if (iHeader!=null) for (int i=0;i<iHeader.length;i++) println(iHeader[i]);
        headerPrinted();
    }
    
    protected void headerPrinted() {};
    
    protected void printFooter() throws DocumentException {
        iEmpty=false;
        out("");
        out(renderEnd(renderMiddle((iFooter==null?"":iFooter),"Page "+(iPageNo+1)),(iPageId==null||iPageId.length()==0?"":iPageId)+"  "));
        if (iPrint!=null) {
            iPrint.print(iBuffer);
        } else {
            Paragraph p = new Paragraph(iBuffer.toString(), FontFactory.getFont(FontFactory.COURIER, 9));
            p.setLeading(9.5f); //was 13.5f
            iDoc.add(p);
        }
        iBuffer = new StringBuffer();
        iPageNo++;
    }
    protected void lastPage() throws DocumentException {
        while (iLineNo<sNrLines) {
            out(""); iLineNo++;
        }
        printFooter();
    }
    
    protected void newPage() throws DocumentException {
        while (iLineNo<sNrLines) {
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
    
    protected void println(String text) throws DocumentException {
        out(text);
        iLineNo++;
        if (iLineNo>=sNrLines) newPage();
    }
    
    public boolean isEmpty() {
        return iEmpty;
    }
    
    public void close() throws IOException, DocumentException {
        if (isEmpty()) { println("Nothing to report."); lastPage(); }
        if (iPrint!=null) {
            iPrint.flush(); iPrint.close();
        } else {
            iDoc.close();
            iOut.close();
        }
    }
}
