package com.parking.system.ui;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Wires "type to filter" behaviour onto an editable JComboBox: as the user types in the
 * editor, the dropdown narrows to items whose display text contains what they typed.
 *
 * <p>Replacing the combo box's model on every keystroke also makes Swing fire the same
 * ActionEvent it fires for a genuine user selection, so a naive implementation ends up
 * treating "the user typed a letter" the same as "the user picked an item". This class
 * guards against that: {@link #onCommit} only fires for real selections (a popup click,
 * Enter, or an explicit {@link #resetItems} call), never for the filtering churn.
 */
public final class ComboBoxFilter {

    private static final String FILTERING_PROPERTY = "comboBoxFilter.isFiltering";

    private ComboBoxFilter() {
    }

    public static <T> void install(JComboBox<T> comboBox, Supplier<List<T>> allItemsSupplier,
                                    Function<T, String> textOf) {
        comboBox.setEditable(true);
        JTextComponent editor = (JTextComponent) comboBox.getEditor().getEditorComponent();

        // Whatever item is currently shown (e.g. the first brand, auto-selected by resetItems)
        // should be replaced, not appended to, the moment the user starts typing a search - so
        // select it all as soon as the field gets focus, same as clicking into a spreadsheet cell.
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(editor::selectAll);
            }
        });

        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            private void refilter() {
                if (isFiltering(comboBox)) {
                    return;
                }
                // A JComboBox/JTextComponent document must not be mutated from within its own
                // change notification (Swing throws "Attempt to mutate in notification" if you
                // try) - setModel()/setText() below both mutate it, so defer to the next EDT
                // cycle, once this insert/removeUpdate call has finished.
                SwingUtilities.invokeLater(() -> {
                    if (isFiltering(comboBox)) {
                        return;
                    }
                    String typed = editor.getText();
                    List<T> matches = typed.isEmpty()
                            ? allItemsSupplier.get()
                            : allItemsSupplier.get().stream()
                                .filter(item -> textOf.apply(item).toLowerCase().contains(typed.toLowerCase()))
                                .toList();

                    setFiltering(comboBox, true);
                    DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
                    for (T item : matches) {
                        model.addElement(item);
                    }
                    comboBox.setModel(model);
                    editor.setText(typed);
                    editor.setCaretPosition(typed.length());
                    setFiltering(comboBox, false);

                    if (comboBox.isShowing()) {
                        if (!matches.isEmpty()) {
                            comboBox.showPopup();
                        } else {
                            comboBox.hidePopup();
                        }
                    }
                });
            }
        });
    }

    /** Registers a listener that fires only for genuine selections, not the filtering churn above. */
    public static <T> void onCommit(JComboBox<T> comboBox, Runnable listener) {
        comboBox.addActionListener(e -> {
            if (!isFiltering(comboBox)) {
                listener.run();
            }
        });
    }

    /** Replaces the full item list (e.g. after loading brands, or switching the selected brand's models). */
    public static <T> void resetItems(JComboBox<T> comboBox, List<T> items, T selected) {
        setFiltering(comboBox, true);
        DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
        for (T item : items) {
            model.addElement(item);
        }
        comboBox.setModel(model);
        setFiltering(comboBox, false);

        if (selected != null) {
            comboBox.setSelectedItem(selected);
        } else if (model.getSize() > 0) {
            comboBox.setSelectedIndex(0);
        }
    }

    private static boolean isFiltering(JComboBox<?> comboBox) {
        return Boolean.TRUE.equals(comboBox.getClientProperty(FILTERING_PROPERTY));
    }

    private static void setFiltering(JComboBox<?> comboBox, boolean value) {
        comboBox.putClientProperty(FILTERING_PROPERTY, value);
    }
}
