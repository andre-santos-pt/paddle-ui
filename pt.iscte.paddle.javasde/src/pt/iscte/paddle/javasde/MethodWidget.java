package pt.iscte.paddle.javasde;

import static java.lang.System.lineSeparator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import pt.iscte.paddle.javasde.Constants.DeleteListener;
import pt.iscte.paddle.model.IProcedure;

public class MethodWidget extends EditorWidget implements SequenceContainer {

	final IProcedure procedure;
	private Id retType;
	private EditorWidget id;
	private SequenceWidget body;
	private ParamList params;
	private EditorWidget header;
	private List<Keyword> modifiers;
	
	MethodWidget(SequenceWidget parent, IProcedure procedure) {
		super(parent);
		this.procedure = procedure;

		setLayout(Constants.ROW_LAYOUT_V_ZERO);

		header = new EditorWidget(this);
		header.setLayout(Constants.ROW_LAYOUT_H);

		modifiers = new ArrayList<>();
		for (Keyword mod : Keyword.methodModifiers())
			if(procedure.is(mod.toString()))
				addModifier(mod);
			

		DeleteListener deleteListener = new Constants.DeleteListener(this);
		retType = new Id(header, procedure.getReturnType().toString(), true, Constants.PRIMITIVE_TYPES_VOID_SUPPLIER);
		retType.addKeyListener(deleteListener);
		retType.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.SPACE) {
					addModifier(Keyword.PUBLIC);
					e.doit = false;
				}
			}
		});
		retType.setToolTip("Return type");

		String name = procedure.getId();
		if(name == null)
			name = "procedure";
		id = new Id(header, name, false);
		id.addKeyListener(deleteListener);
		new FixedToken(header, "(");
		params = new ParamList(header);
		new FixedToken(header, ")");
		new FixedToken(header, "{");
		body = new SequenceWidget(this, Constants.TAB);
		body.addBlockListener(procedure.getBody());
		body.addActions(BlockAction.all(procedure.getBody()));
		body.setDeleteAction(i -> procedure.getBody().removeElement(i));
		
		new FixedToken(this, "}");
		new Label(this, SWT.NONE); // line
	}

	void addModifier(Keyword mod) {
		modifiers.add(mod);
		Token modifier = new Token(header, mod);
		modifier.addKeyListener(new KeyAdapter() { 
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == Constants.DEL_KEY) {
					procedure.setProperty(mod.toString(), null);
					modifier.dispose();
					requestLayout();
					modifiers.remove(mod);
				}
			}
		});
		modifier.moveAbove(retType);
		modifier.setFocus();
		requestLayout();
	}



	private class ParamList extends EditorWidget {
		private InsertWidget insertWidget;
		private GridLayout layout;

		public ParamList(Composite parent) {
			super(parent);
			layout = new GridLayout(1, false);
			layout.marginTop = 0;
			layout.verticalSpacing = 0;
			layout.marginHeight = 0;
			setLayout(layout);
			createInsert();
		}

		private void createInsert() {
			insertWidget = new InsertWidget(this, true);
			insertWidget.addAction(new InsertWidget.Action("parameter",'0') {
				public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
					return Constants.isType(text) && (c == ' ' || c == '[');
				}

				public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
					addParam(insertWidget.text, text, c == '[', false, false);
				}
			});
			//			insertWidget.addFocusListener(Constants.FOCUS_SELECTALL);
		}

		@Override
		public boolean setFocus() {
			return insertWidget.setFocus();
		}

		public void toCode(StringBuffer buffer) {
			boolean first = true;
			for(Control c : getChildren())
				if(c instanceof Param) {
					if(first)
						first = false;
					else
						buffer.append(", ");
					((Param) c).toCode(buffer);
				}
		}
		
		private class Param extends EditorWidget {
			private final Id type;
			private final Id var;
			private FixedToken comma;

			public Param(String type, boolean array, boolean comma) {
				super(ParamList.this);
				setLayout(Constants.ROW_LAYOUT_H_DOT);
				if(comma)
					this.comma = new FixedToken(this, ",");
				this.type = new Id(this, type, true, Constants.PRIMITIVE_TYPES_SUPPLIER);
				if(array)
					this.type.addDimension();
				this.type.setToolTip("Parameter type");
				var = new Id(this, "parameter", false);
				var.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if(e.character == ',')
							addParam(Param.this, Keyword.INT.toString(), false, false, true);
						else if(e.keyCode == Constants.DEL_KEY && var.isAtBeginning()) {
							dispose();
							Control[] children = ParamList.this.getChildren();
							if(children.length == 0) {
								createInsert();
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

			public void toCode(StringBuffer buffer) {
				type.toCode(buffer);
				buffer.append(' ');
				var.toCode(buffer);
			}
			
			@Override
			public boolean setFocus() {
				type.setFocus();
				return true;
			}
		}

		private void addParam(Control control, String type, boolean array, boolean above, boolean focusType) {
			if(insertWidget != null) {
				insertWidget.dispose();
				insertWidget = null;
			}
			boolean comma = ParamList.this.getChildren().length != 0;
			Param param = new Param(type, array, comma);
			layout.numColumns = getChildren().length;
			param.requestLayout();

			if(focusType)
				param.setFocus();
			else
				param.focusVariable();
		}
	}


	public void toCode(StringBuffer buffer) {
		for(Keyword k : modifiers)
			buffer.append(k.toString()).append(' ');
		
		buffer.append(retType).append(' ').append(id.toString()).append("(");
		params.toCode(buffer);
		buffer.append(") {").append(lineSeparator());
		body.toCode(buffer, 2);
		buffer.append("\t}").append(lineSeparator());

	}

	public SequenceWidget getBody() {
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

	void focusParameters() {
		params.setFocus();
	}

}
