package de.jlo.talendcomp.camunda.dmn;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionLogic;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries;
import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableInputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableOutputImpl;
import org.camunda.bpm.dmn.engine.spi.DmnEngineMetricCollector;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

public class TestDMNEngine {

	@Test
	public void testFirstEngineExample() throws Exception {
		String season = "Winter";
		int guestCount = 2;
		VariableMap variables = Variables.putValue("season", season).putValue("guestCount", guestCount);

		DmnEngine dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();

		InputStream inputStream = TestDMNEngine.class.getResourceAsStream("/dish-decision.dmn11.dmn");

		try {
			DmnDecision decision = dmnEngine.parseDecision("decision", inputStream);

			// evaluate decision
			DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

			// print result
			String desiredDish = (String) result.getFirstResult().get("Dish");
			System.out.println("Dish Decision:\n\tI would recommend to serve: " + desiredDish);

		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				System.err.println("Could not close stream: " + e.getMessage());
			}
		}
	}

	@Test
	public void testReturnResults() throws Exception {
		String season = null; //"Winter";
		int guestCount = 2;
		Map<String, Object> variables = Variables.putValue("guestCount", guestCount).putValue("season", season);

		DmnEngine dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();

		InputStream inputStream = TestDMNEngine.class.getResourceAsStream("/dish-decision.dmn11.dmn");

		try {
			DmnDecision decision = dmnEngine.parseDecision("decision", inputStream);
			DmnDecisionLogic l = decision.getDecisionLogic();
			if (l instanceof DmnDecisionTableImpl) {
				DmnDecisionTableImpl dti = (DmnDecisionTableImpl) l;
				List<DmnDecisionTableInputImpl> listInputs = dti.getInputs();
				for (DmnDecisionTableInputImpl impl : listInputs) {
					System.out.println("input var: " + impl.getInputVariable());
					System.out.println("input name: " + impl.getName());
					System.out.println("input id: " + impl.getId());
					System.out.println("input expression: " + impl.getExpression().getExpression());
					System.out.println("input type: " + impl.getExpression().getTypeDefinition().getTypeName());
				}
				List<DmnDecisionTableOutputImpl> listOutputs = dti.getOutputs();
				for (DmnDecisionTableOutputImpl impl : listOutputs) {
					System.out.println("output var: " + impl.getOutputName());
					System.out.println("output name: " + impl.getName());
					System.out.println("output id: " + impl.getId());
					System.out.println("output type: " + impl.getTypeDefinition().getTypeName());
				}
			}
			// evaluate decision
			DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

			for (DmnDecisionRuleResult oneResult : result) {
				for (Map.Entry<String, Object> oneValue : oneResult.entrySet()) {
					String var = oneValue.getKey();
					Object value = oneValue.getValue();
					System.out.println(var + "=" + value);
				}
			}
			
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	@Test
	public void testReturnDRG() throws Exception {
		String season = "Summer";
		int guestCount = 2;
		VariableMap variables = Variables.putValue("season", season).putValue("guestCount", guestCount);

		DmnEngine dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
		DmnEngineMetricCollector collector = dmnEngine.getConfiguration().getEngineMetricCollector();

		InputStream inputStream = TestDMNEngine.class.getResourceAsStream("/dish-decision.dmn11.dmn");

		try {
			DmnDecision decision = dmnEngine.parseDecision("decision", inputStream);

			// evaluate decision
			DmnDecisionResult result = dmnEngine.evaluateDecision(decision, variables);

			for (DmnDecisionResultEntries oneResult : result) {
				for (Map.Entry<String, Object> oneValue : oneResult.entrySet()) {
					String var = oneValue.getKey();
					Object value = oneValue.getValue();
					System.out.println(var + "=" + value);
				}
			}
			System.out.println("executed elements=" + collector.getExecutedDecisionElements());
			System.out.println("executed instances=" + collector.getExecutedDecisionInstances());
			
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	@Test
	public void testReturnMultiRow() throws Exception {
		String product = "Product1";
		String type = "1";
		String grade = "4307";
		int width = 2000;
		VariableMap variables = Variables
								.putValue("product", product)
								.putValue("type", type)
								.putValue("grade", grade)
								.putValue("width", width);

		DmnEngine dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
		DmnEngineMetricCollector collector = dmnEngine.getConfiguration().getEngineMetricCollector();

		InputStream inputStream = TestDMNEngine.class.getResourceAsStream("/collect_sum_example.dmn");

		try {
			DmnDecision decision = dmnEngine.parseDecision("surcharge", inputStream);
			System.out.println("decision-key=" + decision.getKey());
			System.out.println("isDecisionTable=" + decision.isDecisionTable());
			
			// evaluate decision
			DmnDecisionResult result = dmnEngine.evaluateDecision(decision, variables);

			for (DmnDecisionResultEntries oneResult : result) {
				System.out.println("----------------------");
				for (Map.Entry<String, Object> oneValue : oneResult.entrySet()) {
					String var = oneValue.getKey();
					Object value = oneValue.getValue();
					System.out.println(var + "=" + value);
				}
			}
			System.out.println("==========================");
			System.out.println("executed elements=" + collector.getExecutedDecisionElements());
			System.out.println("executed instances=" + collector.getExecutedDecisionInstances());
			assertEquals(2, result.size());
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	@Test
	public void testDRG() throws Exception {
		String season = "Winter";
		int guestCount = 3;
		boolean guestsWithChildren = false;

		VariableMap variables = Variables
								.putValue("season", season)
								.putValue("guestCount", guestCount)
								.putValue("guestsWithChildren", guestsWithChildren);

		DmnEngine dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
		DmnEngineMetricCollector collector = dmnEngine.getConfiguration().getEngineMetricCollector();

		InputStream inputStream = TestDMNEngine.class.getResourceAsStream("/drg_dishes.dmn");

		try {
			DmnDecision decision = dmnEngine.parseDecision("beverages", inputStream);
			System.out.println("drg-key=" + decision.getKey());
			
			// evaluate decision
			DmnDecisionResult result = dmnEngine.evaluateDecision(decision, variables);

			for (DmnDecisionResultEntries oneResult : result) {
				System.out.println("----------------------");
				for (Map.Entry<String, Object> oneValue : oneResult.entrySet()) {
					String var = oneValue.getKey();
					Object value = oneValue.getValue();
					System.out.println(var + "=" + value);
				}
			}
			System.out.println("==========================");
			System.out.println("executed elements=" + collector.getExecutedDecisionElements());
			System.out.println("executed instances=" + collector.getExecutedDecisionInstances());
			assertEquals(1, result.size());
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

}
