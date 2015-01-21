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
