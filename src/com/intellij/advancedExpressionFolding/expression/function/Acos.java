package com.intellij.advancedExpressionFolding.expression.function;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.intellij.advancedExpressionFolding.expression.ArithmeticExpression;
import com.intellij.advancedExpressionFolding.expression.Expression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

public class Acos extends Function implements ArithmeticExpression {
    public Acos(@NotNull PsiElement element, @NotNull TextRange textRange, @NotNull List<Expression> operands) {
        super(element, textRange, "acos", operands);
    }
}
