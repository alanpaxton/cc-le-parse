package org.example;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;

import java.util.*;

public class FilterVisitor extends ASTVisitor {

    public FilterVisitor() {
        super();

        shouldVisitNames = true;
        shouldVisitDeclarations = true;

        shouldVisitDeclarators = true;
        shouldVisitAttributes = true;
        shouldVisitStatements = false;
        shouldVisitTypeIds = true;
    }

    private List<DecoratedNode<Optional<IASTFileLocation>>> topLevelDeclarations = new ArrayList<>();

    private Optional<IASTFileLocation> lastClosed = Optional.empty();

    int simpleCount = 0;
    int templateCount = 0;

    private int visitSimple(IASTSimpleDeclaration simpleDeclaration) {
        IASTDeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
        IASTDeclaration targetDeclaration = simpleDeclaration;
        if (declSpecifier instanceof IASTCompositeTypeSpecifier) {
            // compositeTypeSpecifier is something like "class IllegalArgumentExceptionJni"
            // see also simpleDeclaration.getDeclarators()
            if (simpleDeclaration.getParent() instanceof ICPPASTTemplateDeclaration templateDeclaration) {
                targetDeclaration = templateDeclaration;
                templateCount++;
            } else {
                simpleCount++;
            }

            topLevelDeclarations.add(new DecoratedNode<>(targetDeclaration, lastClosed));
        }
        return ASTVisitor.PROCESS_CONTINUE;
    }

    private int visitDefault(IASTDeclaration declaration) {
        return ASTVisitor.PROCESS_CONTINUE;
    }

    public final List<DecoratedNode<Optional<IASTFileLocation>>> getTopLevelDeclarations() {
        return topLevelDeclarations;
    }

    @Override
    public int visit(IASTDeclaration declaration)
    {
        // more cases to be added
        return switch (declaration) {
            case IASTSimpleDeclaration simpleDeclaration -> visitSimple(simpleDeclaration);
            default -> visitDefault(declaration);
        };
    }

    @Override
    public int leave(IASTDeclaration declaration)
    {
        lastClosed = Optional.of(declaration.getFileLocation());

        return ASTVisitor.PROCESS_CONTINUE;
    }

    @Override
    public String toString() {
        return "FilterVisitor{" +
          "simpleCount=" + simpleCount +
          ", templateCount=" + templateCount +
          '}';
    }
}
