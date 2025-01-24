package com.intellij.advancedExpressionFolding;

import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.codeInsight.lookup.LookupListener;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

public class AdvancedExpressionFoldingPlusMultipleListener implements LookupListener {
    private Project project = null;
    private Editor editor = null;
    private Document document = null;
    private int offset = 0;
    private final AdvancedExpressionFoldingPlusOperatorMethodMapper mapper = AdvancedExpressionFoldingPlusOperatorMethodMapper.getInstance();

    public AdvancedExpressionFoldingPlusMultipleListener(Project project, Editor editor, Document document, int offset) {
        this.project = project;
        this.editor = editor;
        this.document = document;
        this.offset = offset;
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

            editor.getCaretModel().moveToOffset(newCursorPosition);
        });
    }
}