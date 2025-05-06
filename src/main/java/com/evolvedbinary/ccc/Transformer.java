package com.evolvedbinary.ccc;

import org.eclipse.cdt.core.dom.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A class that transforms a translation unit into sometnig useful for a particular task
 * For example, {@link DeclarationTransformer} pulls out top level declarations and associated comments.
 *
 * This class contains a number of static methods useful to building transformers
 * as well as the static subclass {@link DeclarationTransformer} which is the first transformer we built;
 * obviously the static helpers initially reflect the needs of this transformer.
 *
 * @param <TTarget>
 */
public abstract class Transformer<TTarget> {

    private final IASTTranslationUnit translationUnit;
    private final DeclarationDependencies declarationDependencies;

    public Transformer(final IASTTranslationUnit translationUnit, final DeclarationDependencies declarationDependencies) {
        this.translationUnit = translationUnit;
        this.declarationDependencies = declarationDependencies;
    }

    public abstract TTarget transform();

    public static class DeclarationTransformer extends Transformer<List<DecoratedNode<List<IASTComment>>>> {

        public DeclarationTransformer(IASTTranslationUnit translationUnit, final DeclarationDependencies declarationDependencies) {
            super(translationUnit, declarationDependencies);
        }

        @Override
        public List<DecoratedNode<List<IASTComment>>> transform() {
            return getDecoratedNodes();
        }
    }

    protected List<DecoratedNode<List<IASTComment>>> getDecoratedNodes() {
        final FilterVisitor filterVisitor = new FilterVisitor(declarationDependencies);
        translationUnit.accept(filterVisitor);
        System.out.println(filterVisitor);
        IASTComment[] comments = translationUnit.getComments();
        List<DecoratedNode<Optional<IASTFileLocation>>> topLevelComments = Stream.of(comments).map(comment -> new DecoratedNode<>(comment, Optional.of(comment.getFileLocation()))).toList();
        List<DecoratedNode<Optional<IASTFileLocation>>> topLevelDeclarations = filterVisitor.getTopLevelDeclarations();
        List<DecoratedNode<Optional<IASTFileLocation>>> allNodes = new ArrayList<>();
        allNodes.addAll(topLevelComments);
        allNodes.addAll(topLevelDeclarations);
        allNodes.sort((o1, o2) -> {
            int location1 = o1.declaration().getFileLocation().getNodeOffset();
            int location2 = o2.declaration().getFileLocation().getNodeOffset();
            return Integer.compare(location1, location2);
        });

        List<DecoratedNode<Optional<IASTFileLocation>>> containedNodes = filterContained(allNodes);

        return filterAttachedComments(containedNodes);
    }

    /**
     * Remove nodes (we expect comments) which are contained inside other nodes
     * Leaving only the ones preceding declarations
     * @param input
     * @return
     */
    private static List<DecoratedNode<Optional<IASTFileLocation>>> filterContained(final List<DecoratedNode<Optional<IASTFileLocation>>> input) {
        final List<DecoratedNode<Optional<IASTFileLocation>>> output = new ArrayList<>();
        for (int pos = 0; pos < input.size();) {
            output.add(input.get(pos));
            IASTFileLocation posLocation = input.get(pos).declaration().getFileLocation();
            int nextPos = pos;
            for (int next = pos + 1; next < input.size(); next++) {
                IASTFileLocation nextLocation = input.get(next).declaration().getFileLocation();
                if (nextLocation.getNodeOffset() >= posLocation.getNodeOffset() + posLocation.getNodeLength()) {
                    nextPos = next;
                    break;
                }
            }
            if (nextPos == pos) break;
            pos = nextPos;
        }
        return output;
    }

    private static List<DecoratedNode<List<IASTComment>>> filterAttachedComments(final List<DecoratedNode<Optional<IASTFileLocation>>> input) {
        final List<DecoratedNode<List<IASTComment>>> output = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            List<IASTComment> comments = new ArrayList<>();
            for (int j = 0; i + j < input.size(); j++) {
                if (input.get(i+j).declaration() instanceof IASTComment comment) {
                    comments.add(comment);
                } else if (input.get(i+j).declaration() instanceof IASTDeclaration declaration) {
                    IASTNode parentNode = declaration.getParent();
                    final DecoratedNode<Optional<IASTFileLocation>> inputNode = input.get(i+j);
                    Optional<IASTFileLocation> lastClosed = inputNode.decoration();
                    List<IASTComment> filtered = new ArrayList<>();
                    for (IASTComment comment : comments) {
                        if (lastClosed.isEmpty() || lastClosed.get().getNodeOffset() < comment.getFileLocation().getNodeOffset()) {
                            if (parentNode == null || parentNode.getFileLocation().getNodeOffset() < comment.getFileLocation().getNodeOffset()) {
                                filtered.add(comment);
                            }
                        }
                    }
                    output.add(new DecoratedNode<>(declaration, filtered));
                    i = i + j;
                    break;
                }
            }
        }
        return output;
    }
}
