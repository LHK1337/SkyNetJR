/*
* Diese Klasse NeuralProperty<T> ist eine Schnittstelle zwischen einer Eigenschaft einer Kreatur und einem Neuronalen Netz.
* Sie kann als Eingang oder als Ausgang verwendet werden.
* */

package SkyNetJR.AI;

public class NeuralProperty<T> {
    private T _value;                    // Wert der gebundene Eigenschaft
    private NeuralPropertyType _type;    // Type der Eigenschaft
    private Byte _tag;                   // Erweiterung zur Identifikation der Eigenschaft

    public NeuralProperty(NeuralPropertyType t){
        _type = t;
        _tag = 0;
    }
    public NeuralProperty(T value, NeuralPropertyType t) {
        this(t);
        _value = value;
    }
    public NeuralProperty(NeuralPropertyType t, byte tag){
        _type = t;
        _tag = tag;
    }
    public NeuralProperty(T value, NeuralPropertyType t, byte tag) {
        this(t, tag);
        _value = value;
    }

    // Getter und Setter
    public T getValue(){
        return _value;
    }
    public void setValue(T value){
        _value = value;
    }

    public Byte getTag() {
        return _tag;
    }
    public void setTag(Byte tag) {
        _tag = tag;
    }

    public NeuralPropertyType getType(){
        return _type;
    }
    public void setType(NeuralPropertyType value){
        _type = value;
    }
}
