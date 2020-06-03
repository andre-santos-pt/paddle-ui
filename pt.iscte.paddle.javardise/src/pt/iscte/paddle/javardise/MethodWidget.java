package pt.iscte.paddle.javardise;

import static java.lang.System.lineSeparator;

import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import pt.iscte.paddle.javardise.service.IDeclarationWidget;
import pt.iscte.paddle.javardise.service.IMethodWidget;
import pt.iscte.paddle.javardise.service.IWidget;
import pt.iscte.paddle.model.IBlockElement;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariableDeclaration;

public class MethodWidget extends ModiferWidget implements SequenceContainer, IMethodWidget {

	private final IProcedure procedure;
	private ComplexId retType;
	private Id id;
	private SequenceWidget body;
	private ParamList params;
	private Composite header;
	private final boolean isConstructor;

	@Override
	Composite getHeader() {
		return header;
	}
	
	@Override
	Function<List<Keyword>, List<Keyword>> getModifierProvider() {
		return list -> isConstructor ? Keyword.constructorModifiers() : Keyword.methodModifiers();
	}
	
	public MethodWidget(Composite parent, IProcedure procedure) {
		super(parent, procedure);
		this.procedure = procedure;
		isConstructor = Flag.CONSTRUCTOR.is(procedure);
//		RowLayout layout = new RowLayout(SWT.VERTICAL);
//		layout.marginTop = Constants.METHOD_SPACING;
//		setLayout(layout);
		 setLayout(Constants.ROW_LAYOUT_V_ZERO);
		header = Constants.createHeader(this);
		for (Keyword mod : Keyword.methodModifiers())
			if (procedure.is(mod.toString()))
				addModifier(mod);


		Runnable delAction = () -> procedure.getModule().removeProcedure(procedure);
		if (!isConstructor) {
			if(!Flag.INSTANCE.is(procedure)) {
				Token classToken = new Token(header, Keyword.STATIC);
			}
			retType = ComplexId.matchType(header, procedure.getReturnType());
			retType.addDeleteListener(delAction);
			addModifierKey(retType);
			Constants.addInsertLine(retType);
		}

		String name = procedure.getId();
		if (name == null)
			name = "procedure";
		if (name.equals(Constants.EMPTY_EXPRESSION_SERIALIZE))
			name = "";

		id = new Id(header, name);
		if (isConstructor) {
			id.setReadOnly();
			id.addDeleteListener(delAction);
			addModifierKey(id);
			Constants.addInsertLine(id);
		}
		
		new FixedToken(header, "(");
		params = new ParamList(header);
		new FixedToken(header, ")");

		procedure.getParameters().forEach(p -> {
			if(!Flag.INSTANCE.is(p))
				params.addParam(p, false);
		});
		if (procedure.getParameters().isEmpty())
			params.addEmptyParam();

		new FixedToken(header, "{");
		body = new SequenceWidget(this, Constants.TAB);
		body.addBlockListener(procedure.getBody());
		body.addActions(BlockAction.all(procedure.getBody()));
		new FixedToken(this, "}");

		int i = 0;
		for (IBlockElement e : procedure.getBody())
			body.addModelElement(e, i++);
	}

	public IProcedure getProcedure() {
		return procedure;
	}

	private class ParamList extends Composite {
		public ParamList(Composite parent) {
			super(parent, SWT.NONE);
			setLayout(Constants.ROW_LAYOUT_H_ZERO);
			setBackground(Constants.COLOR_BACKGROUND);
		}

		public void toCode(StringBuffer buffer) {
			Control[] children = getChildren();
			if (children.length == 1 && ((Param) children[0]).isEmpty())
				return;

			for (Control c : children)
				if (c instanceof Param)
					((Param) c).toCode(buffer);
				else
					buffer.append(", ");
		}

		private class Param extends EditorWidget implements IDeclarationWidget {
			private ComplexId type;
			private Id id;
			private FixedToken comma;

			Param(IVariableDeclaration v, boolean comma) {
				super(ParamList.this, v);
				init(v.getType(), Constants.variableId(v), comma);
			}

			Param(String type, String id, boolean comma) {
				super(ParamList.this);
				init(null, id, comma);
			}

			private void init(IType type, String id, boolean comma) {
				setLayout(Constants.ROW_LAYOUT_H);
				if (comma)
					this.comma = new FixedToken(this, ",");
				
				this.type = type == null ? 
						new ComplexId(this, "", true) :
							ComplexId.matchType(this, type);
				this.type.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if(e.character == SWT.SPACE && Param.this.type.isAtEnd())
							Param.this.id.setFocus();
					}
				});
				this.id = new Id(this, id);

				if (type == null)
					this.type.setAllowEmpty(() -> this.id.isEmpty());

				if (id.isEmpty()) {
					// this.id.getControl().setLayoutData(new RowData(5, SWT.DEFAULT));
					this.id.setAllowEmpty(() -> this.type.isEmpty());
				}

				this.id.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if (e.character == ',')
							addParam(new IVariableDeclaration.UnboundVariable(IType.INT, "parameter"), true);
//						else if (e.keyCode == Constants.DEL_KEY && Param.this.id.isAtBeginning()) {
//							dispose();
//							Control[] children = ParamList.this.getChildren();
//							// if(children.length == 0) {
//							// createInsert();
//							// }
//							// else {
//							if (children.length == 1 && ((Param) children[0]).comma != null)
//								((Param) children[0]).comma.dispose();
//
//							// FIXME
//							((Param) children[children.length - 1]).focusVariable();
//							// }
//							ParamList.this.requestLayout();
//						}
					}
				});
			}

			public boolean isEmpty() {
				return type.isEmpty() && id.isEmpty();
			}

			void focusVariable() {
				id.setFocus();
			}

			public void toCode(StringBuffer buffer) {
				if(comma != null)
					buffer.append(", ");
				type.toCode(buffer);
				buffer.append(' ');
				id.toCode(buffer);
			}

			@Override
			public boolean setFocus() {
				type.setFocus();
				return true;
			}

			@Override
			public IWidget getVariableType() {
				return type;
			}

			@Override
			public IWidget getVariableName() {
				return id;
			}
		}

		private void addEmptyParam() {
			new Param("", "", false);
		}

		private void addParam(IVariableDeclaration var, boolean focusType) {
			boolean comma = ParamList.this.getChildren().length != 0;
			Param param = new Param(var, comma);
			param.requestLayout();
			if (focusType)
				param.setFocus();
			else
				param.focusVariable();
		}
	}

	public void toCode(StringBuffer buffer) {
		buffer.append(lineSeparator()).append('\t');

		super.toCode(buffer);
		
		if (retType != null) {
			retType.toCode(buffer);
			buffer.append(' ');
		}
		buffer.append(id.toString()).append("(");
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

	@Override
	public IWidget getReturnType() {
		return retType;
	}

	@Override
	public IWidget getMethodName() {
		return id;
	}
}
