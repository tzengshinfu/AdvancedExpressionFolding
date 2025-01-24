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

final class AdvancedExpressionFoldingPlusTypedHandler extends TypedHandlerDelegate {
    private final AdvancedExpressionFoldingSettings settings = AdvancedExpressionFoldingSettings.getInstance();
    private final AdvancedExpressionFoldingPlusOperatorMethodMapper mapper = AdvancedExpressionFoldingPlusOperatorMethodMapper.getInstance();

    @NotNull
    @Override
    public Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        //region Check if the document is a Java file
        if (!file.getFileType().equals(FileTypeRegistry.getInstance().getFileTypeByExtension("java"))) {
            return Result.CONTINUE;
        }
        //endregion

        //region The input mode is single cursor
        if (editor.getCaretModel().getAllCarets().size() != 1) {
            return Result.CONTINUE;
        }
        //endregion

        if (!settings.getState().isOperatorMethodInvoke()) {
            return Result.CONTINUE;
        }

        int currentOffset = editor.getCaretModel().getPrimaryCaret().getOffset();

        String operator = mapper.getOperator(editor.getDocument(), currentOffset);
        if (operator == null) {
            return Result.CONTINUE;
        }

        PsiFile trimPsiFile = PsiFileFactory.getInstance(project).createFileFromText(
                file.getName(),
                JavaFileType.INSTANCE,
                editor.getDocument().getText(TextRange.create(0, currentOffset)),
                LocalTimeCounter.currentTime(),
                true,
                false
        );
        PsiElement lastElement = mapper.getLastElement(trimPsiFile, currentOffset - operator.length() - 1);
        PsiClass methodCallerClass = mapper.getMethodCallerPsiClass(lastElement);
        if (methodCallerClass == null) {
            return Result.CONTINUE;
        }

        mapper.setMethod(methodCallerClass, operator);

        AutoPopupController.getInstance(project).scheduleAutoPopup(editor);

        return Result.CONTINUE;
    }
}