package org.avasthi.java.cli;

import java.text.ParseException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Atom {
    class Element {
        public Element(String symbol) {
            this.symbol = symbol;
        }

        final String symbol;
    }
    class Molecule {
        Element element;
        int count;
    }
    class Formula {
        public Formula(Molecule molecule) {
            this.molecule = molecule;
            this.count = 1;
        }

        public Formula(Molecule molecule, int count) {
            this.molecule = molecule;
            this.count = count;
        }

        Molecule molecule;
        int count;
    }
    public static void main(String[] args) throws ParseException {

        String compoundAtomRegex = "\\((?<formulaList>(\\p{Upper}\\p{Lower}*\\p{Digit}*)*)\\)(?<count>\\p{Digit}*)";
        String compoundAtomRegex1 = "(?<ob>\\()((?<elemName>\\p{Upper}\\p{Lower}*)(?<count>\\p{Digit}*))*(?<cb>\\))";
        String atomRegex = "(?<elemName>\\p{Upper}\\p{Lower}*)(?<count>\\p{Digit}*)";
        String formula = "H2O2He3Mg4(H2O2He3Mg4)3Mg3";

        if (formula.contains("(")) {

            Pattern p = Pattern.compile(compoundAtomRegex);
            Matcher m = p.matcher(formula);
            System.out.println(m.groupCount());
            while (m.find()) {
                System.out.println(m.group("formulaList") + " " + m.group("count"));
            }
        }
        else {

            Pattern p = Pattern.compile(atomRegex);
            Matcher m = p.matcher(formula);
            while (m.find()) {
                System.out.println(m.group("elemName") + " " + m.group("count"));
            }
        }

    }
}