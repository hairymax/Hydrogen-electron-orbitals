/*

Copyright 2002 David Joiner and the Shodor Education Foundation, Inc.

Shodor general use license

This code is provided as is, and is offered with no guarantee.

This code may be used freely in any non commercial product. Any works
using this code must publicly give credit for this work to the Shodor
Education Foundation, and should provide in that credit a link to the
location of the code on the Computational Science Education Reference
Desk (http://www.shodor.org/cserd) as well as to the Shodor Education
Foundation (http://www.shodor.org) and to any other authors listed in
the copyright statement found within the code.

Copyright statements within the code may not be modified.

This license does not grant permission for use in any commercial product.

*/

import org.apache.commons.math3.complex.*;
//import org.opensourcephysics.numerics.specialfunctions.*;
import org.opensourcephysics.numerics.Function;
import org.opensourcephysics.numerics.specialfunctions.Legendre;

import static org.opensourcephysics.numerics.Integral.simpson;

class SphericalHarmonic {
    private int l;
    private int m;
    
    public SphericalHarmonic(){
        l=0;
        m=0;
    }
    public SphericalHarmonic(int l,int m){
        this.l=l;
        this.m=m;
    }
    public void setL(int l){
        this.l=l;
    }
    public void setM(int m){
        this.m=m;
    }
    public void setLM(int l,int m){
        this.l=l;
        this.m=m;
    }
    public int getL(){
        return l;
    }
    public int getM(){
        return m;
    }

    public Complex eval(double theta, double phi){
        int absm=Math.abs(m);
        double sign = ((absm)%2==1)? -1.0 : 1.0;
        Function P = Legendre.getAssociatedFunction(l, absm);
        Complex retval = new Complex(Math.cos((double)m*phi), Math.sin((double)m*phi));
        double factor = sign*
            Math.sqrt((double)(2*l+1)/(4.0*Math.PI)*
            MathLib.factorial(l-absm)/MathLib.factorial(l+absm))*
            P.evaluate(Math.cos(theta));
        retval=retval.multiply(factor);
        
        if (m<0) retval = retval.conjugate().multiply(sign);

        return retval;
    }

    private Function abs2Аzimuth(double phi){
        Function Yphi = new Function() {
            @Override
            public double evaluate(double theta) {
                double absYphi = eval(theta,phi).abs();
                return absYphi*absYphi;
            }
        };
        return Yphi;
    }
    private Function abs2Zenith(double theta){
        Function Ytheta = new Function() {
            @Override
            public double evaluate(double phi) {
                double absTheta = eval(theta,phi).abs();
                return absTheta*absTheta*Math.sin(theta);
            }
        };
        return Ytheta;
    }

    public double evalProbability(double startTheta, double finalTheta, double startPhi, double finalPhi, double step){
        Function intAzimuth = new Function() {
            @Override
            public double evaluate(double theta) {
                int splits_phi = (int) Math.round( (finalPhi-startPhi) / step );
                splits_phi += (splits_phi%2==1) ? 1 : 0;
                return simpson(abs2Zenith(theta),startPhi,finalPhi,splits_phi);
            }
        };
        int splits_theta = (int) Math.round( (finalTheta-startTheta) / step );
        splits_theta += (splits_theta%2==1) ? 1 : 0;
        return simpson(intAzimuth,startTheta,finalTheta,splits_theta);
    }
}