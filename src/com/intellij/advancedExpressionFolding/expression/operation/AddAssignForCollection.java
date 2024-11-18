package com.intellij.advancedExpressionFolding.expression.operation;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.intellij.advancedExpressionFolding.expression.Expression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

public class AddAssignForCollection extends Operation {
    public AddAssignForCollection(@NotNull PsiElement element, @NotNull TextRange textRange, @NotNull List<Expression> operands) {
        super(element, textRange, "+=", 300, operands);
    }
}
