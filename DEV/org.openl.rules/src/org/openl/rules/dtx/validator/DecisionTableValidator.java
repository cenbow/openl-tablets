/**
 * Created Feb 7, 2007
 */
package org.openl.rules.dtx.validator;

import java.util.Map;

import org.openl.OpenL;
import org.openl.rules.dtx.IBaseCondition;
import org.openl.rules.dtx.IDecisionTable;
import org.openl.rules.dtx.type.domains.IDomainAdaptor;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.rules.validator.IValidatedObject;
import org.openl.rules.validator.IValidationResult;
import org.openl.rules.validator.IValidator;
import org.openl.types.IOpenClass;

/**
 * @author snshor
 * 
 */
public final class DecisionTableValidator implements IValidator {
    private static final DecisionTableValidator INSTANCE = new DecisionTableValidator();
    
    private DecisionTableValidator(){
    }
    
    public static DecisionTableValidator getInstance(){
        return INSTANCE;
    }

    public static DesionTableValidationResult validateTable(IDecisionTable decisionTable,
            Map<String, IDomainAdaptor> domains,
            IOpenClass type) throws Exception {

        IDecisionTableValidatedObject validatedObject = new DecisionTableValidatedObject(decisionTable, domains);
        OpenL openl = ((XlsModuleOpenClass) type).getOpenl();

        return (DesionTableValidationResult) getInstance().validate(validatedObject, openl);
    }

    public IValidationResult validate(IValidatedObject validatedObject, OpenL openl) {
        return new ValidationAlgorithm((IDecisionTableValidatedObject) validatedObject, openl).validate();
    }
    
    /**
     * Provides unique name for Condition parameters
     * 
     * @param condition
     * @param pname
     * @return
     */
    
    static public String getUniqueConditionParamName(IBaseCondition condition, String pname) {
        return condition.getName() + "_" + pname;
    }
}
