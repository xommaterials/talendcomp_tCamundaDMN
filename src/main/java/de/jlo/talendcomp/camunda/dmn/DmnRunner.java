/**
 * Copyright 2021 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.jlo.talendcomp.camunda.dmn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionLogic;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableInputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * Utility to run DMN decisions inside Talend
 * @author jan.lolling@gmail.com
 */
public class DmnRunner {
	
	private DmnEngine dmnEngine = null;
	private DmnDecision decision = null;
	private VariableMap variables = null;
	private DmnDecisionResult resultset = null;
	private DmnDecisionResultEntries oneResult = null;
	private boolean useCachedDecision = false;
	private static Map<String, DmnDecision> decisionMap = new HashMap<>();
	private List<DmnDecisionTableInputImpl> listDecisionTableInputs = null;
	private List<DmnDecisionTableOutputImpl> listDecisionTableOutputs = null;
	private List<String> listTalendIncomingColumns = new ArrayList<>();
	private List<String> listTalendOutgoingColumns = new ArrayList<>();
	private boolean provideOneRecordIfNoDecsionResult = false;
	private int currentResultIndex = -1;
	
	public DmnRunner() {
		dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
	}
	
	/**
	 * load the DMN rules from a resource
	 * @param decisionKey the decision key of the final decision table of the DMN
	 * @param resourceName
	 * @throws Exception
	 */
	public void loadDmnFromResource(String decisionKey, String resourceName) throws Exception {
		if (isEmpty(decisionKey)) {
			throw new IllegalArgumentException("decisionKey cannot be null or empty");
		}
		if (isEmpty(resourceName)) {
			throw new IllegalArgumentException("resourceName cannot be null or empty");
		}
		if (useCachedDecision) {
			decision = decisionMap.get(resourceName+"/"+decisionKey);
		}
		if (decision == null) {
			InputStream in = null;
			try {
				in = DmnRunner.class.getResourceAsStream(resourceName);
				if (in == null) {
					throw new Exception("Resource: " + resourceName + " not available");
				}
				decision = dmnEngine.parseDecision(decisionKey, in);
				if (useCachedDecision) {
					decisionMap.put(resourceName+":"+decisionKey, decision);
				}
				inspectDecisionIO();
			} catch (Exception e) {
				throw new Exception("Load decision with key: " + decisionKey + " from resource: " + resourceName + " failed: " + e.getMessage(), e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException ioe) {}
				}
			}
		}
	}

	/**
	 * load the DMN rules from a file
	 * @param decisionKey the decision key of the final decision table of the DMN
	 * @param path the path to the file
	 * @throws Exception
	 */
	public void loadDmnFromFile(String decisionKey, String path) throws Exception {
		if (isEmpty(decisionKey)) {
			throw new IllegalArgumentException("decisionKey cannot be null or empty");
		}
		if (isEmpty(path)) {
			throw new IllegalArgumentException("path cannot be null or empty");
		}
		if (useCachedDecision) {
			decision = decisionMap.get(path+"/"+decisionKey);
		}
		if (decision == null) {
			InputStream in = null;
			try {
				File test = new File(path);
				if (test.canRead() == false) {
					throw new Exception("File: " + test.getAbsolutePath() + " does not exist or cannot be read");
				}
				in = Files.newInputStream(Paths.get(path), StandardOpenOption.READ);
				if (in == null) {
					throw new Exception("File with path: " + path + " cannot be read");
				}
				decision = dmnEngine.parseDecision(decisionKey, in);
				if (useCachedDecision) {
					decisionMap.put(path+":"+decisionKey, decision);
				}
				inspectDecisionIO();
			} catch (Exception e) {
				throw new Exception("Load decision with key: " + decisionKey + " from path: " + path + " failed: " + e.getMessage(), e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException ioe) {}
				}
			}
		}
	}
	
	private void inspectDecisionIO() {
		if (decision == null) {
			throw new IllegalStateException("Decision not loaded");
		}
		DmnDecisionLogic l = decision.getDecisionLogic();
		if (l instanceof DmnDecisionTableImpl) {
			DmnDecisionTableImpl dti = (DmnDecisionTableImpl) l;
			listDecisionTableInputs = dti.getInputs();
			listDecisionTableOutputs = dti.getOutputs();
		}
	}
	
	/**
	 * clears the former values. Must be performed before loading a new schema record
	 */
	public void clearVariables() {
		if (variables != null) {
			variables.clear();
			variables = null;		
		}
		resultset = null;
	}

	/**
	 * adds a new input value
	 * @param variableName
	 * @param value
	 */
	public void addInputValue(String variableName, Object value) {
		if (isEmpty(variableName)) {
			throw new IllegalArgumentException("variableName cannot be null or empty");
		}
		if (variables == null) {
			variables = Variables.putValue(variableName, value);
		} else {
			variables.putValue(variableName, value);
		}
	}
	
	/**
	 * checks if the input variables from the decision have corresponding incoming schema columns
	 * @throws Exception if it is not the case
	 */
	public void validateInputVariables() throws Exception {
		if (listTalendIncomingColumns == null) {
			throw new Exception("No Talend schema variables as input are set!");
		}
		if (listDecisionTableInputs != null) {
			StringBuilder sb = new StringBuilder();
			for (DmnDecisionTableInputImpl input : listDecisionTableInputs) {
				String key = null;
				DmnExpressionImpl ex = input.getExpression();
				if (ex != null) {
					key = ex.getExpression();
				}
				if (isEmpty(key)) {
					key = input.getInputVariable();
				}
				if (listTalendIncomingColumns.contains(key) == false) {
					if (sb.length() > 0) {
						sb.append("\n");
					}
					sb.append("Decisions input: " + key + " has no corresponding Talend incoming schema column");
				}
			}
			if (sb.length() > 0) {
				throw new Exception(sb.toString());
			}
		}
	}
	
	/**
	 * add a Talend schema output variable for the validation for later validation
	 * @param schemaOutputColumn
	 */
	public void addExpectedOutputVariable(String schemaOutputColumn) {
		if (isEmpty(schemaOutputColumn)) {
			throw new IllegalArgumentException("schemaOutputColumn cannot be null or empty");
		}
		listTalendOutgoingColumns.add(schemaOutputColumn);
	}
	
	/**
	 * checks if the Talend output variables have a corresponding output variable in the decision
	 * @throws Exception if it is not the case
	 */
	public void validateOutputVariables() throws Exception {
		if (listTalendOutgoingColumns == null) {
			throw new Exception("No Talend schema variables as output are set!");
		}
		if (listDecisionTableOutputs != null) {
			StringBuilder sb = new StringBuilder();
			for (String name : listTalendOutgoingColumns) {
				boolean exists = false;
				for (DmnDecisionTableOutputImpl output : listDecisionTableOutputs) {
					String key = output.getOutputName();
					if (name.equals(key)) {
						exists = true;
					}
				}
				if (exists == false) {
					if (sb.length() > 0) {
						sb.append("\n");
					}
					sb.append("Talend outgoing schema column: " + name + " has no output variable within the decsion");
				}
			}
			if (sb.length() > 0) {
				throw new Exception(sb.toString());
			}
		}
	}
	
	/**
	 * Evaluates the decision with the former set variables
	 * @throws Exception
	 */
	public void evaluate() throws Exception {
		try {
			resultset = dmnEngine.evaluateDecision(decision, variables);
		} catch (Exception e) {
			throw new Exception("Evaluating decision: " + decision.getName() + " and variables: " + variables + " failed: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Iterate through the result-set
	 * @return true if there is a new result record
	 * @throws Exception
	 */
	public boolean next() throws Exception {
		if (resultset == null) {
			return false;
		} else if (resultset.size() == 0) {
			if (provideOneRecordIfNoDecsionResult) {
				return true;
			}
		}
		if (currentResultIndex < resultset.size()) {
			oneResult = resultset.get(currentResultIndex++);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns a output value for the current result record
	 * @param outgoingSchemaColumn
	 * @return the value
	 */
	public Object getOutputValue(String outgoingSchemaColumn) {
		if (oneResult == null) {
			if (provideOneRecordIfNoDecsionResult) {
				return null;
			} else {
				throw new IllegalStateException("We expect to have one result record but there is no one. Did you have called next and set option provideOneRecordIfNoDecsionResult correctly?");
			}
		} else {
			Object value = oneResult.get(outgoingSchemaColumn);
			return value;
		}
	}
	
	/**
	 * returns true if the string is null or empty or equals "null"
	 * @param s the string
	 * @returns true if empty 
	 */
	public static boolean isEmpty(String s) {
		if (s == null) {
			return true;
		}
		if (s.trim().isEmpty()) {
			return true;
		}
		if (s.trim().equalsIgnoreCase("null")) {
			return true;
		}
		return false;
	}

	public boolean isProvideOneRecordIfNoDecsionResult() {
		return provideOneRecordIfNoDecsionResult;
	}

	/**
	 * set true to take care we get per incoming record an outgoing record in the component
	 * @param provideOneRecordIfNoDecsionResult
	 */
	public void setProvideOneRecordIfNoDecsionResult(boolean provideOneRecordIfNoDecsionResult) {
		this.provideOneRecordIfNoDecsionResult = provideOneRecordIfNoDecsionResult;
	}


}
