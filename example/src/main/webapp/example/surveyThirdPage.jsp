<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html>
<head>
    <title>Survey</title>
</head>

<body>
<p>First name: <s:property  value="surveyBean.firstName" /></p>
<p>Last name: <s:property value="surveyBean.lastName" /></p>
<p>Age: <s:property value="surveyBean.age" /></p>
<p>
  <s:form action="SurveySummaryPage">
    <s:textfield label="Birthdate" name="surveyBean.birthDate"/>
    <s:submit value="Next"/>
  </s:form>
</p>
</body>
</html>