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



import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.map.AbstractObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.core.internal.databinding.identity.IdentityMap;
import org.eclipse.core.internal.databinding.identity.IdentitySet;
import org.eclipse.core.internal.databinding.property.Util;

/**
 * Maps objects to one of their attributes. Tracks changes to the underlying
 * observable set of objects (keys), as well as changes to attribute values.
 */
public class SetCustomValueObservableMap extends
		AbstractObservableMap {

	private IObservableSet keySet;

	private Object valueType;

	private CustomValueProperty detailProperty;

	private IPropertyChangeListener detailListener = new IPropertyChangeListener() {
		@Override
		public void propertyChanged(final ModelObject modelObject,
				final Object oldValue, final Object newValue) {
			if (!isDisposed() && !updating) {
				getRealm().exec(new Runnable() {
					public void run() {
							notifyIfChanged(modelObject, oldValue, newValue);
					}
				});
			}
		}

		@Override
		public void propertyWentStale(final ModelObject modelObject) {
			if (!isDisposed() && !updating) {
				getRealm().exec(new Runnable() {
					public void run() {
						boolean wasStale = !staleKeys
								.isEmpty();
						staleKeys.add(modelObject);
						if (!wasStale)
							fireStale();
					}
				});
			}
		}
	};
	
	/**
	 * The set of all keys that are currently stale.  This is the one
	 * part of this class that is not scalable for it may be that the entire
	 * set of keys go stale.
	 */
	private Set staleKeys;

	private boolean updating;

	/**
	 * The initial reason for this class is for use by the label provider in a
	 * table. The label provider does not need to be informed of additions and
	 * removals, just property value changes. However as this is an
	 * IObservableMap it should keep to the contract for that interface and
	 * notify consumers of additions and removals. That means listening to
	 * changes to the keySet that was passed to this class.
	 */
	private ISetChangeListener setChangeListener = new ISetChangeListener() {
		public void handleSetChange(SetChangeEvent event) {
			Set addedKeys = new HashSet(event.diff.getAdditions());
			Set removedKeys = new HashSet(event.diff.getRemovals());
			Map oldValues = new HashMap();
			Map newValues = new HashMap();
			for (Iterator it = removedKeys.iterator(); it.hasNext();) {
				Object removedKey = it.next();
				Object oldValue = null;
				if (removedKey != null) {
					oldValue = doGet(removedKey);
					staleKeys.remove(removedKey);
				}
				oldValues.put(removedKey, oldValue);
			}
			for (Iterator it = addedKeys.iterator(); it.hasNext();) {
				Object addedKey = it.next();
				Object newValue = null;
				if (addedKey != null) {
					newValue = doGet(addedKey);
				}
				newValues.put(addedKey, newValue);
			}
			Set changedKeys = Collections.emptySet();
			fireMapChange(Diffs.createMapDiff(addedKeys, removedKeys,
					changedKeys, oldValues, newValues));
		}
	};

	private IStaleListener staleListener = new IStaleListener() {
		public void handleStale(StaleEvent staleEvent) {
			fireStale();
		}
	};

	private Set<Map.Entry> entrySet = new EntrySet();

	private class EntrySet extends AbstractSet<Map.Entry> {

		public Iterator<Map.Entry> iterator() {
			final Iterator keyIterator = keySet.iterator();
			return new Iterator<Map.Entry>() {

				public boolean hasNext() {
					return keyIterator.hasNext();
				}

				public Map.Entry next() {
					final Object key = keyIterator.next();
					return new Map.Entry() {

						public Object getKey() {
							getterCalled();
							return key;
						}

						public Object getValue() {
							return get(getKey());
						}

						public Object setValue(Object value) {
							return put(getKey(), value);
						}
					};
				}

				public void remove() {
					keyIterator.remove();
				}
			};
		}

		public int size() {
			return keySet.size();
		}
	}

	/**
	 * @param keySet
	 * @param valueProperty
	 */
	public SetCustomValueObservableMap(IObservableSet keySet,
			CustomValueProperty valueProperty) {
		super(keySet.getRealm());
		this.keySet = keySet;
		this.valueType = valueProperty.getValueType();
		
		keySet.addDisposeListener(new IDisposeListener() {
			public void handleDispose(DisposeEvent disposeEvent) {
				SetCustomValueObservableMap.this.dispose();
			}
		});

		this.detailProperty = valueProperty;
	}

	protected void firstListenerAdded() {
		ModelObject.addPropertyChangeListener(detailProperty, detailListener);
		
		staleKeys = new IdentitySet();
		
		getRealm().exec(new Runnable() {
			public void run() {
				hookListeners();
			}
		});
		
		super.firstListenerAdded();
	}

	protected void lastListenerRemoved() {
		super.lastListenerRemoved();
		staleKeys.clear();
		staleKeys = null;
	}

	private void hookListeners() {
		if (keySet != null) {
			keySet.addSetChangeListener(setChangeListener);
			keySet.addStaleListener(staleListener);
		}
	}

	private void unhookListeners() {
		if (keySet != null) {
			keySet.removeSetChangeListener(setChangeListener);
			keySet.removeStaleListener(staleListener);
		}
	}

	protected final void fireSingleChange(Object key, Object oldValue, Object newValue) {
		fireMapChange(Diffs.createMapDiffSingleChange(key, oldValue, newValue));
	}

	/**
	 * @since 1.2
	 */
	public Object getKeyType() {
		return keySet.getElementType();
	}

	/**
	 * @since 1.2
	 */
	public Object getValueType() {
		return valueType;
	}

	/**
	 * @since 1.3
	 */
	public Object remove(Object key) {
		checkRealm();

		Object oldValue = get(key);
		keySet().remove(key);

		return oldValue;
	}

	/**
	 * @since 1.3
	 */
	public boolean containsKey(Object key) {
		getterCalled();
		return keySet().contains(key);
	}

	public Set<Map.Entry> entrySet() {
		return entrySet;
	}

	public Set keySet() {
		return keySet;
	}

	final public Object get(Object key) {
		getterCalled();
		if (!keySet.contains(key))
			return null;
		return doGet(key);
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	final public Object put(Object key, Object value) {
		checkRealm();
		if (!keySet.contains(key))
			return null;
		return doPut(key, value);
	}

	protected Object doGet(Object key) {
		return detailProperty.getValue(key);
	}

	protected Object doPut(Object key, Object value) {
		Object oldValue = detailProperty.getValue(key);

		updating = true;
		try {
			detailProperty.setValue(key, value);
		} finally {
			updating = false;
		}

		notifyIfChanged(key, oldValue, value);

		return oldValue;
	}

	private void notifyIfChanged(Object key, Object oldValue, Object newValue) {
		assert(newValue == detailProperty.getValue(key));
		if (!Util.equals(oldValue, newValue) || staleKeys.contains(key)) {
			staleKeys.remove(key);
			fireMapChange(Diffs.createMapDiffSingleChange(key, oldValue,
					newValue));
		}
	}

	public boolean isStale() {
		return super.isStale() || keySet.isStale() || staleKeys != null && !staleKeys.isEmpty();
	}

	public IProperty getProperty() {
		return detailProperty;
	}

	public synchronized void dispose() {
		detailListener = null;
		detailProperty = null;
		staleKeys = null;

		unhookListeners();
		entrySet = null;
		keySet = null;
		setChangeListener = null;
		super.dispose();
	}
}
