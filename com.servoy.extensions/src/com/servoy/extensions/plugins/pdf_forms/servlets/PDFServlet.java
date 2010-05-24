/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2010 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 */
package com.servoy.extensions.plugins.pdf_forms.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adobe.fdf.FDFDoc;
import com.servoy.j2db.dataprocessing.IDataServer;
import com.servoy.j2db.dataprocessing.IDataSet;
import com.servoy.j2db.dataprocessing.ISQLStatement;
import com.servoy.j2db.persistence.IServer;
import com.servoy.j2db.persistence.ITable;
import com.servoy.j2db.persistence.RepositoryException;
import com.servoy.j2db.plugins.IServerAccess;
import com.servoy.j2db.server.ApplicationServerSingleton;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.HTTPUtils;
import com.servoy.j2db.util.Utils;

/**
 * @author JBlok
 */
public class PDFServlet extends HttpServlet
{
	private static final String ACTION_PROPERTY = "servoy_action_id"; //$NON-NLS-1$
	private static final String URL_PROPERTY = "servoy_pdf_submit_url"; //$NON-NLS-1$
//	private static final int VIEW = 0;
	private static final int EDIT = 1;
	private static Random rnd = new Random();

	private String PDF_SERVER = "pdf_forms";
	private final IServerAccess app;

	// in order for data broadcast to work correctly we need to sort the values/columns in questiondata in the order they are present inside the Table object;
	// otherwise the insertColumnData in SQLEngine and data broadcast contain values in a different order then how they will be interpreted in event Row creation
	private HashMap<String, Integer> valuesColumnOrderWithoutPk;
	private String valuesColumnInsertStringWithoutPk;
	private HashMap<String, Integer> valuesColumnOrder;
	private String valuesColumnInsertString;

