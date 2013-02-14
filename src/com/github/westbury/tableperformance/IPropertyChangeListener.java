package com.github.westbury.tableperformance;

public interface IPropertyChangeListener {

	void propertyChanged(ModelObject modelObject, Object oldValue,
			Object newValue);

	void propertyWentStale(ModelObject modelObject);

}
