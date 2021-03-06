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
package com.servoy.extensions.beans.dbtreeview.table;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxPostprocessingCallDecorator;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.mozilla.javascript.Function;

import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.SizeUnit;
import com.inmethod.grid.common.ColumnsState;
import com.inmethod.grid.treegrid.TreeGrid;
import com.servoy.base.scripting.api.IJSEvent.EventType;
import com.servoy.extensions.beans.dbtreeview.Binding;
import com.servoy.extensions.beans.dbtreeview.BindingInfo;
import com.servoy.extensions.beans.dbtreeview.FoundSetTreeModel;
import com.servoy.extensions.beans.dbtreeview.FoundSetTreeModel.UserNode;
import com.servoy.extensions.beans.dbtreeview.IWicketTree;
import com.servoy.extensions.beans.dbtreeview.MouseAction;
import com.servoy.extensions.beans.dbtreeview.MouseEventBehavior;
import com.servoy.extensions.beans.dbtreeview.RelationInfo;
import com.servoy.extensions.beans.dbtreeview.WicketTree;
import com.servoy.j2db.dataprocessing.IRecord;
import com.servoy.j2db.dataprocessing.Record;
import com.servoy.j2db.dnd.DRAGNDROP;
import com.servoy.j2db.dnd.ICompositeDragNDrop;
import com.servoy.j2db.dnd.JSDNDEvent;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.server.headlessclient.IWebClientPluginAccess;
import com.servoy.j2db.server.headlessclient.dataui.StyleAppendingModifier;
import com.servoy.j2db.server.headlessclient.dataui.StyleAttributeModifierModel;
import com.servoy.j2db.server.headlessclient.dnd.DraggableBehavior;
import com.servoy.j2db.ui.IStylePropertyChanges;
import com.servoy.j2db.util.DataSourceUtils;


/**
 * Class representing the web client db tree table view
 * 
 * @author gboros
 */
public class InmethodDBTreeTableView extends TreeGrid implements IWicketTree, ITreeTableScriptMethods, ICompositeDragNDrop
{
	private static final long serialVersionUID = 1L;

	private final WicketTree wicketTree;
	private final BindingInfo bindingInfo;

	private final IClientPluginAccess application;
	private final DBTreeTableView dbTreeTableView;
	private final Model dbTreeTableTreeColumnHeaderModel = new Model("");
	private final DBTreeTableTreeColumn dbTreeTableTreeColumn;

	private final List columns;

	private FunctionDefinition fOnDrag;
	private FunctionDefinition fOnDragEnd;
	private FunctionDefinition fOnDragOver;
	private FunctionDefinition fOnDrop;
	private boolean dragEnabled;

