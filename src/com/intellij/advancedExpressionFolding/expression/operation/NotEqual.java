package com.intellij.advancedExpressionFolding.expression.operation;

import com.intellij.advancedExpressionFolding.expression.Expression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

import java.util.List;

public class NotEqual extends Operation {
    public NotEqual(PsiElement element, TextRange textRange, List<Expression> operands) {
        super(element, textRange, "≢", 18, operands);
    }
}
