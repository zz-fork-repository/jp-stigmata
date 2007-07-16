package jp.naist.se.stigmata;

/*
 * $Id$
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

import javax.imageio.spi.ServiceRegistry;

import jp.naist.se.stigmata.event.BirthmarkEngineEvent;
import jp.naist.se.stigmata.event.BirthmarkEngineListener;
import jp.naist.se.stigmata.event.OperationStage;
import jp.naist.se.stigmata.event.OperationType;
import jp.naist.se.stigmata.event.WarningMessages;
import jp.naist.se.stigmata.filter.ComparisonPairFilterManager;
import jp.naist.se.stigmata.filter.FilteredComparisonResultSet;
import jp.naist.se.stigmata.reader.ClassFileArchive;
import jp.naist.se.stigmata.reader.ClassFileEntry;
import jp.naist.se.stigmata.reader.ClasspathContext;
import jp.naist.se.stigmata.reader.DefaultClassFileArchive;
import jp.naist.se.stigmata.reader.JarClassFileArchive;
import jp.naist.se.stigmata.reader.WarClassFileArchive;
import jp.naist.se.stigmata.spi.BirthmarkSpi;
import jp.naist.se.stigmata.utils.ConfigFileImporter;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Birthmarking engine.
 *
 * @author  Haruaki TAMADA
 * @version  $Revision$ $Date$
 */
public final class Stigmata{
    private static final Stigmata instance = new Stigmata();

    private BirthmarkEnvironment defaultEnvironment = BirthmarkEnvironment.getDefaultEnvironment();
    private boolean configDone = false;
    private Stack<WarningMessages> stack = new Stack<WarningMessages>();
    private WarningMessages warnings;
    private List<BirthmarkEngineListener> listeners = new ArrayList<BirthmarkEngineListener>();

    private Stigmata(){
    }

    public static Stigmata getInstance(){
        return instance;
    }

    public void addBirthmarkEngineListener(BirthmarkEngineListener listener){
        listeners.add(listener);
    }

    public void removeBirthmarkEngineListener(BirthmarkEngineListener listener){
        listeners.remove(listener);
    }

    public WarningMessages getWarnings(){
        return warnings;
    }

    public void configuration(){
        operationStart(OperationType.CONFIGURATION);
        configuration(null);
        operationDone(OperationType.CONFIGURATION);
    }

    public void configuration(String filePath){
        InputStream target = null;
        if(filePath != null){
            try{
                target = new FileInputStream(filePath);
            } catch(FileNotFoundException e){
                filePath = null;
            }
        }

        if(filePath == null){
            File file = new File("stigmata.xml");
            if(!file.exists()){
                file = new File(System.getProperty("user.home"), ".stigmata.xml");
                if(!file.exists()){
                    file = null;
                }
            }
            if(file != null){
                try {
                    target = new FileInputStream(file);
                } catch (FileNotFoundException ex) {
                    // never throwed this exception;
                    throw new InternalError(ex.getMessage());
                }
            }
        }
        if(target == null){
            target = getClass().getResourceAsStream("/resources/stigmata.xml");
        }
        initConfiguration(target);
    }

    /**
     * create a new {@link BirthmarkEnvironment <code>BirthmarkEnvironment</code>}.
     */
    public BirthmarkEnvironment createEnvironment(){
        operationStart(OperationType.CREATE_ENVIRONMENT);
        BirthmarkEnvironment environment = new BirthmarkEnvironment();
        operationDone(OperationType.CREATE_ENVIRONMENT);
        return environment;
    }

    public BirthmarkSet[] extract(String[] birthmarks, String[] files) throws BirthmarkExtractionFailedException{
        operationStart(OperationType.EXTRACT_BIRTHMARKS);
        BirthmarkSet[] set = extract(birthmarks, files, createEnvironment());
        operationDone(OperationType.EXTRACT_BIRTHMARKS);

        return set;
    }

    public BirthmarkSet[] extract(String[] birthmarks, String[] files,
                                  BirthmarkEnvironment environment) throws BirthmarkExtractionFailedException{
        operationStart(OperationType.EXTRACT_BIRTHMARKS);
        try{
            return extractImpl(birthmarks, files, environment);
        } catch(IOException e){
            throw new BirthmarkExtractionFailedException(e);
        } finally{
            operationDone(OperationType.EXTRACT_BIRTHMARKS);
        }
    }

