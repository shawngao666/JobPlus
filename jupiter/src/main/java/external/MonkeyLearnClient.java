package external;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.monkeylearn.ExtraParam;
import com.monkeylearn.MonkeyLearn;
import com.monkeylearn.MonkeyLearnException;
import com.monkeylearn.MonkeyLearnResponse;

public class MonkeyLearnClient {

	private static final String API_KEY = "6ee1f1265dbb87fafee35188e6811abe37049cdb";

	public static void main(String[] args) {

		String[] textList = {
				"Elon Musk has shared a photo of the spacesuit designed by SpaceX. This is the second image shared of the new design and the first to feature the spacesuit’s full-body look.", };
		List<List<String>> words = extractKeywords(textList);
		for (List<String> ws : words) {
			for (String w : ws) {
				System.out.println(w);
			}
			System.out.println();
		}
	} // main method to test our code

	public static List<List<String>> extractKeywords(String[] text) {
		if (text == null || text.length == 0) {
			return new ArrayList<>();
		}

		MonkeyLearn ml = new MonkeyLearn(API_KEY);

		ExtraParam[] extraParams = { new ExtraParam("max_keywords", "3") };
		MonkeyLearnResponse response;

		try {
			response = ml.extractors.extract("ex_YCya9nrn", text, extraParams);
			JSONArray resultArray = response.arrayResult;
			return getKeywords(resultArray);
		} catch (MonkeyLearnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<>();

	}

	// "front end developer"
	// "black live matters"
	// "hello world"

	// <"front", "end", "developer">
	// <"black", "live", "matters">
	// <"hello", "world">

	private static List<List<String>> getKeywords(JSONArray mlResultArray) {
		List<List<String>> topKeywords = new ArrayList<>();

		for (int i = 0; i < mlResultArray.size(); ++i) { // scan every line of elements
			List<String> keywords = new ArrayList<>();
			JSONArray keywordsArray = (JSONArray) mlResultArray.get(i);
			// topKeywords.add(keywords); 放到这里 和 放到后面 效果是一样的 这里是java的特性: reference type

			for (int j = 0; j < keywordsArray.size(); ++j) { // scan every words
				JSONObject keywordObject = (JSONObject) keywordsArray.get(j);
				String keyword = (String) keywordObject.get("keyword");

				keywords.add(keyword); // put them into list
			}
			topKeywords.add(keywords); // put all of them into the final list
		}

		return topKeywords;
	}

}
