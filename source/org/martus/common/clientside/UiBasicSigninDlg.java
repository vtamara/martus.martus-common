/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/
package org.martus.common.clientside;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.martus.swing.Utilities;

public class UiBasicSigninDlg extends JDialog
{
	public UiBasicSigninDlg(UiBasicLocalization localizationToUse, CurrentUiState uiStateToUse, JFrame owner, int mode, String username, char[] password)
	{
		super(owner, true);
		initalize(localizationToUse, uiStateToUse, owner, mode, username, password);
	}

	public void initalize(UiBasicLocalization localizationToUse, CurrentUiState uiStateToUse, JFrame owner, int mode, String username, char[] password)
	{
		currentMode = mode;
		localization = localizationToUse;
		uiState = uiStateToUse;
		usersChoice = CANCEL;
		setTitle(getTextForTitle(localization, currentMode));
		
		signinPane = new UiSigninPanel(this, currentMode, username, password);
		
		ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(new OkHandler());
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());
		Box buttonBox = Box.createHorizontalBox();
		JComponent languageComponent = getLanguageComponent();
		buttonBox.add(languageComponent);
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(ok);
		buttonBox.add(cancel);
		buttonBox.add(Box.createHorizontalGlue());
		
		buttonBox.setBorder(new EmptyBorder(5,5,5,5));
		JPanel scrolledPanel = createMainPanel();
	
		Container scrollingPane = new JScrollPane(scrolledPanel);
		getContentPane().add(scrollingPane);
		getContentPane().add(buttonBox, BorderLayout.SOUTH);
		
		getRootPane().setDefaultButton(ok);
		signinPane.refreshForNewVirtualMode();
		setResizable(true);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if(screenSize.width < 1000)
		{	
			setSize(screenSize.width, screenSize.height * 8 / 10);
			setLocation(0,screenSize.height/10);
		}
		else
		{	
			Utilities.centerDlg(this);
		}
		show();
	}
	
	protected JComponent getLanguageComponent()
	{
		return new JLabel();
	}

	protected JPanel createMainPanel()
	{
		JPanel scrolledPanel = new JPanel(); 
		scrolledPanel.add(signinPane);
		return scrolledPanel;
	}

	public String getTextForTitle(UiBasicLocalization localization, int mode)
	{		
		switch (mode)
		{
			case SECURITY_VALIDATE:
				return localization.getWindowTitle("MartusSignInValidate"); 
		
			case RETYPE_USERNAME_PASSWORD:
				return localization.getWindowTitle("MartusSignInRetypePassword"); 
			
			default:
				return getInitialSigninTitle(localization); 
		}			
	}

	public static String getInitialSigninTitle(UiBasicLocalization localization)
	{
		return localization.getWindowTitle("MartusSignIn");
	}

	public int getUserChoice()
	{
		return usersChoice;
	}

	public String getName()
	{
		return signinPane.getName();
	}

	public char[] getPassword()
	{
		return signinPane.getPassword();
	}

	public void sizeHasChanged()
	{
		Utilities.centerDlg(this);
	}

	public void virtualPasswordHasChanged()
	{
		getRootPane().setDefaultButton(ok);
	}

	public void handleOk()
	{
		usersChoice = SIGN_IN;
		dispose();
	}

	public UiBasicLocalization getLocalization()
	{
		return localization;
	}

	public CurrentUiState getCurrentUiState()
	{
		return uiState;
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			handleOk();
		}
	}
	
	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			usersChoice = CANCEL;
			dispose();
		}
	}
	
	public UiSigninPanel signinPane;
	public UiBasicLocalization localization;
	public CurrentUiState uiState;
	public int usersChoice;
	boolean languageChanged;
	private JButton ok;
	protected int currentMode;
	public static final int INITIAL = 1;
	public static final int TIMED_OUT = 2;
	public static final int SECURITY_VALIDATE = 3;
	public static final int RETYPE_USERNAME_PASSWORD = 4;
	public static final int CREATE_NEW = 5;
	public static final int INITIAL_NEW_RECOVER_ACCOUNT = 6;
	public static final int CANCEL = 10;
	public static final int SIGN_IN = 11;
	public static final int NEW_ACCOUNT = 12;
	public static final int RECOVER_ACCOUNT_BY_SHARE = 13;
	public static final int RECOVER_ACCOUNT_BY_BACKUP_FILE = 14;
	public static final int LANGUAGE_CHANGED = 15;

}