	public InmethodDBTreeTableView(String id, IClientPluginAccess application, List columns, DBTreeTableView dbTreeTableView)
	{
		super(id, new Model((Serializable)(new JTree()).getModel()), columns);
		setOutputMarkupId(true);
		setVersioned(false);
		setClickRowToSelect(true);
		setClickRowToDeselect(false);
		setAllowSelectMultiple(false);
		setAutoSelectChildren(false);

		this.application = application;
		this.dbTreeTableView = dbTreeTableView;
		bindingInfo = new BindingInfo(application);
		this.columns = columns;

		dbTreeTableTreeColumn = new DBTreeTableTreeColumn("treeColumn", dbTreeTableTreeColumnHeaderModel);
		columns.add(dbTreeTableTreeColumn);
		setColumnState(new ColumnsState(columns));

		getTree().setRootLess(true);
		wicketTree = new WicketTree(getTree(), bindingInfo, application);

		add(StyleAttributeModifierModel.INSTANCE);

		WebMarkupContainer bodyContainer = (WebMarkupContainer)get("form:bodyContainer"); //$NON-NLS-1$
		if (bodyContainer != null)
		{
			bodyContainer.add(new StyleAppendingModifier(new Model<String>("height: 100%;"))); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.inmethod.grid.treegrid.TreeGrid#onJunctionLinkClicked(org.apache.wicket.ajax.AjaxRequestTarget, java.lang.Object)
	 */
	@Override
	protected void onJunctionLinkClicked(AjaxRequestTarget target, Object node)
	{
		super.onJunctionLinkClicked(target, node);

		// we need to clear all inputs of the form,
		// because this onjunctionclicked is done by a form submit so all 
		// fields have there rawInput set.
		Form< ? > form = getForm();
		if (form != null)
		{
			Form< ? > rootForm = form.getRootForm();
			if (rootForm != null)
			{
				form = rootForm;
			}
			if (!form.hasError())
			{
				form.clearInput();
			}
		}
	}

	/**
	 * @see com.servoy.j2db.ui.IComponent#setCursor(java.awt.Cursor)
	 */
	public void setCursor(Cursor cursor)
	{
		wicketTree.setCursor(cursor);
	}

	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		super.onRender(markupStream);
		synchronized (wicketTree)
		{
			if (!wicketTree.hasChanged)
			{
				wicketTree.jsChangeRecorder.setRendered();
			}
			wicketTree.hasChanged = false;
		}
	}

	/**
	 * @see wicket.Component#getMarkupId()
	 */
	@Override
	public String getMarkupId()
	{
		if (getParent() instanceof ListItem)
		{
			return getParent().getId() + Component.PATH_SEPARATOR + getId();
		}
		else
		{
			return getId();
		}
	}

	public void generateAjaxResponse(AjaxRequestTarget target)
	{
		synchronized (wicketTree)
		{
			boolean isChanged = wicketTree.jsChangeRecorder.isChanged();
			wicketTree.jsChangeRecorder.setRendered();
			if (application instanceof IWebClientPluginAccess) ((IWebClientPluginAccess)application).generateAjaxResponse(target);
			if (isChanged) wicketTree.jsChangeRecorder.setChanged();
		}
	}

	public IClientPluginAccess getClientPluginAccess()
	{
		return application;
	}

	@Override
	protected boolean onCellClicked(AjaxRequestTarget target, IModel rowModel, IGridColumn column)
	{
		boolean onCellClickedReturn = super.onCellClicked(target, rowModel, column);
		onNodeLinkClicked(target, (TreeNode)rowModel.getObject());
		return onCellClickedReturn;
	}

	protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode tn)
	{
		if (tn instanceof FoundSetTreeModel.UserNode)
		{
			wicketTree.getTreeState().selectNode(tn, true);
			FoundSetTreeModel.UserNode un = (FoundSetTreeModel.UserNode)tn;
			IRecord r = un.getRecord();
			if (r != null)
			{
				String returnProvider = bindingInfo.getReturnDataprovider(un);

				String[] server_table = DataSourceUtils.getDBServernameTablename(un.getFoundSet().getDataSource());
				Object[] args = new Object[] { r.getValue(returnProvider), (server_table == null ? null : server_table[1]) };

				FunctionDefinition f = wicketTree.bindingInfo.getCallBack((FoundSetTreeModel.UserNode)tn);
				if (f != null)
				{
					f.executeSync(application, args);
				}
			}
			wicketTree.updateTree(target);
		}
		generateAjaxResponse(target);
	}

	protected void onNodeCheckboxClicked(AjaxRequestTarget target, TreeNode tn)
	{
		if (tn instanceof FoundSetTreeModel.UserNode)
		{
			FoundSetTreeModel.UserNode un = (FoundSetTreeModel.UserNode)tn;
			IRecord r = un.getRecord();
			if (r != null)
			{
				bindingInfo.setCheckBox(un, !bindingInfo.isCheckBoxChecked(un));
				String returnProvider = bindingInfo.getReturnDataproviderOnCheckBoxChange(un);

				String[] server_table = DataSourceUtils.getDBServernameTablename(un.getFoundSet().getDataSource());
				Object[] args = new Object[] { r.getValue(returnProvider), (server_table == null ? null : server_table[1]) };


				FunctionDefinition f = wicketTree.bindingInfo.getMethodToCallOnCheckBoxChange((FoundSetTreeModel.UserNode)tn);
				if (f != null)
				{
					f.executeSync(application, args);
				}
			}
		}

		generateAjaxResponse(target);
	}

