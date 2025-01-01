package com.ruslooob.fxcontrols.column_utils;

import com.ruslooob.fxcontrols.enums.ColumnType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Callback;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.ruslooob.fxcontrols.Utils.*;

// todo подумать над тем, нужно ли это вообще в моей библиотеке. Можно переложить на пользователя ответственность за создание CellValueFactory. Пока оставлю это здесь
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CellFactoryBuilder {
    ColumnType type;
    boolean copyContextMenu = false;
    Clipboard clipboard = Clipboard.getSystemClipboard();

    public static CellFactoryBuilder withType(ColumnType type) {
        var builder = new CellFactoryBuilder();
        builder.type = type;
        return builder;
    }

    public CellFactoryBuilder copyContextMenu() {
        this.copyContextMenu = true;
        return this;
    }

    public <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> build() {
        switch (type) {
            case DATE -> {
                return column -> new TableCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            LocalDate date = (LocalDate) item;
                            //todo add dateFormatter in params
                            String cellContent = date.format(dateFormatter);
                            setText(cellContent);
                            if (copyContextMenu) {
                                var contextMenu = new ContextMenu();
                                contextMenu.getItems().add(createCopyMenuItem(cellContent));
                                setContextMenu(contextMenu);
                            }
                        }
                    }
                };
            }
            case DATE_TIME -> {
                return column -> new TableCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            LocalDateTime date = (LocalDateTime) item;
                            //todo add dateTimeFormatter in map params
                            String cellContent = date.format(dateTimeFormatter);
                            setText(cellContent);
                            if (copyContextMenu) {
                                var contextMenu = new ContextMenu();
                                contextMenu.getItems().add(createCopyMenuItem(cellContent));
                                setContextMenu(contextMenu);
                            }
                        }
                    }
                };
            }
            case TIME -> {
                return column -> new TableCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            LocalTime date = (LocalTime) item;
                            //todo add TimeFormatter in map params
                            String cellContent = date.format(timeFormatter);
                            setText(cellContent);
                            if (copyContextMenu) {
                                var contextMenu = new ContextMenu();
                                contextMenu.getItems().add(createCopyMenuItem(cellContent));
                                setContextMenu(contextMenu);
                            }
                        }
                    }
                };
            }
            case BOOL -> {
                return column -> new TableCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            //todo add dateFormatter in params
                            boolean bool = (boolean) item;
                            String cellContent = bool ? "Да" : "Нет";
                            setText(cellContent);
                            if (copyContextMenu) {
                                var contextMenu = new ContextMenu();
                                contextMenu.getItems().add(createCopyMenuItem(cellContent));
                                setContextMenu(contextMenu);
                            }
                        }
                    }
                };
            }
            default -> throw new IllegalArgumentException("unknown type: %s".formatted(type));
        }
    }

    private MenuItem createCopyMenuItem(String textToCopy) {
        var copyItem = new MenuItem("Копировать");
        copyItem.setOnAction(event -> {
            var content = new ClipboardContent();
            content.putString(textToCopy);
            clipboard.setContent(content);
        });
        return copyItem;
    }

}
