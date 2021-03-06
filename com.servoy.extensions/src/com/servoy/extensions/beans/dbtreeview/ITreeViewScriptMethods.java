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
package com.servoy.extensions.beans.dbtreeview;

import org.mozilla.javascript.Function;

import com.servoy.base.scripting.annotations.ServoyClientSupport;
import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;

/**
 * Interface to enforce the swing and web bean instance to be the same
 * 
 * @author jblok
 */
@ServoyClientSupport(ng = false, wc = true, sc = true)
public interface ITreeViewScriptMethods extends IReturnedTypesProvider, IScriptable, ITreeView
{
	/**
	 * Property for getting/setting the background color of the bean.
	 * 
	 * @sample
	 * %%elementName%%.bgcolor = "#FFFFFF";
	 * var x = %%elementName%%.bgcolor;
	 *  
	 * @return the background color
	 */
	public String js_getBgcolor();

	/**
	 * @param bg the background color of the bean
	 */
	public void js_setBgcolor(String bg);

	/**
	 * Property for getting/setting the foreground color of the bean.
	 * 
	 * @sample
	 * %%elementName%%.fgcolor = "#000000";
	 * var y = %%elementName%%.fgcolor;
	 * 
	 * @return
	 */
	public String js_getFgcolor();

	/**
	 * @param fg the foreground color of the bean
	 */
	public void js_setFgcolor(String fg);

	/**
	 * Get the x coordinate of the location of the tree.
	 * 
	 * @sample
	 * %%elementName%%.getLocationX();
	 * 
	 * @return The x coordinate
	 */
	public int js_getLocationX();

	/**
	 * Get the y coordinate of the location of the tree.
	 * 
	 * @sample
	 * %%elementName%%.getLocationY();
	 * 
	 * @return The y coordinate
	 */
	public int js_getLocationY();

	/**
	 * Sets the location of the tree.
	 * 
	 * @sample
	 * %%elementName%%.setLocation(120,80);
	 * 
	 * @param x
	 * @param y
	 */
	public void js_setLocation(int x, int y);

	/**
	 * Returns the width of the tree.
	 * 
	 * @sample
	 * %%elementName%%.getWidth();
	 * 
	 * @return The width
	 */
	public int js_getWidth();

	/**
	 * Returns the height of the tree.
	 * 
	 * @sample
	 * %%elementName%%.getHeight();
	 * 
	 * @return The height
	 */
	public int js_getHeight();

	/**
	 * Sets the size of the tree.
	 * 
	 * @sample
	 * %%elementName%%.setSize(400,300);
	 * 
	 * @param w
	 * @param h
	 */
	public void js_setSize(int w, int h);

	/**
	 * Returns the name of the tree.
	 * 
	 * @sample
	 * %%elementName%%.getName();
	 * 
	 * @return
	 */
	public String js_getName();

	/**
	 * Get/Set the selection (path), array with pk records values (only single pk key supported)
	 *
	 * @sample
	 * %%elementName%%.selectionPath = new Array(14,24,45,67);
	 * var currentSelectionArray = %%elementName%%.selectionPath; 
	 * 
	 */
	public Object[] js_getSelectionPath();

	public void js_setSelectionPath(Object[] path);

	/**
	 * Property for getting/setting the text of the bean's tooltip
	 * 
	 * @sample
	 * %%elementName%%.toolTipText = 'Hello world!';
	 * 
	 * @return the tooltip text of the bean
	 */
	public String js_getToolTipText();

	/**
	 * @param tip the text for the tooltip of the bean
	 */
	public void js_setToolTipText(String tip);

	/**
	 * Property for getting/setting the enabled state of the bean
	 * 
	 * @sample
	 * var b = %%elementName%%.enabled;
	 * if (b) %%elementName%%.enabled = false;
	 * 
	 * @return the enabled state of the bean
	 */
	public boolean js_isEnabled();

	/**
	 * @param enabled the enabled state of the bean
	 */
	public void js_setEnabled(boolean enabled);

