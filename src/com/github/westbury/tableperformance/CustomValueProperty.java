package com.github.westbury.tableperformance;
/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation of various classes
 *     				from which this class was built
 *     Nigel Westbury - initial API and implementation of this class
 *******************************************************************************/


import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.map.IMapProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.property.ValuePropertyDetailList;
import org.eclipse.core.internal.databinding.property.ValuePropertyDetailMap;
import org.eclipse.core.internal.databinding.property.ValuePropertyDetailSet;
import org.eclipse.core.internal.databinding.property.ValuePropertyDetailValue;
import org.eclipse.core.internal.databinding.property.value.ListSimpleValueObservableList;
import org.eclipse.core.internal.databinding.property.value.MapSimpleValueObservableMap;
import org.eclipse.core.internal.databinding.property.value.SetSimpleValueObservableMap;
import org.eclipse.core.internal.databinding.property.value.SimplePropertyObservableValue;

/**
 * Abstract implementation of IValueProperty
 * 
 * @since 1.2
 */
public abstract class CustomValueProperty implements IValueProperty {

	protected abstract Object doGetValue(Object source);

	protected abstract void doSetValue(Object source, Object value);

	@Override
	public Object getValueType() {
		// TODO support other types
		return String.class;
	}
	
	/**
	 * By default, this method returns <code>null</code> in case the source
	 * object is itself <code>null</code>. Otherwise, this method delegates to
	 * {@link #doGetValue(Object)}.
	 * 
	 * <p>
	 * Clients may override this method if they e.g. want to return a specific
	 * default value in case the source object is <code>null</code>.
	 * </p>
	 * 
	 * @see #doGetValue(Object)
	 * 
	 * @since 1.3
	 */
	public Object getValue(Object source) {
		if (source == null) {
			return null;
		}
		return doGetValue(source);
	}

	/**
	 * @since 1.3
	 */
	public final void setValue(Object source, Object value) {
		if (source != null) {
			doSetValue(source, value);
		}
	}

	public IObservableValue observe(Object source) {
		return observe(Realm.getDefault(), source);
	}

	public IObservableValue observe(Realm realm, Object source) {
		// TODO
		return null;
//		return new SimplePropertyObservableValue(realm, source, this);
	}

	public IObservableFactory valueFactory() {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observe(target);
			}
		};
	}

	public IObservableFactory valueFactory(final Realm realm) {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observe(realm, target);
			}
		};
	}

	public IObservableValue observeDetail(IObservableValue master) {
		return MasterDetailObservables.detailValue(master,
				valueFactory(master.getRealm()), getValueType());
	}

	public IObservableList observeDetail(IObservableList master) {
		// TODO
		return null;
//		return new ListSimpleValueObservableList(master, this);
	}

	@Override
	public IObservableMap observeDetail(IObservableSet master) {
		return new SetCustomValueObservableMap(master, this);
	}

	public IObservableMap observeDetail(IObservableMap master) {
		// TODO
		return null;
//	return new MapSimpleValueObservableMap(master, this);
	}

	public final IValueProperty value(IValueProperty detailValue) {
		return new ValuePropertyDetailValue(this, detailValue);
	}

	public final IListProperty list(IListProperty detailList) {
		return new ValuePropertyDetailList(this, detailList);
	}

	public final ISetProperty set(ISetProperty detailSet) {
		return new ValuePropertyDetailSet(this, detailSet);
	}

	public final IMapProperty map(IMapProperty detailMap) {
		return new ValuePropertyDetailMap(this, detailMap);
	}
}
