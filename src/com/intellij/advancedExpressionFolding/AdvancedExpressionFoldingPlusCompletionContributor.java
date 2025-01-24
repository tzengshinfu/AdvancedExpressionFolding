package com.intellij.advancedExpressionFolding;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiType;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class AdvancedExpressionFoldingPlusCompletionContributor extends CompletionContributor {
    private final AdvancedExpressionFoldingSettings settings = AdvancedExpressionFoldingSettings.getInstance();
    private final AdvancedExpressionFoldingPlusOperatorMethodMapper mapper = AdvancedExpressionFoldingPlusOperatorMethodMapper.getInstance();

    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        //region Do not handle general auto-completion actions
        if (parameters.getEditor().getDocument().getText(TextRange.create(parameters.getOffset() - 1, parameters.getOffset())).equals(".")) {
            return;
        }
        //endregion

        if (!settings.getState().isOperatorMethodInvoke()) {
            return;
        }

        if (mapper.getMethod() == null) {
            return;
        }

        result.stopHere();

        String arguments = String.join(", ", Arrays.stream(mapper.getMethod().getParameters()).map(p -> {
            return ((PsiType) p.getType()).getPresentableText() + " " + p.getName();
        }).toList());

        LookupElement item = LookupElementBuilder.create("").withBoldness(true)
                .withIcon(Icons.METHOD_ICON)
                .withPresentableText(mapper.getMethod().getName())
                .withCaseSensitivity(true)
                .withTypeText(mapper.getMethod().getReturnType().getPresentableText())
                .withTailText("(" + arguments + ")", true)
                .withPsiElement(mapper.getMethod())
                .withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);
        result.addElement(item);

        Lookup lookup = LookupManager.getActiveLookup(parameters.getEditor());

        if (lookup != null) {
            lookup.addLookupListener(new AdvancedExpressionFoldingPlusMultipleListener(parameters.getEditor().getProject(), parameters.getEditor(), parameters.getEditor().getDocument(), parameters.getOffset()));
        }
    }
}