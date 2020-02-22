# tranX-plugin
A plugin for code generation in PyCharm/IntelliJ using tranX

## Development
- Clone this repository
- Open IntelliJ IDEA IDE software, File - Open, select the repo directory.
- The IDE will automatically identify this as a Gradle project (through **build.gradle** file, see https://github.com/JetBrains/gradle-intellij-plugin/). Please enable Gradle auto-reimport if prompted so that modifications to **build.gradle** file will be automatically updated.
- To test run the plugin in a sandboxed PyCharm IDE instance, open "Gradle" panel on the right side of the IDE. Find "Tasks - intellij - runIde" and double click to run it.
- To build and package the plugin into a jar, use "Tasks - intellij - buildPlugin" instead.

## Usage
- In the PyCharm IDE, go to File - Settings, find "TranX plugin setting" under "Tools" category. Make sure to enter a unique user name for identification.
- Create a new Python project and in the editor, press Alt-Ctrl-G or click "Ask question" in the right click context menu.
- After editing the code snippet, make sure the cursor is currently within to range of the auto-generated code block. Press Alt-G to upload edits.
