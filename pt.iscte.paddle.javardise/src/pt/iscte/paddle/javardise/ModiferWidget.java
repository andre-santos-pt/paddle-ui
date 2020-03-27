package pt.iscte.paddle.javardise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.model.IProgramElement;

abstract class ModiferWidget extends EditorWidget {
	private List<Token> modifiers;

	public ModiferWidget(Composite parent, IProgramElement e) {
		super(parent, e);
		modifiers = new ArrayList<>();
	}

	abstract Composite getHeader();
	
	abstract Function<List<Keyword>, List<Keyword>> getModifierProvider();
	
	void addModifierKey(TextWidget control) {
		control.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.SPACE && (!control.isModifiable() || control.isAtBeginning())) {
					Keyword k = inexistentKeyword();
					if (k != null) {
						addModifier(k);
						e.doit = false;
					}
				}
			}

			private Keyword inexistentKeyword() {
				for (Keyword k : getModifierProvider().apply(getModifiers()))
					if (!modifiers.stream().anyMatch(t -> t.getText().equals(k.keyword())))
						return k;
				return null;
			}
		});
	}
	
	public void addModifier(Keyword mod) {
		Token modifier = new Token(getHeader(), mod, getModifierProvider().apply(getModifiers()));
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
			modifier.moveBelow(modifiers.get(modifiers.size()-1).getControl());
		modifier.getControl().requestLayout();
		modifier.setFocus();
		modifiers.add(modifier);
	}
	
	@Override
	public void toCode(StringBuffer buffer) {
		for (Token k : modifiers)
			buffer.append(k.getText()).append(' ');
	}
	
	List<Keyword> getModifiers() {
		return modifiers.stream()
				.map(t -> Keyword.match(t.getText()))
				.collect(Collectors.toList());
	}
}
