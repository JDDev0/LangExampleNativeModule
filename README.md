# LangExampleNativeModule
This is an example Lang native module for the Standard Lang implementation for lang

## Setup & Compile
1. Download the code from the [standard lang repository](https://github.com/JDDev0/lang)
2. Compile as jar
3. Create the lang folder in the project root and copy the jar file into this folder as lang.jar
4. Run the gradle <code>buildLangModule</code> task
5. The <code>module.lm</code> module file will be saved in <code>/build/libs</code>

## Structure
- Java code is stored in <code>/src/main/java</code>
- Lang code is stored in <code>/src/main/langmodule</code>