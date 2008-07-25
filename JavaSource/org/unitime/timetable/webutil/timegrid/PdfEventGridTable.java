package org.unitime.timetable.webutil.timegrid;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

import org.unitime.timetable.form.EventGridForm;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.PdfEventHandler;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PdfEventGridTable extends EventGridTable {
    private PdfWriter iWriter = null;
    private Document iDocument = null;
    
    private static Color sBorderColor = new Color(100,100,100);
    private static Color sNotAvailableColor = new Color(224,224,224);
    
    public PdfEventGridTable(EventGridForm form) {
        super(form);
    }
    
    public void export(File file) throws Exception {
        if (iModel==null || iModel.isEmpty()) return;
        
        FileOutputStream out = null;
        try {
            iDocument = new Document(new Rectangle(1100f, 750f), 30,30,30,30);
            
            out = new FileOutputStream(file);
            iWriter = PdfEventHandler.initFooter(iDocument, out);
            iDocument.open();
            
            printTable();
        
            iDocument.close();
        } finally {
            try {
                if (out!=null) out.close();
            } catch (IOException e) {}
        }
    }
    
    public int getNrColumns() {
        int nrCols = 0;
        if (iDates.size()>1) {
            for (TableModel m : iModel) {
                int nrColsThisModel = 0; 
                for (Date date : iDates) 
                    nrColsThisModel += m.getColSpan(date);
                nrCols = Math.max(nrColsThisModel, nrCols);
            }
        } else {
            Date date = iDates.firstElement();
            for (TableModel m : iModel)
                nrCols += m.getColSpan(date);
        }
        return 1+nrCols;
    }
    
    
    public void printTable() throws IOException, DocumentException {
        DateFormat df1 = new SimpleDateFormat("EEEE");
        DateFormat df2 = new SimpleDateFormat("MMM dd, yyyy");
        DateFormat df3 = new SimpleDateFormat("MM/dd");

        if (iDates.size()>1) {
            for (TableModel m : iModel) {
                int nrCols = 0;
                boolean split = false;
                for (Date date : iDates) {
                    int colSpan = m.getColSpan(date);
                    nrCols += colSpan;
                    if (colSpan>1) split = true;
                }
                MyTable table = new MyTable(m.getLocation().getLabel()+"\n("+m.getLocation().getCapacity()+")");
                for (Date date : iDates)
                    table.addColumn(m.getColSpan(date),df1.format(date)+"\n"+df2.format(date), split);
                table.newLine();
                int lastCol = (iEndSlot-iStartSlot)/iStep;
                TreeSet<Integer> aboveBlank = new TreeSet<Integer>();
                for (int col = 0; col<lastCol; col++) {
                    int start = iStartSlot + col*iStep;
                    int min = start*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
                    int startHour = min/60;
                    int startMin = min%60;
                    table.addRow((startHour>12?startHour-12:startHour)+":"+(startMin<10?"0":"")+startMin+(startHour>=12?"p":"a"));
                    TreeSet<Integer> blank = new TreeSet<Integer>();
                    int mcol = 0;
                    for (int row = 0; row < iDates.size(); row++ ) {
                        Date date = iDates.elementAt(row);
                        TableCell cell = m.getTable()[row][col];
                        int span = m.getColSpan(date);
                        MeetingCell[] content = new MeetingCell[span];
                        for (int i=0;i<content.length;i++) content[i]=null;
                        Queue<MeetingCell> notAssigned = new LinkedList<MeetingCell>();
                        for (MeetingCell mc: cell.getMeetings()) {
                            if (mc.getCol()>=0 && content[mc.getCol()]==null) {
                                content[mc.getCol()]=mc;
                            } else {
                                notAssigned.offer(mc);
                            }
                        }
                        for (int idx=0;idx<content.length;idx++) {
                            MeetingCell mc = (content[idx]==null?notAssigned.poll():content[idx]);
                            if (mc!=null) {
                                mc.setCol(idx);
                                Meeting meeting = mc.getMeeting();
                                int line = mc.getPrinted(); mc.setPrinted(line+1);
                                boolean last = (line+1==mc.getLength());
                                if (line==0) {
                                    if (!last) table.addCell(!last, aboveBlank.contains(mcol), idx==0 && split, meeting.startTime()+" - "+meeting.stopTime(), false);
                                    else table.addCell(!last,aboveBlank.contains(mcol),idx==0 && split,meeting.getEvent().getEventName(),meeting.getApprovedDate()!=null);
                                } else if (line==1) {
                                    table.addCell(!last,aboveBlank.contains(mcol),idx==0 && split,meeting.getEvent().getEventName(),meeting.getApprovedDate()!=null);
                                } else if (line==2) {
                                    table.addCell(!last,aboveBlank.contains(mcol),idx==0 && split,meeting.getEvent().getEventTypeLabel().replaceAll("Event", "").replaceAll("Examination", "Exam"), false);
                                } else {
                                    table.addCell(!last,aboveBlank.contains(mcol),idx==0 && split,null,false);
                                }
                            } else {
                                if (cell.getMeetings().isEmpty()) {
                                    table.addEmptyCell(false, (idx+1<content.length && content[idx+1]==null), idx==0 && split, null);
                                } else {
                                    table.addEmptyCell(col+1<lastCol && !m.getTable()[row][col+1].getMeetings().isEmpty(), 
                                            (idx+1<content.length && content[idx+1]==null), idx==0 && split, sNotAvailableColor);
                                    blank.add(mcol);
                                }
                            }
                            mcol++;
                        }
                    }
                    aboveBlank = blank;
                    table.newLine();
                }
                table.flush();
            }
        } else {
            Date date = iDates.firstElement();
            boolean split = false;
            int nrCols = 0;
            for (TableModel m : iModel) {
                int colSpan = m.getColSpan(date);
                nrCols += colSpan;
                if (colSpan>1) split = true;
            }
            MyTable table = new MyTable(df1.format(date)+"\n"+df2.format(date));
            for (TableModel m : iModel)
                table.addColumn(m.getColSpan(date), m.getLocation().getLabel()+"\n("+m.getLocation().getCapacity()+" seats)", split);
            table.newLine();
            HashSet<Meeting> rendered = new HashSet();
            int lastCol = (iEndSlot-iStartSlot)/iStep;
            TreeSet<Integer> aboveBlank = new TreeSet<Integer>();
            for (int col = 0; col<lastCol; col++) {
                int start = iStartSlot + col*iStep;
                int min = start*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
                int startHour = min/60;
                int startMin = min%60;
                table.addRow((startHour>12?startHour-12:startHour)+":"+(startMin<10?"0":"")+startMin+(startHour>=12?"p":"a"));
                TreeSet<Integer> blank = new TreeSet<Integer>();
                for (TableModel m : iModel) {
                    TableCell cell = m.getTable()[0][col];
                    int span = m.getColSpan(date);
                    MeetingCell[] content = new MeetingCell[span];
                    for (int i=0;i<content.length;i++) content[i]=null;
                    Queue<MeetingCell> notAssigned = new LinkedList<MeetingCell>();
                    for (MeetingCell mc: cell.getMeetings()) {
                        if (mc.getCol()>=0 && content[mc.getCol()]==null) {
                            content[mc.getCol()]=mc;
                        } else {
                            notAssigned.offer(mc);
                        }
                    }
                    for (int idx=0;idx<content.length;idx++) {
                        MeetingCell mc = (content[idx]==null?notAssigned.poll():content[idx]);
                        if (mc!=null) {
                            mc.setCol(idx);
                            Meeting meeting = mc.getMeeting();
                            int line = mc.getPrinted(); mc.setPrinted(line+1);
                            boolean last = (line+1==mc.getLength());
                            if (line==0) {
                                if (!last) table.addCell(!last,aboveBlank.contains(idx),idx==0 && split,meeting.startTime()+" - "+meeting.stopTime(),false);
                                else table.addCell(!last,aboveBlank.contains(idx),idx==0 && split,meeting.getEvent().getEventName(),meeting.getApprovedDate()!=null);
                            } else if (line==1) {
                                table.addCell(!last,aboveBlank.contains(idx),idx==0 && split,meeting.getEvent().getEventName(),meeting.getApprovedDate()!=null);
                            } else if (line==2) {
                                table.addCell(!last,aboveBlank.contains(idx),idx==0 && split,meeting.getEvent().getEventTypeLabel().replaceAll("Event", ""),false);
                            } else {
                                table.addCell(!last,aboveBlank.contains(idx),idx==0 && split,null,false);
                            }
                        } else {
                            if (cell.getMeetings().isEmpty()) {
                                table.addEmptyCell(false, idx+1<content.length && content[idx+1]==null, idx==0 && split, null);
                                //available
                            } else {
                                table.addEmptyCell(col+1<lastCol && !m.getTable()[0][col+1].getMeetings().isEmpty(),
                                        idx+1<content.length && content[idx+1]==null, idx==0 && split, sNotAvailableColor);
                                blank.add(idx);
                            }
                        }
                    }
                }
                aboveBlank = blank;
                table.newLine();
            }
            table.flush();
        }
    }
    
    public class MyTable {
        private PdfPTable iTable = null;
        private MyTable iNext = null;
        private int iNrCols = 0;
        private String iName;
        private int iIndex = 0;
        
        public MyTable(String name) {
            iName = name;
            iTable = new PdfPTable(11);
            iTable.setWidthPercentage(100);
            iTable.getDefaultCell().setPadding(3);
            iTable.getDefaultCell().setBorderWidth(1);
            iTable.setSplitRows(true);
            iTable.setSpacingBefore(10);
            iTable.setKeepTogether(true);
            iTable.setHeaderRows(1);
            PdfPCell c = createCell(1,1,1,1);
            addText(c, iName, true);
            iTable.addCell(c);
        }
        
        public void addColumn(int colSpan, String name, boolean left) {
            if (iNext==null && iNrCols+colSpan<=10) {
                iNrCols += colSpan;
                PdfPCell c = createCell(1,1,(left?1:0),1);
                c.setColspan(colSpan);
                addText(c, name, true);
                iTable.addCell(c);
            } else {
                if (iNext==null) iNext = new MyTable(iName);
                iNext.addColumn(colSpan, name, left);
            }
        }
        
        public void addRow(String name) {
            PdfPCell c = createCell(0,1,1,1);
            addText(c, name, true);
            iTable.addCell(c);
            iIndex = 0;
            if (iNext!=null) iNext.addRow(name);
        }
        
        public void addEmptyCell(boolean hMiddle, boolean vMiddle, boolean left, Color color) {
            if (iIndex<iNrCols) {
                PdfPCell c = createCell(0,(hMiddle?0:1),(left?1:0),(vMiddle?0:1));
                if (color!=null) c.setBackgroundColor(color);
                iTable.addCell(c);
                iIndex++;
            } else if (iNext!=null)
                iNext.addEmptyCell(hMiddle, vMiddle, left, color);
        }
        
        public void addCell(boolean middle, boolean top, boolean left, String name, boolean bold) {
            addCell(middle, top, left, name, bold, null);
        }

        public void addCell(boolean middle, boolean top, boolean left, String name, boolean bold, Color color) {
            if (iIndex<iNrCols) {
                PdfPCell c = createCell((top?1:0),(middle?0:1),(left?1:0),1);
                if (name!=null) addText(c, name, bold);
                if (color!=null) c.setBackgroundColor(color);
                iTable.addCell(c);
                iIndex++;
            } else if (iNext!=null)
                iNext.addCell(middle, top, left, name, bold, color);
        }

        private PdfPCell createCell(int top, int bottom, int left, int right) {
            PdfPCell cell = new PdfPCell();
            cell.setBorderColor(sBorderColor);
            cell.setPadding(3);
            cell.setBorderWidth(0);
            cell.setVerticalAlignment(Element.ALIGN_TOP);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            //cell.setNoWrap(true);
            cell.setBorderWidthTop(top);
            cell.setBorderWidthBottom(bottom);
            cell.setBorderWidthLeft(left);
            cell.setBorderWidthRight(right);
            return cell;
        }
        
        private void addText(PdfPCell cell, String text, boolean bold) {
            if (text==null) return;
            if (cell.getPhrase()==null) {
                cell.setPhrase(new Paragraph(text,FontFactory.getFont(bold?FontFactory.HELVETICA_BOLD:FontFactory.HELVETICA, 10)));
                cell.setVerticalAlignment(Element.ALIGN_TOP);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            } else {
                cell.getPhrase().add(new Chunk("\n"+text,FontFactory.getFont(bold?FontFactory.HELVETICA_BOLD:FontFactory.HELVETICA, 10)));
            }
        }
        
        private void newLine() {
            for (int i=iNrCols;i<10;i++) iTable.addCell(createCell(0,0,0,0));
            if (iNext!=null) iNext.newLine();
        }
        
        private void flush() throws DocumentException {
            iDocument.add(iTable);
            if (iNext!=null) iNext.flush();
        }
        
    }

}
