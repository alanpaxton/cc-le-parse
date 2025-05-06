package com.evolvedbinary.ccc;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Walk the AST of the parsed input file(s)
 * Build a list of the contained declarations
 * Together with some extra information - to wit, the location of the last visited declaration which had been left
 * before this one was entered - that lets us check by file position whether comments (retrieved elsewhere)
 * are attached to the declaration in hand.
 */
public class FilterVisitor extends ASTVisitor {

    private final DeclarationDependencies declarationDependencies;

    public FilterVisitor(final DeclarationDependencies declarationDependencies) {
        super();

        this.declarationDependencies = declarationDependencies;

        shouldVisitNames = true;
        shouldVisitDeclarations = true;

        shouldVisitDeclarators = true;
        shouldVisitAttributes = true;
        shouldVisitStatements = false;
        shouldVisitTypeIds = true;
    }

    private final List<DecoratedNode<Optional<IASTFileLocation>>> topLevelDeclarations = new ArrayList<>();

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

            declarationDependencies.addDependencies(simpleDeclaration);

            topLevelDeclarations.add(new DecoratedNode<>(targetDeclaration, lastClosed));
        }

        return ASTVisitor.PROCESS_CONTINUE;
    }

    private int visitDefault(final IASTDeclaration declaration) {

        return ASTVisitor.PROCESS_CONTINUE;
    }

    public final List<DecoratedNode<Optional<IASTFileLocation>>> getTopLevelDeclarations() {
        return topLevelDeclarations;
    }

    @Override
    public int visit(final IASTDeclaration declaration)
    {
        // more cases to be added
        return switch (declaration) {
            case IASTSimpleDeclaration simpleDeclaration -> visitSimple(simpleDeclaration);
            default -> visitDefault(declaration);
        };
    }

    @Override
    public int leave(final IASTDeclaration declaration)
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
