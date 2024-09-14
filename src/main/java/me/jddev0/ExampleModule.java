package me.jddev0;

import java.util.*;

import at.jddev0.lang.*;
import at.jddev0.lang.DataObject.FunctionPointerObject;
import at.jddev0.lang.LangInterpreter.LangInterpreterInterface;

import static at.jddev0.lang.LangFunction.*;
import static at.jddev0.lang.LangFunction.LangParameter.*;

public class ExampleModule extends LangNativeModule {
    @Override
    public DataObject load(List<DataObject> args, final int SCOPE_ID) {
        LangInterpreterInterface lii = new LangInterpreterInterface(interpreter);
        LangModuleConfiguration lmc = module.getLangModuleConfiguration();

        System.out.println("ExampleModule is loading...");
        System.out.println();

        printModuleInformation(lii, SCOPE_ID);
        System.out.println();

        List<DataObject> combinedArgs = LangUtils.combineArgumentsWithoutArgumentSeparators(args);

        System.out.println("Load args:");
        for(DataObject arg:combinedArgs)
            System.out.println("    " + arg);
        System.out.println();

        //Calling a predefined function
        DataObject funcPrintln = getPredefinedFunctionAsDataObject("println");
        callFunctionPointer(funcPrintln, Arrays.asList(
                new DataObject("Hello world! From this module!")
        ), SCOPE_ID);

        //Calling a predefined function (Alternate way)
        callPredefinedFunction("println", Arrays.asList(
                createDataObject("Another print statement.") //createDataObject can be used instead of new DataObject().set...()
        ), SCOPE_ID);

        exportNativeFunction(new Object() {
            @LangFunction("exampleFunction")
            public DataObject exampleFunctionFunction(
                    int SCOPE_ID,
                    @LangParameter("&args") @LangParameter.VarArgs List<DataObject> args
            ) {
                System.out.println("exampleFunction was called with " + args);

                return createDataObject(args.size());
            }
        });

        exportNormalVariable("testVar", createDataObject("This is a test variable provided by the \"" + lmc.getName() + "\" module!"));
        exportNormalVariable("intVar", createDataObject(-42));
        exportNormalVariableFinal("finalVar", createDataObject(-42));
        exportCompositeVariable("values", createDataObject(new DataObject[] {
                createDataObject("firstVar"), createDataObject(true), createDataObject(new DataObject.ErrorObject(LangInterpreter.InterpretingError.DIV_BY_ZERO))
        }));
        exportCompositeVariable("listOfValues", new DataObject().setList(new LinkedList<>(Arrays.asList(
                createDataObject("Test variable"), new DataObject().setNull()
        ))));
        exportCompositeVariableFinal("finalValues", new DataObject().setArray(new DataObject[] {
                createDataObject('a'), createDataObject(false)
        }));
        exportFunctionPointerVariable("calc", new DataObject().setFunctionPointer(new FunctionPointerObject(
                LangNativeFunction.getSingleLangFunctionFromObject(new Object() {
                    @LangFunction("calc")
                    public DataObject calcFunction(
                            int SCOPE_ID,
                            @LangParameter("$a") @NumberValue Number aNum,
                            @LangParameter("$b") @NumberValue Number bNum,
                            @LangParameter("$c") @NumberValue Number cNum
                    ) {
                        return createDataObject(aNum.intValue() * bNum.intValue() + cNum.intValue() * cNum.intValue());
                    }
                }))));
        exportFunctionPointerVariableFinal("finalFunc", new DataObject().setFunctionPointer(new FunctionPointerObject(
                LangNativeFunction.getSingleLangFunctionFromObject(new Object() {
                    @LangFunction("finalFunc")
                    public DataObject finalFuncFunction(
                            LangInterpreter interpreter, int SCOPE_ID,
                            @LangParameter("&args") @RawVarArgs List<DataObject> ignore
                    ) {
                        return createDataObject(-42);
                    }
                }))));

        DataObject ret = callPredefinedFunction("include", LangUtils.separateArgumentsWithArgumentSeparators(
                Arrays.asList(
                        createDataObject("lib.lang"),
                        createDataObject("Argument1 from ExampleModule")
                )
        ), SCOPE_ID);

        final DataObject.DataTypeConstraint TYPE_CONSTRAINT_OPTIONAL_TEXT = DataObject.DataTypeConstraint.fromAllowedTypes(Arrays.asList(
                DataObject.DataType.NULL, DataObject.DataType.TEXT
        ));

        final DataObject.DataTypeConstraint TYPE_CONSTRAINT_OPTIONAL_DOUBLE = DataObject.DataTypeConstraint.fromAllowedTypes(Arrays.asList(
                DataObject.DataType.NULL, DataObject.DataType.DOUBLE
        ));

        //Structs
        DataObject.StructObject exampleStruct = new DataObject.StructObject(new String[] {
                "$val",
                "$text",
                "$double"
        }, new DataObject.DataTypeConstraint[] {
                DataObject.CONSTRAINT_NORMAL,
                TYPE_CONSTRAINT_OPTIONAL_TEXT,
                TYPE_CONSTRAINT_OPTIONAL_DOUBLE
        });
        exportCompositeVariable("ExampleStruct", new DataObject().setStruct(exampleStruct));

        System.out.println("Member names of &ExampleStruct:");
        for(String memberName:exampleStruct.getMemberNames())
            System.out.println("    " + memberName);
        System.out.println();

        System.out.println("Index of the $val member in &ExampleStruct: " + exampleStruct.getIndexOfMember("$val"));
        System.out.println();

        System.out.println("Type constraints of &ExampleStruct members:");
        for(String memberName:exampleStruct.getMemberNames())
            System.out.println("    " + memberName + exampleStruct.getTypeConstraints()[exampleStruct.getIndexOfMember(memberName)].toTypeConstraintSyntax());

        DataObject.StructObject exampleStructInstance = new DataObject.StructObject(exampleStruct, new DataObject[] {
                createDataObject("A text value"),
                createDataObject("Another text"),
                createDataObject(42.42)
        });
        exportCompositeVariable("exampleStructInstance", new DataObject().setStruct(exampleStructInstance));

        System.out.println("Member names and values of &exampleStructInstance:");
        for(String memberName:exampleStructInstance.getMemberNames())
            System.out.println("    " + memberName + " = " + exampleStructInstance.getMember(memberName).toText());
        System.out.println();

        //Accessing exported module variables within the module (The module name must be used [Use lmc.getName()])
        Map<String, DataObject> exportedVars = lii.getModuleExportedVariables(lmc.getName());
        if(exportedVars != null) {
            System.out.println("Exported variables:");
            for (Map.Entry<String, DataObject> varEntry : exportedVars.entrySet()) {
                System.out.println("  -> " + varEntry.getKey() + ": " + varEntry.getValue());
            }

            DataObject intFromLibDataObject = exportedVars.get("$intFromLib");
            if(intFromLibDataObject != null) {
                System.out.println("$intFromLib[" + intFromLibDataObject.getType() + "]: " + intFromLibDataObject);
            }
        }

        exportNativeFunction(new Object() {
            @LangFunction(value="testConvertToDataObject", hasInfo=true)
            public DataObject testConvertToDataObjectFunction(
                    int SCOPE_ID
            ) {
                callPredefinedFunction("println", Arrays.asList(
                        createDataObject("Call \"func.testConvertToDataObject()\" with 0 for normal output " +
                                "or with 1 for debug output using \"func.printDebug()\" [1 Will only work in the LangShell]")
                ), SCOPE_ID);

                return null;
            }

            @LangFunction("testConvertToDataObject")
            public DataObject testConvertToDataObjectFunction(
                    int SCOPE_ID,
                    @LangParameter("usePrintDebug") @BooleanValue boolean usePrintDebug
            ) {
                printDataObjectInformation(convertToDataObject(null), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject("text"), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject(new Object[] {
                        2, "test", null, Character.class
                }), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject(new byte[] {
                        2, -1, (byte)255, 127, -128, 0, (byte)'A', (byte)'B', (byte)'µ', (byte)1555
                }), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject(42), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject(true), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject(42.2f), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject(42.2), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject('a'), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject(new RuntimeException("A custom error")), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject(new IllegalStateException("Another error")), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject(Integer.class), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject(Boolean.class), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject(Long.class), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject(Class.class), usePrintDebug, SCOPE_ID);
                printDataObjectInformation(convertToDataObject(Object[].class), usePrintDebug, SCOPE_ID); //DataType.ARRAY
                printDataObjectInformation(convertToDataObject(DataObject[].class), usePrintDebug, SCOPE_ID); //DataType.ARRAY
                printDataObjectInformation(convertToDataObject(Byte[].class), usePrintDebug, SCOPE_ID); //DataType.ARRAY
                printDataObjectInformation(convertToDataObject(byte[].class), usePrintDebug, SCOPE_ID); //ByteBuffer.ARRAY

                return null;
            }
        });

        System.out.println("Test convertToDataObject() by calling \"func.testConvertToDataObject()\"");

        System.out.println("Test calling fn.exampleFunction!");

        return createDataObject(new DataObject[] {
                createDataObject("Example module return value (the value at index 1 is from lib.lang)"),
                ret
        });
    }

