import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Snippet {
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("StackOverflow");
		shell.setLayout(new GridLayout(1, false));

		new Text(shell, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		new Text(shell, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		
		final Text text = new Text(shell, SWT.SINGLE | SWT.LEAD | SWT.BORDER);

//		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		text.setText("??????");

		final ControlDecoration dec = new ControlDecoration(text, SWT.RIGHT | SWT.TOP);
		dec.setMarginWidth(50);
		dec.setDescriptionText("blabla");
		dec.setImage(new Image(display, "eclipse128.png")); 
		dec.setShowOnlyOnFocus(false);
//		dec.hide();

		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (text.getText().length() > 0) {
					dec.hide(); 			
				} else {
					dec.show();
				}
			}
		});
		shell.pack();
		shell.setSize(400, 500);
		shell.open();

		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
	}
}