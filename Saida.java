import java.time.LocalDateTime;

public class Saida {
    private double valor;
    private String descricao;
    private LocalDateTime horario;

    public Saida(double valor, String descricao) {
        this.valor = valor;
        this.descricao = descricao;
        this.horario = LocalDateTime.now();
    }

    public double getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getHorario() {
        return horario;
    }
}
