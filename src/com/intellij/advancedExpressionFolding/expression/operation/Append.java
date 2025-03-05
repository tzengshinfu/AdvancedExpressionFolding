package com.intellij.advancedExpressionFolding.expression.operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.intellij.advancedExpressionFolding.expression.Expression;
import com.intellij.advancedExpressionFolding.expression.StringLiteral;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

/**
 * TODO: sb.append(interpolatedString).append(x) - merge x into interpolatedString
 * TODO: merge multiple sb.append() into a single append(interpolatedString)
 */
public class Append extends Operation {
    private boolean assign;

    public Append(@NotNull PsiElement element, @NotNull TextRange textRange, @NotNull List<Expression> operands,
                  boolean assign) {
        super(element, textRange, "+", 10, operands);
        this.assign = assign;
    }

    @Override
    public boolean isCollapsedByDefault() {
        if (!super.isCollapsedByDefault()) {
            return false;
        }
        for (Expression operand : operands) {
            if (operand instanceof Add && ((Add) operand).getOperands().stream()
                    .anyMatch(o -> !(o instanceof StringLiteral))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean supportsFoldRegions(@NotNull Document document, @Nullable Expression parent) {
        return super.supportsFoldRegions(document, parent) & operands.size() > 0;
    }

    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement element, @NotNull Document document, @Nullable Expression parent) {
        // TODO: Generalize this code for Operation
        FoldingGroup group = FoldingGroup.newGroup(Append.class.getName() +
                (operands.size() == 1 ? HIGHLIGHTED_GROUP_POSTFIX : ""));
        ArrayList<FoldingDescriptor> descriptors = new ArrayList<>();
        Expression a = operands.get(0);
        if (element.getTextRange().getStartOffset() < a.getTextRange().getStartOffset()) {
            TextRange range = TextRange.create(
                    element.getTextRange().getStartOffset(),
                    a.getTextRange().getStartOffset() -
                            (a.isLeftOverflow() ? 1 : 0)
            );
            if (!range.isEmpty()) {
                descriptors.add(new FoldingDescriptor(element.getNode(),
                        range, group, ""));
            }
        }
        if (a.supportsFoldRegions(document, this)) {
            if (a.isOverflow()) {
                Collections.addAll(descriptors, a.buildFoldRegions(a.getElement(), document, this,
                        group, "", ""));
            } else {
                Collections.addAll(descriptors, a.buildFoldRegions(a.getElement(), document, this));
            }
        }
        if (operands.size() > 1) {
            for (int i = 0; i < operands.size() - 1; i++) {
                Expression b = operands.get(i);
                Expression c = operands.get(i + 1);
                if (c.supportsFoldRegions(document, this)) {
                    if (c.isOverflow()) {
                        Collections.addAll(descriptors, c.buildFoldRegions(c.getElement(), document, this,
                                group, "", ""));
                    } else {
                        Collections.addAll(descriptors, c.buildFoldRegions(c.getElement(), document, this));
                    }
                }
                descriptors.add(new FoldingDescriptor(element.getNode(),
                        TextRange.create(
                                b.getTextRange().getEndOffset() +
                                        (b.isRightOverflow() ? 1 : 0),
                                c.getTextRange().getStartOffset() -
                                        (c.isLeftOverflow() ? 1 : 0)
                        ), group, i == 0 && assign ? " += " : " + "));
            }
        }
        Expression d = operands.get(operands.size() - 1);
        if (d.getTextRange().getEndOffset() < element.getTextRange().getEndOffset()) {
            TextRange range = TextRange.create(
                    d.getTextRange().getEndOffset() +
                            (d.isRightOverflow() ? 1 : 0),
                    element.getTextRange().getEndOffset()
            );
            if (!range.isEmpty()) {
                descriptors.add(new FoldingDescriptor(element.getNode(),
                        range, group, ""));
            }
        }
        return descriptors.toArray(FoldingDescriptor.EMPTY);
    }

    @Override
    public boolean isHighlighted() {
        return operands.size() == 1;
    }
}
