package com.intellij.advancedExpressionFolding.expression.operation;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.intellij.advancedExpressionFolding.expression.ArithmeticExpression;
import com.intellij.advancedExpressionFolding.expression.Expression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

public class Add extends Operation implements ArithmeticExpression {
    public Add(@NotNull PsiElement element, @NotNull TextRange textRange, @NotNull List<Expression> operands) {
        super(element, textRange, "+", 10, operands);
    }
}
