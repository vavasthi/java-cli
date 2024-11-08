package org.avasthi.java.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Justify {
    class WordWithSpace {
        WordWithSpace(String word) {
            this.word = word;
            this.spacesBefore = 0;
            this.spacesAfter = 0;
        }
        public int length() {
            return word.length() + spacesAfter + spacesBefore;
        }
        public String word;
        public int spacesBefore;
        public int spacesAfter;
    }
    public List<String> fullJustify(String[] words, int maxWidth) {
        List<String> output = new ArrayList<>();
        List<List<WordWithSpace> > outputCandidate = new ArrayList<>();
        if (words.length == 0) {
            return output;
        }
        int i = 0;
        while (i < words.length) {
            if (outputCandidate.size() == 0) {
                outputCandidate.add(new ArrayList<>());
            }
            WordWithSpace wws = new WordWithSpace(words[i]);
            if (outputCandidate.get(outputCandidate.size()-1).size() == 0) {
                outputCandidate.get(outputCandidate.size() - 1).add(wws);
                ++i;
            }
            else {

                if (words[i].length() >= maxWidth) {
                    adjustSpaces(outputCandidate.get(outputCandidate.size()-1), maxWidth, false);
                    outputCandidate.add(new ArrayList<>());
                }
                else if(length(outputCandidate.get(outputCandidate.size()-1)) + 1 + words[i].length() > maxWidth) {

                    adjustSpaces(outputCandidate.get(outputCandidate.size()-1), maxWidth, false);
                    outputCandidate.add(new ArrayList<>());
                }
                else {
                    ++wws.spacesBefore;
                    outputCandidate.get(outputCandidate.size()-1).add(wws);
                    ++i;
                }
            }
        }
        adjustSpaces(outputCandidate.get(outputCandidate.size()-1), maxWidth, true);
        for (List<WordWithSpace> wwsl : outputCandidate) {
            output.add("");
            wwsl.forEach(wws -> {
                String s = output.remove(output.size() - 1);
                for (int j = 0; j < wws.spacesBefore; ++j) {
                    s = s + " ";
                }
                s = s + wws.word;
                for (int j = 0; j < wws.spacesAfter; ++j) {
                    s = s + " ";
                }
                output.add(s);
            });
        }
        return output;
    }
    private void adjustSpaces(List<WordWithSpace> words, int maxWidth, boolean lastLine) {
        if (lastLine) {

            int spaceRemaining = maxWidth - length(words);
            if (spaceRemaining > 0) {
                words.get(words.size() - 1).spacesAfter += spaceRemaining;
            }
        }
        else if (words.size() > 1) {
            int currentLen = length(words);
            int spacesRemaining = maxWidth - currentLen;
            int equallyDistributedSpaces = spacesRemaining / (words.size() - 1);
            for (int i = 1; i < words.size(); ++i) {
                words.get(i).spacesBefore += equallyDistributedSpaces;
            }
            spacesRemaining -= equallyDistributedSpaces * (words.size() - 1);
            while(spacesRemaining > 0) {
                for (int i = 1; i < words.size(); ++i) {
                    ++words.get(i).spacesBefore;
                    --spacesRemaining;
                    if (spacesRemaining == 0) {
                        break;
                    }
                }
            }
        }
        else {
            int spaceRemaining = maxWidth - length(words);
            if (spaceRemaining > 0) {
                words.get(0).spacesAfter = spaceRemaining;
            }
        }
    }
    private int length(List<WordWithSpace> words) {
        int length = 0;
        for (int i = 0; i < words.size(); ++i) {
            length += words.get(i).length();
        }
        return length;
    }
    public static void main(String[] args)  {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Max Width ");
        int maxWidth = Integer.parseInt(scanner.nextLine());
        String line = scanner.nextLine();
        line = line.replaceAll("\"","").replace("[", "").replace("]", "");
        Justify justify = new Justify();
        List<String> words = Arrays.asList(line.split(",")).stream().map(w -> w.strip()).collect(Collectors.toList());
        List<String> output = justify.fullJustify(words.toArray(new String[words.size()]), maxWidth);
        System.out.println();
        output.forEach( s -> {
            System.out.println(s);
        });
    }

    }
