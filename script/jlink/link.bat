@echo off

rd /S /Q "runtime"

jlink --module-path %JAVA_HOME%\jmods --add-modules java.base,java.datatransfer,java.desktop,java.logging,java.prefs,java.security.sasl,java.xml,javafx.base,javafx.controls,javafx.graphics,javafx.media,jdk.unsupported --no-man-pages --no-header-files --strip-debug --output runtime