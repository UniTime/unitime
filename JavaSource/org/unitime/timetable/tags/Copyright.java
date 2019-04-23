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
*/
package org.unitime.timetable.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.commons.Debug;

/**
 * @author Tomas Muller
 */
public class Copyright extends BodyTagSupport {
	private static final long serialVersionUID = -4463420165596054395L;
	private boolean iBr = true;
	
	public void setBr(boolean br) { iBr = br; }
	public boolean isBr() { return iBr; }

	public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
    	// WARNING: Changing or removing the following copyright notice will violate the license terms.
    	// If you need a different licensing, please contact us at support@unitime.org
    	String body = 
    		"<a class='unitime-FooterLink' href='http://www.unitime.org' tabIndex='-1'>&copy;&nbsp;2008&nbsp;-&nbsp;2019&nbsp;The&nbsp;Apereo&nbsp;Foundation</a>," + 
    		(isBr() ? "<br>" : " ") + 
    		"<a class='unitime-FooterLink' href='http://www.unitime.org/uct_license.php' tabIndex='-1'>distributed&nbsp;under&nbsp;the&nbsp;Apache&nbsp;License,&nbsp;Version&nbsp;2.</a>";
        try {
            pageContext.getOut().print(body);
        }
        catch (Exception e) {
            Debug.error("Could not display copyright notice: " + e.getMessage());
        }
		return EVAL_PAGE;
    }

}
