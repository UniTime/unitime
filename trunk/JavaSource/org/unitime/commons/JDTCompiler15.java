/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.commons;

import org.apache.tools.ant.taskdefs.Javac;
import org.eclipse.jdt.core.JDTCompilerAdapter;

/**
 * Hack to allow all the application to run in GWT hosted mode. Set -Dbuild.compiler="org.unitime.commons.JDTCompiler15" in the build configuration.
 * 
 * See http://code.google.com/p/google-web-toolkit/issues/detail?id=3557 for more details.
 *
 * @author Tomas Muller
 */
public class JDTCompiler15 extends JDTCompilerAdapter {
    @Override
    public void setJavac(Javac attributes) {
            if (attributes.getTarget() == null) {
                    attributes.setTarget("1.5");
            }
            if (attributes.getSource() == null) {
                    attributes.setSource("1.5");
            }
            super.setJavac(attributes);
    }
}
