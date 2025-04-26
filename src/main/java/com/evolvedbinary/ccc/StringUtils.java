package com.evolvedbinary.ccc;

import java.util.regex.Pattern;

public class StringUtils {

    private final static Converter converter = new Converter();

    public static String convertCamelCaseToSnakeRegex(String input) {
        return converter.convertCamelCaseToSnakeRegex(input);
    }

    private final static class Converter {

        /**
         * {@link <a href="https://www.baeldung.com/java-camel-snake-case-conversion">Convert Camel case to snake</a>}
         *
         * @param input a string in Camel Case
         * @return the snake case equivalent
         */
        String convertCamelCaseToSnakeRegex(String input) {
            String s = input;
            s = upperUpper.matcher(s).replaceAll("$1_");
            s = lowerUpper.matcher(s).replaceAll("$1_$2");
            s = s.toLowerCase();
            return s;
        }

        final Pattern upperUpper = Pattern.compile("([A-Z])(?=[A-Z])");
        final Pattern lowerUpper = Pattern.compile("([a-z])([A-Z])");
    }
}
