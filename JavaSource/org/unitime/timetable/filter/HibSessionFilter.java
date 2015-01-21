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
package org.unitime.timetable.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.cpsolver.ifs.util.JProf;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.base._BaseRootDAO;


/**
 * This filter is used to close Hibernate Session when response 
 * goes back to user as suggested in Hibernate 3 documentation: 
 * http://www.hibernate.org/hib_docs/v3/reference/en/pdf/hibernate_reference.pdf
 * 19.1.3. Initializing collections and proxies
 * @author Dagmar Murray, Tomas Muller
 */
public class HibSessionFilter implements Filter {

	private FilterConfig filterConfig = null;
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig arg0) throws ServletException {
		filterConfig = arg0;
        Debug.debug("Initializing filter, obtaining Hibernate SessionFactory from HibernateUtil");
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(
	        ServletRequest request, 
	        ServletResponse response,
			FilterChain chain ) throws IOException, ServletException {
	    
		if (filterConfig==null) return;
		
		if (request.getAttribute("TimeStamp")==null)
			request.setAttribute("TimeStamp", new Double(JProf.currentTimeSec()));
		
		try {
			// Process request
			chain.doFilter(request,response);

        	_BaseRootDAO.closeCurrentThreadSessions();
		} catch (Throwable ex) {
			_BaseRootDAO.rollbackCurrentThreadSessions();

            if (ex instanceof ServletException) throw (ServletException)ex;
            if (ex instanceof IOException) throw (IOException)ex;
			if (ex instanceof RuntimeException) throw (RuntimeException)ex;

            // Let others handle it... maybe another interceptor for exceptions?
            throw new ServletException(ex);
        }
 		
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	    this.filterConfig = null;
	}
}
