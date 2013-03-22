/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.test;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.security.UserContext;

/**
 * Simple login test.
 * Example usage:
 * 		java \
 * 			-Dtmtbl.custom.properties=${TOMCAT_HOME}/custom.properties \
 * 			-cp "${TOMCAT_HOME}/webapps/UniTime/WEB-INF/lib/*:${TOMCAT_HOME}/webapps/UniTime/WEB-INF/classes:${TOMCAT_HOME}/webapps/UniTime/WEB-INF" \
 * 			org.unitime.timetable.test.SpringLoginTest
 */
public class SpringLoginTest {
	
	public static void main(String[] args) {
		try {
			// Configure logging
			ToolBox.configureLogging();
			
			// Configure hibernate
			HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
			
			// Setup application context
			ApplicationContext context = new ClassPathXmlApplicationContext("/applicationContext.xml", "/securityContext.xml");
			
			// Get username and password
			String username = System.console().readLine("[%s]", "Username:");
			char[] passwd = System.console().readPassword("[%s]", "Password:");
			
			// Try to authenticate
			SecurityContextHolder.getContext().setAuthentication(
					context.getBean("authenticationManager", AuthenticationManager.class).authenticate(
							new UsernamePasswordAuthenticationToken(username, new String(passwd))
					));

			// Print authentication
			System.out.println("Authentication: " + SecurityContextHolder.getContext().getAuthentication());
			
			// Get user context
			UserContext user = (UserContext)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			
			// Print user name and his/her authorities
			System.out.println("User name:" + user.getName());
			System.out.println("Authorities:" + user.getAuthorities());
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

}
