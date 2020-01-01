package pt.iscte.paddle.javasde;

import static java.lang.System.lineSeparator;
import static pt.iscte.paddle.javasde.Keyword.BREAK;
import static pt.iscte.paddle.javasde.Keyword.CONTINUE;
import static pt.iscte.paddle.javasde.Keyword.ELSE;
import static pt.iscte.paddle.javasde.Keyword.IF;
import static pt.iscte.paddle.javasde.Keyword.RETURN;
import static pt.iscte.paddle.javasde.Keyword.WHILE;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import pt.iscte.paddle.model.IArrayElementAssignment;
import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.IBreak;
import pt.iscte.paddle.model.IContinue;
import pt.iscte.paddle.model.ILoop;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IProcedureCall;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IReturn;
import pt.iscte.paddle.model.ISelection;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariable;
import pt.iscte.paddle.model.IVariableAssignment;

public class MethodWidget extends EditorWidget implements StatementContainer {

	final IProcedure procedure;
	private Id retType;
	private EditorWidget id;
	private SequenceWidget body;
	private ParamList params;

	MethodWidget(SequenceWidget parent, IProcedure procedure) {
		super(parent);
		this.procedure = procedure;

		setLayout(Constants.ROW_LAYOUT_V_ZERO);

		EditorWidget header = new EditorWidget(this);
		header.setLayout(Constants.ROW_LAYOUT_H);

		for (Keyword mod : Keyword.methodModifiers())
			if(procedure.is(mod.toString()))
				new Token(header, mod);
		
		retType = new Id(header, procedure.getReturnType().toString(), true, Constants.PRIMITIVE_TYPES_VOID_SUPPLIER);
		retType.setToolTip("Return type");

		String name = procedure.getId();
		if(name == null)
			name = "procedure";
		id = new Id(header, name, false);
		new FixedToken(header, "(");
		params = new ParamList(header);
		new FixedToken(header, ")");
		new FixedToken(header, "{");
		body = new SequenceWidget(this, Constants.TAB);
		body.addBlockListener(procedure.getBody());
		body.addActions(BlockAction.all(procedure.getBody()));
		new FixedToken(this, "}");
		new Label(this, SWT.NONE); // line
	}

	
	
	private class ParamList extends EditorWidget {
		private InsertWidget insertWidget;

		public ParamList(Composite parent) {
			super(parent);
			setLayout(Constants.ROW_LAYOUT_H_ZERO);
			createInsert2();
		}
		
		private void createInsert2() {
			insertWidget = new InsertWidget(this);
			insertWidget.addAction(new InsertWidget.Action("parameter",'0') {
				public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
					return Constants.isType(text) && c == ' ';
				}
				
				public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
					addParam(insertWidget.text, text, false, false);
				}
			});
			insertWidget.addFocusListener(Constants.FOCUS_SELECTALL);
		}

		private class Param extends EditorWidget {
			private final Id type;
			private final Id var;
			private FixedToken comma;

			public Param(String type, boolean comma) {
				super(ParamList.this);
				setLayout(Constants.ROW_LAYOUT_H_DOT);
				if(comma)
					this.comma = new FixedToken(this, ",");
				this.type = new Id(this, type, true, Constants.PRIMITIVE_TYPES_SUPPLIER);
				this.type.setToolTip("Parameter type");
				var = new Id(this, "parameter", false);
				var.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if(e.character == ',')
							addParam(Param.this, Keyword.INT.toString(), false, true);
						else if(e.keyCode == Constants.DEL_KEY && var.isAtBeginning()) {
							dispose();
							Control[] children = ParamList.this.getChildren();
							if(children.length == 0) {
								createInsert2();
							}
							else {
								if(children.length == 1 && ((Param) children[0]).comma != null)
									((Param) children[0]).comma.dispose();

								((Param) children[children.length-1]).focusVariable();
							}
							ParamList.this.requestLayout();
						}
					}
				});
				var.setToolTip("Parameter name");
			}

			void focusVariable() {
				var.setFocus();
			}

			@Override
			public boolean setFocus() {
				type.setFocus();
				return true;
			}
		}

		private void addParam(Control control, String type, boolean above, boolean focusType) {
			if(insertWidget != null) {
				insertWidget.dispose();
				insertWidget = null;
			}
			boolean comma = ParamList.this.getChildren().length != 0;
			Param param = new Param(type, comma);
			param.requestLayout();
			if(focusType)
				param.setFocus();
			else
				param.focusVariable();
		}
	}


	public void toCode(StringBuffer buffer) {
		buffer.append("\tpublic static ").append(retType).append(" ").append(id.toString())
		.append("(...)") // TODO parameters to code
		.append(" {").append(lineSeparator());
		body.toCode(buffer, 2);
		buffer.append("\t}").append(lineSeparator());

	}

	public SequenceWidget getSequence() {
		return body;
	}

	@Override
	public boolean setFocus() {
		id.setFocus();
		return true;
	}

	void focusReturnType() {
		retType.setFocus();
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
		super.accept(visitor);
	}
}
