/**
 *  Copyright 2014 Riccardo Massera (TheCoder4.Eu)
 *  
 *  This file is part of BootsFaces.
 *  
 *  BootsFaces is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  BootsFaces is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with BootsFaces. If not, see <http://www.gnu.org/licenses/>.
 */

package net.bootsfaces.component;

import java.io.IOException;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import net.bootsfaces.C;
import net.bootsfaces.render.Tooltip;

/**
 *
 * @author thecoder4eu
 */
@ResourceDependencies({
        @ResourceDependency(library="bsf", name="css/core.css", target="head"),
        @ResourceDependency(library="bsf", name="css/thumbnails.css", target="head"),
        @ResourceDependency(library = "bsf", name = "css/tooltip.css", target = "head")
})
@FacesComponent(C.THUMBNAIL_COMPONENT_TYPE)
public class Thumbnail extends UIComponentBase {
    
    /**
     * <p>The standard component type for this component.</p>
     */
    public static final String COMPONENT_TYPE =C.THUMBNAIL_COMPONENT_TYPE;
    /**
     * <p>The component family for this component.</p>
     */
    public static final String COMPONENT_FAMILY = C.BSFCOMPONENT;
    
    public Thumbnail() {
        setRendererType(null); // this component renders itself
        Tooltip.addResourceFile();
    }

    @Override
    public void encodeBegin(FacesContext fc) throws IOException {
        UIComponent c = this;
        if (!c.isRendered()) {
            return;
        }
        ResponseWriter rw = fc.getResponseWriter();
        rw.startElement("div", c);
        rw.writeAttribute("id", c.getClientId(fc), "id");
        Tooltip.generateTooltip(fc, c.getAttributes(), rw);
        rw.writeAttribute("class", "thumbnail", "class");
    }
    
    @Override
    public void encodeEnd(FacesContext fc) throws IOException {
    	UIComponent c = this;
        if (!c.isRendered()) {
            return;
        }
        
        ResponseWriter rw = fc.getResponseWriter();
        UIComponent capt;
        capt = c.getFacet("caption");
        if (capt != null ) {
            rw.startElement("div", c);
            rw.writeAttribute("class", "caption", "class");
            capt.encodeAll(fc);
            rw.endElement("div");
        }
        rw.endElement("div");
        Tooltip.activateTooltips(fc, c.getAttributes(), c);
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }
    
}
