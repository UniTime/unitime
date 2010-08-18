/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.services.MenuServiceAsync;
import org.unitime.timetable.gwt.shared.MenuInterface;
import org.unitime.timetable.gwt.shared.ToolBox;
import org.unitime.timetable.gwt.widgets.LoadingWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.FrameElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class UniTimeSideBar extends Composite {
	protected final MenuServiceAsync iService = GWT.create(MenuService.class);

	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	
	private Timer iTimer = null;
	private Timer iScrollTimer = null;

	private SimplePanel iPanel;
	private DisclosurePanel iDisclosurePanel;
	private MyStackPanel iStackPanel;
	private Tree iTree;
	
	private int iTop = 0;
	
	public UniTimeSideBar(boolean useStackPanel) {
		
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
		iPanel.setHeight("100%");
		DOM.setStyleAttribute(iDisclosurePanel.getElement(), "position", "relative");
			
		initWidget(iPanel);

		iService.getMenu(new AsyncCallback<List<MenuInterface>>() {
			@Override
			public void onSuccess(List<MenuInterface> result) {
				initMenu(result);
			}
			@Override
			public void onFailure(Throwable caught) {
			}
		});
		
		iScrollTimer = new Timer() {
			@Override
			public void run() {
				DOM.setStyleAttribute(iDisclosurePanel.getElement(), "top", String.valueOf(iTop));
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
	}
	
	public boolean isOpenned(String name) {
		String sideBarCookie = Cookies.getCookie("UniTime:SideBar");
		return sideBarCookie != null && sideBarCookie.indexOf("|"+name+"|") >= 0;
	}
	
	private void openedNodes(List<String> ret, TreeItem item) {
		if (item.getState()) ret.add(item.getText());
		for (int i = 0; i < item.getChildCount(); i++)
			openedNodes(ret, item.getChild(i));
	}
	
	public void saveState() {
		List<String> nodes = new ArrayList<String>();
		if (iStackPanel.isAttached()) {
			nodes.add(iStackPanel.getStackText(iStackPanel.getSelectedIndex()));
			for (int i = 0; i < iStackPanel.getWidgetCount(); i++) {
				if (iStackPanel.getWidget(i) instanceof Tree) {
					Tree t = (Tree)iStackPanel.getWidget(i);
					for (int j = 0; j < t.getItemCount(); j++) {
						openedNodes(nodes, t.getItem(j));
					}
				}
			}
		} else {
			for (int i = 0; i < iTree.getItemCount(); i++) {
				openedNodes(nodes, iTree.getItem(i));
			}
		}
		String sideBarCookie = "";
		if (iDisclosurePanel.isOpen()) sideBarCookie += "Root";
		for (String node: nodes) {
			if (!sideBarCookie.isEmpty()) sideBarCookie += "|";
			sideBarCookie += node;
		}
		Cookies.setCookie("UniTime:SideBar", sideBarCookie);
	}
	
	private void openNodes(Set<String> nodes, TreeItem item) {
		if (nodes.contains(item.getText())) item.setState(true);
		for (int i = 0; i < item.getChildCount(); i++)
			openNodes(nodes, item.getChild(i));
	}

	public void restoreState() {
		Set<String> nodes = new HashSet<String>();
		String sideBarCookie = Cookies.getCookie("UniTime:SideBar");
		if (sideBarCookie != null)
			for (String node: sideBarCookie.split("\\|"))
				nodes.add(node);
		iDisclosurePanel.setOpen(nodes.contains("Root"));
		if (iStackPanel.isAttached())
			for (int i = 0 ; i < iStackPanel.getWidgetCount(); i++) {
				if (nodes.contains(iStackPanel.getStackText(i))) {
					iStackPanel.showStack(i);
				}
				if (iStackPanel.getWidget(i) instanceof Tree) {
					Tree t = (Tree)iStackPanel.getWidget(i);
					for (int j = 0; j < t.getItemCount(); j++) {
						openNodes(nodes, t.getItem(j));
					}
				}
			}
		else
			for (int i = 0; i < iTree.getItemCount(); i++) {
				openNodes(nodes, iTree.getItem(i));
			}
	}
	
	public void insert(final RootPanel panel) {
		panel.add(this);
		panel.setVisible(true);
	}
	
	private TreeItem generateItem(final MenuInterface item) {
		Label label = new Label(item.getName(), false);
		TreeItem treeItem = new TreeItem(label);
		if (item.getPage() != null) {
			label.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (item.isGWT()) 
						openUrl(item.getName(), "gwt.jsp?page=" + item.getPage(), item.getTarget());
					else {
						openUrl(item.getName(), item.getPage(), item.getTarget());
					}
				}
			});
		}
		if (item.hasSubMenus())
			for (MenuInterface subItem: item.getSubMenus())
				if (!subItem.isSeparator())
					treeItem.addItem(generateItem(subItem));
		return treeItem;
	}
	
	private void initMenu(List<MenuInterface> items) {
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
			} else {
				iStackPanel.add(new Command() {
					@Override
					public void execute() {
						if (item.isGWT()) 
							openUrl(item.getName(), "gwt.jsp?page=" + item.getPage(), item.getTarget());
						else {
							openUrl(item.getName(), item.getPage(), item.getTarget());
						}
					}
				}, item.getName());
			}
		}
		restoreState();
		iStackPanel.setActive(true);
	}
	
	protected void openUrl(final String name, final String url, String target) {
		if (target == null)
			LoadingWidget.getInstance().show();
		if ("dialog".equals(target)) {
			final DialogBox dialog = new MyDialogBox();
			dialog.setAutoHideEnabled(true);
			dialog.setModal(true);
			final Frame frame = new MyFrame(name);
			frame.getElement().getStyle().setBorderWidth(0, Unit.PX);
			dialog.setGlassEnabled(true);
			dialog.setAnimationEnabled(true);
			dialog.setWidget(frame);
			dialog.setText(name);
			frame.setUrl(url);
			frame.setSize(String.valueOf(Window.getClientWidth() * 3 / 4), String.valueOf(Window.getClientHeight() * 3 / 4));
			
			iTimer = new Timer() {
				@Override
				public void run() {
					if (LoadingWidget.getInstance().isShowing())
						LoadingWidget.getInstance().fail(name + " does not seem to load, " +
								"please check <a href='" + url + "' style='white-space: nowrap;'>" + url + "</a> for yourself.");
				}
			};

			dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
				@Override
				public void onClose(CloseEvent<PopupPanel> event) {
					if (LoadingWidget.getInstance().isShowing())
						LoadingWidget.getInstance().hide();
				}
			});

			dialog.center();
			iTimer.schedule(30000);
		} else {
			ToolBox.open(GWT.getHostPageBaseURL() + url);
		}
	}

	protected class MyDialogBox extends DialogBox {
		private MyDialogBox() { super(); }
		protected void onPreviewNativeEvent(NativePreviewEvent event) {
			super.onPreviewNativeEvent(event);
			if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ESCAPE)
				MyDialogBox.this.hide();
		}
	}
	
	public static void notifyFrameLoaded() {
		LoadingWidget.getInstance().hide();
	}
	
	public class MyFrame extends Frame {
		private String iName;
		
		public MyFrame(String name) {
			super();
			iName = name;
			hookFremaLoaded((FrameElement)getElement().cast());
		}
		
		public void onLoad() {
			super.onLoad();
			LoadingWidget.getInstance().show("Loading " + iName + " ...");
		}
	}
	
	public native void hookFremaLoaded(FrameElement element) /*-{
		element.onload = function() {
			@org.unitime.timetable.gwt.client.UniTimeSideBar::notifyFrameLoaded()();
		}
	}-*/;
	
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
		      Element tdWrapper = DOM.getChild(DOM.getChild(body, index * 2), 0);
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
}
