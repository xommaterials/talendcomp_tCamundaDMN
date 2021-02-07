# Talend Component to evaluate DMN rules inside a Talend job
The underlaying API is Camunda DMN.
At the moment a wrapper class is created and tested to use the Camunda DMN lib as Talend component.
Additional goal in the implementation is to take care misspelling of variables are detected and do not lead to simply null values.
