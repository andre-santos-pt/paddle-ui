package pt.iscte.paddle.pandion.figures;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.swt.SWT;

import pt.iscte.paddle.pandion.FontManager;
import pt.iscte.paddle.pandion.Utils;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.PandionJConstants;

// TODO inutil?
public class ValueExtensionFigure extends PandionJFigure<IValueModel> {
	
	private ValueLabel valueLabel;
	private GridLayout layout;

	public ValueExtensionFigure(IValueModel model, IFigure figure) {
		super(model, false);
		
		layout = new GridLayout(1, false);
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		setLayoutManager(layout);

		Figure comp = new Figure();
		GridLayout compLayout = new GridLayout(2,false);
		compLayout.marginHeight = 0;
		compLayout.marginWidth = 0;
		compLayout.horizontalSpacing = 3;
		compLayout.verticalSpacing = 0;
		
		comp.setLayoutManager(compLayout);
		
		String tooltip = ""; //Utils.getTooltip(model); 
		
		Label nameLabel = new Label(model.getName());
		nameLabel.setForegroundColor(ColorConstants.black);
		FontManager.setFont(nameLabel, PandionJConstants.VAR_FONT_SIZE);
		
		nameLabel.setToolTip(new Label(tooltip));
		comp.add(nameLabel);
		comp.add(figure);
		add(comp);
		
		layout.setConstraint(comp, new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));
	}
}
