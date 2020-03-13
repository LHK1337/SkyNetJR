/*
* Diese Klasse NeuralProperty<T> ist eine Schnittstelle zwischen einer Eigenschaft einer Kreatur und einem Neuronalen Netz.
* Sie kann als Eingang oder als Ausgang verwendet werden.
* */

package SkyNetJR.AI;

public class NeuralProperty<T> {
    private T Value;                    // Wert der gebundene Eigenschaft
    private NeuralPropertyType Type;    // Type der Eigenschaft
    private Byte Tag;                   // Erweiterung zur Identifikation der Eigenschaft

    public NeuralProperty(NeuralPropertyType t){
        Type = t;
        Tag = 0;
    }
    public NeuralProperty(T value, NeuralPropertyType t) {
        this(t);
        Value = value;
    }
    public NeuralProperty(NeuralPropertyType t, byte tag){
        Type = t;
        Tag = tag;
    }
    public NeuralProperty(T value, NeuralPropertyType t, byte tag) {
        this(t, tag);
        Value = value;
    }

    // Getter und Setter
    public T getValue(){
        return Value;
    }
    public void setValue(T value){
        Value = value;
    }

    public Byte getTag() {
        return Tag;
    }
    public void setTag(Byte tag) {
        Tag = tag;
    }

    public NeuralPropertyType getType(){
        return Type;
    }
    public void setType(NeuralPropertyType value){
        Type = value;
    }
}
