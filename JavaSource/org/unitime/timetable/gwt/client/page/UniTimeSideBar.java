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
package org.unitime.timetable.gwt.client.page;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.Client.GwtPageChangeEvent;
import org.unitime.timetable.gwt.client.Client.GwtPageChangedHandler;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.MenuInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.PageNameInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * @author Tomas Muller
 */
public class UniTimeSideBar extends UniTimeMenu {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private Timer iScrollTimer = null;

	private SimplePanel iPanel;
	private DisclosurePanel iDisclosurePanel;
	private MyStackPanel iStackPanel;
	private Tree iTree;
	private boolean iUseStackPanel;
	private HandlerRegistration iPageLabelRegistration = null;
	
	private int iTop = 0;
	
	public UniTimeSideBar(boolean useStackPanel, boolean dynamic) {
		iUseStackPanel = useStackPanel;
		
		iPanel = new SimplePanel();
		iPanel.addStyleName("unitime-NoPrint");
		
		final HorizontalPanel header = new HorizontalPanel();
		final Label menuLabel = new Label("Navigation", false);
		menuLabel.setVisible(false);
		menuLabel.setStyleName("unitime-MenuHeaderLabel");
		header.add(menuLabel);
		final Image menuImage = new Image(RESOURCES.menu_closed());
		header.add(menuImage);
		header.setCellHorizontalAlignment(menuImage, HasHorizontalAlignment.ALIGN_RIGHT);
		header.setCellVerticalAlignment(menuImage, HasVerticalAlignment.ALIGN_MIDDLE);
		header.setStyleName("unitime-MenuHeaderClose");
		
		iDisclosurePanel = new DisclosurePanel();
		iDisclosurePanel.setHeader(header);

		menuImage.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				menuImage.setResource(iDisclosurePanel.isOpen() ? RESOURCES.menu_opened_hover() : RESOURCES.menu_closed_hover());
			}
		});
		
		menuImage.addMouseOutHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				menuImage.setResource(iDisclosurePanel.isOpen() ? RESOURCES.menu_opened() : RESOURCES.menu_closed());
			}
		});
		
		iDisclosurePanel.addOpenHandler(new OpenHandler<DisclosurePanel>() {
			@Override
			public void onOpen(OpenEvent<DisclosurePanel> event) {
				menuImage.setResource(iDisclosurePanel.isOpen() ? RESOURCES.menu_opened() : RESOURCES.menu_closed());
				menuLabel.setVisible(iDisclosurePanel.isOpen());
				header.setStyleName("unitime-MenuHeader" + (iDisclosurePanel.isOpen() ? "Open" : "Close"));
				saveState();
			}
		});
		
		iDisclosurePanel.addCloseHandler(new CloseHandler<DisclosurePanel>() {
			@Override
			public void onClose(CloseEvent<DisclosurePanel> event) {
				menuImage.setResource(iDisclosurePanel.isOpen() ? RESOURCES.menu_opened() : RESOURCES.menu_closed());
				menuLabel.setVisible(iDisclosurePanel.isOpen());
				header.setStyleName("unitime-MenuHeader" + (iDisclosurePanel.isOpen() ? "Open" : "Close"));
				saveState();
			}
		});
		
		iStackPanel = new MyStackPanel();
		iTree = new Tree(RESOURCES, true);
		iTree.addOpenHandler(new OpenHandler<TreeItem>() {
			@Override
			public void onOpen(OpenEvent<TreeItem> event) {
				saveState();
			}
		});
		iTree.addCloseHandler(new CloseHandler<TreeItem>() {
			@Override
			public void onClose(CloseEvent<TreeItem> event) {
				saveState();
			}
		});
		
		SimplePanel simple = new SimplePanel();
		if (useStackPanel)
			simple.setWidget(iStackPanel);
		else
			simple.setWidget(iTree);
		
		iDisclosurePanel.add(simple);
		
		iPanel.setWidget(iDisclosurePanel);
		iPanel.getElement().getStyle().setWidth(100, Unit.PCT);
		iDisclosurePanel.getElement().getStyle().setPosition(Position.RELATIVE);
			
		initWidget(iPanel);

		if (dynamic) {
			iScrollTimer = new Timer() {
				@Override
				public void run() {
					iDisclosurePanel.getElement().getStyle().setTop(iTop, Unit.PX);
				}
			};
			
			Window.addWindowScrollHandler(new Window.ScrollHandler() {
				@Override
				public void onWindowScroll(Window.ScrollEvent event) {
					int fromTop = Math.max(Window.getScrollTop() - iPanel.getAbsoluteTop(), 0); // 20 pixels for the top menu
					int fromBottom = Window.getClientHeight() + Window.getScrollTop() - iDisclosurePanel.getOffsetHeight() - 60;
					iDisclosurePanel.getAbsoluteTop();
					if (fromTop <= fromBottom) {
						iTop = fromTop;
					} else {
						if (fromBottom <= iTop && iTop <= fromTop) {
						} else if (iTop > fromTop) {
							iTop = fromTop;
						} else {
							iTop = fromBottom;
						}
					}
					iScrollTimer.schedule(100);
				}
			});
			
			Client.addGwtPageChangedHandler(new GwtPageChangedHandler() {
				@Override
				public void onChange(GwtPageChangeEvent event) {
					int fromTop = Math.max(Window.getScrollTop() - iPanel.getAbsoluteTop(), 0); // 20 pixels for the top menu
					int fromBottom = Window.getClientHeight() + Window.getScrollTop() - iDisclosurePanel.getOffsetHeight() - 60;
					iDisclosurePanel.getAbsoluteTop();
					if (fromTop <= fromBottom) {
						iTop = fromTop;
					} else {
						if (fromBottom <= iTop && iTop <= fromTop) {
						} else if (iTop > fromTop) {
							iTop = fromTop;
						} else {
							iTop = fromBottom;
						}
					}
					iScrollTimer.schedule(100);
				}
			});			
		}
	}
	
	private void attach(final RootPanel rootPanel) {
		RPC.execute(new MenuInterface.MenuRpcRequest(), new AsyncCallback<GwtRpcResponseList<MenuInterface>>() {
			@Override
			public void onSuccess(GwtRpcResponseList<MenuInterface> result) {
				initMenu(result);
				rootPanel.add(UniTimeSideBar.this);
				rootPanel.getElement().getStyle().clearWidth();
				saveState();
			}
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}
	
	@Override
	public void reload() {
		RPC.execute(new MenuInterface.MenuRpcRequest(), new AsyncCallback<GwtRpcResponseList<MenuInterface>>() {
			@Override
			public void onSuccess(GwtRpcResponseList<MenuInterface> result) {
				iStackPanel.setActive(false);
				iTree.clear();
				iStackPanel.clear();
				if (iPageLabelRegistration != null) {
					iPageLabelRegistration.removeHandler();
					iPageLabelRegistration = null;
				}
				initMenu(result);
			}
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}
	
	private void openedNodes(List<String> ret, TreeItem item, String prefix) {
		if (item.getState()) ret.add((prefix == null ? "" : prefix + " ") + item.getText());
		for (int i = 0; i < item.getChildCount(); i++)
			openedNodes(ret, item.getChild(i), (prefix == null ? "" : prefix + " ") + item.getText());
	}
	
	public void saveState() {
		List<String> nodes = new ArrayList<String>();
		if (iUseStackPanel) {
			nodes.add(iStackPanel.getStackText(iStackPanel.getSelectedIndex()));
			for (int i = 0; i < iStackPanel.getWidgetCount(); i++) {
				if (iStackPanel.getWidget(i) instanceof Tree) {
					Tree t = (Tree)iStackPanel.getWidget(i);
					for (int j = 0; j < t.getItemCount(); j++) {
						openedNodes(nodes, t.getItem(j), iStackPanel.getStackText(i));
					}
				}
			}
		} else {
			for (int i = 0; i < iTree.getItemCount(); i++) {
				openedNodes(nodes, iTree.getItem(i), null);
			}
		}
		String sideBarCookie = "";
		if (iDisclosurePanel.isOpen()) sideBarCookie += "Root";
		for (String node: nodes) {
			if (!sideBarCookie.isEmpty()) sideBarCookie += "|";
			sideBarCookie += node;
		}
		sideBarCookie += "|W:" + iPanel.getElement().getClientWidth();
		Cookies.setCookie("UniTime:SideBar", sideBarCookie);
		resizeWideTables();
	}
	
	private void openNodes(Set<String> nodes, TreeItem item, String prefix) {
		if (nodes.contains((prefix == null ? "" : prefix + " ") + item.getText())) item.setState(true);
		for (int i = 0; i < item.getChildCount(); i++)
			openNodes(nodes, item.getChild(i), (prefix == null ? "" : prefix + " ") + item.getText());
	}

	public void restoreState() {
		Set<String> nodes = new HashSet<String>();
		String sideBarCookie = Cookies.getCookie("UniTime:SideBar");
		if (sideBarCookie != null)
			for (String node: sideBarCookie.split("\\|"))
				nodes.add(node);
		iDisclosurePanel.setOpen(nodes.contains("Root") || sideBarCookie == null);
		if (iUseStackPanel)
			for (int i = 0 ; i < iStackPanel.getWidgetCount(); i++) {
				if (nodes.contains(iStackPanel.getStackText(i))) {
					iStackPanel.showStack(i);
				}
				if (iStackPanel.getWidget(i) instanceof Tree) {
					Tree t = (Tree)iStackPanel.getWidget(i);
					for (int j = 0; j < t.getItemCount(); j++) {
						openNodes(nodes, t.getItem(j), iStackPanel.getStackText(i));
					}
				}
			}
		else
			for (int i = 0; i < iTree.getItemCount(); i++) {
				openNodes(nodes, iTree.getItem(i), null);
			}
	}
	
	public void insert(final RootPanel panel) {
		if ("hide".equals(Window.Location.getParameter("menu")))
			panel.setVisible(false);
		else
			attach(panel);
	}
	
	private TreeItem generateItem(final MenuInterface item) {
		final MenuInterface.ValueEncoder encoder = new MenuInterface.ValueEncoder() {
			@Override
			public String encode(String value) {
				return URL.encodeQueryString(value);
			}
		};
		final Label label = new Label(item.getName(), false);
		final TreeItem treeItem = new TreeItem(label);
		if ("PAGE_HELP".equals(item.getPage())) {
			label.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					PageNameInterface name = UniTimePageLabel.getInstance().getValue();
					if (name.hasHelpUrl())
						openUrl(MESSAGES.pageHelp(name.getName()), name.getHelpUrl(), item.getTarget());
				}
			});
			treeItem.setVisible(UniTimePageLabel.getInstance().getValue().hasHelpUrl());
			iPageLabelRegistration = UniTimePageLabel.getInstance().addValueChangeHandler(new ValueChangeHandler<MenuInterface.PageNameInterface>() {
				@Override
				public void onValueChange(ValueChangeEvent<PageNameInterface> event) {
					treeItem.setVisible(event.getValue().hasHelpUrl());
				}
			});
		} else if (item.hasPage()) {
			label.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					openUrl(item.getName(), item.getURL(encoder), item.getTarget());
				}
			});
		}
		if (item.hasSubMenus())
			for (final MenuInterface subItem: item.getSubMenus()) {
				if (subItem.isSeparator()) continue;
				if (subItem.getName().equals(item.getName()) && !item.hasPage() && subItem.hasPage() && !"PAGE_HELP".equals(subItem.getPage())) {
					label.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							openUrl(subItem.getName(), subItem.getURL(encoder), subItem.getTarget());
						}
					});
				} else {
					treeItem.addItem(generateItem(subItem));
				}
			}
		return treeItem;
	}
	
	private void initMenu(List<MenuInterface> items) {
		final MenuInterface.ValueEncoder encoder = new MenuInterface.ValueEncoder() {
			@Override
			public String encode(String value) {
				return URL.encodeQueryString(value);
			}
		};
		for (final MenuInterface item: items) {
			if (item.isSeparator()) continue;
			iTree.addItem(generateItem(item));
			if (item.hasSubMenus()) {
				Tree tree = new Tree(RESOURCES, true);
				for (MenuInterface subItem: item.getSubMenus())
					if (!subItem.isSeparator())
						tree.addItem(generateItem(subItem));
				iStackPanel.add(tree, item.getName());
				tree.addOpenHandler(new OpenHandler<TreeItem>() {
					@Override
					public void onOpen(OpenEvent<TreeItem> event) {
						saveState();
					}
				});
				tree.addCloseHandler(new CloseHandler<TreeItem>() {
					@Override
					public void onClose(CloseEvent<TreeItem> event) {
						saveState();
					}
				});
			} else if ("PAGE_HELP".equals(item.getPage())) {
				iStackPanel.add(new Command() {
					@Override
					public void execute() {
						PageNameInterface name = UniTimePageLabel.getInstance().getValue();
						if (name.hasHelpUrl())
							openUrl(MESSAGES.pageHelp(name.getName()), name.getHelpUrl(), item.getTarget());
					}
				}, item.getName());
			} else {
				iStackPanel.add(new Command() {
					@Override
					public void execute() {
						if (item.hasPage())
							openUrl(item.getName(), item.getURL(encoder), item.getTarget());
					}
				}, item.getName());
			}
		}
		restoreState();
		iStackPanel.setActive(true);
	}
	
	protected void openUrl(String name, String url, String target) {
		if (target == null)
			LoadingWidget.getInstance().show();
		if ("dialog".equals(target)) {
			UniTimeFrameDialog.openDialog(name, url);
		} else if ("download".equals(target)) {
			ToolBox.open(url);
		} else if ("eval".equals(target)) {
			ToolBox.eval(url);
		} else {
			ToolBox.open(GWT.getHostPageBaseURL() + url);
		}
	}
	
	public class MyStackPanel extends StackPanel {
		private Element body = null;
		private boolean iActive = false;
		
		public MyStackPanel() {
			super();
			body = DOM.getFirstChild(getElement());
		}
		
		public String getStackText(int index) {
		    if (index >= getWidgetCount()) {
		        return null;
		      }
		      Element tdWrapper = DOM.getChild((Element) DOM.getChild(body, index * 2), 0);
		      return DOM.getFirstChild(tdWrapper).getInnerText();
		}
		
		public void add(Command cmd, String text) {
			add(new DummyWidget(cmd), text);
		}
		
		public void showStack(int index) {
			if (iActive) {
				if (getWidget(index) instanceof DummyWidget) {
					((DummyWidget)getWidget(index)).getClickCommand().execute();
				} else {
					super.showStack(index);
					saveState();
				}
			} else {
				super.showStack(index);
			}
		}
		
		public void setActive(boolean active) {
			iActive = active;
		}
		
		public class DummyWidget extends SimplePanel {
			private Command iClickCommand = null;
			public DummyWidget(Command cmd) {
				getElement().getStyle().setDisplay(Display.NONE);
				iClickCommand = cmd;
			}
			public Command getClickCommand() {
				return iClickCommand;
			}
		}
	}
	
	public native static int resizeWideTables() /*-{
		if ($wnd.resizeWideTables)
			$wnd.resizeWideTables();
	}-*/;
}
