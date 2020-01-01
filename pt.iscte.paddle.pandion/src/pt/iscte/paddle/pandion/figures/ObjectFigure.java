package pt.iscte.paddle.pandion.figures;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import pt.iscte.paddle.pandion.FigureProvider;
import pt.iscte.paddle.pandion.FontManager;
import pt.iscte.paddle.pandion.PrimitiveType;
import pt.iscte.paddle.pandion.RuntimeViewer;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectModel.InvocationResult;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IRuntimeModel;
import pt.iscte.pandionj.extensibility.IRuntimeModel.Event;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.extensibility.ModelObserver;
import pt.iscte.pandionj.extensibility.PandionJConstants;
import pt.iscte.pandionj.extensibility.PandionJUI;

public class ObjectFigure extends PandionJFigure<IObjectModel> {

	private RoundedRectangle fig;
	private StackContainer stack;
	private ObjectContainer objectContainer;
	private List<MethodWidget> methodWidgets;
	private RuntimeViewer runtimeViewer;
	private FigureProvider figureProvider;
	private FieldsContainer fieldsContainer;
	private Figure methodsFig;
	private boolean methodsEnabled;

	public ObjectFigure(IObjectModel model, IFigure extensionFigure) {
		super(model, true);
		assert extensionFigure != null;
		methodsEnabled = false;
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 2;
		layout.marginHeight = 5;
		layout.marginWidth = 5;

		extensionFigure.setOpaque(true);

		fig = new RoundedRectangle();
		fig.setLayoutManager(layout);
		fig.setCornerDimensions(PandionJConstants.OBJECT_CORNER);
		fig.setBorder(new MarginBorder(PandionJConstants.OBJECT_PADDING));
		fig.setBackgroundColor(PandionJConstants.Colors.OBJECT);
		fig.setOpaque(true);
		getLayoutManager().setConstraint(fig, new GridData(SWT.DEFAULT, SWT.BEGINNING, false, false));
		fig.add(extensionFigure);

		runtimeViewer = RuntimeViewer.getInstance();
		figureProvider = runtimeViewer.getFigureProvider();

		IRuntimeModel runtimeModel = model.getRuntimeModel();
		if(!runtimeModel.findReferences(model).isEmpty())
			addShowMethodListener();

		model.getRuntimeModel().registerDisplayObserver(new ModelObserver<IRuntimeModel.Event<IStackFrameModel>>() {

			@Override
			public void update(Event<IStackFrameModel> e) {
				if(e.type == IRuntimeModel.Event.Type.REMOVE_FRAME) {
					stack.removeFrame(e.arg);
				}
				else if(e.type == IRuntimeModel.Event.Type.NEW_FRAME) {
					IStackFrameModel f = e.arg;
					if(f.isInstanceFrameOf(model)) {
						fieldsContainer.showPrivateFields();
						setObjectContainerVisible();
						stack.addFrame(f, RuntimeViewer.getInstance(), objectContainer, false);
					}
				}
				else if(e.type == IRuntimeModel.Event.Type.STEP) {
					IStackFrameModel f = e.arg;
					methodsEnabled = !f.isInstanceFrameOf(model);
					setMethodsEnabled(methodsEnabled);
					fig.setBackgroundColor(PandionJConstants.Colors.OBJECT);
				}
				else if(e.type == IRuntimeModel.Event.Type.EVALUATION) {
					fig.setBackgroundColor(PandionJConstants.Colors.OBJECT);
				}
				else if(e.type == IRuntimeModel.Event.Type.TERMINATION) {
					methodsEnabled = false;
					setMethodsEnabled(false);
					fig.removeMouseListener(mouseListener);
				}
			}
		});

		add(fig);

		fieldsContainer = new FieldsContainer();
		fig.add(fieldsContainer);
//		fig.setToolTip(new Label("double-click to show methods"));
		layout.setConstraint(fieldsContainer, new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		
		stack = new StackContainer();
		for (IStackFrameModel f : runtimeModel.getActiveCallStack()) {
			if(f.isInstanceFrameOf(model)) {
				setObjectContainerVisible();
				fieldsContainer.showPrivateFields();
				stack.addFrame(f, RuntimeViewer.getInstance(), objectContainer, false);
			}
		}
		
		fig.add(stack);
		layout.setConstraint(stack, new GridData(SWT.FILL, SWT.DEFAULT, true, false));
	}

	private void setObjectContainerVisible() {
		if(objectContainer == null) {
			objectContainer = ObjectContainer.create(true);
			add(objectContainer);
		}
	}


	private void setMethodsEnabled(boolean enabled) {
		methodsEnabled = enabled;
		if(methodsFig != null) {
			for(Object c : methodsFig.getChildren()) {
				MethodWidget w = (MethodWidget) c;
				w.setEnabled(enabled);
				w.clear();
			}
			methodsFig.invalidate();
		}
	}

	@Override
	public void setBackgroundColor(Color color) {
		fig.setBackgroundColor(color);
	}

	class FieldsContainer extends Figure {
		Figure visibleFields;
		Figure hiddenFields;
		GridData dim;
		boolean visibilityOpen;

		public FieldsContainer() {
			setLayoutManager(new GridLayout(1, false)); 
			addFields(model, figureProvider);
			visibilityOpen = false;
		}

		void showPrivateFields() {
			if(hiddenFields == null || visibilityOpen)
				return;

			visibilityOpen = true;
			getLayoutManager().setConstraint(hiddenFields, new GridData(SWT.FILL, SWT.DEFAULT, true, true));
			hiddenFields.setToolTip(new Label("hidden fields"));
			for (IVariableModel var : model.getFields()) {
				if(!var.isVisible()) {
					PandionJFigure<?> fieldFig = figureProvider.getFigure(var, false);
					hiddenFields.add(fieldFig);
					hiddenFields.getLayoutManager().setConstraint(fieldFig, new GridData(SWT.RIGHT, SWT.FILL, true, false));
					setObjectContainerVisible();
					if(var instanceof IReferenceModel)
						objectContainer.addObjectAndPointer((IReferenceModel) var, ((ReferenceFigure) fieldFig).getAnchor());
				}
			}
			fig.getLayoutManager().layout(fig);
			runtimeViewer.updateLayout();
		}

		void addFields(IObjectModel model, FigureProvider figureProvider) {
			visibleFields = new Figure();
			visibleFields.setLayoutManager(new GridLayout(1, false));
			getLayoutManager().setConstraint(visibleFields, new GridData(SWT.FILL, SWT.DEFAULT, true, true));
			visibleFields.setToolTip(new Label("visible fields"));

			int countNotVisible = 0;
			List<IVariableModel> fields = model.getFields();
			for(IVariableModel v : fields) {
				if(v.isVisible()) {
					PandionJFigure<?> fieldFig = figureProvider.getFigure(v, false);
					visibleFields.add(fieldFig);
					visibleFields.getLayoutManager().setConstraint(fieldFig, new GridData(SWT.RIGHT, SWT.FILL, true, false));
					if(v instanceof IReferenceModel) {
						setObjectContainerVisible();
						objectContainer.addObjectAndPointer((IReferenceModel) v, ((ReferenceFigure) fieldFig).getAnchor());
					}
				}
				else
					countNotVisible++;
			}

			if(!visibleFields.getChildren().isEmpty())
				add(visibleFields);

			if(countNotVisible != 0) {
				hiddenFields = new Figure();
				hiddenFields.setOpaque(true);
				hiddenFields.setBackgroundColor(ColorConstants.lightGray);
				hiddenFields.setBorder(new LineBorder(ColorConstants.gray, 1, SWT.LINE_DASH));
				hiddenFields.setLayoutManager(new GridLayout(1, false));
				hiddenFields.setToolTip(new Label("double-click to show hidden fields"));
				GridData gridData = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
				gridData.heightHint = 10;
				getLayoutManager().setConstraint(hiddenFields, gridData);
				add(hiddenFields);

				hiddenFields.addMouseListener(new MouseListener() {
					public void mouseReleased(MouseEvent me) {}
					public void mousePressed(MouseEvent me) {}
					public void mouseDoubleClicked(MouseEvent me) {
						showPrivateFields();
					}
				});
			}
		}
	}

	private MouseListener mouseListener = new MouseListener() {
		public void mouseReleased(org.eclipse.draw2d.MouseEvent me) { }
		public void mousePressed(org.eclipse.draw2d.MouseEvent me) { }
		public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent me) {
			if(methodsFig == null) {
				IObjectModel model = getModel();
				addMethods(model);
				boolean enableMethods = !model.getRuntimeModel().getTopFrame().isInstanceFrameOf(model);
				setMethodsEnabled(enableMethods);
				fig.setToolTip(new Label("double-click to hide methods"));
			}
			else {
				fig.remove(methodsFig);
				methodsFig = null;
				fig.setToolTip(new Label("double-click to show methods"));
			}
			runtimeViewer.updateLayout();
			invalidate();
		}
	};
	