	public WicketTree getWicketTree()
	{
		return this.wicketTree;
	}

	public void js_setRoots(Object[] vargs)
	{
		wicketTree.js_setRoots(vargs);
	}

	public void js_setCallBackInfo(Function methodToCallOnClick, String returndp)//can be related dp, when clicked and passed as argument to method
	{
		wicketTree.js_setCallBackInfo(methodToCallOnClick, returndp);
	}

	public void js_bindNodeTooltipTextDataProvider(String dp)//can be related dp
	{
		wicketTree.js_bindNodeTooltipTextDataProvider(dp);
	}

	public void js_bindNodeChildSortDataProvider(String dp)//can be related dp
	{
		wicketTree.js_bindNodeChildSortDataProvider(dp);
	}

	public void js_bindNodeFontTypeDataProvider(String dp)//can be related dp
	{
		wicketTree.js_bindNodeFontTypeDataProvider(dp);
	}

	public void js_bindNodeImageURLDataProvider(String dp)//can be related dp
	{
		wicketTree.js_bindNodeImageURLDataProvider(dp);
	}

	public void js_bindNodeImageMediaDataProvider(String dp)//can be related dp
	{
		wicketTree.js_bindNodeImageMediaDataProvider(dp);
	}

	public void js_setNRelationName(String n_relationName)//normally self join
	{
		wicketTree.js_setNRelationName(n_relationName);
	}

	public void js_setMRelationName(String m_relationName)//incase of n-m inbetween table
	{
		wicketTree.js_setMRelationName(m_relationName);
	}


	/*
	 * readonly/editable---------------------------------------------------
	 */
	public boolean js_isEditable()
	{
		return wicketTree.js_isEditable();
	}

	public void js_setEditable(boolean editable)
	{
		wicketTree.js_setEditable(editable);
	}

	public boolean js_isReadOnly()
	{
		return wicketTree.js_isReadOnly();
	}

	public void js_setReadOnly(boolean b)
	{
		wicketTree.js_setReadOnly(b);
	}

	/*
	 * name---------------------------------------------------
	 */
	public String js_getName()
	{
		return wicketTree.js_getName();
	}

	public void setName(String name)
	{
		wicketTree.setName(name);
	}

	public String getName()
	{
		return wicketTree.getName();
	}

	/*
	 * border---------------------------------------------------
	 */
	public void setBorder(Border border)
	{
		wicketTree.setBorder(border);
	}

	public Border getBorder()
	{
		return wicketTree.getBorder();
	}


	/*
	 * opaque---------------------------------------------------
	 */
	public void setOpaque(boolean opaque)
	{
		wicketTree.setOpaque(opaque);
	}

	public boolean js_isTransparent()
	{
		return wicketTree.js_isTransparent();
	}

	public void js_setTransparent(boolean b)
	{
		wicketTree.js_setTransparent(b);
	}

	public boolean isOpaque()
	{
		return wicketTree.isOpaque();
	}


	/*
	 * tooltip---------------------------------------------------
	 */
	public String js_getToolTipText()
	{
		return wicketTree.js_getToolTipText();
	}

	public void setToolTipText(String tooltip)
	{
		wicketTree.setToolTipText(tooltip);
	}

	public void js_setToolTipText(String tooltip)
	{
		wicketTree.js_setToolTipText(tooltip);
	}

	/**
	 * @see com.servoy.j2db.ui.IComponent#getToolTipText()
	 */
	public String getToolTipText()
	{
		return wicketTree.getToolTipText();
	}

