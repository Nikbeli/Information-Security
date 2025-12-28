package org.encrypting;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {
    private JFrame mainFrame;
    private JTextField keyField;
    private JTextField ivField;
    private File inputFile, outputFile;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().showStudentInfo());
    }

    private void showStudentInfo() {
        JOptionPane.showMessageDialog(
                null,
                "<html><b>Студент:</b> " + StudentInfo.FIO + "<br>" +
                        "<b>Группа:</b> " + StudentInfo.GROUP + "<br>" +
                        "<b>Вариант:</b> " + StudentInfo.VARIANT + "<br><br>" +
                        "<b>Задание:</b><br>" + StudentInfo.TASK + "</html>",
                "Сведения о студенте",
                JOptionPane.INFORMATION_MESSAGE
        );
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        mainFrame = new JFrame("Магма (CFB) — Шифрование");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        // Верхняя панель
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Ключ (hex или текст):"), gbc);
        gbc.gridx = 1;
        keyField = new JTextField(40);
        topPanel.add(keyField, gbc);
        gbc.gridx = 2;
        JButton loadKeyBtn = new JButton("Загрузить ключ...");
        loadKeyBtn.addActionListener(this::loadKeyFromFile);
        topPanel.add(loadKeyBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        topPanel.add(new JLabel("IV (16 hex символов):"), gbc);
        gbc.gridx = 1;
        ivField = new JTextField("1234567890ABCDEF");
        ivField.setColumns(20);
        topPanel.add(ivField, gbc);

        mainFrame.add(topPanel, BorderLayout.NORTH);

        // Центр: кнопки
        JPanel centerPanel = new JPanel(new FlowLayout());
        JButton selectInput = new JButton("Выбрать входной файл");
        selectInput.addActionListener(e -> selectFile(true));
        JButton selectOutput = new JButton("Выбрать выходной файл");
        selectOutput.addActionListener(e -> selectFile(false));
        JButton encryptBtn = new JButton("Зашифровать");
        encryptBtn.addActionListener(e -> processFile(true));
        JButton decryptBtn = new JButton("Расшифровать");
        decryptBtn.addActionListener(e -> processFile(false));

        centerPanel.add(selectInput);
        centerPanel.add(selectOutput);
        centerPanel.add(encryptBtn);
        centerPanel.add(decryptBtn);
        mainFrame.add(centerPanel, BorderLayout.CENTER);

        // Лог
        JTextArea logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(logArea);
        mainFrame.add(scroll, BorderLayout.SOUTH);

        // Перенаправление System.out в лог
        System.setOut(new PrintStream(new TextAreaOutputStream(logArea)));

        mainFrame.setSize(800, 500);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private void loadKeyFromFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            try {
                String keyText = FileUtils.readTextFile(chooser.getSelectedFile());
                keyField.setText(keyText);
                System.out.println("Ключ из файла хэширован через SHA-256");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "Ошибка чтения ключа: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void selectFile(boolean input) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            if (input) {
                inputFile = chooser.getSelectedFile();
                System.out.println("Входной файл: " + inputFile.getAbsolutePath());
            } else {
                outputFile = chooser.getSelectedFile();
                System.out.println("Выходной файл: " + outputFile.getAbsolutePath());
            }
        }
    }

    private void processFile(boolean encrypt) {
        if (inputFile == null || outputFile == null) {
            JOptionPane.showMessageDialog(mainFrame, "Выберите входной и выходной файлы!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Обработка ключа
            byte[] keyBytes = parseKey(keyField.getText());
            // Обработка IV
            byte[] ivBytes = parseIV(ivField.getText());

            MagmaCFB cipher = new MagmaCFB(keyBytes, ivBytes);
            byte[] data = FileUtils.readFile(inputFile);
            byte[] result = encrypt ? cipher.encrypt(data) : cipher.decrypt(data);
            FileUtils.writeFile(outputFile, result);

            String op = encrypt ? "Зашифровано" : "Расшифровано";
            System.out.println(op + ": " + outputFile.getAbsolutePath());
            JOptionPane.showMessageDialog(mainFrame, op + " успешно!", "Успех", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private byte[] parseKey(String keyInput) {
        // ВСЕГДА трактуем как пароль
        System.out.println("Ключ обработан как пароль → SHA-256");
        return HashUtils.sha256(keyInput.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] parseIV(String ivInput) {
        ivInput = ivInput.replaceAll("[^0-9A-Fa-f]", "");
        if (ivInput.length() != 16) {
            throw new IllegalArgumentException("IV должен содержать 16 hex-символов");
        }
        return hexToBytes(ivInput);
    }

    private static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    // Вспомогательный класс для перенаправления вывода в JTextArea
    private static class TextAreaOutputStream extends java.io.OutputStream {
        private final JTextArea textArea;

        public TextAreaOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) {
            textArea.append(String.valueOf((char) b));
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }

        @Override
        public void write(byte[] b, int off, int len) {
            textArea.append(new String(b, off, len));
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
}
