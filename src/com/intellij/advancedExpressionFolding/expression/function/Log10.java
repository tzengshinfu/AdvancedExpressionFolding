package com.intellij.advancedExpressionFolding.expression.function;

import com.intellij.advancedExpressionFolding.expression.ArithmeticExpression;
import com.intellij.advancedExpressionFolding.expression.Expression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

import java.util.List;

public class Log10 extends Function implements ArithmeticExpression {
    public Log10(PsiElement element, TextRange textRange, List<Expression> operands) {
        super(element, textRange, "log", operands);
    }
}
