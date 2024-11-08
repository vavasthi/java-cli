package org.avasthi.java.cli;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JohnsonTrotter {
    class Letter {
        Letter(short number) {
            this.number = number;
            pointedTo = 0; // -1 means pointing to left, +1 means pointing to right.
        }
        Letter(short number, byte pointedTo) {
            this.number = number;
            this.pointedTo = pointedTo;
        }
        void flip() {
            pointedTo = (byte) (pointedTo == 0b0 ? 0b1 : 0b0);
        }
        Letter deepCopy() {
            return new Letter(this.number, this.pointedTo);
        }
        int pointedTo() {
            return pointedTo == 0b0 ? -1 : 1;
        }
        short number;
        private byte pointedTo;
    }

    /**
     * This function creates permutations from a range of number.
     * @param numberRange for any valye of numberRange, the numbers 0 to numberRange - 1 are used.
     * @return
     */
    List<Letter[]> permutations(short numberRange) {
        List<Letter[]> output = new ArrayList<>();
        Letter[] letters = new Letter[numberRange];
        for (short i = 0; i < numberRange; ++i) {
            letters[i] = new Letter(i);
        }
        output.add(clone(letters));
        while(permute(letters)) {
            output.add(clone(letters));
        }
        return output;
    }
    boolean permute(Letter[] letters) {
        int maxMobileNum = -1;
        int maxMobileIndex = -1;
        for (int i = 0; i < letters.length; ++i) {
            if (mobile(letters, i) && maxMobileNum < letters[i].number) {
                maxMobileNum = letters[i].number;
                maxMobileIndex = i;
            }
        }
        if (maxMobileIndex != -1) {
            swap(letters, maxMobileIndex);
            return true;
        }
        return false;
    }
    boolean mobile(Letter[] letters, int index) {
        int pointedToIndex = index + letters[index].pointedTo();
        if (pointedToIndex < 0 || pointedToIndex >= letters.length) {
            return false;
        }
        if (letters[index].number > letters[pointedToIndex].number) {
            return true;
        }
        return false;
    }
    void swap(Letter[] letters, int index) {
        int swappedIndex = index + letters[index].pointedTo();
        if (swappedIndex >= 0 && swappedIndex < letters.length) {
            Letter temp = letters[index];
            letters[index] = letters[swappedIndex];
            letters[swappedIndex] = temp;
        }
        for (int i = 0; i < letters.length; ++i) {
            if (letters[i].number > letters[swappedIndex].number) {
                letters[i].flip();
            }
        }
    }
    Letter[] clone(Letter[] array) {
        Letter[] output = new Letter[array.length];
        for (int i = 0; i < array.length; ++i) {
            output[i] = array[i].deepCopy();
        }
        return output;
    }
    void print(Letter[] lt) {
        Arrays.stream(lt).forEach(l -> {
            System.out.print(String.format("%d[%d] ", l.number, l.pointedTo()));
        });
        System.out.println();

    }
    List<String> generateStrings(List<Letter[]> permutations, String[] words) {
        List<String> output = new ArrayList<>();
        for (Letter[] letters: permutations) {
            String s = "";
            for (int i = 0; i < letters.length; ++i) {
                s += words[letters[i].number];
            }
            output.add(s);
        }
        return output;
    }
    public List<Integer> findSubstring(String s, String[] words) {

        List<Letter[]> permutations = permutations((short)words.length);
        List<String> substrings = generateStrings(permutations, words);
        Set<Integer> positions = new HashSet<>();
        for (Letter[] letters: permutations) {
            String substring = "";
            for (int i = 0; i < letters.length; ++i) {
                substring += words[letters[i].number];
            }
            Matcher m = Pattern.compile(String.format("(?=(%s))", substring)).matcher(s);
            while(m.find()) {
                positions.add(m.start());
            }
        }
        return positions.stream().sorted().collect(Collectors.toList());
    }

    public static void main(String[] args)  {
        Scanner scanner = new Scanner(System.in);
/*        System.out.print("Enter Range ");
        int range = Integer.parseInt(scanner.nextLine());*/
        short range = 4;
        String[] words = {"foo","bar"};
        JohnsonTrotter jt = new JohnsonTrotter();
        List<Integer> positions = jt.findSubstring("barfoothefoobarman", words);
    }

}
