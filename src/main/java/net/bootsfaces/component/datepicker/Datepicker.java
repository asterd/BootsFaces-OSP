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

package net.bootsfaces.component.datepicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.Resource;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.application.ResourceHandler;
import javax.faces.component.FacesComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import net.bootsfaces.C;
import net.bootsfaces.component.AttributeMapWrapper;
import net.bootsfaces.listeners.AddResourcesListener;
import net.bootsfaces.render.A;
import net.bootsfaces.render.JQ;
import net.bootsfaces.render.Tooltip;

/** This class holds the attributes of &lt;b:datepicker /&gt;. */
@ResourceDependencies({ @ResourceDependency(library = "bsf", name = "css/core.css", target = "head"),
	@ResourceDependency(library = "bsf", name = "css/jq.ui.core.css", target = "head"),
	@ResourceDependency(library = "bsf", name = "css/jq.ui.theme.css", target = "head"),
	@ResourceDependency(library = "bsf", name = "css/jq.ui.datepicker.css", target = "head"),
	@ResourceDependency(library = "bsf", name = "css/bsf.css", target = "head"),
	/* moved to constructor @ResourceDependency(library = "bsf", name = "jq/ui/datepicker.js", target = "head") */
	@ResourceDependency(library = "bsf", name = "js/bsf.js", target = "head"),
	/* moved to constructor @ResourceDependency(library = "bsf", name = "jq/ui/core.js", target = "body"), */
	@ResourceDependency(library = "bsf", name = "css/tooltip.css", target = "head")

})
@FacesComponent("net.bootsfaces.component.datepicker.Datepicker")
public class Datepicker extends HtmlInputText implements net.bootsfaces.render.IHasTooltip {

	public static final String COMPONENT_TYPE = "net.bootsfaces.component.datepicker.Datepicker";

	public static final String COMPONENT_FAMILY = "net.bootsfaces.component";

	public static final String DEFAULT_RENDERER = "net.bootsfaces.component.datepicker.Datepicker";

	private Map<String, Object> attributes = null;

	/**
	 * Selected Locale
	 */
	private Locale sloc;
	/**
	 * selected Date Format
	 */
	private String sdf;

	public Datepicker() {
		Tooltip.addResourceFile();
		setRendererType(DEFAULT_RENDERER);
        AddResourcesListener.addResourceToHeadButAfterJQuery(C.BSF_LIBRARY, "jq/jquery.js");
        AddResourcesListener.addResourceToHeadButAfterJQuery(C.BSF_LIBRARY, "jq/ui/core.js");
		AddResourcesListener.addResourceToHeadButAfterJQuery(C.BSF_LIBRARY, "jq/ui/datepicker.js");
		FacesContext context = FacesContext.getCurrentInstance();
		Application app = context.getApplication();
		ResourceHandler rh = app.getResourceHandler();
		Resource rdp;
		Iterator<Locale> preferredLanguages = context.getExternalContext().getRequestLocales();
		while (preferredLanguages.hasNext()) {
			final String jsl = "jq/ui/i18n/datepicker-" + preferredLanguages.next().getLanguage() + ".js";
			rdp = rh.createResource(jsl, C.BSF_LIBRARY);
			if (rdp != null) { //rdp is null if the language .js is not present in jar
				AddResourcesListener.addResourceToHeadButAfterJQuery(C.BSF_LIBRARY, jsl);
				break;
			}

		}
	}

	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	@Override
	public Map<String, Object> getAttributes() {
		if (attributes == null)
			attributes = new AttributeMapWrapper(this, super.getAttributes());
		return attributes;
	}
	
