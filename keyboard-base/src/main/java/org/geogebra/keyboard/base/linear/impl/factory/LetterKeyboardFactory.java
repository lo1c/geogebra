package org.geogebra.keyboard.base.linear.impl.factory;

import org.geogebra.keyboard.base.linear.LinearKeyboard;
import org.geogebra.keyboard.base.linear.impl.LinearKeyboardImpl;
import org.geogebra.keyboard.base.linear.impl.RowImpl;

import static org.geogebra.keyboard.base.ButtonConstants.ACTION_BACKSPACE;
import static org.geogebra.keyboard.base.ButtonConstants.ACTION_CAPS_LOCK;
import static org.geogebra.keyboard.base.ButtonConstants.ACTION_LEFT;
import static org.geogebra.keyboard.base.ButtonConstants.ACTION_RETURN;
import static org.geogebra.keyboard.base.ButtonConstants.ACTION_RIGHT;
import static org.geogebra.keyboard.base.ButtonConstants.RESOURCE_BACKSPACE;
import static org.geogebra.keyboard.base.ButtonConstants.RESOURCE_CAPS_LOCK;
import static org.geogebra.keyboard.base.ButtonConstants.RESOURCE_LEFT_ARROW;
import static org.geogebra.keyboard.base.ButtonConstants.RESOURCE_RETURN;
import static org.geogebra.keyboard.base.ButtonConstants.RESOURCE_RIGHT_ARROW;
import static org.geogebra.keyboard.base.linear.impl.factory.Util.addButton;
import static org.geogebra.keyboard.base.linear.impl.factory.Util.addConstantCustomButton;
import static org.geogebra.keyboard.base.linear.impl.factory.Util.addInputButton;
import static org.geogebra.keyboard.base.linear.impl.factory.Util.createEmptySpace;

class LetterKeyboardFactory {

    private static final double MIN_PADDING_WEIGHT = 1.e-4;
    private static final String EXCEPTION_MESSAGE = "Deformed keyboard definition with long bottom row.";

    LinearKeyboard createLetterKeyboard(String topRow, String middleRow, String bottomRow) {
        int topRowLength = topRow.length();
        int middleRowLength = middleRow.length();
        int bottomRowLength = bottomRow.length();

        // sanity checks
        if (bottomRowLength > topRowLength - 1 || bottomRowLength > middleRowLength - 1) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }

        int rowWeightSum;
        if (topRowLength == middleRowLength && middleRowLength - 1 == bottomRowLength) {
            rowWeightSum = middleRowLength + 1;
        } else {
            rowWeightSum = Math.max(topRowLength, middleRowLength);
        }

        float topRowPadding = (rowWeightSum - topRowLength) / 2.0f;
        float middleRowPadding = (rowWeightSum - middleRowLength) / 2.0f;

        float actionButtonSize;
        float actionButtonMargin;
        if (rowWeightSum - bottomRowLength == 2) {
            actionButtonSize = 1.0f;
            actionButtonMargin = 0.0f;
        } else if (rowWeightSum - bottomRowLength > 2) {
            actionButtonSize = 1.2f;
            actionButtonMargin = (rowWeightSum - bottomRowLength - 2.4f) / 2.0f;
        } else {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }
        float spaceSize = rowWeightSum - 5;

        LinearKeyboardImpl letterKeyboard = new LinearKeyboardImpl();

        createRow(letterKeyboard, topRow, rowWeightSum, topRowPadding);
        createRow(letterKeyboard, middleRow, rowWeightSum, middleRowPadding);

        RowImpl bottomRowImpl = letterKeyboard.nextRow(rowWeightSum);

        addConstantCustomButton(bottomRowImpl, RESOURCE_CAPS_LOCK, ACTION_CAPS_LOCK, actionButtonSize);
        addButton(bottomRowImpl, createEmptySpace(actionButtonMargin));
        addButtons(bottomRowImpl, bottomRow);
        addButton(bottomRowImpl, createEmptySpace(actionButtonMargin));
        addConstantCustomButton(bottomRowImpl, RESOURCE_BACKSPACE, ACTION_BACKSPACE, actionButtonSize);

        RowImpl controlRow = letterKeyboard.nextRow(rowWeightSum);
        addInputButton(controlRow, ",");
        addInputButton(controlRow, "'");
        addInputButton(controlRow, " ", spaceSize);
        addConstantCustomButton(controlRow, RESOURCE_LEFT_ARROW, ACTION_LEFT);
        addConstantCustomButton(controlRow, RESOURCE_RIGHT_ARROW, ACTION_RIGHT);
        addConstantCustomButton(controlRow, RESOURCE_RETURN, ACTION_RETURN);

        return letterKeyboard;
    }

    private void createRow(LinearKeyboardImpl keyboard, String definition, float rowWeightSum, float rowPadding) {
        RowImpl rowImpl = keyboard.nextRow(rowWeightSum);
        addPaddingIfNecessary(rowImpl, rowPadding);
        addButtons(rowImpl, definition);
        addPaddingIfNecessary(rowImpl, rowPadding);
    }

    private void addButtons(RowImpl rowImpl, String definition) {
        for (int i = 0; i < definition.length(); i++) {
            addInputButton(rowImpl, String.valueOf(definition.charAt(i)));
        }
    }

    private void addPaddingIfNecessary(RowImpl rowImpl, float paddingWeight) {
        if (paddingWeight > MIN_PADDING_WEIGHT) {
            addButton(rowImpl, createEmptySpace(paddingWeight));
        }
    }
}