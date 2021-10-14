import java.sql.SQLOutput;
import java.util.ArrayList;

public class Levels {

    private ArrayList<Integer> xPos = new ArrayList<>(); //arrayList with x positions of all bricks
    private ArrayList<Integer> yPos = new ArrayList<>(); //arrayList with corresponding y positions of all bricks
    private int levelNum; //number representing level (1-5)
    private int numBullets; //number of bullets provided in each level
    private double ballSpeed; //double from 2 - 4 controlling speed of ball movement in each level

    //constructor to set level settings based on level number given
    public Levels(int lvl){
        levelNum = lvl;
        switch(levelNum){
            case 1:
                ballSpeed = 2;
                numBullets = 5;
                setLevel1();
                break;
            case 2:
                ballSpeed = 2.5;
                numBullets = 5;
                setLevel2();
                break;
            case 3:
                ballSpeed = 3;
                numBullets = 3;
                setLevel3();
                break;
            case 4:
                ballSpeed = 3.5;
                numBullets = 2;
                setLevel4();
                break;
            case 5:
                ballSpeed = 4;
                numBullets = 1;
                setLevel5();
                break;
        }

    }

    //determining brick positions for level 1
    private void setLevel1(){
        for (int i = 0; i < 10; i++) {
            xPos.add(150 + 70 * i);
            yPos.add(100);

            xPos.add(150 + 70 * i);
            yPos.add(200);
        }
    }

    //determining brick positions for level 2
    private void setLevel2(){
        for (int i = 0; i < 3; i++){
            xPos.add(150 + 125 * i);
            yPos.add(75);

            xPos.add(500 + 125 * i);
            yPos.add(75);

            xPos.add(150 + 125 * i);
            yPos.add(150);

            xPos.add(500 + 125 * i);
            yPos.add(150);

            xPos.add(150 + 125 * i);
            yPos.add(225);

            xPos.add(500 + 125 * i);
            yPos.add(225);
        }

        xPos.add(450);
        yPos.add(100);

        xPos.add(450);
        yPos.add(200);
    }

    //determining brick positions for level 3
    private void setLevel3(){
        for (int i = 0; i < 8; i++) {
            xPos.add(200 + 70 * i);
            yPos.add(100);

            xPos.add(200 + 70 * i);
            yPos.add(200);
            if(i==2 || i==5){
                for(int j = 0; j < 5; j++){
                    xPos.add(200 + 70 * i);
                    yPos.add(230 + 30 * j);
                }
            }
        }
        xPos.add(150);
        yPos.add(125);

        xPos.add(100);
        yPos.add(150);

        xPos.add(150);
        yPos.add(175);

        xPos.add(740);
        yPos.add(125);

        xPos.add(790);
        yPos.add(150);

        xPos.add(740);
        yPos.add(175);

    }

    //determining brick positions for level 4
    private void setLevel4(){
        for (int i = 0; i < 5; i++){
            xPos.add(150 + 175 * i);
            yPos.add(100);

            xPos.add(70 + 175 * i);
            yPos.add(150);

            xPos.add(150 + 175 * i);
            yPos.add(200);

            xPos.add(70 + 175 * i);
            yPos.add(250);
        }
    }

    //determining brick positions for level 5
    private void setLevel5() {
        for (int i = 0; i < 8; i++) {
            xPos.add(50);
            yPos.add(20 + 50 * i);

            xPos.add(900);
            yPos.add(20 + 50 * i);
        }

        xPos.add(150);
        yPos.add(200);

        xPos.add(500);
        yPos.add(200);

        xPos.add(400);
        yPos.add(50);

        xPos.add(550);
        yPos.add(100);

        xPos.add(175);
        yPos.add(250);

        xPos.add(700);
        yPos.add(300);

        xPos.add(520);
        yPos.add(250);

        xPos.add(690);
        yPos.add(50);
    }

    public ArrayList<Integer> returnX(){
        return xPos;
    }

    public ArrayList<Integer> returnY(){
        return yPos;
    }

    public int getNumBullets(){
        return numBullets;
    }

    public double getBallSpeed(){
        return ballSpeed;
    }
}
