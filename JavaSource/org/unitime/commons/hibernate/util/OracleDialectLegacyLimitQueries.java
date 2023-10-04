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
package org.unitime.commons.hibernate.util;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.OracleSqlAstTranslator;
import org.hibernate.dialect.pagination.LegacyOracleLimitHandler;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.mutation.EntityMutationTarget;
import org.hibernate.query.sqm.FetchClauseType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.SqlAstTranslatorFactory;
import org.hibernate.sql.ast.spi.StandardSqlAstTranslatorFactory;
import org.hibernate.sql.ast.tree.Statement;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.select.QueryPart;
import org.hibernate.sql.ast.tree.select.QuerySpec;
import org.hibernate.sql.exec.spi.JdbcOperation;
import org.hibernate.sql.model.MutationOperation;
import org.hibernate.sql.model.internal.OptionalTableUpdate;

/**
 * @author Tomas Muller
 */
public class OracleDialectLegacyLimitQueries extends OracleDialect {
	
	public OracleDialectLegacyLimitQueries() {
		super();
	}
	
	public OracleDialectLegacyLimitQueries(DatabaseVersion version) {
		super(version);
	}
	
	@Override
	public boolean supportsFetchClause(FetchClauseType type) {
		return false;
	}
	
	@Override
	public SqlAstTranslatorFactory getSqlAstTranslatorFactory() {
		return new StandardSqlAstTranslatorFactory() {
			@Override
			protected <T extends JdbcOperation> SqlAstTranslator<T> buildTranslator(
					SessionFactoryImplementor sessionFactory, Statement statement) {
				return new OracleSqlAstTranslatorLegacyLimitQueries<>( sessionFactory, statement );
			}
		};
	}
	
	@Override
	public MutationOperation createOptionalTableUpdateOperation(
			EntityMutationTarget mutationTarget,
			OptionalTableUpdate optionalTableUpdate,
			SessionFactoryImplementor factory) {
		final OracleSqlAstTranslator<?> translator = new OracleSqlAstTranslatorLegacyLimitQueries<>( factory, optionalTableUpdate );
		return translator.createMergeOperation( optionalTableUpdate );
	}
	
	
	protected static class OracleSqlAstTranslatorLegacyLimitQueries<T extends JdbcOperation> extends OracleSqlAstTranslator<T> {

		public OracleSqlAstTranslatorLegacyLimitQueries(SessionFactoryImplementor sessionFactory, Statement statement) {
			super(sessionFactory, statement);
		}

		/**
		 * When fetch clause type is ROWS_ONLY, generate the query just like the {@link LegacyOracleLimitHandler} would
		 */
		@Override
		protected void emulateFetchOffsetWithWindowFunctions(
				QueryPart queryPart,
				Expression offsetExpression,
				Expression fetchExpression,
				FetchClauseType fetchClauseType,
				boolean emulateFetchClause) {
			if (emulateFetchClause && fetchClauseType == FetchClauseType.ROWS_ONLY) {
				// use the rownum just like in the LegacyOracleLimitHandler
				if ( offsetExpression != null ) {
					appendSql( "select * from (select row_.*,rownum rownum_ from (" );
					emulateFetchOffsetWithWindowFunctionsVisitQueryPart(((QuerySpec)queryPart).asSubQuery());
					appendSql( ") row_ where rownum<=");
					offsetExpression.accept( this ); appendSql( '+' ); fetchExpression.accept( this );
					appendSql(") where rownum_>");
					offsetExpression.accept( this ); 
				} else {
					appendSql( "select * from (" );
					emulateFetchOffsetWithWindowFunctionsVisitQueryPart(((QuerySpec)queryPart).asSubQuery());
					appendSql( ") where rownum<=" );
					fetchExpression.accept( this );
				}
				// Render the FOR UPDATE clause in the outer query
				if (queryPart instanceof QuerySpec) {
					visitForUpdateClause( (QuerySpec) queryPart );
				}
			} else {
				super.emulateFetchOffsetWithWindowFunctions(queryPart, offsetExpression, fetchExpression, fetchClauseType, emulateFetchClause);
			}
		}
	}
}
