package md2k.mCerebrum.cStress.Features;


/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
 * - Karen Hovsepian <karoaper@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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


public class BinnedStatistics {

	private static final int DEFAULT_MAXVAL = 5000;
	private static final int DEFAULT_MINVAL = 0;
    private double mean;
    private double stdev;
    
    private double winsorized_mean;
    private double winsorized_stdev;
    private long count;

    private int med;
    private double mad;
	private double low;
	private double high;
    
	private int min_value;
	private int max_value;
	private int num_bins;
	
    private int [] bins;
    //TODO: Needs a persistence and initialization layer

    /**
     * Class to keep track of running statistics.
     */
    public BinnedStatistics(int min_value,int max_value) 
    {
        this.mean = 0.0;
        this.stdev = 0.0;
        this.count = 0;
        this.winsorized_mean = 0.0;
        this.winsorized_stdev = 0.0;
        this.mad = 0;
        this.med = 0;
		this.low = 0;
		this.high = 0;
		this.min_value = min_value;
		this.max_value = max_value;
		this.num_bins = max_value-min_value+1;
        this.bins = new int[num_bins];
        for(int i=0;i<num_bins;i++)
        	this.bins[i] = 0;
    }

    public BinnedStatistics()
    {
    	this(BinnedStatistics.DEFAULT_MINVAL,BinnedStatistics.DEFAULT_MAXVAL);
    }
    
    
    public void add(int x) 
    {
    	bins[x]++;
        count++;
    }

    
    public void computeMed()
    {
        int sum = 0;
    	for(int i=0;i<num_bins;i++)
    	{
    		sum += bins[i];
    		if (sum > count/2)
    		{
    			med = i+min_value;
    			break;
    		}
    		else if (sum == count/2)
    		{
    			if(count %2 ==0)
    				med = (2*i+1)/2+min_value;
    			else
    				med = i+min_value;
    			
    			break;
    		}
    	}
    }
    
    public void computeMad()
    {
    	int [] madbins= new int[num_bins-1];
		for(int i=0;i<num_bins-1;i++)
    		madbins[i] = 0;
		
    	for(int i=0;i<num_bins;i++)
    		madbins[Math.abs(i+min_value-med)] += bins[i];
    	
		int sum = 0;
    	for(int i=0;i<num_bins-1;i++)
    	{
    		sum += madbins[i];
    		if (sum > count/2)
    		{
    			mad = i;
    			break;
    		}
    		else if (sum == count/2)
    		{
    			if(count %2 ==0)
    				mad = (2*i+1)/2.0;
    			else
    				mad = i;
    			
    			break;
    		}
    	}
		low = med-3.0*mad;
        high = med+3.0*mad;
    }
    
    
    public double getMean() {
		
		int sum = 0;
    	for(int i=0;i<num_bins;i++)
    	{
    		sum += bins[i]*i;
    	}
		mean = (double)sum/count;
        return mean;
    }

    public double getStdev() {
		int sum = 0;
    	for(int i=0;i<num_bins;i++)
    	{
    		sum += bins[i]*(i-mean)*(i-mean);
		}
		stdev = Math.sqrt(sum/ (count-1));
        return stdev;
    }

	
	public double getWinsorizedMean() 
	{
		computeMed();
		computeMad();
        
		int sum = 0;
    	for(int i=0;i<num_bins;i++)
    	{
    		sum += bins[i]*((i>high) ? high : ((i<low) ? low : i));
    	}
		winsorized_mean = (double)sum/count;
        return winsorized_mean;
    }

    public double getWinsorizedStdev() 
	{
		int sum = 0;
    	for(int i=0;i<num_bins;i++)
    	{
			double temp = ((i>high) ? high : ((i<low) ? low : i));
    		sum += bins[i]*(temp-winsorized_mean)*(temp-winsorized_mean);
		}
		winsorized_stdev = Math.sqrt(sum/ (count-1));
        return winsorized_stdev;
    }
	
}