    public ComparisonResultSet compare(BirthmarkSet[] holders) throws IOException{
        operationStart(OperationType.COMPARE_BIRTHMARKS);
        ComparisonResultSet crs = compare(holders, createEnvironment());
        operationDone(OperationType.COMPARE_BIRTHMARKS);

        return crs;
    }

    public ComparisonResultSet compare(BirthmarkSet[] holders, BirthmarkEnvironment environment) throws IOException{
        operationStart(OperationType.COMPARE_BIRTHMARKS);
        ComparisonResultSet result = new RoundRobinComparisonResultSet(holders, environment, true);
        operationDone(OperationType.COMPARE_BIRTHMARKS);

        return result;
    }

    public ComparisonResultSet compare(BirthmarkSet[] holders1, BirthmarkSet[] holders2) throws IOException{
        operationStart(OperationType.COMPARE_BIRTHMARKS);
        ComparisonResultSet crs = compare(holders1, holders2, createEnvironment());
        operationDone(OperationType.COMPARE_BIRTHMARKS);
        return crs;
    }

    public ComparisonResultSet compare(BirthmarkSet[] holders1, BirthmarkSet[] holders2, BirthmarkEnvironment environment) throws IOException{
        operationStart(OperationType.COMPARE_BIRTHMARKS);
        ComparisonResultSet result = new RoundRobinComparisonResultSet(holders1, holders2, environment);
        operationDone(OperationType.COMPARE_BIRTHMARKS);

        return result;
    }

    public ComparisonResultSet filter(ComparisonResultSet resultset, String[] filters){
        operationStart(OperationType.FILTER_BIRTHMARKS);
        ComparisonResultSet crs = filter(resultset, filters, createEnvironment());
        operationDone(OperationType.FILTER_BIRTHMARKS);

        return crs;
    }

    public ComparisonResultSet filter(ComparisonResultSet resultset, String[] filters, BirthmarkEnvironment environment){
        operationStart(OperationType.FILTER_BIRTHMARKS);
        if(filters != null){
            List<ComparisonPairFilterSet> filterList = new ArrayList<ComparisonPairFilterSet>();
            ComparisonPairFilterManager manager = environment.getFilterManager();
            for(int i = 0; i < filters.length; i++){
                ComparisonPairFilterSet fset = manager.getFilterSet(filters[i]);
                if(fset != null){
                    filterList.add(fset);
                }
                else{
                    Logger logger = Logger.getLogger(getClass().getName());
                    logger.warning(filters[i] + ": filter not found");
                }
            }

            return filter(resultset, filterList.toArray(new ComparisonPairFilterSet[filterList.size()]));
        }
        operationDone(OperationType.FILTER_BIRTHMARKS);
        return resultset;
    }

    public ComparisonResultSet filter(ComparisonResultSet resultset, ComparisonPairFilterSet[] filters){
        operationStart(OperationType.FILTER_BIRTHMARKS);
        ComparisonResultSet crs = filter(resultset, filters, createEnvironment());
        operationDone(OperationType.FILTER_BIRTHMARKS);

        return crs;
    }

    public ComparisonResultSet filter(ComparisonResultSet resultset, ComparisonPairFilterSet[] filters, BirthmarkEnvironment environment){
        operationStart(OperationType.FILTER_BIRTHMARKS);
        FilteredComparisonResultSet filterResultSet = new FilteredComparisonResultSet(resultset);
        operationDone(OperationType.FILTER_BIRTHMARKS);

        return filterResultSet;
    }

    public double compareDetails(BirthmarkSet h1, BirthmarkSet h2){
        operationStart(OperationType.COMPARE_DETAIL_BIRTHMARKS);
        double similarity = compareDetails(h1, h2, createEnvironment());
        operationDone(OperationType.COMPARE_DETAIL_BIRTHMARKS);
        return similarity;
    }

    public double compareDetails(BirthmarkSet h1, BirthmarkSet h2, BirthmarkEnvironment environment){
        operationStart(OperationType.COMPARE_DETAIL_BIRTHMARKS);
        ComparisonPair pair = new ComparisonPair(h1, h2, environment);
        double sim = pair.calculateSimilarity();
        operationDone(OperationType.COMPARE_DETAIL_BIRTHMARKS);

        return sim;
    }

    private BirthmarkExtractor[] createExtractors(String[] birthmarkTypes, BirthmarkEnvironment environment){
        List<BirthmarkExtractor> list = new ArrayList<BirthmarkExtractor>();
        for(String type: birthmarkTypes){
            BirthmarkExtractor extractor = createExtractor(type, environment);
            list.add(extractor);
        }
        return list.toArray(new BirthmarkExtractor[list.size()]);
    }

