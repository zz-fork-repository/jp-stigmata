package jp.sourceforge.stigmata.birthmarks.smc;

import jp.sourceforge.stigmata.Birthmark;
import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.birthmarks.BirthmarkExtractVisitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 
 * 
 *
 * @author Haruaki TAMADA
 */
public class SequentialMethodCallBirthmarkExtractVisitor extends BirthmarkExtractVisitor{
    public SequentialMethodCallBirthmarkExtractVisitor(ClassVisitor visitor, Birthmark birthmark, BirthmarkContext context){
        super(visitor, birthmark, context);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                      String signature, String[] exceptions){

        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);

        return new MethodVisitor(Opcodes.ASM4, visitor){
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc){
                String className = owner.replace('/', '.');
                if(getEnvironment().getWellknownClassManager().isWellKnownClass(className)){
                    addElement(className, name, desc);
                }
                super.visitMethodInsn(opcode, owner, name, desc);
            }
        };
    }

    protected void addElement(String className, String methodName, String description){
        addElement(new MethodCallBirthmarkElement(className, methodName, description));
    }
}
