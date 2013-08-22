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

import java.awt.Color;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.unitime.commons.web.WebTable;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.util.PdfFont;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


/**
 * @author Tomas Muller
 */
public class PdfWebTable extends WebTable {
	private Hashtable iImages = new Hashtable();
	
	public void addImage(String name, java.awt.Image image) {
		iImages.put(name, image);
	}
	
    public PdfWebTable(int columns, String name, String[] headers, String[] align, boolean[] asc) {
        this(columns, name, null, headers, align, asc);
    }

    public PdfWebTable(int columns, String name, String ref, String[] headers, String[] align, boolean[] asc) {
    	super(columns, name, ref, headers, align, asc);
    }
	
	private PdfPCell createCell() {
		PdfPCell cell = new PdfPCell();
		cell.setBorderColor(Color.BLACK);
		cell.setPadding(3);
		cell.setBorderWidth(0);
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		return cell;
	}
	
	private float addImage(PdfPCell cell, String name) {
		try {
			java.awt.Image awtImage = (java.awt.Image)iImages.get(name);
			if (awtImage==null) return 0;
			Image img = Image.getInstance(awtImage, Color.WHITE);
			Chunk ck = new Chunk(img, 0, 0);
			if (cell.getPhrase()==null) {
				cell.setPhrase(new Paragraph(ck));
				cell.setVerticalAlignment(Element.ALIGN_TOP);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			} else {
				cell.getPhrase().add(ck);
			}
			return awtImage.getWidth(null);
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static float getWidth(String text, boolean bold, boolean italic) {
		Font font = PdfFont.getFont(bold, italic);
		float width = 0; 
		if (text.indexOf('\n')>=0) {
			for (StringTokenizer s = new StringTokenizer(text,"\n"); s.hasMoreTokens();)
				width = Math.max(width,font.getBaseFont().getWidthPoint(s.nextToken(), font.getSize()));
		} else 
			width = Math.max(width,font.getBaseFont().getWidthPoint(text, font.getSize()));
		return width;
	}
	
	public static float getWidthOfLastLine(String text, boolean bold, boolean italic) {
		Font font = PdfFont.getFont(bold, italic);
		float width = 0; 
		if (text.indexOf('\n')>=0) {
			for (StringTokenizer s = new StringTokenizer(text,"\n"); s.hasMoreTokens();)
				width = font.getBaseFont().getWidthPoint(s.nextToken(), font.getSize());
		} else 
			width = Math.max(width,font.getBaseFont().getWidthPoint(text, font.getSize()));
		return width;
	}

	private float addText(PdfPCell cell, String text, boolean bold, boolean italic, boolean underline, Color color, Color bgColor) {
		Font font = PdfFont.getFont(bold, italic, underline, color);
		Chunk chunk = new Chunk(text, font);
		if (bgColor!=null) chunk.setBackground(bgColor);
		if (cell.getPhrase()==null) {
		    cell.setPhrase(new Paragraph(chunk));
			cell.setVerticalAlignment(Element.ALIGN_TOP);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		} else {
			cell.getPhrase().add(chunk);
		}
		float width = 0; 
		if (text.indexOf('\n')>=0) {
			for (StringTokenizer s = new StringTokenizer(text,"\n"); s.hasMoreTokens();)
				width = Math.max(width,font.getBaseFont().getWidthPoint(s.nextToken(), font.getSize()));
		} else 
			width = Math.max(width,font.getBaseFont().getWidthPoint(text, font.getSize()));
		return width;
	}
	
	private float addText(PdfPCell cell, String text, boolean bold, boolean italic, boolean underline, Color color,
			boolean borderTop, boolean borderBottom, boolean borderLeft, boolean borderRight, Color borderColor, Color bgColor ) {
		
    	cell.setBorderWidth(1);
    	
		if (borderTop) {
			cell.setBorder(PdfPCell.TOP);
			if (borderColor==null)
				cell.setBorderColorTop(Color.BLACK);
			else
				cell.setBorderColorTop(borderColor);
		}
		
		if (borderBottom) {
			cell.setBorder(PdfPCell.BOTTOM);
			if (borderColor==null)
				cell.setBorderColorBottom(Color.BLACK);
			else
				cell.setBorderColorBottom(borderColor);
		}

		if (borderLeft) {
			cell.setBorder(PdfPCell.LEFT);
			if (borderColor==null)
				cell.setBorderColorLeft(Color.BLACK);
			else
				cell.setBorderColorLeft(borderColor);
		}

		if (borderRight) {
			cell.setBorder(PdfPCell.RIGHT);
			if (borderColor==null)
				cell.setBorderColorRight(Color.BLACK);
			else
				cell.setBorderColorRight(borderColor);
		}

		return addText(cell, text, bold, italic, underline, color, bgColor);
	}	
	
	private float addText(PdfPCell cell, String text, boolean bold, boolean italic, boolean underline, Color color,
			boolean border, Color borderColor, Color bgColor) {
		
		if (border) {
	    	cell.setBorderWidth(1);
			cell.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT | PdfPCell.TOP | PdfPCell.BOTTOM );
			if (borderColor==null)
				cell.setBorderColor(Color.BLACK);
			else
				cell.setBorderColor(borderColor);
		}
		
		return addText(cell, text, bold, italic, underline, color, bgColor);
	}	
	
	private float addText(PdfPCell cell, String text, boolean bold) {
		if (text==null) return addText(cell, "", bold, false, false, Color.BLACK, null);
		if (text.indexOf("@@")<0) return addText(cell, text, bold, false, false, Color.BLACK, null);
		
		Color color = Color.BLACK; 
		Color bcolor = Color.BLACK;
		Color bgColor = null;
		boolean bd=bold, it=false, un=false;
		boolean ba=false, bt=false, bb=false, bl=false, br=false;		
		float maxWidth = 0; 
		boolean first = true;
		
		for (StringTokenizer s = new StringTokenizer(text,"\n"); s.hasMoreTokens();) {
			String line = s.nextToken();
			float width = 0;
			int pos = 0;
			while (true) {
				int idx = line.indexOf("@@", pos);
				if (idx<0) {
					if (ba) {
						width += addText(cell, (!first && pos==0?"\n":"")+line.substring(pos), bd, it, un, color, true, bcolor, bgColor);
					}
					else {
						if (bt || bb || bl || br) {
							width += addText(cell, (!first && pos==0?"\n":"")+line.substring(pos), bd, it, un, color, bt, bb, bl, br, bcolor, bgColor);
						}
						else
							width += addText(cell, (!first && pos==0?"\n":"")+line.substring(pos), bd, it, un, color, bgColor);
					}
					break;
				} else {
					if (ba) {
						width += addText(cell, (!first && pos==0?"\n":"")+line.substring(pos, idx), bd, it, un, color, true, bcolor, bgColor);
					}
					else {
						if (bt || bb || bl || br) {
							width += addText(cell, (!first && pos==0?"\n":"")+line.substring(pos, idx), bd, it, un, color, bt, bb, bl, br, bcolor, bgColor);
						}
						else
							width += addText(cell, (!first && pos==0?"\n":"")+line.substring(pos, idx), bd, it, un, color, bgColor);
					}
					pos = idx;
				}
				pos+=2; //for @@
				String cmd = line.substring(pos, line.indexOf(' ',pos));
				pos+=cmd.length()+1;
				if ("BOLD".equals(cmd))
					bd=true;
				if ("END_BOLD".equals(cmd))
					bd=false;
				if ("ITALIC".equals(cmd))
					it=true;
				if ("END_ITALIC".equals(cmd))
					it=false;
				if ("UNDERLINE".equals(cmd))
					un=true;
				if ("END_UNDERLINE".equals(cmd))
					un=false;
				if ("COLOR".equals(cmd)) {
					String hex = line.substring(pos, line.indexOf(' ',pos));
					pos+=hex.length()+1;
					if (hex.startsWith("#")) hex = hex.substring(1);
					color = new Color(Integer.parseInt(hex,16));
				}
				if ("END_COLOR".equals(cmd)) {
					color = Color.BLACK;
				}
                if ("BGCOLOR".equals(cmd)) {
                    String hex = line.substring(pos, line.indexOf(' ',pos));
                    pos+=hex.length()+1;
                    bgColor = new Color(Integer.parseInt(hex,16));
                }
                if ("END_BGCOLOR".equals(cmd)) {
                    bgColor = null;
                }
				if ("IMAGE".equals(cmd)) {
					String name = line.substring(pos, line.indexOf(' ',pos));
					pos+=name.length()+1;
					width += addImage(cell, name);
				}
				if ("BORDER_ALL".equals(cmd) 
						|| "BORDER_TOP".equals(cmd)	|| "BORDER_BOTTOM".equals(cmd) 
						|| "BORDER_LEFT".equals(cmd) || "BORDER_RIGHT".equals(cmd) ) {
					
					String hex = line.substring(pos, line.indexOf(' ',pos));
					pos+=hex.length()+1;
					bcolor = new Color(Integer.parseInt(hex,16));
					
					if ("BORDER_ALL".equals(cmd)) {
						ba = true;
					}
					else {
						if ("BORDER_TOP".equals(cmd)) {
							bt = true;
						}
						if ("BORDER_BOTTOM".equals(cmd)) {
							bb = true;
						}
						if ("BORDER_LEFT".equals(cmd)) {
							bl = true;
						}
						if ("BORDER_RIGHT".equals(cmd)) {
							br = true;
						}
					}
				}
				if ("NO_WRAP".equals(cmd)) {
					cell.setNoWrap(true);
				}
			}
			maxWidth = Math.max(maxWidth, width);
			first=false;
		}
		return maxWidth;
	}
	
	private float[] widths = null;
	
	/**
	 * Prints pdf table. By default does not split table across
	 * page boundaries
	 * @param ordCol
	 * @return
	 */
	public PdfPTable printPdfTable(int ordCol) {
		return printPdfTable(ordCol, false);
	}
	
	/**
	 * Prints pdf table. By default does not split table across
	 * page boundaries 
	 * @param ordCol
	 * @param keepTogether true does not split table across pages
	 * @return
	 */
	public PdfPTable printPdfTable(int ordCol, boolean keepTogether) {
    	PdfPTable table = new PdfPTable(getNrColumns());
		table.setWidthPercentage(100);
		table.getDefaultCell().setPadding(3);
		table.getDefaultCell().setBorderWidth(0);
		table.setSplitRows(false);
    	table.setKeepTogether(keepTogether);
    	
        boolean asc = (ordCol == 0 || iAsc == null || iAsc.length <= Math.abs(ordCol) - 1 ? true : iAsc[Math.abs(ordCol) - 1]);
        if (ordCol < 0) asc = !asc;

		widths = new float[iColumns];
        for (int i=0;i<iColumns;i++)
        	widths[i] = 0f;

        String lastLine[] = new String[Math.max(iColumns,(iHeaders==null?0:iHeaders.length))];
        
        if (iHeaders != null) {
            for (int i = 0; i < iColumns; i++) {
                if (isFiltered(i)) continue;
            	PdfPCell c = createCell();
            	c.setBorderWidthBottom(1);
            	float width = addText(c,iHeaders[i]==null?"":iHeaders[i],true);
            	widths[i] = Math.max(widths[i],width);
            	String align = (iAlign != null ? iAlign[i] : "left");
            	if ("left".equals(align))
            		c.setHorizontalAlignment(Element.ALIGN_LEFT);
            	if ("right".equals(align))
            		c.setHorizontalAlignment(Element.ALIGN_RIGHT);
            	if ("center".equals(align))
            		c.setHorizontalAlignment(Element.ALIGN_CENTER);
            	table.addCell(c);
            }
            table.setHeaderRows(1);
        }
        if (ordCol != 0) {
            Collections.sort(iLines, new WebTableComparator(Math.abs(ordCol) - 1, asc));
        }
        for (int el = 0 ; el < iLines.size(); el++) {
            WebTableLine wtline = (WebTableLine) iLines.elementAt(el);
            String[] line = wtline.getLine();
            boolean blank = iBlankWhenSame;
            for (int i = 0; i < iColumns; i++) {
                if (isFiltered(i)) continue;
                if (blank && line[i]!=null && !line[i].equals(lastLine[i])) blank=false;
            	PdfPCell c = createCell();
            	float width = addText(c,blank || line[i]==null?"":line[i],false);
            	widths[i] = Math.max(widths[i],width);
            	String align = (iAlign != null ? iAlign[i] : "left");
            	if ("left".equals(align))
            		c.setHorizontalAlignment(Element.ALIGN_LEFT);
            	if ("right".equals(align))
            		c.setHorizontalAlignment(Element.ALIGN_RIGHT);
            	if ("center".equals(align))
            		c.setHorizontalAlignment(Element.ALIGN_CENTER);
                applyPdfStyle(c, wtline, (el+1<iLines.size()?(WebTableLine)iLines.elementAt(el+1):null), ordCol);
            	table.addCell(c);
            	lastLine[i] = line[i];
            }
        }
        
        try {
            if (getNrFilteredColumns()<0) {
                table.setWidths(widths);
            } else {
                float[] x = new float[getNrColumns()];
                int idx = 0;
                for (int i=0;i<iColumns;i++) {
                    if (isFiltered(i)) continue;
                    x[idx++] = widths[i];
                }
                table.setWidths(x);
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return table;
    }
	
	public float getWidth() {
    	float totalWidth = 0f;
    	for (int i=0;i<widths.length;i++)
    		totalWidth += 25f+widths[i];
    	
    	totalWidth = Math.max(PageSize.LETTER.getHeight(), totalWidth);
    	
    	return totalWidth;
	}
	
	public String getName() {
		return iName;
	}
    
    public void exportPdf(OutputStream out, int ordCol) throws Exception {
		PdfPTable table = printPdfTable(ordCol);
    	
		float width = getWidth();
	
		Document doc = new Document(new Rectangle(60f + width, 60f + 0.75f * width),30,30,30,30); 

		PdfWriter iWriter = PdfWriter.getInstance(doc, out);
		iWriter.setPageEvent(new PdfEventHandler());
		doc.open();
	
		if (iName!=null)
			doc.add(new Paragraph(iName, PdfFont.getBigFont(true)));
	
		doc.add(table);
	
		doc.close();
    }
    
	public void applyPdfStyle(PdfPCell cell, WebTableLine currentLine, WebTableLine nextLine, int order) {
		if (iWebTableTweakStyle==null || !(iWebTableTweakStyle instanceof PdfWebTableTweakStyle)) return;
		((PdfWebTableTweakStyle)iWebTableTweakStyle).applyPdfStyle(cell, currentLine, nextLine, order);
	}
    
    
    public static interface PdfWebTableTweakStyle extends WebTableTweakStyle {
    	public void applyPdfStyle(PdfPCell cell, WebTableLine currentLine, WebTableLine nextLine, int orderBy);
    }


}
