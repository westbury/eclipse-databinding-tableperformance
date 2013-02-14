package com.github.westbury.tableperformance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.property.value.IValueProperty;


/**
 * A domain object that uses a single static listener list that all domain
 * object instances fire on when properties are changed.
 */
public class ModelObject {

	private static Map<IValueProperty,List<IPropertyChangeListener>> listeners = new HashMap<IValueProperty,List<IPropertyChangeListener>>(); 

	public static IValueProperty prop1Property = new CustomValueProperty() {
		@Override
		protected Object doGetValue(Object source) {
			return ((ModelObject)source).getProp1();
		}

		@Override
		protected void doSetValue(Object source, Object value) {
			((ModelObject)source).setProp1((String) value);
		}
	};

	public static IValueProperty prop2Property = new CustomValueProperty() {
		@Override
		protected Object doGetValue(Object source) {
			return ((ModelObject)source).getProp2();
		}

		@Override
		protected void doSetValue(Object source, Object value) {
			((ModelObject)source).setProp2((String) value);
		}
	};

	public static IValueProperty prop3Property = new CustomValueProperty() {
		@Override
		protected Object doGetValue(Object source) {
			return ((ModelObject)source).getProp3();
		}

		@Override
		protected void doSetValue(Object source, Object value) {
			((ModelObject)source).setProp3((String) value);
		}
	};

	private String prop1;

	private String prop2;

	private String prop3;

	public String getProp1() { return prop1; };

	public void setProp1(String prop1) {
		firePropertyChange(prop1Property, this.prop1, this.prop1 = prop1);
	};

	public String getProp2() { return prop2; };

	public void setProp2(String prop2) {
		firePropertyChange(prop2Property, this.prop2, this.prop2 = prop2);
	};

	public String getProp3() { return prop3; };

	public void setProp3(String prop3) {
		firePropertyChange(prop3Property, this.prop3, this.prop3 = prop3);
	};

	public static void addPropertyChangeListener(IValueProperty property, IPropertyChangeListener listener) {
		List<IPropertyChangeListener> listenerList = listeners.get(property);
		if (listenerList == null) {
			listenerList = new ArrayList<IPropertyChangeListener>();
			listeners.put(property, listenerList);
		}
		listenerList.add(listener);
	}

	public static void removePropertyChangeListener(IValueProperty property, IPropertyChangeListener listener) {
		// TODO
	}

	protected void firePropertyChange(IValueProperty property, 
			Object oldValue,
			Object newValue) {
		List<IPropertyChangeListener> listenerList = listeners.get(property);
		if (listenerList != null) {
			for (IPropertyChangeListener listener : listenerList) {
				listener.propertyChanged(this, oldValue, newValue);
			}
		}
	}
} 