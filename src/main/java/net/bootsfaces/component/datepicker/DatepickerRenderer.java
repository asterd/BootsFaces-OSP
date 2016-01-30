/**
 *  Copyright 2014-16 by Riccardo Massera (TheCoder4.Eu) and Stephan Rauh (http://www.beyondjava.net).
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

package net.bootsfaces.component.datepicker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import net.bootsfaces.component.icon.IconRenderer;
import net.bootsfaces.render.A;
import net.bootsfaces.render.CoreRenderer;
import net.bootsfaces.render.JQ;
import net.bootsfaces.render.Tooltip;

/** This class generates the HTML code of &lt;b:datepicker /&gt;. */
@FacesRenderer(componentFamily = "net.bootsfaces.component", rendererType = "net.bootsfaces.component.datepicker.Datepicker")
public class DatepickerRenderer extends CoreRenderer {
	private String mode;

	@Override
	public void decode(FacesContext fc, UIComponent component) {
		Datepicker datepicker = (Datepicker) component;
		String subVal = fc.getExternalContext().getRequestParameterMap().get(component.getClientId(fc));

		if (subVal != null) {
			datepicker.setSubmittedValue(subVal);
			datepicker.setValid(true);
		}
	}

	/**
	 * This methods generates the HTML code of the current b:datepicker.
	 * 
	 * @param context
	 *            the FacesContext.
	 * @param component
	 *            the current b:datepicker.
	 * @throws IOException
	 *             thrown if something goes wrong when writing the HTML code.
	 */
	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		if (!component.isRendered()) {
			return;
		}
		Datepicker datepicker = (Datepicker) component;
		encodeHTML(context, datepicker);
		encodeDefaultLanguageJS(context);
		Tooltip.activateTooltips(context, datepicker);
	}

	/**
	 * Generates the default language for the date picker. Originally
	 * implemented in the HeadRenderer, this code has been moved here to provide
	 * better compatibility to PrimeFaces. If multiple date pickers are on the
	 * page, the script is generated redundantly, but this shouldn't do no harm.
	 * 
	 * @param fc
	 *            The current FacesContext
	 * @throws IOException
	 */
	private void encodeDefaultLanguageJS(FacesContext fc) throws IOException {
		ResponseWriter rw = fc.getResponseWriter();
		rw.startElement("script", null);
		rw.write("$.datepicker.setDefaults($.datepicker.regional['" + fc.getViewRoot().getLocale().getLanguage()
				+ "']);");
		rw.endElement("script");
	}

	/**
	 * Encodes the HTML for this context
	 * 
	 * @param fc
	 * @throws IOException
	 */
	private void encodeHTML(FacesContext fc, Datepicker datepicker) throws IOException {
		Map<String, Object> attrs = datepicker.getAttributes();
		String clientId = datepicker.getClientId(fc);
		ResponseWriter rw = fc.getResponseWriter();
		// stz = selectTimeZone(attrs.get(A.TZ));

		datepicker.setSloc(datepicker.selectLocale(fc.getViewRoot().getLocale(), A.asString(attrs.get(JQ.LOCALE))));
		datepicker.setSdf(Datepicker.selectDateFormat(datepicker.getSloc(), A.asString(attrs.get(JQ.DTFORMAT))));

		// Debugging Locale and dateformat
		// rw.write("<span>DEBUG sloc='"+sloc+"', sdf='"+sdf+"' </span>");

		String dpId;

		Object v = datepicker.getSubmittedValue();
		if (v == null) {
			v = datepicker.getValue();
		}

		/*
		 * 6 modes: 1) inline 2) popup (no icons) 3) popup-icon 4) icon-popup 5)
		 * toggle-icon (Default) 6) icon-toggle
		 */
		boolean isDisabled = A.toBool(attrs.get("disabled"));
		mode = A.asString(attrs.get("mode"), "toggle-icon");
		boolean inline = mode.equals("inline");

		if (inline) { // inline => div with ID
			dpId = clientId + "_" + "div";
			rw.startElement("div", datepicker);
			rw.writeAttribute("id", dpId, null);
			rw.endElement("div");
		} else { // popup
			dpId = clientId;

			if (!mode.equals("popup")) { // with icon => div with prepend/append
											// style
				rw.startElement("div", datepicker);
				rw.writeAttribute("class", "input-group", "class");
				if (mode.equals("icon-popup") || mode.equals("icon-toggle")) {
					rw.startElement("span", datepicker);
					rw.writeAttribute("id", clientId + "_" + "input-group-addon", "id");
					rw.writeAttribute("class", "input-group-addon", "class");
					IconRenderer.encodeIcon(rw, datepicker, "calendar", false, null, null, null, false, null, null,
							isDisabled, true);
					rw.endElement("span");
				}
			}
		}
		String type = inline ? "hidden" : "text";

		rw.startElement("input", null);
		rw.writeAttribute("id", clientId, null);
		rw.writeAttribute("name", clientId, null);
		Tooltip.generateTooltip(fc, attrs, rw);
		rw.writeAttribute("type", type, null);
		rw.writeAttribute("class", "form-control", "class");
		if (v != null) {
			rw.writeAttribute("value", getDateAsString(v, datepicker.getSdf(), datepicker.getSloc()), null);
		}

		String ph = A.asString(attrs.get("placeholder"));
		if (ph != null) {
			rw.writeAttribute("placeholder", ph, null);
		}

		if (isDisabled) {
			rw.writeAttribute("disabled", "disabled", null);
		}
		if (A.toBool(attrs.get("readonly"))) {
			rw.writeAttribute("readonly", "readonly", null);
		}
		rw.endElement("input");

		encodeJS(fc, rw, clientId, dpId, datepicker);
		if (mode.equals("popup-icon") || mode.equals("toggle-icon")) {
			rw.startElement("span", datepicker);
			rw.writeAttribute("id", clientId + "_" + "input-group-addon", "id");
			rw.writeAttribute("class", "input-group-addon", "class");

			IconRenderer.encodeIcon(rw, datepicker, "calendar", false, null, null, null, false, null, null, isDisabled,
					true);
			rw.endElement("span");
		}

		if (!inline && !mode.equals("popup")) {
			rw.endElement("div");
			JQ.datePickerToggler(rw, clientId, clientId + "_" + "input-group-addon");

		} // Closes the popup prepend/append style div
	}

	private void encodeJS(FacesContext fc, ResponseWriter rw, String cId, String dpId, Datepicker datepicker)
			throws IOException {
		Map<String, Object> attrs = datepicker.getAttributes();

		StringBuilder sb = new StringBuilder(150);
		sb.append(JQ.DTFORMAT).append(":").append("'" + convertFormat(datepicker.getSdf()) + "'").append(",");

		if (A.toInt(attrs.get(JQ.NUMOFMONTHS)) > 0) {
			sb.append(JQ.NUMOFMONTHS).append(":").append(attrs.get(JQ.NUMOFMONTHS)).append(",");
		}
		if (A.toInt(attrs.get(JQ.FIRSTDAY)) > 0) {
			sb.append(JQ.FIRSTDAY).append(":").append(attrs.get(JQ.FIRSTDAY)).append(",");
		}
		if (A.toBool(attrs.get(JQ.SHOWBUTS))) {
			sb.append(JQ.SHOWBUTS).append(":").append("true").append(",");
		}
		if (A.toBool(attrs.get(JQ.CHNGMONTH))) {
			sb.append(JQ.CHNGMONTH).append(":").append("true").append(",");
		}
		if (A.toBool(attrs.get(JQ.CHNGYEAR))) {
			sb.append(JQ.CHNGYEAR).append(":").append("true").append(",");
		}
		if (A.toBool(attrs.get(JQ.SHOWWK))) {
			sb.append(JQ.SHOWWK).append(":").append("true").append(",");
		}

		if (mode.equals("toggle-icon") || mode.equals("icon-toggle")) {
			sb.append(JQ.SHOWON).append(":").append("'" + "button" + "'").append(",");
		}

		/*
		 * Attributes that need decoding the Date
		 */
		if (attrs.get(JQ.MINDATE) != null) {
			sb.append(JQ.MINDATE + ":" + "'")
					.append(getDateAsString(attrs.get(JQ.MINDATE), datepicker.getSdf(), datepicker.getSloc()))
					.append("'");
		}
		if (attrs.get(JQ.MAXDATE) != null) {
			sb.append(JQ.MAXDATE + ":" + "'")
					.append(getDateAsString(attrs.get(JQ.MAXDATE), datepicker.getSdf(), datepicker.getSloc()))
					.append("'");
		}

		// If user specifies a specific language to use then we render the
		// datepicker using this language
		// else we use the selected locale language
		String l = A.asString(attrs.get(JQ.LANG));
		if (l == null) {
			l = datepicker.getSloc().getLanguage();
		}
		JQ.datePicker(rw, cId, dpId, sb.toString(), l);
	}

	public static String getDateAsString(Object dt, String format, Locale locale) {
		if (dt == null) {
			return null;
		}

		if (dt instanceof String) {
			return (String) dt;
		} else if (dt instanceof Date) {
			SimpleDateFormat dtFormat = new SimpleDateFormat(format, locale);
			dtFormat.setTimeZone(java.util.TimeZone.getDefault());
			// dateFormat.setTimeZone(tz);

			return dtFormat.format((Date) dt);
		} else {
			throw new IllegalArgumentException("Value could be either String or java.util.Date");
		}
	}

	/**
	 * Converts a java Date format to a jQuery date format
	 * 
	 * @param format
	 *            Format to be converted
	 * @return converted format
	 */
	public static String convertFormat(String format) {
		if (format == null)
			return null;
		else {
			// day of week
			format = format.replaceAll("EEE", "D");
			// year
			format = format.replaceAll("yy", "y");

			// month
			if (format.indexOf("MMM") != -1) {
				format = format.replaceAll("MMM", "M");
			} else {
				format = format.replaceAll("M", "m");
			}
			return format;
		}
	}

}