    private BirthmarkExtractor createExtractor(String birthmarkType, BirthmarkEnvironment environment){
        BirthmarkSpi spi = environment.getService(birthmarkType);
        if(spi != null){
            BirthmarkExtractor extractor = spi.getExtractor();
            try{
                Map props = BeanUtils.describe(extractor);
                props.remove("class");
                props.remove("provider");
                for(Object keyObject: props.keySet()){
                    String key = "extractor." + spi.getType() + "." + String.valueOf(keyObject);
                    if(environment.getProperty(key) != null){
                        BeanUtils.setProperty(extractor, (String)keyObject, environment.getProperty(key));
                    }
                }
            } catch(InvocationTargetException e){
                throw new InternalError(e.getMessage());
            } catch(NoSuchMethodException e){
                throw new InternalError(e.getMessage());
            } catch(IllegalAccessException e){
                throw new InternalError(e.getMessage());
            }
            return extractor;
        }
        return null;
    }

    private byte[] inputStreamToByteArray(InputStream in) throws IOException{
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int read;
        byte[] dataBuffer = new byte[512];
        while((read = in.read(dataBuffer, 0, dataBuffer.length)) != -1){
            bout.write(dataBuffer, 0, read);
        }
        byte[] data = bout.toByteArray();

        bout.close();
        return data;
    }

    private void initConfiguration(InputStream in){
        try {
            ConfigFileImporter parser = new ConfigFileImporter(defaultEnvironment);
            parser.parse(in);
        } catch(IOException e){
            throw new ApplicationInitializationError(e);
        }
        for(Iterator<BirthmarkSpi> i = ServiceRegistry.lookupProviders(BirthmarkSpi.class); i.hasNext(); ){
            BirthmarkSpi service = i.next();
            defaultEnvironment.addService(service);
        }
        configDone = true;
    }

    private void operationStart(OperationType type){
        if(type != OperationType.CONFIGURATION && !configDone){
            configuration();
        }
        if(warnings == null){
            warnings = new WarningMessages(type);
            fireEvent(new BirthmarkEngineEvent(OperationStage.OPERATION_START, type, warnings));
        }
        stack.push(warnings);
        fireEvent(new BirthmarkEngineEvent(OperationStage.SUB_OPERATION_START, type, warnings));
    }

    private void operationDone(OperationType type){
        fireEvent(new BirthmarkEngineEvent(OperationStage.SUB_OPERATION_DONE, type, warnings));
        stack.pop();
        if(stack.size() == 0){
            fireEvent(new BirthmarkEngineEvent(OperationStage.OPERATION_DONE, type, warnings));
            warnings = null;
        }
    }

    private void fireEvent(BirthmarkEngineEvent e){
        for(BirthmarkEngineListener listener: listeners){
            switch(e.getStage()){
            case OPERATION_START:
                listener.operationStart(e);
                break;
            case SUB_OPERATION_START:
                listener.subOperationStart(e);
                break;
            case SUB_OPERATION_DONE:
                listener.subOperationDone(e);
                break;
            case OPERATION_DONE:
                listener.operationDone(e);
                break;
            default:
                throw new InternalError("unknown stage: " + e.getStage());
            }
        }
    }

    private BirthmarkSet[] extractImpl(String[] birthmarks, String[] files, BirthmarkEnvironment environment) throws IOException, BirthmarkExtractionFailedException{
        ClassFileArchive[] archives = createArchives(files, environment);
        BirthmarkExtractor[] extractors = createExtractors(birthmarks, environment);
        ExtractionUnit unit = environment.getExtractionUnit();

        if(unit == ExtractionUnit.CLASS){
            return extractFromClass(archives, extractors, environment);
        }
        else if(unit == ExtractionUnit.PACKAGE){
            return extractFromPackage(archives, extractors, environment);
        }
        else if(unit == ExtractionUnit.ARCHIVE){
            return extractFromProduct(archives, extractors, environment);
        }
        return null;
    }

