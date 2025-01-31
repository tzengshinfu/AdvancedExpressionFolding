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

/**
 * Operator-to-method mapping class
 * Responsible for managing the mapping between operators and corresponding methods,
 * as well as maintaining related states
 */
public class AdvancedExpressionFoldingPlusOperatorMethodMapper {
    /**
     * Mapping record class
     * Stores the class name, operator, and corresponding method name
     */
    public record Mapper(
            String className,
            String operator,
            String methodName) {
    }

    private final List<Mapper> mapperList = new ArrayList<Mapper>();  // Mapping list
    PsiMethod method = null;         // Current method
    String operator = null;          // Current operator
    FoldingGroup foldingGroup = null;// Current folding group
    int cursorPosition = -1;         // Current cursor position

    // Getter methods
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

    /**
     * Sets the current method and operator
     *
     * @param currentClass The current class
     * @param operator     The operator
     */
    public void setMethod(PsiClass currentClass, String operator) {
        if (currentClass == null) {
            method = null;
            this.operator = null;
            return;
        }

        method = null;
        this.operator = null;

        // Find the matching mapping
        Optional<Mapper> mapper = mapperList.stream()
                .filter(m -> (m.className.equals(currentClass.getQualifiedName()) || "*".equals(m.className))
                        && m.operator.equals(operator))
                .findFirst();

        if (mapper.isPresent()) {
            // Find the corresponding method in the current class
            Optional<PsiMethod> method = Arrays.stream(currentClass.getMethods())
                    .filter(m -> m.getName().equals(mapper.get().methodName))
                    .findFirst();

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

    /**
     * Clears all states
     */
    public void clear() {
        method = null;
        operator = null;
        foldingGroup = null;
        cursorPosition = -1;
    }

    /**
     * Constructor
     * Initializes the default operator-to-method mappings
     */
    AdvancedExpressionFoldingPlusOperatorMethodMapper() {
        // Add operator mappings for BigDecimal
        mapperList.add(new Mapper("java.math.BigDecimal", "+", "add"));
        mapperList.add(new Mapper("java.math.BigDecimal", "-", "subtract"));
        mapperList.add(new Mapper("java.math.BigDecimal", "*", "multiply"));
        mapperList.add(new Mapper("java.math.BigDecimal", "/", "divide"));
        // Add operator mappings for BigInteger
        mapperList.add(new Mapper("java.math.BigInteger", "+", "add"));
        mapperList.add(new Mapper("java.math.BigInteger", "-", "subtract"));
        mapperList.add(new Mapper("java.math.BigInteger", "*", "multiply"));
        mapperList.add(new Mapper("java.math.BigInteger", "/", "divide"));
        // Add general equals mapping
        mapperList.add(new Mapper("*", "===", "equals"));
    }

    /**
     * Gets the instance (Singleton pattern)
     */
    @NotNull
    public static AdvancedExpressionFoldingPlusOperatorMethodMapper getInstance() {
        return ApplicationManager.getApplication().getService(AdvancedExpressionFoldingPlusOperatorMethodMapper.class);
    }

    /**
     * Retrieves the PsiClass of the method caller
     */
    public PsiClass getMethodCallerPsiClass(PsiElement currentElement) {
        if (currentElement == null) {
            return null;
        }

        PsiType type = null;

        // Traverse parent elements until a type is found
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

    /**
     * Retrieves the type of an element
     */
    public PsiType getElementType(PsiElement element) {
        if (element instanceof PsiIdentifier) {
            element = element.getParent();
        }

        // Return the corresponding type based on the element type
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

    /**
     * Retrieves the last non-whitespace element at the specified position
     */
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

    /**
     * Retrieves the operator at the specified position in the document
     */
    public String getOperator(Document document, int offset) {
        String operator = null;

        // Iterate through all mappings to find a matching operator
        for (Mapper mapper : mapperList) {
            String extractedText = document.getText(TextRange.create(offset - mapper.operator.length(), offset));

            if (extractedText.equals(mapper.operator())) {
                operator = extractedText;
            }
        }

        return operator;
    }
}