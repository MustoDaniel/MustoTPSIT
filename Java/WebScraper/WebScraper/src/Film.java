public class Film {
    public String link;
    public String nome;
    public int annoUscita;
    public String durata;
    public String visibilita;

    public Film(String id, String nome, int annoUscita, String durata, String visibilita) {
        this.link = id;
        this.nome = nome;
        this.annoUscita = annoUscita;
        this.durata = durata;
        this.visibilita = visibilita;
    }

    @Override
    public String toString(){
        return "link: " + link + "; " +  "nome: " + nome + "; " + "annoUscita: " + annoUscita + "; " + "durata: " + durata + "; " + "visibilita: " + visibilita;
    }
}
