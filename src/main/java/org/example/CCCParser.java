package org.example;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * {@link <a href="https://stackoverflow.com/questions/42066626/using-cdt-parser-out-of-eclipsehow-to-make-project">Using CDT parser out of Eclipse(How to make project?)</a>}
 */
public class CCCParser {
    public static void main(String[] args) throws Exception {

        Path rocksdb = Path.of("/Users/alan/swProjects/evolvedBinary/rocksdb-evolved");
        Path rocksjni = rocksdb.resolve(Path.of("java/rocksjni"));
        Path portal = rocksjni.resolve(Path.of("portal.h"));
        File file = portal.toFile();
        char[] fileChars = FileChars.read(file);
        IASTTranslationUnit translationUnit = CCCParser.getIASTTranslationUnit(fileChars);
        final FilterVisitor filterVisitor = new FilterVisitor();
        translationUnit.accept(filterVisitor);
        System.out.println(filterVisitor);
        IASTComment[] comments = translationUnit.getComments();
        List<DecoratedNode<Optional<IASTFileLocation>>> topLevelComments = List.of(comments).stream().map(comment -> new DecoratedNode<>(comment, Optional.of(comment.getFileLocation()))).toList();
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
        List<DecoratedNode<List<IASTComment>>> attachedNodes = filterAttachedComments(containedNodes);
        attachedNodes.stream().map(DecoratedNode::declaration).forEach(node -> System.err.println(node.getRawSignature()));

        System.out.println("Bye!");
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
                    Optional<IASTFileLocation> lastClosed = input.get(i+j).decoration();
                    List<IASTComment> filtered = new ArrayList<>();
                    if (lastClosed.isEmpty()) {
                        filtered.addAll(comments);
                    } else for (IASTComment comment : comments) {
                        if (lastClosed.get().getNodeOffset() < comment.getFileLocation().getNodeOffset()) {
                            filtered.add(comment);
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


    private static List<DecoratedNode<Optional<IASTFileLocation>>> filterAttachedComments0(final List<DecoratedNode<Optional<IASTFileLocation>>> input) {
        final List<DecoratedNode<Optional<IASTFileLocation>>> output = new ArrayList<>();
        if (!input.isEmpty()) {
            if (input.getFirst().declaration() instanceof IASTDeclaration) {
                output.add(input.getFirst());
            }
        }
        for (int i = 0; i < input.size() - 1; i++) {
            if (input.get(i).declaration() instanceof IASTComment comment) {
                if (input.get(i+1).declaration() instanceof IASTDeclaration declaration) {
                    boolean linkedComment = true;
                    Optional<IASTFileLocation> lastClosed = input.get(i+1).decoration();
                    if (lastClosed.isPresent()) {
                        if (lastClosed.get().getNodeOffset() > comment.getFileLocation().getNodeOffset()) {
                            // the node before the declaration is after the comment
                            // so the comment does not relate to the declaration
                            linkedComment = false;
                        }
                    }
                    // Yield the declaration, and the preceding comment if it is linked
                    if (linkedComment) {
                        output.add(input.get(i));
                    }
                    output.add(input.get(i+1));
                }
            }
        }
        return output;
    }

    public static IASTTranslationUnit getIASTTranslationUnit(char[] code) throws Exception {
        FileContent fc = FileContent.create("", code);
        Map<String, String> macroDefinitions = new HashMap<>();
        String[] includeSearchPaths = new String[0];
        IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
        IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
        IIndex idx = null;
        int options = ILanguage.OPTION_IS_SOURCE_UNIT;
        IParserLogService log = new DefaultLogService();
        return GPPLanguage.getDefault().getASTTranslationUnit(fc, si, ifcp, idx, options, log);
    }

}