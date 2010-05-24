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
package com.servoy.extensions.plugins.tabxport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.servoy.extensions.plugins.tabxport.ExportSpecifyDestinationPanel.DataProviderWithLabel;
import com.servoy.j2db.Messages;
import com.servoy.j2db.dataprocessing.IFoundSet;
import com.servoy.j2db.dataprocessing.IRecord;
import com.servoy.j2db.util.Utils;
import com.servoy.j2db.util.gui.FileNameSuggestionFileChooser;
import com.servoy.j2db.util.wizard.IWizard;
import com.servoy.j2db.util.wizard.IWizardPanel;
import com.servoy.j2db.util.wizard.IWizardState;

/**
 * @author jblok
 */
public class ExportSpecifyFilePanel extends JPanel implements ActionListener, IWizardPanel
{
	private StringBuffer fileData;
	private final IWizard parent;
	private final IWizardState state;
	private final JCheckBox header;
	private final JComboBox encodingCombo;
	private final JButton browse;

	public ExportSpecifyFilePanel(IWizard parent, IWizardState state)
	{
		this.parent = parent;
		this.state = state;
		setName("SpecifyFilePanel"); //$NON-NLS-1$
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout(10, 10));

		JPanel fieldPanel = new JPanel();
//		fieldPanel.setPreferredSize(new Dimension(500,20));
//		fieldPanel.setMaximumSize(new Dimension(500,20));
//		fieldPanel.setSize(new Dimension(500,20));
		fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.X_AXIS));
		fieldPanel.add(Box.createHorizontalGlue());
		fieldPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("servoy.plugin.exportImport.specifyFileTitle"))); //$NON-NLS-1$

		browse = new JButton(Messages.getString("servoy.button.browse")); //$NON-NLS-1$
		browse.setActionCommand("browse"); //$NON-NLS-1$
		browse.addActionListener(this);

		JLabel label = new JLabel(Messages.getString("servoy.plugin.exportImport.specifyFileLabel"), SwingConstants.LEFT); //$NON-NLS-1$

		fieldPanel.add(label);
		fieldPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		fieldPanel.add(browse);
		fieldPanel.add(Box.createHorizontalGlue());

		add(fieldPanel, BorderLayout.CENTER);

		JPanel titlePanel = new JPanel();
		header = new JCheckBox(Messages.getString("servoy.plugin.export.exportHeader")); //$NON-NLS-1$
		JLabel encodingLabel = new JLabel(Messages.getString("servoy.plugin.export.exportEncoding")); //$NON-NLS-1$

		Charset sys_file_cs = Charset.forName(System.getProperty("file.encoding"));
		int selectionIndex = 0;
		Object[] encodings = Charset.availableCharsets().values().toArray();
		for (int i = 0; i < encodings.length; i++)
		{
			if (encodings[i].equals(sys_file_cs))
			{
				selectionIndex = i;
			}
		}
		encodingCombo = new JComboBox(encodings);
		encodingCombo.setSelectedIndex(selectionIndex);

		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
		titlePanel.add(header);
		titlePanel.add(Box.createHorizontalGlue());
		titlePanel.add(encodingLabel);
		titlePanel.add(Box.createRigidArea(new Dimension(10, 0)));
		titlePanel.add(encodingCombo);
		titlePanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("servoy.plugin.export.fileOptions.title"))); //$NON-NLS-1$
		add(titlePanel, BorderLayout.NORTH);

		setPreferredSize(new Dimension(550, 400));
	}

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();
		if (command.equals("browse")) browse(); //$NON-NLS-1$
	}

	private void browse()
	{
		try
		{
			String sep = (String)state.getProperty("separator"); //$NON-NLS-1$
			if (header.isSelected())
			{
				DefaultListModel dlm = (DefaultListModel)state.getProperty("dataProviderIDs"); //$NON-NLS-1$
				StringBuffer headerBuffer = new StringBuffer();
				for (int k = 0; k < dlm.getSize(); k++)
				{
					DataProviderWithLabel dpl = (DataProviderWithLabel)dlm.get(k);
					headerBuffer.append("\""); //$NON-NLS-1$
					headerBuffer.append(dpl.label);
					headerBuffer.append("\""); //$NON-NLS-1$
					if (k < dlm.getSize() - 1) headerBuffer.append(sep);
				}
				headerBuffer.append("\n"); //$NON-NLS-1$
				fileData.insert(0, headerBuffer.toString());
			}

			boolean suc6 = false;
			FileNameSuggestionFileChooser fc = new FileNameSuggestionFileChooser();
			String fName = "export.csv"; //$NON-NLS-1$
			if (sep.equals(",")) //$NON-NLS-1$
			{
				fName = "export.csv"; //$NON-NLS-1$
			}
			else if (sep.equals("\t")) //$NON-NLS-1$
			{
				fName = "export.tab"; //$NON-NLS-1$
			}
			fc.suggestFileName(fName);
			int returnVal = fc.showSaveDialog(parent.getMainApplicationWindow());
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				FileOutputStream fos = new FileOutputStream(fc.getSelectedFile());
				Charset encoding = (Charset)encodingCombo.getSelectedItem();
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, encoding));
				bw.write(fileData.toString());
				bw.close();
				suc6 = true;

				fileData = null;
				if (suc6) JOptionPane.showMessageDialog(this, Messages.getString("servoy.plugin.export.success", new Object[] { new Integer(rows) })); //$NON-NLS-1$
				browse.setEnabled(false);
			}
		}
		catch (Exception ex)
		{
			parent.reportError(Messages.getString("servoy.plugin.exportImport.fileSelect.exception"), ex); //$NON-NLS-1$
		}
	}

	public String getNextPanelName()
	{
		return null;//"TransferExportPanel";
	}

	public boolean isDone()
	{
		return (fileData == null && !browse.isEnabled());
	}

	private int rows;

	public Runnable needsToRunFirst(boolean forward)
	{
		browse.setEnabled(forward);

		return new Runnable()
		{
			public void run()
			{
				rows = 0;
				parent.blockGUI(Messages.getString("servoy.plugin.export.status.loadingData")); //$NON-NLS-1$
				try
				{
					fileData = new StringBuffer();
					String sep = (String)state.getProperty("separator"); //$NON-NLS-1$
					DefaultListModel dlm = (DefaultListModel)state.getProperty("dataProviderIDs"); //$NON-NLS-1$
					IFoundSet data = (IFoundSet)state.getProperty("foundset"); //$NON-NLS-1$
					for (int i = 0; i < data.getSize(); i++)
					{
						IRecord s = data.getRecord(i);
						for (int k = 0; k < dlm.getSize(); k++)
						{
							DataProviderWithLabel dpl = (DataProviderWithLabel)dlm.get(k);
							Object obj = s.getValue(dpl.dataProvider.getDataProviderID());
							if (obj instanceof String && ((String)obj).length() != 0)
							{
								fileData.append("\""); //$NON-NLS-1$
								obj = Utils.stringReplace((String)obj, "\"", "\"\""); //$NON-NLS-1$//$NON-NLS-2$
								//obj = Utils.stringReplace((String)obj, "\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
							}
							if (obj instanceof Date)
							{
								fileData.append("\""); //$NON-NLS-1$
							}
							if (obj != null) fileData.append(obj);
							if (obj instanceof String && ((String)obj).length() != 0)
							{
								fileData.append("\""); //$NON-NLS-1$
							}
							if (obj instanceof Date)
							{
								fileData.append("\""); //$NON-NLS-1$
							}
							if (k < dlm.getSize() - 1) fileData.append(sep);
						}
						fileData.append("\n"); //$NON-NLS-1$
						rows++;
					}
				}
				catch (Exception ex)
				{
					rows = 0;
					parent.reportError(Messages.getString("servoy.plugin.export.exception"), ex); //$NON-NLS-1$
				}
				finally
				{
					parent.releaseGUI();
				}
			}
		};
	}
}
