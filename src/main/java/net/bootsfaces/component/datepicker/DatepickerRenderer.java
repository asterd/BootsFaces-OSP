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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import net.bootsfaces.component.icon.IconRenderer;
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
		String clientId = datepicker.getClientId(fc);
		ResponseWriter rw = fc.getResponseWriter();

		datepicker.setSloc(datepicker.selectLocale(fc.getViewRoot().getLocale(), datepicker.getLocale()));
		datepicker.setSdf(Datepicker.selectDateFormat(datepicker.getSloc(), datepicker.getDateFormat()));

		Object v = datepicker.getSubmittedValue();
		if (v == null) {
			v = datepicker.getValue();
		}

		rw.startElement("div", datepicker);
		if (null != datepicker.getDir()) {
			rw.writeAttribute("dir", datepicker.getDir(), "dir");
		}

		if (datepicker.isInline()) {
			rw.writeAttribute("class", "form-inline", "class");

		} else {
			rw.writeAttribute("class", "form-group", "class");
		}
		String label = datepicker.getLabel();
		if (label != null) {
			rw.startElement("label", datepicker);
			rw.writeAttribute("for", "input_" + clientId, "for");
			generateErrorAndRequiredClass(datepicker, rw, clientId);

			rw.writeText(label, null);
			rw.endElement("label");
		}

		/*
		 * 6 modes: 1) inline 2) popup (no icons) 3) popup-icon 4) icon-popup 5)
		 * toggle-icon (Default) 6) icon-toggle
		 */
		boolean isDisabled = datepicker.isDisabled();
		mode = datepicker.getMode();
		boolean inline = mode.equals("inline");

		String dpId;
		if (inline) { // inline => div with ID
			dpId = clientId + "_" + "div";
			rw.startElement("div", datepicker);
			rw.writeAttribute("id", dpId, null);
			writeAttribute(rw, "style", datepicker.getStyle());
			writeAttribute(rw, "class", datepicker.getStyleClass());
			rw.endElement("div");
		} else { // popup
			dpId = clientId;

			if (!mode.equals("popup")) { // with icon => div with prepend/append
											// style
				rw.startElement("div", datepicker);
				rw.writeAttribute("class", "input-group", "class");
				if (mode.equals("icon-popup") || mode.equals("icon-toggle")) {
					rw.startElement("span", datepicker);
					rw.writeAttribute("id", clientId + "_input-group-addon", "id");
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
		Tooltip.generateTooltip(fc, datepicker, rw);
		rw.writeAttribute("type", type, null);
		writeAttribute(rw, "style", datepicker.getStyle());
		String styleClass = datepicker.getStyleClass();
		if (styleClass == null)
			styleClass = "form-control";
		else
			styleClass = "form-control" + styleClass;
		rw.writeAttribute("class", styleClass, "class");
		if (v != null) {
			rw.writeAttribute("value", getDateAsString(v, datepicker.getSdf(), datepicker.getSloc()), null);
		}

		String ph = datepicker.getPlaceholder();
		if (ph != null) {
			rw.writeAttribute("placeholder", ph, null);
		}

		if (isDisabled) {
			rw.writeAttribute("disabled", "disabled", null);
		}
		if (datepicker.isReadonly()) {
			rw.writeAttribute("readonly", "readonly", null);
		}
		rw.endElement("input");

		encodeJS(fc, rw, clientId, dpId, datepicker);
		if (mode.equals("popup-icon") || mode.equals("toggle-icon")) {
			rw.startElement("span", datepicker);
			rw.writeAttribute("id", clientId + "_input-group-addon", "id");
			rw.writeAttribute("class", "input-group-addon", "class");

			IconRenderer.encodeIcon(rw, datepicker, "calendar", false, null, null, null, false, null, null, isDisabled,
					true);
			rw.endElement("span");
		}

		if (!inline && !mode.equals("popup")) {
			rw.endElement("div");
			JQ.datePickerToggler(rw, clientId, clientId + "_input-group-addon");

		} // Closes the popup prepend/append style div
		rw.endElement("div"); // closes the form-group div
	}

	private void encodeJS(FacesContext fc, ResponseWriter rw, String cId, String dpId, Datepicker datepicker)
			throws IOException {

		StringBuilder sb = new StringBuilder(150);
		sb.append(JQ.DTFORMAT).append(":").append("'" + convertFormat(datepicker.getSdf()) + "'").append(",");

		if (datepicker.getNumberOfMonths() > 0) {
			sb.append(JQ.NUMOFMONTHS).append(":").append(datepicker.getNumberOfMonths()).append(",");
		}
		if (datepicker.getFirstDay() > 0) {
			sb.append(JQ.FIRSTDAY).append(":").append(datepicker.getFirstDay()).append(",");
		}
		if (datepicker.isShowButtonPanel()) {
			sb.append("showButtonPanel").append(":").append("true").append(",");
		}
		if (datepicker.isChangeMonth()) {
			sb.append("changeMonth").append(":").append("true").append(",");
		}
		if (datepicker.isChangeYear()) {
			sb.append("changeYear").append(":").append("true").append(",");
		}
		if (datepicker.isShowWeek()) {
			sb.append("showWeek").append(":").append("true").append(",");
		}

		if (mode.equals("toggle-icon") || mode.equals("icon-toggle")) {
			sb.append("showOn").append(":").append("'" + "button" + "'").append(",");
		}

		/*
		 * Attributes that need decoding the Date
		 */
		if (datepicker.getMinDate() != null) {
			sb.append("minDate" + ":" + "'")
					.append(getDateAsString(datepicker.getMinDate(), datepicker.getSdf(), datepicker.getSloc()))
					.append("'");
		}
		if (datepicker.getMaxDate() != null) {
			sb.append("maxDate" + ":" + "'")
					.append(getDateAsString(datepicker.getMaxDate(), datepicker.getSdf(), datepicker.getSloc()))
					.append("'");
		}

		// If user specifies a specific language to use then we render the
		// datepicker using this language
		// else we use the selected locale language
		String l = datepicker.getLang();
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
