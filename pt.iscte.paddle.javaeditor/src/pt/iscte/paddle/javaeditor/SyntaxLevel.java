package pt.iscte.paddle.javaeditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import pt.iscte.paddle.javaeditor.UiMode.Syntax;

enum SyntaxLevel {
		RECURSIVE("Recursion", true, Syntax.SELECTION, Syntax.CALLS),
		LOOPS("Loops", true, Syntax.WHILE_LOOP, Syntax.ASSIGNMENT),
		ARRAYS("Arrays", true, Syntax.ARRAYS),
		OBJECTS("Objects", false, Syntax.RECORDS),
		ENCAPSULATION("Encapsulation", false, Syntax.ENCAPSULATION);

		String uiText;
		boolean selected;
		Syntax[] elements;
		
		SyntaxLevel(String uiText, boolean selected, Syntax ... elements) {
			this.uiText = uiText;
			this.selected = selected;
			this.elements = elements;
			if(selected)
				UiMode.addSyntax(elements);
			else
				UiMode.removeSyntax(elements);
		}

		void createMenuItem(Menu parent) {
			MenuItem item = new MenuItem(parent, SWT.CHECK);
			item.setText(uiText);
			item.setSelection(selected);
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(item.getSelection())
						UiMode.addSyntax(elements);
					else
						UiMode.removeSyntax(elements);
				}
			});
		}
	}