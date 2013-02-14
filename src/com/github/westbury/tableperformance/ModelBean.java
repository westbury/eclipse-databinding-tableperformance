package com.github.westbury.tableperformance;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * A domain object that follows the Java Bean convention for
 * property listeners.
 */
public class ModelBean {
  private PropertyChangeSupport changeSupport = 
      new PropertyChangeSupport(this);

  private String prop1;
  
  private String prop2;
  
  private String prop3;
  
  public String getProp1() { return prop1; };
  
  public void setProp1(String prop1) {
	  changeSupport.firePropertyChange("prop1", this.prop1, this.prop1 = prop1);
  };
  
  public String getProp2() { return prop2; };
  
  public void setProp2(String prop2) {
	  changeSupport.firePropertyChange("prop2", this.prop2, this.prop2 = prop2);
  };
  
  public String getProp3() { return prop3; };
  
  public void setProp3(String prop3) {
	  changeSupport.firePropertyChange("prop3", this.prop3, this.prop3 = prop3);
  };
  
  public void addPropertyChangeListener(PropertyChangeListener 
      listener) {
    changeSupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener 
      listener) {
    changeSupport.removePropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName,
      PropertyChangeListener listener) {
    changeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(String propertyName,
      PropertyChangeListener listener) {
    changeSupport.removePropertyChangeListener(propertyName, listener);
  }

  protected void firePropertyChange(String propertyName, 
      Object oldValue,
      Object newValue) {
    changeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }
} 