	/**
	 * Property for getting/setting the transparent or opaque state of the bean
	 * 
	 * @sample
	 * var t = %%elementName%%.transparent;
	 * if (!t) %%elementName%%.transparent = true;  
	 * 
	 * @return the transparent state of the bean
	 */
	public boolean js_isTransparent();

	/**
	 * @param transparent the transparent state of the bean (true for transparent, false for opaque)
	 */
	public void js_setTransparent(boolean transparent);

	/**
	 * Property for getting/setting the visibility of the bean.
	 * 
	 * @sample
	 * var v = %%elementName%%.visible;
	 * 
	 * @return the visibility of the bean
	 */
	public boolean js_isVisible();

	/**
	 * @param visible the visible state of the bean
	 */
	public void js_setVisible(boolean visible);

	/**
	 * Sets the specified font as the font of the tree.
	 * 
	 * @sample
	 * %%elementName%%.setFont('Times New Roman, 1, 22');
	 * @param font
	 */
	public void js_setFont(String font);

	/**
	 * Expand/collapse the path, array with pk records values (only single pk key supported)
	 * 
	 * @sample
	 * var pathArray = new Array(14,24,45,67);
	 * %%elementName%%.setExpandNode(pathArray, true);
	 * 
	 * @param nodePath
	 * 
	 * @param expand_collapse
	 * 
	 */
	public void js_setExpandNode(Object[] nodePath, boolean expand_collapse);

	/**
	 * Check the path (array with pk records values (only single pk key supported)) expanded status
	 * 
	 * @sample
	 * var pathArray = new Array(14,24,45,67);
	 * %%elementName%%.isNodeExpanded(pathArray);
	 * 
	 * @param nodePath
	 * 
	 * @return True if the node is expanded, False otherwise
	 */
	public boolean js_isNodeExpanded(Object[] nodePath);

	/**
	 * Set the level of visible nodes (expand or collapse to certain level)
	 *
	 * @sample
	 * %%elementName%%.setNodeLevelVisible(scopes.globals.g_treeview_level, (scopes.globals.g_treeview_expand == 1 ? true : false)); 
	 * 
	 * @param level
	 * 
	 * @param visible
	 * 
	 */
	public void js_setNodeLevelVisible(int level, boolean visible);

	/**
	 * Refresh the tree display
	 * 
	 * @sample
	 * %%elementName%%.refresh();
	 * 
	 */
	public void js_refresh();

	/**
	 * Add foundset to the list of foundsets used to create the tree's root nodes.\nNote: the bean will use a clone of the foundset, so any changes on the foundset parameter will be ignored in the tree. 
	 * 
	 * @sample
	 * var addedRootNodes = %%elementName%%.addRoots(foundset);
	 * 
	 * @param foundSet
	 * @return The number of added root nodes
	 */
	public int js_addRoots(Object foundSet);

	/**
	 * Remove all root foundsets
	 * 
	 * @sample
	 * %%elementName%%.removeAllRoots();
	 * 
	 */
	public void js_removeAllRoots();

	/**
	 * Create and add binding object for a database table used to set data bindings for nodes.
	 * 
	 * @sample
	 * var companies_binding = %%elementName%%.createBinding('example_data', 'companies');
	 * companies_binding.setTextDataprovider('company_name');
	 * companies_binding.setNRelationName('companies_to_companies');
	 * companies_binding.setImageURLDataprovider('type_icon');
	 * companies_binding.setChildSortDataprovider('company_sort');
	 * 
	 * @param datasource
	 * @return Binding object for a database table
	 */
	public Binding js_createBinding(String datasource);

	/**
	 * @clodenesc {@link #js_createBinding(String)}
	 * @sample
	 * /** @type {JSFoundSet<db:/udm/companies>} *&#47;
	 * var fs = databaseManager.getFoundSet("db:/udm/companies");
	 * var companies_binding = %%elementName%%.createBinding(fs.getDataSource());
	 * companies_binding.setTextDataprovider('company_name');
	 * companies_binding.setNRelationName('companies_to_companies');
	 * companies_binding.setImageURLDataprovider('type_icon');
	 * companies_binding.setChildSortDataprovider('company_sort');
	 * 
	 * @param serverName
	 * @param tableName
	 * @return Binding object for a database table
	 */
	public Binding js_createBinding(String serverName, String tableName);

