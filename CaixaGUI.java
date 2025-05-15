import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.*;
import javax.swing.text.StyleConstants;

public class CaixaGUI extends JFrame {
    private double saldo = 0.0;
    private double fundoCaixa = 0.0;
    private ArrayList<Saida> saidas = new ArrayList<>();
    private JLabel lblSaldo = new JLabel("Saldo: R$ 0.00");

    public CaixaGUI() {
        setTitle("Controle de Caixa");
        setSize(450, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        JButton btnAbertura = new JButton("Abertura do Caixa");
        JButton btnEntrada = new JButton("Adicionar Entrada");
        JButton btnSaida = new JButton("Adicionar Saída");
        JButton btnFechamento = new JButton("Fechar Caixa");
        JButton btnConsultar = new JButton("Consultar Fechamentos");

        add(lblSaldo);
        add(btnAbertura);
        add(btnEntrada);
        add(btnSaida);
        add(btnFechamento);
        add(btnConsultar);

        btnEntrada.setEnabled(false);
        btnSaida.setEnabled(false);
        btnFechamento.setEnabled(false);

        btnAbertura.addActionListener(e -> {
            String valor = JOptionPane.showInputDialog(this, "Valor do fundo de caixa:");
            if (valor == null || valor.trim().isEmpty()) return;
            valor = valor.replace(",", ".");
            try {
                fundoCaixa = Double.parseDouble(valor);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valor inválido!");
                return;
            }
            saldo = fundoCaixa;
            atualizarSaldo();
            btnEntrada.setEnabled(true);
            btnSaida.setEnabled(true);
            btnFechamento.setEnabled(true);
        });

        btnEntrada.addActionListener(e -> {
            String valor = JOptionPane.showInputDialog(this, "Valor da entrada:");
            if (valor == null || valor.trim().isEmpty()) return;
            valor = valor.replace(",", ".");
            try {
                saldo += Double.parseDouble(valor);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valor inválido!");
                return;
            }
            atualizarSaldo();
        });

        btnSaida.addActionListener(e -> {
            String valor = JOptionPane.showInputDialog(this, "Valor da saída:");
            if (valor == null || valor.trim().isEmpty()) return;
            String descricao = JOptionPane.showInputDialog(this, "Descrição da saída:");
            if (descricao == null || descricao.trim().isEmpty()) return;
            valor = valor.replace(",", ".");
            double v;
            try {
                v = Double.parseDouble(valor);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valor inválido!");
                return;
            }
            saldo -= v;
            saidas.add(new Saida(v, descricao));
            atualizarSaldo();
        });

        btnFechamento.addActionListener(e -> {
            while (true) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                StringBuilder relatorio = new StringBuilder();
                relatorio.append("Relatório de Saídas:\n");
                double totalSaidas = 0.0;
                for (Saida s : saidas) {
                    relatorio.append(String.format("R$ %.2f - %s - %s\n",
                            s.getValor(), s.getDescricao(), s.getHorario().format(formatter)));
                    totalSaidas += s.getValor();
                }
                relatorio.append("\nFundo de Caixa: R$ " + String.format("%.2f", fundoCaixa));
                relatorio.append("\nTotal de Saídas: R$ " + String.format("%.2f", totalSaidas));
                relatorio.append("\nSaldo Final (sistema): R$ " + String.format("%.2f", saldo));

                // Input do valor físico
                String valorFisicoStr = JOptionPane.showInputDialog(this, "Digite o valor contado no caixa físico:");
                double valorFisico = 0.0;
                if (valorFisicoStr == null) return; // Cancelou
                if (!valorFisicoStr.trim().isEmpty()) {
                    valorFisicoStr = valorFisicoStr.replace(",", ".");
                    try {
                        valorFisico = Double.parseDouble(valorFisicoStr);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Valor físico inválido! Considerando R$ 0,00.");
                    }
                }
                double diferenca = valorFisico - saldo;
                relatorio.append("\nValor no Caixa Físico: R$ " + String.format("%.2f", valorFisico));
                relatorio.append("\nDiferença (Físico - Sistema): R$ " + String.format("%.2f", diferenca));
                if (diferenca > 0) {
                    relatorio.append("\nSOBROU dinheiro no caixa.");
                } else if (diferenca < 0) {
                    relatorio.append("\nATENÇÃO: Faltou dinheiro no caixa!");
                } else {
                    relatorio.append("\nCaixa conferido sem diferenças.");
                }

                // Mostra relatório com cor na diferença
                JTextPane textPane = new JTextPane();
                textPane.setText(relatorio.toString());
                textPane.setEditable(false);

                // Destaca a diferença em verde ou vermelho
                try {
                    int idx = textPane.getText().indexOf("Diferença (Físico - Sistema):");
                    if (idx >= 0) {
                        int start = idx + "Diferença (Físico - Sistema): R$ ".length();
                        int end = textPane.getText().indexOf("\n", start);
                        if (end == -1) end = textPane.getText().length();
                        javax.swing.text.StyledDocument doc = textPane.getStyledDocument();
                        javax.swing.text.Style style = textPane.addStyle("Color", null);
                        if (diferenca > 0) {
                            StyleConstants.setForeground(style, Color.GREEN.darker());
                        } else if (diferenca < 0) {
                            StyleConstants.setForeground(style, Color.RED);
                        }
                        doc.setCharacterAttributes(start, end - start, style, false);
                    }
                } catch (Exception ex) {
                    // Se der erro, apenas mostra o texto normal
                }

                JScrollPane scrollPane = new JScrollPane(textPane);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                JOptionPane.showMessageDialog(this, scrollPane, "Relatório de Fechamento", JOptionPane.INFORMATION_MESSAGE);

                // Pergunta se deseja ajustar
                int opcao = JOptionPane.showConfirmDialog(this,
                        "Deseja adicionar mais entradas ou saídas antes de fechar o caixa?",
                        "Ajustar Caixa", JOptionPane.YES_NO_OPTION);

                if (opcao == JOptionPane.YES_OPTION) {
                    boolean ajustando = true;
                    while (ajustando) {
                        String[] opcoes = {"Adicionar Entrada", "Adicionar Saída", "Finalizar"};
                        int escolha = JOptionPane.showOptionDialog(this, "Escolha uma ação:",
                                "Ajustar Caixa", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                                null, opcoes, opcoes[0]);
                        if (escolha == 0) { // Entrada
                            String valor = JOptionPane.showInputDialog(this, "Valor da entrada:");
                            if (valor == null || valor.trim().isEmpty()) continue;
                            valor = valor.replace(",", ".");
                            try {
                                saldo += Double.parseDouble(valor);
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(this, "Valor inválido!");
                                continue;
                            }
                            atualizarSaldo();
                        } else if (escolha == 1) { // Saída
                            String valor = JOptionPane.showInputDialog(this, "Valor da saída:");
                            if (valor == null || valor.trim().isEmpty()) continue;
                            String descricao = JOptionPane.showInputDialog(this, "Descrição da saída:");
                            if (descricao == null || descricao.trim().isEmpty()) continue;
                            valor = valor.replace(",", ".");
                            double v;
                            try {
                                v = Double.parseDouble(valor);
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(this, "Valor inválido!");
                                continue;
                            }
                            saldo -= v;
                            saidas.add(new Saida(v, descricao));
                            atualizarSaldo();
                        } else {
                            ajustando = false;
                        }
                    }
                    continue; // Volta para o início do fechamento para refazer o relatório
                } else {
                    // Salva relatório em arquivo
                    salvarRelatorio(relatorio.toString());

                    // Agora zera tudo e atualiza a tela
                    saldo = 0.0;
                    fundoCaixa = 0.0;
                    saidas.clear();
                    atualizarSaldo();
                    btnEntrada.setEnabled(false);
                    btnSaida.setEnabled(false);
                    btnFechamento.setEnabled(false);
                    JOptionPane.showMessageDialog(this, "Caixa reiniciado para nova abertura.");
                    break;
                }
            }
        });

        btnConsultar.addActionListener(e -> {
            File dir = new File("fechamentos");
            if (!dir.exists() || dir.listFiles() == null || dir.listFiles().length == 0) {
                JOptionPane.showMessageDialog(this, "Nenhum fechamento encontrado.");
                return;
            }
            File[] arquivos = dir.listFiles((d, name) -> name.endsWith(".txt"));
            String[] nomes = new String[arquivos.length];
            for (int i = 0; i < arquivos.length; i++) {
                nomes[i] = arquivos[i].getName();
            }
            String escolhido = (String) JOptionPane.showInputDialog(this, "Escolha um fechamento:", "Consultar Fechamento",
                    JOptionPane.PLAIN_MESSAGE, null, nomes, nomes[0]);
            if (escolhido != null) {
                try (BufferedReader reader = new BufferedReader(new FileReader(new File(dir, escolhido)))) {
                    StringBuilder conteudo = new StringBuilder();
                    String linha;
                    while ((linha = reader.readLine()) != null) {
                        conteudo.append(linha).append("\n");
                    }
                    JTextArea area = new JTextArea(conteudo.toString());
                    area.setEditable(false);
                    JScrollPane scroll = new JScrollPane(area);
                    scroll.setPreferredSize(new Dimension(400, 300));
                    JOptionPane.showMessageDialog(this, scroll, "Fechamento: " + escolhido, JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao ler o arquivo: " + ex.getMessage());
                }
            }
        });
    }

    private void atualizarSaldo() {
        lblSaldo.setText(String.format("Saldo: R$ %.2f", saldo));
    }

    private void salvarRelatorio(String texto) {
        try {
            File dir = new File("fechamentos");
            if (!dir.exists()) dir.mkdir();
            String nomeArquivo = "fechamentos/fechamento__" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy__HH-mm")) + ".txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo))) {
                writer.write(texto);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar relatório: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CaixaGUI().setVisible(true));
    }
}