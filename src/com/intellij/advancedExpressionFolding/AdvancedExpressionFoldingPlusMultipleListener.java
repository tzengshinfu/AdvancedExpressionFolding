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

/**
 * Advanced Expression Folding Plus Multiple Listener
 * Handles code completion selection and mouse events for operator-to-method conversion
 */
public class AdvancedExpressionFoldingPlusMultipleListener implements LookupListener, EditorMouseListener {
    private Project project = null;
    private Editor editor = null;
    private Document document = null;
    private int offset = 0;
    // Get instance of plugin settings
    private final AdvancedExpressionFoldingSettings settings = AdvancedExpressionFoldingSettings.getInstance();
    // Get instance of operator method mapper
    private final AdvancedExpressionFoldingPlusOperatorMethodMapper mapper = AdvancedExpressionFoldingPlusOperatorMethodMapper.getInstance();

    /**
     * Constructor - initializes listener and sets up folding region monitoring
     */
    public AdvancedExpressionFoldingPlusMultipleListener(Project project, Editor editor, Document document, int offset) {
        this.project = project;
        this.editor = editor;
        this.document = document;
        this.offset = offset;

        // Get current file editor
        FileEditor fileEditor = FileEditorManager.getInstance(project).getAllEditors()[0];
        EditorEx editorEx = ((EditorEx) ((TextEditor) fileEditor).getEditor());

        // Add folding model listener
        editorEx.getFoldingModel().addListener(new FoldingListener() {
            @Override
            public void onFoldRegionStateChange(@NotNull FoldRegion region) {
                // Return if immediate collapse is disabled
                if (!settings.getState().isImmediatelyCollapse()) {
                    return;
                }

                //region Check if cursor is within the folding region just converted
                if (!(mapper.getCursorPosition() >= region.getStartOffset() && mapper.getCursorPosition() <= region.getEndOffset())) {
                    return;
                }
                //endregion

                // Clear method and set folding group
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

    /**
     * Handle completion item selection
     * Converts operator expression to method call
     */
    @Override
    public void itemSelected(LookupEvent event) {
        // Return if no method is mapped
        if (mapper.getMethod() == null) {
            return;
        }

        // Get PSI file from document
        PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
        assert file != null;

        PsiElement currentElement;
        // Find element at operator position
        currentElement = file.findElementAt(offset - mapper.getOperator().length());
        currentElement = currentElement.getParent();

        // Return if not a binary expression
        if (!(currentElement instanceof PsiBinaryExpression)) {
            return;
        }

        PsiBinaryExpression binaryExpression = (PsiBinaryExpression) currentElement;
        PsiAssignmentExpression assignmentExpression;

        // Check if parent is assignment expression
        if (currentElement.getParent() instanceof PsiAssignmentExpression) {
            assignmentExpression = (PsiAssignmentExpression) currentElement.getParent();
        } else {
            assignmentExpression = null;
        }

        // Run write action to modify code
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiExpression lExpr = null;
            PsiExpression rExpr = null;

            // Get left and right operands
            if (assignmentExpression != null) {
                lExpr = ((PsiBinaryExpression) assignmentExpression.getLExpression()).getLOperand();
                rExpr = assignmentExpression.getRExpression();
            } else {
                lExpr = binaryExpression.getLOperand();
                rExpr = binaryExpression.getROperand();
            }

            String b = rExpr != null ? "b" : "";

            // Create method call expression
            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            PsiMethodCallExpression equalsCall =
                    (PsiMethodCallExpression) factory.createExpressionFromText("a." + mapper.getMethod().getName() + "(" + b + ")", null);

            // Replace qualifier and arguments
            equalsCall.getMethodExpression().getQualifierExpression().replace(lExpr);

            if (rExpr != null) {
                equalsCall.getArgumentList().getExpressions()[0].replace(rExpr);
            }

            // Replace original expression
            if (assignmentExpression != null) {
                assignmentExpression.replace(equalsCall);
            } else {
                binaryExpression.replace(equalsCall);
            }

            // Calculate and set new cursor position
            int operatorPosition = ((assignmentExpression != null) ? assignmentExpression : binaryExpression).getText().lastIndexOf(mapper.getOperator());
            int startingPosition = offset - operatorPosition - (mapper.getOperator().length() - 1);
            int shiftPosition = (rExpr != null) ? equalsCall.getText().lastIndexOf(")") : equalsCall.getText().lastIndexOf("(");
            int newCursorPosition = startingPosition + shiftPosition;

            mapper.setCursorPosition(newCursorPosition);
            editor.getCaretModel().moveToOffset(newCursorPosition);
        });
    }

    /**
     * Handle mouse click events
     * Expands folded regions when clicked
     */
    @Override
    public void mouseClicked(EditorMouseEvent e) {
        // Return if immediate collapse is disabled
        if (!settings.getState().isImmediatelyCollapse()) {
            return;
        }

        // Return if no folded region clicked
        if (e.getCollapsedFoldRegion() == null) {
            return;
        }

        // Return if clicked region is not the current folding group
        if (e.getCollapsedFoldRegion().getGroup() != mapper.getFoldingGroup()) {
            return;
        }

        // Clear mapper and expand region
        mapper.clear();
        e.getEditor().getFoldingModel().runBatchFoldingOperation(() -> e.getCollapsedFoldRegion().setExpanded(true));
    }
}