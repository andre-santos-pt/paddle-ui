package pt.iscte.paddle.javaeditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.model.ISelection;

class IfElseWidget extends ControlWidget {

	private ControlWidget elseBlock;

	public IfElseWidget(Composite parent, ISelection selection) {
		super(parent, Keyword.IF, selection);
		if(selection.hasAlternativeBlock())
			createElseBlock(selection);
		
		selection.addPropertyListener((k,n,o) -> {
			if(Flag.ELSE.is(k.toString())) {
				if(n == Boolean.TRUE) {
					createElseBlock(selection);
					elseBlock.focusIn();
				}
				else {
					elseBlock.dispose();
					elseBlock = null;
					setFocus();
				}
				requestLayout();
			}
		});
	}


	private void createElseBlock(ISelection selection) {
		elseBlock = new ControlWidget(this, Keyword.ELSE, selection.getAlternativeBlock());
		elseBlock.keyword.getWidget().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == Constants.DEL_KEY)
					selection.deleteAlternativeBlock();
				else if(e.keyCode == SWT.ARROW_UP)
					blockSeq.focusLast();
				else if(e.keyCode == SWT.ARROW_DOWN)
					elseBlock.focusIn();
			}
		});
	}
	
	
	@Override
	public void toCode(StringBuffer buffer, int level) {
		super.toCode(buffer, level);
		if(elseBlock != null)
			elseBlock.toCode(buffer, level);
	}

}
