package md2k.mCerebrum.cStress.library.structs;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Karen Hovsepian <karoaper@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class SVCModel extends Model{
    private double intercept;
    private double bias;
    private double probA;
    private double probB;

    private Kernel kernel;
    private Support [] support;
    private NormParam [] normparams;
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

    class Kernel {
        String type;
        KernelParameter[] parameters;

        public Kernel(String type, KernelParameter[] parameters) {
            this.type = type;
            this.parameters = parameters;
        }

        class KernelParameter {
            String name;
            double value;

            public KernelParameter(String name, double value) {
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
    }

    class Support {
        double dualCoef;
        double[] supportVector;
//        ArrayRealVector supportVector;

        public Support(double dualCoef, double[] supportVector) {
            this.dualCoef = dualCoef;
            this.supportVector = supportVector;//new ArrayRealVector(supportVector);
        }
    }

    class NormParam {
        double mean;
        double std;

        public NormParam(double mean, double std) {
            this.mean = mean;
            this.std = std;
        }
    }
}

