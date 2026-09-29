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
package org.unitime.timetable.server.administration.utils;

import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.gwt.client.admin.HibernateStatisticsPage.HibernateStatisticsRequest;
import org.unitime.timetable.gwt.client.admin.HibernateStatisticsPage.HibernateStatisticsResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(HibernateStatisticsRequest.class)
public class HibernateStatisticsBackend implements GwtRpcImplementation<HibernateStatisticsRequest, HibernateStatisticsResponse>{

	@Override
	public HibernateStatisticsResponse execute(HibernateStatisticsRequest request, SessionContext context) {
		context.checkPermission(Right.HibernateStatistics);
		switch (request.getOperation()) {
		case DISABLE:
			HibernateUtil.getSession().getSessionFactory().getStatistics().setStatisticsEnabled(false);
			break;
		case ENABLE:
			HibernateUtil.getSession().getSessionFactory().getStatistics().setStatisticsEnabled(true);
			break;
		default:
			break;
		}
		
		HibernateStatisticsResponse response = new HibernateStatisticsResponse();
		response.setDetails(request.isDetails());
		response.setEnabled(HibernateUtil.getSession().getSessionFactory().getStatistics().isStatisticsEnabled());
		if (response.isEnabled())
			fillInStatistics(request.isPrint(), response, context);
		return response;
	}
	
