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

package net.bootsfaces.component.dataTable;

import net.bootsfaces.component.ajax.AJAXRenderer;
import net.bootsfaces.component.dataTable.DataTable.DataTablePropertyType;
import net.bootsfaces.render.CoreRenderer;
import net.bootsfaces.render.Tooltip;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/** This class generates the HTML code of &lt;b:dataTable /&gt;. */
@FacesRenderer(componentFamily = "net.bootsfaces.component", rendererType = "net.bootsfaces.component.dataTable.DataTable")
public class DataTableRenderer extends CoreRenderer {

	/**
	 * This methods generates the HTML code of the current b:dataTable.
	 * <code>encodeBegin</code> generates the start of the component. After the,
	 * the JSF framework calls <code>encodeChildren()</code> to generate the
	 * HTML code between the beginning and the end of the component. For
	 * instance, in the case of a panel component the content of the panel is
	 * generated by <code>encodeChildren()</code>. After that,
	 * <code>encodeEnd()</code> is called to generate the rest of the HTML code.
	 * 
	 * @param context
	 *            the FacesContext.
	 * @param component
	 *            the current b:dataTable.
	 * @throws IOException
	 *             thrown if something goes wrong when writing the HTML code.
	 */
	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

		if (!component.isRendered()) {
			return;
		}
		DataTable dataTable = (DataTable) component;

		ResponseWriter rw = context.getResponseWriter();
		String clientId = dataTable.getClientId();

		// put custom code here
		// Simple demo widget that simply renders every attribute value
		rw.startElement("table", dataTable);
		rw.writeAttribute("id", clientId, "id");

		String styleClass = "table table-striped table-bordered";
		if (dataTable.getStyleClass() != null)
			styleClass += " " + dataTable.getStyleClass();
		styleClass += " " + clientId.replace(":", "") + "Table";
		rw.writeAttribute("class", styleClass, "class");
		Tooltip.generateTooltip(context, dataTable, rw);
		rw.writeAttribute("cellspacing", "0", "cellspacing");
		rw.writeAttribute("style", dataTable.getStyle(), "style");
		AJAXRenderer.generateBootsFacesAJAXAndJavaScript(context, dataTable, rw);