	/*
	 * font---------------------------------------------------
	 */
	public void setFont(Font font)
	{
		wicketTree.setFont(font);
	}

	public void js_setFont(String spec)
	{
		wicketTree.js_setFont(spec);
	}

	public Font getFont()
	{
		return wicketTree.getFont();
	}


	/*
	 * bgcolor---------------------------------------------------
	 */
	public String js_getBgcolor()
	{
		return wicketTree.js_getBgcolor();
	}

	public void js_setBgcolor(String bgcolor)
	{
		wicketTree.js_setBgcolor(bgcolor);
	}

	public void setBackground(Color cbg)
	{
		wicketTree.setBackground(cbg);
	}

	public Color getBackground()
	{
		return wicketTree.getBackground();
	}


	/*
	 * fgcolor---------------------------------------------------
	 */
	public String js_getFgcolor()
	{
		return wicketTree.js_getFgcolor();
	}

	public void js_setFgcolor(String fgcolor)
	{
		wicketTree.js_setFgcolor(fgcolor);
	}

	public void setForeground(Color cfg)
	{
		wicketTree.setForeground(cfg);
	}

	public Color getForeground()
	{
		return wicketTree.getForeground();
	}


	/*
	 * visible---------------------------------------------------
	 */
	public void setComponentVisible(boolean visible)
	{
		wicketTree.setComponentVisible(visible);
	}

	public boolean js_isVisible()
	{
		return wicketTree.js_isVisible();
	}

	public void js_setVisible(boolean visible)
	{
		wicketTree.js_setVisible(visible);
	}

	/*
	 * enabled---------------------------------------------------
	 */
	public void js_setEnabled(boolean enabled)
	{
		setEnabled(enabled);
		wicketTree.js_setEnabled(enabled);
	}

	public void setComponentEnabled(boolean enabled)
	{
		wicketTree.setComponentEnabled(enabled);
	}


	public boolean js_isEnabled()
	{
		return wicketTree.js_isEnabled();
	}

	/*
	 * location---------------------------------------------------
	 */
	public int js_getLocationX()
	{
		return wicketTree.js_getLocationX();
	}

	public int js_getLocationY()
	{
		return wicketTree.js_getLocationY();
	}

	public void js_setLocation(int x, int y)
	{
		wicketTree.js_setLocation(x, y);
	}

	public void setLocation(Point location)
	{
		wicketTree.setLocation(location);
	}

	public Point getLocation()
	{
		return wicketTree.getLocation();
	}


	/*
	 * size---------------------------------------------------
	 */
	public Dimension getSize()
	{
		return wicketTree.getSize();
	}

	public void js_setSize(int width, int height)
	{
		setContentHeight(height - 30, SizeUnit.PX);
		wicketTree.js_setSize(width, -1);
	}

	public void setSize(Dimension size)
	{
		setContentHeight(size.height - 30, SizeUnit.PX);
		size.height = -1;
		wicketTree.setSize(size);
	}

	public int js_getWidth()
	{
		return wicketTree.js_getWidth();
	}

	public int js_getHeight()
	{
		return wicketTree.js_getHeight();
	}

	/*
	 * jsmethods---------------------------------------------------
	 */
	public void js_setNodeLevelVisible(int level, boolean visible)
	{
		wicketTree.js_setNodeLevelVisible(level, visible);
	}


	public Object[] js_getSelectionPath()
	{
		return wicketTree.js_getSelectionPath();
	}

	public void js_setSelectionPath(Object[] selectionPath)
	{
		wicketTree.js_setSelectionPath(selectionPath);
	}


	public void js_setExpandNode(Object[] nodePath, boolean expand_collapse)
	{
		wicketTree.js_setExpandNode(nodePath, expand_collapse);
	}

	public boolean js_isNodeExpanded(Object[] nodePath)
	{
		return wicketTree.js_isNodeExpanded(nodePath);
	}

	public void js_refresh()
	{
		wicketTree.js_refresh();
	}

