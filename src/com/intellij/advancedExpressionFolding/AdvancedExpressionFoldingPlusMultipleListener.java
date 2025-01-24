package com.intellij.advancedExpressionFolding;

import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.codeInsight.lookup.LookupListener;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.FoldingListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public class AdvancedExpressionFoldingPlusMultipleListener implements LookupListener, EditorMouseListener {
    private Project project = null;
    private Editor editor = null;
    private Document document = null;
    private int offset = 0;
    private final AdvancedExpressionFoldingSettings settings = AdvancedExpressionFoldingSettings.getInstance();
    private final AdvancedExpressionFoldingPlusOperatorMethodMapper mapper = AdvancedExpressionFoldingPlusOperatorMethodMapper.getInstance();

    public AdvancedExpressionFoldingPlusMultipleListener(Project project, Editor editor, Document document, int offset) {
        this.project = project;
        this.editor = editor;
        this.document = document;
        this.offset = offset;

        FileEditor fileEditor = FileEditorManager.getInstance(project).getAllEditors()[0];
        EditorEx editorEx = ((EditorEx) ((TextEditor) fileEditor).getEditor());

        editorEx.getFoldingModel().addListener(new FoldingListener() {
            @Override
            public void onFoldRegionStateChange(@NotNull FoldRegion region) {
                if (!settings.getState().isImmediatelyCollapse()) {
                    return;
                }

                //region Positioned as a Folding Region just converted from operator to method
                if (!(mapper.getCursorPosition() >= region.getStartOffset() && mapper.getCursorPosition() <= region.getEndOffset())) {
                    return;
                }
                //endregion

                mapper.setMethod(null, null);
                mapper.setFoldingGroup(region.getGroup());
                region.setExpanded(false);
            }

            @Override
            public void onFoldProcessingEnd() {
            }
        }, fileEditor);
    }

    public AdvancedExpressionFoldingPlusMultipleListener() {
    }

    @Override
    public void itemSelected(LookupEvent event) {
        if (mapper.getMethod() == null) {
            return;
        }

        PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);

        assert file != null;

        PsiElement currentElement;

        currentElement = file.findElementAt(offset - mapper.getOperator().length());

        currentElement = currentElement.getParent();

        if (!(currentElement instanceof PsiBinaryExpression)) {
            return;
        }

        PsiBinaryExpression binaryExpression = (PsiBinaryExpression) currentElement;
        PsiAssignmentExpression assignmentExpression;

        if (currentElement.getParent() instanceof PsiAssignmentExpression) {
            assignmentExpression = (PsiAssignmentExpression) currentElement.getParent();
        } else {
            assignmentExpression = null;
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiExpression lExpr = null;
            PsiExpression rExpr = null;

            if (assignmentExpression != null) {
                lExpr = ((PsiBinaryExpression) assignmentExpression.getLExpression()).getLOperand();
                rExpr = assignmentExpression.getRExpression();
            } else {
                lExpr = binaryExpression.getLOperand();
                rExpr = binaryExpression.getROperand();
            }

            String b = rExpr != null ? "b" : "";

            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            PsiMethodCallExpression equalsCall =
                    (PsiMethodCallExpression) factory.createExpressionFromText("a." + mapper.getMethod().getName() + "(" + b + ")", null);

            equalsCall.getMethodExpression().getQualifierExpression().replace(lExpr);

            if (rExpr != null) {
                equalsCall.getArgumentList().getExpressions()[0].replace(rExpr);
            }

            if (assignmentExpression != null) {
                assignmentExpression.replace(equalsCall);
            } else {
                binaryExpression.replace(equalsCall);
            }

            int operatorPosition = ((assignmentExpression != null) ? assignmentExpression : binaryExpression).getText().lastIndexOf(mapper.getOperator());
            int startingPosition = offset - operatorPosition - (mapper.getOperator().length() - 1);
            int shiftPosition = (rExpr != null) ? equalsCall.getText().lastIndexOf(")") : equalsCall.getText().lastIndexOf("(");
            int newCursorPosition = startingPosition + shiftPosition;

            mapper.setCursorPosition(newCursorPosition);
            editor.getCaretModel().moveToOffset(newCursorPosition);
        });
    }

    @Override
    public void mouseClicked(EditorMouseEvent e) {
        if (!settings.getState().isImmediatelyCollapse()) {
            return;
        }

        if (e.getCollapsedFoldRegion() == null) {
            return;
        }

        if (e.getCollapsedFoldRegion().getGroup() != mapper.getFoldingGroup()) {
            return;
        }

        mapper.clear();
        e.getEditor().getFoldingModel().runBatchFoldingOperation(() -> e.getCollapsedFoldRegion().setExpanded(true));
    }
}