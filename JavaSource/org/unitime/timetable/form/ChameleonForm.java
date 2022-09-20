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
package org.unitime.timetable.form;

import org.unitime.timetable.action.UniTimeAction;

/** 
 * @author Tomas Muller
 */
public class ChameleonForm implements UniTimeForm {
	private static final long serialVersionUID = -2016021904772358915L;

	private String puid;
    private String op;
    private String name;
    private boolean canLookup;
    
    @Override
    public void validate(UniTimeAction action) {}

    @Override
    public void reset() {
        puid = null;
        op = null;
        name = null;
        canLookup = false;
    }

    public String getPuid() {
        return puid;
    }
    public void setPuid(String puid) {
        this.puid = puid;
    }
    
    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isCanLookup() { return canLookup; }
    public void setCanLookup(boolean canLookup) { this.canLookup = canLookup; }
}
