/**
 * Project: Solo Lab 5 Assignment: Space Game Mods
 * Purpose Details: Modify a given space game to instructions
 * Course: IST 242
 * Author: Gustavo Reyes
 * Date Developed: 6/22/2025
 * Last Date Changed: 6/22/2025
 * Revision: 1
 */


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class SpaceGame extends JFrame implements KeyListener {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int OBSTACLE_WIDTH = 40;
    private static final int OBSTACLE_HEIGHT = 40;
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;
    private static final int PLAYER_SPEED = 12;
    private static final int OBSTACLE_SPEED = 3;
    private static final int PROJECTILE_SPEED = 20;
    private int score = 0;
    private BufferedImage backgroundImage;
    private int playerHealth = 2;
    private int timeLeft = 60;

    private int level = 1;
    private int levelNum = 20;

    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    private final int DUCK_FRAME_COUNT =5;
    private int currentDuckFrame = 0;
    private long finalDuckFrame = 0;
    private final int DUCK_ANIMATION_DELAY = 50;

    private boolean isShieldActive = false;
    private long shieldEndTime = 0;
    private static final int SHIELD_DURATION = 1000;

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private Timer timer;
    private boolean isGameOver;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private java.util.List<Obstacle> obstacles;
    private class Obstacle {
        int x, y;
        int spriteIndex;

        Obstacle(int x, int y, int spriteIndex) {
            this.x = x;
            this.y = y;
            this.spriteIndex = spriteIndex;
        }
    }





    private java.util.List<Healthboost> healthBoosts = new ArrayList<>();
    private class Healthboost {
        int x, y;
        boolean active;

        Healthboost(int x, int y) {
            this.x = x;
            this.y = y;
            this.active = true;
        }
    }


//    //*private java.util.List<Star> stars;
//    private class Star {
//        int x, y;
//        Color color;
//
//        Star(int x, int y, Color color) {
//            this.x = x;
//            this.y = y;
//            this.color = color;
//        }
//    }*/
    private BufferedImage healthboostImage;
    private BufferedImage playerfish;
    private BufferedImage squidSpriteSheet;
    private final int SPRITE_WIDTH = 32;
    private final int SPRITE_HEIGHT = 32;

    private Clip fireClip;
    private Clip collisionClip;



//    private java.util.List<Star> generateStars(int count) {
//        java.util.List<Star> starList = new ArrayList<>();
//        java.util.Random rand = new java.util.Random();
//        for (int i = 0; i < count; i++) {
//            int x = rand.nextInt(WIDTH);
//            int y = rand.nextInt(HEIGHT);
//            Color color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
//            starList.add(new Star(x, y, color));
//        }
//        return starList;
//    }

    public SpaceGame() {
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
//        stars = generateStars(350);

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

        try {
            healthboostImage = ImageIO.read(new File("Healthboost.png"));
        } catch (IOException e) {
            System.out.println("Cant find the healthboost image");
            e.printStackTrace();
        }

        try {
            playerfish = ImageIO.read(new File("Player duck.png"));
        } catch (IOException e) {
            System.out.println("Cant find the fish image");
            e.printStackTrace();
        }

        try {
            squidSpriteSheet = ImageIO.read(new File("Enemy squid.png"));
        } catch (IOException e) {
            System.out.println("Cant find the squid image");
            e.printStackTrace();
        }
        try {
            backgroundImage = ImageIO.read(new File("background.png"));
        } catch (IOException e) {
            System.out.println("Cant find the background image");
            e.printStackTrace();
        }
        try {
            AudioInputStream fireStream = AudioSystem.getAudioInputStream(new File("fire2.wav"));
            fireClip = AudioSystem.getClip();
            fireClip.open(fireStream);

            AudioInputStream collisionStream = AudioSystem.getAudioInputStream(new File("collision2.wav"));
            collisionClip = AudioSystem.getClip();
            collisionClip.open(collisionStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Cant find the clips");
            e.printStackTrace();
        }

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.BLUE);
        scoreLabel.setBounds(10, 10, 300, 300);
        gamePanel.add(scoreLabel);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        playerX = 10;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH;
        projectileY = playerY;
        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;
        obstacles = new ArrayList<>();

        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) {
                    update();
                    gamePanel.repaint();
                }
            }
        });
        timer.start();
        new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver && timeLeft > 0) {
                    timeLeft--;
                    if (timeLeft == 0) {
                        isGameOver = true;
                    }
                }
            }
        }).start();
    }

    private void draw(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, null);
//        g.setColor(Color.BLACK);
//        g.fillRect(0, 0, WIDTH, HEIGHT);

