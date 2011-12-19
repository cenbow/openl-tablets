package org.openl.rules.datatype.gen.bean.writers;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.openl.rules.datatype.gen.ByteCodeGeneratorHelper;
import org.openl.rules.datatype.gen.FieldDescription;
import org.openl.util.StringTool;

/**
 * Writes getters and setters to the generated bean class.
 * 
 * @author DLiauchuk
 * TODO: separate for two writers: for getters and setters separately.
 */
public class GettersAndSettersWriter implements BeanByteCodeWriter {
    
    private String beanNameWithPackage;
    private Map<String, FieldDescription> beanFields;
    
    /**
     * 
     * @param beanNameWithPackage name of the class being generated with package, symbol '/' is used as separator<br> 
     * (e.g. <code>my/test/TestClass</code>)
     * @param beanFields fields of generating class.
     */
    public GettersAndSettersWriter(String beanNameWithPackage, Map<String, FieldDescription> beanFields) {
        this.beanNameWithPackage = beanNameWithPackage;
        this.beanFields = new HashMap<String, FieldDescription>(beanFields);
    }
    
    public void write(ClassWriter classWriter) {
        for(Map.Entry<String, FieldDescription> field : beanFields.entrySet()) {
            generateGetter(beanNameWithPackage, classWriter, field);
            generateSetter(beanNameWithPackage, classWriter, field);
        }

    }
    
    /**
     * Generates setter for the field.
     * 
     * @param beanNameWithPackage
     * @param classWriter
     * @param field
     */
    private void generateSetter(String beanNameWithPackage, ClassWriter classWriter, Map.Entry<String, FieldDescription> field) {
        MethodVisitor methodVisitor;
        String fieldName = field.getKey();
        FieldDescription fieldType = field.getValue();        
        String setterName = StringTool.getSetterName(field.getKey());
        
        methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC,  setterName, String.format("(%s)V", 
            ByteCodeGeneratorHelper.getJavaType(fieldType)), null, null);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitVarInsn(ByteCodeGeneratorHelper.getConstantForVarInsn(fieldType), 1);
        
        methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, beanNameWithPackage, fieldName, ByteCodeGeneratorHelper.getJavaType(fieldType));
        methodVisitor.visitInsn(Opcodes.RETURN);
        
        // long and double types are the biggest ones, so they use a maximum of three stack  
        // elements and three local variables for setter method.
        if (long.class.equals(fieldType.getType()) || double.class.equals(fieldType.getType())) {
            methodVisitor.visitMaxs(3, 3);
        } else {
            methodVisitor.visitMaxs(2, 2);
        }
    }
    
    /**
     * Generates getter for the field.
     * 
     * @param beanNameWithPackage
     * @param classWriter
     * @param field
     */
    private void generateGetter(String beanNameWithPackage, ClassWriter classWriter, Map.Entry<String, FieldDescription> field) {
        MethodVisitor methodVisitor;
        String fieldName = field.getKey();
        FieldDescription fieldType = field.getValue();
        String getterName = StringTool.getGetterName(fieldName);
        
        methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC,  getterName, String.format("()%s",
            ByteCodeGeneratorHelper.getJavaType(fieldType)), null, null);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, beanNameWithPackage, fieldName, ByteCodeGeneratorHelper.getJavaType(fieldType));
        methodVisitor.visitInsn(ByteCodeGeneratorHelper.getConstantForReturn(fieldType));
        
        // long and double types are the biggest ones, so they use a maximum of two stack  
        // elements and one local variable for getter method.
        if (long.class.equals(fieldType.getType()) || double.class.equals(fieldType.getType())) {
            methodVisitor.visitMaxs(2, 1);
        } else {
            methodVisitor.visitMaxs(1, 1);
        }
    }

}