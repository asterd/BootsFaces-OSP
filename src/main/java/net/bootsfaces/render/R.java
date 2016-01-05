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

package net.bootsfaces.render;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * Rendering functions for the core or common to more than one component
 * 
 * @author thecoder4.eu
 */
public final class R {
	/**
	 * Encodes a Column
	 * 
	 * @param rw
	 * @param c
	 * @param span
	 * @param cxs
	 * @param csm
	 * @param clg
	 * @param offset
	 * @param oxs
	 * @param osm
	 * @param olg
	 * @param style
	 * @param sclass
	 * @throws IOException
	 */
	public static final void encodeColumn(ResponseWriter rw, UIComponent c, int span, int cxs, int csm, int clg,
			int offset, int oxs, int osm, int olg, String style, String sclass) throws IOException {

		rw.startElement("div", c);
		Map<String, Object> componentAttrs = new HashMap<String, Object>();

		if (c != null) {
			rw.writeAttribute("id", c.getClientId(), "id");
			Tooltip.generateTooltip(FacesContext.getCurrentInstance(), c.getAttributes(), rw);
			componentAttrs = c.getAttributes();
		}

		StringBuilder sb = new StringBuilder();
		if (span > 0 || offset > 0) {
			if (span > 0) {
				sb.append("col-md-").append(span);
			}
			if (offset > 0) {
				if (span > 0) {
					sb.append(" ");
				}
				sb.append("col-md-offset-" + offset);
			}
		}

		if (cxs > 0) {
			sb.append(" col-xs-").append(cxs);
		}
		if (componentAttrs.get("col-xs") != null && cxs == 0) {
			sb.append(" hidden-xs");
		}

		if (csm > 0) {
			sb.append(" col-sm-").append(csm);
		}
		if (componentAttrs.get("col-sm") != null && csm == 0) {
			sb.append(" hidden-sm");
		}

		if (clg > 0) {
			sb.append(" col-lg-").append(clg);
		}
		if (componentAttrs.get("col-lg") != null && clg == 0) {
			sb.append(" hidden-lg");
		}

		if (oxs > 0) {
			sb.append(" col-xs-offset-").append(oxs);
		}
		if (osm > 0) {
			sb.append(" col-sm-offset-").append(osm);
		}
		if (olg > 0) {
			sb.append(" col-lg-offset-").append(olg);
		}

		if (sclass != null) {
			sb.append(" ").append(sclass);
		}
		rw.writeAttribute("class", sb.toString().trim(), "class");
		if (style != null) {
			rw.writeAttribute("style", style, "style");
		}

		if (null != c) {
			Tooltip.activateTooltips(FacesContext.getCurrentInstance(), c.getAttributes(), c);
		}
	}

	/**
	 * Adds a CSS class to a component within a facet.
	 * 
	 * @param f
	 *            the facet
	 * @param cname
	 *            the class name of the component to be manipulated.
	 * @param aclass
	 *            the CSS class to be added
	 */
	public static void addClass2FacetComponent(UIComponent f, String cname, String aclass) {
		// If the facet contains only one component, getChildCount()=0 and the
		// Facet is the UIComponent
		if (f.getClass().getName().endsWith(cname)) {
			addClass2Component(f, aclass);
		} else {
			if (f.getChildCount() > 0) {
				for (UIComponent c : f.getChildren()) {
					if (c.getClass().getName().endsWith(cname)) {
						addClass2Component(c, aclass);
					}
				}
			}
		}
	}

	/**
	 * Adds a CSS class to a component in the view tree. The class is appended
	 * to the styleClass value.
	 * 
	 * @param c
	 *            the component
	 * @param aclass
	 *            the CSS class to be added
	 */
	protected static void addClass2Component(UIComponent c, String aclass) {
		Map<String, Object> a = c.getAttributes();
		if (a.containsKey("styleClass")) {
			a.put("styleClass", a.get("styleClass") + " " + aclass);
		} else {
			a.put("styleClass", aclass);
		}
	}

	/**
	 * Encodes component attributes (HTML 4 + DHTML) TODO: replace this method
	 * with CoreRenderer.renderPassThruAttributes()
	 * 
	 * @param rw
	 *            ResponseWriter instance
	 * @param attrs
	 * @param alist
	 * @throws IOException
	 */
	public static void encodeHTML4DHTMLAttrs(ResponseWriter rw, Map<String, Object> attrs, String[] alist)
			throws IOException {
		// Encode attributes (HTML 4 + DHTML)

		for (String a : alist) {
			if (attrs.get(a) != null) {
				String val = A.asString(attrs.get(a));
				if (val != null && val.length() > 0) {
					rw.writeAttribute(a, val, a);
				}
			}
		}
	}




	// Suppress default constructor for noninstantiability
	private R() {
		throw new AssertionError();
	}
}
