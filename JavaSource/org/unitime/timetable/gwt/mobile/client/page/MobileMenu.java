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
package org.unitime.timetable.gwt.mobile.client.page;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeMenu;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.mobile.resources.MobileResourceHolder;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.MenuInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.animation.AnimationHelper;
import com.googlecode.mgwt.ui.client.widget.animation.AnimationEndCallback;
import com.googlecode.mgwt.ui.client.widget.animation.Animations;
import com.googlecode.mgwt.ui.client.widget.button.ImageButton;
import com.googlecode.mgwt.ui.client.widget.button.image.PreviousitemImageButton;
import com.googlecode.mgwt.ui.client.widget.header.HeaderPanel;
import com.googlecode.mgwt.ui.client.widget.header.HeaderTitle;
import com.googlecode.mgwt.ui.client.widget.list.celllist.Cell;
import com.googlecode.mgwt.ui.client.widget.list.celllist.CellList;
import com.googlecode.mgwt.ui.client.widget.list.celllist.CellSelectedEvent;
import com.googlecode.mgwt.ui.client.widget.list.celllist.CellSelectedHandler;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FixedSpacer;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexSpacer;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollPanel;

/**
 * @author Tomas Muller
 */
public class MobileMenu extends UniTimeMenu {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final Template TEMPLATE = GWT.create(Template.class);
	private AnimationHelper iAnimationHelper;
	private ImageButton iButton;
	private MenuWidget iMenu = null;
	private HandlerRegistration iPageLabelRegistration = null;
	
