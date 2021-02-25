# Talend Component to evaluate DMN rules inside a Talend job
The underlaying API is Camunda DMN. It is Open Source ands freely usable.
At the moment a wrapper class is created and tested to use the Camunda DMN lib as Talend component.
Additional goal in the implementation is to take care misspelling of variables are detected and do not lead to simply null values.

## Processing
The processing is quite simple.
The component expects and input flow containing all input variables from the decision and an output flow containing all output variables of the decision.
You can configure in the component which column will be used for the output.

The component tries to feed the expected input variables of the decision with the incoming flow columns. 
If there are columnbs missing, the component will complain the missing column and fail. 
The component also checks if the expected output can be matched with the output of the decision definition. If a expected output column cannot be matched with a output variable of the decision, the component will fail with a meaningful error message. 

Variables not used for for the output are simply untouched by the component.

To define the decision you can choose a file (xml file with dmn-extension) or you can take the decision from a resource e.g. if you have the decision within a jar file.
You need to know the final decision table id and set this id in the setting decision-key.
![Here an simple example job](https://github.com/jlolling/talendcomp_tCamundaDMN/blob/master/doc/tCamundaDMN_2_tests_scenario.png)
