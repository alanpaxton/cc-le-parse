package com.evolvedbinary.ccc;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;

import java.util.List;

public record DecoratedNode<T>(IASTNode declaration, T decoration) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        buildString(sb);
        return sb.toString();
    }

    /**
     * Append the stringification of this object to the supplied StringBuilder
     *
     * @param sb StringBuilder to append to
     * @return the original StringBuilder, with this object appended
     */
    public void buildString(final StringBuilder sb) {
        if (decoration instanceof List<?> decorationList) {
            for (Object item : decorationList) {
                sb.append(item).append('\n');
            }
        } else {
            sb.append(decoration);
        }
        sb.append(declaration.getRawSignature());
    }

    private String nameOfDeclaration(final IASTDeclSpecifier declSpecifier) {
        String[] words =  declSpecifier.toString().split(" ");
        if (words.length > 0) {
            return StringUtils.convertCamelCaseToSnakeRegex(words[words.length - 1]);
        }
        throw new RuntimeException("No name for declaration: " + declSpecifier.getRawSignature());
    }

    private String nameOfDeclaration(final IASTNode declaration) {
        return switch (declaration) {
            case IASTSimpleDeclaration simpleDeclaration -> nameOfDeclaration(simpleDeclaration.getDeclSpecifier());
            case ICPPASTTemplateDeclaration templateDeclaration -> nameOfDeclaration(templateDeclaration.getDeclaration());
            default -> throw new IllegalStateException("Unexpected value: " + declaration);
        };
    }

    public String getDeclarationName() {
        return nameOfDeclaration(declaration);
    }
}
