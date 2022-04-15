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
package org.unitime.timetable.action;

import java.util.Iterator;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;

/**
 * @author Tomas Muller
 */
@Action(value="itypeDescList", results = {
		@Result(name="success", type = "tiles", location="itypeDescList.tiles"),
	})
@TilesDefinitions(value = {
		@TilesDefinition(name = "itypeDescList.tiles", extend = "baseLayout", putAttributes = {
			@TilesPutAttribute(name = "title", value = "Instructional Types"),
			@TilesPutAttribute(name = "body", value = "/admin/itypeDescList.jsp"),
			@TilesPutAttribute(name = "showNavigation", value = "false", cascade = true)
		})
	})
public class ItypeDescListAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = 1462237807775084625L;
	protected CourseMessages MSG = Localization.create(CourseMessages.class);
	
	protected String ord;
	public String getOrd() { return ord; }
	public void setOrd(String ord) { this.ord = ord; }
	
	protected Integer id;
	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }

	@Override
	public String execute() throws Exception {
		// Check if user is logged in
		sessionContext.checkPermission(Right.InstructionalTypes);

		// Create new table
		PdfWebTable webTable = new PdfWebTable(6, null, "itypeDescList.action?ord=%%",
				new String[] { MSG.fieldIType(), MSG.fieldAbbreviation(), MSG.fieldName(), MSG.fieldReference(), MSG.fieldType(), MSG.fieldParent(), MSG.fieldOrganized() },
				new String[] { "left", "left", "left", "left", "left", "left", "center" },
				new boolean[] { true, true, true, true, false, true, true });

		PdfWebTable.setOrder(sessionContext, "itypeDescList.ord", ord, 1);

		for (Iterator i = ItypeDesc.findAll(false).iterator(); i.hasNext();) {
			ItypeDesc itypeDesc = (ItypeDesc) i.next();

			// Add to web table
			WebTableLine line = webTable.addLine(
					sessionContext.hasPermission(itypeDesc, Right.InstructionalTypeEdit)
							? "onclick=\"document.location='itypeDescEdit.action?id=" + itypeDesc.getItype()
									+ "';\""
							: null,
					new String[] { itypeDesc.getItype().toString(), itypeDesc.getAbbv(), itypeDesc.getDesc(),
							(itypeDesc.getSis_ref() == null ? "" : itypeDesc.getSis_ref()), itypeDesc.getBasicType(),
							(itypeDesc.getParent() == null ? "" : itypeDesc.getParent().getDesc()),
							(itypeDesc.isOrganized() ? MSG.yes() : MSG.no()) },
					new Comparable[] { itypeDesc.getItype(), itypeDesc.getAbbv(), itypeDesc.getDesc(),
							(itypeDesc.getSis_ref() == null ? "" : itypeDesc.getSis_ref()), itypeDesc.getBasic(),
							(itypeDesc.getParent() == null ? Integer.valueOf(-1) : itypeDesc.getParent().getItype()),
							(itypeDesc.isOrganized() ? 0 : 1) });
			
			if (itypeDesc.getItype().equals(id))
				line.setBgColor("rgb(168,187,225)");
		}

		if (MSG.actionExportPdf().equals(op)) {
			ExportUtils.exportPDF(webTable, PdfWebTable.getOrder(sessionContext, "itypeDescList.ord"), response,
					"itypes");
			return null;
		}

		String tblData = webTable.printTable(PdfWebTable.getOrder(sessionContext, "itypeDescList.ord"));
		request.setAttribute("itypeDescList", tblData);
		return "success";
	}
}
