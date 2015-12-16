package md2k.mCerebrum.cStress.library.structs;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Created by karenhovsepian on 12/15/15.
 */
public class SVCModel extends Model{
    private double intercept;
    private double bias;
    private double probA;
    private double probB;

    private Kernel kernel;
    class Kernel
    {
        String type;
        KernelParameter [] parameters;

        class KernelParameter
        {
            String name;
            double value;

            public KernelParameter(String name, double value)
            {
                this.name = name;
                this.value = value;
            }
/*            public KernelParameter(int degree)
            {
                this.degree = degree;
            }

            public KernelParameter(){}
*/
        }

        public Kernel(String type, KernelParameter [] parameters)
        {
            this.type = type;
            this.parameters = parameters;
        }
    }


    private Support [] support;
    class Support
    {
        double dualCoef;
        double [] supportVector;
//        ArrayRealVector supportVector;

        public Support(double dualCoef, double [] supportVector)
        {
            this.dualCoef = dualCoef;
            this.supportVector = supportVector;//new ArrayRealVector(supportVector);
        }
    }


    private NormParam [] normparams;
    class NormParam
    {
        double mean;
        double std;
        public NormParam(double mean,double std)
        {
            this.mean = mean;
            this.std = std;
        }
    }



    public SVCModel(String modelName,String modelType,double intercept, double bias, double probA, double probB, Kernel kernel, Support [] support,NormParam [] normparams)
    {
        super(modelName,modelType);
        this.intercept = intercept;
        this.bias = bias;
        this.probA = probA;
        this.probB = probB;
        this.kernel = kernel;
        this.support = support;
        this.normparams = normparams;
    }


    public double getBias()
    {
        return bias;
    }


    public double computeProbability(DataPointArray ap)
    {
        double outputvalue = 0;
        double [] vals = new double[ap.value.size()];

        for(int i=0;i<vals.length;i++)
        {
            vals[i] = (ap.value.get(i)-normparams[i].mean)/normparams[i].std;
        }
        if(kernel.type.equals("rbf"))
        {
            for(Support supportvector: support) {
                double norm = 0;
                //computing second norm
                for (int i = 0; i < ap.value.size(); i++) {
                    double temp = (vals[i] - supportvector.supportVector[i]);
                    norm += temp * temp;
                }
                outputvalue += Math.exp(-kernel.parameters[0].value * norm)* supportvector.dualCoef;
            }
            outputvalue += intercept;
        }
        else if(kernel.type.equals("poly"))
        {
            for(Support supportvector: support) {
                double dotvalue  = 0;
                //computing dot product
                for (int i = 0; i < ap.value.size(); i++)
                    dotvalue += vals[i]*supportvector.supportVector[i];

                outputvalue += Math.pow(dotvalue/ap.value.size(),(int)kernel.parameters[0].value)*supportvector.dualCoef;
            }
            outputvalue += intercept;
        }
        else //linear
        {
            for(Support supportvector: support) {
                double dotvalue  = 0;
                //computing dot product
                for (int i = 0; i < ap.value.size(); i++)
                    dotvalue += vals[i]*supportvector.supportVector[i];

                outputvalue += dotvalue*supportvector.dualCoef;
            }
            outputvalue += intercept;
        }
        return 1.0/(1.0+Math.exp(probA*outputvalue+probB));
    }
}