	/**
	 * Create relation info object used to set multiple child relations for a tree node
	 * 
	 * @sample
	 * var company_relations = new Array();
	 * company_relations[0] = %%elementName%%.createRelationInfo();
	 * company_relations[0].setLabel('Employees');
	 * company_relations[0].setNRelationName('companies_to_employees');
	 * company_relations[1] = %%elementName%%.createRelationInfo();
	 * company_relations[1].setLabel('Customers');
	 * company_relations[1].setNRelationName('companies_to_customers');
	 * companies_binding.setNRelationInfos(company_relations);
	 * 
	 * @return RelationInfo object
	 */
	public RelationInfo js_createRelationInfo();

	/**
	 * Set row height
	 * 
	 * @sample
	 * %%elementName%%.setRowHeight(40);
	 * 
	 * @param height
	 */
	public void js_setRowHeight(int height);

	/**
	 * Set method to be called when a drag is started on the tree. For more details about the method arguments and return value check the same property of a form
	 * 
	 * @sample
	 * %%elementName%%.setOnDrag(onDrag);
	 * 
	 * @param fOnDrag
	 */
	public void js_setOnDrag(Function fOnDrag);

	/**
	 * Set method to be called when a drag of on the tree is ended. For more details about the method arguments and return value check the same property of a form
	 * 
	 * @sample
	 * %%elementName%%.setOnDragEnd(onDragEnd);
	 * 
	 * @param fOnDragEnd
	 */
	public void js_setOnDragEnd(Function fOnDragEnd);

	/**
	 * Set method to be called during a drag over the tree. For more details about the method arguments and return value check the same property of a form
	 * 
	 * @sample
	 * %%elementName%%.setOnDragOver(onDragOver);
	 * @param fOnDragOver
	 */
	public void js_setOnDragOver(Function fOnDragOver);

	/**
	 * Set method to be called on a drop on the tree. For more details about the method arguments and return value check the same property of a form
	 * 
	 * @sample
	 * %%elementName%%.setOnDrop(onDrop);
	 * @param fOnDrop
	 */
	public void js_setOnDrop(Function fOnDrop);

	/**
	 * for compatibility, just call addRoots,createBinding (store in compatibility Bindings instance) with passed info
	 * 
	 * @deprecated for compatibility, just call addRoots,createBinding (store in compatibility Bindings instance) with passed info
	 */
	@Deprecated
	public void js_setRoots(Object[] vargs);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated use Binding.setMRelationName(name) instead
	 */
	@Deprecated
	public void js_setMRelationName(String name);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated use Binding.setNRelationName(name) instead
	 */
	@Deprecated
	public void js_setNRelationName(String name);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated use Binding.setFontTypeDataprovider(fontTypeDataprovider) instead
	 */
	@Deprecated
	public void js_bindNodeFontTypeDataProvider(String dp);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated use Binding.setImageMediaDataprovider(imageMediaDataprovider) instead
	 */
	@Deprecated
	public void js_bindNodeImageMediaDataProvider(String dp);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated use Binding.setImageURLDataprovider(imageURLDataprovider) instead
	 */
	@Deprecated
	public void js_bindNodeImageURLDataProvider(String dp);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated use Binding.setToolTipTextDataprovider(toolTipTextDataprovider) instead
	 */
	@Deprecated
	public void js_bindNodeTooltipTextDataProvider(String dp);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated use Binding.setCallBackInfo(f,returnDataprovider)
	 */
	@Deprecated
	public void js_setCallBackInfo(Function f, String returndp);

	/**
	 * @deprecated use Binding.setChildSortDataprovider(childSortDataprovider)
	 */
	@Deprecated
	public void js_bindNodeChildSortDataProvider(String dp);

}
