package dk.aau.d507e19.warehousesim.storagegrid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import dk.aau.d507e19.warehousesim.Drawable;
import dk.aau.d507e19.warehousesim.GraphicsManager;

public class Tile implements Drawable {

    private static final float GRID_LINE_WIDTH = 0.1f;

    private int posX, posY;
    private Color color;
    public static final int TILE_SIZE = 1;

    public static final Color defaultCenterColor = Color.GRAY;
    public static final Color defaultEdgeColor = Color.BLACK;

    public Tile(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch) {
        renderCenterAndBorder(shapeRenderer, defaultCenterColor);
    }

    public void renderCenterAndBorder(ShapeRenderer shapeRenderer, Color centerColor) {
        renderFilledCenter(shapeRenderer, centerColor);
        renderBorder(shapeRenderer, defaultEdgeColor);
    }

    private void renderBorder(ShapeRenderer shapeRenderer, Color borderColor){
        shapeRenderer.setColor(borderColor);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3f);
        shapeRenderer.rect(posX, posY, TILE_SIZE, TILE_SIZE);
        shapeRenderer.end();
    }

    private void renderFilledCenter(ShapeRenderer shapeRenderer, Color centerColor){
        shapeRenderer.setColor(centerColor);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(posX, posY, TILE_SIZE, TILE_SIZE);
        shapeRenderer.end();
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }
}
