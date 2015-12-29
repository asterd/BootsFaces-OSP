/**
 *  Copyright 2014-15 by Riccardo Massera (TheCoder4.Eu) and Stephan Rauh (http://www.beyondjava.net).
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

package net.bootsfaces.component.navLink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import net.bootsfaces.component.NavBarLinks;
import net.bootsfaces.component.ajax.AJAXRenderer;
import net.bootsfaces.component.icon.IconRenderer;
import net.bootsfaces.render.CoreRenderer;
import net.bootsfaces.render.H;
import net.bootsfaces.render.JSEventHandlerRenderer;
import net.bootsfaces.render.R;
import net.bootsfaces.render.Tooltip;

/** This class generates the HTML code of &lt;b:navLink /&gt;. */
@FacesRenderer(componentFamily = "net.bootsfaces.component", rendererType = "net.bootsfaces.component.navLink.NavLink")
public class NavLinkRenderer extends CoreRenderer {

	@Override
	public void decode(FacesContext context, UIComponent component) {
		if (componentIsDisabledOrReadonly(component)) {
			return;
		}

		String param = component.getClientId(context);
		if (context.getExternalContext().getRequestParameterMap().containsKey(param)) {
			new AJAXRenderer().decode(context, component);
		}
	}

	/**
	 * This methods generates the HTML code of the current b:navLink.
	 * 
	 * @param context
	 *            the FacesContext.
	 * @param component
	 *            the current b:navLink.
	 * @throws IOException
	 *             thrown if something goes wrong when writing the HTML code.
	 */
	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		NavLink navlink = (NavLink) component;
		if (!navlink.isRendered()) {
			return;
		}