	public Class[] getAllReturnedTypes()
	{
		return DBTreeTableView.getAllReturnedTypes();
	}

	public IStylePropertyChanges getStylePropertyChanges()
	{
		return wicketTree.jsChangeRecorder;
	}

	public Binding js_createBinding(String datasource)
	{
		Binding binding = new Binding();
		binding.setDataSource(datasource);
		bindingInfo.addBinding(binding);
		return binding;
	}

	public Binding js_createBinding(String serverName, String tableName)
	{
		Binding binding = new Binding();
		binding.setServerName(serverName);
		binding.setTableName(tableName);
		bindingInfo.addBinding(binding);
		return binding;
	}

	public int js_addRoots(Object foundSet)
	{
		return wicketTree.js_addRoots(foundSet);
	}

	public void js_removeAllRoots()
	{
		wicketTree.js_removeAllRoots();
	}

	@Deprecated
	public Column js_createColumn(String servername, String tablename, String header, String fieldname)
	{
		return js_createColumn(servername, tablename, header, fieldname, -1);
	}

	@Deprecated
	public Column js_createColumn(String servername, String tablename, String header, String fieldname, int preferredWidth)
	{
		return js_createColumn(DataSourceUtils.createDBTableDataSource(servername, tablename), header, fieldname, preferredWidth);
	}

	public Column js_createColumn(String datasource, String header, String fieldname)
	{
		return js_createColumn(datasource, header, fieldname, -1);
	}

	public Column js_createColumn(String datasource, String header, String fieldname, int preferredWidth)
	{
		Column column = new Column();
		column.setDBTreeTableView(dbTreeTableView);
		column.setDatasource(datasource);
		column.setPreferredWidth(preferredWidth);
		column.js_setHeader(header);
		column.js_setDataprovider(fieldname);

		dbTreeTableView.addColumn(column);
		while (columns.size() > 1)
			columns.remove(1);
		ArrayList<ArrayList<Column>> sameHeaderColumns = dbTreeTableView.getColumns();
		for (int i = 0; i < sameHeaderColumns.size(); i++)
		{
			columns.add(new DBTreeTableColumn(Integer.toString(columns.size()), bindingInfo, sameHeaderColumns.get(i)));
		}
		setColumnState(new ColumnsState(columns));

		return column;
	}

	public void js_removeAllColumns()
	{
		dbTreeTableView.removeAllColumns();
		Object theTree = columns.get(0);
		columns.clear();
		columns.add(theTree);
		setColumnState(new ColumnsState(columns));
	}

	public void js_setTreeColumnHeader(String treeColumnHeader)
	{
		dbTreeTableView.setTreeColumnHeader(treeColumnHeader);
		dbTreeTableTreeColumnHeaderModel.setObject(treeColumnHeader);
	}

	public void js_setTreeColumnPreferredWidth(int preferredWidth)
	{
		dbTreeTableView.setTreeColumnPreferredWidth(preferredWidth);
		dbTreeTableTreeColumn.setInitialSize(preferredWidth);
	}

	public void js_setRowHeight(int height)
	{
		wicketTree.js_setRowHeight(height);
	}

