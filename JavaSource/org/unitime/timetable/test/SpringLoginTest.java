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
package org.unitime.timetable.test;


import org.cpsolver.ifs.util.ToolBox;
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
 *
 * @author Tomas Muller
 */
public class SpringLoginTest {
	
	public static void main(String[] args) {
		try {
			// Configure logging
			ToolBox.configureLogging();
			
			// Configure hibernate
			HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
			
			// Setup application context
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/applicationContext.xml", "/securityContext.xml");
			
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
			
			context.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
