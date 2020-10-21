package pt.iscte.paddle.javardise;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.javardise.api.ICodeElement;

public class Id extends EditorWidget implements TextWidget {

	private boolean readOnly;
	private final Text text;
	private Runnable editAction = () -> {};
	private Supplier<Boolean> allowEmpty = () -> false;
	
	public Id(Composite parent, String id) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		setBackground(Constants.COLOR_BACKGROUND);
		readOnly = false;
		text = Constants.createText(this, id);
		if(id.isBlank() && !allowEmpty.get())
			text.setBackground(Constants.COLOR_ERROR);
		text.addVerifyListener(e -> e.doit =
				!readOnly && (ILanguageConfiguration.INSTANCE.isValidIdCharacter(e.character) || e.character == Constants.DEL_KEY || e.character == SWT.CR));	
	
		text.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				text.setBackground(Constants.COLOR_BACKGROUND);
				text.selectAll();
			}
			
			public void focusLost(FocusEvent e) {
				setBackgroundError();
				editAction.run();
			}
		});

		text.addModifyListener(Constants.MODIFY_PACK);
		text.setMenu(new Menu(text)); // prevent system menu
		Constants.addArrowKeys(text, this);
		setBackgroundError();
	}
	
	private void setBackgroundError() {
		if(text.getText().isBlank() && !allowEmpty.get())
			text.setBackground(Constants.COLOR_ERROR);
		else
			text.setBackground(Constants.COLOR_BACKGROUND);
	}	
	
	public void setAllowEmpty(Supplier<Boolean> allowEmpty) {
		this.allowEmpty = allowEmpty;
		setBackgroundError();
	}
	
	public void setReadOnly() {
//		text.setEditable(false);
		readOnly = true;
	}
	
	public void setEditAction(Runnable editAction) {
		this.editAction = editAction == null ? () -> {} : editAction;
	}
	
	@Override
	public Text getWidget() {
		return text;
	}

	@Override
	public boolean setFocus() {
		text.setFocus();
		return true;
	}

	public void setMenu(Menu menu) {
		text.setMenu(menu);
	}
	
	@Override
	public void toCode(StringBuffer buffer) {
		ICodeElement.toCode(text, buffer);
	}

	public void set(String id) {
		text.setText(id);
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		text.addKeyListener(listener);
	}
}
