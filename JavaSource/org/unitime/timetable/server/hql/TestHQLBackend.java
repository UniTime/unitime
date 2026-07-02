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
package org.unitime.timetable.server.hql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.Query;
import org.hibernate.query.internal.ParameterMetadataImpl;
import org.hibernate.query.internal.QueryParameterBindingsImpl;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.query.spi.SqmQuery;
import org.hibernate.query.sqm.internal.DomainParameterXref;
import org.hibernate.query.sqm.sql.SqmTranslation;
import org.hibernate.query.sqm.sql.StandardSqmTranslatorFactory;
import org.hibernate.query.sqm.tree.SqmStatement;
import org.hibernate.query.sqm.tree.delete.SqmDeleteStatement;
import org.hibernate.query.sqm.tree.insert.SqmInsertStatement;
import org.hibernate.query.sqm.tree.select.SqmSelectStatement;
import org.hibernate.query.sqm.tree.update.SqmUpdateStatement;
import org.hibernate.sql.ast.spi.SqlAstCreationContext;
import org.hibernate.sql.ast.tree.delete.DeleteStatement;
import org.hibernate.sql.ast.tree.insert.InsertStatement;
import org.hibernate.sql.ast.tree.select.SelectStatement;
import org.hibernate.sql.ast.tree.update.UpdateStatement;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.export.Exporter.Printer;
import org.unitime.timetable.export.hql.SavedHqlExportToCSV;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SavedHQLException;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.IdValue;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.Table;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.TestHQLRequest;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.TestHQLResponse;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

import jakarta.persistence.Tuple;

