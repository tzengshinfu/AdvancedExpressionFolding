package com.intellij.advancedExpressionFolding;

public class AdvancedExpressionFoldingOptionsProvider extends com.intellij.openapi.options.BeanConfigurable<AdvancedExpressionFoldingSettings.State> implements com.intellij.application.options.editor.CodeFoldingOptionsProvider {
    protected AdvancedExpressionFoldingOptionsProvider() {
        super(AdvancedExpressionFoldingSettings.getInstance().getState());
        AdvancedExpressionFoldingSettings settings = AdvancedExpressionFoldingSettings.getInstance();
        checkBox("arithmeticExpressionsCollapse", "Math, BigDecimal and BigInteger expressions (deprecated)");
        checkBox("concatenationExpressionsCollapse", "StringBuilder.append and Collection.add/remove expressions, interpolated Strings and Stream expressions");
        checkBox("slicingExpressionsCollapse", "List.subList and String.substring expressions");
        checkBox("comparingExpressionsCollapse", "Object.equals and Comparable.compareTo expressions");
        checkBox("getExpressionsCollapse", "List.get, List.set, Map.get and Map.put expressions, array and list literals");
        checkBox("rangeExpressionsCollapse", "For loops, range expressions");
        checkBox("checkExpressionsCollapse", "Null safe calls");
        checkBox("castExpressionsCollapse", "Type cast expressions");
        checkBox("varExpressionsCollapse", "Variable declarations");
        checkBox("getSetExpressionsCollapse", "Getters and setters");
        checkBox("controlFlowSingleStatementCodeBlockCollapse", "Control flow single-statement code block braces (read-only files)");
        checkBox("controlFlowMultiStatementCodeBlockCollapse", "Control flow multi-statement code block braces (read-only files, deprecated)");
        checkBox("compactControlFlowSyntaxCollapse", "Compact control flow condition syntax");
        checkBox("semicolonsCollapse", "Semicolons (read-only files)");
        checkBox("assertsCollapse", "Asserts");
        checkBox("Math, BigDecimal and BigInteger expressions (deprecated)", () -> settings.getState().isArithmeticExpressionsCollapse(), aBoolean -> settings.getState().setArithmeticExpressionsCollapse(aBoolean));
        checkBox("StringBuilder.append and Collection.add/remove expressions, interpolated Strings and Stream expressions", () -> settings.getState().isConcatenationExpressionsCollapse(), aBoolean -> settings.getState().setConcatenationExpressionsCollapse(aBoolean));
        checkBox("List.subList and String.substring expressions", () -> settings.getState().isSlicingExpressionsCollapse(), aBoolean -> settings.getState().setSlicingExpressionsCollapse(aBoolean));
        checkBox("Object.equals and Comparable.compareTo expressions", () -> settings.getState().isComparingExpressionsCollapse(), aBoolean -> settings.getState().setComparingExpressionsCollapse(aBoolean));
        checkBox("List.get, List.set, Map.get and Map.put expressions, array and list literals", () -> settings.getState().isGetExpressionsCollapse(), aBoolean -> settings.getState().setGetExpressionsCollapse(aBoolean));
        checkBox("For loops, range expressions", () -> settings.getState().isRangeExpressionsCollapse(), aBoolean -> settings.getState().setRangeExpressionsCollapse(aBoolean));
        checkBox("Null safe calls", () -> settings.getState().isCheckExpressionsCollapse(), aBoolean -> settings.getState().setCheckExpressionsCollapse(aBoolean));
        checkBox("Type cast expressions", () -> settings.getState().isCastExpressionsCollapse(), aBoolean -> settings.getState().setCastExpressionsCollapse(aBoolean));
        checkBox("Variable declarations", () -> settings.getState().isVarExpressionsCollapse(), aBoolean -> settings.getState().setVarExpressionsCollapse(aBoolean));
        checkBox("Getters and setters", () -> settings.getState().isGetSetExpressionsCollapse(), aBoolean -> settings.getState().setGetSetExpressionsCollapse(aBoolean));
        checkBox("Control flow single-statement code block braces (read-only files)", () -> settings.getState().isControlFlowSingleStatementCodeBlockCollapse(), aBoolean -> settings.getState().setControlFlowSingleStatementCodeBlockCollapse(aBoolean));
        checkBox("Control flow multi-statement code block braces (read-only files, deprecated)", () -> settings.getState().isControlFlowMultiStatementCodeBlockCollapse(), aBoolean -> settings.getState().setControlFlowMultiStatementCodeBlockCollapse(aBoolean));
        checkBox("Compact control flow condition syntax", () -> settings.getState().isCompactControlFlowSyntaxCollapse(), aBoolean -> settings.getState().setCompactControlFlowSyntaxCollapse(aBoolean));
        checkBox("Semicolons (read-only files)", () -> settings.getState().isSemicolonsCollapse(), aBoolean -> settings.getState().setSemicolonsCollapse(aBoolean));
        checkBox("Asserts", () -> settings.getState().isAssertsCollapse(), aBoolean -> settings.getState().setAssertsCollapse(aBoolean));
    }
}
