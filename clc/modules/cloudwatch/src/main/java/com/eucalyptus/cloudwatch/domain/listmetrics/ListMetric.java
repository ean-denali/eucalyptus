package com.eucalyptus.cloudwatch.domain.listmetrics;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Entity;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;

import com.eucalyptus.entities.AbstractPersistent;

@Entity @javax.persistence.Entity
@PersistenceContext(name="eucalyptus_cloudwatch")
@Table(name="list_metrics")
@Cache( usage = CacheConcurrencyStrategy.TRANSACTIONAL )
public class ListMetric extends AbstractPersistent {
  @Override
  public String toString() {
    return "ListMetric [getAccountId()=" + getAccountId() + ", getNamespace()="
        + getNamespace() + ", getMetricName()=" + getMetricName()
        + ", getDimensions()=" + getDimensions() + "]";
  }

  public static final int MAX_DIM_NUM = 10;
  private static final Logger LOG = Logger.getLogger(ListMetric.class);
  public ListMetric() {
    super();
  }
  
  public String getAccountId() {
    return accountId;
  }
  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }
  public String getNamespace() {
    return namespace;
  }
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }
  public String getMetricName() {
    return metricName;
  }
  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }
  public Collection<ListMetricDimension> getDimensions() {
    TreeSet<ListMetricDimension> dimensions = new TreeSet<ListMetricDimension>();
    for (int dimNum = 1; dimNum <= MAX_DIM_NUM; dimNum++) {
      String dimName = getDimName(dimNum);
      String dimValue = getDimValue(dimNum);
      if (dimName != null && dimValue != null) {
        dimensions.add(new ListMetricDimension(dimName, dimValue));
      }
    }
    return dimensions;
  }
  public void setDimensions(Collection<ListMetricDimension> dimensions) {
    if (dimensions != null && dimensions.size() > MAX_DIM_NUM) {
      throw new IllegalArgumentException("Too many dimensions, " + dimensions.size());
    }
    for (int dimNum = 1; dimNum <= MAX_DIM_NUM; dimNum++) {
      setDimName(dimNum, null);
      setDimValue(dimNum, null);
    }
    if (dimensions == null) {
      return;
    }
    dimensions = new TreeSet<ListMetricDimension>(dimensions); // sort it
    Iterator<ListMetricDimension> iter = dimensions.iterator();
    ListMetricDimension d = null;
    for (int dimNum = 1; dimNum <= MAX_DIM_NUM; dimNum++) {
      if (!iter.hasNext()) {
        return;
      }
      d = iter.next();
      setDimName(dimNum, d.getName());
      setDimValue(dimNum, d.getValue());
    }
  }

  @Column( name = "account_id" , nullable = false)
  private String accountId;
  @Column( name = "namespace" , nullable = false)
  private String namespace; 
  @Column( name = "metric_name" , nullable = false)
  private String metricName;

  
  @Column( name = "dim_1_name" )
  private String dim1Name;
  @Column( name = "dim_2_name" )
  private String dim2Name;
  @Column( name = "dim_3_name" )
  private String dim3Name;
  @Column( name = "dim_4_name" )
  private String dim4Name;
  @Column( name = "dim_5_name" )
  private String dim5Name;
  @Column( name = "dim_6_name" )
  private String dim6Name;
  @Column( name = "dim_7_name" )
  private String dim7Name;
  @Column( name = "dim_8_name" )
  private String dim8Name;
  @Column( name = "dim_9_name" )
  private String dim9Name;
  @Column( name = "dim_10_name" )
  private String dim10Name;

  @Column( name = "dim_1_value" )
  private String dim1Value;
  @Column( name = "dim_2_value" )
  private String dim2Value;
  @Column( name = "dim_3_value" )
  private String dim3Value;
  @Column( name = "dim_4_value" )
  private String dim4Value;
  @Column( name = "dim_5_value" )
  private String dim5Value;
  @Column( name = "dim_6_value" )
  private String dim6Value;
  @Column( name = "dim_7_value" )
  private String dim7Value;
  @Column( name = "dim_8_value" )
  private String dim8Value;
  @Column( name = "dim_9_value" )
  private String dim9Value;
  @Column( name = "dim_10_value" )
  private String dim10Value;
  public String getDim1Name() {
    return dim1Name;
  }

  public void setDim1Name(String dim1Name) {
    this.dim1Name = dim1Name;
  }

  public String getDim2Name() {
    return dim2Name;
  }

  public void setDim2Name(String dim2Name) {
    this.dim2Name = dim2Name;
  }

  public String getDim3Name() {
    return dim3Name;
  }

  public void setDim3Name(String dim3Name) {
    this.dim3Name = dim3Name;
  }

  public String getDim4Name() {
    return dim4Name;
  }

  public void setDim4Name(String dim4Name) {
    this.dim4Name = dim4Name;
  }

  public String getDim5Name() {
    return dim5Name;
  }

  public void setDim5Name(String dim5Name) {
    this.dim5Name = dim5Name;
  }

  public String getDim6Name() {
    return dim6Name;
  }

  public void setDim6Name(String dim6Name) {
    this.dim6Name = dim6Name;
  }

  public String getDim7Name() {
    return dim7Name;
  }

  public void setDim7Name(String dim7Name) {
    this.dim7Name = dim7Name;
  }

  public String getDim8Name() {
    return dim8Name;
  }

  public void setDim8Name(String dim8Name) {
    this.dim8Name = dim8Name;
  }

  public String getDim9Name() {
    return dim9Name;
  }

  public void setDim9Name(String dim9Name) {
    this.dim9Name = dim9Name;
  }

  public String getDim10Name() {
    return dim10Name;
  }

  public void setDim10Name(String dim10Name) {
    this.dim10Name = dim10Name;
  }

  public String getDim1Value() {
    return dim1Value;
  }

  public void setDim1Value(String dim1Value) {
    this.dim1Value = dim1Value;
  }

  public String getDim2Value() {
    return dim2Value;
  }

  public void setDim2Value(String dim2Value) {
    this.dim2Value = dim2Value;
  }

  public String getDim3Value() {
    return dim3Value;
  }

  public void setDim3Value(String dim3Value) {
    this.dim3Value = dim3Value;
  }

  public String getDim4Value() {
    return dim4Value;
  }

  public void setDim4Value(String dim4Value) {
    this.dim4Value = dim4Value;
  }

  public String getDim5Value() {
    return dim5Value;
  }

  public void setDim5Value(String dim5Value) {
    this.dim5Value = dim5Value;
  }

  public String getDim6Value() {
    return dim6Value;
  }

  public void setDim6Value(String dim6Value) {
    this.dim6Value = dim6Value;
  }

  public String getDim7Value() {
    return dim7Value;
  }

  public void setDim7Value(String dim7Value) {
    this.dim7Value = dim7Value;
  }

  public String getDim8Value() {
    return dim8Value;
  }

  public void setDim8Value(String dim8Value) {
    this.dim8Value = dim8Value;
  }

  public String getDim9Value() {
    return dim9Value;
  }

  public void setDim9Value(String dim9Value) {
    this.dim9Value = dim9Value;
  }

  public String getDim10Value() {
    return dim10Value;
  }

  public void setDim10Value(String dim10Value) {
    this.dim10Value = dim10Value;
  }
  
  private void setDimName(int dimNum, String value) {
    try {
      if ((dimNum < 1) || (dimNum > MAX_DIM_NUM)) {
        throw new IllegalArgumentException("No such method");
      }
      Method m = this.getClass().getMethod("setDim"+dimNum+"Name", String.class);
      m.invoke(this, value);
    } catch (Exception ex) {
      LOG.error("Unable to invoke setDim"+dimNum+"Name, method may not exist");
      LOG.error(ex,ex);
    }
  }

  private void setDimValue(int dimNum, String value) {
    try {
      if ((dimNum < 1) || (dimNum > MAX_DIM_NUM)) {
        throw new IllegalArgumentException("No such method");
      }
      Method m = this.getClass().getMethod("setDim"+dimNum+"Value", String.class);
      m.invoke(this, value);
    } catch (Exception ex) {
      LOG.error("Unable to invoke setDim"+dimNum+"Value, method may not exist");
      LOG.error(ex,ex);
    }
  }

  private String getDimName(int dimNum) {
    try {
      if ((dimNum < 1) || (dimNum > MAX_DIM_NUM)) {
        throw new IllegalArgumentException("No such method");
      }
      Method m = this.getClass().getMethod("getDim"+dimNum+"Name");
      return (String) m.invoke(this);
    } catch (Exception ex) {
      LOG.error("Unable to invoke setDim"+dimNum+"Name, method may not exist");
      LOG.error(ex,ex);
      return null;
    }
  }

  private String getDimValue(int dimNum) {
    try {
      if ((dimNum < 1) || (dimNum > MAX_DIM_NUM)) {
        throw new IllegalArgumentException("No such method");
      }
      Method m = this.getClass().getMethod("getDim"+dimNum+"Value");
      return (String) m.invoke(this);
    } catch (Exception ex) {
      LOG.error("Unable to invoke setDim"+dimNum+"Value, method may not exist");
      LOG.error(ex,ex);
      return null;
    }
  }
}