	private void addShowMethodListener() {
		methodWidgets = new ArrayList<ObjectFigure.MethodWidget>();
		fig.addMouseListener(mouseListener);
	}


	private class MethodWidget extends Figure {

		Button button;
		Label resultLabel;

		MethodWidget(IMethod method) {
			setLayoutManager(new FlowLayout());
			button = new Button(shortSig(method));
			button.setToolTip(new Label(longSig(method)));
			button.setForegroundColor(ColorConstants.black);
			FontManager.setFont(button, PandionJConstants.BUTTON_FONT_SIZE);
			button.setEnabled(methodsEnabled);
			add(button);
			resultLabel = new Label();
			resultLabel.setForegroundColor(ColorConstants.black);
			add(resultLabel);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					invoke(model, method, resultLabel);
				}
			});
		}

		private String shortSig(IMethod method) {
			return method.getElementName() + (method.getNumberOfParameters() == 0 ? "()" : "(...)");
		}

		private String longSig(IMethod method) {
			try {
				return Signature.getSignatureSimpleName(method.getReturnType()) + " " + method.getElementName() + "(" + String.join(", ", method.getParameterNames()) + ")";
			} catch (JavaModelException e) {
				return method.getElementName() + "(...)";
			}
		}

		void clear() {
			resultLabel.setText("");
		}

		@Override
		public void setEnabled(boolean value) {
			button.setEnabled(value);
		}
	}




	private void addMethods(IObjectModel model) {
		methodsFig = new Figure();
		methodsFig.setLayoutManager(new GridLayout(1, false));
		for(IMethod m : model.getVisibleMethods()) {
			MethodWidget w = new MethodWidget(m);
			methodWidgets.add(w);
			methodsFig.add(w);
		}
		fig.add(methodsFig);
		
		RuntimeViewer.getInstance().updateLayout();
	}


	private static String[] NO_PARAMS = new String[0];

	private void invoke(IObjectModel model, IMethod m, Label resultLabel) {

		PandionJUI.InvocationAction action = new PandionJUI.InvocationAction() {
			public void invoke(String expression, String[] paramValues) {
				model.invoke(m.getElementName(), new InvocationResult() {
					public void valueReturn(Object o) {
//						PandionJUI.executeUpdate(() -> {
//							for(MethodWidget w : methodWidgets)
//								w.clear();
//							try {
//								if(PrimitiveType.isPrimitive(Signature.getSignatureSimpleName(m.getReturnType()))) {
//									String val = o.toString();
//									if(m.getReturnType().equals("char"))
//										val = "'" + val + "'";
//									resultLabel.setText(" = " + val);
//								}
//								else if(!m.getReturnType().equals("V")) {
//									if(o == null)
//										resultLabel.setText(" = null");
//									else
//										RuntimeViewer.getInstance().addObject((IEntityModel) o);
//								}
//							} catch (JavaModelException e) {
//								e.printStackTrace();
//							}
//							getModel().getRuntimeModel().evaluationNotify();
//						});
					}
				}, paramValues);
			}
		};

		if(m.getNumberOfParameters() != 0)
			PandionJUI.openInvocation(m, action);
		else
			action.invoke("", NO_PARAMS);
	}

	public ConnectionAnchor getIncommingAnchor() {
		return new PositionAnchor(fig, PositionAnchor.Position.LEFT);
	}
}
