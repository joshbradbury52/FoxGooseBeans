package assignment13;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

public class Display extends JPanel {

	private BufferedImage grassImage, waterImage, boatImage;
	private BufferedImage[] movingImages;
	private boolean boatLeft, boatMoving, gameOver, win;
	private Position[] positions;
	private float boatLoc;
	private int boatCount, counter;
	private View view;

	private SwingWorker<Void, Void> anim1, anim2, anim3;

	public Display(View view) {
		reset();

		this.view = view;
		movingImages = new BufferedImage[Characters.values().length];
		try {
			boatImage = ImageIO.read(new File("src/images/boat.png"));
			grassImage = ImageIO.read(new File("src/images/grass.png"));
			waterImage = ImageIO.read(new File("src/images/water.png"));
			for (int i = 0; i < movingImages.length; i++)
				movingImages[i] = ImageIO.read(new File("src/images/" + Characters.values()[i].name().toLowerCase() + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reset() {
		gameOver = false;
		win = false;

		if (anim1 != null) {
			anim1.cancel(true);
		}

		if (anim2 != null) {
			anim2.cancel(true);
		}
		
		if (anim3 != null) {
			anim3.cancel(true);
		}

		if (boatLeft && boatImage != null) {
			AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-boatImage.getWidth(null), 0);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			boatImage = op.filter(boatImage, null);
			
			for (int i = 0; i < positions.length; i++) {
				if (positions[i] == Position.BOAT) {
					BufferedImage image = movingImages[i];

					tx = AffineTransform.getScaleInstance(-1, 1);
					tx.translate(-image.getWidth(null), 0);
					op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
					movingImages[i] = op.filter(image, null);
				}
			}
		}

		boatLeft = false;
		boatMoving = false;
		boatCount = 0;

		counter = 0;
		positions = new Position[Characters.values().length];
		for (int i = 0; i < positions.length; i++) {
			positions[i] = Position.RIGHT;
		}
	}
	
	public float sinusoidalEasing(float currTime, float startValue, float changeInValue, float duration) {
		return (float) (-changeInValue / 2 * (Math.cos(Math.PI * currTime / duration) - 1) + startValue);
	}

	public void moveBoat() {
		anim1 = new SwingWorker<Void, Void>() {
			protected Void doInBackground() throws Exception {
				if (boatMoving)
					return null;
				boatMoving = true;
				for (int i = 0; i < 2000; i++) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						break;
					}
					boatLoc = (boatLeft ? 1 : -1) * sinusoidalEasing(i, 0, 1, 2000);
					view.repaint();
				}
				boatMoving = false;
				boatLeft = !boatLeft;
				boatLoc = 0;
				AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
				tx.translate(-boatImage.getWidth(null), 0);
				AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
				boatImage = op.filter(boatImage, null);

				for (int i = 0; i < positions.length; i++) {
					if (positions[i] == Position.BOAT) {
						BufferedImage image = movingImages[i];

						tx = AffineTransform.getScaleInstance(-1, 1);
						tx.translate(-image.getWidth(null), 0);
						op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
						movingImages[i] = op.filter(image, null);
					}
				}

				return null;
			}
		};
		anim1.execute();
	}

	public void addToBoat(Characters character) {
		if (boatCount < 2) {
			positions[character.ordinal()] = Position.BOAT;
			boatCount++;
		}
		view.repaint();
	}

	public void moveCharacterRight(Characters character) {
		positions[character.ordinal()] = Position.RIGHT;
		boatCount--;
		view.repaint();
	}

	public void moveCharacterLeft(Characters character) {
		positions[character.ordinal()] = Position.LEFT;
		boatCount--;
		view.repaint();
	}
	
	public void win() {
		win = true;
		anim3 = new SwingWorker<Void, Void>() {
			protected Void doInBackground() throws Exception {
				for (int i = 0; i < 500; i++) {
					view.repaint();
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						break;
					}
				}
				return null;
			}
		};
		anim3.execute();
	}

