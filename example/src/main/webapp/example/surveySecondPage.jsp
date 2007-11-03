<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html>
<head>
    <title>Survey</title>
</head>

<body>
<p>First name: <s:property  value="surveyBean.firstName" /></p>
<p>Last name: <s:property value="surveyBean.lastName" /></p>
<p>
  <s:form action="SurveyThirdPage">
    <s:textfield label="Age" name="surveyBean.age"/>
    <s:submit value="Next"/>
  </s:form>
</p>
</body>
</html>