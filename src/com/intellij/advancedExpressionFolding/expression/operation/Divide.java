package com.intellij.advancedExpressionFolding.expression.operation;

import com.intellij.advancedExpressionFolding.expression.ArithmeticExpression;
import com.intellij.advancedExpressionFolding.expression.Expression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

import java.util.List;

public class Divide extends Operation implements ArithmeticExpression {
    public Divide(PsiElement element, TextRange textRange, List<Expression> operands) {
        super(element, textRange, "/", 100, operands);
    }
}
