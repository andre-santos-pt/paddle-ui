package pt.iscte.paddle.javaeditor;

import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javaeditor.api.IDeclarationWidget;
import pt.iscte.paddle.javaeditor.api.IMethodWidget;
import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.FixedToken;
import pt.iscte.paddle.javardise.Id;
import pt.iscte.paddle.javardise.ModiferWidget;
import pt.iscte.paddle.javardise.SequenceContainer;
import pt.iscte.paddle.javardise.SequenceWidget;
import pt.iscte.paddle.javardise.api.IWidget;
import pt.iscte.paddle.model.IBlockElement;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariableDeclaration;

public class MethodWidget extends ModiferWidget<IProgramElement> implements SequenceContainer, IMethodWidget {
	private ExpressionChain retType;
	private Id id;
	private SequenceWidget body;
	private ParamList params;
	private Composite header;
	private final boolean isConstructor;

	@Override
	protected Composite getHeader() {
		return header;
	}
	
	@Override
	public Function<List<String>, List<String>> getModifierProvider() {
		return list -> isConstructor ? Keyword.constructorModifiers() : Keyword.methodModifiers();
	}
	
	public MethodWidget(Composite parent, IProcedure procedure) {
		super(parent, procedure);
//		this.procedure = procedure;
		isConstructor = Flag.CONSTRUCTOR.is(procedure);
//		RowLayout layout = new RowLayout(SWT.VERTICAL);
//		layout.marginTop = Constants.METHOD_SPACING;
//		setLayout(layout);
		 setLayout(Constants.ROW_LAYOUT_V_ZERO);
		header = Constants.createHeader(this);
		for (String mod : Keyword.methodModifiers())
			if (procedure.is(mod))
				addModifier(mod);

		if(!isConstructor && !Flag.INSTANCE.is(procedure))
			addModifier(Keyword.STATIC.keyword());
		
		Runnable delAction = () -> procedure.getModule().removeProcedure(procedure);
		if (!isConstructor) {
			retType = ExpressionChain.matchType(header, procedure.getReturnType());
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
		body = new SequenceWidget(this, 1);
		BlockAction.addBlockListener(procedure.getBody(), body);
		body.addActions(BlockAction.all(procedure.getBody()));
		new FixedToken(this, "}");

		int i = 0;
		for (IBlockElement e : procedure.getBody())
			BlockAction.addModelElement(e, i++, body);
	}


	private class ParamList extends Composite {
		public ParamList(Composite parent) {
			super(parent, SWT.NONE);
			setLayout(Constants.ROW_LAYOUT_H_ZERO);
			setBackground(Constants.COLOR_BACKGROUND);
		}

		private class Param extends EditorWidget implements IDeclarationWidget {
			private ExpressionChain type;
			private Id id;
			private FixedToken comma;

			Param(IVariableDeclaration v, boolean comma) {
				super(ParamList.this, v);
				init(v.getType(), BlockAction.variableId(v), comma);
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
						new ExpressionChain(this, "", true) :
							ExpressionChain.matchType(this, type);
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

	public void focusParameters() {
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