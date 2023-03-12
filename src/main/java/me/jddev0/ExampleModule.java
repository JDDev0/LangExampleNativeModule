package me.jddev0;

import java.util.*;

import me.jddev0.module.lang.*;
import me.jddev0.module.lang.DataObject.FunctionPointerObject;
import me.jddev0.module.lang.LangInterpreter.LangInterpreterInterface;

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
		
		exportFunction("exampleFunction", (argumentList, INNER_SCOPE_ID) -> {
			List<DataObject> innerCombinedArgs = LangUtils.combineArgumentsWithoutArgumentSeparators(argumentList);

			System.out.println("exampleFunction was called with " + innerCombinedArgs);

			return createDataObject(innerCombinedArgs.size());
		});

		exportNormalVariable("testVar", createDataObject("This is a test variable provided by the \"" + lmc.getName() + "\" module!"));
		exportNormalVariable("intVar", createDataObject(-42));
		exportNormalVariableFinal("finalVar", createDataObject(-42));
		exportCollectionVariable("values", createDataObject(new DataObject[] {
				createDataObject("firstVar"), createDataObject(true), createDataObject(new DataObject.ErrorObject(LangInterpreter.InterpretingError.DIV_BY_ZERO))
		}));
		exportCollectionVariable("listOfValues", new DataObject().setList(new LinkedList<>(Arrays.asList(
				createDataObject("Test variable"), new DataObject().setNull()
		))));
		exportCollectionVariableFinal("finalValues", new DataObject().setArray(new DataObject[] {
				createDataObject('a'), createDataObject(false)
		}));
		exportFunctionPointerVariable("calc", new DataObject().setFunctionPointer(new FunctionPointerObject((interpreter, argumentList, INNER_SCOPE_ID) -> {
			List<DataObject> combinedArgumentList = LangUtils.combineArgumentsWithoutArgumentSeparators(argumentList);
			if(combinedArgumentList.size() != 3)
				return new LangInterpreterInterface(interpreter).setErrnoErrorObject(LangInterpreter.InterpretingError.INVALID_ARG_COUNT, "3 arguments are needed", INNER_SCOPE_ID);

			DataObject aObj = combinedArgumentList.get(0);
			DataObject bObj = combinedArgumentList.get(1);
			DataObject cObj = combinedArgumentList.get(2);

			Number aNum = aObj.toNumber();
			if(aNum == null)
				return new LangInterpreterInterface(interpreter).setErrnoErrorObject(LangInterpreter.InterpretingError.INVALID_ARGUMENTS, "Argument 1 must be a number", INNER_SCOPE_ID);
			Number bNum = bObj.toNumber();
			if(bNum == null)
				return new LangInterpreterInterface(interpreter).setErrnoErrorObject(LangInterpreter.InterpretingError.INVALID_ARGUMENTS, "Argument 2 must be a number", INNER_SCOPE_ID);
			Number cNum = cObj.toNumber();
			if(cNum == null)
				return new LangInterpreterInterface(interpreter).setErrnoErrorObject(LangInterpreter.InterpretingError.INVALID_ARGUMENTS, "Argument 2 must be a number", INNER_SCOPE_ID);

			return createDataObject(aNum.intValue() * bNum.intValue() + cNum.intValue() * cNum.intValue());
		})));
		exportFunctionPointerVariableFinal("finalFunc", new DataObject().setFunctionPointer(new FunctionPointerObject((interpreter, argumentList, INNER_SCOPE_ID) -> {
			return createDataObject(-42);
		})));

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
		exportCollectionVariable("ExampleStruct", new DataObject().setStruct(exampleStruct));

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
		exportCollectionVariable("exampleStructInstance", new DataObject().setStruct(exampleStructInstance));

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

		exportFunction("testConvertToDataObject", (argumentList, INNER_SCOPE_ID) -> {
			List<DataObject> combinedArgumentList = LangUtils.combineArgumentsWithoutArgumentSeparators(argumentList);
			if(combinedArgumentList.size() > 1)
				return new LangInterpreterInterface(interpreter).setErrnoErrorObject(LangInterpreter.InterpretingError.INVALID_ARG_COUNT, "0 ore 1 argument(s) are needed", INNER_SCOPE_ID);

			if(combinedArgumentList.size() == 0) {
				callPredefinedFunction("println", Arrays.asList(
						createDataObject("Call \"func.testConvertToDataObject()\" with 0 for normal output or with 1 for debug output using \"func.printDebug()\" [1 Will only work in the LangShell]")
				), INNER_SCOPE_ID);

				return null;
			}

			boolean useLangShellsPrintDebug = combinedArgumentList.get(0).getBoolean();

			printDataObjectInformation(convertToDataObject(null), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject("text"), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject(new Object[] {
					2, "test", null, Character.class
			}), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject(new byte[] {
					2, -1, (byte)255, 127, -128, 0, (byte)'A', (byte)'B', (byte)'µ', (byte)1555
			}), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject(42), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject(true), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject(42.2f), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject(42.2), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject('a'), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject(new RuntimeException("A custom error")), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject(new IllegalStateException("Another error")), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject(Integer.class), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject(Boolean.class), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject(Long.class), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject(Class.class), useLangShellsPrintDebug, INNER_SCOPE_ID);
			printDataObjectInformation(convertToDataObject(Object[].class), useLangShellsPrintDebug, INNER_SCOPE_ID); //DataType.ARRAY
			printDataObjectInformation(convertToDataObject(DataObject[].class), useLangShellsPrintDebug, INNER_SCOPE_ID); //DataType.ARRAY
			printDataObjectInformation(convertToDataObject(Byte[].class), useLangShellsPrintDebug, INNER_SCOPE_ID); //DataType.ARRAY
			printDataObjectInformation(convertToDataObject(byte[].class), useLangShellsPrintDebug, INNER_SCOPE_ID); //ByteBuffer.ARRAY

			return null;
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