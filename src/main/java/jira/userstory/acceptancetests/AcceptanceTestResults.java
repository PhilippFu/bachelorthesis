package jira.userstory.acceptancetests;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AcceptanceTestResults extends AbstractJiraContextProvider {

    @Override
    public Map<String, Object> getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {

        Map<String, Object> contextMap = new HashMap<>();
        Map<String, Object> resultsMap = new HashMap<>();
        Map<Integer, Object> scenarioMap = new HashMap<>();

        Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");
        String currentIssueKey = currentIssue.getKey();

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("C:/Program Files (x86)/Jenkins/workspace/Generate Cucumber-Reports/target/" + currentIssueKey + ".json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONArray elementsList = (JSONArray) obj;

            //Iterate over JSONObject
            elementsList.forEach( ele -> parseElementObject( (JSONObject) ele , contextMap, resultsMap, scenarioMap) );

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return contextMap;
    }

    public static void parseElementObject (JSONObject elements, Map<String, Object> contextMap, Map<String, Object> resultsMap, Map<Integer, Object> scenarioMap) {

        String featureName = (String) elements.get("name");
        JSONArray elementsArray = (JSONArray) elements.get("elements");
        contextMap.put ("featureName", featureName);

        for (int i = 0; i < elementsArray.size(); i++) {

            JSONObject scenario = (JSONObject) elementsArray.get(i);
            String scenarioName = (String) scenario.get("name");
            scenarioMap.put(i, scenarioName);
            resultsMap.put("result"+i, "undefined");

            JSONArray stepsArray = (JSONArray) scenario.get("steps");

            for (int e = 0; e < stepsArray.size(); e++) {

                JSONObject steps = (JSONObject) stepsArray.get(e);
                JSONObject results = (JSONObject) steps.get("result");
                String stepStatus = (String) results.get("status");

                if(stepStatus.equals("failed")){
                    resultsMap.put("result"+i, stepStatus);
                }else if(stepStatus.equals("passed") && !resultsMap.get("result"+i).equals("failed")){
                        resultsMap.put("result" + i, stepStatus);
                }
            }
        }
        contextMap.put("resultsMap", resultsMap);
        contextMap.put("scenarioMap", scenarioMap);
    }
}
