<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html>
<head>
    <title>Survey</title>
</head>

<body>
<h3>Welcome to a sample survey</h3>
<p>
  <s:form action="SurveySecondPage">
    <s:textfield label="First Name" name="surveyBean.firstName"/>
    <s:textfield label="Last Name" name="surveyBean.lastName"/>
    <s:submit value="Next"/>
  </s:form>
</p>
</body>
</html>