package org.example;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class MainWindow extends JFrame {

    private final JTextField inputField;
    private final JTextField resultField;

    private final JButton hashTextButton;
    private final JButton hashFileButton;
    private final JButton saveButton;
    private final JButton infoButton;

    private String lastHash = "";

    public MainWindow() {
        setTitle("MD5 Hash Analyzer (Java)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 320);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel labelInput = new JLabel("Введите текст: ");
        labelInput.setBounds(20, 20, 150, 25);
        add(labelInput);

        inputField = new JTextField();
        inputField.setBounds(20, 50, 540, 25);
        add(inputField);

        hashTextButton = new JButton("Хэшировать текст");
        hashTextButton.setBounds(20, 90, 170, 30);
        hashTextButton.addActionListener(this::onHashText);
        add(hashTextButton);

        hashFileButton = new JButton("Хэшировать файл");
        hashFileButton.setBounds(200, 90, 170, 30);
        hashFileButton.addActionListener(this::onHashFile);
        add(hashFileButton);

        saveButton = new JButton("Сохранить хэш в файл");
        saveButton.setBounds(380, 90, 180, 30);
        saveButton.addActionListener(this::onSaveHash);
        add(saveButton);

        infoButton = new JButton("Справка");
        infoButton.setBounds(20, 220, 120, 30);
        infoButton.addActionListener(e -> showInfo());
        add(infoButton);

        JLabel labelResult = new JLabel("Результат MD5: ");
        labelResult.setBounds(20, 140, 150, 25);
        add(labelResult);

        resultField = new JTextField();
        resultField.setEditable(false);
        resultField.setBounds(20, 170, 540, 25);
        add(resultField);
    }

    private void onHashText(ActionEvent e) {
        String text = inputField.getText();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите текст для хэширования", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        lastHash = MD5.hash(text.getBytes());
        resultField.setText(lastHash);
    }

    private void onHashFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            try {
                lastHash = MD5.hashFromFile(file);
                resultField.setText(lastHash);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при чтении файла: \n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onSaveHash(ActionEvent e) {
        if (lastHash.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет результата для сохранения!", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        MD5.saveToFile(lastHash);
        JOptionPane.showMessageDialog(this, "Хэш успешно сохранён в файл", "Успех", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showInfo() {
        JOptionPane.showMessageDialog(this,
                """
                        Студент: Белянин Никита Николаевич
                        Группа: ПИбд-41
                        Вариант №6
                                        
                        Алгоритм MD5 — криптографическая хэш-функция,
                        создающая 128-битное (32-символьное) представление данных.
                                        
                         Этапы алгоритма:
                           - Дополнение данных до длины 512 бит - 64.
                           - Разбиение на блоки по 512 бит.
                           - Инициализация 4-х регистров (A, B, C, D).
                           - 64 раунда с функциями F, G, H, I и сдвигами.
                           - Склеивание результата в 128-битный хэш.
                                        
                          Программа позволяет вычислить MD5 хэш строки или файла
                          и по желанию сохранить результат
                                       
                        """,
                "Справочная информация",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
