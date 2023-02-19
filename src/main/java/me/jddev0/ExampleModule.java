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

		List<DataObject> funcPrintlnArgs = new ArrayList<>();
		funcPrintlnArgs.add(new DataObject("Hello world! From this module!"));

		callFunctionPointer(funcPrintln, funcPrintlnArgs, SCOPE_ID);

		//Calling a predefined function (Alternate way)
		List<DataObject> funcPrintlnArgs2 = new ArrayList<>();
		funcPrintlnArgs2.add(new DataObject("Another print statement."));
		callPredefinedFunction("println", funcPrintlnArgs2, SCOPE_ID);
		
		exportFunction("exampleFunction", (argumentList, INNER_SCOPE_ID) -> {
			List<DataObject> innerCombinedArgs = LangUtils.combineArgumentsWithoutArgumentSeparators(argumentList);

			System.out.println("exampleFunction was called with " + innerCombinedArgs);

			return new DataObject().setInt(innerCombinedArgs.size());
		});

		exportNormalVariable("testVar", new DataObject("This is a test variable provided by the \"" + lmc.getName() + "\" module!"));
		exportNormalVariable("intVar", new DataObject().setInt(-42));
		exportNormalVariableFinal("finalVar", new DataObject().setInt(-42));
		exportCollectionVariable("values", new DataObject().setArray(new DataObject[] {
				new DataObject("firstVar"), new DataObject().setBoolean(true), new DataObject().setError(new DataObject.ErrorObject(LangInterpreter.InterpretingError.DIV_BY_ZERO))
		}));
		exportCollectionVariable("listOfValues", new DataObject().setList(new LinkedList<>(Arrays.asList(
				new DataObject("Test variable"), new DataObject().setNull()
		))));
		exportCollectionVariableFinal("finalValues", new DataObject().setArray(new DataObject[] {
				new DataObject().setChar('a'), new DataObject().setBoolean(false)
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

			return new DataObject().setInt(aNum.intValue() * bNum.intValue() + cNum.intValue() * cNum.intValue());
		})));
		exportFunctionPointerVariableFinal("finalFunc", new DataObject().setFunctionPointer(new FunctionPointerObject((interpreter, argumentList, INNER_SCOPE_ID) -> {
			return new DataObject().setInt(-42);
		})));

		DataObject linkerInclude = getPredefinedFunctionAsDataObject("include");

		List<DataObject> linkerIncludeArgs = new ArrayList<>();
		linkerIncludeArgs.add(new DataObject("lib.lang"));
		linkerIncludeArgs.add(new DataObject("Argument1 from ExampleModule"));
		linkerIncludeArgs = LangUtils.separateArgumentsWithArgumentSeparators(linkerIncludeArgs);

		DataObject ret = callFunctionPointer(linkerInclude, linkerIncludeArgs, SCOPE_ID);

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

		System.out.println("Test calling fn.exampleFunction!");

		return new DataObject().setArray(new DataObject[] {
				new DataObject("Example module return value (the value at index 1 is from lib.lang)"),
				ret
		});
	}
	
	@Override
	public DataObject unload(List<DataObject> args, final int SCOPE_ID) {
		System.out.println("ExampleModule is unloading...");
		
		return new DataObject("Good bye!");
	}

	private void printModuleInformation(LangInterpreterInterface lii, final int SCOPE_ID) {
		System.out.println("Module-Data:");
		System.out.println("    File: " + module.getFile());
		System.out.println("    Load: " + module.isLoad());
		System.out.println();

		LangModuleConfiguration lmc = module.getLangModuleConfiguration();

		System.out.println("Lang Module configuration:");
		System.out.println("    Name              : " + lmc.getName());
		System.out.println("    Min ver           : " + lmc.getMinSupportedVersion());
		System.out.println("    Max ver           : " + lmc.getMaxSupportedVersion());
		System.out.println("    Native entry point: " + lmc.getNativeEntryPoint());
		System.out.println("    Type              : " + lmc.getModuleType());
		System.out.println();

		System.out.println("Module Path: " + lii.getData(SCOPE_ID).var.get("$LANG_MODULE_PATH"));
		System.out.println("Module File: " + lii.getData(SCOPE_ID).var.get("$LANG_MODULE_FILE"));
	}
}