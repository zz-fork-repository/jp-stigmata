package jp.naist.se.stigmata.ui.swing.mds.mark;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

public class StarDrawer extends LineMarkDrawer{
    public StarDrawer(){
    }

    @Override
    public Shape getPlainShape(int w, int h){
        float ww = w / 2f;
        float hh = h / 2f;

        GeneralPath path = new GeneralPath();
        path.moveTo(0, hh); path.lineTo(w, hh);
        path.moveTo(ww, 0); path.lineTo(ww, h);
        path.moveTo(0, 0);  path.lineTo(w, h);
        path.moveTo(w, 0);  path.lineTo(0, h);

        return path;
    }

    @Override
    public Shape getOveredShape(int w, int h){
        return getPlainShape(w, h);
    }
}