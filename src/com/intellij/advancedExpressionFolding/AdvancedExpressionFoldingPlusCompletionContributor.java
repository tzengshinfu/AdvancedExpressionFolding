package com.intellij.advancedExpressionFolding;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiType;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Advanced Expression Folding Plus Completion Contributor
 * Provides code completion suggestions for operator method invocations
 */
public class AdvancedExpressionFoldingPlusCompletionContributor extends CompletionContributor {
    // Get instance of plugin settings
    private final AdvancedExpressionFoldingSettings settings = AdvancedExpressionFoldingSettings.getInstance();
    // Get instance of operator method mapper
    private final AdvancedExpressionFoldingPlusOperatorMethodMapper mapper = AdvancedExpressionFoldingPlusOperatorMethodMapper.getInstance();

    /**
     * Fill completion suggestions
     *
     * @param parameters Completion parameters
     * @param result     Completion result set
     */
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        //region Do not handle general auto-completion actions triggered by dot
        if (parameters.getEditor().getDocument().getText(TextRange.create(parameters.getOffset() - 1, parameters.getOffset())).equals(".")) {
            return;
        }
        //endregion

        // Return if operator method invoke feature is disabled
        if (!settings.getState().isOperatorMethodInvoke()) {
            return;
        }

        // Return if no method is mapped
        if (mapper.getMethod() == null) {
            return;
        }

        // Stop processing other completion contributors
        result.stopHere();

        // Generate method parameter list string
        String arguments = String.join(", ", Arrays.stream(mapper.getMethod().getParameterList().getParameters()).map(p -> p.getType().getPresentableText() + " " + p.getName()).toList());

        // Create completion lookup element
        LookupElement item = LookupElementBuilder.create("").withBoldness(true)
                .withIcon(PlatformIcons.METHOD_ICON)                        // Set method icon
                .withPresentableText(mapper.getMethod().getName())  // Set display text
                .withCaseSensitivity(true)                         // Enable case sensitivity
                .withTypeText(mapper.getMethod().getReturnType().getPresentableText())  // Set return type text
                .withTailText("(" + arguments + ")", true)         // Set tail text (parameter list)
                .withPsiElement(mapper.getMethod())                // Associate PSI element
                .withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);  // Set auto-completion policy
        result.addElement(item);

        // Get active lookup window
        Lookup lookup = LookupManager.getActiveLookup(parameters.getEditor());

        // Add custom listener if lookup exists
        if (lookup != null) {
            lookup.addLookupListener(new AdvancedExpressionFoldingPlusMultipleListener(
                    parameters.getEditor().getProject(),
                    parameters.getEditor(),
                    parameters.getEditor().getDocument(),
                    parameters.getOffset()
            ));
        }
    }
}