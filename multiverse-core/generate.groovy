import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine

@Grab(group = 'org.apache.velocity', module = 'velocity', version = '1.6.4')

class TxnClosure {
    String type
    String name
    String typeParameter
}

class TxnExecutor {
    String name
    boolean lean
}

class TransactionalObject {
    String type//the type of data it contains
    String objectType//the type of data it contains
    String initialValue//the initial value
    String typeParameter
//  String parametrizedTranlocal
    String functionClass//the class of the callable used for commuting operations
    String incFunctionMethod
    boolean isReference
    String referenceInterface
    boolean isNumber
    String predicateClass
}

VelocityEngine engine = new VelocityEngine();
engine.init();

def refs = createTransactionalObjects();
def txnClosures = createClosures();TxnClosure
def txnExecutors = [new TxnExecutor(name: 'FatGammaTxnExecutor', lean: false),
        new TxnExecutor(name: 'LeanGammaTxnExecutor', lean: true)]

generateRefFactory(engine, refs);

for (def param in refs) {
    generateRefs(engine, param)
    generatePredicate(engine, param)
    generateFunction(engine, param)
}

for (def closure in txnClosures) {
    generateTxnClosure(engine, closure)
}

generateTxnExecutor(engine, txnClosures)
//generateOrElseBlock(engine, atomicClosures)
generateGammaOrElseBlock(engine, txnClosures)
generateStmUtils(engine, txnClosures)

for (def txnExecutor in txnExecutors) {
    generateGammaTxnExecutor(engine, txnExecutor, txnClosures)
}


List<TxnClosure> createClosures() {
    def result = []
    result << new TxnClosure(
            name: 'TxnClosure',
            type: 'E',
            typeParameter: '<E>'
    )
    result << new TxnClosure(
            name: 'TxnIntClosure',
            type: 'int',
            typeParameter: ''
    )
    result << new TxnClosure(
            name: 'TxnLongClosure',
            type: 'long',
            typeParameter: ''
    )
    result << new TxnClosure(
            name: 'TxnDoubleClosure',
            type: 'double',
            typeParameter: ''
    )
    result << new TxnClosure(
            name: 'TxnBooleanClosure',
            type: 'boolean',
            typeParameter: ''
    )
    result << new TxnClosure(
            name: 'TxnVoidClosure',
            type: 'void',
            typeParameter: ''
    )
    result
}

List<TransactionalObject> createTransactionalObjects() {
    def result = []
    result.add new TransactionalObject(
            type: 'E',
            objectType: '',
            typeParameter: '<E>',
            initialValue: 'null',
            referenceInterface: 'TxnRef',
            functionClass: 'Function',
            isReference: true,
            isNumber: false,
            predicateClass: "Predicate",
            incFunctionMethod: '')
    result.add new TransactionalObject(
            type: 'int',
            objectType: 'Integer',
            referenceInterface: 'TxnInteger',
            typeParameter: '',
            initialValue: '0',
            functionClass: 'IntFunction',
            isReference: true,
            isNumber: true,
            predicateClass: "IntPredicate",
            incFunctionMethod: 'newIncIntFunction')
    result.add new TransactionalObject(
            type: 'boolean',
            objectType: 'Boolean',
            referenceInterface: 'TxnBoolean',
            typeParameter: '',
            initialValue: 'false',
            functionClass: 'BooleanFunction',
            isReference: true,
            isNumber: false,
            predicateClass: "BooleanPredicate",
            incFunctionMethod: '')
    result.add new TransactionalObject(
            type: 'double',
            objectType: 'Double',
            referenceInterface: 'TxnDouble',
            typeParameter: '',
            initialValue: '0',
            functionClass: 'DoubleFunction',
            isReference: true,
            isNumber: true,
            predicateClass: "DoublePredicate",
            incFunctionMethod: '')
    result.add new TransactionalObject(
            referenceInterface: 'TxnLong',
            type: 'long',
            objectType: 'Long',
            typeParameter: '',
            initialValue: '0',
            functionClass: 'LongFunction',
            isReference: true,
            isNumber: true,
            predicateClass: "LongPredicate",
            incFunctionMethod: 'newIncLongFunction')
    result.add new TransactionalObject(
            type: '',
            objectType: '',
            typeParameter: '',
            initialValue: '',
            functionClass: 'Function',
            referenceInterface: '',
            isReference: false,
            isNumber: false,
            predicateClass: "",
            incFunctionMethod: '')
    result
}

void generateTxnClosure(VelocityEngine engine, TxnClosure closure) {
    Template t = engine.getTemplate('src/main/java/org/multiverse/api/closures/TxnClosure.vm')

    VelocityContext context = new VelocityContext()
    context.put('closure', closure)

    StringWriter writer = new StringWriter()
    t.merge(context, writer)

    File file = new File("src/main/java/org/multiverse/api/closures/${closure.name}.java")
    file.createNewFile()
    file.text = writer.toString()
}

