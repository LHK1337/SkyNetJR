/*
* Abstrakte Basisklasse, von der alle anderen Renderer erben.
* */

package SkyNetJR.Graphics.Rendering;

public abstract class Renderer {
    protected int positionX;
    protected int positionY;

    // Methodenprotyp bzw. Methodenmuster
    public abstract void Render(int offsetX, int offsetY);

    // Getter und Setter
    public int getPositionX() {
        return positionX;
    }
    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }
    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public abstract void Destroy();
}
