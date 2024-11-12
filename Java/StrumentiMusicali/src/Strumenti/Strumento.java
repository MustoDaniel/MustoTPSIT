package Strumenti;

public abstract class Strumento {
    protected String marca;
    protected String modello;
    protected String _id;

    public String get_id(){
        return _id;
    }
    public void set_id(String _id){
        this._id = _id;
    }

    protected Strumento(String marca, String modello) {
        this.marca = marca;
        this.modello = modello;
    }

    @Override
    public String toString() {
        return "marca: " + marca + ", modello: " + modello;
    }
}
