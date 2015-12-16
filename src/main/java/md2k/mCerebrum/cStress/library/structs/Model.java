package md2k.mCerebrum.cStress.library.structs;

/**
 * Created by karenhovsepian on 12/15/15.
 */
public class Model {
    protected String modelName;
    protected String modelType;


    public Model(String modelName, String modelType)
    {
        this.modelName = modelName;
        this.modelType = modelType;
    }

    public String getModelName()
    {
        return modelName;
    }

    public String getModelType()
    {
        return modelType;
    }
}

