package org.polytechtours.javaperformance.tp.paintingants;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;

public class PaintingAnts extends Application {
    // parametres
    private int mLargeur;
    private int mHauteur;

    // l'objet graphique lui meme
    private CPainting mPainting;

    // les fourmis
    private final List<CFourmi> mColonie = new ArrayList<>();
    //private Vector<CFourmi> mColonie = new Vector<CFourmi>();
    private CColonie mColony;

    private Thread mApplis, mThreadColony;

    private Dimension mDimension;
    private long mCompteur = 0;
    private final Object mMutexCompteur = new Object();
    private boolean mPause = false;

    public BufferedImage mBaseImage;
    private Timer fpsTimer;

    /**
     * Fourmis per second :)
     */
    private Long fpsCounter = 0L;
    /**
     * stocke la valeur du compteur lors du dernier timer
     */
    private Long lastFps = 0L;

    public static void main(String[] args) {
        launch(args);
    }

    /****************************************************************************/
    /**
     * incrémenter le compteur
     */
    public void compteur() {
        synchronized (mMutexCompteur) {
            mCompteur++;
        }
    }


    /****************************************************************************/
    /**
     * Obtenir l'état de pause
     */
    public boolean getPause() {
        return mPause;
    }

    public synchronized void IncrementFpsCounter() {
        fpsCounter++;
    }

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        // lecture des parametres de l'applet

        mDimension = new Dimension(400, 400);
        mLargeur = mDimension.width;
        mHauteur = mDimension.height;

        mPainting = new CPainting(mDimension, this);

        readParameterFourmis();
        Scene scene = new Scene(root);

        root.getChildren().add(mPainting);
        stage.setScene(scene);

        stage.show();

        mPainting.init();