@GwtRpcImplements(TestHQLRequest.class)
public class TestHQLBackend implements GwtRpcImplementation<TestHQLRequest, TestHQLResponse>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	private static Log sLog = LogFactory.getLog(TestHQLBackend.class);

	@Override
	public TestHQLResponse execute(TestHQLRequest request, SessionContext context) {
		context.checkPermission(Right.TestHQL);
		
		TestHQLResponse response = new TestHQLResponse();
		switch (request.getOperation()) {
		case LOAD:
			for (SavedHQL.Option o: SavedHQL.Option.values()) {
				if (!o.allowSingleSelection() && !o.allowMultiSelection()) continue;
				SavedHQLInterface.Option option = new SavedHQLInterface.Option();
				option.setMultiSelect(o.allowMultiSelection());
				option.setName(HQLOptionsBackend.getLocalizedText(o));
				option.setType(o.name());
				Map<Long, String> values = o.values(context.getUser());
				if (values == null || values.isEmpty()) continue;
				for (Map.Entry<Long, String> e: values.entrySet()) {
					SavedHQLInterface.IdValue v = new SavedHQLInterface.IdValue();
					v.setText(e.getValue());
					v.setValue(e.getKey().toString());
					option.values().add(v);
				}
				Collections.sort(option.values());
				response.addOption(option);
			}
			response.setMaxRows(ApplicationProperty.TestHQLMaxLines.intValue());
			break;
		case CLEAR_CACHE:
			HibernateUtil.clearCache();
			break;
		case EXECUTE:
			execute(request, response, context);
		}
		return response;
	}
	
	protected void execute(TestHQLRequest request, final TestHQLResponse response, SessionContext context) {
		try {
			int limit = ApplicationProperty.TestHQLMaxLines.intValue();
			if (request.getMaxRows() != null)
				limit = request.getMaxRows();
	        String hql = request.getQuery();
	        for (SavedHQL.Option o: SavedHQL.Option.values()) {
				if (hql.indexOf("%" + o.name() + "%") >= 0) {
					String value = null;
					for (IdValue v: request.getOptions())
						if (o.name().equals(v.getValue())) { value = v.getText(); break; }
					if (value == null || value.isEmpty()) {
						Map<Long, String> vals = o.values(context.getUser());
						if (vals == null || vals.isEmpty())
							throw new SavedHQLException(MESSAGES.errorUnableToSetParameterNoValues(o.name()));
						value = "";
						for (Long id: vals.keySet()) {
							if (!value.isEmpty()) value += ",";
							value += id.toString();
							if (!o.allowMultiSelection()) break;
						}
					}
					hql = hql.replace("%" + o.name() + "%", "(" + value + ")");
				}
			}
			if (hql.indexOf("%USER%") >= 0)
				hql = hql.replace("%USER%", "'" + HibernateUtil.escapeSql(context.getUser().getExternalUserId()) + "'");
			
	        _RootDAO rdao = new _RootDAO();
	        Session hibSession = rdao.getSession();
	        Query<Tuple> q = null;
	        MutationQuery updateQuery = null;
	        try {
	        	q = hibSession.createQuery(hql, Tuple.class);
	        } catch (IllegalArgumentException e) {
	        	// update query
	        	updateQuery = hibSession.createMutationQuery(hql);
	        }
	        
	        try {
	        	SqmStatement sqm = null;
	        	if (updateQuery != null) {
	        		sqm = ((SqmQuery)updateQuery).getSqmStatement();
	        	} else {
	        		sqm = ((SqmQuery)q).getSqmStatement();
	        	}
	        	SessionFactoryImplementor sfi = (SessionFactoryImplementor)hibSession.getSessionFactory();
	        	Dialect dialect = sfi.getJdbcServices().getDialect();
	        	if (sqm instanceof SqmSelectStatement) {
		        	SqmTranslation tr = new StandardSqmTranslatorFactory().createSelectTranslator(
		        			(SqmSelectStatement)sqm,
		        			QueryOptions.NONE,
		        			DomainParameterXref.from(sqm),
		        			QueryParameterBindingsImpl.from(ParameterMetadataImpl.EMPTY, sfi),
		        			new LoadQueryInfluencers(sfi),
		        			(SqlAstCreationContext)hibSession.getSessionFactory(),
		        			false).translate();
		        	String sql = dialect.getSqlAstTranslatorFactory()
		        			.buildSelectTranslator(sfi, (SelectStatement)tr.getSqlAst())
		        			.translate(JdbcParameterBindings.NO_BINDINGS, QueryOptions.NONE).getSqlString();
		        	response.setSQL(sql);
	        	} else if (sqm instanceof SqmDeleteStatement) {
	        		SqmTranslation tr = new StandardSqmTranslatorFactory().createSimpleDeleteTranslator(
		        			(SqmDeleteStatement)sqm,
		        			QueryOptions.NONE,
		        			DomainParameterXref.from(sqm),
		        			QueryParameterBindingsImpl.from(ParameterMetadataImpl.EMPTY, sfi),
		        			new LoadQueryInfluencers(sfi),
		        			(SqlAstCreationContext)hibSession.getSessionFactory()).translate();
		        	String sql = dialect.getSqlAstTranslatorFactory()
		        			.buildMutationTranslator(sfi, (DeleteStatement)tr.getSqlAst())
		        			.translate(JdbcParameterBindings.NO_BINDINGS, QueryOptions.NONE).getSqlString();
		        	response.setSQL(sql);
	        	} else if (sqm instanceof SqmInsertStatement) {
	        		SqmTranslation tr = new StandardSqmTranslatorFactory().createInsertTranslator(
		        			(SqmInsertStatement)sqm,
		        			QueryOptions.NONE,
		        			DomainParameterXref.from(sqm),
		        			QueryParameterBindingsImpl.from(ParameterMetadataImpl.EMPTY, sfi),
		        			new LoadQueryInfluencers(sfi),
		        			(SqlAstCreationContext)hibSession.getSessionFactory()).translate();
		        	String sql = dialect.getSqlAstTranslatorFactory()
		        			.buildMutationTranslator(sfi, (InsertStatement)tr.getSqlAst())
		        			.translate(JdbcParameterBindings.NO_BINDINGS, QueryOptions.NONE).getSqlString();
		        	response.setSQL(sql);
	        	} else if (sqm instanceof SqmUpdateStatement) {
	        		SqmTranslation tr = new StandardSqmTranslatorFactory().createSimpleUpdateTranslator(
		        			(SqmUpdateStatement)sqm,
		        			QueryOptions.NONE,
		        			DomainParameterXref.from(sqm),
		        			QueryParameterBindingsImpl.from(ParameterMetadataImpl.EMPTY, sfi),
		        			new LoadQueryInfluencers(sfi),
		        			(SqlAstCreationContext)hibSession.getSessionFactory()).translate();
		        	String sql = dialect.getSqlAstTranslatorFactory()
		        			.buildMutationTranslator(sfi, (UpdateStatement)tr.getSqlAst())
		        			.translate(JdbcParameterBindings.NO_BINDINGS, QueryOptions.NONE).getSqlString();
		        	response.setSQL(sql);
	        	}
	        } catch (Exception e) {
	        	sLog.error(e.getMessage(), e);
	        }
	        
	        if (q != null) {
	        	q.setCacheable(true);
		        q.setFirstResult(request.getFromRow());
		        if (limit > 0) q.setMaxResults(limit + 1);
		        
		        response.setTable(new Table());
				SavedHqlExportToCSV.enumerate(new Printer() {
					@Override
					public void printLine(String... fields) throws IOException {
						response.getTable().add(fields);
					}
					
					@Override
					public void printHeader(String... fields) throws IOException {
						response.getTable().add(fields);
					}
					
					@Override
					public void hideColumn(int col) {}
					
					@Override
					public String getContentType() { return null; }
					
					@Override
					public void flush() throws IOException {}
					
					@Override
					public void close() throws IOException {}
				}, q);
	        } else if (updateQuery != null) {
	            Transaction tx = null;
	            try {
	                tx = hibSession.beginTransaction();
	                int i = updateQuery.executeUpdate();
	                response.setMessage(MSG.queryLinesUpdated(i));
	                tx.commit();
	            } catch (Exception ex) {
	                if (tx!=null && tx.isActive()) tx.rollback();
	                throw ex;
	            }
	            hibSession.flush();
	            HibernateUtil.clearCache();
	        }
		} catch (GwtRpcException e) {
			throw e;
	    } catch (Exception e) {
	    	String message = null;
	    	Throwable f = e;
	    	while (f != null) {
	    		if (f instanceof JDBCException) {
	    			SQLException s = ((JDBCException)f).getSQLException();
	    			if (s != null && s.getMessage() != null && !s.getMessage().isEmpty()) {
	    				message = s.getMessage();
	        			break;
	    			}
	    		}
	    		if (f instanceof HibernateException)
	    			message = f.getMessage();
	    		if (f instanceof IllegalArgumentException)
	    			message = f.getMessage();
	    		f = f.getCause();
	    	}
	    	
	    	if (message != null && !message.isEmpty()) {
	    		sLog.error(message, e);
	    		throw new GwtRpcException(message, e);
	    	} else { 
	    		sLog.error(e.getMessage(), e);
	    		throw new GwtRpcException(e.getMessage(), e);
	    	}
	    }
	}

}
