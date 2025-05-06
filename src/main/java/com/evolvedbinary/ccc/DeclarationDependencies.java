package com.evolvedbinary.ccc;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeclarationDependencies {

    final Set<String> observed = new HashSet<>();
    final List<String> ordered = new ArrayList<>();
    final Map<String, List<String>> requires = new HashMap<>();

    // Reference to a class with JNI or Jni in the name..
    Pattern classReference = Pattern.compile("[A-Za-z]+");

    public void addDependencies(IASTDeclaration declaration) {
        final List<String> myRequires = extractDependencies(declaration);
        final String me = Naming.nameOfDeclaration(declaration);
        if (!myRequires.isEmpty()) {
            requires.put(me, myRequires);
        }
        observed.add(me);
        ordered.add(me);
    }

    public List<String> getDeclaredDependencies(final String me) {
        if (requires.containsKey(me)) {
            return requires.get(me);
        }
        return List.of();
    }

    private List<String> extractDependencies(IASTDeclaration declaration) {

        String sig = declaration.getRawSignature();
        final Matcher matcher = classReference.matcher(sig);
        final List<String> collect = new ArrayList<>();
        int pos = 0;
        while (matcher.find(pos)) {
            String group = matcher.group();
            if (observed.contains(group)) {
                collect.add(group);
            }
            if (RepositoryConfiguration.explicitDependencies.containsKey(group)) {
                collect.add(group);
            }
            pos = matcher.end();
        }
        final List<String> result = new ArrayList<>(Set.copyOf(collect));
        Collections.sort(result);
        return result;
    }
}
