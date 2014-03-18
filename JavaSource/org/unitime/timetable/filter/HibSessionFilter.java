/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