	public void gameOver() {
		gameOver = true;
		anim2 = new SwingWorker<Void, Void>() {
			protected Void doInBackground() throws Exception {
				for (int i = 0; i < 500; i++) {
					view.repaint();
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						break;
					}
				}
				return null;
			}
		};
		anim2.execute();
	}

	@Override
	public void paint(Graphics g) {
		int boatX = (boatLeft ? 175 : getWidth() - 175 - boatImage.getWidth()) + (int) (boatLoc * (getWidth() - 350 - boatImage.getWidth()));
		int boatY = (getHeight() - boatImage.getHeight()) / 2;

		g.drawImage(grassImage.getSubimage(0, 0, 175, Math.min(getHeight(), grassImage.getHeight())), 0, 0, 175, this.getHeight(), null);
		g.drawImage(waterImage.getSubimage(0, 0, getWidth() - 350, Math.min(getHeight(), waterImage.getHeight())), 175, 0, getWidth() - 350, this.getHeight(), null);
		g.drawImage(grassImage.getSubimage(0, 0, 175, Math.min(getHeight(), grassImage.getHeight())), getWidth() - 175, 0, 175, this.getHeight(), null);
		g.drawImage(boatImage, boatX, boatY, null);

		int maxWidth = 0;
		int sumOfHeightsLeft = 0, sumOfHeightsRight = 0;
		int countLeft = 0, countRight = 0;
		for (int i = 0; i < movingImages.length; i++) {
			if (movingImages[i].getWidth() > maxWidth)
				maxWidth = movingImages[i].getWidth();
			if (positions[i] == Position.LEFT) {
				sumOfHeightsLeft += movingImages[i].getHeight();
				countLeft++;
			} else if (positions[i] == Position.RIGHT) {
				sumOfHeightsRight += movingImages[i].getHeight();
				countRight++;
			}
		}

		int lSpacing = (getHeight() - sumOfHeightsLeft) / (countLeft == 0 ? 1 : countLeft), rSpacing = (getHeight() - sumOfHeightsRight) / (countRight == 0 ? 1 : countRight), lHeight = lSpacing / 2, rHeight = rSpacing / 2;

		for (int i = 0; i < positions.length; i++) {
			Position position = positions[i];
			if (position == Position.LEFT) {
				g.drawImage(movingImages[i], (maxWidth - movingImages[i].getWidth()) / 2, lHeight, movingImages[i].getWidth(), movingImages[i].getHeight(), null);
				lHeight += movingImages[i].getHeight() + lSpacing;
			} else if (position == Position.RIGHT) {
				g.drawImage(movingImages[i], getWidth() - ((maxWidth + movingImages[i].getWidth()) / 2), rHeight, movingImages[i].getWidth(), movingImages[i].getHeight(), null);
				rHeight += movingImages[i].getHeight() + rSpacing;
			} else if (position == Position.BOAT) {
				int boatRight = boatX + boatImage.getWidth() - movingImages[i].getWidth(), boatHeight = boatY + boatImage.getHeight() - movingImages[i].getHeight() - 20;
				if (Characters.values()[i].name().equalsIgnoreCase("FARMER")) {
					g.drawImage(movingImages[i], boatLeft ? boatRight : boatX, boatHeight, null);
				} else {
					g.drawImage(movingImages[i], !boatLeft ? boatRight : boatX, boatHeight, null);
				}
			}
		}

		if (gameOver) {
			g.setColor((counter / 10) % 2 == 0 ? Color.RED : Color.BLUE);
			g.setFont(getFont().deriveFont((float) Math.sin(counter / 2) * 30.0f + 60.0f));
			g.drawString("GAME OVER", (getWidth() / 2) - (g.getFontMetrics(g.getFont()).stringWidth("GAME OVER") / 2), (getHeight() / 2) - (g.getFontMetrics(g.getFont()).getHeight() / 2));
			counter++;
		}
		if (win) {
			g.setColor((counter / 10) % 2 == 0 ? Color.GREEN : Color.BLUE);
			g.setFont(getFont().deriveFont((float) Math.sin(counter / 2) * 30.0f + 60.0f));
			g.drawString("WIN", (getWidth() / 2) - (g.getFontMetrics(g.getFont()).stringWidth("WIN") / 2), (getHeight() / 2) - (g.getFontMetrics(g.getFont()).getHeight() / 2));
			counter++;
		}
	}
}
