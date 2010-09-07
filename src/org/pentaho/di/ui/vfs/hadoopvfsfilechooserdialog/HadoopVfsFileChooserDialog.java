/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 */

package org.pentaho.di.ui.vfs.hadoopvfsfilechooserdialog;

import java.net.Socket;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.GenericFileName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.HadoopSpoonPlugin;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hdfs.vfs.HDFSFileObject;
import org.pentaho.vfs.ui.CustomVfsUiPanel;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

public class HadoopVfsFileChooserDialog extends CustomVfsUiPanel {

  // for message resolution
  private static Class<?> PKG = HadoopVfsFileChooserDialog.class;

  // for logging
  private LogChannel log = new LogChannel(this);

  // URL label and field
  private Label wlUrl;
  private Text wUrl;
  private GridData fdlUrl, fdUrl;

  // Port label and field
  private Label wlPort;
  private Text wPort;
  private GridData fdlPort, fdPort;

  // UserID label and field
  private Label wlUserID;
  private Text wUserID;
  private GridData fdlUserID, fdUserID;

  // Password label and field
  private Label wlPassword;
  private Text wPassword;
  private GridData fdlPassword, fdPassword;

  // Place holder - for creating a blank widget in a grid layout
  private Label wPlaceHolderLabel;
  private GridData fdlPlaceHolderLabel;

  // Connection button
  private Button wConnectionButton;
  private GridData fdConnectionButton;

  // Default root file - used to avoid NPE when rootFile was not provided
  // and the browser is resolved
  FileObject defaultInitialFile = null;

  // File objects to keep track of when the user selects the radio buttons
  FileObject hadoopRootFile = null;
  String hadoopOpenFromFolder = null;

  FileObject rootFile = null;
  FileObject initialFile = null;
  VfsFileChooserDialog vfsFileChooserDialog = null;
  
  public HadoopVfsFileChooserDialog(VfsFileChooserDialog vfsFileChooserDialog, FileObject rootFile, FileObject initialFile) {
    super(HadoopSpoonPlugin.HDFS_SCHEME, HadoopSpoonPlugin.HDFS_SCHEME, vfsFileChooserDialog, SWT.NONE);
    this.rootFile = rootFile;
    this.initialFile = initialFile;
    this.vfsFileChooserDialog = vfsFileChooserDialog;
    // Create the Hadoop panel
    GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    setLayoutData(gridData);
    setLayout(new GridLayout(1, false));

    createConnectionPanel();
    initializeConnectionPanel();
  }

