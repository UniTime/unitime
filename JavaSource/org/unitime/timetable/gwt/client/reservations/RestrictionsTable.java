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
package org.unitime.timetable.gwt.client.reservations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.ReservationInterface.Clazz;
import org.unitime.timetable.gwt.shared.ReservationInterface.Config;
import org.unitime.timetable.gwt.shared.ReservationInterface.IdName;
import org.unitime.timetable.gwt.shared.ReservationInterface.Offering;
import org.unitime.timetable.gwt.shared.ReservationInterface.Subpart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class RestrictionsTable extends UniTimeTable<RestrictionsTable.Node> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private UniTimeWidget<UniTimeTextBox> iLimit;
	private Offering iOffering = null;
	private Map<Long, Node> iConfigs = new HashMap<Long, Node>();
	private Map<Long, Node> iClasses = new HashMap<Long, Node>();
	
	public RestrictionsTable(UniTimeWidget<UniTimeTextBox> limit) {
		iLimit = limit;
		iLimit.getWidget().addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				computeLimit();
				
			}
		});
		List<Widget> header = new ArrayList<Widget>();
		header.add(new UniTimeTableHeader(MESSAGES.colConfigOrClass()));
		header.add(new UniTimeTableHeader(MESSAGES.colExternalId()));
		header.add(new UniTimeTableHeader(MESSAGES.colLimit()));
		header.add(new UniTimeTableHeader(MESSAGES.colTime()));
		header.add(new UniTimeTableHeader(MESSAGES.colDate()));
		header.add(new UniTimeTableHeader(MESSAGES.colRoom()));
		header.add(new UniTimeTableHeader(MESSAGES.colInstructor()));
		addRow(null, header);
		setAllowSelection(true);
		addMouseClickListener(new UniTimeTable.MouseClickListener<Node>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<Node> event) {
				selectRow(event.getRow(), isSelected(event.getRow()));
			}
		});
		setVisible(false);
		setStyleName("unitime-RestrictionsTable");
	}
	
	@Override
	public void clear() {
		clearTable(1);
		iConfigs.clear();
		iClasses.clear();
		iOffering = null;
		setVisible(false);
	}
	
	public void setOffering(Offering offering) {
		clear();
		iOffering = offering;
		for (Config config: offering.getConfigs())
			addConfig(config);
		computeLimit();
		
		setVisible(true);
		boolean hasInstructor = false, hasAssignment = false, hasExternalId = false;
		for (Config config: offering.getConfigs()) {
			for (Subpart subpart: config.getSubparts())
				for (Clazz clazz: subpart.getClasses()) {
					if (clazz.hasInstructor()) hasInstructor = true;
					if (clazz.hasTime() || clazz.hasRoom() || clazz.hasDate() || clazz.isCancelled())
						hasAssignment = true;
					if (clazz.hasExternalId()) hasExternalId = true;
				}
			if (config.hasInstructionalMethod()) hasExternalId = true;
		}
		
		setColumnVisible(1, hasExternalId);
		setColumnVisible(3, hasAssignment);
		setColumnVisible(4, hasAssignment);
		setColumnVisible(5, hasAssignment);
		setColumnVisible(6, hasInstructor);
	}
	
	private void addConfig(Config config) {
		List<Widget> line = new ArrayList<Widget>();
		Node node = new Node(null, MESSAGES.labelConfiguration(config.getAbbv()), config);
		line.add(node);
		line.add(new Label(config.hasInstructionalMethod() ? config.getInstructionalMethod() : ""));
		line.add(new UniTimeTable.NumberCell(config.getLimit() == null ? MESSAGES.configUnlimited() : config.getLimit().toString()));
		line.add(new Label(""));
		line.add(new Label(""));
		line.add(new Label(""));
		line.add(new Label(""));
		iConfigs.put(config.getId(), node);
		node.setRow(addRow(node, line));
		for (Subpart subpart: config.getSubparts()) {
			if (subpart.getParentId() == null)
				addClasses(node, subpart, null);
		}
	}
	
	protected void addClasses(Node node, Subpart subpart, Long parent) {
		for (Clazz clazz: subpart.getClasses()) {
			if (parent != null && !parent.equals(clazz.getParentId())) continue;
			Node classNode = addClass(node, clazz);
			for (Subpart child: subpart.getConfig().getSubparts()) {
				if (subpart.getId().equals(child.getParentId()))
					addClasses(classNode, child, clazz.getId());
			}
		}		
	}
	
	protected Node addClass(Node parent, Clazz clazz) {
		List<Widget> line = new ArrayList<Widget>();
		Node node = new Node(parent, clazz.getAbbv(), clazz);
		line.add(node);
		line.add(new Label(clazz.hasExternalId() ? clazz.getExternalId() : ""));
		line.add(new UniTimeTable.NumberCell(clazz.getLimit() == null ? "" : clazz.getLimit().toString()));
		if (clazz.isCancelled()) {
			line.add(new CancelledCell());
		} else {
			line.add(new Label(clazz.hasTime() ? clazz.getTime() : ""));
			line.add(new Label(clazz.hasDate() ? clazz.getDate() : ""));
			line.add(new Label(clazz.hasRoom() ? clazz.getRoom() : ""));
		}
		line.add(new Label(clazz.hasInstructor() ? clazz.getInstructor() : ""));
		node.setRow(addRow(node, line));
		if (clazz.isCancelled())
			for (Widget w: line)
				w.addStyleName("cancelled");
		iClasses.put(clazz.getId(), node);
		parent.addChildNode(node);
		return node;
	}
	
	public void selectRow(int row, boolean value) {
		Node node = getData(row);
		if (node != null && node.isEnabled())
			node.setValue(value);
		else if (value)
			selectRow(row, false);
	}

	public class Node extends P implements HasValue<Boolean>, HasEnabled {
		private Image iImage;
		private IdName iItem;
		private CheckBox iCheck;
		private Node iParent = null;
		private List<Node> iChildren = new ArrayList<Node>();
		private boolean iOpened = false;
		private int iRow = -1;
		
		public Node(Node parent, String name, IdName item) {
			super("unitime-RestrictionsNode");
			iImage = new Image();
			iImage.addStyleName("tree-icon");
			iItem = item;
			iParent = parent;
			iCheck = new CheckBox(name);
			iCheck.addStyleName("check-box");
			update();
			iImage.getElement().getStyle().setPaddingLeft(16 * getIndent(), Unit.PX);
			iCheck.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
			iCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if (iRow >= 0) setSelected(iRow, event.getValue());
					propagate();
				}
			});
			add(iImage);
			add(iCheck);
			iCheck.addKeyDownHandler(new KeyDownHandler() {
				@Override
				public void onKeyDown(KeyDownEvent event) {
					switch (event.getNativeKeyCode()) {
					case KeyCodes.KEY_RIGHT:
						if (!getChildrenNodes().isEmpty()) {
							if (!isOpened()) {
								setOpened(true);
							} else {
								for (Node n: getChildrenNodes()) {
									if (n.isEnabled() && n.isNodeVisible()) {
										n.getCheckBox().setFocus(true);
										break;
									}
								}
							}
						}
						event.preventDefault();
						break;
					case KeyCodes.KEY_LEFT:
						if (!getChildrenNodes().isEmpty()) {
							if (isOpened())
								setOpened(false);
							else if (hasParentNode())
								getParentNode().getCheckBox().setFocus(true);
						} else if (hasParentNode())
							getParentNode().getCheckBox().setFocus(true);
						event.preventDefault();
						break;
					case KeyCodes.KEY_UP:
						for (int r = iRow - 1; r > 0; r--) {
							Node n = getData(r);
							if (n.isEnabled() && n.isNodeVisible()) {
								n.getCheckBox().setFocus(true);
								break;
							}
						}
						event.preventDefault();
						break;
					case KeyCodes.KEY_DOWN:
						for (int r = iRow + 1; r < getRowCount(); r++) {
							Node n = getData(r);
							if (n.isEnabled() && n.isNodeVisible()) {
								n.getCheckBox().setFocus(true);
								break;
							}
						}
						event.preventDefault();
						break;
					}
				}
			}); 
		}
		
		public Clazz getClazz() { return (Clazz)iItem; }
		public Config getConfig() { return (Config)iItem; }
		public boolean isConfig() { return iParent == null; }
		public boolean isClazz() { return iParent != null; }
		
		public Node getParentNode() { return iParent; }
		public boolean hasParentNode() { return iParent != null; }
		public List<Node> getChildrenNodes() { return iChildren; }
		
		public CheckBox getCheckBox() { return iCheck; }
		
		public void addChildNode(Node node) {
			iChildren.add(node);
			update();
			if (iChildren.size() == 1) {
				iImage.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						setOpened(!isOpened());
					}
				});
			}
		}
		
		public void setRow(int row) {
			iRow = row;
		}
		
		public int getRow() {
			return iRow;
		}
		
		protected int getIndent() {
			int ret = 0;
			Node p = iParent;
			while (p != null) { ret++; p = p.iParent; }
			return ret;
		}
		
		public boolean isNodeVisible() {
			Node p = iParent;
			while (p != null) {
				if (!p.isOpened()) return false;
				p = p.iParent;
			}
			return true;
		}
		
		protected void update() {
			if (iChildren.isEmpty())
				iImage.setResource(RESOURCES.treeLeaf());
			else if (iOpened)
				iImage.setResource(RESOURCES.treeOpen());
			else
				iImage.setResource(RESOURCES.treeClosed());
			if (iRow >= 0)
				getRowFormatter().setVisible(iRow, isNodeVisible());
			for (Node child: iChildren)
				child.update();
		}

		@Override
		public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
			return addHandler(handler, ValueChangeEvent.getType());
		}
		
		public boolean isOpened() { return iOpened; }
		
		public void setOpened(boolean opened) {
			iOpened = opened;
			update();
		}

		@Override
		public Boolean getValue() { return iCheck.getValue(); }

		@Override
		public void setValue(Boolean value) { setValue(value, true); }

		@Override
		public void setValue(Boolean value, boolean fireEvents) {
			iCheck.setValue(value);
			if (iRow >= 0)
				setSelected(iRow, value && isEnabled());
			if (fireEvents) {
				propagate();
				ValueChangeEvent.fire(this, value);
			}
		}

		@Override
		public boolean isEnabled() {
			return iCheck.isEnabled();
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			iCheck.setEnabled(enabled);
			if (iRow >= 0)
				setSelected(iRow, getValue() && isEnabled());
		}
		
		private void propagate() {
			propagateDown();
			propagateUp();
			computeLimit();
		}
		
		private void propagateDown() {
			for (Node child: getChildrenNodes()) {
				child.setEnabled(!getValue());
				child.setValue(getValue(), false);
				child.propagateDown();
			}
		}
		
		private void propagateUp() {
			if (hasParentNode()) {
				if (!getValue()) {
					for (Node c: getParentNode().getChildrenNodes()) {
						if (c.getValue()) return;
					}
				}
				getParentNode().setEnabled(!getValue());
				getParentNode().setValue(getValue(), false);
				getParentNode().propagateUp();
			}
		}
	}
	
	public void populate(ReservationInterface reservation) {
		for (Config config: reservation.getConfigs()) {
			Node node = iConfigs.get(config.getId());
			if (node != null)
				node.setValue(true);
		}
		for (Clazz clazz: reservation.getClasses()) {
			Node node = iClasses.get(clazz.getId());
			if (node != null) {
				node.setValue(true);
				Node p = node.getParentNode();
				while (p != null) {
					p.setOpened(true); p = p.getParentNode();
				}
			}
		}
	}
	
	public void validate(ReservationInterface reservation) {
		for (Node config: iConfigs.values()) {
			if (config.getValue() && config.isEnabled()) {
				Config c = new Config();
				c.setId(config.getConfig().getId());
				c.setName(config.getConfig().getName());
				reservation.getConfigs().add(c);
			}
		}
		for (Node clazz: iClasses.values()) {
			if (clazz.getValue() && clazz.isEnabled()) {
				Clazz c = new Clazz();
				c.setId(clazz.getClazz().getId());
				c.setName(clazz.getClazz().getName());
				reservation.getClasses().add(c);
			}
		}
	}
	
	public void computeLimit() {
		if (iOffering == null) { 
			iLimit.clearHint();
		}
		// if (iLimit.isReadOnly()) return;
		int total = 0, limit = -1;
		boolean totalUnlimited = false, unlimited = false;
		for (Config config: iOffering.getConfigs()) {
			for (Subpart subpart: config.getSubparts()) {
				int lim = 0; boolean selected = false;
				for (Clazz clazz: subpart.getClasses()) {
					Node node = iClasses.get(clazz.getId());
					if (node.getValue()) {
						lim += clazz.getLimit();
						selected = true;
					}
				}
				if (selected && (limit < 0 || limit > lim)) { limit = lim; }
			}
		}
		int lim = 0; boolean selected = false;
		for (Config config: iOffering.getConfigs()) {
			if (config.getLimit() == null)
				totalUnlimited = true;
			else
				total += config.getLimit();
			Node cfg = iConfigs.get(config.getId());
			if (cfg != null && cfg.getValue()) {
				selected = true;
				if (cfg.getConfig().getLimit() == null)
					unlimited = true;
				else
					lim += cfg.getConfig().getLimit();
			}
		}
		if (selected && (limit < 0 || limit > lim)) { limit = lim; }
		int entered = Integer.MAX_VALUE;
		try {
			entered = Integer.parseInt(iLimit.getWidget().getValue());
		} catch (NumberFormatException e) {}
		if (limit >= 0 || unlimited) {
			if (unlimited || limit >= entered)
				iLimit.clearHint();
			else
				iLimit.setHint(limit == 0 ? MESSAGES.hintNoSpaceSelected() : limit == 1 ? MESSAGES.hintOnlyOneSpaceSelected() : MESSAGES.hintOnlyNSpacesSelected(limit));
		} else {
			if (!iOffering.isOffered())
				iLimit.setHint(MESSAGES.hintCourseNotOffered(iOffering.getAbbv()));
			else if (totalUnlimited || total >= entered || entered == Integer.MAX_VALUE)
				iLimit.clearHint();
			else
				iLimit.setHint(total == 0 ? MESSAGES.hintNoSpaceInCourse(iOffering.getAbbv()) : total == 1 ? MESSAGES.hintOnlyOneSpaceInCourse(iOffering.getAbbv()) : MESSAGES.hintOnlyNSpacesInCourse(total, iOffering.getAbbv()));
		}
	}
	
	private class CancelledCell extends Label implements UniTimeTable.HasColSpan, UniTimeTable.HasCellAlignment {
		
		public CancelledCell() {
			super(MESSAGES.reservationCancelledClass());
		}

		@Override
		public int getColSpan() {
			return 3;
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_CENTER;
		}
	}
}
