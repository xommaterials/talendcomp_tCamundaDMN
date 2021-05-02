package de.jlo.talendcomp.camunda.dmn;

import java.util.Collection;


import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.dmn.instance.Text;
import org.camunda.bpm.model.xml.type.ModelElementType;

public class DmnModelCreator {

  public static void main(String[] args) {
    DmnModelInstance modelInstance = Dmn.createEmptyModel();
    Definitions definitions = modelInstance.newInstance(Definitions.class);
    definitions.setNamespace("http://camunda.org/schema/1.0/dmn");
    definitions.setName("definitions");
    definitions.setId("definitions");
    modelInstance.setDefinitions(definitions);
    
    Decision decision = modelInstance.newInstance(Decision.class);
    decision.setId("testGenerated");
    decision.setName("generationtest");
    definitions.addChildElement(decision);
    
    DecisionTable decisionTable = modelInstance.newInstance(DecisionTable.class);
    decisionTable.setId("decisionTable");
    decisionTable.setHitPolicy(HitPolicy.UNIQUE);
    decision.addChildElement(decisionTable);
    
    Input jahreszeitInput = modelInstance.newInstance(Input.class);
    jahreszeitInput.setId("Input_1");
    jahreszeitInput.setLabel("Season");
    
    InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
    inputExpression.setId("InputExpression_1");
    inputExpression.setTypeRef("string");
    Text text = modelInstance.newInstance(Text.class);
    text.setTextContent("season");
    inputExpression.setText(text);
    jahreszeitInput.addChildElement(inputExpression);
    decisionTable.addChildElement(jahreszeitInput);
    
    Input anzahlGaesteInput = modelInstance.newInstance(Input.class);
    anzahlGaesteInput.setId("Input_2");
    anzahlGaesteInput.setLabel("Number of guests");
    
    InputExpression inputExpression2 = modelInstance.newInstance(InputExpression.class);
    inputExpression2.setId("InputExpression_2");
    inputExpression2.setTypeRef("integer");
    Text text3 = modelInstance.newInstance(Text.class);
    text3.setTextContent("guestCount");
    inputExpression2.setText(text3);
    anzahlGaesteInput.addChildElement(inputExpression2);
    decisionTable.addChildElement(anzahlGaesteInput);
    
    Output output = modelInstance.newInstance(Output.class);
    output.setId("Output_1");
    output.setLabel("Dish");
    output.setName("dish");
    output.setTypeRef("string");
    decisionTable.addChildElement(output);
    
    Collection<Input> ins = decisionTable.getChildElementsByType(Input.class);
    for (Input in : ins) {
    	System.out.println(in.getId());
    }
    Collection<Output> outs = decisionTable.getChildElementsByType(Output.class);
    for (Output out : outs) {
    	System.out.println(out.getId());
    }
    
    Rule rule = modelInstance.newInstance(Rule.class);
    rule.setId("Rule_1");
    Text text1 = modelInstance.newInstance(Text.class);
    text1.setTextContent("\"Summer\"");
    InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
    inputEntry.setId("input_3");
    inputEntry.addChildElement(text1);
    
    rule.addChildElement(inputEntry);
    
    Text text4 = modelInstance.newInstance(Text.class);
    text4.setTextContent("<4");
    InputEntry inputEntry2 = modelInstance.newInstance(InputEntry.class);
    inputEntry2.setId("input_4");
    inputEntry2.addChildElement(text4);
    
    rule.addChildElement(inputEntry2);
    
    OutputEntry outputEntry = modelInstance.newInstance(OutputEntry.class);
    outputEntry.setId("output_2");
    
    Text text2 = modelInstance.newInstance(Text.class);
    text2.setTextContent("\"Strawberries\"");
    outputEntry.addChildElement(text2);
    
    rule.addChildElement(outputEntry);
    
    decisionTable.addChildElement(rule);
    
    Rule rule2 = modelInstance.newInstance(Rule.class);
    Text text5 = modelInstance.newInstance(Text.class);
    text5.setTextContent("\"Summer\"");
    InputEntry inputEntry1 = modelInstance.newInstance(InputEntry.class);
    inputEntry1.setId("input_1");
    inputEntry1.addChildElement(text5);
    
    rule2.addChildElement(inputEntry1);
    
    Text text41 = modelInstance.newInstance(Text.class);
    text41.setTextContent(">=4");
    InputEntry inputEntry21 = modelInstance.newInstance(InputEntry.class);
    inputEntry21.setId("input_2");
    inputEntry21.addChildElement(text41);
    
    rule2.addChildElement(inputEntry21);
    
    OutputEntry outputEntry1 = modelInstance.newInstance(OutputEntry.class);
    outputEntry1.setId("output_1");
    
    Text text21 = modelInstance.newInstance(Text.class);
    text21.setTextContent("\"Icecream\"");
    outputEntry1.addChildElement(text21);
    
    rule2.addChildElement(outputEntry1);
    decisionTable.addChildElement(rule2);
    
    
    Dmn.validateModel(modelInstance);
    
    System.out.println(Dmn.convertToString(modelInstance));
    
    DmnEngine dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
    
    DmnDecision decision2 = dmnEngine.parseDecision("testGenerated", modelInstance);
    
    VariableMap variables = Variables
        .createVariables()
        .putValue("season", "Summer")
        .putValue("guestCount", 9);
    DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision2, variables);
    
    System.out.println(result.toString());
    
    Collection<Input> inputs = modelInstance.getModelElementsByType(Input.class);
    for (Input input2 : inputs) {
      System.out.println("" + input2.getRawTextContent());
    }
    System.out.println();
    Collection<InputEntry> inputEntries = modelInstance.getModelElementsByType(InputEntry.class);
    for (InputEntry inputEntry3 : inputEntries) {
      System.out.println("" + inputEntry3.getRawTextContent());
      
    }    
  }

}
