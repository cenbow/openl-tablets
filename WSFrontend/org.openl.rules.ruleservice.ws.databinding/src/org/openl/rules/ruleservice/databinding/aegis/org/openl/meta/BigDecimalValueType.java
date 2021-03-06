package org.openl.rules.ruleservice.databinding.aegis.org.openl.meta;

/*
 * #%L
 * OpenL - RuleService - RuleService - Web Services Databinding
 * %%
 * Copyright (C) 2013 OpenL Tablets
 * %%
 * See the file LICENSE.txt for copying permission.
 * #L%
 */

import java.math.BigDecimal;

import org.apache.cxf.aegis.Context;
import org.apache.cxf.aegis.type.basic.BigDecimalType;
import org.apache.cxf.aegis.xml.MessageReader;
import org.openl.meta.BigDecimalValue;

public class BigDecimalValueType extends BigDecimalType {
    @Override
    public Object readObject(MessageReader reader, Context context) {
        BigDecimal value = (BigDecimal) super.readObject(reader, context);
        return (value == null) ? null : new BigDecimalValue(value);
    }
}