	private static final CompressedResourceReference CSS = new CompressedResourceReference(InmethodDBTreeTableView.class, "res/style.css"); //$NON-NLS-1$

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.renderCSSReference(CSS);
		Iterator selectedNodesIte = getTreeState().getSelectedNodes().iterator();
		if (selectedNodesIte.hasNext())
		{
			TreeNode firstSelectedNode = (TreeNode)selectedNodesIte.next();
			Component nodeComponent = getTree().getNodeComponent(firstSelectedNode);
			if (nodeComponent != null)
			{
				String treeId = getMarkupId();
				String nodeId = nodeComponent.getMarkupId();
				response.renderOnDomReadyJavascript("document.getElementById('" + treeId + "').scrollTop = document.getElementById('" + nodeId +
					"').offsetTop;\n");
			}
		}
	}


	@Override
	public void onBeforeRender()
	{
		wicketTree.onBeforeRender();
		super.onBeforeRender();
	}

	public RelationInfo js_createRelationInfo()
	{
		return wicketTree.js_createRelationInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeViewScriptMethods#js_setOnDrag(org.mozilla.javascript.Function)
	 */
	public void js_setOnDrag(Function fOnDrag)
	{
		this.fOnDrag = new FunctionDefinition(fOnDrag);
		dragEnabled = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeViewScriptMethods#js_setOnDragEnd(org.mozilla.javascript.Function)
	 */
	public void js_setOnDragEnd(Function fOnDragEnd)
	{
		this.fOnDragEnd = new FunctionDefinition(fOnDragEnd);
		dragEnabled = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeViewScriptMethods#js_setOnDragOver(org.mozilla.javascript.Function)
	 */
	public void js_setOnDragOver(Function fOnDragOver)
	{
		this.fOnDragOver = new FunctionDefinition(fOnDragOver);
		dragEnabled = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeViewScriptMethods#js_setOnDrop(org.mozilla.javascript.Function)
	 */
	public void js_setOnDrop(Function fOnDrop)
	{
		this.fOnDrop = new FunctionDefinition(fOnDrop);
		dragEnabled = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#setStyleClass(java.lang.String)
	 */
	public void setStyleClass(String styleClass)
	{
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#getStyleClass()
	 */
	public String getStyleClass()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.dnd.ICompositeDragNDrop#onDrag(com.servoy.j2db.dnd.JSDNDEvent)
	 */
	public int onDrag(JSDNDEvent event)
	{
		if (fOnDrag != null)
		{
			Object dragReturn = fOnDrag.executeSync(application, new Object[] { event });
			if (dragReturn instanceof Number) return ((Number)dragReturn).intValue();
		}

		return DRAGNDROP.NONE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.dnd.ICompositeDragNDrop#onDragOver(com.servoy.j2db.dnd.JSDNDEvent)
	 */
	public boolean onDragOver(JSDNDEvent event)
	{
		if (fOnDragOver != null)
		{
			Object dragOverReturn = fOnDragOver.executeSync(application, new Object[] { event });
			if (dragOverReturn instanceof Boolean) return ((Boolean)dragOverReturn).booleanValue();
		}

		return fOnDrop != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.dnd.ICompositeDragNDrop#onDrop(com.servoy.j2db.dnd.JSDNDEvent)
	 */
	public boolean onDrop(JSDNDEvent event)
	{
		if (fOnDrop != null)
		{
			Object dropHappened = fOnDrop.executeSync(application, new Object[] { event });
			if (dropHappened instanceof Boolean) return ((Boolean)dropHappened).booleanValue();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.dnd.ICompositeDragNDrop#onDragEnd(com.servoy.j2db.dnd.JSDNDEvent)
	 */
	public void onDragEnd(JSDNDEvent event)
	{
		if (fOnDragEnd != null)
		{
			fOnDragEnd.executeSync(application, new Object[] { event });
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.dnd.ICompositeDragNDrop#getDragSource(java.awt.Point)
	 */
	public Object getDragSource(Point xy)
	{
		// TODO Auto-generated method stub
		return null;
	}

	private JSDNDEvent createScriptEvent(EventType type, Point xy, int modifiers, WebMarkupContainer row)
	{
		JSDNDEvent jsEvent = new JSDNDEvent();
		jsEvent.setType(type);
		//jsEvent.setFormName(getDragFormName());
		//IRecordInternal dragRecord = getDragRecord(xy);
		//if (dragRecord instanceof Record) jsEvent.setRecord((Record)dragRecord);

		jsEvent.setSource(this);
		String dragSourceName = getName();
		if (dragSourceName == null) dragSourceName = getId();
		jsEvent.setElementName(dragSourceName);

		if (xy != null) jsEvent.setLocation(xy);
		jsEvent.setModifiers(modifiers);

		Object rowSource = row.getDefaultModelObject();
		if (rowSource instanceof UserNode)
		{
			IRecord dragRecord = ((UserNode)rowSource).getRecord();
			if (dragRecord instanceof Record) jsEvent.setRecord((Record)dragRecord);
		}

		return jsEvent;
	}

	private void addDragNDropBehavior(final WebMarkupContainer row)
	{
		DraggableBehavior dragBehavior = new DraggableBehavior()
		{
			private boolean isHoverAcceptDrop;

			@Override
			protected void onDragEnd(String id, int x, int y, int m, AjaxRequestTarget ajaxRequestTarget)
			{
				if (getCurrentDragOperation() != DRAGNDROP.NONE)
				{
					JSDNDEvent event = InmethodDBTreeTableView.this.createScriptEvent(EventType.onDragEnd, null, m, row);
					event.setData(getDragData());
					event.setDataMimeType(getDragDataMimeType());
					event.setDragResult(getDropResult() ? getCurrentDragOperation() : DRAGNDROP.NONE);
					InmethodDBTreeTableView.this.onDragEnd(event);
				}

				super.onDragEnd(id, x, y, m, ajaxRequestTarget);
			}

			@Override
			protected boolean onDragStart(final String id, int x, int y, int m, AjaxRequestTarget ajaxRequestTarget)
			{
				JSDNDEvent event = InmethodDBTreeTableView.this.createScriptEvent(EventType.onDrag, new Point(x, y), m, row);
				setDropResult(false);
				int dragOp = InmethodDBTreeTableView.this.onDrag(event);
				if (dragOp == DRAGNDROP.NONE) return false;
				setCurrentDragOperation(dragOp);
				setDragData(event.getData(), event.getDataMimeType());
				isHoverAcceptDrop = false;
				return true;
			}

			@Override
			protected void onDrop(String id, final String targetid, int x, int y, int m, AjaxRequestTarget ajaxRequestTarget)
			{
				if (getCurrentDragOperation() != DRAGNDROP.NONE && isHoverAcceptDrop)
				{
					JSDNDEvent event = InmethodDBTreeTableView.this.createScriptEvent(EventType.onDrop, new Point(x, y), m, row);
					event.setData(getDragData());
					event.setDataMimeType(getDragDataMimeType());
					setDropResult(InmethodDBTreeTableView.this.onDrop(event));
				}
			}

			@Override
			protected void onDropHover(String id, final String targetid, int m, AjaxRequestTarget ajaxRequestTarget)
			{
				if (getCurrentDragOperation() != DRAGNDROP.NONE)
				{
					JSDNDEvent event = InmethodDBTreeTableView.this.createScriptEvent(EventType.onDragOver, null, m, row);
					event.setData(getDragData());
					event.setDataMimeType(getDragDataMimeType());
					isHoverAcceptDrop = InmethodDBTreeTableView.this.onDragOver(event);
				}
			}

		};
		dragBehavior.setUseProxy(true);
		dragBehavior.setRenderOnHead(false);
		row.add(dragBehavior);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#isTransparent()
	 */
	public boolean isTransparent()
	{
		return !isOpaque();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#setTransparent(boolean)
	 */
	public void setTransparent(boolean transparent)
	{
		setOpaque(!transparent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#getBorderType()
	 */
	public Border getBorderType()
	{
		return getBorder();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#setBorderType(javax.swing.border.Border)
	 */
	public void setBorderType(Border border)
	{
		setBorder(border);
	}

	@Override
	protected void onRowPopulated(final WebMarkupContainer rowComponent)
	{
		super.onRowPopulated(rowComponent);
		if (dragEnabled && js_isEnabled()) addDragNDropBehavior(rowComponent);
		rowComponent.add(new MouseEventBehavior(new MouseAction(this)
		{
			@Override
			public String getName()
			{
				return "onclick";
			}

			@Override
			public Object getModelObject()
			{
				return rowComponent.getDefaultModelObject();
			}

			@Override
			public String getReturnProvider(UserNode userNode)
			{
				String returnProvider = bindingInfo.getReturnDataproviderOnClick(userNode);
				if (returnProvider == null)
				{
					returnProvider = wicketTree.bindingInfo.getReturnDataproviderOnClick(userNode);
				}

				return returnProvider;
			}

			@Override
			public FunctionDefinition getMethodToCall(UserNode userNode)
			{
				return wicketTree.bindingInfo.getMethodToCallOnClick(userNode);
			}

			@Override
			public AjaxPostprocessingCallDecorator getPostprocessingCallDecorator()
			{
				return new AjaxPostprocessingCallDecorator(null)
				{
					private static final long serialVersionUID = 1L;

					@SuppressWarnings("nls")
					@Override
					public CharSequence postDecorateScript(CharSequence script)
					{
						return MouseEventBehavior.MOUSE_POSITION_SCRIPT + "Servoy.Utils.startClickTimer(function() { " + script +
							" Servoy.Utils.clickTimerRunning = false; return false; });";
					}
				};
			}
		}));

		rowComponent.add(new MouseEventBehavior(new MouseAction(this)
		{
			@Override
			public String getName()
			{
				return "ondblclick";
			}

			@Override
			public Object getModelObject()
			{
				return rowComponent.getDefaultModelObject();
			}

			@Override
			public String getReturnProvider(UserNode userNode)
			{
				String returnProvider = bindingInfo.getReturnDataproviderOnDoubleClick(userNode);
				if (returnProvider == null)
				{
					returnProvider = wicketTree.bindingInfo.getReturnDataproviderOnDoubleClick(userNode);
				}

				return returnProvider;
			}

			@Override
			public FunctionDefinition getMethodToCall(UserNode userNode)
			{
				return wicketTree.bindingInfo.getMethodToCallOnDoubleClick(userNode);
			}

			@Override
			public AjaxPostprocessingCallDecorator getPostprocessingCallDecorator()
			{
				return new AjaxPostprocessingCallDecorator(null)
				{
					private static final long serialVersionUID = 1L;

					@SuppressWarnings("nls")
					@Override
					public CharSequence postDecorateScript(CharSequence script)
					{
						return MouseEventBehavior.MOUSE_POSITION_SCRIPT + "Servoy.Utils.stopClickTimer();" + script + "return !" +
							IAjaxCallDecorator.WICKET_CALL_RESULT_VAR + ";";
					}
				};
			}
		}));

		rowComponent.add(new MouseEventBehavior(new MouseAction(this)
		{
			@Override
			public String getName()
			{
				return "oncontextmenu";
			}

			@Override
			public Object getModelObject()
			{
				return rowComponent.getDefaultModelObject();
			}

			@Override
			public String getReturnProvider(UserNode userNode)
			{
				String returnProvider = bindingInfo.getReturnDataproviderOnRightClick(userNode);
				if (returnProvider == null)
				{
					returnProvider = wicketTree.bindingInfo.getReturnDataproviderOnRightClick(userNode);
				}

				return returnProvider;
			}

			@Override
			public FunctionDefinition getMethodToCall(UserNode userNode)
			{
				return wicketTree.bindingInfo.getMethodToCallOnRightClick(userNode);
			}

			@SuppressWarnings("nls")
			@Override
			public AjaxPostprocessingCallDecorator getPostprocessingCallDecorator()
			{
				return new AjaxPostprocessingCallDecorator(null)
				{
					private static final long serialVersionUID = 1L;

					@Override
					public CharSequence postDecorateScript(CharSequence script)
					{
						return MouseEventBehavior.MOUSE_POSITION_SCRIPT + script + "return !" + IAjaxCallDecorator.WICKET_CALL_RESULT_VAR + ";";
					}
				};
			}
		}));
	}
}