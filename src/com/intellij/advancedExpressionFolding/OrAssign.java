package com.intellij.advancedExpressionFolding;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

import java.util.List;

public class OrAssign extends Operation implements ArithmeticExpression {
    public OrAssign(PsiElement element, TextRange textRange, List<Expression> operands) {
        super(element, textRange, "|=", 300, operands);
    }

    @Override
    protected Operation copy(List<Expression> newOperands) {
        return new OrAssign(element, textRange, newOperands);
    }
}
