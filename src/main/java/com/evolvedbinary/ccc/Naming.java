package com.evolvedbinary.ccc;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;

public class Naming {

    public static String nameOfDeclaration(final IASTDeclSpecifier declSpecifier) {
        String[] words =  declSpecifier.toString().split(" ");
        if (words.length > 0) {
            return words[words.length - 1];
        }
        throw new RuntimeException("No name for declaration: " + declSpecifier.getRawSignature());
    }

    public static String nameOfDeclaration(final IASTNode declaration) {
        return switch (declaration) {
            case IASTSimpleDeclaration simpleDeclaration -> nameOfDeclaration(simpleDeclaration.getDeclSpecifier());
            case ICPPASTTemplateDeclaration templateDeclaration -> nameOfDeclaration(templateDeclaration.getDeclaration());
            default -> throw new IllegalStateException("Unexpected value: " + declaration);
        };
    }

    public static String classToHeader(String className) {
        return StringUtils.convertCamelCaseToSnakeRegex(className);
    }
}
