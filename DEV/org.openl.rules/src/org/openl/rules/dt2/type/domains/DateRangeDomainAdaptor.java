package org.openl.rules.dt2.type.domains;

import java.util.Date;

import org.openl.domain.DateRangeDomain;

import org.openl.ie.constrainer.IntVar;

/**
 * Adaptor for date ranges. Helps to access dates in range by index and retrieve
 * index of date within the range.
 * 
 * @author PUdalau
 * 
 */
public class DateRangeDomainAdaptor implements IDomainAdaptor {
    private DateRangeDomain domain;

    public DateRangeDomainAdaptor(DateRangeDomain domain) {
        this.domain = domain;
    }

    public int getIndex(Object value) {
        return domain.getIndex((Date) value);
    }

    public int getIntVarDomainType() {
        return IntVar.DOMAIN_PLAIN;
    }

    public int getMax() {
        return domain.getIndex(domain.getMax());
    }

    public int getMin() {
        return 0;
    }

    public Object getValue(int index) {
        return domain.getValue(index);
    }

    @Override
    public String toString() {
        return "[" + getMin() + ";" + getMax() + "]";
    }

    public IDomainAdaptor merge(IDomainAdaptor adaptor) {
        DateRangeDomainAdaptor a = (DateRangeDomainAdaptor)adaptor;
        
        Date min = domain.getMin().before(a.domain.getMin()) ? domain.getMin() : a.domain.getMin();
        Date max = domain.getMax().after(a.domain.getMax()) ? domain.getMax() : a.domain.getMax();
        
        
        
        return new DateRangeDomainAdaptor(new DateRangeDomain(min, max));
    }
}
