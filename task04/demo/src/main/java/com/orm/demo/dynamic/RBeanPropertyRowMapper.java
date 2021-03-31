/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orm.demo.dynamic;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.*;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;


public class RBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {

    /** Logger available to subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /** The class we are mapping to */
    private Class<T> mappedClass;

    /** Whether we're defaulting primitives when mapping a null value */
    private boolean primitivesDefaultedForNullValue = false;

    /** Map of the fields we provide mapping for */
    private Map<String, PropertyDescriptor> mappedFields;

    /** Set of bean properties we provide mapping for */
    private Set<String> mappedProperties;

    private ObjectMapper objectMapper;

    private Map<String, TypeReference> typeReferenceMap;


    /**
     * Create a new {@code BeanPropertyRowMapper} for bean-style configuration.
     * @see #setMappedClass
     * @see #setCheckFullyPopulated
     */
    public RBeanPropertyRowMapper() {
    }

    /**
     * Create a new {@code BeanPropertyRowMapper}, accepting unpopulated
     * properties in the target bean.
     * <p>Consider using the {@link #newInstance} factory method instead,
     * which allows for specifying the mapped type once only.
     * @param mappedClass the class that each row should be mapped to
     */
    public RBeanPropertyRowMapper(Class<T> mappedClass) {
        initialize(mappedClass);
    }

    /**
     * Create a new {@code BeanPropertyRowMapper}, accepting unpopulated
     * properties in the target bean.
     * <p>Consider using the {@link #newInstance} factory method instead,
     * which allows for specifying the mapped type once only.
     * @param mappedClass the class that each row should be mapped to
     */
    public RBeanPropertyRowMapper(Class<T> mappedClass, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        initialize(mappedClass);
    }

    public RBeanPropertyRowMapper(Class<T> mappedClass, ObjectMapper objectMapper, Map<String, TypeReference> typeReferenceMap) {
        this.objectMapper = objectMapper;
        this.typeReferenceMap = typeReferenceMap;
        initialize(mappedClass);
    }

    /**
     * Initialize the mapping metadata for the given class.
     * @param mappedClass the mapped class
     */
    protected void initialize(Class<T> mappedClass) {

        this.mappedClass = mappedClass;
        this.mappedFields = new HashMap<String, PropertyDescriptor>();
        this.mappedProperties = new HashSet<String>();
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(mappedClass);
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null) {
                this.mappedFields.put(lowerCaseName(pd.getName()), pd);
                String underscoredName = underscoreName(pd.getName());
                if (!lowerCaseName(pd.getName()).equals(underscoredName)) {
                    this.mappedFields.put(underscoredName, pd);
                }
                this.mappedProperties.add(pd.getName());
            }
        }
    }


    /**
     * Extract the values for all columns in the current row.
     * <p>Utilizes public setters and result set metadata.
     * @see ResultSetMetaData
     */
    @Override
    public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
        Assert.state(this.mappedClass != null, "Mapped class was not specified");
        T mappedObject = BeanUtils.instantiate(this.mappedClass);
        BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
        initBeanWrapper(bw);

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Set<String> populatedProperties = (isCheckFullyPopulated() ? new HashSet<String>() : null);

        for (int index = 1; index <= columnCount; index++) {
            String column = JdbcUtils.lookupColumnName(rsmd, index);
            String field = lowerCaseName(column.replaceAll(" ", ""));
            PropertyDescriptor pd = this.mappedFields.get(field);
            if (pd != null) {
                try {
                    Object value = super.getColumnValue(rs, index, pd);


                    if (rowNumber == 0 && logger.isDebugEnabled()) {
                        logger.debug("Mapping column '" + column + "' to property '" + pd.getName() +
                                "' of type [" + ClassUtils.getQualifiedName(pd.getPropertyType()) + "]");
                    }
                    try {
                        // MySQL 字段字符串 转换 json => List<T>  start
                        if (value instanceof String && objectMapper != null && typeReferenceMap.size() > 0 && pd.getPropertyType().getSimpleName().contains("List")) {
                            List list = objectMapper.readValue(value.toString(), typeReferenceMap.get(column));
                            bw.setPropertyValue(pd.getName(), list);
                        } else {
                            bw.setPropertyValue(pd.getName(), value);
                        }
                        // MySQL 字段字符串 转换 json => List<T>  end
                    } catch (TypeMismatchException ex) {
                        if (value == null && this.primitivesDefaultedForNullValue) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Intercepted TypeMismatchException for row " + rowNumber +
                                        " and column '" + column + "' with null value when setting property '" +
                                        pd.getName() + "' of type [" +
                                        ClassUtils.getQualifiedName(pd.getPropertyType()) +
                                        "] on object: " + mappedObject, ex);
                            }
                        } else {
                            throw ex;
                        }
                    } catch (JsonParseException e) {
                        logger.error(e.getMessage(),e);
                    } catch (JsonMappingException e) {
                        logger.error(e.getMessage(),e);
                    } catch (IOException e) {
                        logger.error(e.getMessage(),e);
                    }
                    if (populatedProperties != null) {
                        populatedProperties.add(pd.getName());
                    }
                } catch (NotWritablePropertyException ex) {
                    throw new DataRetrievalFailureException(
                            "Unable to map column '" + column + "' to property '" + pd.getName() + "'", ex);
                }
            } else {
                // No PropertyDescriptor found
                if (rowNumber == 0 && logger.isDebugEnabled()) {
                    logger.debug("No property found for column '" + column + "' mapped to field '" + field + "'");
                }
            }
        }

        if (populatedProperties != null && !populatedProperties.equals(this.mappedProperties)) {
            throw new InvalidDataAccessApiUsageException("Given ResultSet does not contain all fields " +
                    "necessary to populate object of class [" + this.mappedClass.getName() + "]: " +
                    this.mappedProperties);
        }

        return mappedObject;
    }


}