void generateGammaTxnExecutor(VelocityEngine engine, TxnExecutor txnExecutor, List<TxnClosure> closures) {
    Template t = engine.getTemplate('src/main/java/org/multiverse/stms/gamma/GammaTxnExecutor.vm')

    VelocityContext context = new VelocityContext()
    context.put('txnExecutor', txnExecutor)
    context.put('closures', closures)

    StringWriter writer = new StringWriter()
    t.merge(context, writer)

    File file = new File("src/main/java/org/multiverse/stms/gamma/${txnExecutor.name}.java")
    file.createNewFile()
    file.text = writer.toString()
}

void generateTxnExecutor(VelocityEngine engine, List<TxnClosure> closures) {
    Template t = engine.getTemplate('src/main/java/org/multiverse/api/TxnExecutor.vm')

    VelocityContext context = new VelocityContext()
    context.put('closures', closures)

    StringWriter writer = new StringWriter()
    t.merge(context, writer)

    File file = new File('src/main/java/org/multiverse/api/TxnExecutor.java')
    file.createNewFile()
    file.text = writer.toString()
}

void generateOrElseBlock(VelocityEngine engine, List<TxnClosure> closures) {
    Template t = engine.getTemplate('src/main/java/org/multiverse/api/OrElseBlock.vm')

    VelocityContext context = new VelocityContext()
    context.put('closures', closures)

    StringWriter writer = new StringWriter()
    t.merge(context, writer)

    File file = new File('src/main/java/org/multiverse/api/OrElseBlock.java')
    file.createNewFile()
    file.text = writer.toString()
}

void generateGammaOrElseBlock(VelocityEngine engine, List<TxnClosure> closures) {
    Template t = engine.getTemplate('src/main/java/org/multiverse/stms/gamma/GammaOrElseBlock.vm')

    VelocityContext context = new VelocityContext()
    context.put('closures', closures)

    StringWriter writer = new StringWriter()
    t.merge(context, writer)

    File file = new File('src/main/java/org/multiverse/stms/gamma/GammaOrElseBlock.java')
    file.createNewFile()
    file.text = writer.toString()
}

void generateStmUtils(VelocityEngine engine, List<TxnClosure> closures) {
    Template t = engine.getTemplate('src/main/java/org/multiverse/api/StmUtils.vm')

    VelocityContext context = new VelocityContext()
    context.put('closures', closures)

    StringWriter writer = new StringWriter()
    t.merge(context, writer)

    File file = new File('src/main/java/org/multiverse/api/StmUtils.java')
    file.createNewFile()
    file.text = writer.toString()
}

void generatePredicate(VelocityEngine engine, TransactionalObject transactionalObject) {
    if (!transactionalObject.isReference) {
        return
    }

    Template t = engine.getTemplate('src/main/java/org/multiverse/api/predicates/Predicate.vm')

    VelocityContext context = new VelocityContext()
    context.put('transactionalObject', transactionalObject)

    StringWriter writer = new StringWriter()
    t.merge(context, writer)

    File file = new File('src/main/java/org/multiverse/api/predicates/', "${transactionalObject.predicateClass}.java")
    file.createNewFile()
    file.text = writer.toString()
}

void generateFunction(VelocityEngine engine, TransactionalObject transactionalObject) {
    if (!transactionalObject.isReference) {
        return
    }

    Template t = engine.getTemplate('src/main/java/org/multiverse/api/functions/Function.vm')

    VelocityContext context = new VelocityContext()
    context.put('transactionalObject', transactionalObject)

    StringWriter writer = new StringWriter()
    t.merge(context, writer)

    File file = new File('src/main/java/org/multiverse/api/functions/', "${transactionalObject.functionClass}.java")
    file.createNewFile()
    file.text = writer.toString()
}

void generateRefs(VelocityEngine engine, TransactionalObject transactionalObject) {
    if (!transactionalObject.isReference) {
        return;
    }

    Template t = engine.getTemplate('src/main/java/org/multiverse/api/references/TxnRef.vm');

    VelocityContext context = new VelocityContext()
    context.put('transactionalObject', transactionalObject)

    StringWriter writer = new StringWriter()
    t.merge(context, writer)

    File file = new File('src/main/java/org/multiverse/api/references/', "${transactionalObject.referenceInterface}.java")
    file.createNewFile()
    file.text = writer.toString()
}

void generateRefFactory(VelocityEngine engine, List<TransactionalObject> refs) {
    Template t = engine.getTemplate('src/main/java/org/multiverse/api/references/TxnRefFactory.vm');

    VelocityContext context = new VelocityContext()
    context.put('refs', refs)

    StringWriter writer = new StringWriter()
    t.merge(context, writer)

    File file = new File('src/main/java/org/multiverse/api/references/TxnRefFactory.java')
    file.createNewFile()
    file.text = writer.toString()
}

