package com.intellij.advancedExpressionFolding;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AdvancedExpressionFoldingPlusOperatorMethodMapper {
    public record Mapper(
            String className,
            String operator,
            String methodName) {
    }

    private final List<Mapper> mapperList = new ArrayList<Mapper>();
    PsiMethod method = null;
    String operator = null;
    FoldingGroup foldingGroup = null;
    int cursorPosition = -1;

    public PsiMethod getMethod() {
        return method;
    }

    public String getOperator() {
        return operator;
    }

    public FoldingGroup getFoldingGroup() {
        return foldingGroup;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setMethod(PsiClass currentClass, String operator) {
        if (currentClass == null) {
            method = null;
            this.operator = null;

            return;
        }

        method = null;
        this.operator = null;

        Optional<Mapper> mapper = mapperList.stream().filter(m -> (m.className.equals(currentClass.getQualifiedName()) || "*".equals(m.className)) && m.operator.equals(operator)).findFirst();

        if (mapper.isPresent()) {
            Optional<PsiMethod> method = Arrays.stream(currentClass.getMethods()).filter(m -> m.getName().equals(mapper.get().methodName)).findFirst();

            method.ifPresent(psiMethod -> {
                this.method = psiMethod;
                this.operator = operator;
            });
        }
    }

    public void setFoldingGroup(FoldingGroup foldingGroup) {
        this.foldingGroup = foldingGroup;
    }

    public void setCursorPosition(int cursorPosition) {
        this.cursorPosition = cursorPosition;
    }

    public void clear() {
        method = null;
        operator = null;
        foldingGroup = null;
        cursorPosition = -1;
    }

    AdvancedExpressionFoldingPlusOperatorMethodMapper() {
        mapperList.add(new Mapper("java.math.BigDecimal", "+", "add"));
        mapperList.add(new Mapper("java.math.BigDecimal", "-", "subtract"));
        mapperList.add(new Mapper("java.math.BigDecimal", "*", "multiply"));
        mapperList.add(new Mapper("java.math.BigDecimal", "/", "divide"));
        mapperList.add(new Mapper("java.math.BigInteger", "+", "add"));
        mapperList.add(new Mapper("java.math.BigInteger", "-", "subtract"));
        mapperList.add(new Mapper("java.math.BigInteger", "*", "multiply"));
        mapperList.add(new Mapper("java.math.BigInteger", "/", "divide"));
        mapperList.add(new Mapper("*", "===", "equals"));
    }

    @NotNull
    public static AdvancedExpressionFoldingPlusOperatorMethodMapper getInstance() {
        return ApplicationManager.getApplication().getService(AdvancedExpressionFoldingPlusOperatorMethodMapper.class);
    }

    public PsiClass getMethodCallerPsiClass(PsiElement currentElement) {
        if (currentElement == null) {
            return null;
        }

        PsiType type = null;

        do {
            type = getElementType(currentElement);

            currentElement = currentElement.getParent();
        } while (type == null);

        try {
            return ((PsiClassType) type).resolve();
        } catch (Exception ex) {
            return null;
        }
    }

    public PsiType getElementType(PsiElement element) {
        if (element instanceof PsiIdentifier) {
            element = element.getParent();
        }

        if (element instanceof PsiVariable variable) {
            return variable.getType();
        }

        if (element instanceof PsiExpression expression) {
            return expression.getType();
        }

        if (element instanceof PsiMethod method) {
            return method.getReturnType();
        }

        if (element instanceof PsiParameter parameter) {
            return parameter.getType();
        }

        if (element instanceof PsiReference reference) {
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement instanceof PsiVariable) {
                return ((PsiVariable) resolvedElement).getType();
            }
        }

        return null;
    }

    public PsiElement getLastElement(PsiFile file, int offset) {
        PsiElement lastElement = null;
        int currentOffset = offset;

        do {
            lastElement = file.findElementAt(currentOffset);

            if (lastElement == null) {
                break;
            }

            if (!(lastElement instanceof PsiWhiteSpace)) {
                break;
            }

            currentOffset--;
        } while (true);

        return lastElement;
    }

    public String getOperator(Document document, int offset) {
        String operator = null;

        for (Mapper mapper : mapperList) {
            String extractedText = document.getText(TextRange.create(offset - mapper.operator.length(), offset));

            if (extractedText.equals(mapper.operator())) {
                operator = extractedText;
            }
        }

        return operator;
    }
}