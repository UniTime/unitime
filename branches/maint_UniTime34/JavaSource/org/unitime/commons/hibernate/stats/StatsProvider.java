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
package org.unitime.commons.hibernate.stats;

import java.util.Date;

import org.hibernate.SessionFactory;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.unitime.commons.web.htmlgen.Table;
import org.unitime.commons.web.htmlgen.TableCell;
import org.unitime.commons.web.htmlgen.TableHeaderCell;
import org.unitime.commons.web.htmlgen.TableRow;
import org.unitime.timetable.model.dao._RootDAO;


/**
 * Displays hibernate statistics in html format
 * @author Heston Fernandes
 */
public class StatsProvider {
    
    public static String getStatsHtml(boolean summaryOnly) {
        return new StatsProvider().getStatsHtml(new _RootDAO().getSession().getSessionFactory(), summaryOnly);
    }

    /**
     * Format statistics in HTML
     * @param sessionFactory Hibernate Session Factory
     * @param summaryOnly true - Display only summary info
     * @return HTML String
     */
    public String getStatsHtml(
            SessionFactory sessionFactory, 
            boolean summaryOnly ) {
        
        StringBuffer hibStats = new StringBuffer();
        
        
        try {
            // Get statistics
            Statistics stats = sessionFactory.getStatistics();

            // Checks statistics enabled
            if(!stats.isStatisticsEnabled()) {
                return "<font color='red'><b>Hibernate statistics is not enabled.</b></font>";
            }
            
            
            // Row Color for even numbered rows
            String evenRowColor = "#FAFAFA";
            
            // Generate HTML table
            Table table = new Table();
            table.setWidth("100%");
            table.setBorder(0);
            table.setCellSpacing(0);
            table.setCellPadding(3);

            // Links
            StringBuffer links = new StringBuffer("");
            
            links.append("<A class=\"l7\" href=\"#Entity\">Entity</A>");
            if(!summaryOnly)
                links.append(" - <A class=\"l7\" href=\"#EntityDetail\">Detail</A>");
            
            links.append(" | <A class=\"l7\" href=\"#Collection\">Collection</A>");
            if(!summaryOnly)
                links.append(" - <A class=\"l7\" href=\"#CollectionDetail\">Detail</A>");
            
            links.append(" | <A class=\"l7\" href=\"#SecondLevelCache\">Second Level Cache</A>");
            if(!summaryOnly)
                links.append(" - <A class=\"l7\" href=\"#SecondLevelCacheDetail\">Detail</A>");

            links.append(" | <A class=\"l7\" href=\"#Query\">Query</A>");
            if(!summaryOnly)
                links.append(" - <A class=\"l7\" href=\"#QueryDetail\">Detail</A>");
            
        	TableRow row = new TableRow();
            row.addContent(cell(links.toString(), 1, 2, true, "center", "middle"));
        	table.addContent(row);
            
        	// Link to top
        	TableRow linkToTop = new TableRow();
        	linkToTop.addContent(cell("<A class=\"l7\" href=\"#BackToTop\">Back to Top</A>", 1, 2, true, "right", "middle"));
            
        	
            // ---------------------- Overall Stats ------------------------
        	row = new TableRow();
        	row.addContent(headerCell("<A name=\"BackToTop\">Metric</A>", 1, 1));
        	row.addContent(headerCell("Value", 1, 1));
        	table.addContent(row);

        	row = new TableRow();
            row.addContent(cell(" &nbsp; Start Time", 1, 1, true));
            row.addContent(cell(new Date(stats.getStartTime()).toString(), 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Connect Count", 1, 1, true));
            row.addContent(cell(stats.getConnectCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Flush Count", 1, 1, true));
            row.addContent(cell(stats.getFlushCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Session Open Count", 1, 1, true));
            row.addContent(cell(stats.getSessionOpenCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Session Close Count", 1, 1, true));
            row.addContent(cell(stats.getSessionCloseCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Transaction Count", 1, 1, true));
            row.addContent(cell(stats.getTransactionCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Successful Transaction Count", 1, 1, true));
            row.addContent(cell(stats.getSuccessfulTransactionCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Prepare Statement Count", 1, 1, true));
            row.addContent(cell(stats.getPrepareStatementCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Close Statement Count", 1, 1, true));
            row.addContent(cell(stats.getCloseStatementCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Optimistic Failure Count", 1, 1, true));
            row.addContent(cell(stats.getOptimisticFailureCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell("<hr>", 1, 2, false));
            table.addContent(row);

 
            // ---------------------- Entity Stats ------------------------
            row = new TableRow();
            row.addContent(headerCell("<A name=\"Entity\">Entity</A>:", 1, 2));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Fetch Count", 1, 1, true));
            row.addContent(cell(stats.getEntityFetchCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Load Count", 1, 1, true));
            row.addContent(cell(stats.getEntityLoadCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Insert Count", 1, 1, true));
            row.addContent(cell(stats.getEntityInsertCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Update Count", 1, 1, true));
            row.addContent(cell(stats.getEntityUpdateCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Delete Count", 1, 1, true));
            row.addContent(cell(stats.getEntityDeleteCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell("<hr>", 1, 2, false));
            table.addContent(row);

        	table.addContent(linkToTop);
            

            // ---------------------- Detailed Entity Stats ------------------------
            
            if(!summaryOnly) {

	            row = new TableRow();
	            row.addContent(headerCell("<A name=\"EntityDetail\">Entity Statistics Detail</A>:", 1, 2));
	            table.addContent(row);
	
	            String[] cEntityNames = stats.getEntityNames();
	            
	            if(cEntityNames==null || cEntityNames.length==0) {
	                row = new TableRow();
	                row.addContent(cell("No entity names found", 1, 2, false));
	                table.addContent(row);
	            }
	            else {
	                Table subTable = new Table();
	                subTable.setCellSpacing(1);
	                subTable.setCellPadding(3);
	
	            	row = new TableRow();
	            	row.addContent(headerCell(" &nbsp; ", 1, 1));
	            	row.addContent(headerCell(" Fetches ", 1, 1));
	            	row.addContent(headerCell(" Loads ", 1, 1));
	            	row.addContent(headerCell(" Inserts ", 1, 1));
	            	row.addContent(headerCell(" Updates ", 1, 1));
	            	row.addContent(headerCell(" Deletes ", 1, 1));
	            	subTable.addContent(row);
	                
	                for (int i=0; i<cEntityNames.length; i++) {
	                    String entityName = cEntityNames[i];
	                    EntityStatistics eStats = stats.getEntityStatistics(entityName);
	                    
	                    row = new TableRow();
	                    if(i%2==0)
	                        row.setBgColor(evenRowColor);
	                    row.addContent(cell(entityName + " &nbsp;", 1, 1, true));
	                    row.addContent(cell(eStats.getFetchCount()+"", 1, 1, false));
	                    row.addContent(cell(eStats.getLoadCount()+"", 1, 1, false));
	                    row.addContent(cell(eStats.getInsertCount()+"", 1, 1, false));
	                    row.addContent(cell(eStats.getUpdateCount()+"", 1, 1, false));
	                    row.addContent(cell(eStats.getDeleteCount()+"", 1, 1, false));
	                    subTable.addContent(row);
	                }
	                
	                row = new TableRow();
	                row.addContent(cell(subTable.toHtml(), 1, 2, true));
	                table.addContent(row);
	            }
	
	            row = new TableRow();
	            row.addContent(cell("<hr>", 1, 2, false));
	            table.addContent(row);

	        	table.addContent(linkToTop);
            }            
           
            
            // ---------------------- Collection Stats ------------------------
            row = new TableRow();
            row.addContent(headerCell("<A name=\"Collection\">Collection</A>:", 1, 2));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Fetch Count", 1, 1, true));
            row.addContent(cell(stats.getCollectionFetchCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Load Count", 1, 1, true));
            row.addContent(cell(stats.getCollectionLoadCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Update Count", 1, 1, true));
            row.addContent(cell(stats.getCollectionUpdateCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Remove Count", 1, 1, true));
            row.addContent(cell(stats.getCollectionRemoveCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Recreate Count", 1, 1, true));
            row.addContent(cell(stats.getCollectionRecreateCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell("<hr>", 1, 2, false));
            table.addContent(row);

        	table.addContent(linkToTop);


            // ---------------------- Detailed Collection Stats ------------------------
            if(!summaryOnly) {

	            row = new TableRow();
	            row.addContent(headerCell("<A name=\"CollectionDetail\">Collection Statistics Detail</A>:", 1, 2));
	            table.addContent(row);
	
	            String[] cRoleNames = stats.getCollectionRoleNames();
	            
	            if(cRoleNames==null || cRoleNames.length==0) {
	                row = new TableRow();
	                row.addContent(cell("No collection roles found", 1, 2, false));
	                table.addContent(row);
	            }
	            else {
	                Table subTable = new Table();
	                subTable.setCellSpacing(1);
	                subTable.setCellPadding(3);
	
	            	row = new TableRow();
	            	row.addContent(headerCell(" &nbsp; ", 1, 1));
	            	row.addContent(headerCell(" Fetches ", 1, 1));
	            	row.addContent(headerCell(" Loads ", 1, 1));
	            	row.addContent(headerCell(" Updates ", 1, 1));
	            	row.addContent(headerCell(" Removes ", 1, 1));
	            	row.addContent(headerCell(" Recreates ", 1, 1));
	            	subTable.addContent(row);
	
	                for (int i=0; i<cRoleNames.length; i++) {
	                    String roleName = cRoleNames[i];
	                    CollectionStatistics cStats = stats.getCollectionStatistics(roleName);
	                    
	                    row = new TableRow();
	                    if(i%2==0)
	                        row.setBgColor(evenRowColor);
	                    row.addContent(cell(roleName + " &nbsp;", 1, 1, true));
	                    row.addContent(cell(cStats.getFetchCount()+"", 1, 1, false));
	                    row.addContent(cell(cStats.getLoadCount()+"", 1, 1, false));
	                    row.addContent(cell(cStats.getUpdateCount()+"", 1, 1, false));
	                    row.addContent(cell(cStats.getRemoveCount()+"", 1, 1, false));
	                    row.addContent(cell(cStats.getRecreateCount()+"", 1, 1, false));
	                    subTable.addContent(row);
	                }
	
	                row = new TableRow();
	                row.addContent(cell(subTable.toHtml(), 1, 2, true));
	                table.addContent(row);
	            }
	
	            row = new TableRow();
	            row.addContent(cell("<hr>", 1, 2, false));
	            table.addContent(row);

	        	table.addContent(linkToTop);
            }
            
            
            // ---------------------- Second Level Cache Stats ------------------------
            row = new TableRow();
            row.addContent(headerCell("<A name=\"SecondLevelCache\">Second Level Cache</A>:", 1, 2));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Hit Count", 1, 1, true));
            row.addContent(cell(stats.getSecondLevelCacheHitCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Miss Count", 1, 1, true));
            row.addContent(cell(stats.getSecondLevelCacheMissCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Put Count", 1, 1, true));
            row.addContent(cell(stats.getSecondLevelCachePutCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell("<hr>", 1, 2, false));
            table.addContent(row);

        	table.addContent(linkToTop);

            
            // ---------------------- Detailed Second Level Cache Stats ------------------------
            if(!summaryOnly) {

	            row = new TableRow();
	            row.addContent(headerCell("<A name=\"SecondLevelCacheDetail\">Second Level Cache Statistics Detail</A>:", 1, 2));
	            table.addContent(row);
	
	            String[] cRegionNames = stats.getSecondLevelCacheRegionNames();
	            
	            if(cRegionNames==null || cRegionNames.length==0) {
	                row = new TableRow();
	                row.addContent(cell("No region names found", 1, 2, false));
	                table.addContent(row);
	            }
	            else {
	                Table subTable = new Table();
	                subTable.setCellSpacing(1);
	                subTable.setCellPadding(3);
	
	            	row = new TableRow();
	            	row.addContent(headerCell(" &nbsp; ", 1, 1));
	            	row.addContent(headerCell(" Entities ", 1, 1));
	            	row.addContent(headerCell(" Hits ", 1, 1));
	            	row.addContent(headerCell(" Misses ", 1, 1));
	            	row.addContent(headerCell(" Puts ", 1, 1));
	            	row.addContent(headerCell(" In Memory ", 1, 1));
	            	row.addContent(headerCell(" On Disk ", 1, 1));
	            	row.addContent(headerCell(" Memory ", 1, 1));
	            	subTable.addContent(row);
	            	
	            	long elementsInMem = 0, elementsOnDisk = 0, putCnt = 0, missCnt = 0, hitCnt = 0, size = 0;
	
	                for (int i=0; i<cRegionNames.length; i++) {
	                    String cRegionName = cRegionNames[i];
	                    SecondLevelCacheStatistics sStats = stats.getSecondLevelCacheStatistics(cRegionName);
	                    
	                    row = new TableRow();
	                    if(i%2==0)
	                        row.setBgColor(evenRowColor);
	                    row.addContent(cell(cRegionName + " &nbsp;", 1, 1, true));
	                    row.addContent(cell(String.valueOf(sStats.getElementCountInMemory()+sStats.getElementCountOnDisk()), 1, 1, false)); //sStats.getEntries().size()
	                    row.addContent(cell(sStats.getHitCount()+"", 1, 1, false));
	                    row.addContent(cell(sStats.getMissCount()+"", 1, 1, false));
	                    row.addContent(cell(sStats.getPutCount()+"", 1, 1, false));
	                    row.addContent(cell(sStats.getElementCountInMemory()+"", 1, 1, false));
	                    row.addContent(cell(sStats.getElementCountOnDisk()+"", 1, 1, false));
	                    row.addContent(cell(sStats.getSizeInMemory()+" bytes", 1, 1, false));
	                    elementsInMem += sStats.getElementCountInMemory();
	                    elementsOnDisk += sStats.getElementCountOnDisk();
	                    putCnt += sStats.getPutCount();
	                    missCnt += sStats.getMissCount();
	                    hitCnt += sStats.getHitCount();
	                    size += sStats.getSizeInMemory();
	                    subTable.addContent(row);
	                }
	                
	            	row = new TableRow();
	            	row.addContent(headerCell("Total &nbsp;", 1, 1));
	            	row.addContent(headerCell(""+(elementsInMem+elementsOnDisk), 1, 1));
	            	row.addContent(headerCell(""+hitCnt, 1, 1));
	            	row.addContent(headerCell(""+missCnt, 1, 1));
	            	row.addContent(headerCell(""+putCnt, 1, 1));
	            	row.addContent(headerCell(""+elementsInMem, 1, 1));
	            	row.addContent(headerCell(""+elementsOnDisk, 1, 1));
	            	row.addContent(headerCell(size+" bytes", 1, 1));
	            	subTable.addContent(row);
	                
	
	                row = new TableRow();
	                row.addContent(cell(subTable.toHtml(), 1, 2, true));
	                table.addContent(row);
	            }
	
	            row = new TableRow();
	            row.addContent(cell("<hr>", 1, 2, false));
	            table.addContent(row);

	        	table.addContent(linkToTop);
            }

            
            // ---------------------- Query Stats ------------------------
           row = new TableRow();
            row.addContent(headerCell("<A name=\"Query\">Query</A>:", 1, 2));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Execution Count", 1, 1, true));
            row.addContent(cell(stats.getQueryExecutionCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Execution Max Time", 1, 1, true));
            row.addContent(cell(stats.getQueryExecutionMaxTime()+" ms", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Cache Hit Count", 1, 1, true));
            row.addContent(cell(stats.getQueryCacheHitCount()+" ms", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Cache Miss Count", 1, 1, true));
            row.addContent(cell(stats.getQueryCacheMissCount()+" ms", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Cache Put Count", 1, 1, true));
            row.addContent(cell(stats.getQueryCachePutCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell("<hr>", 1, 2, false));
            table.addContent(row);

        	table.addContent(linkToTop);


            // ---------------------- Detailed Query Stats ------------------------
            if(!summaryOnly) {

	            row = new TableRow();
	           	row.addContent(headerCell("<A name=\"QueryDetail\">Query Statistics Detail</A>:", 1, 2));
	           	table.addContent(row);
	
	           	String[] cQueryStrings = stats.getQueries();
	            
	            if(cQueryStrings==null || cQueryStrings.length==0) {
	                row = new TableRow();
	                row.addContent(cell("No query strings found", 1, 2, false));
	                table.addContent(row);
	            }
	            else {
	                Table subTable = new Table();
	                subTable.setCellSpacing(1);
	                subTable.setCellPadding(3);
	
	            	row = new TableRow();
	            	row.addContent(headerCell(" &nbsp; ", 1, 1));
	            	row.addContent(headerCell(" Execs ", 1, 1));
	            	row.addContent(headerCell(" Rows ", 1, 1));
	            	row.addContent(headerCell(" Max Time ", 1, 1));
	            	row.addContent(headerCell(" Min Time ", 1, 1));
	            	row.addContent(headerCell(" Avg Time ", 1, 1));
	            	row.addContent(headerCell(" Cache Hits ", 1, 1));
	            	row.addContent(headerCell(" Cache Misses ", 1, 1));
	            	row.addContent(headerCell(" Cache Puts ", 1, 1));
	            	subTable.addContent(row);
	
	            	for (int i=0; i<cQueryStrings.length; i++) {
	                    String cQueryString = cQueryStrings[i];
	                    QueryStatistics qStats = stats.getQueryStatistics(cQueryString);
	                    
	                    row = new TableRow();
	                    if(i%2==0)
	                        row.setBgColor(evenRowColor);
	                    row.addContent(cell(cQueryString + " &nbsp;", 1, 1, false));
	                    row.addContent(cell(qStats.getExecutionCount()+"", 1, 1, false));
	                    row.addContent(cell(qStats.getExecutionRowCount()+"", 1, 1, false));
	                    row.addContent(cell(qStats.getExecutionMaxTime()+" ms", 1, 1, false));
	                    row.addContent(cell(qStats.getExecutionMinTime()+" ms", 1, 1, false));
	                    row.addContent(cell(qStats.getExecutionAvgTime()+" ms", 1, 1, false));
	                    row.addContent(cell(qStats.getCacheHitCount()+"", 1, 1, false));
	                    row.addContent(cell(qStats.getCacheMissCount()+"", 1, 1, false));
	                    row.addContent(cell(qStats.getCachePutCount()+"", 1, 1, false));
	                    subTable.addContent(row);
	                }
	            	
	                row = new TableRow();
	                row.addContent(cell(subTable.toHtml(), 1, 2, true));
	                table.addContent(row);
	            }
	
	            row = new TableRow();
	            row.addContent(cell("<hr>", 1, 2, false));
	            table.addContent(row);

	        	table.addContent(linkToTop);
            }
            
            // Add to generated HTML
            hibStats.append(table.toHtml());
        }
        catch (Exception e) {
            hibStats.append("Exception occured: " + e.getMessage());
            e.printStackTrace();
        }
        
        return hibStats.toString();
    }
    
    /**
     * Generate header cell
     * @param content Content of cell
     * @param rowSpan Row Span
     * @param colSpan Column Span
     * @return TableHeaderCell Object
     */
    private TableHeaderCell headerCell(String content, int rowSpan, int colSpan){
    	TableHeaderCell cell = new TableHeaderCell();
    	cell.setRowSpan(rowSpan);
    	cell.setColSpan(colSpan);
    	cell.setNoWrap(true);
    	cell.setAlign("left");
    	cell.setValign("top");
    	cell.addContent("<font size=\"-1\">");
    	cell.addContent(content);
    	cell.addContent("</font>");
    	return(cell);
     }
        
    /**
     * Generate table cell (align=left and valign=top)
     * @param content Content of cell
     * @param rowSpan Row Span
     * @param colSpan Column Span
     * @param noWrap noWrap attribute (true / false)
     * @return TableCell Object
     */
    private TableCell cell(String content, int rowSpan, int colSpan, boolean noWrap){
    	TableCell cell = cell(content, rowSpan, colSpan, noWrap, "left", "top");
    	return(cell);
    }
    
    /**
     * Generate table cell
     * @param content Content of cell
     * @param rowSpan Row Span
     * @param colSpan Column Span
     * @param noWrap noWrap attribute (true / false)
     * @param align left / right / center
     * @param valign top / bottom / middle
     * @return TableCell Object
     */
    private TableCell cell(String content, int rowSpan, int colSpan, boolean noWrap, String align, String valign){
    	TableCell cell = new TableCell();
    	cell.setRowSpan(rowSpan);
    	cell.setColSpan(colSpan);
    	cell.setNoWrap(noWrap);
    	cell.setAlign(align);
    	cell.setValign(valign);
    	cell.addContent("<font size=\"-1\">");
    	cell.addContent(content);
    	cell.addContent("</font>");
    	return(cell);
    }
    
}