	public interface Template extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<div>{0}</div>")
		SafeHtml content(String cellContents);
	}
	
	public MobileMenu() {
        iAnimationHelper = new AnimationHelper();
        iAnimationHelper.addStyleName("unitime-AnimationHelper");
        
        iButton = new ImageButton(MobileResourceHolder.get().menu());
        iButton.addTapHandler(new TapHandler() {
			@Override
			public void onTap(TapEvent event) {
			    RootPanel.get().add(iAnimationHelper);
				iAnimationHelper.goTo(iMenu, Animations.SLIDE);
			}
		});
	}
	
	private void attach(final RootPanel rootPanel) {
		RPC.execute(new MenuInterface.MenuRpcRequest(), new AsyncCallback<GwtRpcResponseList<MenuInterface>>() {
			@Override
			public void onSuccess(GwtRpcResponseList<MenuInterface> result) {
				iMenu = new MenuWidget(null, result, new TapHandler() {
					@Override
					public void onTap(TapEvent event) {
						iAnimationHelper.goTo(null, Animations.SLIDE_REVERSE, new AnimationEndCallback() {
							@Override
							public void onAnimationEnd() {
								if (iAnimationHelper.isAttached())
									RootPanel.get().remove(iAnimationHelper);
							}
						});
					}
				});
				rootPanel.add(iButton);
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
				if (iPageLabelRegistration != null) {
					iPageLabelRegistration.removeHandler();
					iPageLabelRegistration = null;
				}
				iMenu = new MenuWidget(null, result, new TapHandler() {
					@Override
					public void onTap(TapEvent event) {
						iAnimationHelper.goTo(null, Animations.SLIDE_REVERSE, new AnimationEndCallback() {
							@Override
							public void onAnimationEnd() {
								if (iAnimationHelper.isAttached())
									RootPanel.get().remove(iAnimationHelper);
							}
						});
					}
				});
			}
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}
	
	public void insert(final RootPanel panel) {
		if ("hide".equals(Window.Location.getParameter("menu")))
			panel.setVisible(false);
		else
			attach(panel);
	}
	
	public class MenuWidget extends FlexPanel {
		private HeaderPanel iHeaderPanel;
		private PreviousitemImageButton iHeaderBackButton;
		private HeaderTitle iHeaderTitle;
		private CellList<MenuInterface> iCellList;
		private List<MenuInterface> iMenu;
		private boolean iFirstItemIsParent = false;
		
		public MenuWidget(MenuInterface parent, List<MenuInterface> menus, TapHandler back) {
			super();
			if (MGWT.getFormFactor().isTablet())
				setStyleName("unitime-MobileMenuTablet");
			else
				setStyleName("unitime-MobileMenuPhone");
			
			iHeaderPanel = new HeaderPanel();
	        add(iHeaderPanel);

	        iHeaderBackButton = new PreviousitemImageButton();
	        iHeaderPanel.add(iHeaderBackButton);
	        
	        iHeaderPanel.add(new FlexSpacer());
	        
	        iHeaderTitle = new HeaderTitle(parent == null ? MESSAGES.navigation() : parent.getName());
	        iHeaderPanel.add(iHeaderTitle);
	        iHeaderPanel.add(new FlexSpacer());
	        
	        FixedSpacer fixedSpacer = new FixedSpacer();
	        fixedSpacer.setVisible(!MGWT.getOsDetection().isAndroid());
	        iHeaderPanel.add(fixedSpacer);

	        ScrollPanel scrollPanel = new ScrollPanel();
	        
	        iCellList = new CellList<MenuInterface>(new Cell<MenuInterface>() {

				@Override
				public void render(SafeHtmlBuilder safeHtmlBuilder, MenuInterface item) {
					safeHtmlBuilder.append(TEMPLATE.content(item.getName()));
				}

				@Override
				public boolean canBeSelected(MenuInterface item) {
					return item.hasSubMenus() || item.hasPage();
				}
			});
	        
	        scrollPanel.setWidget(iCellList);
	        scrollPanel.setScrollingEnabledX(false);

	        add(scrollPanel);
	        
	        iMenu = new ArrayList<MenuInterface>();
	        if (parent != null && parent.hasPage()) {
	        	iFirstItemIsParent = true;
	        	iMenu.add(parent);
	        } else {
	        	iFirstItemIsParent = false;
	        }
	        for (MenuInterface menu: menus)
	        	if (!menu.isSeparator()) iMenu.add(menu);
	        iCellList.render(iMenu);
	        
			final MenuInterface.ValueEncoder encoder = new MenuInterface.ValueEncoder() {
				@Override
				public String encode(String value) {
					return URL.encodeQueryString(value);
				}
			};

			iCellList.addCellSelectedHandler(new CellSelectedHandler() {
				@Override
				public void onCellSelected(CellSelectedEvent event) {
					MenuInterface item = iMenu.get(event.getIndex());
					if (event.getIndex() == 0 && iFirstItemIsParent && item.hasPage()) {
						openUrl(item.getName(), item.getURL(encoder), item.getTarget());
					} else if (item.hasSubMenus()) {
						MenuWidget m = new MenuWidget(item, item.getSubMenus(), new TapHandler() {
							@Override
							public void onTap(TapEvent event) {
								iAnimationHelper.goTo(MenuWidget.this, Animations.SLIDE_REVERSE);								
							}
						});
						iAnimationHelper.goTo(m, Animations.SLIDE);
					} else if (item.hasPage()) {
						iAnimationHelper.goTo(null, Animations.SLIDE_REVERSE, new AnimationEndCallback() {
							@Override
							public void onAnimationEnd() {
								if (iAnimationHelper.isAttached())
									RootPanel.get().remove(iAnimationHelper);
							}
						});						
						openUrl(item.getName(), item.getURL(encoder), item.getTarget());
					}
				}
			});
			
			iHeaderBackButton.addTapHandler(back);
		}
	}
	
	protected void openUrl(String name, String url, String target) {
		if ("PAGE_HELP".equals(url)) {
			url = UniTimePageLabel.getInstance().getValue().getHelpUrl();
			name = UniTimePageLabel.getInstance().getValue().getName();
			if (url == null || url.isEmpty()) return;
		}
		if (target == null)
			LoadingWidget.getInstance().show();
		if ("dialog".equals(target)) {
			UniTimeFrameDialog.openDialog(name, url);
		} else if ("eval".equals(target)) {
			ToolBox.eval(url);
		} else if ("download".equals(target)) {
			ToolBox.open(url);
		} else {
			ToolBox.open(GWT.getHostPageBaseURL() + url);
		}
	}

}
