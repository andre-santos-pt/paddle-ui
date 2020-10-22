package pt.iscte.paddle.javardise;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

public abstract class ModiferWidget<T> extends EditorWidget {
	private List<TokenWidget> modifiers;

	public ModiferWidget(Composite parent, Object e) {
		super(parent, e);
		modifiers = new ArrayList<>();
	}

	protected abstract Composite getHeader();
	
	protected abstract Function<List<String>, List<String>> getModifierProvider();
	
	protected void addModifierKey(TextWidget control) {
		control.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.SPACE && (!control.isModifiable() || control.isAtBeginning())) {
					String k = inexistentKeyword();
					if (k != null) {
						addModifier(k);
						e.doit = false;
					}
				}
			}

			private String inexistentKeyword() {
				for (String k : getModifierProvider().apply(getModifiers()))
					if (!modifiers.stream().anyMatch(t -> t.getText().equals(k)))
						return k;
				return null;
			}
		});
	}
	
	public void addModifier(String mod) {
		TokenWidget modifier = new TokenWidget(getHeader(), mod, getModifierProvider().apply(getModifiers()));
		modifier.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == Constants.DEL_KEY) {
					// TODO property update
//					procedure.setProperty(mod.toString(), null);
					modifier.dispose();
					getHeader().requestLayout();
					int i = modifiers.indexOf(modifier);
					modifiers.remove(modifier);
					if (i < modifiers.size())
						modifiers.get(i).setFocus();
					else
						getHeader().setFocus();

				}
			}
		});
		if(modifiers.isEmpty())
			modifier.moveAbove(getHeader().getChildren()[0]);
		else
			modifier.moveBelow(modifiers.get(modifiers.size()-1).getWidget());
		modifier.getWidget().requestLayout();
		modifier.setFocus();
		modifiers.add(modifier);
	}
	
	List<String> getModifiers() {
		return modifiers.stream()
				.map(t -> t.getText())
				.collect(Collectors.toList());
	}
}
