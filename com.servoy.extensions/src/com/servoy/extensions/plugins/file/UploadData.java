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

package com.servoy.extensions.plugins.file;

import java.io.File;

import com.servoy.j2db.plugins.IUploadData;

/**
 * Implementation of an {@link IAbstractFile} for web accessible file<br/>

 * @author jcompagner
 * @since Servoy 5.2
 */
public class UploadData extends AbstractFile
{

	private final IUploadData upload;

	public UploadData(IUploadData upload)
	{
		this.upload = upload;
	}

	@Override
	public File getFile()
	{
		return upload.getFile();
	}

	public byte[] getBytes()
	{
		return upload.getBytes();
	}

	public String getName()
	{
		return upload.getName();
	}

	@Override
	public String getContentType()
	{
		return upload.getContentType();
	}

	@Override
	public long lastModified()
	{
		return System.currentTimeMillis();
	}

	@Override
	public long size()
	{
		return getBytes().length;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		return "UploadData[name:" + getName() + ",contenttype:" + getContentType() + "]";
	}

}