		generateHeader(context, dataTable, rw);
		generateBody(context, dataTable, rw);
		generateFooter(context, dataTable, rw);
	}

	private void generateFooter(FacesContext context, DataTable dataTable, ResponseWriter rw) throws IOException {
		rw.startElement("tfoot", dataTable);
		rw.startElement("tr", dataTable);
		List<UIComponent> columns = dataTable.getChildren();
		for (UIComponent column : columns) {
			rw.startElement("th", dataTable);
			if (column.getFacet("header") != null) {
				UIComponent facet = column.getFacet("header");
				facet.encodeAll(context);
			}
			rw.endElement("th");
		}
		rw.endElement("tr");
		rw.endElement("tfoot");
	}

	private void generateBody(FacesContext context, DataTable dataTable, ResponseWriter rw) throws IOException {
		rw.startElement("tbody", dataTable);
		int rows = dataTable.getRowCount();
		dataTable.setRowIndex(-1);
		for (int row = 0; row < rows; row++) {
			dataTable.setRowIndex(row);
			if (dataTable.isRowAvailable()) {
				rw.startElement("tr", dataTable);
				List<UIComponent> columns = dataTable.getChildren();
				for (UIComponent column : columns) {
					rw.startElement("td", dataTable);
					column.encodeChildren(context);
					rw.endElement("td");
				}
				rw.endElement("tr");
			}
		}
		rw.endElement("tbody");
		dataTable.setRowIndex(-1);
	}

	private void generateHeader(FacesContext context, DataTable dataTable, ResponseWriter rw) throws IOException {
		rw.startElement("thead", dataTable);
		rw.startElement("tr", dataTable);
		int index = 0;
		List<UIComponent> columns = dataTable.getChildren();
		for (UIComponent column : columns) {
			rw.startElement("th", dataTable);
			if (column.getFacet("header") != null) {
				UIComponent facet = column.getFacet("header");
				facet.encodeAll(context);
			} else if (column.getAttributes().get("label") != null) {
				rw.writeText(column.getAttributes().get("label"), null);
			} else {
				boolean labelHasBeenRendered = false;
				for (UIComponent c : column.getChildren()) {
					if (c.getAttributes().get("label") != null) {
						rw.writeText(c.getAttributes().get("label"), null);
						labelHasBeenRendered = true;
						break;
					}
				}
				if (!labelHasBeenRendered) {
					for (UIComponent c : column.getChildren()) {
						if (c.getAttributes().get("value") != null) {
							rw.writeText(c.getAttributes().get("value"), null);
							labelHasBeenRendered = true;
							break;
						}
					}

				}
				if (!labelHasBeenRendered) {
					rw.writeText("Column #" + index, null);
				}
			}

			rw.endElement("th");
			index++;
		}
		rw.endElement("tr");
		rw.endElement("thead");
	}

	/**
	 * This methods generates the HTML code of the current b:dataTable.
	 * <code>encodeBegin</code> generates the start of the component. After the,
	 * the JSF framework calls <code>encodeChildren()</code> to generate the
	 * HTML code between the beginning and the end of the component. For
	 * instance, in the case of a panel component the content of the panel is
	 * generated by <code>encodeChildren()</code>. After that,
	 * <code>encodeEnd()</code> is called to generate the rest of the HTML code.
	 * 
	 * @param context
	 *            the FacesContext.
	 * @param component
	 *            the current b:dataTable.
	 * @throws IOException
	 *             thrown if something goes wrong when writing the HTML code.
	 */
	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		if (!component.isRendered()) {
			return;
		}
		DataTable dataTable = (DataTable) component;
		Map<DataTablePropertyType, Object> dataTableProperties = dataTable.getDataTableProperties();
		Integer page = 0;
		Integer pageLength = 10;
		String searchTerm = "''";
		if(dataTableProperties != null){
			Object currentPage = dataTableProperties.get( DataTablePropertyType.currentPage );
			Object currentPageLength = dataTableProperties.get( DataTablePropertyType.pageLength );
			Object currentSearchTerm = dataTableProperties.get( DataTablePropertyType.searchTerm );
			if(currentPage != null){
				page = (Integer)currentPage;
			}
			if(currentPageLength != null){
				pageLength = (Integer)currentPageLength;
			}
			if(currentSearchTerm != null){
				searchTerm = String.format("'%s'", (String)currentSearchTerm);
			}
		}
		ResponseWriter rw = context.getResponseWriter();
		String clientId = dataTable.getClientId().replace(":", "");
		rw.endElement("table");
		Tooltip.activateTooltips(context, dataTable);
		rw.startElement("script", component);
		//# Start JS
		rw.writeText("$(document).ready(function() {", null);
		//# Initialize table at nth page
		rw.writeText("var element = $('." + clientId + "Table" + "');" +
					 "var table = element.DataTable();" +
					 "table.page("+page+");" +
					 "table.search("+searchTerm+");" +
					 "table.page.len("+pageLength+").draw('page');", null);
		//# Event setup: http://datatables.net/reference/event/page
		rw.writeText( "element.on('page.dt', function(){" +
					  "var info = table.page.info();" +
					  "BsF.ajax.callAjax(this, event, null, null, null, " +
					  "'" + DataTablePropertyType.currentPage + ":'+info.page);" +
					  "});", null );
		//# Event setup: https://datatables.net/reference/event/length
		rw.writeText( "element.on('length.dt', function(e, settings, len) {" +
					  "BsF.ajax.callAjax(this, event, null, null, null, " +
					  "'" + DataTablePropertyType.pageLength + ":'+len);" +
					  "});", null );
		//# Event setup: https://datatables.net/reference/event/search
		rw.writeText( "element.on('search.dt', function() {" +
					  "BsF.ajax.callAjax(this, event, null, null, null, " +
					  "'" + DataTablePropertyType.searchTerm + ":'+table.search());" +
					  "});", null );
		//# Footer stuff: https://datatables.net/examples/api/multi_filter.html
		//# Convert footer column text to input textfields
		rw.writeText( "$('#" + clientId +" tfoot th').each(function() {" +
					  "var title = $(this).text();" +
					  "$(this).html('<input class=\"form-control input-sm\" type=\"text\" placeholder=\"Search ' + title + '\" />');" +
					  "});", null );
		//# Add event listeners for each input
		rw.writeText( "table.columns().every( function () {" +
					  "var that = this;" +
					  "$( 'input', this.footer() ).on( 'keyup change', function () {" +
					  "    if ( that.search() !== this.value ) {" +
					  "        that.search( this.value ).draw('page');" +
					  "    }" +
					  "} );" +
					  "} );", null );
		//# End JS
		rw.writeText("} );",null );
		rw.endElement("script");
	}

	@Override
	public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
		// Children are already rendered in encodeBegin()
	}

}
