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
package org.unitime.timetable.gwt.client.widgets;

import org.unitime.timetable.gwt.client.aria.HasAriaLabel;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.dom.client.AnchorElement; 
import com.google.gwt.dom.client.Document; 
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.user.client.Event; 
import com.google.gwt.user.client.ui.Image; 
import com.google.gwt.user.client.ui.Widget; 

/**
 * @author Tomas Muller
 */
public class ImageLink extends Widget implements HasAriaLabel { 
    private Image iImage;
    private String iUrl; 
    private String iTarget; 
    private SpanElement iElement; 
    private AnchorElement iAnchor;
    private Element iImageElement = null;
    private SpanElement iTextElement = null;
    
    public ImageLink(Image img, String url){ 
        initElements(); 
        setImage(img); 
        setUrl(url); 
    }
    
    public ImageLink(){ 
        this(null, ""); 
    }
    
    private void initElements() { 
        iElement = Document.get().createSpanElement(); 
        iAnchor = Document.get().createAnchorElement(); 
        iElement.appendChild(iAnchor); 
        setElement(iElement); 
        sinkEvents(Event.MOUSEEVENTS); 
        setTarget("_blank"); 
    } 

    public void onBrowserEvent(Event event) { 
        if(event.getTypeInt() == Event.ONMOUSEOVER){ 
            iAnchor.getStyle().setProperty("cursor", "hand"); 
        } 
        super.onBrowserEvent(event); 
    } 
    
    public Image getImage() { 
        return iImage; 
    } 

    public void setImage(Image img) {
    	if (iImageElement != null)
    		iAnchor.removeChild(iImageElement);
    	if (img == null) return;
        iImage = img; 
        iImageElement = img.getElement();
        iImageElement.getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
        iAnchor.insertFirst(iImageElement); 
    } 

    public String getUrl() { 
        return iUrl; 
    } 

    public void setUrl(String url) { 
        iUrl = url; 
        iAnchor.setHref(url); 
    } 

    public String getTarget() { 
        return iTarget; 
    } 

    public void setTarget(String target) { 
        iTarget = target; 
        iAnchor.setTarget(target); 
    } 
    
    public void setText(String text) {
    	if (iTextElement == null) {
    		iTextElement = Document.get().createSpanElement();
    		iAnchor.appendChild(iTextElement);
    	}
    	iTextElement.setInnerText(text);
    }
    
    public String getText() {
    	return (iTextElement == null ? null : iTextElement.getInnerText());
    }

	@Override
	public String getAriaLabel() {
		return Roles.getLinkRole().getAriaLabelProperty(iAnchor);
	}

	@Override
	public void setAriaLabel(String text) {
		if (text == null || text.isEmpty())
			Roles.getLinkRole().removeAriaLabelledbyProperty(iElement);
		else
			Roles.getLinkRole().setAriaLabelProperty(iElement, text);
	}
} 