package com.intellij.advancedExpressionFolding.expression;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;

public class Get extends Expression {
    private final @NotNull
    Expression object;
    private final @NotNull
    Expression key;
    private final @NotNull
    Style style;

    public enum Style {
        NORMAL,
        /*FIRST,*/
        LAST
    }

    public Get(@NotNull PsiElement element, @NotNull TextRange textRange, @NotNull Expression object, @NotNull Expression key, @NotNull Style style) {
        super(element, textRange);
        this.object = object;
        this.key = key;
        this.style = style;
    }

    @Override
    public boolean supportsFoldRegions(@NotNull Document document,
                                       @Nullable Expression parent) {
        return style == Style.NORMAL && object.getTextRange().getEndOffset() < key.getTextRange().getStartOffset()
                && key.getTextRange().getEndOffset() < getTextRange().getEndOffset() ||
                style != Style.NORMAL && object.getTextRange().getEndOffset() < key.getTextRange().getStartOffset() - 1
                        && key.getTextRange().getStartOffset() < key.getTextRange().getEndOffset();
    }

    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement element, @NotNull Document document, @Nullable Expression parent) {
        FoldingGroup group = FoldingGroup.newGroup(Get.class.getName());
        ArrayList<FoldingDescriptor> descriptors = new ArrayList<>();
        if (style == Style.NORMAL) {
            descriptors.add(new FoldingDescriptor(element.getNode(),
                    TextRange.create(object.getTextRange().getEndOffset(),
                            key.getTextRange().getStartOffset()), group, "["));
            descriptors.add(new FoldingDescriptor(element.getNode(),
                    TextRange.create(key.getTextRange().getEndOffset(),
                            getTextRange().getEndOffset()), group, "]"));
        } else {
            descriptors.add(new FoldingDescriptor(element.getNode(),
                    TextRange.create(object.getTextRange().getEndOffset(),
                            key.getTextRange().getStartOffset() - 1), group, "." + (/*style == Style.FIRST ? "first" : */"last")));
            descriptors.add(new FoldingDescriptor(element.getNode(),
                    TextRange.create(key.getTextRange().getStartOffset(),
                            key.getTextRange().getEndOffset()), group, ""));
        }
        // TODO: Generalize it
        if (object.supportsFoldRegions(document, this)) {
            Collections.addAll(descriptors, object.buildFoldRegions(object.getElement(), document, this));
        }
        if (style == Style.NORMAL) {
            if (key.supportsFoldRegions(document, this)) {
                Collections.addAll(descriptors, key.buildFoldRegions(key.getElement(), document, this));
            }
        }
        return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY);
    }
}
