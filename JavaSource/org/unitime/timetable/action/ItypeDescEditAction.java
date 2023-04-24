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


import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.ItypeDescEditForm;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.dao.ItypeDescDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;

/**
 * @author Tomas Muller
 */
@Action(value="itypeDescEdit", results = {
		@Result(name="edit", type = "tiles", location="itypeDescEdit.tiles"),
		@Result(name="add", type = "tiles", location="itypeDescAdd.tiles"),
		@Result(name="back", type="redirectAction", location = "itypeDescList", params = { "id", "${form.id}" })
	})
@TilesDefinitions(value = {
		@TilesDefinition(name = "itypeDescEdit.tiles", extend = "baseLayout", putAttributes = {
				@TilesPutAttribute(name = "title", value = "Edit Instructional Type"),
				@TilesPutAttribute(name = "body", value = "/admin/itypeDescEdit.jsp")
			}),
		@TilesDefinition(name = "itypeDescAdd.tiles", extend = "baseLayout", putAttributes = {
				@TilesPutAttribute(name = "title", value = "Add Instructional Type"),
				@TilesPutAttribute(name = "body", value = "/admin/itypeDescEdit.jsp")
			})
	})
public class ItypeDescEditAction extends UniTimeAction<ItypeDescEditForm> {
	private static final long serialVersionUID = -3956260021665092931L;
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	protected Integer id;
	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }

	@Override
	public String execute() throws Exception {
		try {
			if (form == null) {
				form = new ItypeDescEditForm();
			}
			// Check Access
			sessionContext.checkPermission(Right.InstructionalTypes);

			// Return
			if (MSG.actionBackITypes().equals(op)) {
				return "back";
			}

			if (MSG.actionAddIType().equals(op)) {
				sessionContext.checkPermission(Right.InstructionalTypeAdd);
			}

			LookupTables.setupItypes(request, true);

			// Add / Update
			if (MSG.actionUpdateIType().equals(op) || MSG.actionSaveIType().equals(op)) {
				// Validate input
				form.validate(this);
				if (hasFieldErrors()) {
					return MSG.actionSaveIType().equals(op) ? "add" : "edit";
				} else {
					Transaction tx = null;

					if (form.getUniqueId() == null || form.getUniqueId() < 0)
						sessionContext.checkPermission(Right.InstructionalTypeAdd);
					else
						sessionContext.checkPermission(form.getUniqueId(), "ItypeDesc", Right.InstructionalTypeEdit);

					try {
						org.hibernate.Session hibSession = (ItypeDescDAO.getInstance()).getSession();
						if (hibSession.getTransaction() == null || !hibSession.getTransaction().isActive())
							tx = hibSession.beginTransaction();

						form.saveOrUpdate(hibSession);

						if (tx != null)
							tx.commit();
					} catch (Exception e) {
						if (tx != null)
							tx.rollback();
						throw e;
					}

					return "back";
				}
			}

			// Edit
			if (op == null) {
				sessionContext.checkPermission(Integer.valueOf(id), "ItypeDesc", Right.InstructionalTypeEdit);
				setOp(MSG.actionUpdateIType());

				if (id == null) {
					throw new Exception(MSG.errorRequiredField(MSG.fieldIType()));
				} else {
					ItypeDesc itype = ItypeDescDAO.getInstance().get(Integer.valueOf(id));

					if (itype == null) {
						return "back";
					} else {
						form.load(itype);
					}
				}
			}

			// Delete
			if (MSG.actionDeleteIType().equals(op)) {
				Transaction tx = null;

				sessionContext.checkPermission(form.getUniqueId(), "ItypeDesc", Right.InstructionalTypeDelete);

				try {
					org.hibernate.Session hibSession = (ItypeDescDAO.getInstance()).getSession();
					if (hibSession.getTransaction() == null || !hibSession.getTransaction().isActive())
						tx = hibSession.beginTransaction();

					form.delete(hibSession);

					tx.commit();
				} catch (Exception e) {
					if (tx != null)
						tx.rollback();
					throw e;
				}

				return "back";
			}
			
			if (MSG.actionAddIType().equals(op)) {
				setOp(MSG.actionSaveIType());
			}

			return (MSG.actionSaveIType().equals(getOp()) ? "add" : "edit");
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}

}
