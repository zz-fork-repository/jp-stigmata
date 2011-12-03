package jp.sourceforge.stigmata.birthmarks;

import java.util.ArrayList;
import java.util.List;

import jp.sourceforge.stigmata.Birthmark;
import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.BirthmarkElement;
import jp.sourceforge.stigmata.BirthmarkEnvironment;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Abstract visitor class of extracting birthmarks from class file.
 *
 * @author Haruaki TAMADA
 */
public abstract class BirthmarkExtractVisitor extends ClassVisitor{
    private Birthmark birthmark;
    private BirthmarkContext context;
    private List<Throwable> causes = new ArrayList<Throwable>();

    public BirthmarkExtractVisitor(ClassVisitor visitor, Birthmark birthmark, BirthmarkContext context){
        super(Opcodes.ASM4, visitor);
        this.birthmark = birthmark;
        this.context = context;
    }

    protected BirthmarkEnvironment getEnvironment(){
        return context.getEnvironment();
    }

    protected BirthmarkContext getContext(){
        return context;
    }

    protected void addElement(BirthmarkElement element){
        birthmark.addElement(element);
    }

    public Birthmark getBirthmark(){
        return birthmark;
    }

    public synchronized void addFailur(Throwable e){
        causes.add(e);
    }

    public boolean isSuccess(){
       return causes.size() == 0;
    }

    public synchronized Throwable[] getCauses(){
       return causes.toArray(new Throwable[causes.size()]);
    }
}
