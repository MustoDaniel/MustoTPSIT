public class Film {
    public String id;
    public String nome;
    public int annoUscita;
    public String regista;

    public Film(String id, String nome, int annoUscita, String regista) {
        this.id = id;
        this.nome = nome;
        this.annoUscita = annoUscita;
        this.regista = regista;
    }

    @Override
    public String toString(){
        return "id: " + id + "; " +  "nome: " + nome + "; " + "annoUscita: " + annoUscita + "; " + "regista: " + regista;
    }
}