	@Override
	protected Object getConvertedValue(FacesContext fc, Object sval) throws ConverterException {
		if (sval == null) {
			return null;
		}

		String val = (String) sval;
		// If the Trimmed submitted value is empty, return null
		if (val.trim().length() == 0) {
			return null;
		}

		Converter converter = getConverter();

		// If the user supplied a converter, use it
		if (converter != null) {
			return converter.getAsObject(fc, this, val);
		}
		// Else we use our own converter
		setSloc(selectLocale(fc.getViewRoot().getLocale(), A.asString(getAttributes().get(JQ.LOCALE))));
		setSdf(selectDateFormat(getSloc(), A.asString(getAttributes().get(JQ.DTFORMAT))));
		SimpleDateFormat format = null;
		Object date = null;
		try {
			format = new SimpleDateFormat(getSdf(), getSloc());

			format.setTimeZone(java.util.TimeZone.getDefault());

			date = format.parse(val);
			((Date) date).setHours(12);

		} catch (ParseException e) {
			this.setValid(false);
			throw new ConverterException(getMessage("javax.faces.converter.DateTimeConverter.DATE", val, getSdf(), getLabel(fc)));
		}

		return date;
	}
	
	/**
	 * <p>
	 * Returns the <code>label</code> property from the specified component.
	 * </p>
	 * Simplified and adapted version of the implementation of Mojarra 2.2.8-b02 (see MessageFactory).
	 *
	 * @param context
	 *            - the <code>FacesContext</code> for the current request
	 *
	 * @return the label, if any, of the component
	 */
	public String getLabel(FacesContext context) {
		Object o = getAttributes().get("label");
		if (o == null || (o instanceof String && ((String) o).length() == 0)) {
			ValueExpression vex = getValueExpression("label");
			if (null != vex)
				return (String) vex.getValue(context.getELContext());
		}
		// Use the "clientId" if there was no label specified.
		return (String) getClientId(context);
	}