    @Override
    public DataObject unload(List<DataObject> args, final int SCOPE_ID) {
        System.out.println("ExampleModule is unloading...");

        return createDataObject("Good bye!");
    }

    private void printModuleInformation(LangInterpreterInterface lii, final int SCOPE_ID) {
        System.out.println("Module-Data:");
        System.out.println("    File: " + module.getFile());
        System.out.println("    Load: " + module.isLoad());
        System.out.println();

        LangModuleConfiguration lmc = module.getLangModuleConfiguration();

        System.out.println("Lang Module configuration:");
        System.out.println("    Name              : " + lmc.getName());
        System.out.println("    Description       : " + lmc.getDescription());
        System.out.println("    Version           : " + lmc.getVersion());
        System.out.println("    Min ver           : " + lmc.getMinSupportedVersion());
        System.out.println("    Max ver           : " + lmc.getMaxSupportedVersion());
        System.out.println("    Native entry point: " + lmc.getNativeEntryPoint());
        System.out.println("    Type              : " + lmc.getModuleType());
        System.out.println();

        System.out.println("Module Path: " + lii.getData(SCOPE_ID).var.get("$LANG_MODULE_PATH"));
        System.out.println("Module File: " + lii.getData(SCOPE_ID).var.get("$LANG_MODULE_FILE"));
    }

    private void printDataObjectInformation(DataObject dataObject, boolean useLangShellsPrintDebug, final int SCOPE_ID) {
        if(useLangShellsPrintDebug) {
            DataObject printDebugFunc = getPredefinedFunctionAsDataObject("printDebug");
            if(printDebugFunc == null) {
                throwError(LangInterpreter.InterpretingError.INVALID_ARGUMENTS, "func.printDebug() can only be used inside the LangShell", SCOPE_ID);

                return;
            }

            callFunctionPointer(printDebugFunc, Arrays.asList(
                    dataObject
            ), SCOPE_ID);
            callPredefinedFunction("println", new LinkedList<>(), SCOPE_ID);

            return;
        }

        System.out.println("DataObject:");
        System.out.println("    Type: " + dataObject.getType());
        System.out.println("    String representation: " + dataObject);
        System.out.println();
    }
}