  private void createConnectionPanel() {
    // The Connection group
    Group connectionGroup = new Group(this, SWT.SHADOW_ETCHED_IN);
    connectionGroup.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.ConnectionGroup.Label")); //$NON-NLS-1$;
    GridLayout connectionGroupLayout = new GridLayout();
    connectionGroupLayout.marginWidth = 5;
    connectionGroupLayout.marginHeight = 5;
    connectionGroupLayout.verticalSpacing = 5;
    connectionGroupLayout.horizontalSpacing = 5;
    GridData gData = new GridData(SWT.FILL, SWT.FILL, true, false);
    connectionGroup.setLayoutData(gData);
    connectionGroup.setLayout(connectionGroupLayout);

    // The composite we need in the group
    Composite textFieldPanel = new Composite(connectionGroup, SWT.NONE);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    textFieldPanel.setLayoutData(gridData);
    textFieldPanel.setLayout(new GridLayout(5, false));

    // URL label and text field
    wlUrl = new Label(textFieldPanel, SWT.RIGHT);
    wlUrl.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.URL.Label")); //$NON-NLS-1$
    fdlUrl = new GridData();
    fdlUrl.widthHint = 75;
    wlUrl.setLayoutData(fdlUrl);
    wUrl = new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    fdUrl = new GridData();
    fdUrl.widthHint = 150;
    wUrl.setLayoutData(fdUrl);
    wUrl.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent arg0) {
        handleConnectionButton();
      }
    });

    // UserID label and field
    wlUserID = new Label(textFieldPanel, SWT.RIGHT);
    wlUserID.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.UserID.Label")); //$NON-NLS-1$
    fdlUserID = new GridData();
    fdlUserID.widthHint = 75;
    wlUserID.setLayoutData(fdlUserID);

    wUserID = new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    fdUserID = new GridData();
    fdUserID.widthHint = 150;
    wUserID.setLayoutData(fdUserID);

    // Place holder
    wPlaceHolderLabel = new Label(textFieldPanel, SWT.RIGHT);
    wPlaceHolderLabel.setText("");
    fdlPlaceHolderLabel = new GridData();
    fdlPlaceHolderLabel.widthHint = 75;
    wlUserID.setLayoutData(fdlPlaceHolderLabel);

    // Port label and text field
    wlPort = new Label(textFieldPanel, SWT.RIGHT);
    wlPort.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.Port.Label")); //$NON-NLS-1$
    fdlPort = new GridData();
    fdlPort.widthHint = 75;
    wlPort.setLayoutData(fdlPort);

    wPort = new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    fdPort = new GridData();
    fdPort.widthHint = 150;
    wPort.setLayoutData(fdPort);
    wPort.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent arg0) {
        handleConnectionButton();
      }
    });


    // password label and field
    wlPassword = new Label(textFieldPanel, SWT.RIGHT);
    wlPassword.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.Password.Label")); //$NON-NLS-1$
    fdlPassword = new GridData();
    fdlPassword.widthHint = 75;
    wlPassword.setLayoutData(fdlPassword);

    wPassword = new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wPassword.setEchoChar('*');
    fdPassword = new GridData();
    fdPassword.widthHint = 150;
    wPassword.setLayoutData(fdPassword);

    // Connection button
    wConnectionButton = new Button(textFieldPanel, SWT.CENTER);
    fdConnectionButton = new GridData();
    fdConnectionButton.widthHint = 75;
    wConnectionButton.setLayoutData(fdConnectionButton);

    wConnectionButton.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.ConnectionButton.Label"));
    wConnectionButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        try {
          Socket testHdfsSocket = new Socket(wUrl.getText(), Integer.parseInt(wPort.getText()));
          testHdfsSocket.getOutputStream();
          testHdfsSocket.close();
        } catch (Throwable t) {
          showMessageAndLog("HadoopVfsFileChooserDialog.error", "HadoopVfsFileChooserDialog.Connection.error", t.getMessage());
          return;
        }
        vfsFileChooserDialog.openFileCombo.setText(buildHadoopFileSystemUrlString());
        vfsFileChooserDialog.resolveVfsBrowser();
      }
    });

    // set the tab order
    textFieldPanel.setTabList(new Control[] { wUrl, wPort, wUserID, wPassword, wConnectionButton });
  }

  /**
   * Build a URL given Url and Port provided by the user.
   * 
   * @return
   * @TODO: relocate to a Hadoop helper class or similar
   */
  public String buildHadoopFileSystemUrlString() {
    if (wUserID.getText() == null || "".equals(wUserID.getText())) {
      String urlString = "hdfs://" + wUrl.getText() + ":" + wPort.getText();
      return urlString;
    } else {
      String urlString = "hdfs://" + wUserID.getText() + ":" + wPassword.getText() + "@" + wUrl.getText() + ":" + wPort.getText();
      return urlString;
    }
  }

  private void initializeConnectionPanel() {
    if (initialFile != null && initialFile instanceof HDFSFileObject) {
      // populate the server and port fields
      try {
        GenericFileName genericFileName = (GenericFileName) initialFile.getFileSystem().getRoot().getName();
        wUrl.setText(genericFileName.getHostName());
        wPort.setText(String.valueOf(genericFileName.getPort()));
        wUserID.setText(genericFileName.getUserName() == null ? "" : genericFileName.getUserName()); //$NON-NLS-1$
        wPassword.setText(genericFileName.getPassword() == null ? "" : genericFileName.getPassword()); //$NON-NLS-1$
      } catch (FileSystemException fse) {
        showMessageAndLog("HadoopVfsFileChooserDialog.error", "HadoopVfsFileChooserDialog.FileSystem.error", fse.getMessage());
      }
    }

    handleConnectionButton();
  }

  private void showMessageAndLog(String title, String message, String messageToLog) {
    MessageBox box = new MessageBox(this.getShell());
    box.setText(BaseMessages.getString(PKG, title)); //$NON-NLS-1$
    box.setMessage(BaseMessages.getString(PKG, message));
    log.logError(messageToLog);
    box.open();
  }

  private void handleConnectionButton() {
    if (!Const.isEmpty(wUrl.getText()) && !Const.isEmpty(wPort.getText())) {
      wConnectionButton.setEnabled(true);
    } else {
      wConnectionButton.setEnabled(false);
    }
  }
}
