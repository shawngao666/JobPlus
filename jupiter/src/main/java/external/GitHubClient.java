package external;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class GitHubClient {
	private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
	private static final String DEFAULT_KEYWORD = "developer";

	public List<Item> search(double lat, double lon, String keyword) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8"); // transform the keyword
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // surround with try catch
		String url = String.format(URL_TEMPLATE, keyword, lat, lon); // define URL

		CloseableHttpClient httpclient = HttpClients.createDefault(); // define Client
		HttpGet httpget = new HttpGet(url); // // define HttpGet

		ResponseHandler<List<Item>> responseHandler = new ResponseHandler<List<Item>>() {

			@Override
			public List<Item> handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();

				if (status != 200) {
					return new ArrayList<>();
				}
				HttpEntity entity = response.getEntity();
				if (entity == null) {
					return new ArrayList<>();
				}
				String responseBody = EntityUtils.toString(entity); // read response body as String
				JSONArray array = new JSONArray(responseBody);
				return getItemList(array);

			}
		};
		try {
			return httpclient.execute(httpget, responseHandler);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return new ArrayList<>();
	}

	// convert JSONArray to a list of item objects
	// NOW we get the results from the github server
	private List<Item> getItemList(JSONArray array) throws JSONException {
		List<Item> itemList = new ArrayList<>();
		List<String> descriptionList = new ArrayList<>();

		for (int i = 0; i < array.length(); i++) {
			// We need to extract keywords from description since GitHub API
			// doesn't return keywords.
			String description = getStringFieldOrEmpty(array.getJSONObject(i), "description"); // 把description提取出来成一个String
			if (description.equals("") || description.equals("\n")) {
				descriptionList.add(getStringFieldOrEmpty(array.getJSONObject(i), "title")); //如果没有description,我们就把title放进来
			} else {
				descriptionList.add(description); // 把所有的description都变成各个String然后存进一个String[] array里面。
			} // 现在的 String array 就可以当作input 放进我们的 monkey learn API里面了。
		}

		// We need to get keywords from multiple text in one request since
		// MonkeyLearnAPI has limitations on request per minute.
		List<List<String>> keywords = MonkeyLearnClient
				.extractKeywords(descriptionList.toArray(new String[descriptionList.size()])); // monkey learn要求传入一个Array
		
		// 用List的话，是ordered的
		// set 是没有order的。 是unique的。 

		for (int i = 0; i < array.length(); ++i) {
			JSONObject object = array.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder();

			builder.setItemId(getStringFieldOrEmpty(object, "id"));
			builder.setName(getStringFieldOrEmpty(object, "title"));
			builder.setAddress(getStringFieldOrEmpty(object, "location"));
			builder.setUrl(getStringFieldOrEmpty(object, "url"));
			builder.setImageUrl(getStringFieldOrEmpty(object, "company_logo"));

			builder.setKeywords(new HashSet<String>(keywords.get(i))); //因为API要求set，需要一个unordered and unique的set。
			
			Item item = builder.build();
			itemList.add(item);
		}

		return itemList;
	}

	private String getStringFieldOrEmpty(JSONObject obj, String field) {
		return obj.isNull(field) ? "" : obj.getString(field);
	}

}