	protected void fillInStatistics(boolean print, HibernateStatisticsResponse response, SessionContext context) {
		Statistics stats = HibernateUtil.getSession().getSessionFactory().getStatistics();
		
		CellInterface topLinks = null;
		if (!print) {
			TableInterface links = new TableInterface();
			links.setAnchor("top");
			topLinks = links.addLine().addCell().setTextAlignment(Alignment.CENTER);
			topLinks.add("Metrics").setUrl("#metric").setClassName("link");
			topLinks.add(" | ");
			topLinks.add("Entity").setUrl("#entity").setClassName("link");
			if (response.isDetails()) {
				topLinks.add(" - ");
				topLinks.add("Details").setUrl("#entity-details").setClassName("link");
			}
			topLinks.add(" | ");
			topLinks.add("Collection").setUrl("#collection").setClassName("link");
			if (response.isDetails()) {
				topLinks.add(" - ");
				topLinks.add("Details").setUrl("#collection-details").setClassName("link");
			}
			topLinks.add(" | ");
			topLinks.add("Cache").setUrl("#cache").setClassName("link");
			if (response.isDetails()) {
				topLinks.add(" - ");
				topLinks.add("Details").setUrl("#cache-details").setClassName("link");
			}
			topLinks.add(" | ");
			topLinks.add("Query").setUrl("#query").setClassName("link");
			if (response.isDetails()) {
				topLinks.add(" - ");
				topLinks.add("Details").setUrl("#query-details").setClassName("link");
			}
			response.addTable(links);
		}
		
		TableInterface metric = new TableInterface("HibernateStats.Metric", "Metric");
		metric.addStyle("width: 400px;");
		metric.setAnchor("metric");
		metric.addProperty("Start Time").setText(stats.getStart().toString()).setTextAlignment(Alignment.RIGHT);
		metric.addProperty("Connect Count").setText(stats.getConnectCount()).setTextAlignment(Alignment.RIGHT);
		metric.addProperty("Flush Count").setText(stats.getFlushCount()).setTextAlignment(Alignment.RIGHT);
		metric.addProperty("Session Open Count").setText(stats.getSessionOpenCount()).setTextAlignment(Alignment.RIGHT);
		metric.addProperty("Session Close").setText(stats.getSessionCloseCount()).setTextAlignment(Alignment.RIGHT);
		metric.addProperty("Transaction Count").setText(stats.getTransactionCount()).setTextAlignment(Alignment.RIGHT);
		metric.addProperty("Successful Transaction Count").setText(stats.getSuccessfulTransactionCount()).setTextAlignment(Alignment.RIGHT);
		metric.addProperty("Prepare Statement Count").setText(stats.getPrepareStatementCount()).setTextAlignment(Alignment.RIGHT);
		metric.addProperty("Close Statement Count").setText(stats.getCloseStatementCount()).setTextAlignment(Alignment.RIGHT);
		metric.addProperty("Optimistic Failure Count").setText(stats.getOptimisticFailureCount()).setTextAlignment(Alignment.RIGHT);
		response.addTable(metric);
		
		TableInterface entity = new TableInterface("HibernateStats.Entity", "Entity");
		entity.setAnchor("entity");
		entity.addStyle("width: 400px;");
		entity.addProperty("Fetch Count").setText(stats.getEntityFetchCount()).setTextAlignment(Alignment.RIGHT);
		entity.addProperty("Load Count").setText(stats.getEntityLoadCount()).setTextAlignment(Alignment.RIGHT);
		entity.addProperty("Insert Count").setText(stats.getEntityInsertCount()).setTextAlignment(Alignment.RIGHT);
		entity.addProperty("Update Count").setText(stats.getEntityUpdateCount()).setTextAlignment(Alignment.RIGHT);
		entity.addProperty("Delete Count").setText(stats.getEntityDeleteCount()).setTextAlignment(Alignment.RIGHT);
		response.addTable(entity);
		
		if (response.isDetails()) {
			TableInterface details = new TableInterface("HibernateStats.EntityDetail", "Entity Statistics Detail");
			details.setAnchor("entity-details");
			String[] cEntityNames = stats.getEntityNames();
			LineInterface header = details.addHeader();
			header.addCell("Name");
			header.addCell("Fetches").setTextAlignment(Alignment.RIGHT);
			header.addCell("Loads").setTextAlignment(Alignment.RIGHT);
			header.addCell("Inserts").setTextAlignment(Alignment.RIGHT);
			header.addCell("Updates").setTextAlignment(Alignment.RIGHT);
			header.addCell("Deletes").setTextAlignment(Alignment.RIGHT);
			for (CellInterface cell: header.getCells()) {
        		cell.setClassName("WebTableHeader");
        		cell.setSortable(true);
        	}
			if (cEntityNames==null || cEntityNames.length==0) {
				details.setErrorMessage("No etitities found.");
			} else {
				for (String entityName: cEntityNames) {
                    EntityStatistics eStats = stats.getEntityStatistics(entityName);
                    LineInterface line = details.addLine();
                    line.addCell(entityName.substring(entityName.lastIndexOf('.') + 1));
                    line.addCell(eStats.getFetchCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(eStats.getLoadCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(eStats.getInsertCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(eStats.getUpdateCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(eStats.getDeleteCount()).setTextAlignment(Alignment.RIGHT);
				}
			}
			response.addTable(details);
		}
		
		TableInterface collection = new TableInterface("HibernateStats.Collection", "Collection");
		collection.setAnchor("collection");
		collection.addStyle("width: 400px;");
		collection.addProperty("Fetch Count").setText(stats.getCollectionFetchCount()).setTextAlignment(Alignment.RIGHT);
		collection.addProperty("Load Count").setText(stats.getCollectionLoadCount()).setTextAlignment(Alignment.RIGHT);
		collection.addProperty("Update Count").setText(stats.getCollectionUpdateCount()).setTextAlignment(Alignment.RIGHT);
		collection.addProperty("Remove Count").setText(stats.getCollectionRemoveCount()).setTextAlignment(Alignment.RIGHT);
		collection.addProperty("Recreate Count").setText(stats.getCollectionRecreateCount()).setTextAlignment(Alignment.RIGHT);
		response.addTable(collection);
		
		if (response.isDetails()) {
			TableInterface details = new TableInterface("HibernateStats.CollectionDetail", "Colection Statistics Detail");
			details.setAnchor("collection-details");
			String[] cRoleNames = stats.getCollectionRoleNames();
			LineInterface header = details.addHeader();
			header.addCell("Collection");
			header.addCell("Fetches").setTextAlignment(Alignment.RIGHT);
			header.addCell("Loads").setTextAlignment(Alignment.RIGHT);
			header.addCell("Updates").setTextAlignment(Alignment.RIGHT);
			header.addCell("Removes").setTextAlignment(Alignment.RIGHT);
			header.addCell("Recreates").setTextAlignment(Alignment.RIGHT);
			for (CellInterface cell: header.getCells()) {
        		cell.setClassName("WebTableHeader");
        		cell.setSortable(true);
        	}
			if (cRoleNames==null || cRoleNames.length==0) {
				details.setErrorMessage("No colections found.");
			} else {
				for (String roleName: cRoleNames) {
                    CollectionStatistics cStats = stats.getCollectionStatistics(roleName);
                    LineInterface line = details.addLine();
                    String col = roleName.substring(roleName.lastIndexOf('.') + 1);
                    String ent = roleName.substring(0, roleName.lastIndexOf('.') - 1);
                    line.addCell(ent.substring(ent.lastIndexOf('.') + 1) + "." + col);
                    line.addCell(cStats.getFetchCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(cStats.getLoadCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(cStats.getUpdateCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(cStats.getRemoveCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(cStats.getRecreateCount()).setTextAlignment(Alignment.RIGHT);
				}
			}
			response.addTable(details);
		}
		
		TableInterface cache = new TableInterface("HibernateStats.Cache", "Second Level Cache");
		cache.setAnchor("cache");
		cache.addStyle("width: 400px;");
		cache.addProperty("Hit Count").setText(stats.getSecondLevelCacheHitCount()).setTextAlignment(Alignment.RIGHT);
		cache.addProperty("Miss Count").setText(stats.getSecondLevelCacheMissCount()).setTextAlignment(Alignment.RIGHT);
		cache.addProperty("Put Count").setText(stats.getSecondLevelCachePutCount()).setTextAlignment(Alignment.RIGHT);
		response.addTable(cache);
		
		if (response.isDetails()) {
			TableInterface details = new TableInterface("HibernateStats.CacheDetail", "Second Level Cache Statistics Detail");
			details.setAnchor("cache-details");
			String[] cRegionNames = stats.getSecondLevelCacheRegionNames();
			LineInterface header = details.addHeader();
			header.addCell("Region");
			header.addCell("Entities").setTextAlignment(Alignment.RIGHT);
			header.addCell("Hits").setTextAlignment(Alignment.RIGHT);
			header.addCell("Misses").setTextAlignment(Alignment.RIGHT);
			header.addCell("Puts").setTextAlignment(Alignment.RIGHT);
			for (CellInterface cell: header.getCells()) {
        		cell.setClassName("WebTableHeader");
        		cell.setSortable(true);
        	}
			if (cRegionNames==null || cRegionNames.length==0) {
				details.setErrorMessage("No regions found.");
			} else {
				long[] totals = new long[] { 0l, 0l, 0l, 0l };
				for (String region: cRegionNames) {
					if (region.indexOf('.') < 0) continue;
					CacheRegionStatistics sStats = null;
					try {
						sStats = stats.getDomainDataRegionStatistics(region);
					} catch (IllegalArgumentException e) {
						continue;
					}
                    LineInterface line = details.addLine();
                    String col = region.substring(region.lastIndexOf('.') + 1);
                    String ent = null;
                    if (Character.isLowerCase(col.charAt(0)))
                    	ent = region.substring(0, region.lastIndexOf('.'));
                    line.addCell(ent == null ? col : ent.substring(ent.lastIndexOf('.') + 1) + "." + col);
                    line.addCell(sStats.getElementCountInMemory()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(sStats.getHitCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(sStats.getMissCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(sStats.getPutCount()).setTextAlignment(Alignment.RIGHT);
                    totals[0] += sStats.getElementCountInMemory();
                    totals[1] += sStats.getHitCount();
                    totals[2] += sStats.getMissCount();
                    totals[3] += sStats.getPutCount();
				}
				LineInterface line = details.addLine();
				line.addCell("Totals").addStyle("font-weight: bold;").setComparable("ZZZZZZZZZZ");
				line.setBgColor("#d0e4f6");
				for (int i = 0; i < totals.length; i++)
					line.addCell(totals[i]).setComparable(Long.MAX_VALUE).addStyle("font-weight: bold;").setTextAlignment(Alignment.RIGHT);
			}
			response.addTable(details);
		}
		
		TableInterface query = new TableInterface("HibernateStats.Query", "Query");
		query.setAnchor("query");
		query.addStyle("width: 400px;");
		query.addProperty("Execution Count").setText(stats.getQueryExecutionCount()).setTextAlignment(Alignment.RIGHT);
		query.addProperty("Execution Max Time").setText(stats.getQueryExecutionMaxTime() + " ms").setTextAlignment(Alignment.RIGHT);
		query.addProperty("Cache Hit Count").setText(stats.getQueryCacheHitCount()).setTextAlignment(Alignment.RIGHT);
		query.addProperty("Cache Miss Count").setText(stats.getQueryCacheMissCount()).setTextAlignment(Alignment.RIGHT);
		query.addProperty("Cache Put Count").setText(stats.getQueryCachePutCount()).setTextAlignment(Alignment.RIGHT);
		response.addTable(query);
		
		if (response.isDetails()) {
			TableInterface details = new TableInterface("HibernateStats.QueryDetail", "Query Statistics Detail");
			details.setAnchor("query-details");
			String[] cQueryStrings = stats.getQueries();
			LineInterface header = details.addHeader();
			header.addCell("Query");
			header.addCell("Execs").setTextAlignment(Alignment.RIGHT);
			header.addCell("Rows").setTextAlignment(Alignment.RIGHT);
			header.addCell("Max Time").setTextAlignment(Alignment.RIGHT);
			header.addCell("Min Time").setTextAlignment(Alignment.RIGHT);
			header.addCell("Avg Time").setTextAlignment(Alignment.RIGHT);
			header.addCell("Cache Hits").setTextAlignment(Alignment.RIGHT);
			header.addCell("Cache Misses").setTextAlignment(Alignment.RIGHT);
			header.addCell("Cache Puts").setTextAlignment(Alignment.RIGHT);
			
			for (CellInterface cell: header.getCells()) {
        		cell.setClassName("WebTableHeader");
        		cell.setSortable(true);
        	}
			if (cQueryStrings==null || cQueryStrings.length==0) {
				details.setErrorMessage("No queries found.");
			} else {
				for (String q: cQueryStrings) {
					QueryStatistics qStats = stats.getQueryStatistics(q);
                    LineInterface line = details.addLine();
                    line.addCell(q).addStyle("max-width: 800px; white-space: pre-line; word-wrap: anywhere;");
                    line.addCell(qStats.getExecutionCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(qStats.getExecutionRowCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(qStats.getExecutionMaxTime()).setTextAlignment(Alignment.RIGHT);
                    if (qStats.getExecutionCount() > 1)
                    	line.addCell(qStats.getExecutionMinTime()).setTextAlignment(Alignment.RIGHT);
                    else
                    	line.addCell();
                    line.addCell(qStats.getExecutionAvgTime()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(qStats.getCacheHitCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(qStats.getCacheMissCount()).setTextAlignment(Alignment.RIGHT);
                    line.addCell(qStats.getCachePutCount()).setTextAlignment(Alignment.RIGHT);
				}
			}
			response.addTable(details);
		}
	}

}
