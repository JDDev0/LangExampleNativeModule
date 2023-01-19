package me.jddev0;

import java.util.ArrayList;
import java.util.List;

import me.jddev0.module.lang.*;
import me.jddev0.module.lang.DataObject.FunctionPointerObject;
import me.jddev0.module.lang.LangInterpreter.LangInterpreterInterface;

public class ExampleModule extends LangNativeModule {
	@Override
	public DataObject load(List<DataObject> args, final int SCOPE_ID) {
		LangInterpreterInterface lii = new LangInterpreterInterface(interpreter);

		System.out.println("ExampleModule is loading...");
		System.out.println();

		printModuleInformation(lii, SCOPE_ID);
		System.out.println();

		List<DataObject> combinedArgs = LangUtils.combineArgumentsWithoutArgumentSeparators(args);
		
		System.out.println("Load args:");
		for(DataObject arg:combinedArgs)
			System.out.println("    " + arg);
		System.out.println();
		
		LangPredefinedFunctionObject funcPrintln = lii.getPredefinedFunctions().get("println");

		List<DataObject> funcPrintlnArgs = new ArrayList<>();
		funcPrintlnArgs.add(new DataObject("Hello world! From this module!"));

		lii.callFunctionPointer(new FunctionPointerObject(funcPrintln), "println", funcPrintlnArgs, SCOPE_ID);
		
		exportFunction("exampleFunction", (argumentList, INNER_SCOPE_ID) -> {
			List<DataObject> innerCombinedArgs = LangUtils.combineArgumentsWithoutArgumentSeparators(argumentList);

			System.out.println("exampleFunction was called with " + innerCombinedArgs);

			return new DataObject().setInt(innerCombinedArgs.size());
		});

		LangPredefinedFunctionObject linkerInclude = lii.getPredefinedFunctions().get("include");

		List<DataObject> linkerIncludeArgs = new ArrayList<>();
		linkerIncludeArgs.add(new DataObject("lib.lang"));
		linkerIncludeArgs.add(new DataObject("Argument1 from ExampleModule"));
		linkerIncludeArgs = LangUtils.separateArgumentsWithArgumentSeparators(linkerIncludeArgs);

		DataObject ret = lii.callFunctionPointer(new FunctionPointerObject(linkerInclude), "include", linkerIncludeArgs, SCOPE_ID);

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