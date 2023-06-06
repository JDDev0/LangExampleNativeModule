# Lang Example Native Module
This is an example Lang native module for the Standard Lang implementation of Lang

## Git tags
The git tags starting with `lang-` are used to mark the last commit of this repository which is still compatible with the standard Lang release with that version number.
Every commit after a tag requires either the next standard Lang version or if no newer version was released, the latest state of the standard Lang repository (= newest commit).

## Setup & Compile
1. Download the code from the [standard lang repository](https://github.com/JDDev0/lang)
2. Compile as jar
3. Create the lang folder in the project root and copy the jar file into this folder as lang.jar
4. Run the gradle `buildLangModule` task
5. The `module.lm` module file will be saved in `/build/libs`

## Structure
- Java code is stored in `/src/main/java`
- Lang code is stored in `/src/main/langmodule`