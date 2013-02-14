package com.github.westbury.tableperformance;


import java.util.Date;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple TableViewer to demonstrate usage
 * 
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class JfaceTableWithBeans {

	public JfaceTableWithBeans(Shell shell) {
		final TableViewer viewer = new TableViewer(shell);
		viewer.getTable().setLinesVisible(true);

		final IObservableList elements = new WritableList();

		for( int i = 0; i < 200000; i++ ) {
			ModelBean element = new ModelBean();
			element.setProp1("Value 1," + i);
			element.setProp2("Value 2," + i);
			element.setProp3("Value 3," + i);
			elements.add(element);
		}

		TableViewerColumn column1 = new TableViewerColumn(viewer,SWT.NONE);
		column1.getColumn().setText("Property 1");
		column1.getColumn().setWidth(200);

		TableViewerColumn column2 = new TableViewerColumn(viewer,SWT.NONE);
		column2.getColumn().setText("Property 2");
		column2.getColumn().setWidth(200);

		TableViewerColumn column3 = new TableViewerColumn(viewer,SWT.NONE);
		column3.getColumn().setText("Property 3");
		column3.getColumn().setWidth(200);

		Date now = new Date();
		
		ViewerSupport.bind(viewer, elements, 
			    BeanProperties.
			    values(new String[] { "prop1", "prop2", "prop3" })); 

		System.out.println("time: " + (new Date().getTime() - now.getTime()));
		
		final Display current = Display.getCurrent();
		
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
					current.asyncExec(new Runnable() {
						@Override
						public void run() {
							((ModelBean)elements.get(2)).setProp1("update 1");
						}
					});
					Thread.sleep(5000);
					current.asyncExec(new Runnable() {
						@Override
						public void run() {
							((ModelBean)elements.get(4)).setProp2("update 2");
						}
					});
					Thread.sleep(5000);
					current.asyncExec(new Runnable() {
						@Override
						public void run() {
							for( int i = 10; i < 20; i++ ) {
								ModelBean element = new ModelBean();
								element.setProp1("Value 1," + i);
								element.setProp2("Value 2," + i);
								element.setProp3("Value 3," + i);
								elements.add(element);
							}
						}
					});
					Thread.sleep(5000);
					current.asyncExec(new Runnable() {
						@Override
						public void run() {
							((ModelBean)elements.get(12)).setProp3("update 3");
						}
					});
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Display display = new Display ();
		
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() { 
			public void run() { 
				Shell shell = new Shell(display);
				shell.setLayout(new FillLayout());
				new JfaceTableWithBeans(shell);
				shell.open ();

				while (!shell.isDisposed ()) {
					if (!display.readAndDispatch ()) display.sleep ();
				}
			}
		}); 

		display.dispose ();
	}
}
