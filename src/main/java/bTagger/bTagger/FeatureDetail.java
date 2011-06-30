package bTagger;

/**
 * Store details about a feature. These details are computed and stored at the
 * beginning and used later to speed up the features identification.
 * 
 * @author Andrea Gesmundo
 */
public class FeatureDetail
{
	/**
	 * Index of the feature to use in {@link TagLearn}.addTagsNames and in
	 * {@link TagSample}.addTags. Has the value -1 if this is not an addTag.
	 */
	public final int addFeatIndex;

	/**
	 * signed distance from the current position
	 */
	public final int offset;
	
	/**
	 * unsigned distance from the current position
	 */
	public final int distance;

	public final boolean isMainTag;

	public final boolean isOnLeft;

	public final boolean isOnRight;

	public final boolean isWord;

	public final String positionAndTagName;

	public final String tagName;

	public final FeatureProperty featureProperty;
	
	/**
	 * Initialize the FDetail instance using two fundamental information all the
	 * other info are redundant, are pre-computed to speed up the successive
	 * operations.
	 * 
	 * @param name
	 *            name of the tag
	 * @param offst
	 *            offset, distance from the current position, negative is on the left.
	 */
	public FeatureDetail(final String name, final int offst,FeatureProperty fp,
			final Context context) {
		
		featureProperty =fp;
		
		offset = offst;
			
		isOnLeft = (offst < 0);

		isOnRight = (offst > 0);

		// compute distance
		if (isOnLeft) {
			distance = -1 * offst;
		} else {
			distance = offst;
		}

		// compute the additional feature index
		addFeatIndex = context.addTagsNames.indexOf(name);

		tagName = name;

		isWord = name.equalsIgnoreCase("W");

		isMainTag = name.equals(context.mainTag);

		// compute the name used in the feature description
		final String fDescName;
		if (isWord) {
			fDescName = "W";
		} else if (!isMainTag) {
			fDescName = "t" + name; //TODO remove the 't' update all the weight files
		} else {
			fDescName = "";
		}

		if (isOnLeft) {
			positionAndTagName = "|L" + distance + fDescName + ":";
		} else if (isOnRight) {
			positionAndTagName = "|R" + distance + fDescName + ":";
		} else {
			positionAndTagName = "|" + fDescName + ":";
		}
	}
	
	public String applyProperty(String form){
		if(featureProperty ==null){
			return form;
		}
		return featureProperty.applyProperty(form);
	}
	
}