    private BirthmarkSet[] extractFromPackage(ClassFileArchive[] archives, BirthmarkExtractor[] extractors, BirthmarkEnvironment environment) throws IOException, BirthmarkExtractionFailedException{
        Map<String, BirthmarkSet> list = new HashMap<String, BirthmarkSet>();

        for(ClassFileArchive archive: archives){
            for(ClassFileEntry entry: archive){
                try{
                    String name = entry.getClassName();
                    String packageName = parsePackageName(name);
                    BirthmarkSet bs = list.get(packageName);
                    if(bs == null){
                        bs = new BirthmarkSet(packageName, archive.getLocation());
                        list.put(packageName, bs);
                    }

                    byte[] data = inputStreamToByteArray(entry.getLocation().openStream());
                    for(BirthmarkExtractor extractor: extractors){
                        if(extractor.isAcceptable(ExtractionUnit.PACKAGE)){
                            Birthmark b = bs.getBirthmark(extractor.getProvider().getType());
                            if(b == null){
                                b = extractor.createBirthmark();
                                bs.addBirthmark(b);
                            }
                            extractor.extract(b, new ByteArrayInputStream(data), environment);
                        }
                    }
                } catch(IOException e){
                    warnings.addMessage(e, archive.getName());
                }
            }
        }

        return list.values().toArray(new BirthmarkSet[list.size()]);
    }

    private String parsePackageName(String name){
        String n = name.replace('/', '.');
        int index = n.lastIndexOf('.');
        if(index > 0){
            n = n.substring(0, index - 1);
        }

        return n;
    }

    private BirthmarkSet[] extractFromClass(ClassFileArchive[] archives, BirthmarkExtractor[] extractors, BirthmarkEnvironment environment) throws IOException, BirthmarkExtractionFailedException{
        List<BirthmarkSet> list = new ArrayList<BirthmarkSet>();

        for(ClassFileArchive archive: archives){
            for(ClassFileEntry entry: archive){
                try{
                    BirthmarkSet birthmarkset = new BirthmarkSet(entry.getClassName(), entry.getLocation());
                    list.add(birthmarkset);
                    byte[] data = inputStreamToByteArray(entry.getLocation().openStream());
                    for(BirthmarkExtractor extractor: extractors){
                        if(extractor.isAcceptable(ExtractionUnit.CLASS)){
                            Birthmark b = extractor.extract(new ByteArrayInputStream(data), environment);
                            birthmarkset.addBirthmark(b);
                        }
                    }
                } catch(IOException e){
                    warnings.addMessage(e, entry.getClassName());
                }
            }
        }
        return list.toArray(new BirthmarkSet[list.size()]);
    }

    private BirthmarkSet[] extractFromProduct(ClassFileArchive[] archives, BirthmarkExtractor[] extractors, BirthmarkEnvironment environment) throws IOException, BirthmarkExtractionFailedException{
        List<BirthmarkSet> list = new ArrayList<BirthmarkSet>();

        for(ClassFileArchive archive: archives){
            BirthmarkSet birthmarkset = new BirthmarkSet(archive.getName(), archive.getLocation());
            list.add(birthmarkset);

            for(ClassFileEntry entry: archive){
                try{
                    byte[] data = inputStreamToByteArray(entry.getLocation().openStream());
                    for(BirthmarkExtractor extractor: extractors){
                        if(extractor.isAcceptable(ExtractionUnit.ARCHIVE)){
                            Birthmark b = birthmarkset.getBirthmark(extractor.getProvider().getType());
                            if(b == null){
                                b = extractor.createBirthmark();
                                birthmarkset.addBirthmark(b);
                            }
                            extractor.extract(b, new ByteArrayInputStream(data), environment);
                        }
                    }
                } catch(IOException e){
                    warnings.addMessage(e, entry.getClassName());
                }
            }
        }
        for(Iterator<BirthmarkSet> i = list.iterator(); i.hasNext(); ){
            BirthmarkSet set = i.next();
            if(set.getBirthmarksCount() == 0){
                i.remove();
            }
        }

        return list.toArray(new BirthmarkSet[list.size()]);
    }

    private ClassFileArchive[] createArchives(String[] files, BirthmarkEnvironment environment) throws IOException, MalformedURLException{
        ClasspathContext bytecode = environment.getClasspathContext();
        List<ClassFileArchive> archives = new ArrayList<ClassFileArchive>();
        for(int i = 0; i < files.length; i++){
            try{
                if(files[i].endsWith(".class")){
                    archives.add(new DefaultClassFileArchive(files[i]));
                }
                else if(files[i].endsWith(".jar") || files[i].endsWith(".zip")){
                    archives.add(new JarClassFileArchive(files[i]));
                    bytecode.addClasspath(new File(files[i]).toURI().toURL());
                }
                else if(files[i].endsWith(".war")){
                    archives.add(new WarClassFileArchive(files[i]));
                }
            } catch(IOException e){
                warnings.addMessage(e, files[i]);
            }
        }
        return archives.toArray(new ClassFileArchive[archives.size()]);
    }
}
