package bTagger;

public class FeatureProperty
{
	FeaturePropertyType type;
	/**value of the property*/
	int param;

	public FeatureProperty(FeaturePropertyType tp, int prm){
		type=tp;
		param=prm;
	}

	public String applyProperty(String form)
	{
		if(type==FeaturePropertyType.PREFIX){
			if(param<=form.length()){ //TODO try -1
				return "p:"+form.substring(0, param);
			}
		}
		else if(type==FeaturePropertyType.SUFFIX){
			if(param<=form.length()){ //TODO try -1
				return "s:"+form.substring(form.length()-param);
			}
		}
		return null;
	}

}
