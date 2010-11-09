/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.client.widgets;

import com.google.gwt.dom.client.AnchorElement; 
import com.google.gwt.dom.client.Document; 
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.user.client.Event; 
import com.google.gwt.user.client.ui.Image; 
import com.google.gwt.user.client.ui.Widget; 

/**
 * @author Tomas Muller
 */
public class ImageLink extends Widget { 
    private Image iImage; 
    private String iUrl; 
    private String iTarget; 
    private SpanElement iElement; 
    private AnchorElement iAnchor;
    
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
    	if (img == null) return;
        iImage = img; 
        iAnchor.appendChild(img.getElement()); 
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
} 