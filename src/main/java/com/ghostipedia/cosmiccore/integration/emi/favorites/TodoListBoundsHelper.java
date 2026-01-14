package com.ghostipedia.cosmiccore.integration.emi.favorites;

public class TodoListBoundsHelper {

    private static int todoListX = 0;
    private static int todoListY = 0;
    private static int todoListWidth = 0;
    private static int todoListHeight = 0;
    private static int todoListRows = 0;
    private static int todoListCols = 0;
    private static final int SLOT_SIZE = 18;

    public static void setBounds(int x, int y, int cols, int rows) {
        todoListX = x;
        todoListY = y;
        todoListWidth = cols * SLOT_SIZE;
        todoListHeight = rows * SLOT_SIZE;
        todoListRows = rows;
        todoListCols = cols;
    }

    public static int getX() {
        return todoListX;
    }

    public static int getY() {
        return todoListY;
    }

    public static int getWidth() {
        return todoListWidth;
    }

    public static int getHeight() {
        return todoListHeight;
    }

    public static int getRows() {
        return todoListRows;
    }

    public static int getCols() {
        return todoListCols;
    }

    public static int getSlotSize() {
        return SLOT_SIZE;
    }
}