//        for (Star star : stars) {
//            g.setColor(star.color);
//            g.fillRect(star.x, star.y, 3, 3);
//        }

        int duckFrameWidth = playerfish.getWidth() / DUCK_FRAME_COUNT;
        g.drawImage(playerfish.getSubimage(currentDuckFrame * duckFrameWidth, 0, duckFrameWidth,playerfish.getHeight()), playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);

        if (isProjectileVisible) {
            g.setColor(Color.BLUE);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }


        for (Obstacle obstacle : obstacles) {
            int sx = obstacle.spriteIndex * SPRITE_WIDTH;
            int sy = 0;
            g.drawImage(
                    squidSpriteSheet.getSubimage(sx, sy, SPRITE_WIDTH, SPRITE_HEIGHT),
                    obstacle.x,
                    obstacle.y,
                    OBSTACLE_WIDTH,
                    OBSTACLE_HEIGHT,
                    null
            );
        }

        if (isShieldActive) {
            g.setColor(Color.CYAN);
            g.drawOval(playerX - 5, playerY - 5, PLAYER_HEIGHT, PLAYER_WIDTH);
        }

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
        }

        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Health: " + playerHealth, 10, 20);

        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Time: " + timeLeft, 10, 60);

        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Level: " + level, 10, 80);

        for (Healthboost healthboost : healthBoosts) {
            g.drawImage(healthboostImage, healthboost.x, healthboost.y, 50, 50, null);

        }
    }


    private void update() {
        if (!isGameOver) {
            // Move obstacles
            for (int i = 0; i < obstacles.size(); i++) {
                obstacles.get(i).x -= OBSTACLE_SPEED + (level -3);
                if (obstacles.get(i).x + OBSTACLE_WIDTH < 0) {
                    obstacles.remove(i);
                    i--;
                }
            }
            if (movingUp && playerY > 0) {
                playerY -= PLAYER_SPEED;
            }
            if (movingDown && playerY + PLAYER_SPEED < HEIGHT - PLAYER_HEIGHT) {
                playerY += PLAYER_SPEED;
            }
            if (movingLeft && playerX > 0) {
                playerX -= PLAYER_SPEED;
            }
            if (movingRight && playerX < WIDTH - PLAYER_WIDTH) {
                playerX += PLAYER_SPEED;
            }

            if (Math.random() < 0.005) {
                int healthBoostY = (int) (Math.random() * HEIGHT - 20);
                healthBoosts.add(new Healthboost(WIDTH, healthBoostY));
            }

            if (isShieldActive && System.currentTimeMillis() > shieldEndTime) {
                isShieldActive = false;
            }

//            if (Math.random() < 0.02) {
//                stars = generateStars(300);
//            }

            // Generate new obstacles
            if (Math.random() < 0.02) {
                int obstacleY = (int) (Math.random() * (HEIGHT - OBSTACLE_HEIGHT));
                int spriteIndex = (int) (Math.random() * 4);
                obstacles.add(new Obstacle(WIDTH, obstacleY, spriteIndex));
            }

            // Move projectile
            if (isProjectileVisible) {
                projectileX += PROJECTILE_SPEED;
                if (projectileX > WIDTH) {
                    isProjectileVisible = false;
                }
            }

            // Check collision with player
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            for (int i = 0; i < obstacles.size(); i++) {
                Obstacle obstacle = obstacles.get(i);
                Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (playerRect.intersects(obstacleRect)) {
                    if (!isShieldActive) {
                        playerHealth--;
                        obstacles.remove(i);
                        i--;

                        if (playerHealth <= 0) {
                            isGameOver = true;
                            break;
                        }
                    }
                }
            }


            // Check collision with obstacle
            Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
            for (int i = 0; i < obstacles.size(); i++) {
                Rectangle obstacleRect = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);

                if (projectileRect.intersects(obstacleRect)) {
                    collisionClip.stop();
                    collisionClip.setFramePosition(0);
                    collisionClip.start();
                }

                if (projectileRect.intersects(obstacleRect)) {
                    obstacles.remove(i);
                    score += 10;
                    if (score >= level * levelNum){
                        level++;
                    }
                    isProjectileVisible = false;
                    break;

                }

            }
            for (int i = 0; i < healthBoosts.size(); i++) {
                Healthboost healthboost = healthBoosts.get(i);
                healthboost.x -= OBSTACLE_SPEED;

                if (healthboost.x < 0) {
                    healthBoosts.remove(i);
                    i--;
                }

            }
            for (int i = 0; i < healthBoosts.size(); i++) {
                Healthboost healthboost = healthBoosts.get(i);
                Rectangle boostRect = new Rectangle(healthboost.x, healthboost.y, 50, 50);

                if (boostRect.intersects(playerRect)) {
                    if (playerHealth < 2) {
                        playerHealth++;
                    }
                    healthBoosts.remove(i);
                    i--;
                }

            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - finalDuckFrame > DUCK_ANIMATION_DELAY) {
                currentDuckFrame = (currentDuckFrame + 1) % DUCK_FRAME_COUNT;
                finalDuckFrame = currentTime;
            }

            scoreLabel.setText("Score: " + score);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP) {
            movingUp = true;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            movingDown = true;
        } else if (keyCode == KeyEvent.VK_LEFT) {
            movingLeft = true;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            movingRight = true;
        } else if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
            isFiring = true;
            projectileX = playerX + PLAYER_WIDTH;
            projectileY = playerY;
            isProjectileVisible = true;

            fireClip.setFramePosition(0);
            fireClip.start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500); // Limit firing rate
                        isFiring = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        } else if (keyCode == KeyEvent.VK_SHIFT && !isShieldActive) {
            isShieldActive = true;
            shieldEndTime = System.currentTimeMillis() + SHIELD_DURATION;
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP) {
            movingUp = false;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            movingDown = false;
        } else if (keyCode == KeyEvent.VK_LEFT) {
            movingLeft = false;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            movingRight = false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceGame().setVisible(true);
            }
        });
    }
}
