/*
* Abstrakte Basisklasse, von der alle anderen Renderer erben.
* */

package SkyNetJR.Graphics.Rendering;

public abstract class Renderer {
    protected int _positionX;
    protected int _positionY;

    // Methodenprotyp bzw. Methodenmuster
    public abstract void render(int offsetX, int offsetY);

    // Getter und Setter
    public int getPositionX() {
        return _positionX;
    }
    public void setPositionX(int positionX) {
        this._positionX = positionX;
    }

    public int getPositionY() {
        return _positionY;
    }
    public void setPositionY(int positionY) {
        this._positionY = positionY;
    }

    public abstract void destroy();
}