        startSimulation();
    }

  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/

    /****************************************************************************/
    /**
     * Mettre en pause
     */
    public void pause() {
        mPause = !mPause;
        // if (!mPause)
        // {
        // notify();
        // }
    }

    // =========================================================================
    // cette fonction analyse une chaine :
    // si pStr est un nombre : sa valeur est retournée
    // si pStr est un interval x..y : une valeur au hasard dans [x,y] est
    // retournée
    private float readFloatParameter(String pStr) {
        float lMin, lMax, lResult;
        // System.out.println(" chaine pStr: "+pStr);
        StringTokenizer lStrTok = new StringTokenizer(pStr, ":");
        // on lit une premiere valeur
        lMin = Float.parseFloat(lStrTok.nextToken());
        // System.out.println(" lMin: "+lMin);
        lResult = lMin;
        // on essaye d'en lire une deuxieme
        try {
            lMax = Float.parseFloat(lStrTok.nextToken());
            // System.out.println(" lMax: "+lMax);
            if (lMax > lMin) {
                // on choisit un nombre entre lMin et lMax
                lResult = (float) (Math.random() * (lMax - lMin)) + lMin;
            }
        } catch (java.util.NoSuchElementException e) {
            // il n'y pas de deuxieme nombre et donc le nombre retourné correspond au
            // premier nombre
        }
        return lResult;
    }

    // =========================================================================
    // cette fonction analyse une chaine :
    // si pStr est un nombre : sa valeur est retournée
    // si pStr est un interval x..y : une valeur au hasard dans [x,y] est
    // retournée
    private int readIntParameter(String pStr) {
        int lMin, lMax, lResult;
        StringTokenizer lStrTok = new StringTokenizer(pStr, ":");
        // on lit une premiere valeur
        lMin = Integer.parseInt(lStrTok.nextToken());
        lResult = lMin;
        // on essaye d'en lire une deuxieme
        try {
            lMax = Integer.parseInt(lStrTok.nextToken());
            if (lMax > lMin) {
                // on choisit un nombre entre lMin et lMax
                lResult = (int) (Math.random() * (lMax - lMin + 1)) + lMin;
            }
        } catch (java.util.NoSuchElementException e) {
            // il n'y pas de deuxième nombre et donc le nombre retourné correspond au
            // premier nombre
        }
        return lResult;
    }

    // =========================================================================
    // lecture des paramètres de l'applet
    private void readParameterFourmis() {
        String lChaine;
        int R, G, B;
        Color lCouleurDeposee, lCouleurSuivie;
        CFourmi lFourmi;
        float lProbaTD, lProbaG, lProbaD, lProbaSuivre, lSeuilLuminance;
        char lTypeDeplacement;
        int lInitDirection, lTaille;
        float lInit_x, lInit_y;
        int lNbFourmis;

        // Lecture des paramètres des fourmis

        // Lecture du seuil de luminance
        // <PARAM NAME="SeuilLuminance" VALUE="N">
        // N : seuil de luminance : -1 = random(2..60), x..y = random(x..y)
        lChaine = null; //TODO: Luminance
        if (lChaine != null) {
            lSeuilLuminance = readFloatParameter(lChaine);
        } else {
            // si seuil de luminance n'est pas défini
            lSeuilLuminance = 40f;
        }
        System.out.println("Seuil de luminance:" + lSeuilLuminance);

        // Lecture du nombre de fourmis :
        // <PARAM NAME="NbFourmis" VALUE="N">
        // N : nombre de fourmis : -1 = random(2..6), x..y = random(x..y)
        lChaine = null;// TODO; NbFourmis
        if (lChaine != null) {
            lNbFourmis = readIntParameter(lChaine);
        } else {
            // si le parametre NbFourmis n'est pas défini
            lNbFourmis = -1;
        }
        // si le parametre NbFourmis n'est pas défini ou alors s'il vaut -1 :
        if (lNbFourmis == -1) {
            // Le nombre de fourmis est aléatoire entre 2 et 6 !
            lNbFourmis = (int) (Math.random() * 5) + 2;
        }

        // <PARAM NAME="Fourmis"
        // VALUE="(255,0,0)(255,255,255)(20,40,1)([d|o],0.2,0.6,0.2,0.8)">
        // (R,G,B) de la couleur déposée : -1 = random(0...255); x:y = random(x...y)
        // (R,G,B) de la couleur suivie : -1 = random(0...255); x:y = random(x...y)
        // (x,y,d,t) position , direction initiale et taille du trait
        // x,y = 0.0 ... 1.0 : -1 = random(0.0 ... 1.0); x:y = random(x...y)
        // d = 7 0 1
        // 6 X 2
        // 5 4 3 : -1 = random(0...7); x:y = random(x...y)
        // t = 0, 1, 2, 3 : -1 = random(0...3); x:y = random(x...y)
        //
        // (type deplacement,proba gauche,proba tout droit,proba droite,proba
        // suivre)
        // type deplacement = o/d : -1 = random(o/d)
        // probas : -1 = random(0.0 ... 1.0); x:y = random(x...y)

        lChaine = null;//TODO: Fourmis
        if (lChaine != null) {
            // on affiche la chaine de parametres
            System.out.println("Paramètres:" + lChaine);

            // on va compter le nombre de fourmis dans la chaine de parametres :
            lNbFourmis = 0;
            // chaine de paramètres pour une fourmi
            StringTokenizer lSTFourmi = new StringTokenizer(lChaine, ";");
            while (lSTFourmi.hasMoreTokens()) {
                // chaine de parametres de couleur et proba
                StringTokenizer lSTParam = new StringTokenizer(lSTFourmi.nextToken(), "()");
                // lecture de la couleur déposée
                StringTokenizer lSTCouleurDéposée = new StringTokenizer(lSTParam.nextToken(), ",");
                R = readIntParameter(lSTCouleurDéposée.nextToken());
                if (R == -1) {
                    R = (int) (Math.random() * 256);
                }

                G = readIntParameter(lSTCouleurDéposée.nextToken());
                if (G == -1) {
                    G = (int) (Math.random() * 256);
                }
                B = readIntParameter(lSTCouleurDéposée.nextToken());
                if (B == -1) {
                    B = (int) (Math.random() * 256);
                }
                lCouleurDeposee = Color.rgb(R, G, B);
                System.out.print("Parametres de la fourmi " + lNbFourmis + ":(" + R + "," + G + "," + B + ")");

                // lecture de la couleur suivie
                StringTokenizer lSTCouleurSuivi = new StringTokenizer(lSTParam.nextToken(), ",");
                R = readIntParameter(lSTCouleurSuivi.nextToken());
                G = readIntParameter(lSTCouleurSuivi.nextToken());
                B = readIntParameter(lSTCouleurSuivi.nextToken());
                lCouleurSuivie = Color.rgb(R, G, B);
                System.out.print("(" + R + "," + G + "," + B + ")");

                // lecture de la position de la direction de départ et de la taille de
                // la trace
                StringTokenizer lSTDéplacement = new StringTokenizer(lSTParam.nextToken(), ",");
                lInit_x = readFloatParameter(lSTDéplacement.nextToken());
                if (lInit_x < 0.0 || lInit_x > 1.0) {
                    lInit_x = (float) Math.random();
                }
                lInit_y = readFloatParameter(lSTDéplacement.nextToken());
                if (lInit_y < 0.0 || lInit_y > 1.0) {
                    lInit_y = (float) Math.random();
                }
                lInitDirection = readIntParameter(lSTDéplacement.nextToken());
                if (lInitDirection < 0 || lInitDirection > 7) {
                    lInitDirection = (int) (Math.random() * 8);
                }
                lTaille = readIntParameter(lSTDéplacement.nextToken());
                if (lTaille < 0 || lTaille > 3) {
                    lTaille = (int) (Math.random() * 4);
                }
                System.out.print("(" + lInit_x + "," + lInit_y + "," + lInitDirection + "," + lTaille + ")");

                // lecture des probas
                StringTokenizer lSTProbas = new StringTokenizer(lSTParam.nextToken(), ",");
                lTypeDeplacement = lSTProbas.nextToken().charAt(0);
                // System.out.println(" lTypeDeplacement:"+lTypeDeplacement);

                if (lTypeDeplacement != 'o' && lTypeDeplacement != 'd') {
                    if (Math.random() < 0.5) {
                        lTypeDeplacement = 'o';
                    } else {
                        lTypeDeplacement = 'd';
                    }
                }

                lProbaG = readFloatParameter(lSTProbas.nextToken());
                lProbaTD = readFloatParameter(lSTProbas.nextToken());
                lProbaD = readFloatParameter(lSTProbas.nextToken());
                lProbaSuivre = readFloatParameter(lSTProbas.nextToken());
                // on normalise au cas ou
                float lSomme = lProbaG + lProbaTD + lProbaD;
                lProbaG /= lSomme;
                lProbaTD /= lSomme;
                lProbaD /= lSomme;

                System.out.println(
                        "(" + lTypeDeplacement + "," + lProbaG + "," + lProbaTD + "," + lProbaD + "," + lProbaSuivre + ");");

                // création de la fourmi
                lFourmi = new CFourmi(lCouleurDeposee, lCouleurSuivie, lProbaTD, lProbaG, lProbaD, lProbaSuivre, mPainting,
                        lTypeDeplacement, lInit_x, lInit_y, lInitDirection, lTaille, lSeuilLuminance, this);
                mColonie.add(lFourmi);
                lNbFourmis++;
            }
        } else // initialisation aléatoire des fourmis
        {
           Instance randomInstance = new Instance();
           ArrayList<AntData> rawDataAnts = randomInstance.getRawDataAnts();
            /* print(Instance)
                System.out.print("Random:(" + lTabColor[i].getRed() + "," + lTabColor[i].getGreen() + "," + lTabColor[i].getBlue() + ")");
                System.out.print("(" + lTabColor[lColor].getRed() + "," + lTabColor[lColor].getGreen() + "," + lTabColor[lColor].getBlue() + ")");
                System.out.print("(" + lInit_x + "," + lInit_y + "," + lInitDirection + "," + lTaille + ")");
                System.out.println("(" + lTypeDeplacement + "," + lProbaG + "," + lProbaTD + "," + lProbaD + "," + lProbaSuivre + ");");
                */

           Color droppedColor,followedColor;
           float xStart,yStart;
           int direction,trailSize;
           float fwdProb,lftProb,rgtProb,followProb;
           char moveType;

           for(AntData rawAnt : rawDataAnts){
               droppedColor = rawAnt.getDroppedColor();
               followedColor = rawAnt.getFollowedColor();
               xStart = rawAnt.getX();
               yStart = rawAnt.getY();
               direction = rawAnt.getDirection();
               trailSize = rawAnt.getTrailSize();
               fwdProb = rawAnt.getForwardProb();
               lftProb = rawAnt.getLeftProb();
               rgtProb = rawAnt.getRightProb();
               followProb = rawAnt.getFollowProb();
               moveType = rawAnt.getMoveType();

               lFourmi = new CFourmi(droppedColor,followedColor,fwdProb,lftProb,rgtProb,followProb,
                       mPainting,moveType,xStart,yStart,direction,trailSize,lSeuilLuminance,this);
                // création et ajout de la fourmi dans la colonie
                synchronized (mColonie) {
                    mColonie.add(lFourmi);
                }
            }
        }
        // on affiche le nombre de fourmis
        // System.out.println("Nombre de Fourmis:"+lNbFourmis);
    }



    /****************************************************************************/
    /**
     * Lancer l'applet
     */

    public void startSimulation() {
        // System.out.println(this.getName()+ ":start()");
        mColony = new CColonie(mColonie, this);
        mThreadColony = new Thread(mColony);
        mThreadColony.setPriority(Thread.MIN_PRIORITY);
        mThreadColony.start();


        // fpsTimer = new Timer(1000, new ActionListener() {
        //     @Override
        //     public void actionPerformed(ActionEvent e) {
        //         updateFPS();
        //     }
        // });
        // fpsTimer.setRepeats(true);
        // fpsTimer.start();

        //showStatus("starting...");
        // Create the thread.
        // mApplis = new Thread(new SimulationThread(mMutexCompteur, mPainting, mThreadColony));
        // // and let it start running
        // mApplis.setPriority(Thread.MIN_PRIORITY);
        // mApplis.start();
    }

    /****************************************************************************/
    /**
     * Arrêter l'applet
     */
    @Override
    public void stop() {
        //showStatus("stopped...");

        fpsTimer.cancel();

        // On demande au Thread Colony de s'arreter et on attend qu'il s'arrete
        mColony.pleaseStop();
        try {
            mThreadColony.join();
        } catch (Exception ignored) {
        }

        mThreadColony = null;
        mApplis = null;
    }

    /**
     * update Fourmis per second
     */
    private synchronized void updateFPS() {
        lastFps = fpsCounter;
        fpsCounter = 0L;
    }
}
