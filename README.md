# Talend Component to evaluate DMN rules inside a Talend job
The underlaying API is Camunda DMN.
At the moment a wrapper class is created and tested to use the Camunda DMN lib as Talend component.
Additional goal in the implementation is to take care misspelling of variables are detected and do not lead to simply null values.

The processing is quite simple.
The component expects and input flow containing all input variables from the decision and an output flow containing all output variables of the decision.
You can configure in the component which column will be used for the input or for the output.
Variables not used for both are simply untouched by the component.

To define the decision you can choose a file (xml file with dmn-extension) or you can take the decision from a resource e.g. if you have the decision within a jar file.
You need to know the final decision table id and set this id in the setting decision-key.
![Here an simple example job](https://github.com/jlolling/talendcomp_tCamundaDMN/blob/master/doc/tCamundaDMN_2_tests_scenario.png)