	/**
	 * <p>
	 * Creates and returns a FacesMessage for the specified Locale.
	 * </p>
	 * Simplified and streamlined version of the implementation of Mojarra 2.2.8-b02 (see MessageFactory).
	 *
	 * @param messageId
	 *            - the key of the message in the resource bundle
	 * @param params
	 *            - substitution parameters
	 *
	 * @return a localized <code>FacesMessage</code> with the severity of FacesMessage.SEVERITY_ERROR
	 */
	public static FacesMessage getMessage(String messageId, String... params) {
		String summary = null;
		String detail = null;
		ResourceBundle bundle;
		String bundleName;
		FacesContext context = FacesContext.getCurrentInstance();
		Locale locale = context.getViewRoot().getLocale();

		// see if we have a user-provided bundle
		Application app = (FacesContext.getCurrentInstance().getApplication());
		if (null != (bundleName = app.getMessageBundle())) {
			if (null != (bundle = ResourceBundle.getBundle(bundleName, locale, Thread.currentThread().getContextClassLoader()))) {
				// see if we have a hit
				try {
					summary = bundle.getString(messageId);
					detail = bundle.getString(messageId + "_detail");
				} catch (MissingResourceException e) {
					// ignore
				}
			}
		}

		// we couldn't find a summary in the user-provided bundle
		if (null == summary) {
			// see if we have a summary in the app provided bundle
			bundle = ResourceBundle.getBundle(FacesMessage.FACES_MESSAGES, locale, Thread.currentThread().getContextClassLoader());
			if (null == bundle) {
				throw new NullPointerException();
			}
			// see if we have a hit
			try {
				summary = bundle.getString(messageId);
				detail = bundle.getString(messageId + "_detail");
			} catch (MissingResourceException e) {
				// ignore
			}
		}

		for (int i = 0; i < params.length; i++) {
			summary = summary.replace("{" + i + "}", params[i]);
			detail = detail.replace("{" + i + "}", params[i]);
		}

		// At this point, we have a summary and a bundle.
		FacesMessage ret = new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail);
		ret.setSeverity(FacesMessage.SEVERITY_ERROR);
		return ret;
	}


	// Pass the attrs timezone value
	/*
	 * public static TimeZone selectTimeZone(Object utz) { java.util.TimeZone selTimeZone; if (utz != null) { if (utz instanceof String) {
	 * selTimeZone = java.util.TimeZone.getTimeZone((String) utz); } else if (utz instanceof java.util.TimeZone) { selTimeZone =
	 * (java.util.TimeZone) utz; } else { throw new IllegalArgumentException("TimeZone should be either String or java.util.TimeZone"); } }
	 * else { selTimeZone = java.util.TimeZone.getDefault(); } return selTimeZone; }
	 */

	// Pass facesContext.getViewRoot().getLocale() and attrs locale value
	public Locale selectLocale(Locale vrloc, Object loc) {
		java.util.Locale selLocale = vrloc;

		if (loc != null) {
			if (loc instanceof String) {
				selLocale = toLocale((String) loc);
			} else if (loc instanceof java.util.Locale) {
				selLocale = (java.util.Locale) loc;
			} else {
				throw new IllegalArgumentException("Type:" + loc.getClass() + " is not a valid locale type for DatePicker:"
						+ this.getClientId());
			}
		}

		return selLocale;
	}
	

	/**
	 * Implementation from Apache Commons Lang
	 * 
	 * @param str
	 * @return
	 */
	public static Locale toLocale(String str) {
		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len != 2 && len != 5 && len < 7) {
			throw new IllegalArgumentException("Invalid locale format: " + str);
		}
		char ch0 = str.charAt(0);
		char ch1 = str.charAt(1);
		if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
			throw new IllegalArgumentException("Invalid locale format: " + str);
		}
		if (len == 2) {
			return new Locale(str, "");
		} else {
			if (str.charAt(2) != '_') {
				throw new IllegalArgumentException("Invalid locale format: " + str);
			}
			char ch3 = str.charAt(3);
			if (ch3 == '_') {
				return new Locale(str.substring(0, 2), "", str.substring(4));
			}
			char ch4 = str.charAt(4);
			if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
				throw new IllegalArgumentException("Invalid locale format: " + str);
			}
			if (len == 5) {
				return new Locale(str.substring(0, 2), str.substring(3, 5));
			} else {
				if (str.charAt(5) != '_') {
					throw new IllegalArgumentException("Invalid locale format: " + str);
				}
				return new Locale(str.substring(0, 2), str.substring(3, 5), str.substring(6));
			}
		}
	}


	/**
	 * Selects the Date Pattern to use based on the given Locale if the input format is null
	 * 
	 * @param locale
	 *            Locale (may be the result of a call to selectLocale)
	 * @param format
	 *            Input format String
	 * @return Date Pattern eg. dd/MM/yyyy
	 */
	public static String selectDateFormat(Locale locale, String format) {
		String selFormat;

		if (format == null) {
			selFormat = ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale)).toPattern();
			// Since DateFormat.SHORT is silly, return a smart format
			if (selFormat.equals("M/d/yy")) {
				return "MM/dd/yyyy";
			}
			if (selFormat.equals("d/M/yy")) {
				return "dd/MM/yyyy";
			}
		} else {
			selFormat = format;
		}

		return selFormat;
	}


	protected enum PropertyKeys {
		binding, changeMonth, changeYear, firstDay, lang, mode, numberOfMonths, placeholder, showButtonPanel, showWeek, tooltip, tooltipContainer, tooltipDelay, tooltipDelayHide, tooltipDelayShow, tooltipPosition;

		String toString;

		PropertyKeys(String toString) {
			this.toString = toString;
		}

		PropertyKeys() {
		}

		public String toString() {
			return ((this.toString != null) ? this.toString : super.toString());
		}
	}

	/**
	 * An el expression referring to a server side UIComponent instance in a
	 * backing bean.
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public javax.faces.component.UIComponent getBinding() {
		javax.faces.component.UIComponent value = (javax.faces.component.UIComponent) getStateHelper()
				.eval(PropertyKeys.binding);
		return value;
	}

	/**
	 * An el expression referring to a server side UIComponent instance in a
	 * backing bean.
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setBinding(javax.faces.component.UIComponent _binding) {
		getStateHelper().put(PropertyKeys.binding, _binding);
	}

	/**
	 * Boolean value to specify if month selector should be shown.
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public boolean isChangeMonth() {
		Boolean value = (Boolean) getStateHelper().eval(PropertyKeys.changeMonth, false);
		return (boolean) value;
	}

	/**
	 * Boolean value to specify if month selector should be shown.
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setChangeMonth(boolean _changeMonth) {
		getStateHelper().put(PropertyKeys.changeMonth, _changeMonth);
	}

	/**
	 * Boolean value to specify if year selector should be shown.
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public boolean isChangeYear() {
		Boolean value = (Boolean) getStateHelper().eval(PropertyKeys.changeYear, false);
		return (boolean) value;
	}

	/**
	 * Boolean value to specify if year selector should be shown.
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setChangeYear(boolean _changeYear) {
		getStateHelper().put(PropertyKeys.changeYear, _changeYear);
	}

	/**
	 * Set the first day of the week: Sunday is 0, Monday is 1, etc.
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public int getFirstDay() {
		Integer value = (Integer) getStateHelper().eval(PropertyKeys.firstDay, 0);
		return (int) value;
	}

	/**
	 * Set the first day of the week: Sunday is 0, Monday is 1, etc.
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setFirstDay(int _firstDay) {
		getStateHelper().put(PropertyKeys.firstDay, _firstDay);
	}

	/**
	 * This option allows you to localize the DatePicker, specifying the
	 * language code (eg. it, fr, es, nl). The datepicker uses the ISO 639-1
	 * language codes eventually followed by ISO 3166-1 country codes. The
	 * Datepicker is localized with the language specified by the ViewRoot
	 * Locale.
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public String getLang() {
		String value = (String) getStateHelper().eval(PropertyKeys.lang);
		return value;
	}

	/**
	 * This option allows you to localize the DatePicker, specifying the
	 * language code (eg. it, fr, es, nl). The datepicker uses the ISO 639-1
	 * language codes eventually followed by ISO 3166-1 country codes. The
	 * Datepicker is localized with the language specified by the ViewRoot
	 * Locale.
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setLang(String _lang) {
		getStateHelper().put(PropertyKeys.lang, _lang);
	}

	/**
	 * Controls how the Calendar is showed, can be inline or popup. Default is
	 * popup.
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public String getMode() {
		String value = (String) getStateHelper().eval(PropertyKeys.mode);
		return value;
	}

	/**
	 * Controls how the Calendar is showed, can be inline or popup. Default is
	 * popup.
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setMode(String _mode) {
		getStateHelper().put(PropertyKeys.mode, _mode);
	}

	/**
	 * Number of months to show.
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public int getNumberOfMonths() {
		Integer value = (Integer) getStateHelper().eval(PropertyKeys.numberOfMonths, 0);
		return (int) value;
	}

	/**
	 * Number of months to show.
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setNumberOfMonths(int _numberOfMonths) {
		getStateHelper().put(PropertyKeys.numberOfMonths, _numberOfMonths);
	}

	/**
	 * The placeholder attribute shows text in a field until the field is
	 * focused upon, then hides the text.
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public String getPlaceholder() {
		String value = (String) getStateHelper().eval(PropertyKeys.placeholder);
		return value;
	}

	/**
	 * The placeholder attribute shows text in a field until the field is
	 * focused upon, then hides the text.
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setPlaceholder(String _placeholder) {
		getStateHelper().put(PropertyKeys.placeholder, _placeholder);
	}

	/**
	 * Boolean value to specify if row Buttons to the bottom of calendar should
	 * be shown.
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public boolean isShowButtonPanel() {
		Boolean value = (Boolean) getStateHelper().eval(PropertyKeys.showButtonPanel, false);
		return (boolean) value;
	}

	/**
	 * Boolean value to specify if row Buttons to the bottom of calendar should
	 * be shown.
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setShowButtonPanel(boolean _showButtonPanel) {
		getStateHelper().put(PropertyKeys.showButtonPanel, _showButtonPanel);
	}

	/**
	 * Boolean value to specify if Week number should be shown.
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public boolean isShowWeek() {
		Boolean value = (Boolean) getStateHelper().eval(PropertyKeys.showWeek, false);
		return (boolean) value;
	}

	/**
	 * Boolean value to specify if Week number should be shown.
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setShowWeek(boolean _showWeek) {
		getStateHelper().put(PropertyKeys.showWeek, _showWeek);
	}

	/**
	 * The text of the tooltip.
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public String getTooltip() {
		String value = (String) getStateHelper().eval(PropertyKeys.tooltip);
		return value;
	}

	/**
	 * The text of the tooltip.
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setTooltip(String _tooltip) {
		getStateHelper().put(PropertyKeys.tooltip, _tooltip);
	}

	/**
	 * Where is the tooltip div generated? That's primarily a technical value
	 * that can be used to fix rendering error in special cases. Also see
	 * data-container in the documentation of Bootstrap. The default value is
	 * body.
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public String getTooltipContainer() {
		String value = (String) getStateHelper().eval(PropertyKeys.tooltipContainer, "body");
		return value;
	}

	/**
	 * Where is the tooltip div generated? That's primarily a technical value
	 * that can be used to fix rendering error in special cases. Also see
	 * data-container in the documentation of Bootstrap. The default value is
	 * body.
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setTooltipContainer(String _tooltipContainer) {
		getStateHelper().put(PropertyKeys.tooltipContainer, _tooltipContainer);
	}

	/**
	 * The tooltip is shown and hidden with a delay. This value is the delay in
	 * milliseconds. Defaults to 0 (no delay).
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public int getTooltipDelay() {
		Integer value = (Integer) getStateHelper().eval(PropertyKeys.tooltipDelay, 0);
		return (int) value;
	}

	/**
	 * The tooltip is shown and hidden with a delay. This value is the delay in
	 * milliseconds. Defaults to 0 (no delay).
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setTooltipDelay(int _tooltipDelay) {
		getStateHelper().put(PropertyKeys.tooltipDelay, _tooltipDelay);
	}

	/**
	 * The tooltip is hidden with a delay. This value is the delay in
	 * milliseconds. Defaults to 0 (no delay).
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public int getTooltipDelayHide() {
		Integer value = (Integer) getStateHelper().eval(PropertyKeys.tooltipDelayHide, 0);
		return (int) value;
	}

	/**
	 * The tooltip is hidden with a delay. This value is the delay in
	 * milliseconds. Defaults to 0 (no delay).
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setTooltipDelayHide(int _tooltipDelayHide) {
		getStateHelper().put(PropertyKeys.tooltipDelayHide, _tooltipDelayHide);
	}

	/**
	 * The tooltip is shown with a delay. This value is the delay in
	 * milliseconds. Defaults to 0 (no delay).
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public int getTooltipDelayShow() {
		Integer value = (Integer) getStateHelper().eval(PropertyKeys.tooltipDelayShow, 0);
		return (int) value;
	}

	/**
	 * The tooltip is shown with a delay. This value is the delay in
	 * milliseconds. Defaults to 0 (no delay).
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setTooltipDelayShow(int _tooltipDelayShow) {
		getStateHelper().put(PropertyKeys.tooltipDelayShow, _tooltipDelayShow);
	}

	/**
	 * Where is the tooltip to be displayed? Possible values: "top", "bottom",
	 * "right", "left", "auto", "auto top", "auto bottom", "auto right" and
	 * "auto left". Default to "bottom".
	 * <P>
	 * 
	 * @return Returns the value of the attribute, or null, if it hasn't been
	 *         set by the JSF file.
	 */
	public String getTooltipPosition() {
		String value = (String) getStateHelper().eval(PropertyKeys.tooltipPosition);
		return value;
	}

	/**
	 * Where is the tooltip to be displayed? Possible values: "top", "bottom",
	 * "right", "left", "auto", "auto top", "auto bottom", "auto right" and
	 * "auto left". Default to "bottom".
	 * <P>
	 * Usually this method is called internally by the JSF engine.
	 */
	public void setTooltipPosition(String _tooltipPosition) {
		getStateHelper().put(PropertyKeys.tooltipPosition, _tooltipPosition);
	}

	public Locale getSloc() {
		return sloc;
	}

	public void setSloc(Locale sloc) {
		this.sloc = sloc;
	}

	public String getSdf() {
		return sdf;
	}

	public void setSdf(String sdf) {
		this.sdf = sdf;
	}

}