		// If there is the header attribute, we only render a Header
		String head = navlink.getHeader();
		if (head != null) {
			encodeHeader(context, head, navlink);
		} else {
			// if there is no href, no outcome and no value attributes
			// we render a divider

			if (navlink.getValue() == null) {
				encodeDivider(context, navlink);
			} else {
				encodeHTML(context, navlink);
			}
		} // if header
		Tooltip.activateTooltips(context, navlink);

	}

	public void encodeHeader(FacesContext context, String h, NavLink navlink) throws IOException {
		ResponseWriter rw = context.getResponseWriter();

		rw.startElement("li", navlink);
		writeAttribute(rw, "id", navlink.getClientId(context), "id");
		String styleClass = navlink.getStyleClass();
		if (null == styleClass)
			writeAttribute(rw, "class", "dropdown-header", "class");
		else
			writeAttribute(rw, "class", "dropdown-header " + styleClass, "class");
		writeAttribute(rw, "style", navlink.getStyle(), "style");
		writeAttribute(rw, "role", "presentation", null);
		rw.writeText(h, null);
		rw.endElement("li");
	}

	public void encodeDivider(FacesContext context, NavLink navlink) throws IOException {
		ResponseWriter rw = context.getResponseWriter();
		rw.startElement("li", navlink);
		Tooltip.generateTooltip(context, navlink, rw);
		String styleClass = navlink.getStyleClass();
		if (null == styleClass)
			styleClass = "";
		else
			styleClass += " ";
		if (navlink.getParent().getClass().equals(NavBarLinks.class)) {
			writeAttribute(rw, "class", styleClass + "divider-vertical", "class");
		} else {
			writeAttribute(rw, "class", styleClass + "divider", "class");
		}
		writeAttribute(rw, "style", navlink.getStyle(), "style");
		writeAttribute(rw, "role", "presentation", null);

		rw.endElement("li");
	}

	public void encodeHTML(FacesContext context, NavLink navlink) throws IOException {
		ResponseWriter rw = context.getResponseWriter();

		String value = (String) navlink.getValue();
		rw.startElement("li", navlink);
		writeAttribute(rw, "id", navlink.getClientId(context), "id");
		Tooltip.generateTooltip(context, navlink, rw);
		AJAXRenderer.generateBootsFacesAJAXAndJavaScript(context, navlink, rw);

		R.encodeHTML4DHTMLAttrs(rw, navlink.getAttributes(), H.ALLBUTTON);

		writeAttribute(rw, "class", getStyleClasses(navlink));
		writeAttribute(rw, "style", navlink.getStyle());

		rw.startElement("a", navlink);
		writeAttribute(rw, "style", navlink.getContentStyle(), "style");
		writeAttribute(rw, "class", navlink.getContentClass(), "class");
		if (navlink.getUpdate() == null && (!navlink.isAjax()) && (navlink.getActionExpression()==null)) {
			String url = encodeHref(context, navlink);
			if (url == null) {
				/*
				 * If we cannot get an outcome we use the Bootstrap Framework to
				 * give a feedback to the developer if this build is in the
				 * Development Stage
				 */
				if (FacesContext.getCurrentInstance().getApplication().getProjectStage()
						.equals(ProjectStage.Development)) {
					writeAttribute(rw, "data-toggle", "tooltip", null);
					writeAttribute(rw, "title", FacesContext.getCurrentInstance().getApplication().getProjectStage()
							+ "WARNING! " + "This link is disabled because a navigation case could not be matched.",
							null);
				}
				url = "#";

			}
			writeAttribute(rw, "href", url, null);
		}
		writeAttribute(rw, "role", "menuitem", null);
		writeAttribute(rw, "tabindex", "-1", null);

		String icon = navlink.getIcon();
		String faicon = navlink.getIconAwesome();
		boolean fa = false; // flag to indicate wether the selected icon set is
							// Font Awesome or not.
		if (faicon != null) {
			icon = faicon;
			fa = true;
		}
		if (icon != null) {
			Object ialign = navlink.getIconAlign(); // Default Left
			if (ialign != null && ialign.equals("right")) {
				rw.writeText(value + " ", null);
				IconRenderer.encodeIcon(rw, navlink, icon, fa);
			} else {
				IconRenderer.encodeIcon(rw, navlink, icon, fa);
				rw.writeText(" " + value, null);
			}

		} else {
			rw.writeText(value, null);
		}
		rw.endElement("a");
		rw.endElement("li");
	}

	private String getStyleClasses(NavLink navlink) {
		String c = "";
		boolean active = navlink.isActive();
		if (active) {
			c += "active";
		}

		String styleClass = navlink.getStyleClass();
		if (null != styleClass)
			c += " " + styleClass;

		return c;
	}

	private String encodeHref(FacesContext context, NavLink navlink) {
		String href = navlink.getHref();

		String url;

		if (href != null) {
			url = getResourceURL(context, href);
			return url;
		} else {
			String outcome = navlink.getOutcome();
			outcome = (outcome == null) ? context.getViewRoot().getViewId() : outcome;

			ConfigurableNavigationHandler cnh = (ConfigurableNavigationHandler) context.getApplication()
					.getNavigationHandler();
			NavigationCase navCase = cnh.getNavigationCase(context, null, outcome);
			if (navCase == null) {
				return null;
			}
			String vId = navCase.getToViewId(context);

			Map<String, List<String>> params = getParams(navCase, navlink);

			url = context.getApplication().getViewHandler().getBookmarkableURL(context, vId, params,
					navlink.isIncludeViewParams() || navCase.isIncludeViewParams());

			if (url != null) {
				String frag = navlink.getFragment();
				if (frag != null) {
					url += "#" + frag;
				}
				return url;
			} else {
				return "#";
			}
		}

	}

	protected String getResourceURL(FacesContext fc, String value) {
		return fc.getExternalContext().encodeResourceURL(value);
	}

	/**
	 * Find all parameters to include by looking at nested uiparams and params
	 * of navigation case
	 */
	protected Map<String, List<String>> getParams(NavigationCase navCase, NavLink button) {
		Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();

		// UIParams
		for (UIComponent child : button.getChildren()) {
			if (child.isRendered() && (child instanceof UIParameter)) {
				UIParameter uiParam = (UIParameter) child;

				if (!uiParam.isDisable()) {
					List<String> paramValues = params.get(uiParam.getName());
					if (paramValues == null) {
						paramValues = new ArrayList<String>();
						params.put(uiParam.getName(), paramValues);
					}

					paramValues.add(String.valueOf(uiParam.getValue()));
				}
			}
		}

		// NavCase Params
		Map<String, List<String>> navCaseParams = navCase.getParameters();
		if (navCaseParams != null && !navCaseParams.isEmpty()) {
			for (Map.Entry<String, List<String>> entry : navCaseParams.entrySet()) {
				String key = entry.getKey();

				// UIParams take precedence
				if (!params.containsKey(key)) {
					params.put(key, entry.getValue());
				}
			}
		}

		return params;
	}
}