	public PDFServlet(IServerAccess app)
	{
		this.app = app;
		String serverName = app.getSettings().getProperty("pdf_forms_plugin_servername", PDF_SERVER); //$NON-NLS-1$ 
		if (serverName != null && serverName.length() != 0) PDF_SERVER = serverName;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String hostname = request.getServerName();
		int port = request.getServerPort();
		String base = request.getScheme() + "://" + hostname; //$NON-NLS-1$ 
		if (port != 80) base += ":" + port;
		String uri = request.getRequestURI();//with servlet name
		String path = request.getPathInfo(); //without servlet name

		Connection conn = null;
		OutputStream out = null;
		try
		{
			// Determine target page.
			if (path == null)
			{
				response.sendRedirect(request.getRequestURI() + "/");
				return;
			}
			else if ("/".equals(path))
			{
				response.sendError(404);
				return;
			}
			else if (path.startsWith("/pdf_forms/pdf_form"))
			{
				HTTPUtils.setNoCacheHeaders(response);

				String s_action_id = request.getParameter("action_id"); //$NON-NLS-1$
				int action_id = Utils.getAsInteger(s_action_id);
				conn = app.getDBServerConnection(PDF_SERVER, null);
				if (conn == null) Debug.error("Could not find Server " + PDF_SERVER);
				if (action_id > 0 && conn != null)
				{

					Statement st = conn.createStatement();
					ResultSet rs = st.executeQuery("select form_id,template_id,action_type,closed from pdf_actions where action_id = " + action_id); //$NON-NLS-1$
					if (rs.next())
					{
						int form_id = rs.getInt(1);
						int template_id = rs.getInt(2);
						int action_type = rs.getInt(3);
						int closed = rs.getInt(4);

						if (closed == 0)
						{
							FDFDoc outputFDF = new FDFDoc();

							String sub = uri.substring(0, uri.length() - path.length());
							String url = base + sub + "/pdf_forms/pdf_process_data"; //$NON-NLS-1$
							outputFDF.SetValue(ACTION_PROPERTY, Integer.toString(action_id));//add action_id property
							outputFDF.SetValue(URL_PROPERTY, url);//add url property

							//fill
							Statement st1 = conn.createStatement();
							ResultSet rs1 = st1.executeQuery("select value_name,field_value from pdf_form_values where form_id = " + form_id); //$NON-NLS-1$
							while (rs1.next())
							{
								String name = rs1.getString(1);
								String val = rs1.getString(2);
								if (val != null) outputFDF.SetValue(name, val);
							}
							rs1.close();
							st1.close();

							//get name
							String filename = "fromdb"; //$NON-NLS-1$
							boolean skipButton = false;
							Statement st2 = conn.createStatement();
							ResultSet rs2 = st2.executeQuery("select filename,skip_placing_submit_button from pdf_templates where template_id = " + template_id); //$NON-NLS-1$
							if (rs2.next())
							{
								filename = rs2.getString(1);
								skipButton = rs2.getBoolean(2);
							}
							rs2.close();
							st2.close();

							String overrideTemplateLocation = request.getParameter("overrideTemplateLocation"); //$NON-NLS-1$
							if (overrideTemplateLocation == null)
							{
								outputFDF.SetFile(base + sub + "/pdf_forms/pdf_template/" + filename + "?template_id=" + template_id + "&rnd=" + rnd.nextInt());
							}
							else
							{
								outputFDF.SetFile(overrideTemplateLocation);
							}

							StringBuffer sb = new StringBuffer();
							if (action_type == EDIT)
							{
								if (!skipButton)
								{
									sb.append("var inch = 72;\n"); //$NON-NLS-1$
									sb.append("var aRect = this.getPageBox( {nPage: 0} );\n");
									sb.append("aRect[0] = 1;\n");//.5*inch; // position rectangle (.5 inch, .5 inch) 
									sb.append("aRect[2] = aRect[0]+.5*inch;\n"); // from upper left hand corner of page. 
									sb.append("aRect[1] -= 1;//.5*inch;\n"); // Make it .5 inch wide 
									sb.append("aRect[3] = aRect[1] - 24;\n");// and 24 points high 
									//							sb.append("var aRect2 = this.getPageBox( {nPage: 0} );\n");
									//							sb.append("aRect2[0] = .5*inch; // position rectangle (.5 inch, .5 inch)\n");
									//							sb.append("aRect2[2] = aRect2[0]+.5*inch; // from upper left hand corner of page.\n");
									//							sb.append("aRect2[1] -= .5*inch; // Make it .5 inch wide\n");
									//							sb.append("aRect2[3] = aRect2[1] - 24; // and 24 points high\n");
									sb.append("var f = this.addField('servoySubmit', 'button', 0, aRect )\n");
									sb.append("f.setAction('MouseUp', 'this.submitForm(\"" + url + "?action_id=" + action_id + "\")');\n");
									sb.append("f.display = display.noPrint;\n");
									sb.append("f.borderStyle = border.b;\n");
									sb.append("f.highlight = 'push';\n");
									sb.append("f.textSize = 0; // auto sized\n");
									sb.append("f.textColor = color.blue;\n");
									sb.append("f.fillColor = color.green;//ltGray;\n");
									sb.append("f.print = false;\n");
									// sb.append("f.textFont = font.ZapfD\n");
									sb.append("f.buttonSetCaption('Submit')\n");
									sb.append("f.delay = false;\n");
									//							sb.append("var h = this.addField('pdmSubmitAction', 'text', 0, aRect2 )\n");
									//							sb.append("h.visible = display.hidden;\n");
									//							sb.append("h.defaultValue = '"+action_id+"';\n");
									//							sb.append("h.readonly = true;\n");
								}
							}
							else
							{
								sb.append("for (var i = 0; i < this.numFields; i++)\n"); //$NON-NLS-1$
								sb.append("{\n");
								sb.append("    var fname = this.getNthFieldName(i);\n");
								sb.append("    if (fname != 'pdmSubmitAction')\n");
								sb.append("    {\n");
								sb.append("        var ef = this.getField(fname);\n");
								sb.append("        ef.readonly = true;\n");
								sb.append("    }\n");
								sb.append("}\n");
							}

							if (sb.length() != 0) outputFDF.SetOnImportJavaScript(sb.toString(), false);

							response.setContentType("application/vnd.fdf");

							out = response.getOutputStream();

							outputFDF.Save(out);
						}
						else
						{
							String msg = "<html><head><title></title></head><body>Security violation, use the pdf system to edit pdfs</body></html>"; //$NON-NLS-1$
							response.setContentType("text/html");
							Writer wr = response.getWriter();
							wr.write(msg);
							wr.close();
						}
					}
					rs.close();
					st.close();
				}
			}
			else if (path.startsWith("/pdf_forms/pdf_template"))
			{
				HTTPUtils.setNoCacheHeaders(response);

				String s_template_id = request.getParameter("template_id"); //$NON-NLS-1$
				int template_id = Utils.getAsInteger(s_template_id);
				if (template_id > 0)
				{
					conn = app.getDBServerConnection(PDF_SERVER, null);
					if (conn == null) Debug.error("Could not find Server " + PDF_SERVER);
					if (conn != null)
					{
						Statement st = conn.createStatement();
						ResultSet rs = st.executeQuery("select actual_pdf_form from pdf_templates where template_id = " + template_id); //$NON-NLS-1$
						if (rs.next())
						{
							response.setContentType("application/pdf");
							byte[] array = rs.getBytes(1);
							if (array != null)
							{
								response.setContentLength(array.length);
								out = response.getOutputStream();
								out.write(array);
							}
							else
							{
								//missing??
								response.sendError(404);
							}
						}
						rs.close();
						st.close();
					}
					return;
				}
				response.sendError(404);
				return;
			}
			else
			{
				response.sendError(404);
				return;
			}
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		finally
		{
			if (conn != null) Utils.closeConnection(conn);
			if (out != null)
			{
				out.flush();
				out.close();
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String path = request.getPathInfo(); //without servlet name

		PrintWriter out = null;
		try
		{
			if (path == null)
			{
				response.sendRedirect(request.getRequestURI() + "/");
				return;
			}
			else if ("/".equals(path))
			{
				response.sendError(404);
				return;
			}
			else if (path.startsWith("/pdf_forms/pdf_process_data"))
			{
				HTTPUtils.setNoCacheHeaders(response);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				// read data into byte array
				InputStream is = request.getInputStream();

				Utils.streamCopy(is, baos);

				// create FDFDoc from data
				FDFDoc FdfInput = new FDFDoc(baos.toByteArray());

//				Iterator itt = FdfInput.GetFieldNameIterator();
//				while (itt.hasNext())
//				{
//					String element = (String) itt.next();
//					System.out.println(element+" "+FdfInput.GetValue(element));
//				}			

				String s_action_id = request.getParameter("action_id"); //$NON-NLS-1$

				if (s_action_id == null || s_action_id.length() == 0) s_action_id = FdfInput.GetValue(ACTION_PROPERTY);

				int action_id = Utils.getAsInteger(s_action_id);
				if (action_id > 0)
				{
					String redirect_url = null;

					IDataServer ds = ApplicationServerSingleton.get().getDataServer();
					String sql = "select form_id,template_id,closed,redirect_url from pdf_actions where action_id = ?";//$NON-NLS-1$
					IDataSet rs = ds.performQuery(ApplicationServerSingleton.get().getClientId(), PDF_SERVER,
						"pdf_actions", null, sql, new Object[] { new Integer(action_id) }, 0, -1); //$NON-NLS-1$
					for (int r = 0; r < rs.getRowCount(); r++)//normally just one (or zero)
					{
						Object[] row = rs.getRow(r);
						int form_id = Utils.getAsInteger(row[0]);
						int template_id = Utils.getAsInteger(row[1]);
						int closed = Utils.getAsInteger(row[2]);
						redirect_url = (String)row[3];
						if (redirect_url == null || redirect_url.trim().length() == 0)
						{
							IDataSet valContainer = ds.performQuery(ApplicationServerSingleton.get().getClientId(), PDF_SERVER, "pdf_templates", null,
								"select redirect_url from pdf_templates where template_id = ?", new Object[] { new Integer(template_id) }, 0, -1);
							if (valContainer.getRowCount() > 0 && valContainer.getColumnCount() > 0)
							{
								redirect_url = (String)valContainer.getRow(0)[0];
							}
						}

						if (closed == 0)
						{
							Map currentValues = new HashMap();
							String sql1 = "select value_name,fval_id from pdf_form_values where form_id = ?"; //$NON-NLS-1$
							IDataSet rs1 = ds.performQuery(ApplicationServerSingleton.get().getClientId(), PDF_SERVER,
								"pdf_form_values", null, sql1, new Object[] { new Integer(form_id) }, 0, -1); //$NON-NLS-1$
							for (int i = 0; i < rs1.getRowCount(); i++)
							{
								Object[] row1 = rs1.getRow(i);
								currentValues.put(row1[0], row1[1]);
							}

							Iterator it = FdfInput.GetFieldNameIterator();
							while (it.hasNext())
							{
								String name = (String)it.next();
								if (name.equals(ACTION_PROPERTY) || name.equals(URL_PROPERTY)) continue;
								String val = ""; //$NON-NLS-1$
								try
								{
									val = FdfInput.GetValue(name);
								}
								catch (Exception e1)
								{
									//FDFNoValueException is thrown sometimes,ignore
									continue;
								}

								ISQLStatement ps = null;
								if (!currentValues.containsKey(name))
								{
									Number i = (Number)app.getNextSequence(PDF_SERVER, "pdf_form_values", "fval_id"); //$NON-NLS-1$ //$NON-NLS-2$
									Object[] pkData;
									Object[] questionData;
									String sql2;
									// In case we work with DBIDENT (this is the meaning of getting back a NULL), we don't send the id to the database.
									if (i == null)
									{
										pkData = new Object[] { };
										questionData = new Object[3];
										if (valuesColumnOrderWithoutPk == null) createValuesColumnOrderWithoutPk();
										questionData[valuesColumnOrderWithoutPk.get("form_id")] = new Integer(form_id);
										questionData[valuesColumnOrderWithoutPk.get("value_name")] = name;
										questionData[valuesColumnOrderWithoutPk.get("field_value")] = val;

										sql2 = "insert into pdf_form_values (" + valuesColumnInsertStringWithoutPk + ") values (?,?,?)";
									}
									else
									{
										pkData = new Object[] { i };
										questionData = new Object[4];
										if (valuesColumnOrder == null) createValuesColumnOrder();
										questionData[valuesColumnOrder.get("fval_id")] = i;
										questionData[valuesColumnOrder.get("form_id")] = new Integer(form_id);
										questionData[valuesColumnOrder.get("value_name")] = name;
										questionData[valuesColumnOrder.get("field_value")] = val;

										sql2 = "insert into pdf_form_values (" + valuesColumnInsertString + ") values (?,?,?,?)";
									}
									ps = ds.createSQLStatement(ISQLStatement.INSERT_ACTION, PDF_SERVER, "pdf_form_values", pkData, null, sql2, questionData);
								}
								else
								{
									Number fval_id = (Number)currentValues.get(name);
									Object[] pkData = new Object[] { fval_id };
									Object[] questionData = new Object[] { val, fval_id };
									String sql2 = "update pdf_form_values set field_value = ? where fval_id = ?"; //$NON-NLS-1$
									ps = ds.createSQLStatement(ISQLStatement.UPDATE_ACTION, PDF_SERVER, "pdf_form_values", pkData, null, sql2, questionData);

									currentValues.remove(name);
								}
								ds.performUpdates(ApplicationServerSingleton.get().getClientId(), new ISQLStatement[] { ps });
							}

							//delete the leftovers (it seems empty fields are not always submitted)
							List delList = new ArrayList();
							Iterator it2 = currentValues.values().iterator();
							while (it2.hasNext())
							{
								Object fval_id = it2.next();
								Object[] pkData = new Object[] { fval_id };
								Object[] questionData = new Object[] { fval_id };
								ISQLStatement ps = ds.createSQLStatement(ISQLStatement.DELETE_ACTION, PDF_SERVER,
									"pdf_form_values", pkData, null, "delete from pdf_form_values where fval_id = ?", questionData); //$NON-NLS-1$
								delList.add(ps);
							}
							ds.performUpdates(ApplicationServerSingleton.get().getClientId(),
								(ISQLStatement[])delList.toArray(new ISQLStatement[delList.size()]));

							Object[] pkData = new Object[] { new Integer(action_id) };
							Object[] questionData = new Object[] { new Integer(action_id) };
							String sql5 = "update pdf_actions set closed = 1 where action_id = ?"; //$NON-NLS-1$
							ds.performUpdates(ApplicationServerSingleton.get().getClientId(), new ISQLStatement[] { ds.createSQLStatement(
								ISQLStatement.UPDATE_ACTION, PDF_SERVER, "pdf_actions", pkData, null, sql5, questionData) });
						}
						else
						{
							response.setContentType("text/html");
							out = response.getWriter();
							out.println("<html><head><title></title></head><body>Security violation, use the pdf system to edit pdfs</body></html>");
						}
					}

					if (redirect_url != null)
					{
						response.sendRedirect(redirect_url);
					}
					else
					{
						response.setContentType("text/html");
						out = response.getWriter();
						String msg = "<html><head><title></title></head><body>\n" + //$NON-NLS-1$
							"<script language=\"JavaScript\" type=\"text/javascript\">\n" + //$NON-NLS-1$
							"window.opener = self;\n" + //$NON-NLS-1$
							"setTimeout('window.opener.close()',0);\n" + //$NON-NLS-1$
							"</script>\n" + //$NON-NLS-1$
							"Data successfully stored,close this window</body></html>"; //$NON-NLS-1$
						out.println(msg);
					}
				}
				else
				{
					response.setContentType("text/html");
					out = response.getWriter();
					out.println("<html><head><title></title></head><body>Error could not process data (action_id not found)</body></html>");
				}
			}
			else
			{
				response.sendError(404);
				return;
			}
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		finally
		{
			if (out != null)
			{
				out.flush();
				out.close();
			}
		}
	}

	private List<String> getValuesTableColumnNames()
	{
		// sorts and returns relevant column names
		IServer s = app.getDBServer(PDF_SERVER, true, true);
		String[] columnNames = null;
		if (s != null)
		{
			ITable valuesTable = null;
			try
			{
				valuesTable = s.getTable("pdf_form_values");
			}
			catch (RepositoryException e)
			{
				Debug.log(e);
			}
			catch (RemoteException e)
			{
				Debug.log(e);
			}
			if (valuesTable != null)
			{
				columnNames = valuesTable.getColumnNames();
			}
		}
		if (columnNames == null)
		{
			// shouldn't happen, but use some default order anyway
			columnNames = new String[] { "value_name", "field_value", "fval_id", "form_id" };
		}

		List<String> l = new ArrayList<String>();
		for (String cn : columnNames)
		{
			if ("value_name".equals(cn) || "field_value".equals(cn) || "fval_id".equals(cn) || "form_id".equals(cn)) l.add(cn);
		}
		return l;
	}

	private void createValuesColumnOrderWithoutPk()
	{
		List<String> columnList = getValuesTableColumnNames();
		columnList.remove("fval_id");

		valuesColumnInsertStringWithoutPk = "";
		valuesColumnOrderWithoutPk = new HashMap<String, Integer>(columnList.size());
		for (int ii = 0; ii < columnList.size(); ii++)
		{
			valuesColumnInsertStringWithoutPk += columnList.get(ii);
			if (ii + 1 < columnList.size()) valuesColumnInsertStringWithoutPk += ",";
			valuesColumnOrderWithoutPk.put(columnList.get(ii), Integer.valueOf(ii));
		}
	}

	private void createValuesColumnOrder()
	{
		List<String> columnList = getValuesTableColumnNames();

		valuesColumnInsertString = "";
		valuesColumnOrder = new HashMap<String, Integer>(columnList.size());
		for (int ii = 0; ii < columnList.size(); ii++)
		{
			valuesColumnInsertString += columnList.get(ii);
			if (ii + 1 < columnList.size()) valuesColumnInsertString += ",";
			valuesColumnOrder.put(columnList.get(ii), Integer.valueOf(ii));
		}
	}

}
