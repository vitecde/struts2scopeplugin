<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="ww" uri="/webwork" %>

<html>
<head>
    <title>Survey</title>
</head>

<body>
<h3>Welcome to a sample survey</h3>
<p>
  <ww:form action="SurveySave" method="post">
    <ww:textfield label="First Name" name="surveyBean.firstName"/>
    <ww:textfield label="Last Name" name="surveyBean.lastName"/>
    <ww:submit value="Next"/>
  </ww:form>
</p>
</body>
</html>