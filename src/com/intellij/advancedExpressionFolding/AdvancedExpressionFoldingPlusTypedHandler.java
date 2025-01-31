package com.intellij.advancedExpressionFolding;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;

/**
 * Advanced Expression Folding Enhanced Typing Handler
 * Handles the logic for triggering auto-completion when the user types characters.
 */
final class AdvancedExpressionFoldingPlusTypedHandler extends TypedHandlerDelegate {
    // Get the plugin settings instance
    private final AdvancedExpressionFoldingSettings settings = AdvancedExpressionFoldingSettings.getInstance();
    // Get the operator method mapper instance
    private final AdvancedExpressionFoldingPlusOperatorMethodMapper mapper = AdvancedExpressionFoldingPlusOperatorMethodMapper.getInstance();

    /**
     * Handles user-typed characters.
     *
     * @param c       The typed character
     * @param project The current project
     * @param editor  The current editor
     * @param file    The current file
     * @return Processing result
     */
    @NotNull
    @Override
    public Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        //region Check if the current file is a Java file
        if (!file.getFileType().equals(FileTypeRegistry.getInstance().getFileTypeByExtension("java"))) {
            return Result.CONTINUE;
        }
        //endregion

        //region Check if it is in single caret mode
        if (editor.getCaretModel().getAllCarets().size() != 1) {
            return Result.CONTINUE;
        }
        //endregion

        // Check if operator method invocation is enabled
        if (!settings.getState().isOperatorMethodInvoke()) {
            return Result.CONTINUE;
        }

        // Get the current caret position
        int currentOffset = editor.getCaretModel().getPrimaryCaret().getOffset();

        // Get the operator at the current position
        String operator = mapper.getOperator(editor.getDocument(), currentOffset);
        if (operator == null) {
            return Result.CONTINUE;
        }

        // Create a temporary PSI file for analysis
        PsiFile trimPsiFile = PsiFileFactory.getInstance(project).createFileFromText(
                file.getName(),                // File name
                JavaFileType.INSTANCE,         // File type
                editor.getDocument().getText(TextRange.create(0, currentOffset)),  // File content
                LocalTimeCounter.currentTime(), // Timestamp
                true,                          // Physical file
                false                          // Event system
        );

        // Get the last non-whitespace element before the operator
        PsiElement lastElement = mapper.getLastElement(trimPsiFile, currentOffset - operator.length() - 1);
        // Get the class of the method caller
        PsiClass methodCallerClass = mapper.getMethodCallerPsiClass(lastElement);
        if (methodCallerClass == null) {
            return Result.CONTINUE;
        }

        // Set the current method and operator
        mapper.setMethod(methodCallerClass, operator);

        // Trigger the auto-completion popup
        AutoPopupController.getInstance(project).scheduleAutoPopup(editor);

        return Result.CONTINUE;
    }
}