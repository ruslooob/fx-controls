package com.ruslooob.fxcontrols.controls;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class MaskedTextField extends TextField {
    static final char MASK_ESCAPE = '\'';
    static final char MASK_NUMBER = '#';
    static final char MASK_CHARACTER = '?';
    static final char MASK_HEXADECIMAL = 'H';
    static final char MASK_UPPER_CHARACTER = 'U';
    static final char MASK_LOWER_CHARACTER = 'L';
    static final char MASK_CHAR_OR_NUM = 'A';
    static final char MASK_ANYTHING = '*';

    @Getter
    char placeholder;
    StringProperty maskProperty;
    StringProperty plainTextProperty;
    StringBuilder plainTextBuilder;

    List<MaskCharacter> semanticMask;

    public MaskedTextField() {
        this("", '_');
    }

    public MaskedTextField(String mask) {
        this(mask, '_');
    }

    public MaskedTextField(String mask, char placeholder) {
        this.maskProperty = new SimpleStringProperty(this, "mask", mask);
        this.placeholder = placeholder;
        this.plainTextProperty = new SimpleStringProperty(this, "plaintext", "");
        this.plainTextBuilder = new StringBuilder();
        this.semanticMask = new ArrayList<>();

        init();
    }

    private void init() {
        buildSemanticMask();
        updateSemanticMask("");

        // When MaskedTextField gains focus caret goes to first placeholder position
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> {
                    int pos = firstPlaceholderPosition();
                    selectRange(pos, pos);
                    positionCaret(pos);
                });
            }
        });

        // Add a listener to the plain text property so that binding will properly update the formatting as well
        plainTextProperty.addListener((observable, oldValue, newValue) -> updateSemanticMask(newValue));
    }

    /**
     * Build internal mask from input mask using AbstractFactory to add the right MaskCharacter.
     */
    private void buildSemanticMask() {
        char[] mask = getMask().toCharArray();
        int i = 0;
        int length = mask.length;

        semanticMask.clear();

        MaskFactory factory = new MaskFactory();
        while (i < length) {
            char maskValue = mask[i];

            // If the actual char is MASK_ESCAPE look the next char as literal
            if (maskValue == MASK_ESCAPE) {
                semanticMask.add(factory.createMask(maskValue, mask[i + 1]));
                i++;
            } else {
                char value = isLiteral(maskValue) ? maskValue : placeholder;
                semanticMask.add(factory.createMask(maskValue, value));
            }

            i++;
        }
    }

    // triggers after each text change
    private void updateSemanticMask(String newText) {
        //reset mask before update
        semanticMask.forEach(maskCharacter -> maskCharacter.setValue(placeholder));
        setPlainTextWithoutUpdate(getValidText(newText));

        String newMask = semanticMask.stream()
                .map(maskCharacter -> String.valueOf(maskCharacter.getValue()))
                .collect(Collectors.joining());
        setText(newMask);
    }

    private void resetSemanticMask() {
        //set all semanticMask characters to placeholder
        semanticMask.forEach(maskCharacter -> maskCharacter.setValue(placeholder));
    }

    // метод также мутирует переменные в semantic mask
    private String getValidText(String input) {
        var inputText = new StringBuilder(input);
        var validText = new StringBuilder();

        int maskPosition = 0;
        int textPosition = 0;

        while (textPosition < input.length() && maskPosition < semanticMask.size()) {
            MaskCharacter maskCharacter = semanticMask.get(maskPosition);

            if (!maskCharacter.isLiteral()) {
                char ch = inputText.charAt(textPosition);

                if (maskCharacter.accept(ch)) {
                    maskCharacter.setValue(ch); //this sense like code smell
                    validText.append(maskCharacter.getValue());
                    maskPosition++;
                }

                textPosition++;
            } else {
                maskPosition++;
            }
        }

        return validText.toString();
    }

    private void setPlainTextWithoutUpdate(String text) {
        plainTextBuilder.setLength(0);
        plainTextBuilder.append(text);
        //заставляет вызывать родительскую функцию updateSemanticMask дважды, но почему-то рекурсивного зацикления не случается
        plainTextProperty.set(text);
    }

    @Override
    public void replaceText(int start, int end, String newText) {
        int position = convertToPlainTextPosition(start);
        int endPosition = convertToPlainTextPosition(end);

        String newString;
        if (start != end) {
            newString = plainTextBuilder.replace(position, endPosition, newText).toString();
        } else {
            newString = plainTextBuilder.insert(position, newText).toString();
        }
        updateSemanticMask(newString);

        int newCaretPosition = convertToMaskPosition(position + newText.length());
        selectRange(newCaretPosition, newCaretPosition);
    }

    /**
     * Given a position in mask convert it into plainText position
     */
    private int convertToPlainTextPosition(int maskPos) {
        int count = 0;

        for (int i = 0; i < semanticMask.size() && i < maskPos; i++) {
            MaskCharacter m = semanticMask.get(i);
            if (m.getValue() != placeholder && !m.isLiteral()) {//why we need skip placeholders?
                count++;
            }
        }

        return count;
    }

    /**
     * Given a position in plain text convert it into mask position
     */
    private int convertToMaskPosition(int plainTextPos) {
        int countLiterals = 0;
        int countNonLiterals = 0;

        for (int i = 0; i < semanticMask.size() && countNonLiterals < plainTextPos; i++) {
            if (semanticMask.get(i).isLiteral()) {
                countLiterals++;
            } else {
                countNonLiterals++;
            }
        }

        return countLiterals + countNonLiterals;
    }

    public String getPlainText() {
        return plainTextProperty.get();
    }

    public void setPlainText(String text) {
        setPlainTextWithUpdate(text);
    }

    private void setPlainTextWithUpdate(String text) {
        String newText = (text != null) ? text : "";
        setPlainTextWithoutUpdate(newText);
        updateSemanticMask(newText);
    }

    public StringProperty plainTextProperty() {
        return this.plainTextProperty;
    }

    public String getMask() {
        return maskProperty.get();
    }

    /**
     * Set input mask, rebuild internal mask and update view.
     *
     * @param mask Mask dictating legal character values.
     */
    public void setMask(String mask) {
        this.maskProperty.set(mask);
        buildSemanticMask();
        updateSemanticMask("");
    }

    public StringProperty maskProperty() {
        return this.maskProperty;
    }

    /**
     * Return true if a given char isn't a mask.
     */
    private boolean isLiteral(char c) {
        return (c != MASK_ANYTHING &&
                c != MASK_CHARACTER &&
                c != MASK_ESCAPE &&
                c != MASK_NUMBER &&
                c != MASK_CHAR_OR_NUM &&
                c != MASK_HEXADECIMAL &&
                c != MASK_LOWER_CHARACTER &&
                c != MASK_UPPER_CHARACTER);
    }

    /**
     * Return the position of first mask with placeholder on value.
     */
    private int firstPlaceholderPosition() {
        for (int i = 0; i < semanticMask.size(); i++) {
            if (semanticMask.get(i).getValue() == placeholder) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Вызывается, когда вставляем элемент в поле ввода
     */
    @Override
    public void replaceSelection(String string) {
        IndexRange range = getSelection();
        if (string.isEmpty()) {
            deleteText(range.getStart(), range.getEnd());
        } else {
            replaceText(range.getStart(), range.getEnd(), string);
        }
    }

    @Override
    public void deleteText(int start, int end) {
        int plainStart = convertToPlainTextPosition(start);
        int plainEnd = convertToPlainTextPosition(end);

        plainTextBuilder.delete(plainStart, plainEnd);
        updateSemanticMask(plainTextBuilder.toString());

        selectRange(start, start);
    }

    @Override
    public void clear() {
        setPlainText("");
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @ToString
    private abstract static class MaskCharacter {
        private char value;

        public boolean isLiteral() {
            return false;
        }

        abstract boolean accept(char value);
    }

    private static class MaskFactory {
        //todo make static method instead of class
        public MaskCharacter createMask(char mask, char value) {
            return switch (mask) {
                case MASK_ANYTHING -> new AnythingCharacter(value);
                case MASK_CHARACTER -> new LetterCharacter(value);
                case MASK_NUMBER -> new NumericCharacter(value);
                case MASK_CHAR_OR_NUM -> new AlphaNumericCharacter(value);
                case MASK_HEXADECIMAL -> new HexCharacter(value);
                case MASK_LOWER_CHARACTER -> new LowerCaseCharacter(value);
                case MASK_UPPER_CHARACTER -> new UpperCaseCharacter(value);
                default -> new LiteralCharacter(value);
            };
        }
    }

    private static class AnythingCharacter extends MaskCharacter {
        public AnythingCharacter(char value) {
            super(value);
        }

        public boolean accept(char value) {
            return true;
        }
    }

    private static class AlphaNumericCharacter extends MaskCharacter {
        public AlphaNumericCharacter(char value) {
            super(value);
        }

        public boolean accept(char value) {
            return Character.isLetterOrDigit(value);
        }
    }

    private static class LiteralCharacter extends MaskCharacter {
        public LiteralCharacter(char value) {
            super(value);
        }

        @Override
        public boolean isLiteral() {
            return true;
        }

        @Override
        public void setValue(char value) {
            // Literal can't be changed
        }

        public boolean accept(char value) {
            return false;
        }
    }

    private static class LetterCharacter extends MaskCharacter {
        public LetterCharacter(char value) {
            super(value);
        }

        public boolean accept(char value) {
            return Character.isLetter(value);
        }
    }

    private static class LowerCaseCharacter extends MaskCharacter {
        public LowerCaseCharacter(char value) {
            super(value);
        }

        @Override
        public char getValue() {
            return Character.toLowerCase(super.getValue());
        }

        public boolean accept(char value) {
            return Character.isLetter(value);
        }
    }

    private static class UpperCaseCharacter extends MaskCharacter {
        public UpperCaseCharacter(char value) {
            super(value);
        }

        @Override
        public char getValue() {
            return Character.toUpperCase(super.getValue());
        }

        public boolean accept(char value) {
            return Character.isLetter(value);
        }
    }

    private static class NumericCharacter extends MaskCharacter {
        public NumericCharacter(char value) {
            super(value);
        }

        @Override
        public boolean accept(char value) {
            return Character.isDigit(value);
        }
    }

    private static class HexCharacter extends MaskCharacter {
        public HexCharacter(char value) {
            super(value);
        }

        @Override
        public boolean accept(char value) {
            return Pattern.matches("[0-9a-fA-F]", String.valueOf(value));
        }
    }
}
