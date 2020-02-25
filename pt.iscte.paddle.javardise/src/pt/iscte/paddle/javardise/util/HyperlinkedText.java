package pt.iscte.paddle.javardise.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import pt.iscte.paddle.model.IProgramElement;

public class HyperlinkedText {

	private StringBuffer buffer;
	private Map<Integer, Runnable> actionMap;
	private Multimap<Integer, IProgramElement> elementMap;
	private int linkCount;
	private Consumer<Iterable<IProgramElement>> consumer;

	public HyperlinkedText(Consumer<Iterable<IProgramElement>> consumer) {
		buffer = new StringBuffer();
		actionMap = new HashMap<>();
		elementMap = ArrayListMultimap.create();
		linkCount = 0;
		this.consumer = consumer;
	}

	public HyperlinkedText words(String text) {
		buffer.append(text);
		return this;
	}

	public HyperlinkedText line(String text) {
		buffer.append(text).append(System.lineSeparator());
		return this;
	}

	public HyperlinkedText newline() {
		buffer.append(System.lineSeparator());
		return this;
	}

	public HyperlinkedText link(String text, Iterable<? extends IProgramElement> elements) {
		linkCount++;
		buffer.append("<a href=\"" + linkCount + "\">" + text + "</a>");
		for(IProgramElement e : elements)
			elementMap.put(linkCount, e);
		return this;
	}

	public HyperlinkedText link(String text, IProgramElement ... elements) {
		return link(text, Arrays.asList(elements));
	}
	
	public HyperlinkedText link(String text, Runnable action) {
		linkCount++;
		buffer.append("<a href=\"" + linkCount + "\">" + text + "</a>");
		actionMap.put(linkCount, action);
		return this;
	}
	

	public Link create(Composite parent, int style) {
		Link link = new Link(parent, style);
		link.setText(buffer.toString());
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int id = Integer.parseInt(e.text);
				if(elementMap.containsKey(id)) {
					Collection<IProgramElement> collection = elementMap.get(Integer.parseInt(e.text));
					consumer.accept(collection);
				}
				else if(actionMap.containsKey(id)) {
					actionMap.get(id).run();
				}
			}
		});
		
		Point size = link.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if(parent.getLayout() instanceof GridLayout)
			link.setLayoutData(new GridData(size.x, size.y));
		else if(parent.getLayout() instanceof RowLayout)
			link.setLayoutData(new RowData(size.x, size.y));
		
		return link;
	}
}
